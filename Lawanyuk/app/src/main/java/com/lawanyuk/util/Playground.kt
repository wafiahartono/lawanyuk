package com.lawanyuk.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun main() = runBlocking {

}

suspend fun uploadPhoto1() {

    val a = coroutineScope {
        List(5) { it }.map {
            println("$this $it starting async")
            async {
                val failed = Random.nextBoolean()
                println("$this async $it. failed? $failed")
                if (failed) throw Exception()
            }
        }
    }.awaitAll()
}

suspend fun uploadPhoto2() {
    val a = List(5) { it }.map {
        coroutineScope {
            println("$this $it starting async")
            async {
                val failed = Random.nextBoolean()
                println("$this async $it. failed? $failed")
                if (failed) throw Exception()
            }
        }
    }.awaitAll()
}

//TODO 1. animate stfalcon image viewer on fragment create report, also make it non full screen
//     2. rename resources
//     3. hide photo rv in craete fragment when no photo present
