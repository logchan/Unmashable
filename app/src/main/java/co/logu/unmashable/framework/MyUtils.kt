package co.logu.unmashable.framework

import android.util.Base64

class MyUtils {
    companion object {
        fun toBase64(bytes: ByteArray) : String {
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        fun fromBase64(string: String) : ByteArray {
            return Base64.decode(string, Base64.DEFAULT)
        }

        fun toHexString(bytes: ByteArray) : String {
            val sb = StringBuilder()
            bytes.forEach {
                sb.append(String.format("%02X", it))
            }
            return sb.toString()
        }
    }
}