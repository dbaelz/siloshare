package de.dbaelz.siloshare.controller

import de.dbaelz.siloshare.model.Note
import de.dbaelz.siloshare.service.NoteService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateNoteRequest(
    @field:NotBlank
    val text: String
)

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService
) {
    @PostMapping
    fun add(@Valid @RequestBody request: CreateNoteRequest): ResponseEntity<Note> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(noteService.add(request.text))
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<Note>> {
        return ResponseEntity.ok(noteService.getAll())
    }
}
