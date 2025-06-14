package com.project.fileviewer.data.model

import java.io.File

data class FileModel(
    val file: File,
    val isDirectory: Boolean = file.isDirectory,
    var children: List<FileModel> = emptyList(),
    var isExpanded: Boolean = false
)