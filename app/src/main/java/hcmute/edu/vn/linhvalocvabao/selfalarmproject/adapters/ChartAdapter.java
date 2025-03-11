package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Context context;
    private OnItemClickListener listener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        Music music = musicList.get(position);
        
        // Set rank position (1-based)
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Highlight top 3 ranks with different colors
        if (position == 0) {
            holder.tvRank.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (position == 1) {
            holder.tvRank.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        } else if (position == 2) {
            holder.tvRank.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvRank.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtists.setText(music.getArtists());
        
        // Load thumbnail with rounded corners
        if (music.getThumbnailM() != null && !music.getThumbnailM().isEmpty()) {
            Glide.with(context)
                .load(music.getThumbnailM())
                .placeholder(R.drawable.ic_play_circle)
                .error(R.drawable.ic_play_circle)
                .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_play_circle);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(music, position);
            }
        });
        
        holder.ivMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreClick(music, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }
    
    public void updateData(List<Music> newMusicList) {
        this.musicList = newMusicList;
        notifyDataSetChanged();
    }

    static class ChartViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        ImageView ivThumbnail;
        ImageView ivPlayIndicator;
        TextView tvTitle;
        TextView tvArtists;
        ImageView ivMore;

        ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayIndicator = itemView.findViewById(R.id.ivPlayIndicator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtists = itemView.findViewById(R.id.tvArtists);
            ivMore = itemView.findViewById(R.id.ivMore);
        }
    }
}
