package cn.atd3.weirdbox

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import org.json.JSONStringer
import java.io.*
import java.net.*

/**
 * Created by TTHHR on 2016/5/25.
 */
class GetService : Service() {
     var socket: Socket?=null
    var `in`: InputStream?=null
    var out: OutputStream?=null
    var link = true
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Thread(Runnable {
            while (link) {
                try {

                    val i = Intent("cn.atd3.getjson")
                    i.putExtra("json", initClientSocket())
                    sendBroadcast(i)
                    Thread.sleep(2000)
                } catch (e: Exception) {
                    stopSelf()
                }

            }
        }).start()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        link = false

        super.onDestroy()
    }

    private fun initClientSocket(): String? {
        var s: String? = String()
        try {
            /* 连接服务器 */
            socket = Socket()
            socket!!.connect(InetSocketAddress(InetAddress.getByName("blog.qingyuyu.cn"), 10008), 1500)
            `in` = socket!!.getInputStream()
            val bf = BufferedReader(InputStreamReader(`in`))
            /* 获取输出流 */
            out = socket!!.getOutputStream()
            val dataJson = JSONStringer()
            dataJson.`object`()
            dataJson.key("type")
            dataJson.value("app")
            dataJson.key("need")
            dataJson.value("get")
            dataJson.endObject()
            out!!.write((dataJson.toString() + "\n").toByteArray())
            out!!.flush()
            s = bf.readLine()

        } catch (e: Exception) {
            Log.e("TTHHR", "exception:e  " + e.toString())
            s = null
        } finally {
            try {
                `in`!!.close()
                out!!.close()
                socket!!.close()
            } catch (e: Exception) {

            }

        }
        return s
    }
}
