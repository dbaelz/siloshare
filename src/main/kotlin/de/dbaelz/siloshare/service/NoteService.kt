package de.dbaelz.siloshare.service

import de.dbaelz.siloshare.model.Note
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface NoteService {
    fun add(text: String): Note
    fun getAll(): List<Note>
}

@Service
class InMemoryNoteService(
    @Value("\${notes.remove-duration-seconds:$DEFAULT_REMOVE_SECONDS}")
    private val removeDurationSeconds: Long
) : NoteService {
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
        val removeTime = Instant.now().minusSeconds(removeDurationSeconds)

        synchronized(entries) {
            val validEntries = entries.entries
                .filter { it.value.timestamp.isAfter(removeTime) }
                .associate { it.key to it.value }

            entries.clear()
            entries.putAll(validEntries)

            // Return a new array list to avoid issues with GraalVM native image
            return ArrayList(entries.values)
        }
    }

    private companion object {
        const val DEFAULT_REMOVE_SECONDS = 600L
    }
}
