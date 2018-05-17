package co.logu.unmashable

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.logu.unmashable.framework.MyUtils
import co.logu.unmashable.log.AppLog
import co.logu.unmashable.model.HandshakeParty
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    enum class HomeAction {
        Stop,
        Advertise,
        Scan
    }

    private var action = HomeAction.Stop
    private var totalHandshakes = 0
    private var successHandshakes = 0
    private val handler = Handler()

    private val handshakeListener = object : HandshakeParty.OnHandshakeEventListener {
        override fun onHandshakeSuccess() {
            totalHandshakes += 1
            successHandshakes += 1
            AppLog.i("Handshake success")
            updateDisplay()
            handler.postDelayed( { startNextHandshake() }, 1000)
        }

        override fun onHandshakeFailure(reason: String) {
            totalHandshakes += 1
            AppLog.i("Handshake failure: $reason")
            updateDisplay()
            handler.postDelayed({startNextHandshake()}, 1000)
        }
    }

    private fun updateDisplay() {
        activity.runOnUiThread {
            home_msg.text = "$successHandshakes / $totalHandshakes"
        }
    }

    private fun startNextHandshake() {
        when (action) {
            HomeAction.Stop -> {
                // nothing
            }
            HomeAction.Advertise -> {
                AppGlobal.advertiser!!.start(handshakeListener)
            }
            HomeAction.Scan -> {
                AppGlobal.scanner!!.start(handshakeListener)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        start_adv_btn.setOnClickListener {
            if (action == HomeAction.Stop) {
                action = HomeAction.Advertise
                AppGlobal.advertiser!!.start(handshakeListener)
                status_text.text = "Advertising"
            }
        }
        stop_adv_btn.setOnClickListener {
            if (action == HomeAction.Advertise) {
                action = HomeAction.Stop
                AppGlobal.advertiser!!.stop()
                status_text.text = "Idle"
            }
        }
        start_scan_btn.setOnClickListener {
            if (action == HomeAction.Stop) {
                action = HomeAction.Scan
                AppGlobal.scanner!!.start(handshakeListener)
                status_text.text = "Scanning"
            }
        }
        stop_scan_btn.setOnClickListener {
            if (action == HomeAction.Scan) {
                action = HomeAction.Stop
                AppGlobal.scanner!!.stop()
                status_text.text = "Idle"
            }
        }
        test_btn.setOnClickListener {

        }
    }

    private fun testCredentials() {
        val a = AppGlobal.advertiserMember!!
        val s = AppGlobal.scannerMember!!

        s.newIdentity()
        a.newIdentity()

        Thread.sleep(100)

        s.generateSessionKey(a.getPublicCredentials())
        a.generateSessionKey(s.getPublicCredentials())

        AppLog.d("scanner key: ${MyUtils.toBase64(s.getSessionKey())}")
        AppLog.d("advertiser key: ${MyUtils.toBase64(a.getSessionKey())}")
    }

    private fun testEncrypt() {
        val key = ByteArray(32)
        key[0] = 0x11
        key[1] = 0x22
        val data = ByteArray(16)
        data[0] = 0x33
        data[1] = 0x44
        val enc = HandshakeParty.encrypt(key, data)
        AppLog.d("enc ${enc.size}: ${MyUtils.toHexString(enc)}")
        val dec = HandshakeParty.decrypt(key, enc)
        AppLog.d("dec ${dec.size}: ${MyUtils.toHexString(dec)}")
    }
}
