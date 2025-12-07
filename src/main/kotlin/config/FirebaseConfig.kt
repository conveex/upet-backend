package com.upet.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.Application
import java.io.ByteArrayInputStream
import java.io.FileInputStream

fun Application.configureFirebase() {
    val logger = environment.log

    if (FirebaseApp.getApps().isNotEmpty()) {
        logger.info("Firebase ya esta iniciada, saltando.")
        return
    }

    try {
        val jsonEnv = System.getenv("FIREBASE_CREDENTIALS_JSON")
        val credentials = when {
            !jsonEnv.isNullOrBlank() -> {
                logger.info("Inicializando Firebase desde FIREBASE_CREDENTIALS_JSON")
                GoogleCredentials.fromStream(
                    ByteArrayInputStream(jsonEnv.toByteArray(Charsets.UTF_8))
                )
            }

            !System.getenv("GOOGLE_APPLICATION_CREDENTIALS").isNullOrBlank() -> {
                val path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
                logger.info("Iniciando Firebase desde GOOGLE_APPLICATION_CREDENTIALS en $path")
                GoogleCredentials.fromStream(FileInputStream(path))
            }

            else -> {
                logger.warn("Firebase no fue inicializada: no hay FIREBASE_CREDENTIALS_JSON o GOOGLE_APPLICATION_CREDENTIALS establecidas.")
                return
            }
        }

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        FirebaseApp.initializeApp(options)
        logger.info("Firebase inicializada exitosamente.")
    } catch (e: Exception) {
        logger.error("Error inicializando Firebase: ", e)
    }
}