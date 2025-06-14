package com.project.fileviewer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.fileviewer.R
import com.project.fileviewer.data.model.FileModel
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(
    private val onFileClick: (FileModel) -> Unit,
    private val onRenameClick: (FileModel) -> Unit,
    private val onDeleteClick: (FileModel) -> Unit
) : ListAdapter<FileModel, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)
        val fileName: TextView = itemView.findViewById(R.id.fileName)
        val fileSize: TextView = itemView.findViewById(R.id.fileSize)
        val fileDate: TextView = itemView.findViewById(R.id.fileDate)
        val renameButton: ImageButton = itemView.findViewById(R.id.renameButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileModel = getItem(position)
        with(holder) {
            fileName.text = fileModel.file.name
            fileIcon.setImageResource(if (fileModel.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)
            fileSize.text = if (fileModel.isDirectory) "Folder" else formatSize(fileModel.file.length())
            fileDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fileModel.file.lastModified())
            itemView.setOnClickListener { onFileClick(fileModel) }
            renameButton.setOnClickListener { onRenameClick(fileModel) }
            deleteButton.setOnClickListener { onDeleteClick(fileModel) }
            itemView.translationX = if (fileModel.isDirectory) 16f * (fileModel.file.path.split("/").size - 1) else 0f
            if (fileModel.isDirectory && fileModel.isExpanded) {
                fileIcon.rotation = 90f
                fileIcon.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.expand))
            } else {
                fileIcon.rotation = 0f
                fileIcon.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.collapse))
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size > 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format("%.1f %s", size, units[unitIndex])
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<FileModel>() {
    override fun areItemsTheSame(oldItem: FileModel, newItem: FileModel): Boolean =
        oldItem.file.absolutePath == newItem.file.absolutePath

    override fun areContentsTheSame(oldItem: FileModel, newItem: FileModel): Boolean =
        oldItem == newItem
}