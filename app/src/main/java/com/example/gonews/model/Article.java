package com.example.gonews.model;

import com.google.gson.annotations.SerializedName;
import java.util.List; // Import untuk List

public class Article {

    // NewsData.io mengembalikan struktur ini (contoh beberapa field utama):
    // "article_id": "...",
    // "title": "...",
    // "link": "...",          -> Ini adalah URL artikel
    // "keywords": [...],
    // "creator": ["...", "..."], -> Ini adalah author/penulis
    // "description": "...",
    // "content": "...",
    // "pubDate": "...",       -> Ini adalah tanggal publikasi
    // "image_url": "...",     -> Ini adalah URL gambar
    // "source_id": "...",     -> Ini adalah ID sumber
    // "source_url": "...",    -> Ini adalah URL sumber
    // "source_icon": "...",
    // "language": "en",
    // "country": ["us"],
    // "category": ["business"],
    // "sentiment": "..."

    @SerializedName("article_id")
    private String articleId;

    @SerializedName("title")
    private String title;

    @SerializedName("link") // Menggantikan "url" dari NewsAPI.org
    private String link;

    @SerializedName("creator") // NewsData.io sering mengembalikan ini sebagai List<String>
    private List<String> creator;

    @SerializedName("description")
    private String description;

    @SerializedName("content")
    private String content;

    @SerializedName("pubDate") // Menggantikan "publishedAt"
    private String pubDate;

    @SerializedName("image_url") // Menggantikan "urlToImage"
    private String imageUrl;

    @SerializedName("source_id") // Menggantikan "source.id"
    private String sourceId;

    @SerializedName("source_url") // Menambahkan URL sumber secara langsung
    private String sourceUrl;

    @SerializedName("source_icon") // Menambahkan ikon sumber
    private String sourceIcon;

    @SerializedName("language")
    private String language;

    @SerializedName("country")
    private List<String> country; // Biasanya List<String> untuk NewsData.io

    @SerializedName("category")
    private List<String> category; // Biasanya List<String>

    @SerializedName("sentiment")
    private String sentiment;

    // --- Constructor ---
    // Anda bisa membuat constructor yang lebih ringkas atau tanpa parameter
    public Article() {
        // Default constructor for Gson
    }

    // Constructor dengan semua field (opsional, sesuaikan dengan kebutuhan Anda)
    public Article(String articleId, String title, String link, List<String> creator, String description,
                   String content, String pubDate, String imageUrl, String sourceId,
                   String sourceUrl, String sourceIcon, String language, List<String> country,
                   List<String> category, String sentiment) {
        this.articleId = articleId;
        this.title = title;
        this.link = link;
        this.creator = creator;
        this.description = description;
        this.content = content;
        this.pubDate = pubDate;
        this.imageUrl = imageUrl;
        this.sourceId = sourceId;
        this.sourceUrl = sourceUrl;
        this.sourceIcon = sourceIcon;
        this.language = language;
        this.country = country;
        this.category = category;
        this.sentiment = sentiment;
    }


    // --- Getters ---
    public String getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public List<String> getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getSourceIcon() {
        return sourceIcon;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getCountry() {
        return country;
    }

    public List<String> getCategory() {
        return category;
    }

    public String getSentiment() {
        return sentiment;
    }

    // --- Setters (Opsional, sesuai kebutuhan) ---
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setCreator(List<String> creator) {
        this.creator = creator;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSourceIcon(String sourceIcon) {
        this.sourceIcon = sourceIcon;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}