package com.example.gonews.viewmodel;

import android.util.Log; // Pastikan ini diimport
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.gonews.api.ApiClient;
import com.example.gonews.api.NewsApiService;
import com.example.gonews.model.Article;
import com.example.gonews.model.NewsResponse;
import com.example.gonews.util.Constants;
import java.io.IOException; // Pastikan ini diimport
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntertainmentViewModel extends ViewModel {

    private MutableLiveData<List<Article>> entertainmentNews = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(); // Indikator loading
    private String nextPageToken = null; // Untuk paginasi NewsData.io
    private final String CATEGORY = "entertainment"; // Kategori spesifik untuk ViewModel ini

    public LiveData<List<Article>> getEntertainmentNews() {
        return entertainmentNews;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Metode untuk mengambil berita hiburan
    // Panggil ini tanpa parameter untuk halaman pertama
    public void fetchEntertainmentNews(boolean isLoadMore) {
        // Jangan fetch jika sedang loading atau tidak ada next page token untuk load more
        if (isLoading.getValue() != null && isLoading.getValue()) {
            return;
        }
        if (isLoadMore && nextPageToken == null) {
            Log.d("EntertainmentViewModel", "No more pages to load for category: " + CATEGORY);
            return;
        }

        isLoading.postValue(true); // Set loading menjadi true

        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);

        // Gunakan nextPageToken jika ini adalah panggilan 'load more', jika tidak, null untuk halaman pertama
        String pageToFetch = isLoadMore ? nextPageToken : null;

        Call<NewsResponse> call = apiService.getCategoryNews( // Menggunakan getCategoryNews dari NewsApiService yang baru
                Constants.COUNTRY_CODE,
                CATEGORY, // Menggunakan kategori spesifik
                Constants.API_KEY,
                Constants.LANGUAGE_CODE, // Tambahkan parameter bahasa
                pageToFetch // Gunakan token halaman
        );

        Log.d("EntertainmentViewModel", "Fetching entertainment news for country: " + Constants.COUNTRY_CODE +
                ", category: " + CATEGORY +
                ", language: " + Constants.LANGUAGE_CODE +
                ", API Key: " + Constants.API_KEY +
                ", page: " + (pageToFetch != null ? pageToFetch : "initial"));

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                isLoading.postValue(false); // Selesai loading
                if (response.isSuccessful() && response.body() != null) {
                    NewsResponse newsResponse = response.body();
                    if (newsResponse.getResults() != null && !newsResponse.getResults().isEmpty()) {
                        List<Article> currentArticles = entertainmentNews.getValue();
                        if (currentArticles == null || !isLoadMore) {
                            // Jika ini adalah panggilan pertama atau refresh
                            entertainmentNews.postValue(newsResponse.getResults());
                        } else {
                            // Jika ini adalah load more, tambahkan ke daftar yang sudah ada
                            currentArticles.addAll(newsResponse.getResults());
                            entertainmentNews.postValue(currentArticles);
                        }
                        errorMessage.postValue(null); // Bersihkan error sebelumnya
                        nextPageToken = newsResponse.getNextPage(); // Simpan token untuk halaman berikutnya

                        Log.d("EntertainmentViewModel", "Entertainment news fetched successfully. Total results: " + newsResponse.getTotalResults() +
                                ". Articles fetched in this call: " + newsResponse.getResults().size() +
                                ". Next Page Token: " + nextPageToken);
                    } else {
                        String msg = "Tidak ada berita hiburan ditemukan untuk " + Constants.COUNTRY_CODE + " (" + Constants.LANGUAGE_CODE + "). Ini mungkin karena batasan API Key gratis atau tidak ada berita yang tersedia.";
                        errorMessage.postValue(msg);
                        if (!isLoadMore) { // Hanya kosongkan jika ini bukan load more dan tidak ada hasil
                            entertainmentNews.postValue(null);
                        }
                        Log.w("EntertainmentViewModel", msg + " Full response: " + newsResponse.toString());
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorBody = "Gagal memparsing body error.";
                    }
                    String msg = "Error fetching entertainment news: HTTP " + response.code() + " - " + response.message() + " | " + errorBody;
                    errorMessage.postValue(msg);
                    if (!isLoadMore) { // Hanya kosongkan jika ini bukan load more dan ada error
                        entertainmentNews.postValue(null);
                    }
                    Log.e("EntertainmentViewModel", msg);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                isLoading.postValue(false); // Selesai loading
                String msg = "Kesalahan Jaringan saat mengambil berita hiburan: " + t.getMessage();
                errorMessage.postValue(msg);
                if (!isLoadMore) { // Hanya kosongkan jika ini bukan load more dan ada kegagalan
                    entertainmentNews.postValue(null);
                }
                Log.e("EntertainmentViewModel", msg, t);
            }
        });
    }

    // Metode untuk mereset paginasi (misalnya saat refresh)
    public void resetPagination() {
        nextPageToken = null;
        entertainmentNews.postValue(null); // Kosongkan data yang ada
        errorMessage.postValue(null); // Bersihkan error
    }

    // Metode untuk memuat lebih banyak berita (dipanggil dari Fragment saat scrolling)
    public void loadMoreEntertainmentNews() {
        fetchEntertainmentNews(true);
    }
}