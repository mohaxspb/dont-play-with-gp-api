package ru.kuchanov.gp.util

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.InetAddress

fun getFullServerAddressForContext() = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

fun getServerAddressForContext() = ServletUriComponentsBuilder.fromCurrentContextPath().build().scheme!! +
        "://" + ServletUriComponentsBuilder.fromCurrentContextPath().build().host

fun getServerAddress(): String {
    val hostName = InetAddress.getLoopbackAddress().hostName
    val scheme = if (hostName == "localhost") "http://" else "https://"
    return scheme + hostName
}