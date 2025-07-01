package de.dbaelz.siloshare.controller

import de.dbaelz.siloshare.model.Note
import de.dbaelz.siloshare.service.NoteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

data class CreateNoteRequest(
    val text: String
)

@RestController
@RequestMapping("/api/notes")
@Validated
class NoteController(
    private val noteService: NoteService
) {
    @PostMapping
    fun add(@RequestBody request: CreateNoteRequest): ResponseEntity<Note> {
        if (request.text.isBlank()) {
            return ResponseEntity.badRequest().body(
                Note(
                    id = "error",
                    timestamp = java.time.Instant.now(),
                    text = "text can not be empty"
                )
            )
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(noteService.add(request.text))
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<Note>> {
        return ResponseEntity.ok(noteService.getAll())
    }
}
