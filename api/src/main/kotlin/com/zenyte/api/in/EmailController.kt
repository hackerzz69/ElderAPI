package com.zenyte.api.`in`

import com.zenyte.api.aws.SendEmailSES
import com.zenyte.api.model.EmailStruct
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
@RestController
@RequestMapping("/email")
object EmailController {

    @PostMapping("/send")
    fun email(@RequestBody request: EmailStruct): Boolean {
        return (SendEmailSES(request).getResults().first as SendEmailSES.AmazonSESSendResult).result
    }


}