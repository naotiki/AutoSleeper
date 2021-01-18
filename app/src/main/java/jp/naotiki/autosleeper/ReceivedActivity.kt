package jp.naotiki.autosleeper

import android.app.admin.DevicePolicyManager
import android.widget.Toast
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver
import android.media.AudioManager
import android.content.Context.MODE_PRIVATE
import android.view.KeyEvent


class ReceivedActivity : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "時間です", Toast.LENGTH_LONG).show()
        val dataStore= context.getSharedPreferences("Sleep", MODE_PRIVATE)
        val editor = dataStore.edit()
        editor.putBoolean("alarm",false)
        editor.apply()
        val manager= context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        manager.dispatchMediaKeyEvent(downEvent)
        (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
    }
}