class Philosopher(id: Int, leftFork: String, rightFork:String) : Runnable{
    private val id: Int
    private val leftFork: String
    private val rightFork: String

    init {
        this.id = id
        this.leftFork = leftFork
        this.rightFork = rightFork
    }

    private fun eating(){
        println(State.EATING.toString())
        Thread.sleep((Math.random() * 2000).toLong())
    }

    private fun thinking(){
        println(State.THINKING.toString())
        Thread.sleep((Math.random() * 20).toLong())
    }

    override fun run() {
        while (true){
            thinking()
            synchronized(leftFork){
                println(State.TAKING_LEFT_FORK.toString() + ". Fork #$leftFork")
                synchronized(rightFork){
                    println(State.TAKING_RIGHT_FORK.toString() + ". Fork #$rightFork")
                    eating()
                    println(State.RELEASING_RIGHT_FORK.toString() + ". Fork #$rightFork")
                }
                println(State.RELEASING_LEFT_FORK.toString() + ". Fork #$leftFork")
            }
        }
    }
}

enum class State{
    THINKING {
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is thinking")
        }
    },
    EATING {
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is eating makaroni")
        }
    },
    RELEASING_LEFT_FORK{
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is releasing left fork")
        }
    },
    RELEASING_RIGHT_FORK{
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is releasing right fork")
        }
    },
    TAKING_LEFT_FORK{
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is taking left fork")
        }
    },
    TAKING_RIGHT_FORK{
        override fun toString(): String {
            return ("Philosopher #${Thread.currentThread().name} is taking right fork")
        }
    }
}

fun main(){
    val phAmount = 5
    val forksAmount = 5
    val philosophers = ArrayList<Thread>()
    val forks = Array(forksAmount){"$it"}
    for (i in 0 until phAmount) {
        if (i == 4){
            philosophers.add(Thread(Philosopher(i, forks[(i + 1) % 5], forks[i])))
            break
        }
        philosophers.add(Thread(Philosopher(i, forks[i], forks[(i + 1) % 5])))
    }
    philosophers.forEach { it.start() }
}

