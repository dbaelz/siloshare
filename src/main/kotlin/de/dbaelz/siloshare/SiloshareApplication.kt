package de.dbaelz.siloshare

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SiloshareApplication

fun main(args: Array<String>) {
	runApplication<SiloshareApplication>(*args)
}
