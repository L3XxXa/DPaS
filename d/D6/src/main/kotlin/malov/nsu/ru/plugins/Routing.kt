package malov.nsu.ru.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import malov.nsu.ru.repository.ApplicationDAO
import malov.nsu.ru.repository.ApplicationDAOImpl
import malov.nsu.ru.serializators.BookRequestSerializer
import malov.nsu.ru.serializators.CheckinRequestSerializer
import malov.nsu.ru.serializators.airports.AirportResponseSerializer

fun Application.configureRouting() {
    val dao = ApplicationDAOImpl()
    dao.init()
    routing {
        get("/") {
            call.respondText(call.request.queryParameters["echo"]!!)
        }

        get("/api/v1/airports"){
            val airports = dao.getAirports()
            if (airports.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No airports found")
            }
            val airportsResponse = ArrayList<AirportResponseSerializer>()
            airports.forEach {
                airportsResponse.add(AirportResponseSerializer(it.code, it.airportCity, it.airportsName, it.timeZone, it.coordinates))
            }
            call.respond(HttpStatusCode.OK, airportsResponse)
        }

        get("/api/v1/cities"){
            call.respondText("List of all cities")

        }

        get("/api/v1/airportsCity"){
            if (call.request.queryParameters["city"] == null || call.request.queryParameters["city"] == ""){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified city to find")
            }
            val airports = dao.getAirportsCity(call.request.queryParameters["city"]!!)
            if (airports.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No airports found for city ${call.request.queryParameters["city"]}")
            }
            val airportsResponse = ArrayList<AirportResponseSerializer>()
            airports.forEach {
                airportsResponse.add(AirportResponseSerializer(it.code, it.airportCity, it.airportsName, it.timeZone, it.coordinates))
            }
            call.respond(HttpStatusCode.OK, airportsResponse)        }

        get("/api/v1/airportInboundSchedule"){
            if (call.request.queryParameters["airport"] == null || call.request.queryParameters["airport"] == ""){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified airport")
            }
            call.respondText(call.request.queryParameters["airport"]!!)
        }

        get("/api/v1/airportOutboundSchedule"){
            if (call.request.queryParameters["airport"] == null || call.request.queryParameters["airport"] == ""){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified airport")
            }
            call.respondText(call.request.queryParameters["airport"]!!)
        }

        get("/api/v1/routes") {
            if (call.request.queryParameters["origin"] == null || call.request.queryParameters["origin"] == "" ||
                call.request.queryParameters["destination"] == null || call.request.queryParameters["destination"] == "" ||
                call.request.queryParameters["time_from"] == null || call.request.queryParameters["time_from"] == "" ||
                call.request.queryParameters["time_to"] == null || call.request.queryParameters["time_to"] == "" ||
                call.request.queryParameters["booking_class"] == null || call.request.queryParameters["booking_class"] == "" ||
                call.request.queryParameters["connections"] == null || call.request.queryParameters["connections"] == ""
                ){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified all params")
            }
            call.respondText(call.request.queryParameters["origin"]!! +
                    call.request.queryParameters["destination"]!! +
                    call.request.queryParameters["time_from"]!! +
                    call.request.queryParameters["time_to"]!! +
                    call.request.queryParameters["booking_class"]!! +
                    call.request.queryParameters["connections"]!!)
        }

        put("/api/v1/book"){
            try {
                val request = call.receive<BookRequestSerializer>()
                call.respond(HttpStatusCode.OK, request)
            } catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }

        }

        put("/api/v1/checkin"){
            try {
                val request = call.receive<CheckinRequestSerializer>()
                call.respond(HttpStatusCode.OK, request)
            } catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
    }
}
