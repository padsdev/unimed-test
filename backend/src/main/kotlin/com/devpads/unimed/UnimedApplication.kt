package com.devpads.unimed

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UnimedApplication

fun main(args: Array<String>) {
	runApplication<UnimedApplication>(*args)
}
