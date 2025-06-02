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

public class EntertainmentViewModel extends ViewModel {

    private MutableLiveData<List<Article>> entertainmentNews = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private static final int DEFAULT_PAGE_SIZE = 25;

    public LiveData<List<Article>> getEntertainmentNews() {
        return entertainmentNews;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchEntertainmentNews() {
        NewsApiService apiService = ApiClient.getClient().create(NewsApiService.class);
        Call<NewsResponse> call = apiService.getCategoryHeadlines(Constants.COUNTRY_CODE, "entertainment", Constants.API_KEY, DEFAULT_PAGE_SIZE);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    entertainmentNews.postValue(response.body().getArticles());
                } else {
                    errorMessage.postValue("Error fetching entertainment news: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                errorMessage.postValue("Network error: " + t.getMessage());
            }
        });
    }
}