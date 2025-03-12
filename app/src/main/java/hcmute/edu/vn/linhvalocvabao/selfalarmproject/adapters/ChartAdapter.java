package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapters;

import android.annotation.SuppressLint;
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Music> newList) {
        this.musicList = newList;
        notifyDataSetChanged();
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.bind(music, position, listener);
    }

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }

    public static class ChartViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle, tvArtist, tvPosition;
        private final ImageView ivThumbnail;
        private final ImageButton btnMore;

        public ChartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        public void bind(Music music, int position, OnItemClickListener listener) {
            tvTitle.setText(music.getTitle());
            tvArtist.setText(music.getArtists());
            tvPosition.setText(String.valueOf(position + 1));

            Glide.with(itemView.getContext())
                    .load(music.getThumbnail())
                    .placeholder(R.drawable.placeholder_album)
                    .into(ivThumbnail);

            itemView.setOnClickListener(v -> listener.onItemClick(music, getAdapterPosition()));
            btnMore.setOnClickListener(v -> listener.onMoreClick(music, getAdapterPosition()));
        }
    }
}
