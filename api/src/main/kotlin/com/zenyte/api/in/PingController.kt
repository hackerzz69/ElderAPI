package com.zenyte.api.`in`

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @author Corey
 * @since 30/04/19
 */
@RestController
@RequestMapping("/ping")
object PingController {
    @PostMapping
    fun request(@RequestParam payload: String) = if (payload == "ping") "pong" else "error"
}