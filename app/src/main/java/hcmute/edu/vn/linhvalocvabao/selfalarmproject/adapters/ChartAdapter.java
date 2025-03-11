package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ChartViewHolder> {

    private List<Music> musicList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Music music, int position);
        void onMoreClick(Music music, int position);
    }

    public ChartAdapter(List<Music> musicList, Context context, OnItemClickListener listener) {
        this.musicList = musicList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.bind(music, position);
    }

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }

    public void updateData(List<Music> newList) {
        this.musicList = newList;
        notifyDataSetChanged();
    }

    // Add this method to fix the error
    public List<Music> getMusicList() {
        return musicList;
    }

    class ChartViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPosition;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final ImageView ivThumbnail;
        private final ImageButton btnMore;

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        void bind(final Music music, final int position) {
            // Set position number (add 1 because positions are 0-based)
            tvPosition.setText(String.valueOf(position + 1));
            
            // Set title and artist
            tvTitle.setText(music.getTitle());
            tvArtist.setText(music.getArtists());
            
            // Load thumbnail with Glide
            if (music.getThumbnailM() != null && !music.getThumbnailM().isEmpty()) {
                Glide.with(context)
                        .load(music.getThumbnailM())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .placeholder(R.drawable.placeholder_album)
                        .error(R.drawable.placeholder_album)
                        .into(ivThumbnail);
            } else {
                ivThumbnail.setImageResource(R.drawable.placeholder_album);
            }
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(music, position);
                }
            });
            
            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoreClick(music, position);
                }
            });
        }
    }
}
