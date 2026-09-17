package pl.joblin.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan(basePackages = ["pl.joblin"])
@EnableScheduling
class JoblinApplication

fun main(args: Array<String>) {
    runApplication<JoblinApplication>(*args)
}
