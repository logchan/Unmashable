package co.logu.unmashable.model

import co.logu.unmashable.log.AppLog
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

abstract class HandshakeParty (protected val member: GroupMember, protected val ble: BleWrapper) {

    private val rng = Random()
    protected val challenge = ByteArray(16)

    companion object {
        const val ADVERTISER_MID = 0xBEEF
        const val ADVERTISER_MID_2 = 0xBEEA
        const val SCANNER_MID = 0xDEAF
        const val SCANNER_MID_2 = 0xDEAA

        const val FIRST_PART_SIZE = 24
        const val HANDSHAKE_TIMEOUT: Long = 15000

        private fun cipherTransform(sessionKey: ByteArray, data: ByteArray, mode: Int): ByteArray {
            if (sessionKey.size != 32 || data.size != 16) {
                return ByteArray(0)
            }

            try {
                val cipher = Cipher.getInstance("AES/CBC/NoPadding")
                cipher.init(mode, SecretKeySpec(sessionKey, 0, sessionKey.size, "AES"), IvParameterSpec(sessionKey, 0, 16))
                return cipher.doFinal(data, 0, data.size)
            }
            catch (ex: Throwable) {
                AppLog.e("cipher transform error ${ex.message}")
                return ByteArray(0)
            }
        }

        fun encrypt(sessionKey: ByteArray, data: ByteArray) : ByteArray {
            return cipherTransform(sessionKey, data, Cipher.ENCRYPT_MODE)
        }

        fun decrypt(sessionKey: ByteArray, data: ByteArray) : ByteArray {
            return cipherTransform(sessionKey, data, Cipher.DECRYPT_MODE)
        }
    }

    interface OnHandshakeEventListener {
        fun onHandshakeSuccess()
        fun onHandshakeFailure(reason: String)
    }

    abstract fun start(listener: OnHandshakeEventListener?)
    abstract fun stop()

    protected fun createChallenge() {
        for (i in 0..15) {
            challenge[i] = rng.nextInt(255).toByte()
        }
    }

    protected fun checkResponse(resp: ByteArray): Boolean {
        for (i in 8..15) {
            if (challenge[i] != resp[i - 8]) {
                return false
            }
        }
        return true
    }
}