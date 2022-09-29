class Lab1: Runnable{
    override fun run() {
        for (i in 1..10){
            println("$i")
            Thread.sleep(1)
        }
    }

}

fun main() {
    Thread(Lab1()).start()

    Thread {
        for (i in 1..10){
            println("Thread $i")
            Thread.sleep(1)
        }
    }.start()
}