package de.dbaelz.siloshare.service

import de.dbaelz.siloshare.model.Checklist
import de.dbaelz.siloshare.model.ChecklistItem
import de.dbaelz.siloshare.model.Note
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface NoteService {
    fun add(text: String, checklistItems: List<String>? = null): Note
    fun delete(id: String): Boolean
    fun getAll(): List<Note>

    fun addChecklist(
        noteId: String,
        items: List<String>
    ): Note?

    fun deleteChecklist(noteId: String): Boolean

    fun addChecklistItem(noteId: String, text: String): Checklist?
    fun updateChecklistItem(
        noteId: String,
        itemId: String,
        text: String?,
        done: Boolean?
    ): Checklist?

    fun getChecklist(noteId: String): Checklist?

    fun deleteChecklistItem(noteId: String, itemId: String): Boolean

}

@Service
class InMemoryNoteService(
    @param:Value("\${notes.remove-duration-seconds:$DEFAULT_REMOVE_SECONDS}")
    private val removeDurationSeconds: Long
) : NoteService {
    private val entries = ConcurrentHashMap<String, Note>()

    override fun add(text: String, checklistItems: List<String>?): Note {
        val id = UUID.randomUUID().toString()

        val entry = Note(
            id = id,
            timestamp = Instant.now(),
            text = text
        )

        if (!checklistItems.isNullOrEmpty()) {
            entry.checklist = Checklist(
                items = checklistItems.map {
                    ChecklistItem(
                        id = UUID.randomUUID().toString(),
                        text = it
                    )
                }.toMutableList(),
                updatedAt = Instant.now()
            )
        }

        entries[id] = entry

        return entry
    }

    override fun delete(id: String): Boolean {
        return entries.remove(id) != null
    }

    override fun getAll(): List<Note> {
        synchronized(entries) {
            if (removeDurationSeconds <= 0L) {
                return ArrayList(entries.values.sortedBy { it.timestamp })
            }

            val validEntries = entries.entries
                .filter {
                    it.value.timestamp.isAfter(
                        Instant.now().minusSeconds(removeDurationSeconds)
                    )
                }
                .associate { it.key to it.value }

            entries.clear()
            entries.putAll(validEntries)

            // Return a new array list to avoid issues with GraalVM native image
            return ArrayList(entries.values.sortedBy { it.timestamp })
        }
    }

    override fun addChecklist(
        noteId: String,
        items: List<String>
    ): Note? {
        val note = entries[noteId] ?: return null

        synchronized(note) {
            note.checklist = Checklist(
                items = items.map {
                    ChecklistItem(
                        id = UUID.randomUUID().toString(),
                        text = it
                    )
                }.toMutableList()
            )

            return note
        }
    }

    override fun getChecklist(noteId: String): Checklist? {
        val note = entries[noteId] ?: return null

        synchronized(note) {
            return note.checklist
        }
    }

    override fun deleteChecklist(noteId: String): Boolean {
        val note = entries[noteId] ?: return false

        synchronized(note) {
            note.checklist = null
            return true
        }
    }

    override fun addChecklistItem(noteId: String, text: String): Checklist? {
        val note = entries[noteId] ?: return null

        synchronized(note) {
            val checklist = note.checklist ?: Checklist().also { note.checklist = it }

            checklist.items.add(
                ChecklistItem(
                    id = UUID.randomUUID().toString(),
                    text = text
                )
            )
            checklist.updatedAt = Instant.now()

            return checklist
        }
    }

    override fun updateChecklistItem(
        noteId: String,
        itemId: String,
        text: String?,
        done: Boolean?
    ): Checklist? {
        val note = entries[noteId] ?: return null

        synchronized(note) {
            val checklist = note.checklist ?: return null

            val item = checklist.items.firstOrNull { it.id == itemId } ?: return null
            var changed = false
            if (text != null) {
                item.text = text
                changed = true
            }
            if (done != null) {
                item.done = done
                changed = true
            }
            if (changed) checklist.updatedAt = Instant.now()

            return checklist
        }
    }

    override fun deleteChecklistItem(noteId: String, itemId: String): Boolean {
        val note = entries[noteId] ?: return false

        synchronized(note) {
            val checklist = note.checklist ?: return false
            val removed = checklist.items.removeIf { it.id == itemId }
            if (removed) checklist.updatedAt = Instant.now()

            return removed
        }
    }

    private companion object {
        const val DEFAULT_REMOVE_SECONDS = 600L
    }
}
