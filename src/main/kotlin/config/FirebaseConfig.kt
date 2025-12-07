package com.upet.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import java.io.ByteArrayInputStream

fun Application.configureFirebase() {
    val logger = environment.log

    if (FirebaseApp.getApps().isNotEmpty()) {
        logger.info("Firebase ya está inicializada, se omite reinicio.")
        return
    }

    try {
        val jsonEnv = System.getenv("FIREBASE_CREDENTIALS_JSON")

        if (jsonEnv.isNullOrBlank()) {
            logger.warn("Firebase no fue inicializada: no hay FIREBASE_CREDENTIALS_JSON establecida.")
            return
        }

        logger.info("Inicializando Firebase desde FIREBASE_CREDENTIALS_JSON (longitud=${jsonEnv.length})")

        val credentials = GoogleCredentials.fromStream(
            ByteArrayInputStream(jsonEnv.toByteArray(Charsets.UTF_8))
        )

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        FirebaseApp.initializeApp(options)
        logger.info("Firebase inicializada exitosamente.")
    } catch (e: Exception) {
        logger.error("Error inicializando Firebase: ", e)
    }
}