package com.example.gonews.ui.home;

import android.util.Log; // Tambahkan import ini
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions; // Tambahkan ini jika Anda ingin transisi
import com.example.gonews.R;
import com.example.gonews.model.Article; // Pastikan ini mengarah ke kelas Article Anda yang benar

import java.util.ArrayList;
import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private static final String TAG = "SliderAdapter"; // Tag untuk Logcat

    private List<Article> articles;
    private OnSliderItemClickListener itemClickListener;

    public SliderAdapter(List<Article> articles, OnSliderItemClickListener itemClickListener) {
        this.articles = (articles != null) ? new ArrayList<>(articles) : new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    public void updateData(List<Article> newArticles) {
        this.articles.clear();
        if (newArticles != null) {
            this.articles.addAll(newArticles);
        }
        Log.d(TAG, "updateData: New articles size: " + this.articles.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Binding position " + position);

        if (articles == null) {
            Log.e(TAG, "onBindViewHolder: Articles list is NULL!");
            holder.clearView();
            return;
        }

        if (position < 0 || position >= articles.size()) {
            Log.e(TAG, "onBindViewHolder: Position " + position + " is out of bounds for list size " + articles.size());
            holder.clearView();
            return;
        }

        Article article = articles.get(position);

        if (article == null) {
            Log.e(TAG, "onBindViewHolder: Article at position " + position + " is NULL inside the list!");
            holder.clearView();
            return;
        }

        // Jika sampai di sini, 'article' dijamin tidak null
        holder.bind(article, itemClickListener);
    }

    @Override
    public int getItemCount() {
        int count = articles != null ? articles.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSliderImage;
        TextView tvSliderTitle;
        TextView tvSliderSource;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSliderImage = itemView.findViewById(R.id.iv_slider_image);
            tvSliderTitle = itemView.findViewById(R.id.tv_slider_title);
            tvSliderSource = itemView.findViewById(R.id.tv_slider_source);
        }

        public void bind(final Article article, final OnSliderItemClickListener listener) {
            // Ini adalah pengecekan defensif tambahan
            if (article == null) {
                Log.e(TAG, "bind: Article object passed to bind is NULL!");
                clearView();
                itemView.setOnClickListener(null);
                return;
            }

            // Muat gambar dengan Glide
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(article.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivSliderImage);
            } else {
                ivSliderImage.setImageDrawable(null);
            }

            // Menggunakan getTitle()
            String title = article.getTitle();
            if (title != null) {
                tvSliderTitle.setText(title);
            } else {
                tvSliderTitle.setText("No Title Available");
            }

            // --- PERUBAHAN KRUSIAL DI SINI UNTUK SUMBER ---
            // Menggunakan getSourceId() karena Article.java Anda tidak memiliki getSource()
            String source = article.getSourceId(); // <<< PENTING: GANTI DARI getSource() KE getSourceId()
            if (source != null && !source.isEmpty()) {
                tvSliderSource.setText(source);
            } else {
                // Sebagai fallback, coba gunakan creator jika sourceId kosong/null
                if (article.getCreator() != null && !article.getCreator().isEmpty()) {
                    tvSliderSource.setText(article.getCreator().get(0)); // Ambil creator pertama
                } else {
                    tvSliderSource.setText("Unknown Source"); // Teks default jika tidak ada sumber atau creator
                }
            }
            // ------------------------------------------------

            // Set OnClickListener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSliderItemClick(article);
                }
            });
        }

        // Metode untuk membersihkan tampilan holder
        public void clearView() {
            ivSliderImage.setImageDrawable(null);
            tvSliderTitle.setText("");
            tvSliderSource.setText("");
            itemView.setOnClickListener(null);
        }
    }

    public interface OnSliderItemClickListener {
        void onSliderItemClick(Article article);
    }
}