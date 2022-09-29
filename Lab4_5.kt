import java.util.Scanner

class Lab4 : Runnable{
    private var flag = true
    override fun run() {
        while (flag){
            println("Lorem ipsum")
        }
    }

    fun stop(){
        flag = false
    }

}

class Lab5: Runnable{
    override fun run() {
        while (true){
            println("Lorem Ipsum")
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException){
                error("You are trying to kill the thread in wait")
            }
        }
    }

}

fun main() {
    val scanner = Scanner(System.`in`)
    println("Which solution to run? \n1 - Flags\n2 - Interrupted Exception")
    val sol = scanner.nextInt()
    if (sol == 1){
        val lab4 = Lab4()
        val thread = Thread(lab4)
        thread.start()
        Thread.sleep(2000)
        lab4.stop()
    }
    else{
        val lab5 = Lab5()
        val thread = Thread(lab5)
        thread.start()
        Thread.sleep(2000)
        thread.interrupt()
        val threads = Thread.getAllStackTraces().keys
        for (t:Thread in threads){
            t.interrupt()
        }
    }
}