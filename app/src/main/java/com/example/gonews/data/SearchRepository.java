// app/src/main/java/com/example/gonews/data/SearchRepository.java
package com.example.gonews.data;

import androidx.lifecycle.MutableLiveData;
import com.example.gonews.model.Article;
import com.example.gonews.model.NewsResponse; // Menggunakan NewsResponse
import com.example.gonews.api.NewsApiService; // Menggunakan NewsApiService dari package api
import com.example.gonews.util.Constants; // Pastikan ini mengarah ke Constants Anda
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException; // Tambahkan import ini untuk IOException

public class SearchRepository {

    private final NewsApiService newsApiService;
    private final MutableLiveData<List<Article>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    private String currentQuery;
    private String currentPageToken = null; // Token halaman selanjutnya dari NewsData.io

    public SearchRepository(NewsApiService newsApiService) {
        this.newsApiService = newsApiService;
    }

    public MutableLiveData<List<Article>> getSearchResults() {
        return _searchResults;
    }

    public MutableLiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    public void searchNews(String query, boolean isLoadMore) {
        if (!isLoadMore) {
            this.currentQuery = query;
            this.currentPageToken = null; // Reset token untuk pencarian baru
            _isLoading.setValue(true);
            _searchResults.setValue(null); // Kosongkan hasil sebelumnya
            _errorMessage.setValue(null); // Bersihkan error sebelumnya
        } else if (currentPageToken == null) {
            // Tidak ada halaman selanjutnya untuk dimuat
            _isLoading.setValue(false);
            return;
        }

        _isLoading.setValue(true);

        // Perbaikan: Gunakan Constants.API_KEY
        Call<NewsResponse> call = newsApiService.searchNews(
                currentQuery,
                Constants.API_KEY, // Perbaikan: Gunakan Constants.API_KEY
                Constants.LANGUAGE_CODE, // Gunakan Constants.LANGUAGE_CODE
                currentPageToken // Menggunakan token halaman untuk permintaan selanjutnya
        );

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    NewsResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        List<Article> articles = apiResponse.getResults();
                        if (articles != null && !articles.isEmpty()) {
                            List<Article> currentArticles = _searchResults.getValue();
                            if (isLoadMore && currentArticles != null) {
                                // Tambahkan artikel baru ke daftar yang sudah ada
                                ArrayList<Article> updatedList = new ArrayList<>(currentArticles);
                                updatedList.addAll(articles);
                                _searchResults.setValue(updatedList);
                            } else {
                                // Set daftar baru untuk pencarian awal
                                _searchResults.setValue(articles);
                            }
                            // Perbarui token halaman selanjutnya
                            currentPageToken = apiResponse.getNextPage();
                            // Jika tidak ada token selanjutnya, berarti ini halaman terakhir
                            // Tidak perlu set _errorMessage di sini, biarkan null jika berhasil
                        } else {
                            if (!isLoadMore) { // Tampilkan "tidak ada hasil" hanya untuk pencarian awal
                                _errorMessage.setValue("Tidak ada berita ditemukan untuk pencarian ini.");
                            } else {
                                currentPageToken = null; // Tidak ada hasil lagi
                            }
                        }
                    } else {
                        // Perbaikan: Ambil pesan error dari errorBody jika status bukan 'success'
                        String errorMsg = "Gagal memuat berita.";
                        if (response.errorBody() != null) {
                            try {
                                // NewsData.io error responses might have a 'message' field directly
                                // Or you can parse the errorBody for more details.
                                // For simplicity, we'll just use the HTTP message for now.
                                errorMsg = response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        _errorMessage.setValue("Error API: " + response.code() + " - " + errorMsg);
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
                    _errorMessage.setValue("Error respons HTTP: " + response.code() + " - " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _errorMessage.setValue("Koneksi internet bermasalah atau server tidak merespon: " + t.getMessage());
            }
        });
    }

    public void loadMoreNews() {
        searchNews(currentQuery, true);
    }

    public void resetPagination() {
        currentPageToken = null;
    }
}