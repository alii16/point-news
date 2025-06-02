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

public class TechViewModel extends ViewModel {

    private MutableLiveData<List<Article>> techNews = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private static final int DEFAULT_PAGE_SIZE = 25;

    public LiveData<List<Article>> getTechNews() {
        return techNews;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchTechNews() {
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = apiService.getCategoryHeadlines(Constants.COUNTRY_CODE, "technology", Constants.API_KEY, DEFAULT_PAGE_SIZE);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    techNews.postValue(response.body().getArticles());
                } else {
                    errorMessage.postValue("Error fetching tech news: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }
}