package com.example.notesappwithsqlite.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.notesappwithsqlite.R
import com.example.notesappwithsqlite.UpdateNoteActivity
import com.example.notesappwithsqlite.databaseController.NoteDatabaseHelper
import com.example.notesappwithsqlite.model.Note

class NotesAdapter(private var notes: List<Note>, private val context: Context) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private var db: NoteDatabaseHelper = NoteDatabaseHelper(context)

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvShowTitle)
        val dateTextView: TextView = itemView.findViewById(R.id.tvShowDate)
        val subjectTextView: TextView = itemView.findViewById(R.id.tvShowSubjectTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.tvShowContent)
        val priorityTextView: TextView = itemView.findViewById(R.id.tvShowContent2)
        val updateButton: ImageView = itemView.findViewById(R.id.ivNoteEdit)
        val deleteButton: ImageView = itemView.findViewById(R.id.ivDeleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.dateTextView.text = note.date
        holder.subjectTextView.text = note.subject
        holder.contentTextView.text = note.content
        holder.priorityTextView.text = note.priority

        // Set the card background color based on the note priority
        val colorId = when (note.priority) {
            "High" -> R.color.priority_high
            "Medium" -> R.color.priority_medium
            "Low" -> R.color.priority_low
            else -> R.color.priority_low // Default to low if priority is not recognized
        }
        (holder.itemView as androidx.cardview.widget.CardView).setCardBackgroundColor(ContextCompat.getColor(context, colorId))

        holder.updateButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, UpdateNoteActivity::class.java).apply {
                putExtra("note_id", note.id)
                putExtra("folder_id", note.folderID)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            db.deleteNote(note.id)
            refreshData(db.getAllNotesByFolderId(note.folderID)) // Ensure RecyclerView updates after deleting
            Toast.makeText(holder.itemView.context, "Note Deleted", Toast.LENGTH_SHORT).show()
        }
    }
    fun refreshData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}