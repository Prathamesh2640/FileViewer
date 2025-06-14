package com.project.fileviewer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.fileviewer.data.FileManager
import com.project.fileviewer.data.model.FileModel
import java.io.File

class MainViewModel : ViewModel() {
    private val fileManager = FileManager()
    private val _files = MutableLiveData<List<FileModel>>()
    val files: LiveData<List<FileModel>> = _files
    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    fun loadFiles(directory: File) {
        _files.value = fileManager.loadFiles(directory)
        _currentPath.value = directory.absolutePath
    }

    fun searchFiles(directory: File, query: String) {
        _files.value = fileManager.searchFiles(directory, query)
    }

    fun getFolderSize(file: File): Long {
        return fileManager.calculateFolderSize(file)
    }
}