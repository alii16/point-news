package com.example.pointnews.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pointnews.api.ApiClient;
import com.example.pointnews.api.NewsApiService;
import com.example.pointnews.model.Article;
import com.example.pointnews.model.NewsResponse;
import com.example.pointnews.util.Constants;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends ViewModel {
    private MutableLiveData<List<Article>> searchResults = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final int DEFAULT_PAGE_SIZE = 25;

    public LiveData<List<Article>> getSearchResults() {
        return searchResults;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void searchNews(String query) {

        if (query == null || query.trim().isEmpty()) {
            searchResults.postValue(null); // Kosongkan hasil jika query kosong
            errorMessage.postValue("Please enter a search query.");
            return;
        }
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);

// Menggunakan sortBy "relevancy" untuk hasil yang relevan dengan query
        Call<NewsResponse> call = apiService.searchNews(query, "relevancy", Constants.API_KEY, DEFAULT_PAGE_SIZE);
        call.enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.postValue(response.body().getArticles());
                } else {
                    errorMessage.postValue("Error searching news: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }
}