package jp.naotiki.autosleeper

import android.app.*
import android.app.PendingIntent.getActivity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.icu.util.Calendar
import android.os.Build
import android.os.SystemClock
import android.widget.MediaController
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.SharedPreferences.Editor
import android.content.DialogInterface
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.view.KeyEvent


class MainActivity : AppCompatActivity() {
     private val ADMIN_INTENT=100
    private lateinit var devicePolicyManager:DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private lateinit var dataStore: SharedPreferences

    override fun onResume() {
        super.onResume()

        if(dataStore.getBoolean("alarm",false)){
            button.text="キャンセル"
            button.setOnClickListener {
                CancelAlarm()
            }
        }else{
            button.text="スタート"
            button.setOnClickListener{
                SetTime()
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataStore = getSharedPreferences("Sleep", MODE_PRIVATE)



        devicePolicyManager= getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName= ComponentName(this,Admin::class.java)
        hourPicker.minValue=0
        hourPicker.maxValue=10
        minutePicker.minValue=0
        minutePicker.maxValue=59


AdminRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==ADMIN_INTENT){
            if (resultCode== RESULT_OK){

            }else{
                Toast.makeText(this,"許可してください",Toast.LENGTH_SHORT).show()

            }
        }
    }
fun CancelAlarm(){
    AlertDialog.Builder(this)
        .setTitle("警告")
        .setMessage("タイマーをキャンセルしますか？")
        .setPositiveButton("はい", DialogInterface.OnClickListener { dialog, which ->
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val i = Intent(applicationContext, ReceivedActivity::class.java) // ReceivedActivityを呼び出すインテントを作成

            val sender = PendingIntent.getBroadcast(this, 0, i, 0) // ブロードキャストを投げるPendingIntentの作成
            alarmManager.cancel(sender)
            Toast.makeText(this,"タイマーをキャンセルしました",Toast.LENGTH_LONG).show()
            button.text="スタート"
            button.setOnClickListener{
                SetTime()
            }
        })
        .setNegativeButton("いいえ", null)
        .show()

}
    fun SetTime() {
        if (devicePolicyManager.isAdminActive(adminComponentName)){
            if (minutePicker.value==0&&hourPicker.value==0){
                Toast.makeText(this,"1分以上で指定してください",Toast.LENGTH_SHORT).show()
                return
            }
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val calendar: Calendar = Calendar.getInstance() // Calendar取得

            calendar.timeInMillis = System.currentTimeMillis() // 現在時刻を取得
            calendar.add(Calendar.MINUTE, minutePicker.value)
            calendar.add(Calendar.HOUR,hourPicker.value)
            val i = Intent(applicationContext, ReceivedActivity::class.java) // ReceivedActivityを呼び出すインテントを作成

            val sender = PendingIntent.getBroadcast(this, 0, i, 0) // ブロードキャストを投げるPendingIntentの作成


            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,sender)
            val editor = dataStore.edit()
            editor.putBoolean("alarm",true)
            editor.apply()
            Toast.makeText(this,"タイマーをセットしました",Toast.LENGTH_LONG).show()
            button.text="キャンセル"
            button.setOnClickListener {
                CancelAlarm()
            }
        }else{
            AdminRequest()
        }


    }
    fun AdminRequest(){
        if (!devicePolicyManager.isAdminActive(adminComponentName)){
            val i=Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,adminComponentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"これを許可してデバイスを自動でロックします。")
            }
            startActivityForResult(i,ADMIN_INTENT)
        }
    }
}