package co.logu.unmashable.model

import android.bluetooth.le.*
import android.os.Handler
import co.logu.unmashable.framework.MyUtils
import co.logu.unmashable.log.AppLog

class HandshakeAdvertiser(member: GroupMember, ble: BleWrapper) : HandshakeParty(member, ble) {

    private var listener: OnHandshakeEventListener? = null
    private val timeoutHandler = Handler()
    private var step = 0

    private val advertiseCallback1 = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            ble.startScanning(SCANNER_MID, scanCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val data1 = result?.scanRecord?.getManufacturerSpecificData(SCANNER_MID)
            val data2 = result?.scanRecord?.getManufacturerSpecificData(SCANNER_MID_2)

            if (data1 == null) {
                return
            }

            when (step) {
                0 -> {
                    if (data2 == null) {
                        return
                    }

                    AppLog.d("advertiser step 0")
                    ble.stopAdvertising()

                    timeoutHandler.postDelayed({
                        listener?.onHandshakeFailure("operation timeout")
                        stop()
                    }, HANDSHAKE_TIMEOUT)

                    // AppLog.d("advertiser found scanner data $callbackType ${MyUtils.toHexString(data)} ${MyUtils.toHexString(data2)}")
                    if (data1.size + data2.size != 48) {
                        AppLog.e("scanner data is not 48 bytes")
                        return
                    }

                    member.generateSessionKey(data1 + data2.copyOfRange(0, 32 - FIRST_PART_SIZE))
                    Thread.sleep(100)
                    // AppLog.d("advertiser session key: ${MyUtils.toHexString(member.getSessionKey())}")

                    val resp = decrypt(member.getSessionKey(), data2.copyOfRange(32 - FIRST_PART_SIZE, data2.size))
                    if (resp.isEmpty()) {
                        listener?.onHandshakeFailure("resp decryption error")
                        stop()
                    }

                    val ch = encrypt(member.getSessionKey(), challenge)
                    if (ch.isEmpty()) {
                        listener?.onHandshakeFailure("challenge encryption error")
                        stop()
                    }

                    ble.startAdvertising(
                            AdvertiseData.Builder()
                                    .addManufacturerData(ADVERTISER_MID, ch + resp.copyOfRange(8, 16))
                                    .build(),
                            null,
                            null)

                    step = 1
                }
                1 -> {
                    if (data2 != null) {
                        return
                    }

                    AppLog.d("advertiser step 1")
                    ble.stopAdvertising()
                    ble.stopScanning()

                    if (data1.size != 8) {
                        listener?.onHandshakeFailure("scanner resp is not 8 bytes")
                    }

                    if (checkResponse(data1)) {
                        listener?.onHandshakeSuccess()
                    }
                    else {
                        listener?.onHandshakeFailure("scanner resp incorrect")
                    }

                    stop()
                }
            }
        }
    }

    override fun start(listener: OnHandshakeEventListener?) {
        this.listener = listener
        step = 0

        member.newIdentity()
        Thread.sleep(100) // JPBC library needs this

        val cred = member.getPublicCredentials()
        AppLog.d("advertiser start with ${MyUtils.toHexString(cred)}")
        if (cred.size != 32) {
            AppLog.e("credential size is not 32 bytes")
            return
        }

        createChallenge()
        ble.startAdvertising(
                AdvertiseData.Builder()
                        .addManufacturerData(ADVERTISER_MID, cred.copyOfRange(0, FIRST_PART_SIZE))
                        .build(),
                AdvertiseData.Builder()
                        .addManufacturerData(ADVERTISER_MID_2, cred.copyOfRange(FIRST_PART_SIZE, 32))
                        .build(),
                advertiseCallback1)
    }

    override fun stop() {
        timeoutHandler.removeCallbacksAndMessages(null)
        listener = null
        ble.stopAdvertising()
        ble.stopScanning()
    }
}