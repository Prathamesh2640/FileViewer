package com.project.fileviewer.data

import com.project.fileviewer.data.model.FileModel
import java.io.File

class FileManager {
    fun loadFiles(directory: File): List<FileModel> {
        return directory.listFiles()?.map { FileModel(it) }?.sortedWith(compareBy(
            { !it.isDirectory },
            { it.file.name.lowercase() }
        )) ?: emptyList()
    }

    fun searchFiles(directory: File, query: String): List<FileModel> {
        val results = mutableListOf<FileModel>()
        directory.listFiles()?.forEach { file ->
            if (file.name.contains(query, ignoreCase = true)) {
                results.add(FileModel(file))
            }
            if (file.isDirectory) {
                results.addAll(searchFiles(file, query))
            }
        }
        return results
    }

    fun calculateFolderSize(file: File): Long {
        return if (file.isDirectory) {
            file.listFiles()?.sumOf { calculateFolderSize(it) } ?: 0L
        } else {
            file.length()
        }
    }
}