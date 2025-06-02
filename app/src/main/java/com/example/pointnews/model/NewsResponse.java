package com.example.pointnews.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    // NewsAPI.org biasanya mengembalikan struktur ini:
    // "status": "ok",
    // "totalResults": 12345,
    // "articles": [ { ... }, { ... }, ... ]

    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("articles")
    private List<Article> articles; // List dari objek Article

    // Constructor
    public NewsResponse(String status, int totalResults, List<Article> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<Article> getArticles() {
        return articles;
    }

    // Setters (opsional)
    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}