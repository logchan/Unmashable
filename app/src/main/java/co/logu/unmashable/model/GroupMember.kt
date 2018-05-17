package co.logu.unmashable.model

import co.logu.unmashable.framework.MyUtils
import co.logu.unmashable.log.AppLog
import it.unisa.dia.gas.jpbc.Element

class GroupMember(val cred: GroupCredentials) {

    private var rnd = cred.pairing.zr.newRandomElement().immutable
    private var sessionKey: Element? = null
    private var sessionId: Element? = null

    fun newIdentity() {
        rnd = cred.pairing.zr.newRandomElement().immutable
        sessionId = cred.pseudonym.powZn(rnd).immutable
    }

    fun getPublicCredentials(): ByteArray {
        return sessionId!!.toBytes()
    }

    fun generateSessionKey(bytes: ByteArray) {
        val otherG = when (cred.role) {
            GroupRole.Scanner -> { cred.pairing.g2 }
            GroupRole.Advertiser -> { cred.pairing.g1 }
        }

        val otherCred = otherG.newElementFromBytes(bytes).immutable
        sessionKey = when (cred.role) {
            GroupRole.Scanner -> { cred.pairing.pairing(cred.secret, otherCred).powZn(rnd).immutable }
            GroupRole.Advertiser -> {cred.pairing.pairing(otherCred, cred.secret).powZn(rnd).immutable }
        }
    }

    fun getSessionKey() : ByteArray {
        return sessionKey!!.toBytes()
    }
}