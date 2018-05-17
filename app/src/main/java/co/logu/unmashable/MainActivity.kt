package co.logu.unmashable

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import co.logu.unmashable.model.BleWrapper
import kotlinx.android.synthetic.main.activity_main.*

const val FRAG_HOME = "frag_home"
const val FRAG_CONFIG = "frag_config"
const val FRAG_LOGS = "frag_logs"

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                setFragment(FRAG_HOME)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_config -> {
                setFragment(FRAG_CONFIG)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_logs -> {
                setFragment(FRAG_LOGS)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun setFragment(tag: String) {
        var frag = supportFragmentManager.findFragmentByTag(tag)
        if (frag == null) {
            frag = when (tag) {
                FRAG_HOME -> HomeFragment()
                FRAG_CONFIG -> ConfigFragment()
                FRAG_LOGS -> LogsFragment()
                else -> throw Exception("unknown fragment tag $tag")
            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, frag, tag)
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppGlobal.initMaster(this, BleWrapper((getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter))
        AppGlobal.initMembers(this)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        setFragment(FRAG_HOME)
    }
}
