package cn.atd3.weirdbox

import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import org.json.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    /* 服务器地址 */
    private val SERVER_HOST_IP = "blog.qingyuyu.cn"
    /* 服务器端口 */
    private val SERVER_HOST_PORT = 10008
    private var socket: Socket? = null
    private var `in`: InputStream? = null
    private var out: OutputStream? = null
    private var clock = "1465539000000"
    private var feeltemp = 0
    private var clockmusic = 0
    private var light = 0
    private var chuo: Long = 0
    private val fileurl = "file:///android_asset/"
     var qinmiBar: ProgressBar?=null
     var jieBar: ProgressBar?=null
     var xinqingBar: ProgressBar?=null
     var qinmiText: TextView?=null
     var jieText: TextView?=null
     var xinqingText: TextView?=null
     var handler: Handler?=null
     var gf: WebView?=null
    var mr: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val json = intent.getStringExtra("json")
            if (json == null) {
                Toast.makeText(this@MainActivity, "网络错误！", Toast.LENGTH_LONG).show()
                return
            }
            val message = Message()
            message.obj = json
            handler!!.sendMessage(message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setLogo(R.mipmap.online)


        qinmiBar = findViewById(R.id.qinmiBar) as ProgressBar
        jieBar = findViewById(R.id.jieBar) as ProgressBar
        xinqingBar = findViewById(R.id.xinqingBar) as ProgressBar
        qinmiText = findViewById(R.id.qinmiText) as TextView
        xinqingText = findViewById(R.id.xinqingText) as TextView
        jieText = findViewById(R.id.jieText) as TextView
        gf = findViewById(R.id.gif) as WebView
        gf!!.loadUrl(fileurl + "gif0.gif")

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener(View.OnClickListener { view ->
            if (System.currentTimeMillis() - chuo < 3000) {
                Snackbar.make(view, "ww才被戳了一下，需要等一会~", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                return@OnClickListener
            }
            chuo = System.currentTimeMillis()
            Snackbar.make(view, "你戳了ww一下", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            feeltemp += 1
            Thread(Runnable {
                val message = Message()
                message.obj = initClientSocket()
                handler!!.sendMessage(message)
            }).start()
        })

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
           override fun onDrawerOpened(view: View) {
                toolbar.setTitle("诡异~")
                //  invalidateOptionsMenu();
            }

          override  fun onDrawerClosed(view: View) {
                toolbar.setTitle("Weird Box")
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        }
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)



        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val s = msg.obj as String
                /*try{
                    JSONObject jsonObject=new JSONObject(s);
                    Date dt=new Date(jsonObject.getLong("clock"));
                    DateFormat df = new SimpleDateFormat("HH:mm");
                    light=jsonObject.getInt("light");
                    clockmusic=jsonObject.getInt("clockmusic");
                    qinmiText.setText("亲密度："+jsonObject.getInt("qinmi"));
                    jieText.setText("饥饿度："+jsonObject.getInt("hunger"));
                    xinqingText.setText("心情："+jsonObject.getInt("feel"));
                    qinmiBar.setProgress(jsonObject.getInt("qinmi"));
                    xinqingBar.setProgress(jsonObject.getInt("feel"));
                    jieBar.setProgress(jsonObject.getInt("hunger"));
                    toolbar.setLogo(jsonObject.getBoolean("online")==true?R.mipmap.online:R.mipmap.outline);
                    webView.loadUrl(fileurl+jsonObject.getInt("biaoqing")+".gif");
                    clock=jsonObject.getLong("clock")+"";
                    Log.e("tthhr",jsonObject.getLong("clock")+"clock"+System.currentTimeMillis()+"now");
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this,"发生错误"+e, Toast.LENGTH_LONG).show();
                }
                */
                Log.e("msg", s)
                super.handleMessage(msg)
            }
        }


        val ifilter = IntentFilter("cn.atd3.getjson")
        this.registerReceiver(mr, ifilter)
        val startService = Intent(this, GetService::class.java)
        startService(startService)
    }

    override fun onDestroy() {
        val stopService = Intent(this, GetService::class.java)
        stopService(stopService)
        this.unregisterReceiver(mr)
        super.onDestroy()
    }

   override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

   override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        val id = item.itemId
        if (id == R.id.linkbox) {
            val i = Intent(this@MainActivity, SelectDeviceActivity::class.java)
            startActivity(i)
        } else if (id == R.id.nav_share) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_SUBJECT, "分享")
            intent.putExtra(Intent.EXTRA_TEXT, "Weird Box好有趣，快来玩玩吧！下载地址：http://www.15wedian.cn/ ")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(Intent.createChooser(intent, getTitle()))
        } else if (id == R.id.nav_exit) {
            finish()
            System.exit(0)
        } else if (id == R.id.clock) {
            val dt = Date(java.lang.Long.parseLong(clock))
            var df: DateFormat = SimpleDateFormat("HH")
            val hour = Integer.parseInt(df.format(dt))
            df = SimpleDateFormat("mm")
            val minute = Integer.parseInt(df.format(dt))

            val mTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                clock = hourOfDay.toString() + ":" + minute
                item.title = "当前闹钟： " + clock
                var dt = Date()
                var df: DateFormat = SimpleDateFormat("HH:mm")
                var nowTime = ""
                nowTime = df.format(dt)
                if (nowTime.compareTo(clock) <= 0) {//如果就是今天
                    df = SimpleDateFormat("yyyy-MM-dd")
                    clock = df.format(dt) + " " + clock
                    df = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    try {
                        dt = df.parse(clock)
                    } catch (e: Exception) {

                    }

                    clock = dt.time.toString() + ""
                } else
                //否则日期加一
                {
                    var date = Date()
                    df = SimpleDateFormat("yyyy-MM-dd")
                    df.format(date)
                    try {
                        date = df.parse(df.format(date))
                    } catch (e: Exception) {

                    }

                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DATE, 1)
                    clock = df.format(calendar.time) + " " + clock
                    try {
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(clock)
                    } catch (e: Exception) {

                    }

                    clock = date.time.toString() + ""
                }
                Thread(Runnable {
                    val message = Message()
                    message.obj = initClientSocket()
                    handler!!.sendMessage(message)
                }).start()

                val music = arrayOf("Alarm_Beep", "Alarm_Classic", "Alarm_Rooster", "Bugle", "Ripple")
                val musicId = intArrayOf(R.raw.alarm_beep, R.raw.alarm_classic, R.raw.alarm_rooster, R.raw.bugle, R.raw.ripple)
                val lv = ListView(this@MainActivity)
                val listAdapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, music)
                lv.adapter = listAdapter
                lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    var mp3 = MediaPlayer()    //创建一个MediaPlayer对象
                    mp3 = MediaPlayer.create(this@MainActivity, musicId[position])
                    try {

                        mp3.start()
                    } catch (e: Exception) {
                    }
                }
                val b = AlertDialog.Builder(this@MainActivity)
                b.setTitle("设置闹钟音乐：").setView(lv).setNegativeButton("确定", DialogInterface.OnClickListener { dialog, which ->
                    clockmusic = lv.checkedItemPosition
                    Thread(Runnable {
                        val message = Message()
                        message.obj = initClientSocket()
                        handler!!.sendMessage(message)
                    }).start()
                }).setPositiveButton("取消", null).setIcon(R.mipmap.light).show()
            }

            val mTimePickerDialog = TimePickerDialog(this, mTimeSetListener, hour, minute, true)
            mTimePickerDialog.show()
        } else if (id == R.id.light) {
            val factory = LayoutInflater.from(this)
            val lightView = factory.inflate(R.layout.activity_light, null)
            val lightText = lightView.findViewById(R.id.lightText) as TextView
            lightText.text = "当前亮度：" + light
            val seekbar = lightView.findViewById(R.id.lightbar) as SeekBar
            seekbar.progress = light
            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                //触发操作，拖动
                override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                               fromUser: Boolean) {
                    lightText.text = "当前亮度：" + seekBar.progress
                }

                //表示进度条刚开始拖动，开始拖动时候触发的操作
                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    lightText.text = "当前亮度：" + seekBar.progress
                }

                //停止拖动时候
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // TODO Auto-generated method stub
                    lightText.text = "当前亮度：" + seekBar.progress
                }
            })

            val b = AlertDialog.Builder(this@MainActivity)
            b.setTitle("调节亮度：").setView(lightView).setNegativeButton("确定", DialogInterface.OnClickListener { dialog, which ->
                light = seekbar.progress
                Thread(Runnable {
                    val message = Message()
                    message.obj = initClientSocket()
                    handler!!.sendMessage(message)
                }).start()
            }).setPositiveButton("取消", null).setIcon(R.mipmap.light).show()
        }
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initClientSocket(): String {
        var s = String()
        try {
            /* 连接服务器 */
            socket = Socket()
            socket!!.connect(InetSocketAddress(InetAddress.getByName(SERVER_HOST_IP), SERVER_HOST_PORT), 1500)
            `in` = socket!!.getInputStream()
            val bf = BufferedReader(InputStreamReader(`in`!!))
            /* 获取输出流 */
            out = socket!!.getOutputStream()
            val dataJson = JSONStringer()
            dataJson.`object`()
            dataJson.key("type")
            dataJson.value("app")
            dataJson.key("need")
            dataJson.value("put")

            dataJson.key("clock")
            dataJson.value(java.lang.Long.parseLong(clock))
            dataJson.key("light")
            dataJson.value(light.toLong())
            dataJson.key("feeltemp")
            dataJson.value(feeltemp.toLong())
            feeltemp = 0
            dataJson.key("clockmusic")
            dataJson.value(clockmusic.toLong())
            dataJson.endObject()
            out!!.write((dataJson.toString() + "\n").toByteArray())
            out!!.flush()
            s = bf.readLine()

        } catch (e: Exception) {
            Log.e("TTHHR", "exception: " + e.toString())
            s = e.toString()
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
