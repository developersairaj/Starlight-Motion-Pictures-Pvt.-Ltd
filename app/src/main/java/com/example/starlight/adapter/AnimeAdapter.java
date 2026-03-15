package com.example.starlight.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.example.starlight.R;
import com.example.starlight.model.AnimeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.VH> {

    private final Context context;
    private List<AnimeItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public interface OnItemClickListener {
        void onItemClick(AnimeItem item);
    }

    public AnimeAdapter(Context ctx) {
        context = ctx;
        setHasStableIds(true);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getMalId();
    }

    public void submitList(List<AnimeItem> list) {
        items = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_anime_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.bind(items.get(pos));
    }

    @Override
    public void onViewRecycled(@NonNull VH h) {
        super.onViewRecycled(h);
        Glide.with(context).clear(h.imgPoster);
        h.imgPoster.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final CardView  card;
        final ImageView imgPoster;
        final TextView  tvTitle, tvRating, tvGenre,
                tvYear, tvStream, tvLanguage, tvType;

        VH(@NonNull View v) {
            super(v);
            card       = v.findViewById(R.id.cardRoot);
            imgPoster  = v.findViewById(R.id.imgPoster);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvRating   = v.findViewById(R.id.tvRating);
            tvGenre    = v.findViewById(R.id.tvGenre);
            tvYear     = v.findViewById(R.id.tvYear);
            tvStream   = v.findViewById(R.id.tvStream);
            tvLanguage = v.findViewById(R.id.tvLanguage);
            tvType     = v.findViewById(R.id.tvType);
        }

        void bind(AnimeItem item) {
            tvTitle.setText(item.getDisplayTitle());
            tvRating.setText(item.getFormattedScore());
            tvGenre.setText(item.getGenresString());
            tvYear.setText(item.getEpisodeInfo());
            tvStream.setText(item.getStreamingInfo());

            // Type badge
            boolean isManga = "Manga".equalsIgnoreCase(item.getType());
            if (isManga) {
                tvType.setText("MANGA");
                tvType.setTextColor(0xFFA78BFA);
            } else {
                tvType.setText("ANIME");
                tvType.setTextColor(0xFF00C9A7);
            }

            // Status badge
            String status = item.getAiringStatus();
            switch (status) {
                case "AIRING NOW":
                    tvLanguage.setText("ON AIR");
                    tvLanguage.setTextColor(0xFF00C9A7);
                    break;
                case "UPCOMING":
                    tvLanguage.setText("SOON");
                    tvLanguage.setTextColor(0xFFFFD060);
                    break;
                default:
                    tvLanguage.setText("DONE");
                    tvLanguage.setTextColor(0xFF2E3850);
                    break;
            }

            // Load image with Glide + MAL headers
            String url = item.getPosterUrl();
            imgPoster.setImageDrawable(null);
            imgPoster.setBackgroundColor(0xFF0A0F1E);

            if (url != null && !url.isEmpty()) {
                GlideUrl glideUrl = new GlideUrl(
                        url,
                        new LazyHeaders.Builder()
                                .addHeader("User-Agent",
                                        "Mozilla/5.0 (Linux; Android 10) " +
                                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                                "Chrome/91.0.4472.120 Mobile Safari/537.36")
                                .addHeader("Referer", "https://myanimelist.net")
                                .addHeader("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                                .build());

                Glide.with(context)
                        .load(glideUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.color.bg_card)
                        .error(R.color.bg_card)
                        .thumbnail(0.1f)
                        .into(imgPoster);
            }

            card.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}