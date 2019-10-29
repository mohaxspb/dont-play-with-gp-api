package ru.kuchanov.gp

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener


@SpringBootApplication
@EnableScheduling
class Application : SpringBootServletInitializer() {

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    fun sessionListener(): ServletListenerRegistrationBean<HttpSessionListener> =
        ServletListenerRegistrationBean(object : HttpSessionListener {
            override fun sessionCreated(se: HttpSessionEvent?) {
                super.sessionCreated(se)
                se?.session?.maxInactiveInterval = 60 * 60 * 24 * 7 //one week
            }
        })

    @Bean
    fun logger(): Logger =
        LoggerFactory.getLogger("application")
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
