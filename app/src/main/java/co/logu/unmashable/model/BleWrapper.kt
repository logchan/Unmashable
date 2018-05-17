package co.logu.unmashable.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.os.ParcelUuid
import co.logu.unmashable.log.AppLog

class BleWrapper(private val adapter: BluetoothAdapter) {

    enum class BleWrapperStatus {
        Idle,
        Starting,
        Running
    }

// region advertiser
    private val advertiser : BluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser!!
    private var advertiserStatus: BleWrapperStatus = BleWrapperStatus.Idle
    private var advertiserCallback: AdvertiseCallback? = null

    fun getAdvertiserStatus() : BleWrapperStatus {
        return advertiserStatus
    }

    fun stopAdvertising() {
        if (advertiserStatus != BleWrapperStatus.Running) {
            return
        }

        advertiserStatus = BleWrapperStatus.Starting
        advertiser.stopAdvertising(advertiserCallback)
        advertiserStatus = BleWrapperStatus.Idle
    }

    fun startAdvertising(data: AdvertiseData, scanResp: AdvertiseData?, callback: AdvertiseCallback?) {
        if (advertiserStatus != BleWrapperStatus.Idle) {
            return
        }
        advertiserStatus = BleWrapperStatus.Starting

        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

        advertiserCallback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                advertiserStatus = BleWrapperStatus.Idle

                AppLog.e("startAdvertising onStartFailure $errorCode")
                callback?.onStartFailure(errorCode)
            }

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                advertiserStatus = BleWrapperStatus.Running

                AppLog.d("startAdvertising onStartSuccess")
                callback?.onStartSuccess(settingsInEffect)
            }
        }

        if (scanResp != null) {
            advertiser.startAdvertising(settings, data, scanResp, advertiserCallback)
        }
        else {
            advertiser.startAdvertising(settings, data, advertiserCallback)
        }
    }
// endregion

// region scanner

    private val scanner = adapter.bluetoothLeScanner!!
    private var scannerStatus = BleWrapperStatus.Idle
    private var scannerCallback: ScanCallback? = null
    private val zeros = ByteArray(1, { 0.toByte() })

    fun getScannerStatus(): BleWrapperStatus {
        return scannerStatus
    }

    fun stopScanning() {
        if (scannerStatus != BleWrapperStatus.Running) {
            return
        }

        scannerStatus = BleWrapperStatus.Starting
        scanner.stopScan(scannerCallback)
        scannerStatus = BleWrapperStatus.Idle
    }

    fun startScanning(id: Int, callback: ScanCallback?) {
        if (scannerStatus != BleWrapperStatus.Idle) {
            return
        }

        scannerStatus = BleWrapperStatus.Starting

        val filter = listOf(ScanFilter.Builder()
                .setManufacturerData(id, zeros, zeros)
                .build())

        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()

        scannerCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                AppLog.d("startScanning onScanFailed $errorCode")
                callback?.onScanFailed(errorCode)
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                callback?.onScanResult(callbackType, result)
            }
        }
        scanner.startScan(filter, settings, scannerCallback)
        scannerStatus = BleWrapperStatus.Running
    }
// endregion
}