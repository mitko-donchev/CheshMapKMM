package com.epicmillennium.cheshmap

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform