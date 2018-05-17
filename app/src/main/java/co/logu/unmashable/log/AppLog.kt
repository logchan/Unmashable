package co.logu.unmashable.log

import android.util.Log
import java.util.*

const val LOG_TAG = "AppLog"

class AppLog {
    companion object {
        val logs = mutableListOf<AppLogEntry>()

        private fun addLog(msg: String, l: AppLogLevel) {
            logs.add(AppLogEntry().apply {
                time = Date()
                message = msg
                level = l
            })
        }

        fun d(msg: String) {
            addLog(msg, AppLogLevel.Debug)
            Log.d(LOG_TAG, msg)
        }

        fun i(msg: String) {
            addLog(msg, AppLogLevel.Information)
            Log.i(LOG_TAG, msg)
        }

        fun w(msg: String) {
            addLog(msg, AppLogLevel.Warning)
            Log.w(LOG_TAG, msg)
        }

        fun e(msg: String) {
            addLog(msg, AppLogLevel.Error)
            Log.e(LOG_TAG, msg)
        }
    }
}