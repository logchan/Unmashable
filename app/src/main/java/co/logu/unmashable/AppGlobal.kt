package co.logu.unmashable

import android.content.Context
import co.logu.unmashable.AppPref.Companion.getPrefString
import co.logu.unmashable.AppPref.Companion.setPrefString
import co.logu.unmashable.framework.MyUtils
import co.logu.unmashable.log.AppLog
import co.logu.unmashable.model.*

class AppGlobal {
    companion object {
        lateinit var master: GroupMaster
        lateinit var ble: BleWrapper
        var advertiserMember: GroupMember? = null
        var scannerMember: GroupMember? = null
        var advertiser: HandshakeAdvertiser? = null
        var scanner: HandshakeScanner? = null

        private fun toBase64(bytes: ByteArray) : String {
            return MyUtils.toBase64(bytes)
        }

        private fun fromBase64(string: String) : ByteArray {
            return MyUtils.fromBase64(string)
        }

        private fun restoreMemberCredentials(pseudonymKey: String, role: GroupRole, context: Context) : GroupCredentials {
            val n = context.getPrefString(pseudonymKey, "")
            try {
                if (n.isEmpty())
                    throw Exception("no saved credential found")

                return master.restoreMemberSecret(role, fromBase64(n))
            }
            catch (ex: Throwable) {
                AppLog.d("restoring member secret: ${ex.message}, create new one")

                val cred = master.createMemberSecret(role)
                context.setPrefString(pseudonymKey, toBase64(cred.pseudonym.toBytes()))
                return cred
            }
        }

        fun initMaster(context: Context, theBle: BleWrapper) {
            master = GroupMaster(context.applicationInfo.dataDir)
            ble = theBle
        }

        fun initMembers(context: Context) {
            AppLog.d("init members")
            AppLog.d("master secret")
            val s = context.getPrefString(AppPref.masterSecretKey, "")
            try {
                if (s.isEmpty())
                    throw Exception("no saved master secret found")

                master.setSecretBytes(fromBase64(s))
            }
            catch (ex: Throwable) {
                AppLog.w("restoring master secret: ${ex.message}, create new one")

                master.createNewSecret()
                context.setPrefString(AppPref.masterSecretKey, toBase64(master.getSecretBytes()))
            }

            AppLog.d("scanner")
            scannerMember = GroupMember(restoreMemberCredentials(AppPref.scannerPseudonymKey, GroupRole.Scanner, context))

            AppLog.d("advertiser")
            advertiserMember = GroupMember(restoreMemberCredentials(AppPref.advertiserPseudonymKey, GroupRole.Advertiser, context))

            AppLog.d("parties")
            scanner = HandshakeScanner(scannerMember!!, ble)
            advertiser = HandshakeAdvertiser(advertiserMember!!, ble)
        }
    }
}