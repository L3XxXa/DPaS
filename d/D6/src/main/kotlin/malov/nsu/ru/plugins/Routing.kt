package malov.nsu.ru.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import malov.nsu.ru.serializators.BookRequestSerializer
import malov.nsu.ru.serializators.CheckinRequestSerializer

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/api/v1/airports"){
            call.respondText("List of all airports")
        }
        get("/api/v1/cities"){
            call.respondText("List of all cities")
        }
        get("/api/v1/airportsCity"){
            call.respondText(call.request.queryParameters["city"]!!)
        }
        get("/api/v1/airportInboundSchedule"){
            call.respondText(call.request.queryParameters["airport"]!!)
        }
        get("/api/v1/airportOutboundSchedule"){
            call.respondText(call.request.queryParameters["airport"]!!)
        }
        get("/api/v1/routes") {
            call.respondText(call.request.queryParameters["origin"]!! +
                    call.request.queryParameters["destination"]!! +
                    call.request.queryParameters["time_from"]!! +
                    call.request.queryParameters["time_to"]!! +
                    call.request.queryParameters["booking_class"]!! +
                    call.request.queryParameters["connections"]!!)

        }
        put("/api/v1/book"){
            val request = call.receive<BookRequestSerializer>()
            call.respond(HttpStatusCode.OK, request)
        }
        put("/api/v1/checkin"){
            val request = call.receive<CheckinRequestSerializer>()
            call.respond(HttpStatusCode.OK, request)
        }
    }
}
