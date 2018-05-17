package co.logu.unmashable.model

import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing
import it.unisa.dia.gas.jpbc.PairingParametersGenerator
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator
import java.io.File
import java.nio.charset.Charset

class GroupMaster (fileDir: String) {

    val pairing: Pairing
    init {
        val file = File(File(fileDir), "params.txt")
        val params = "type a\n" +
        "q 90826651984863431296123265801980415339\n" +
        "r 1208925819614629174697983\n" +
        "h 75130045624980\n" +
        "exp1 13\n" +
        "exp2 80\n" +
        "sign0 -1\n" +
        "sign1 -1\n"

        file.writeText(params, Charset.defaultCharset())
        pairing = PairingFactory.getPairing(file.absolutePath)
    }

    var secret: Element = pairing.zr.newOneElement()

    fun createNewSecret() {
        secret = pairing.zr.newRandomElement()
    }

    fun getSecretBytes() : ByteArray {
        return secret.toBytes()
    }

    fun setSecretBytes(bytes: ByteArray) {
        secret = pairing.zr.newElementFromBytes(bytes)
    }

    fun createMemberSecret(role: GroupRole): GroupCredentials {
        val g = when (role) {
            GroupRole.Scanner -> { pairing.g1 }
            GroupRole.Advertiser -> { pairing.g2 }
        }
        val cred = GroupCredentials()
        cred.pairing = pairing
        cred.role = role
        cred.pseudonym = g.newRandomElement().immutable
        cred.secret = cred.pseudonym.powZn(secret).immutable
        return cred
    }

    fun restoreMemberSecret(role: GroupRole, bytes: ByteArray): GroupCredentials {
        val g = when (role) {
            GroupRole.Scanner -> { pairing.g1 }
            GroupRole.Advertiser -> { pairing.g2 }
        }
        val cred = GroupCredentials()
        cred.pairing = pairing
        cred.role = role
        cred.pseudonym = g.newElementFromBytes(bytes).immutable
        cred.secret = cred.pseudonym.powZn(secret).immutable
        return cred
    }
}