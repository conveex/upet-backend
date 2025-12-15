package com.upet.notifications

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

class NotificationService {
    fun sendPush(
        token: String?,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Boolean {
        if (token.isNullOrBlank()) return false
        if (FirebaseApp.getApps().isEmpty()) return false

        val msg = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .putAllData(data)
            .build()

        FirebaseMessaging.getInstance().send(msg)
        return true
    }
}