package com.zenyte.api.aws

import com.zenyte.api.APIRequestResult
import com.zenyte.api.APIRequestRunnable
import com.zenyte.api.auth.AWSCredential
import com.zenyte.api.model.EmailStruct
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*

/**
 * Sends an email via AWS SES.
 *
 * @author Noele
 * @since modernized for AWS SDK v2
 */
class SendEmailSES(private val email: EmailStruct) : APIRequestRunnable() {

    private val auth = AWSCredential(
        System.getenv("SENDY_AWS_ACCESS"),
        System.getenv("SENDY_AWS_SECRET")
    )

    private val fromEmail = "new@zenyte.com"

    override fun execute(): Pair<APIRequestResult, Exception?> {
        return try {
            val creds = AwsBasicCredentials.create(auth.accessId, auth.secretKey)
            SesClient.builder()
                .region(Region.US_EAST_1) // TODO: make configurable via env
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build()
                .use { client ->
                    val request = SendEmailRequest.builder()
                        .source(fromEmail)
                        .destination(
                            Destination.builder()
                                .toAddresses(email.toEmail)
                                .build()
                        )
                        .message(
                            Message.builder()
                                .subject(
                                    Content.builder()
                                        .charset("UTF-8")
                                        .data(email.subject)
                                        .build()
                                )
                                .body(
                                    Body.builder()
                                        .html(
                                            Content.builder()
                                                .charset("UTF-8")
                                                .data(email.htmlDigest)
                                                .build()
                                        )
                                        .text(
                                            Content.builder()
                                                .charset("UTF-8")
                                                .data(email.textDigest)
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()

                    client.sendEmail(request)
                    AmazonSESSendResult(true) to null
                }
        } catch (e: Exception) {
            AmazonSESSendResult(false) to e
        }
    }

    data class AmazonSESSendResult(override val result: Boolean) : APIRequestResult
}
