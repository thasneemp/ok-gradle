package me.scana.okgradle.data.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class NetworkClient(private val okHttpClient: OkHttpClient) {

    fun execute(request: Request): Response {
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return response
        }
        throw HttpException(response)
    }

}
