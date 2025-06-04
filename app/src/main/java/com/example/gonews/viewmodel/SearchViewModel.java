// app/src/main/java/com/example/gonews/viewmodel/SearchViewModel.java
package com.example.gonews.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData; // Tambahkan import ini
import com.example.gonews.data.SearchRepository;
import com.example.gonews.model.Article;
import com.example.gonews.api.ApiClient; // Menggunakan ApiClient dari package api
import com.example.gonews.api.NewsApiService; // Menggunakan NewsApiService dari package api
import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final SearchRepository searchRepository;

    public SearchViewModel(Application application) {
        super(application);
        // Perbaikan: Gunakan ApiClient.getClient()
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        searchRepository = new SearchRepository(apiService);
    }

    public LiveData<List<Article>> getSearchResults() {
        return searchRepository.getSearchResults();
    }

    public LiveData<String> getErrorMessage() {
        return searchRepository.getErrorMessage();
    }

    public LiveData<Boolean> getIsLoading() {
        return searchRepository.getIsLoading();
    }

    public void searchNews(String query) {
        searchRepository.searchNews(query, false); // false untuk pencarian awal
    }

    public void loadMoreSearchResults() {
        searchRepository.loadMoreNews(); // true untuk memuat lebih banyak
    }

    public void resetSearch() {
        searchRepository.resetPagination();
        // Anda bisa menambahkan logika untuk mengosongkan LiveData hasil pencarian jika diperlukan
        // Contoh:
        ((MutableLiveData<List<Article>>) getSearchResults()).setValue(null);
        ((MutableLiveData<String>) getErrorMessage()).setValue(null);
    }
}