package me.scana.okgradle.data.repository

import okhttp3.Response

class HttpException(val response: Response) : RuntimeException() {

    override val message: String
        get() = exceptionMessage()

    fun code(): Int = response.code()

    private fun exceptionMessage(): String {
        return "HTTP ${response.code()}:${response.message()}"
    }
}
