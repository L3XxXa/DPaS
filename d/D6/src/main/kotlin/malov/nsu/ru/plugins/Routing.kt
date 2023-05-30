package malov.nsu.ru.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import malov.nsu.ru.Validator
import malov.nsu.ru.entity.RouteEntity
import malov.nsu.ru.repository.ApplicationDAOImpl
import malov.nsu.ru.serializators.book.BookRequestSerializer
import malov.nsu.ru.serializators.checkin.CheckinRequestSerializer
import malov.nsu.ru.serializators.RoutesResponseSerializer
import malov.nsu.ru.serializators.airports.AirportResponseSerializer
import malov.nsu.ru.serializators.book.BookResponseSerializer
import malov.nsu.ru.serializators.checkin.CheckinResponseSerializer
import malov.nsu.ru.serializators.cities.CitiesResponseSerializer
import malov.nsu.ru.serializators.flights.ScheduledFlightsResponseSerializer
import java.util.*
import kotlin.collections.ArrayList

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
            val cities = dao.getCities()
            if (cities.isEmpty()){
                call.respond(HttpStatusCode.OK, "No cities found")
            }
            val citiesResponse = ArrayList<CitiesResponseSerializer>()
            cities.forEach {
                citiesResponse.add(CitiesResponseSerializer(it.city))
            }
            call.respond(HttpStatusCode.OK, citiesResponse)
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
            val flights = dao.getAirportInboundSchedule(call.request.queryParameters["airport"]!!.uppercase())
            if (flights.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No inbound flights for airport ${call.request.queryParameters["airport"]}")
            }
            val flightsResponse = ArrayList<ScheduledFlightsResponseSerializer>()
            flights.forEach {
                flightsResponse.add(ScheduledFlightsResponseSerializer(it.from, it.to, it.flight, it.departureDate))
            }
            call.respond(HttpStatusCode.OK, flightsResponse)
        }

        get("/api/v1/airportOutboundSchedule"){
            if (call.request.queryParameters["airport"] == null || call.request.queryParameters["airport"] == ""){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified airport")
            }
            val flights = dao.getAirportOutboundSchedule(call.request.queryParameters["airport"]!!.uppercase())
            if (flights.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No inbound flights for airport ${call.request.queryParameters["airport"]}")
            }
            val flightsResponse = ArrayList<ScheduledFlightsResponseSerializer>()
            flights.forEach {
                flightsResponse.add(ScheduledFlightsResponseSerializer(it.from, it.to, it.flight, it.departureDate))
            }
            call.respond(HttpStatusCode.OK, flightsResponse)
        }

        get("/api/v1/routes") {
            if (call.request.queryParameters["origin"] == null || call.request.queryParameters["origin"] == "" ||
                call.request.queryParameters["destination"] == null || call.request.queryParameters["destination"] == "" ||
                call.request.queryParameters["departure_date"] == null || call.request.queryParameters["departure_date"] == "" ||
                call.request.queryParameters["max_arrival_date"] == null || call.request.queryParameters["max_arrival_date"] == "" ||
                call.request.queryParameters["booking_class"] == null || call.request.queryParameters["booking_class"] == "" ||
                call.request.queryParameters["connections"] == null || call.request.queryParameters["connections"] == ""
                ){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified all params")
            }
            val origin = call.request.queryParameters["origin"]!!.uppercase()
            val destination = call.request.queryParameters["destination"]!!.uppercase()
            val departureDate = call.request.queryParameters["departure_date"]!!
            val maxArrivalDate = call.request.queryParameters["max_arrival_date"]!!
            val bookingClass = call.request.queryParameters["booking_class"]!!.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            val connections = call.request.queryParameters["connections"]!!.toInt()
            val routes: MutableSet<RouteEntity> = dao.getRoute(origin, destination, departureDate, maxArrivalDate, bookingClass, connections)
            val response: ArrayList<RoutesResponseSerializer> = ArrayList()
            routes.forEach {
                response.add(RoutesResponseSerializer(
                    it.route, it.departureAirport, it.arrivalAirport, it.scheduledDeparture, it.flightNo, it.count, it.price
                ))
            }
            if (routes.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No routes on $departureDate for $connections connections between $origin, $destination, with $bookingClass class")
            }

            call.respond(HttpStatusCode.OK, response)
        }

        put("/api/v1/book"){
            try {
                val request = call.receive<BookRequestSerializer>()
                Validator.validatePhone(request.contact_phone)
                Validator.validateEmail(request.contact_email)
                Validator.validateId(request.passenger_id)
                val ticketEntity = dao.bookPerson(request.date, request.flight_no, request.fare_condition, request.name, request.passenger_id, request.contact_phone, request.contact_email)
                val bookResponseSerializer = BookResponseSerializer(ticketEntity.ticket, ticketEntity.flight, ticketEntity.aircraft, ticketEntity.date, ticketEntity.price)
                call.respond(HttpStatusCode.OK, bookResponseSerializer)
            } catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
                e.printStackTrace()
            }
        }

        put("/api/v1/checkin"){
            try {
                val request = call.receive<CheckinRequestSerializer>()
                val checkin = dao.checkin(request.ticket_no, request.flight)
                call.respond(HttpStatusCode.OK, CheckinResponseSerializer(checkin.seatNo, checkin.boardingNo))
            } catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
                e.printStackTrace()
            }
        }
    }
}
