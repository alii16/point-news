package com.example.pointnews.api;

import okhttp3.OkHttpClient; // Import ini
import okhttp3.logging.HttpLoggingInterceptor; // Import ini
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit; // Import ini

public class ApiClient {

    private static final String BASE_URL = "https://newsapi.org/v2/"; // Base URL NewsAPI
    private static Retrofit retrofit = null;

    // Metode untuk mendapatkan instance Retrofit
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Buat HttpLoggingInterceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Sangat detail, akan menampilkan request/response body

            // Buat OkHttpClient dengan interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging) // Tambahkan logging interceptor di sini
                    .connectTimeout(30, TimeUnit.SECONDS) // Opsional: tambahkan timeout
                    .readTimeout(30, TimeUnit.SECONDS)   // Opsional: tambahkan timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Menggunakan GSON untuk konversi JSON
                    .client(client) // Gunakan OkHttpClient dengan interceptor yang baru dibuat
                    .build();
        }
        return retrofit;
    }
}