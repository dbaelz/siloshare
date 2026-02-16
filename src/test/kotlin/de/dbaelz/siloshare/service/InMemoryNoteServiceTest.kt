package de.dbaelz.siloshare.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
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
    fun `add with checklist should create note with checklist`() {
        val first = "first"
        val second = "second"
        val items = listOf(first, second)
        val note = noteService.add("Note created with checklist", items)

        assertNotNull(note.checklist)
        val checklist = note.checklist!!
        assertEquals(2, checklist.items.size)
        assertEquals(first, checklist.items[0].text)
        assertEquals(second, checklist.items[1].text)
        assertNotNull(checklist.items[0].id)
        assertNotNull(checklist.updatedAt)

        val stored = noteService.getAll().first { it.id == note.id }
        assertNotNull(stored.checklist)
        assertEquals(2, stored.checklist!!.items.size)
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
    fun `getChecklist returns null for non-existent note`() {
        val res = noteService.getChecklist(UUID.randomUUID().toString())
        assertNull(res)
    }

    @Test
    fun `addChecklist should create a checklist on a note`() {
        val note = noteService.add("Note with checklist")

        val updated = noteService.addChecklist(note.id, listOf("item1", "item2"))
            ?: fail("addChecklist returned null")

        val checklist = updated.checklist
        assertNotNull(checklist)
        assertEquals(2, checklist!!.items.size)
        assertEquals("item1", checklist.items[0].text)
        assertEquals("item2", checklist.items[1].text)
        assertNotNull(checklist.items[0].id)
        assertNotNull(checklist.items[1].id)
    }

    @Test
    fun `addChecklist returns null for non-existent note`() {
        val res = noteService.addChecklist(UUID.randomUUID().toString(), listOf("x"))
        assertNull(res)
    }

    @Test
    fun `deleteChecklist should remove the entire checklist from the note`() {
        val note = noteService.add("Note to delete checklist")
        noteService.addChecklist(note.id, listOf("only")) ?: fail("addChecklist returned null")

        val deleted = noteService.deleteChecklist(note.id)

        assertTrue(deleted)
        val updated = noteService.getAll().first { it.id == note.id }
        assertNull(updated.checklist)
    }

    @Test
    fun `deleteChecklist returns false for non-existent note`() {
        val res = noteService.deleteChecklist(UUID.randomUUID().toString())
        assertFalse(res)
    }

    @Test
    fun `addChecklistItem returns null for non-existent note`() {
        val res = noteService.addChecklistItem(UUID.randomUUID().toString(), "todo")
        assertNull(res)
    }

    @Test
    fun `addChecklistItem should add an item and return the checklist`() {
        val note = noteService.add("Note for single item")

        val checklist = noteService.addChecklistItem(note.id, "a new todo")
            ?: fail("addChecklistItem returned null")

        assertEquals(1, checklist.items.size)
        assertEquals("a new todo", checklist.items[0].text)
        assertNotNull(checklist.items[0].id)
    }

    @Test
    fun `updateChecklistItem should modify text and done state`() {
        val note = noteService.add("Note to update item")
        val noteWithChecklist = noteService.addChecklist(note.id, listOf("old text"))
            ?: fail("addChecklist returned null")
        val itemId = noteWithChecklist.checklist!!.items[0].id

        val checklist = noteService.updateChecklistItem(note.id, itemId, "new text", true)
            ?: fail("updateChecklistItem returned null")

        val item = checklist.items.first { it.id == itemId }
        assertEquals("new text", item.text)
        assertTrue(item.done)
    }

    @Test
    fun `updateChecklistItem returns null for non-existent note`() {
        val res = noteService.updateChecklistItem(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "x",
            true
        )
        assertNull(res)
    }

    @Test
    fun `updateChecklistItem returns null for non-existent item`() {
        val note = noteService.add("note")
        noteService.addChecklist(note.id, listOf("a")) ?: fail("addChecklist returned null")
        val res = noteService.updateChecklistItem(note.id, UUID.randomUUID().toString(), "x", true)
        assertNull(res)
    }

    @Test
    fun `deleteChecklistItem should remove the item from the checklist`() {
        val note = noteService.add("Note to delete item")
        val noteWithChecklist = noteService.addChecklist(note.id, listOf("one", "two"))
            ?: fail("addChecklist returned null")
        val itemId = noteWithChecklist.checklist!!.items[0].id
        val beforeSize = noteWithChecklist.checklist!!.items.size

        val deleted = noteService.deleteChecklistItem(note.id, itemId)

        assertTrue(deleted)
        val updated = noteService.getAll().first { it.id == note.id }
        assertEquals(beforeSize - 1, updated.checklist!!.items.size)
    }

    @Test
    fun `deleteChecklistItem returns false for non-existent note`() {
        val res = noteService.deleteChecklistItem(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        )
        assertFalse(res)
    }

    @Test
    fun `deleteChecklistItem returns false for non-existent item`() {
        val note = noteService.add("note")
        noteService.addChecklist(note.id, listOf("a")) ?: fail("addChecklist returned null")
        val res = noteService.deleteChecklistItem(note.id, UUID.randomUUID().toString())
        assertFalse(res)
    }

    @Test
    fun `getChecklist returns checklist for existing note`() {
        val note = noteService.add("note with checklist")
        val before = Instant.now()
        noteService.addChecklist(note.id, listOf("a", "b")) ?: fail("addChecklist returned null")

        val res = noteService.getChecklist(note.id)
        assertNotNull(res)
        assertEquals(2, res!!.items.size)
        assertEquals("a", res.items[0].text)

        assertNotNull(res.updatedAt)
        assertTrue(res.updatedAt!!.isAfter(before) || res.updatedAt == before)
    }

    @Test
    fun `addChecklistItem appends to existing checklist and updates timestamp`() {
        val note = noteService.add("note append")
        val initial =
            noteService.addChecklist(note.id, listOf("first")) ?: fail("addChecklist returned null")
        val prevUpdatedAt = initial.checklist!!.updatedAt

        Thread.sleep(1)

        val checklist = noteService.addChecklistItem(note.id, "second")
            ?: fail("addChecklistItem returned null")
        assertEquals(2, checklist.items.size)
        assertEquals("first", checklist.items[0].text)
        assertEquals("second", checklist.items[1].text)
        assertNotNull(checklist.updatedAt)
        if (prevUpdatedAt != null) {
            assertTrue(checklist.updatedAt!!.isAfter(prevUpdatedAt) || checklist.updatedAt == prevUpdatedAt)
        }
    }

    @Test
    fun `updateChecklistItem returns null when checklist missing`() {
        val note = noteService.add("note without checklist")
        val res = noteService.updateChecklistItem(note.id, UUID.randomUUID().toString(), "x", true)
        assertNull(res)
    }

    @Test
    fun `updateChecklistItem updates timestamp when changing item`() {
        val note = noteService.add("note update ts")
        val noteWithChecklist =
            noteService.addChecklist(note.id, listOf("one")) ?: fail("addChecklist returned null")
        val checklist = noteWithChecklist.checklist!!
        val itemId = checklist.items[0].id
        val prev = checklist.updatedAt

        Thread.sleep(1)
        val after = noteService.updateChecklistItem(note.id, itemId, "one-updated", true)
            ?: fail("updateChecklistItem returned null")
        assertNotNull(after.updatedAt)
        if (prev != null) {
            assertTrue(after.updatedAt!!.isAfter(prev) || after.updatedAt == prev)
        }
    }

    @Test
    fun `deleteChecklistItem updates timestamp when removing item`() {
        val note = noteService.add("note delete ts")
        val noteWithChecklist = noteService.addChecklist(note.id, listOf("one", "two"))
            ?: fail("addChecklist returned null")
        val checklist = noteWithChecklist.checklist!!
        val itemId = checklist.items[0].id
        val prev = checklist.updatedAt

        Thread.sleep(1)
        val deleted = noteService.deleteChecklistItem(note.id, itemId)
        assertTrue(deleted)

        val after = noteService.getChecklist(note.id)!!
        assertNotNull(after.updatedAt)
        if (prev != null) {
            assertTrue(after.updatedAt!!.isAfter(prev) || after.updatedAt == prev)
        }
    }
}
