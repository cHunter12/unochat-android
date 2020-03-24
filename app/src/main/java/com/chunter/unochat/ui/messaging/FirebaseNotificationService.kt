package com.chunter.unochat.ui.messaging

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chunter.unochat.R
import com.chunter.unochat.ui.host.HostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        Firebase.firestore.document("users/${currentUser.uid}")
            .update("token", token)
            .addOnFailureListener { exception ->
                Timber.e(exception)
                if (exception is FirebaseFirestoreException
                    && exception.code == FirebaseFirestoreException.Code.NOT_FOUND
                ) {
                    onNewToken(token)
                }
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notification = remoteMessage.notification ?: return
        val intent = Intent(this, HostActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // TODO: Open room
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.message_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // TODO: Unique id per room
        notificationManager.notify(0, notificationBuilder.build())
    }
}