package com.upet.tracking

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.SetOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import java.util.UUID

class FirestoreTrackingRepository {
    private fun firestore() =
        if(FirebaseApp.getApps().isEmpty()) null else FirestoreClient.getFirestore()

    /*
    * Guarda punto en:
    * tracking/{walkId}/points/{pointId}
    * */
    fun savePoint(walkId: UUID, req: TrackingPointRequest) {
        val db = firestore() ?: throw IllegalStateException("Firestore no está disponible.")

        val pointId = UUID.randomUUID().toString()

        val data = mutableMapOf<String, Any>(
            "lat" to req.lat,
            "lng" to req.lng,
            "is_manual" to req.isManual
        )

        req.accuracyMeters?.let { data["accuracy"] = it }
        req.speedMetersPerSecond?.let { data["speed_m_s"] = it }
        req.batteryLevel?.let { data["battery_level"] = it }

        if(req.timestampMillis != null) {
            data["timestamp"] = req.timestampMillis
        } else {
            data["timestamp"] = FieldValue.serverTimestamp()
        }

        db.collection("tracking")
            .document(walkId.toString())
            .collection("points")
            .document(pointId)
            .set(data)
    }

    /*
    * Lee puntos para generar el summary
    * */
    fun fetchRecentPoints(walkId: UUID, limit: Int = 200): List<Map<String, Any?>> {
        val db = firestore() ?: throw IllegalStateException("Firestore no está disponible. ¿Firebase inicializado?")

        val snap = db.collection("tracking")
            .document(walkId.toString())
            .collection("points")
            .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong().toInt())
            .get()
            .get()

        return snap.documents.map { it.data ?: emptyMap() }
    }

    fun upsertMeta(
        walkId: UUID,
        clientId: UUID,
        polylineEncoded: String,
        walkerId: UUID? = null,
        deviationThresholdMeters: Int = 120,
        evaluationIntervalSeconds: Int = 15,
        active: Boolean = true
    ) {
        val metaRef = firestore()?.collection("tracking")
            ?.document(walkId.toString())
            ?.collection("meta")
            ?.document("meta")

        val payload = mutableMapOf<String, Any>(
            "active" to active,
            "clientId" to clientId.toString(),
            "polylineEncoded" to polylineEncoded,
            "deviationThresholdMeters" to deviationThresholdMeters,
            "evaluationIntervalSeconds" to evaluationIntervalSeconds,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (walkerId != null) payload["walkerId"] = walkerId.toString()

        metaRef?.set(payload, SetOptions.merge())
    }

    fun initState(walkId: UUID) {
        val stateRef = firestore()?.collection("tracking")
            ?.document(walkId.toString())
            ?.collection("state")
            ?.document("state")

        stateRef?.set(
            mapOf(
                "lastDeviationNotifiedAt" to 0L,
                "createdAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
    }

    fun setActive(walkId: UUID, active: Boolean) {
        val metaRef = firestore()?.collection("tracking")
            ?.document(walkId.toString())
            ?.collection("meta")
            ?.document("meta")

        metaRef?.set(
            mapOf(
                "active" to active,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
    }
}