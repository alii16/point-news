package com.example.pointnews.viewmodel;

import android.util.Log; // Pastikan ini diimport
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pointnews.api.ApiClient;
import com.example.pointnews.api.NewsApiService;
import com.example.pointnews.model.Article;
import com.example.pointnews.model.NewsResponse;
import com.example.pointnews.util.Constants; // Import Constants
import java.io.IOException; // Pastikan ini diimport
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<Article>> topHeadlines = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private static final int DEFAULT_PAGE_SIZE = 25;

    public LiveData<List<Article>> getTopHeadlines() {
        return topHeadlines;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchTopHeadlines() {
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = apiService.getTopHeadlines(Constants.COUNTRY_CODE, Constants.API_KEY, DEFAULT_PAGE_SIZE);

        Log.d("HomeViewModel", "Fetching top headlines for country: " + Constants.COUNTRY_CODE + " with API Key: " + Constants.API_KEY + " and pageSize: " + DEFAULT_PAGE_SIZE); // Debug log

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getArticles() != null && !response.body().getArticles().isEmpty()) {
                        topHeadlines.postValue(response.body().getArticles());
                        errorMessage.postValue(null); // Clear any previous error
                        Log.d("HomeViewModel", "Top headlines fetched successfully. Total articles: " + response.body().getArticles().size());
                    } else {
                        // Kasus umum untuk NewsAPI Developer Plan: respons OK, tapi articles kosong.
                        // Ini berarti tidak ada berita yang tersedia untuk filter tersebut dengan API Key Anda.
                        String msg = "Tidak ada berita utama ditemukan untuk " + Constants.COUNTRY_CODE + ". Ini mungkin karena batasan API Key gratis atau tidak ada berita yang tersedia.";
                        errorMessage.postValue(msg); // Pesan ini akan dikirim ke UI
                        topHeadlines.postValue(null); // Kosongkan daftar artikel
                        Log.w("HomeViewModel", msg + " Full response: " + response.body().toString()); // Log sebagai warning
                    }
                } else {
                    // API mengembalikan respons error HTTP (misalnya 401 Unauthorized, 403 Forbidden, 429 Too Many Requests)
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorBody = "Failed to parse error body.";
                    }
                    String msg = "Error fetching top headlines: HTTP " + response.code() + " - " + response.message() + " | " + errorBody;
                    errorMessage.postValue(msg); // Pesan ini akan dikirim ke UI
                    topHeadlines.postValue(null); // Kosongkan daftar artikel
                    Log.e("HomeViewModel", msg); // Log sebagai error
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                // Masalah jaringan (misalnya tidak ada internet), timeout, atau parsing JSON yang salah.
                String msg = "Kesalahan Jaringan saat mengambil berita utama: " + t.getMessage();
                errorMessage.postValue(msg); // Pesan ini akan dikirim ke UI
                topHeadlines.postValue(null); // Kosongkan daftar artikel
                Log.e("HomeViewModel", msg, t); // Log error dan exception
            }
        });
    }

}