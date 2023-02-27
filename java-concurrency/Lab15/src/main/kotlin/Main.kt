fun main() {
    for (i in 1000 downTo 0 step 7){
        println("$i - 7 = ${i - 7}")
        Thread.sleep(500)
    }
}