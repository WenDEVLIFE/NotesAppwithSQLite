package com.example.notesappwithsqlite.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.notesappwithsqlite.MainActivity
import com.example.notesappwithsqlite.R
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper
import com.example.notesappwithsqlite.model.Folder

class FolderAdapter(private var folders: List<Folder>, private val context: Context, private val username: String) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var db: NoteDatabaseHelper = NoteDatabaseHelper(context)

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvShowTitle)
        val dateTextView: TextView = itemView.findViewById(R.id.tvShowDate)
        val updateButton: ImageView = itemView.findViewById(R.id.ivNoteEdit)
        val deleteButton: ImageView = itemView.findViewById(R.id.ivDeleteNote)
        val openfolderButton: ImageView = itemView.findViewById(R.id.openfolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.titleTextView.text = folder.name
        holder.dateTextView.text = folder.date

        holder.updateButton.setOnClickListener {
            showEditDialog(folder)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteFolder(folder.id)
            refreshData(db.getAllFolders())
            Toast.makeText(context, "Folder deleted successfully!", Toast.LENGTH_SHORT).show()
        }

        holder.openfolderButton.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("FOLDER_ID", folder.id)
            intent.putExtra("FOLDER_NAME", folder.name)
            intent.putExtra("USERNAME", username)
            context.startActivity(intent)
        }
    }

    fun refreshData(newFolders: List<Folder>) {
        folders = newFolders
        notifyDataSetChanged()
    }

    private fun showEditDialog(folder: Folder) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogLayout = inflater.inflate(R.layout.dialog_custom, null)
        val input = dialogLayout.findViewById<EditText>(R.id.editTextFolderName)
        input.setText(folder.name)
        val btnOk = dialogLayout.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogLayout.findViewById<Button>(R.id.btnCancel)

        builder.setView(dialogLayout)
        val dialog = builder.create()

        btnOk.setOnClickListener {
            val newName = input.text.toString()
            folder.name = newName
            db.updateFolder(folder)
            refreshData(db.getAllFolders())
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }
}