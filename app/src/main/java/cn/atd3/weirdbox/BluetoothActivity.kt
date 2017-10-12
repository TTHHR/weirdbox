package cn.atd3.weirdbox

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.UUID
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Administrator on 2016/5/20.
 */
class BluetoothActivity : AppCompatActivity() {
    var MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    private var blueSocket: BluetoothSocket? = null
    private var handler: Handler? = null
    private var running = true
    //蓝牙适配器
 var blueAdapter: BluetoothAdapter? = null
 var os: OutputStream? = null//输出流
 var osw: OutputStreamWriter? = null
 var `is`: InputStream? = null

     var pd: ProgressDialog? = null
     var listView: ListView? = null
    /** Called when the activity is first created.  */
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        listView = findViewById(R.id.listView) as ListView
        blueAdapter = BluetoothAdapter.getDefaultAdapter()//获取本机蓝牙
        if (blueAdapter == null)
        //如果没有蓝牙
        {
            Toast.makeText(this, "抱歉，您的手机没有蓝牙功能！", Toast.LENGTH_LONG).show()
            finish()//结束程序
        } else {
            if (blueAdapter!!.isEnabled == false)
            //如果本机蓝牙没有打开
            {
                blueAdapter!!.enable()//打开
            }
        }
        val ab = AlertDialog.Builder(this)
        val link = TextView(this)
        link.text = "正在连接蓝牙。。。"
        val pb = ProgressBar(this)
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(link)
        ll.addView(pb)
        ab.setView(ll)
        val ad = ab.create()
        ad.setCancelable(false)//不可以取消
        ad.setCanceledOnTouchOutside(false) //点击外面区域不会让dialog消失
        ad.show()

        handler = object : Handler() {
            override fun handleMessage(ms: Message) {
                val mess = ms.obj as String
                Log.e("mmmmmmmm", mess)
                if (mess == "connect") {
                    if (!blueSocket!!.isConnected)
                        blueSocket!!.connect()
                    try {
                        /* 获取输出流 */
                        if (os == null)
                            os = blueSocket!!.outputStream
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "蓝牙连接失败", Toast.LENGTH_SHORT).show()
                        ad.cancel()
                        e.printStackTrace()
                    }
                    Toast.makeText(applicationContext, "蓝牙连接成功", Toast.LENGTH_SHORT).show()
                    ad.cancel()

                } else if (mess == "ui") {

                } else {
                    Toast.makeText(applicationContext, mess, Toast.LENGTH_LONG).show()
                }
            }
        }


        Thread(Runnable {
            try {
                val i = intent
                val address: String = i.getStringExtra("address")
                val btDev: BluetoothDevice
                btDev = blueAdapter!!.getRemoteDevice(address)
                Thread.sleep(500)
                val uuid = UUID.fromString(MY_UUID)
                blueSocket = btDev.createRfcommSocketToServiceRecord(uuid)
                if (blueSocket == null)
                    blueSocket!!.connect()
                val message = Message()
                message.obj = "connect"
                handler!!.sendMessage(message)
            } catch (e: Exception) {
                Log.e("error", e.toString())
                Toast.makeText(applicationContext, "蓝牙连接失败", Toast.LENGTH_SHORT).show()
                ad.cancel()
            }
        }).start()

        val b = findViewById(R.id.button) as Button
        b.setOnClickListener {
            try {
                if (os == null)
                    os = blueSocket!!.outputStream
                `is` = blueSocket!!.inputStream
                os!!.write("r".toByteArray())
                Log.e("rr","rrrrr......................")
                os!!.flush()
                val br = BufferedReader(InputStreamReader(`is`!!))
                val list = ArrayList<String>()
                val wifi: MutableList<String> = mutableListOf()
                var s=br.readLine()
                while (s!=null) {
                    if(s.equals("OK"))
                        break
                    Log.e("ss","ssssss......................")
                    val pa = Pattern.compile("\"(.*)\",-")//源码中的正则表达式
                    val ma = pa.matcher(s)
                    if (ma.find())
                    //寻找符合el的字串
                    {
                        s = ma.group()
                        s = s!!.replace("\",-".toRegex(), "").replace("\"".toRegex(), "")
                        wifi.add(s)
                    }
                    s=br.readLine()
                    Log.e("....", wifi.toString())
                }
                Log.e("....", wifi.toString())
                val listAdapter = ArrayAdapter<String>(this@BluetoothActivity, android.R.layout.simple_list_item_1, wifi)

                listView!!.adapter = listAdapter
                listView!!.onItemClickListener = AdapterView.OnItemClickListener { l, v, position, id ->
                    val wifiname = wifi[position]
                    val password = EditText(this@BluetoothActivity)
                    password.inputType = InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    val b = AlertDialog.Builder(this@BluetoothActivity)
                    b.setTitle("设置" + wifiname + "的密码：").setView(password).setNegativeButton("确定", DialogInterface.OnClickListener { dialog, which ->
                        try {

                            os!!.write(("\n\"" + wifiname + "\",\"" + password.text.toString() + "\"\n").toByteArray())
                            os!!.flush()
                            val br = BufferedReader(InputStreamReader(`is`!!))
                            val info=br.readLine()
                            Log.e("info",info)
                            if (info == "link to Router Success!") {
                                Toast.makeText(this@BluetoothActivity, "连接成功！", Toast.LENGTH_SHORT).show()
                                this@BluetoothActivity.finish()
                            } else {
                                Toast.makeText(this@BluetoothActivity, "连接失败，请检查！！", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {

                        }
                    }).setPositiveButton("取消", null).setIcon(R.mipmap.light).show()
                }
            } catch (e: Exception) {
                Log.e("tthhr", e.toString())
            }
        }
    }



    override fun onDestroy() {
        // TODO: Implement this method
        super.onDestroy()

        try {
            if (os != null)
                os!!.close()
            if (osw != null)
                osw!!.close()
        } catch (e: Exception) {
        }
        blueAdapter!!.disable()//关闭
    }


}