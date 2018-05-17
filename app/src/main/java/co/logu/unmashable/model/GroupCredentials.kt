package co.logu.unmashable.model

import it.unisa.dia.gas.jpbc.Element
import it.unisa.dia.gas.jpbc.Pairing

class GroupCredentials {
    lateinit var pairing: Pairing
    lateinit var secret: Element
    lateinit var pseudonym: Element
    lateinit var role: GroupRole
}