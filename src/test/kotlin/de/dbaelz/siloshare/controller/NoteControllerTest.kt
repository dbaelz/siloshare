package de.dbaelz.siloshare.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
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
        val id = createNoteReturnId()

        mockMvc.perform(get("/api/notes").header("Authorization", authHeader))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString(id)))
    }

    @Test
    fun `delete note with basic auth`() {
        val id = createNoteReturnId()

        mockMvc.perform(delete("/api/notes/$id").header("Authorization", authHeader))
            .andExpect(status().isNoContent)

        mockMvc.perform(delete("/api/notes/$id").header("Authorization", authHeader))
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
        return node.get("id").asText()
    }
}
