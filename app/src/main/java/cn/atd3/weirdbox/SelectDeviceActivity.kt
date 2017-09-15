package cn.atd3.weirdbox

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_splash.*

class SelectDeviceActivity : Activity() {
    lateinit var bluetoothAdapter: BluetoothAdapter
    //lateinit 是延迟初始化的意思
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_splash)
        // 去掉界面任务条
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override  fun onStart() {
        super.onStart()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "你的手机不支持蓝牙，应用即将退出！", Toast.LENGTH_LONG).show()
            Handler().postDelayed({ this.finish() }, 1000)
        } else {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            }//确保蓝牙已经打开

            val pairedDevices = bluetoothAdapter.bondedDevices
            val deviceName: MutableList<String> = mutableListOf()
            for (device in pairedDevices) {
                deviceName.add(device.name)
            }
            val aa = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, deviceName)
            devicelist.adapter = aa

            devicelist.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val intent = Intent()
                intent.putExtra("address", pairedDevices.elementAt(i).address)
                intent.setClass(this@SelectDeviceActivity, BluetoothActivity::class.java)
                startActivity(intent)
                this@SelectDeviceActivity.finish()
            }
        }
    }
}
