package co.logu.unmashable.log

import java.util.*

class AppLogEntry {
    lateinit var level: AppLogLevel
    lateinit var message: String
    lateinit var time: Date
}