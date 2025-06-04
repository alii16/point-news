package com.example.gonews.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    // NewsData.io biasanya mengembalikan struktur ini:
    // "status": "success",
    // "totalResults": 12345, // NewsData.io menggunakan "totalResults" atau "total_results"
    // "results": [ { ... }, { ... }, ... ], // Daftar artikel
    // "nextPage": "unique_token_for_next_page" // Token untuk paginasi

    @SerializedName("status")
    private String status;

    @SerializedName("totalResults") // Atau "total_results" tergantung pada versi API atau konfigurasi
    private int totalResults;

    @SerializedName("results") // Menggantikan "articles" dari NewsAPI.org
    private List<Article> results; // List dari objek Article (yang sudah kita sesuaikan sebelumnya)

    @SerializedName("nextPage") // Ini adalah field baru yang penting untuk paginasi
    private String nextPage;

    // Constructor (opsional, Gson dapat melakukan deserialization tanpa constructor eksplisit)
    public NewsResponse() {
        // Default constructor for Gson
    }

    public NewsResponse(String status, int totalResults, List<Article> results, String nextPage) {
        this.status = status;
        this.totalResults = totalResults;
        this.results = results;
        this.nextPage = nextPage;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    // Perhatikan nama getter berubah dari getArticles() menjadi getResults()
    public List<Article> getResults() {
        return results;
    }

    public String getNextPage() {
        return nextPage;
    }

    // Setters (opsional, jika Anda perlu mengubah data setelah dibuat)
    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    // Perhatikan nama setter berubah dari setArticles() menjadi setResults()
    public void setResults(List<Article> results) {
        this.results = results;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }
}