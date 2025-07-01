package de.dbaelz.siloshare.controller

import de.dbaelz.siloshare.model.Note
import de.dbaelz.siloshare.service.NoteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService
) {
    @PostMapping
    fun add(@RequestBody text: String): ResponseEntity<Note> {
        return ResponseEntity.ok(noteService.add(text))
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<Note>> {
        return ResponseEntity.ok(noteService.getAll())
    }
}
