package com.example.gonews.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gonews.R;
import com.example.gonews.model.Article; // Ensure this imports your updated Article model

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Import ArrayList
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Article> articles = new ArrayList<>(); // Initialize to avoid NullPointerException
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // This method replaces all existing articles and notifies the adapter
    public void setArticles(List<Article> newArticles) {
        if (newArticles != null) {
            this.articles.clear();
            this.articles.addAll(newArticles);
        } else {
            this.articles.clear(); // Clear if null to show no data
        }
        notifyDataSetChanged();
    }

    // This method adds new articles to the existing list for infinite scrolling
    public void addArticles(List<Article> newArticles) {
        if (newArticles != null && !newArticles.isEmpty()) {
            int startPosition = this.articles.size();
            this.articles.addAll(newArticles);
            notifyItemRangeInserted(startPosition, newArticles.size());
        }
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNewsThumbnail;
        TextView tvTimeAgo;
        TextView tvAuthor;
        TextView tvNewsSourceBadge;
        TextView tvNewsTitle;
        TextView tvDateTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNewsThumbnail = itemView.findViewById(R.id.img_news_thumbnail);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvNewsSourceBadge = itemView.findViewById(R.id.tv_news_source_badge);
            tvNewsTitle = itemView.findViewById(R.id.tv_news_title);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(articles.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Article article) {
            // News Title
            tvNewsTitle.setText(article.getTitle() != null ? article.getTitle() : "No Title Available");

            // Thumbnail Image (using Glide)
            // Use getImageUrl() instead of getUrlToImage()
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(article.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgNewsThumbnail);
            } else {
                imgNewsThumbnail.setImageResource(R.drawable.ic_launcher_background); // Placeholder image
            }

            // --- CHANGES HERE FOR AUTHOR AND SOURCE ---
            // Author
            // NewsData.io's creator is a List<String>. Display the first one, or "Unknown"
            String author = (article.getCreator() != null && !article.getCreator().isEmpty()) ?
                    article.getCreator().get(0) : "Unknown";
            tvAuthor.setText(author);

            // Source (for badge)
            // Use getSourceId() for the source name
            String sourceId = article.getSourceId() != null ? article.getSourceId() : "Unknown";
            tvNewsSourceBadge.setText(sourceId);
            // ------------------------------------------

            // Time Ago and Date/Time
            // Use getPubDate() instead of getPublishedAt()
            if (article.getPubDate() != null) {
                try {
                    // NewsData.io pubDate format: "yyyy-MM-dd HH:mm:ss"
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    // NewsData.io dates are often in UTC, ensure consistency
                    // inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Uncomment if dates are consistently UTC

                    Date publishedDate = inputFormat.parse(article.getPubDate());
                    if (publishedDate != null) {
                        long diffMillis = System.currentTimeMillis() - publishedDate.getTime();
                        String timeAgo = formatTimeAgo(diffMillis);
                        tvTimeAgo.setText(timeAgo);

                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
                        SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String formattedDate = outputDateFormat.format(publishedDate);
                        String formattedTime = outputTimeFormat.format(publishedDate);
                        tvDateTime.setText(formattedDate + " - " + formattedTime);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    tvTimeAgo.setText("");
                    tvDateTime.setText("Invalid Date"); // Indicate an issue with parsing
                }
            } else {
                tvTimeAgo.setText("");
                tvDateTime.setText("No Date");
            }
        }

        private String formatTimeAgo(long milliseconds) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
            long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
            long days = TimeUnit.MILLISECONDS.toDays(milliseconds);

            if (seconds < 60) {
                return "Baru saja";
            } else if (minutes < 60) {
                return minutes + " menit yang lalu";
            } else if (hours < 24) {
                return hours + " jam yang lalu";
            } else if (days < 7) {
                return days + " hari yang lalu";
            } else {
                return "Lebih dari seminggu yang lalu";
            }
        }
    }
}