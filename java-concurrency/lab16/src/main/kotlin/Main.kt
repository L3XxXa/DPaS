import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

class Main {
    private val url = "https://jsonplaceholder.typicode.com/comments"
    private val httpClient: HttpClient = HttpClient.newBuilder().build()
    private val request = HttpRequest.newBuilder()
        .uri(URI.create("https://something.com"))
        .build();

    private fun waitUserInput(){
        println("Press SPACE for update")
        var input: String
        var isSpace = false
        while (!isSpace){
            input = readln()
            if (input == " "){
                isSpace = true
            }
        }
    }

    fun sendAsyncRequest(){

    }

}

fun main() {
    val main = Main()
    main.sendAsyncRequest()
}