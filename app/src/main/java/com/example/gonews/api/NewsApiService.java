package com.example.gonews.api; // Make sure this package is correct for your NewsApiService

import com.example.gonews.model.NewsResponse; // Corrected: Import NewsResponse
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    // NewsData.io uses the 'latest' endpoint for trending/headline news
    // Example: https://newsdata.io/api/1/latest?country=id&apikey=YOUR_API_KEY&language=id
    @GET("1/latest") // Note the API version '1' here
    Call<NewsResponse> getLatestNews( // Corrected: Use NewsResponse
                                      @Query("country") String country, // For country (e.g., "id" for Indonesia)
                                      @Query("apikey") String apiKey, // Your API key (note 'apikey' not 'apiKey')
                                      @Query("language") String language, // Recommendation: add language (e.g., "id")
                                      @Query("page") String page // NewsData.io uses the 'page' parameter for pagination
    );

    // For news by category
    // Same endpoint, just need to add the 'category' parameter
    // Example: https://newsdata.io/api/1/latest?country=id&category=health&apikey=YOUR_API_KEY&language=id
    @GET("1/latest")
    Call<NewsResponse> getCategoryNews( // Corrected: Use NewsResponse
                                        @Query("country") String country,
                                        @Query("category") String category, // Category (e.g., "health", "technology")
                                        @Query("apikey") String apiKey,
                                        @Query("language") String language,
                                        @Query("page") String page
    );

    // Endpoint to search for news by keyword
    // Example: https://newsdata.io/api/1/latest?q=android&apikey=YOUR_API_KEY&language=id
    @GET("1/latest")
    Call<NewsResponse> searchNews( // Corrected: Use NewsResponse
                                   @Query("q") String query, // Search keyword
                                   @Query("apikey") String apiKey,
                                   @Query("language") String language,
                                   @Query("page") String page
    );
}