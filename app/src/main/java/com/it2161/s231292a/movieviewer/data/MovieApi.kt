package com.it2161.s231292a.movieviewer.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MovieApi {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    val api: MovieService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovieService::class.java)
    }
}
