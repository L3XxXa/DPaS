import kotlin.math.pow

fun main(args: Array<String>) {
    val threadAmount = args[0].toInt()
    val n = args[1].toInt()
    val counters = ArrayList<Counter>()
    val threads = ArrayList<Thread>()
    for (i in 0 until threadAmount){
        counters.add(Counter(threadAmount, i, n))
        threads.add(Thread(counters[i]))
    }
    for (i in 0 until threadAmount){
        threads[i].start()
    }
    var result = 0.0
    Thread.sleep((n/10).toLong())
    for (i in 0 until threadAmount){
        result += counters[i].result
    }
    println("Result of pi is ${result * 4}")


}

class Counter(threadAmount: Int, threadNum: Int, n: Int): Runnable{
    private val threadAmount: Int
    private val threadNum: Int
    private val n: Int
    var result = 0.0
        get(){
            return field
        }
    init {
        this.threadAmount = threadAmount
        this.threadNum = threadNum
        this.n = n
    }

    override fun run() {
        for (i in threadNum .. n step threadAmount){
            result += (-1.0).pow(i.toDouble()) / (2 * i + 1)
        }
    }
}