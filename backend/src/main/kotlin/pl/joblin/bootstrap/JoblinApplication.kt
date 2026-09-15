package pl.joblin.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["pl.joblin"])
class JoblinApplication

fun main(args: Array<String>) {
    runApplication<JoblinApplication>(*args)
}
