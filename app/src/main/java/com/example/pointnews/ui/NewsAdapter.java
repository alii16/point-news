package com.example.pointnews.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.pointnews.R;
import com.example.pointnews.model.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Article> articles;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
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
        // Hapus: TextView tvAuthorSource;
        TextView tvAuthor; // Deklarasikan TextView baru untuk penulis
        TextView tvNewsSourceBadge; // Deklarasikan TextView baru untuk badge sumber
        TextView tvNewsTitle;
        TextView tvDateTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNewsThumbnail = itemView.findViewById(R.id.img_news_thumbnail);
            tvTimeAgo = itemView.findViewById(R.id.tv_time_ago);
            // Hapus baris ini: tvAuthorSource = itemView.findViewById(R.id.tv_author_source);
            tvAuthor = itemView.findViewById(R.id.tv_author); // Inisialisasi TextView penulis
            tvNewsSourceBadge = itemView.findViewById(R.id.tv_news_source_badge); // Inisialisasi TextView badge sumber
            tvNewsTitle = itemView.findViewById(R.id.tv_news_title);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(articles.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Article article) {
            // Judul Berita
            tvNewsTitle.setText(article.getTitle());

            // Gambar Thumbnail (menggunakan Glide)
            if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(article.getUrlToImage())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgNewsThumbnail);
            } else {
                imgNewsThumbnail.setImageResource(R.drawable.ic_launcher_background);
            }

            // --- PERUBAHAN DI SINI UNTUK PENULIS DAN SUMBER ---
            // Penulis
            String author = article.getAuthor() != null && !article.getAuthor().isEmpty() ? article.getAuthor() : "Unknown";
            tvAuthor.setText(author); // Set teks ke tvAuthor

            // Sumber (untuk badge)
            String source = article.getSource() != null && article.getSource().getName() != null ? article.getSource().getName() : "Unknown";
            tvNewsSourceBadge.setText(source); // Set teks ke tvNewsSourceBadge
            // ----------------------------------------------------

            // Waktu Berlalu dan Tanggal/Jam
            if (article.getPublishedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    Date publishedDate = inputFormat.parse(article.getPublishedAt());
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
                    tvDateTime.setText("");
                }
            } else {
                tvTimeAgo.setText("");
                tvDateTime.setText("");
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