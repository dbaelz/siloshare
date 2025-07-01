package de.dbaelz.siloshare

import de.dbaelz.siloshare.service.InMemoryNoteService
import de.dbaelz.siloshare.service.NoteService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class SiloshareApplication

@Configuration
class AppConfig {
    @Bean
    fun noteService(): NoteService {
        return InMemoryNoteService()
    }
}

fun main(args: Array<String>) {
    runApplication<SiloshareApplication>(*args)
}
