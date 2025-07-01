package de.dbaelz.siloshare.service

import de.dbaelz.siloshare.model.Note
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

interface NoteService {
    fun add(text: String): Note
    fun getAll(): List<Note>
}

class InMemoryNoteService : NoteService {
    private val entries = ConcurrentHashMap<String, Note>()

    override fun add(text: String): Note {
        val id = UUID.randomUUID().toString()

        val entry = Note(
            id = id,
            timestamp = Instant.now(),
            text = text
        )
        entries[id] = entry

        return entry
    }

    override fun getAll(): List<Note> {
        return entries.values.toList()
    }
}
