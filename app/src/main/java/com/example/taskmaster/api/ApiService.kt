package com.example.taskmaster.api

import java.net.HttpURLConnection
import java.net.URL

class ApiService {

    fun fetchData(): String {
        return try {
            val url = URL("https://jsonplaceholder.typicode.com/todos/1")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }
            } else {
                "Error: HTTP $responseCode"
            }

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}