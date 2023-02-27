import java.util.concurrent.Semaphore

class Conveyor(time: Int, type: Char, semaphore: Array<Semaphore>) : Runnable{
    private val time: Int
    private val type: Char
    private val semaphoreA : Semaphore
    private val semaphoreB: Semaphore
    private val semaphoreC : Semaphore

    init{
        this.time = time
        this.type = type
        this.semaphoreA = semaphore[0]
        this.semaphoreB = semaphore[1]
        this.semaphoreC = semaphore[2]
    }

    override fun run() {
        while (true){
            when(type){
                'A' -> {
                    println("${Thread.currentThread().name} is doing part $type")
                    Thread.sleep(time.toLong())
                    semaphoreA.release()
                }
                'B' -> {
                    println("${Thread.currentThread().name} is doing part $type")
                    Thread.sleep(time.toLong())
                    semaphoreB.release()
                }
                'C' -> {
                    println("${Thread.currentThread().name} is doing part $type")
                    Thread.sleep(time.toLong())
                    semaphoreC.release()
                }
                'W' -> {
                    println("${Thread.currentThread().name} is doing part widget")
                    semaphoreA.acquire()
                    semaphoreB.acquire()
                    semaphoreC.acquire()
                    println("Widget has been done by ${Thread.currentThread().name}")
                }
            }
        }

    }

}

fun main() {
    val makePartsTime = arrayOf(1000, 2000, 3000, 0)
    val semaphore = Array(4){Semaphore(0)}
    val type = arrayOf('A', 'B', 'C', 'W')
    val conveyors = Array(4){Conveyor(makePartsTime[it], type[it], semaphore)}
    val threads = Array(4){Thread(conveyors[it])}
    threads.forEach { it.start() }
}