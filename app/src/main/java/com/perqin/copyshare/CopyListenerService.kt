package com.perqin.copyshare

import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

/**
 * Author: perqin
 * Date  : 6/16/17
 */
class CopyListenerService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, getForegroundServiceNotification(this))
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!listenerAdded) {
            listenerAdded = true
            clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
            Log.d(TAG, "Listener added")
        }
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerAdded = false
        clipboardManager.removePrimaryClipChangedListener(onPrimaryClipChangedListener)
        Log.d(TAG, "Service destroyed")
    }

    private fun notifyUserOfClipItem(meta: ClipDescription, item: ClipData.Item?) {
        // Now we just use Notification
        val notification = getNotification(this, meta, item)
        if (notification != null) {
            val id = getNotificationId()
            notificationManager.notify(id, notification)
            uiHandler.postDelayed({
                notificationManager.cancel(id)
            }, 8000)
        }
    }

    private var listenerAdded = false
    private val clipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    private val uiHandler by lazy { Handler(Looper.getMainLooper()) }
    private val onPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (clipboardManager.primaryClip != null && clipboardManager.primaryClipDescription != null) {
            for (i in 1..clipboardManager.primaryClip!!.itemCount) {
                notifyUserOfClipItem(clipboardManager.primaryClipDescription!!, clipboardManager.primaryClip!!.getItemAt(i - 1))
            }
        }
    }

    companion object {
        const val TAG = "CopyListenerService"
    }
}
