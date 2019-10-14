package ru.kuchanov.gp.util

import org.springframework.web.servlet.support.ServletUriComponentsBuilder

fun getFullServerAddress() = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
fun getServerAddress() = ServletUriComponentsBuilder.fromCurrentContextPath().build().scheme!! +
        "://" + ServletUriComponentsBuilder.fromCurrentContextPath().build().host