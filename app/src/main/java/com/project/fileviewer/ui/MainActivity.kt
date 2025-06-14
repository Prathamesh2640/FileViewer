package com.project.fileviewer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.fileviewer.R
import com.project.fileviewer.data.model.FileModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var fileAdapter: FileAdapter
    private val fileList = mutableListOf<FileModel>()
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        fileAdapter = FileAdapter(
            onFileClick = { fileModel -> handleFileClick(fileModel) },
            onRenameClick = { fileModel -> showRenameDialog(fileModel) },
            onDeleteClick = { fileModel -> handleDelete(fileModel) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFiles(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            loadFiles()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadFiles()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFiles(directory: File = Environment.getExternalStorageDirectory()) {
        fileList.clear()
        directory.listFiles()?.sortedBy { it.name.lowercase() }?.forEach { file ->
            fileList.add(FileModel(file))
        }
        fileAdapter.submitList(fileList.toList())
    }

    private fun handleFileClick(fileModel: FileModel) {
        if (fileModel.isDirectory) {
            fileModel.isExpanded = !fileModel.isExpanded
            if (fileModel.isExpanded) {
                val children = fileModel.file.listFiles()?.sortedBy { it.name.lowercase() }
                    ?.map { FileModel(it) } ?: emptyList()
                fileModel.children = children
                val newList = mutableListOf<FileModel>()
                fileList.forEach { parent ->
                    newList.add(parent)
                    if (parent == fileModel && parent.isExpanded) {
                        newList.addAll(children)
                    }
                }
                fileList.clear()
                fileList.addAll(newList)
                fileAdapter.submitList(fileList.toList())
            } else {
                fileList.removeAll { it in fileModel.children }
                fileModel.children = emptyList()
                fileAdapter.submitList(fileList.toList())
            }
        } else {
            Toast.makeText(this, "File: ${fileModel.file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRenameDialog(fileModel: FileModel) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rename, null)
        val renameEditText = dialogView.findViewById<EditText>(R.id.renameEditText)
        renameEditText.setText(fileModel.file.name)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Rename File")
            .setView(dialogView)
            .setPositiveButton("Rename", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newName = renameEditText.text.toString().trim()
                if (newName.isNotEmpty() && newName != fileModel.file.name) {
                    val newFile = File(fileModel.file.parent, newName)
                    if (fileModel.file.renameTo(newFile)) {
                        fileList[fileList.indexOf(fileModel)] = FileModel(newFile)
                        fileAdapter.submitList(fileList.toList())
                        dialog.dismiss()
                        Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun handleDelete(fileModel: FileModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${fileModel.file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (fileModel.file.delete()) {
                    fileList.remove(fileModel)
                    fileAdapter.submitList(fileList.toList())
                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun filterFiles(query: String) {
        val filteredList = if (query.isEmpty()) {
            fileList.toList()
        } else {
            fileList.filter { it.file.name.contains(query, ignoreCase = true) }
        }
        fileAdapter.submitList(filteredList)
    }
}