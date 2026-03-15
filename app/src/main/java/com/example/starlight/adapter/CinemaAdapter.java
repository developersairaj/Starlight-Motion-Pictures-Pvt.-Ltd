package com.example.starlight.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.starlight.R;
import com.example.starlight.model.CinemaItem;

import java.util.ArrayList;
import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.VH> {

    public interface OnBookClickListener {
        void onBookClick(CinemaItem movie);
    }

    private final Context           context;
    private List<CinemaItem>        movies = new ArrayList<>();
    private OnBookClickListener     listener;

    public CinemaAdapter(Context context) {
        this.context = context;
    }

    public void setOnBookClickListener(OnBookClickListener l) {
        this.listener = l;
    }

    public void setMovies(List<CinemaItem> list) {
        movies = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cinema_movie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CinemaItem m = movies.get(pos);

        h.tvTitle.setText(m.getTitle());
        h.tvRating.setText("⭐ " + m.getFormattedRating());
        h.tvYear.setText(m.getYear());

        // Language badge
        String lang = m.getOriginalLanguage();
        if (!lang.isEmpty()) {
            h.tvLang.setVisibility(View.VISIBLE);
            h.tvLang.setText(lang);
        } else {
            h.tvLang.setVisibility(View.GONE);
        }

        // Overview
        String ov = m.getOverview();
        if (ov.isEmpty()) {
            h.tvOverview.setVisibility(View.GONE);
        } else {
            h.tvOverview.setVisibility(View.VISIBLE);
            h.tvOverview.setText(ov.length() > 130 ? ov.substring(0, 130) + "…" : ov);
        }

        // Poster
        String url = m.getPosterUrl();
        if (url != null) {
            Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.bg_card_glass)
                    .error(R.drawable.bg_card_glass)
                    .into(h.ivPoster);
        } else {
            Glide.with(context).clear(h.ivPoster);
            h.ivPoster.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.bg_card_glass));
        }

        // Book tickets click
        h.btnBookTickets.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(m);
        });
    }

    @Override
    public int getItemCount() { return movies.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView  tvTitle, tvRating, tvYear, tvLang, tvOverview;
        View      btnBookTickets;

        VH(@NonNull View v) {
            super(v);
            ivPoster       = v.findViewById(R.id.ivCinemaPoster);
            tvTitle        = v.findViewById(R.id.tvCinemaTitle);
            tvRating       = v.findViewById(R.id.tvCinemaRating);
            tvYear         = v.findViewById(R.id.tvCinemaYear);
            tvLang         = v.findViewById(R.id.tvCinemaLang);
            tvOverview     = v.findViewById(R.id.tvCinemaOverview);
            btnBookTickets = v.findViewById(R.id.btnBookTickets);
        }
    }
}