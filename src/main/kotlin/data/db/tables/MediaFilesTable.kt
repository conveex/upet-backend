package com.upet.data.db.tables

import com.upet.domain.model.MediaFileType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

object MediaFilesTable : Table("media_files") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val walkId = uuid("walk_id")
        .references(
            WalksTable.id,
            onDelete = ReferenceOption.CASCADE
        )

    val type = enumerationByName(
        name = "type",
        length = 30,
        klass = MediaFileType::class
    )

    val storagePath = varchar("storage_path", 512)
    val sizeBytes = long("size_bytes")
    val mimeType = varchar("mime_type", 100)

    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}