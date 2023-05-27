package malov.nsu.ru.plugins

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import malov.nsu.ru.entity.RouteEntity
import malov.nsu.ru.repository.ApplicationDAOImpl
import malov.nsu.ru.serializators.BookRequestSerializer
import malov.nsu.ru.serializators.CheckinRequestSerializer
import malov.nsu.ru.serializators.RoutesResponseSerializer
import malov.nsu.ru.serializators.airports.AirportResponseSerializer
import malov.nsu.ru.serializators.cities.CitiesResponseSerializer
import malov.nsu.ru.serializators.flights.ScheduledFlightsResponseSerializer

fun Application.configureRouting() {
    val dao = ApplicationDAOImpl()
    val gsonBuilder = GsonBuilder()
    val gson: Gson = gsonBuilder.create()
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
                call.request.queryParameters["booking_class"] == null || call.request.queryParameters["booking_class"] == "" ||
                call.request.queryParameters["connections"] == null || call.request.queryParameters["connections"] == ""
                ){
                call.respond(HttpStatusCode.BadRequest, "You didn't specified all params")
            }
            val origin = call.request.queryParameters["origin"]!!
            val destination = call.request.queryParameters["destination"]!!
            val departureDate = call.request.queryParameters["departure_date"]!!
            val bookingClass = call.request.queryParameters["booking_class"]!!
            val connections = call.request.queryParameters["connections"]!!
            var routes: MutableSet<RouteEntity> = mutableSetOf()
            when (connections.toInt()){
                1 -> {
                    routes = dao.getRouteWithOneConnection(origin.uppercase(), destination.uppercase(), departureDate, bookingClass)
                }
                2 -> {
                    routes = dao.getRouteWithOneConnection(origin.uppercase(), destination.uppercase(), departureDate, bookingClass)
                    routes.addAll(dao.getRouteWithTwoConnection(origin.uppercase(), destination.uppercase(), departureDate, bookingClass))
                }
                else -> {
                    call.respond(HttpStatusCode.BadRequest, "Sorry, I can't create routes for $connections connections")
                }
            }
            if (routes.isEmpty()){
                call.respond(HttpStatusCode.BadRequest, "No routes on $departureDate for $connections connections between $origin, $destination, with $bookingClass class")
            }
            val response: ArrayList<RoutesResponseSerializer> = ArrayList()
            routes.forEach {
                println(it)
                response.add(RoutesResponseSerializer(
                    it.connections, it.departureAirports, it.arrivalAirports, it.flights, it.price
                ))
            }

            call.respond(HttpStatusCode.OK, response)
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
