package com.upet.media

import com.upet.domain.model.MediaFileType
import kotlinx.datetime.LocalDateTime

data class MediaFileRow(
    val id: String,
    val walkId: String,
    val type: MediaFileType,
    val storagePath: String,
    val sizeBytes: Long,
    val mimeType: String,
    val createdAt: LocalDateTime
)
