package com.example.starlight.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.starlight.R;
import com.example.starlight.model.MediaItem;
import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends
        RecyclerView.Adapter<MediaAdapter.VH> {

    private final Context context;
    private List<MediaItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    // Glide options — reused for every image = no memory leak
    private final RequestOptions glideOptions =
            new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.color.bg_card)
                    .error(R.color.bg_card);

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public MediaAdapter(Context ctx) {
        context = ctx;
        setHasStableIds(true);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    // DiffUtil — only redraws changed items, not everything
    public void submitList(final List<MediaItem> newList) {
        if (newList == null) return;
        final List<MediaItem> oldList = new ArrayList<>(items);
        final List<MediaItem> finalNew = new ArrayList<>(newList);

        new Thread(() -> {
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(
                    new DiffUtil.Callback() {
                        @Override public int getOldListSize() {
                            return oldList.size(); }
                        @Override public int getNewListSize() {
                            return finalNew.size(); }
                        @Override public boolean areItemsTheSame(
                                int o, int n) {
                            return oldList.get(o).getId()
                                    == finalNew.get(n).getId(); }
                        @Override public boolean areContentsTheSame(
                                int o, int n) {
                            return oldList.get(o).getId()
                                    == finalNew.get(n).getId(); }
                    });

            new android.os.Handler(
                    android.os.Looper.getMainLooper()).post(() -> {
                items = finalNew;
                diff.dispatchUpdatesTo(this);
            });
        }).start();
    }

    @NonNull @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent, int vt) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_media_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.bind(items.get(pos));
    }

    // Release Glide when item scrolls off screen
    @Override
    public void onViewRecycled(@NonNull VH h) {
        super.onViewRecycled(h);
        Glide.with(context).clear(h.imgPoster);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        final CardView  card;
        final ImageView imgPoster;
        final TextView  tvTitle, tvYear, tvRating,
                tvLanguage, tvStream, tvType;

        VH(@NonNull View v) {
            super(v);
            card       = v.findViewById(R.id.cardRoot);
            imgPoster  = v.findViewById(R.id.imgPoster);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvYear     = v.findViewById(R.id.tvYear);
            tvRating   = v.findViewById(R.id.tvRating);
            tvLanguage = v.findViewById(R.id.tvLanguage);
            tvStream   = v.findViewById(R.id.tvStream);
            tvType     = v.findViewById(R.id.tvType);
        }

        void bind(MediaItem item) {
            tvTitle.setText(item.getDisplayTitle());
            tvYear.setText(item.getYear());
            tvRating.setText(item.getFormattedRating());
            tvLanguage.setText(item.getLanguageFlag());
            tvStream.setText(item.getStreamingPlatforms());

            // Type badge
            String type = item.getResolvedType();
            if (type == null) type = item.getMediaType();
            if (type == null) type = "";
            switch (type) {
                case "movie":
                    tvType.setText("FILM");
                    tvType.setTextColor(0xFFFFD060);
                    break;
                case "cartoon":
                    tvType.setText("ANIM");
                    tvType.setTextColor(0xFF00C9A7);
                    break;
                default:
                    tvType.setText("SERIES");
                    tvType.setTextColor(0xFFA78BFA);
                    break;
            }

            // Load image — only if URL exists
            String url = item.getPosterUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(context)
                        .load(url)
                        .apply(glideOptions)
                        .into(imgPoster);
            } else {
                imgPoster.setImageDrawable(null);
                imgPoster.setBackgroundColor(0xFF0A0F1E);
            }

            card.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}