package com.zenyte.api.`in`

import com.zenyte.api.model.AntiknoxEndpointResult
import com.zenyte.sql.query.AntiknoxCheckQuery
import com.zenyte.sql.query.IpVerificationQuery
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

@RestController
@RequestMapping("/ip")
class IpVerificationController {
    
    @GetMapping("/check/{ip}")
    fun check(@PathVariable ip: String): Boolean {
        return (IpVerificationQuery(ip).getResults().first as IpVerificationQuery.IpVerificationResult).success
    }

    @GetMapping("/antiknox/{ip}")
    fun antiknox(@PathVariable ip: String): AntiknoxEndpointResult {
        val results = AntiknoxCheckQuery(ip).getResults().first as AntiknoxCheckQuery.AntiknoxResult
        val digest = results.digest
        val legit = results.legit
        return AntiknoxEndpointResult(digest, legit)
    }
}