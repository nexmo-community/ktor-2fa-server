package com.example

import com.vonage.client.VonageClient
import com.vonage.client.verify.VerifyStatus
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.Serializable

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    // Retrieve API_KEY and API_SECRET from https://dashboard.nexmo.com/settings
    val client: VonageClient = VonageClient.builder()
        .apiKey("API_KEY")
        .apiSecret("API_SECRET")
        .build()

    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("2FA app is working", ContentType.Text.Html)
        }
        get("/verifyNumber") {
            val phoneNumber = call.parameters["phoneNumber"]
            require(!phoneNumber.isNullOrBlank()) { "phoneNumber is missing" }

            val ongoingVerify = client.verifyClient.verify(phoneNumber, "VONAGE")
            val response = VerifyNumberResponse(ongoingVerify.requestId)
            call.respond(response)
        }
        get("/verifyCode") {
            val code = call.parameters["code"]
            val requestId = call.parameters["requestId"]

            val checkResponse = client.verifyClient.check(requestId, code)
            println(checkResponse.status)

            val status = if(checkResponse.status == VerifyStatus.OK) {
                "OK"
            } else {
                "ERROR: ${checkResponse.status}"
            }

            val response = VerifyCodeResponse(status)
            call.respond(response)
        }
    }
}

@Serializable
data class VerifyNumberResponse(val requestId: String)

@Serializable
data class VerifyCodeResponse(val status: String)