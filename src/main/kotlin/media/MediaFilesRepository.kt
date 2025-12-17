package com.upet.media

import com.upet.data.db.tables.MediaFilesTable
import com.upet.domain.model.MediaFileType
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

private fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

class MediaFilesRepository {
    fun insertMediaFile(
        walkId: UUID,
        type: MediaFileType,
        storagePath: String,
        sizeBytes: Long,
        mimeType: String
    ): MediaFileRow = transaction {
        val id = UUID.randomUUID()
        val created = nowUtc()

        MediaFilesTable.insert { row ->
            row[MediaFilesTable.id] = id
            row[MediaFilesTable.walkId] = walkId
            row[MediaFilesTable.type] = type
            row[MediaFilesTable.storagePath] = storagePath
            row[MediaFilesTable.sizeBytes] = sizeBytes
            row[MediaFilesTable.mimeType] = mimeType
            row[MediaFilesTable.createdAt] = created
        }

        MediaFileRow(
            id = id.toString(),
            walkId = walkId.toString(),
            type = type,
            storagePath = storagePath,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
            createdAt = created
        )
    }

    fun findByWalkId(walkId: UUID): List<MediaFileRow> = transaction {
        MediaFilesTable
            .selectAll()
            .where { MediaFilesTable.walkId eq walkId }
            .map { r ->
                MediaFileRow(
                    id = r[MediaFilesTable.id].toString(),
                    walkId = r[MediaFilesTable.walkId].toString(),
                    type = r[MediaFilesTable.type],
                    storagePath = r[MediaFilesTable.storagePath],
                    sizeBytes = r[MediaFilesTable.sizeBytes],
                    mimeType = r[MediaFilesTable.mimeType],
                    createdAt = r[MediaFilesTable.createdAt]
                )
            }
    }
}