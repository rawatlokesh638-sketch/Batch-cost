package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class WhatsAppNotificationListener : NotificationListenerService() {
    private val TAG = "WhatsAppNotificationListener"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: ""
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") {
            return
        }

        try {
            val extras = sbn.notification.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim() ?: ""

            // Filter out system or status messages
            if (text.isBlank() || title.isBlank()) return
            if (text.contains("Checking for new messages", ignoreCase = true) ||
                text.contains("WhatsApp Web is currently active", ignoreCase = true) ||
                text.contains("Backup in progress", ignoreCase = true) ||
                text.contains("Incoming voice call", ignoreCase = true) ||
                text.contains("new messages", ignoreCase = true) && text.length < 20) {
                return
            }

            Log.d(TAG, "Captured WhatsApp Notification from: $title: $text")
            WhatsAppOrderCaptureHub.processCapturedWhatsAppMessage(
                senderName = title,
                messageText = text,
                source = "WhatsApp Notification",
                context = applicationContext
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling WhatsApp notification", e)
        }
    }
}
