package co.logu.unmashable.model

import android.bluetooth.le.*
import android.os.Handler
import co.logu.unmashable.framework.MyUtils
import co.logu.unmashable.log.AppLog
import java.util.*

class HandshakeScanner(member: GroupMember, ble: BleWrapper) : HandshakeParty(member, ble) {

    private var cred = ByteArray(32)
    private var listener: OnHandshakeEventListener? = null
    private val timeoutHandler = Handler()
    private var step = 0

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val data1 = result?.scanRecord?.getManufacturerSpecificData(ADVERTISER_MID)
            val data2 = result?.scanRecord?.getManufacturerSpecificData(ADVERTISER_MID_2)
            if (data1 == null) {
                return
            }

            when (step) {
                0 -> {
                    if (data2 == null) {
                        return
                    }

                    AppLog.d("scanner step 0")
                    timeoutHandler.postDelayed({
                        listener?.onHandshakeFailure("operation timeout")
                        stop()
                    }, HANDSHAKE_TIMEOUT)

                    //AppLog.d("scanner found advertiser data $callbackType ${MyUtils.toHexString(data1)} (${data1.size}) ${MyUtils.toHexString(data2)} (${data2.size})")
                    if (data1.size + data2.size != 32) {
                        listener?.onHandshakeFailure("advertiser data is not 32 bytes")
                        stop()
                    }

                    member.generateSessionKey(data1 + data2)
                    Thread.sleep(100)
                    //AppLog.d("scanner session key: ${MyUtils.toHexString(member.getSessionKey())}")

                    val ch = encrypt(member.getSessionKey(), challenge)
                    if (ch.isEmpty()) {
                        listener?.onHandshakeFailure("challenge encryption error")
                        stop()
                    }

                    ble.startAdvertising(
                            AdvertiseData.Builder()
                                    .addManufacturerData(SCANNER_MID, cred.copyOfRange(0, FIRST_PART_SIZE))
                                    .build(),
                            AdvertiseData.Builder()
                                    .addManufacturerData(SCANNER_MID_2, cred.copyOfRange(FIRST_PART_SIZE, 32) + ch)
                                    .build(),
                            null)
                    step = 1
                }
                1 -> {
                    if (data2 != null) {
                        return
                    }

                    AppLog.d("scanner step 1")
                    ble.stopAdvertising()
                    ble.stopScanning()

                    if (data1.size != 24) {
                        listener?.onHandshakeFailure("advertiser resp is not 24 bytes")
                    }

                    if (checkResponse(data1.copyOfRange(16, 24))) {
                        val resp = decrypt(member.getSessionKey(), data1.copyOfRange(0, 16))
                        if (resp.isEmpty()) {
                            listener?.onHandshakeFailure("resp decryption error")
                        }
                        else {
                            Thread.sleep(100)
                            ble.startAdvertising(
                                    AdvertiseData.Builder()
                                            .addManufacturerData(SCANNER_MID, resp.copyOfRange(8, 16))
                                            .build(),
                                    null,
                                    advertiseCallback2)
                        }
                    }
                    else {
                        listener?.onHandshakeFailure("advertiser resp incorrect")
                    }
                }
            }
        }
    }

    private val advertiseCallback2 = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            timeoutHandler.removeCallbacksAndMessages(null)

            Handler().postDelayed({
                listener?.onHandshakeSuccess()
                stop()
            }, 4000)
        }
    }

    override fun start(listener: OnHandshakeEventListener?) {
        this.listener = listener
        step = 0

        member.newIdentity()
        Thread.sleep(100)

        cred = member.getPublicCredentials()
        // AppLog.d("scanner start with ${MyUtils.toHexString(cred)}")
        if (cred.size != 32) {
            listener?.onHandshakeFailure("credential size is not 32 bytes")
            stop()
        }

        createChallenge()
        ble.startScanning(ADVERTISER_MID, scanCallback)
    }

    override fun stop() {
        timeoutHandler.removeCallbacksAndMessages(null)
        listener = null
        ble.stopScanning()
        ble.stopAdvertising()
    }
}