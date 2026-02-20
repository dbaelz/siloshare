package de.dbaelz.siloshare.controller

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {
    private val authHeader: String =
        "Basic " + Base64.getEncoder().encodeToString("user:password".toByteArray())

    @Test
    fun `unauthorized access to GET notes returns 401`() {
        mockMvc.perform(get("/api/notes"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create and get note with basic auth`() {
        val noteText = "integration test note"
        val id = createNoteReturnId(noteText)

        mockMvc.perform(get("/api/notes").header("Authorization", authHeader))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(id)))
    }

    @Test
    fun `create note without checklist returns created with null checklist`() {
        val req = mapOf("text" to "note without checklist")
        val content = objectMapper.writeValueAsString(req)

        val mvcResult = mockMvc.perform(
            post("/api/notes")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val node = objectMapper.readTree(mvcResult.response.contentAsString)
        assert(node.get("id").asString().isNotEmpty())
        assert(node.get("checklist") == null || node.get("checklist").isNull)
    }

    @Test
    fun `create note with checklist returns created with checklist items`() {
        val todo1 = "todo1"
        val todo2 = "todo2"
        val req = mapOf(
            "text" to "note with checklist via post",
            "checklist" to mapOf("items" to listOf(mapOf("text" to todo1), mapOf("text" to todo2)))
        )
        val content = objectMapper.writeValueAsString(req)

        val mvcResult = mockMvc.perform(
            post("/api/notes")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val node = objectMapper.readTree(mvcResult.response.contentAsString)
        val checklist = node.get("checklist")
        assertNotNull(checklist)
        val items = checklist.get("items")
        assert(items.size() == 2)
        assert(items[0].get("text").asString() == todo1)
        assert(items[1].get("text").asString() == todo2)
    }

    @Test
    fun `delete note with basic auth`() {
        val noteText = "note to delete"
        val id = createNoteReturnId(noteText)

        mockMvc.perform(delete("/api/notes/$id").header("Authorization", authHeader))
            .andExpect(status().isNoContent)

        mockMvc.perform(delete("/api/notes/$id").header("Authorization", authHeader))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `put checklist on note should return checklist`() {
        val id = createNoteReturnId("checklist note")
        val first = "first"
        val second = "second"
        val req = mapOf("items" to listOf(mapOf("text" to first, "done" to true), mapOf("text" to second)))
        val content = objectMapper.writeValueAsString(req)

        val mvcResult = mockMvc.perform(
            put("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isOk)
            .andReturn()

        val node = objectMapper.readTree(mvcResult.response.contentAsString)
        val items = node.get("checklist").get("items")

        assert(items.size() == 2)
        assert(items[0].get("text").asString() == first)
        assertTrue(items[0].get("done").asBoolean())
        assert(items[1].get("text").asString() == second)
        assertFalse(items[1].get("done").asBoolean())
    }

    @Test
    fun `post checklist item should add item and return checklist`() {
        val id = createNoteReturnId("single-item note")
        val todoText = "todo item"
        val req = mapOf("text" to todoText)
        val content = objectMapper.writeValueAsString(req)

        val mvcResult = mockMvc.perform(
            post("/api/notes/$id/checklist/items")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val node = objectMapper.readTree(mvcResult.response.contentAsString)
        val items = node.get("items")
        assert(items.size() == 1)
        assert(items[0].get("text").asString() == todoText)
        assert(items[0].get("id").asString().isNotEmpty())
    }

    @Test
    fun `patch checklist item should update item text and done flag`() {
        val id = createNoteReturnId("update-item note")
        val oldText = "old"
        val createReq = mapOf("text" to oldText)
        val createContent = objectMapper.writeValueAsString(createReq)
        val createResult = mockMvc.perform(
            post("/api/notes/$id/checklist/items")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createContent)
        )
            .andExpect(status().isCreated)
            .andReturn()

        val createdNode = objectMapper.readTree(createResult.response.contentAsString)
        val itemId = createdNode.get("items").get(0).get("id").asString()

        val updatedText = "updated"
        val doneFlag = true
        val patchReq = mapOf("text" to updatedText, "done" to doneFlag)
        val patchContent = objectMapper.writeValueAsString(patchReq)

        val patchResult = mockMvc.perform(
            patch("/api/notes/$id/checklist/items/$itemId")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchContent)
        )
            .andExpect(status().isOk)
            .andReturn()

        val patched = objectMapper.readTree(patchResult.response.contentAsString)
        val patchedItem = patched.get("items").first { it.get("id").asString() == itemId }
        assert(patchedItem.get("text").asString() == updatedText)
        assert(patchedItem.get("done").asBoolean())
    }

    @Test
    fun `delete checklist item should remove item from checklist`() {
        val id = createNoteReturnId("delete-item note")
        val first = "one"
        val second = "two"
        val putReq = mapOf("items" to listOf(mapOf("text" to first), mapOf("text" to second)))
        val putContent = objectMapper.writeValueAsString(putReq)

        val putResult = mockMvc.perform(
            put("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(putContent)
        )
            .andExpect(status().isOk)
            .andReturn()

        val putNode = objectMapper.readTree(putResult.response.contentAsString)
        val itemsNode = putNode.get("checklist").get("items")
        val removeId = itemsNode.get(0).get("id").asString()

        mockMvc.perform(
            delete("/api/notes/$id/checklist/items/$removeId")
                .header("Authorization", authHeader)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
        )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(second)))
            .andExpect(content().string(containsString("id")))
    }

    @Test
    fun `delete checklist should remove checklist from note`() {
        val id = createNoteReturnId("delete-checklist note")
        val only = "only"
        val putReq = mapOf("items" to listOf(mapOf("text" to only)))
        val putContent = objectMapper.writeValueAsString(putReq)
        mockMvc.perform(
            put("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(putContent)
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            delete("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/notes/$id/checklist")
                .header("Authorization", authHeader)
        )
            .andExpect(status().isNotFound)
    }

    private fun createNoteReturnId(text: String = "integration test note"): String {
        val req = mapOf("text" to text)
        val content = objectMapper.writeValueAsString(req)

        val mvcResult = mockMvc.perform(
            post("/api/notes")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = mvcResult.response.contentAsString
        val node = objectMapper.readTree(responseBody)

        return node.get("id").asString()
    }
}
