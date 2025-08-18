package de.dbaelz.siloshare.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

class InMemoryNoteServiceTest {
    private lateinit var noteService: InMemoryNoteService

    @BeforeEach
    fun setUp() {
        noteService = InMemoryNoteService(1)
    }

    @Test
    fun `add should store and return a note`() {
        val text = "Test note"

        val note = noteService.add(text)

        assertEquals(text, note.text)
        assertNotNull(note.id)
        assertTrue(note.timestamp.isBefore(Instant.now().plusSeconds(1)))
    }

    @Test
    fun `getAll should return all notes within remove duration`() {
        val note1 = noteService.add("Note 1")
        val note2 = noteService.add("Note 2")

        val notes = noteService.getAll()

        assertEquals(2, notes.size)
        assertEquals(note1.id, notes[0].id)
        assertEquals(note2.id, notes[1].id)
    }

    @Test
    fun `getAll should remove all notes older than remove duration`() {
        noteService.add("Note 1")
        noteService.add("Note 2")

        TimeUnit.SECONDS.sleep(2)

        val notes = noteService.getAll()

        assertTrue(notes.isEmpty())
    }

    @Test
    fun `getAll should remove only notes older than remove duration`() {
        noteService.add("Note 1")

        TimeUnit.SECONDS.sleep(2)

        val expected = noteService.add("Note 2")

        val notes = noteService.getAll()

        assertEquals(1, notes.size)
        assertEquals(expected, notes.first())
    }

    @Test
    fun `delete should remove the note and return true`() {
        val note = noteService.add("To be deleted")

        val deleted = noteService.delete(note.id)

        assertTrue(deleted)
        assertTrue(noteService.getAll().none { it.id == note.id })
    }

    @Test
    fun `delete should return false for non-existent note`() {
        val deleted = noteService.delete(UUID.randomUUID().toString())

        assertFalse(deleted)
    }
}
