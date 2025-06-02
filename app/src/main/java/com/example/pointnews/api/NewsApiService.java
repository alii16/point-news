package com.example.pointnews.api;

import com.example.pointnews.model.NewsResponse; // Import kelas NewsResponse
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    // Endpoint untuk mendapatkan berita utama (Top Headlines)
    // https://newsapi.org/v2/top-headlines?country=id&apiKey=YOUR_API_KEY
    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country, // Untuk negara (misal: "id" untuk Indonesia)
            @Query("apiKey") String apiKey, // Kunci API Anda
            @Query("pageSize") int pageSize
    );

    // Endpoint untuk mendapatkan berita berdasarkan kategori (misal: kesehatan, teknologi)
    // https://newsapi.org/v2/top-headlines?country=id&category=health&apiKey=YOUR_API_KEY
    @GET("top-headlines")
    Call<NewsResponse> getCategoryHeadlines(
            @Query("country") String country,
            @Query("category") String category, // Kategori (misal: "health", "technology")
            @Query("apiKey") String apiKey,
            @Query("pageSize") int pageSize
    );

    // Endpoint untuk mencari berita berdasarkan kata kunci
    // https://newsapi.org/v2/everything?q=android&sortBy=relevancy&apiKey=YOUR_API_KEY
    @GET("everything")
    Call<NewsResponse> searchNews(
            @Query("q") String query, // Kata kunci pencarian
            @Query("sortBy") String sortBy, // Urutkan berdasarkan (misal: "publishedAt", "relevancy")
            @Query("apiKey") String apiKey,
            @Query("pageSize") int pageSize
    );
}