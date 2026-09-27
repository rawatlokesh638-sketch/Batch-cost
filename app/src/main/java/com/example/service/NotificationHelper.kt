package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "batchcost_orders_channel"
    private const val CHANNEL_NAME = "WhatsApp Order Notifications"

    fun showOrderDetectedNotification(
        context: Context,
        customerName: String,
        productName: String,
        amount: Double,
        deliveryDate: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when BatchCost auto-detects a bakery order in WhatsApp"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO_TAB", 2) // Orders tab
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 10000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = "🎂 New WhatsApp Order: $productName"
            val text = "$customerName • ₹${String.format(java.util.Locale.US, "%.0f", amount)} • Delivery: $deliveryDate"

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText("$text\n✨ Auto-extracted by Agent 3.8 Flash & recorded into BatchCost."))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Failed to show order notification: ${e.message}")
        }
    }
}
