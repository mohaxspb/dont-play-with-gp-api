package ru.kuchanov.gp.filter

import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class GpOAuth2AuthenticationProcessingFilter: OAuth2AuthenticationProcessingFilter() {

//    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
//        val httpRequest = req as HttpServletRequest
//        println("GpOAuth2AuthenticationProcessingFilter httpRequest: ${httpRequest.requestURL}")
//        println("GpOAuth2AuthenticationProcessingFilter httpRequest.cookies: ${httpRequest.cookies.asList()}")
//        httpRequest.cookies?.asList()?.forEach {
//            println("Cookie ${it.name}: ${it.value}")
//        }
//        println("GpOAuth2AuthenticationProcessingFilter httpRequest.headerNames: ${httpRequest.headerNames.toList()}")
//        httpRequest.headerNames.toList().forEach {
//            println("Header $it: ${httpRequest.getHeader(it)}")
//        }
//        println("GpOAuth2AuthenticationProcessingFilter httpRequest.userPrincipal: ${httpRequest.userPrincipal}")
//
//        println("GpOAuth2AuthenticationProcessingFilter req: $req")
//        println("GpOAuth2AuthenticationProcessingFilter req: $req")
//        println("GpOAuth2AuthenticationProcessingFilter: ${req.parameterNames.toList()}")
//        println("GpOAuth2AuthenticationProcessingFilter req.parameterMap: ${req.parameterMap.values.map { it.toList() }}")
//        super.doFilter(req, res, chain)
//    }
}
