package de.dbaelz.siloshare.controller

import de.dbaelz.siloshare.model.Checklist
import de.dbaelz.siloshare.model.NewChecklistItem
import de.dbaelz.siloshare.model.Note
import de.dbaelz.siloshare.service.NoteService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateNoteRequest(
    @field:NotBlank
    val text: String,
    val checklist: CreateChecklistRequest? = null
)

data class CreateChecklistRequest(
    val items: List<CreateChecklistItemRequest> = listOf()
)

data class CreateChecklistItemRequest(
    @field:NotBlank
    val text: String,
    val done: Boolean = false
)

data class PutChecklistRequest(
    val items: List<NewChecklistItem> = listOf()
)

data class PatchChecklistItemRequest(
    val text: String?,
    val done: Boolean?
)

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService
) {
    @PostMapping
    fun add(@Valid @RequestBody request: CreateNoteRequest): ResponseEntity<Note> {
        val checklistItems = request.checklist?.items?.map { NewChecklistItem(it.text, it.done) }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(noteService.add(request.text, checklistItems))
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<Note>> {
        return ResponseEntity.ok(noteService.getAll())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        return if (noteService.delete(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}/checklist")
    fun updateChecklist(
        @PathVariable id: String,
        @RequestBody request: PutChecklistRequest
    ): ResponseEntity<Note> {
        val items = request.items.map { NewChecklistItem(it.text, it.done) }
        val note = noteService.addChecklist(id, items)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(note)
    }

    @GetMapping("/{id}/checklist")
    fun getChecklist(@PathVariable id: String): ResponseEntity<Checklist> {
        val checklist = noteService.getChecklist(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(checklist)
    }

    @PostMapping("/{id}/checklist/items")
    fun addChecklistItem(
        @PathVariable id: String,
        @Valid @RequestBody request: CreateChecklistItemRequest
    ): ResponseEntity<Checklist> {
        val item = noteService.addChecklistItem(id, request.text)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(item)
    }

    @PatchMapping("/{id}/checklist/items/{itemId}")
    fun updateChecklistItem(
        @PathVariable id: String,
        @PathVariable itemId: String,
        @RequestBody request: PatchChecklistItemRequest
    ): ResponseEntity<Checklist> {
        val item = noteService.updateChecklistItem(id, itemId, request.text, request.done)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(item)
    }

    @DeleteMapping("/{id}/checklist/items/{itemId}")
    fun deleteChecklistItem(
        @PathVariable id: String,
        @PathVariable itemId: String
    ): ResponseEntity<Void> {
        return if (noteService.deleteChecklistItem(id, itemId)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}/checklist")
    fun deleteChecklist(@PathVariable id: String): ResponseEntity<Void> {
        return if (noteService.deleteChecklist(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
