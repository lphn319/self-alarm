package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;

public class ChartAdapter extends ListAdapter<Music, ChartAdapter.ChartViewHolder> {

    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Music music, int position);

        void onMoreClick(Music music, int position);
    }
    
    // DiffUtil callback for efficient list updates
    private static final DiffUtil.ItemCallback<Music> DIFF_CALLBACK = new DiffUtil.ItemCallback<Music>() {
        @Override
        public boolean areItemsTheSame(@NonNull Music oldItem, @NonNull Music newItem) {
            // Compare unique identifiers
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Music oldItem, @NonNull Music newItem) {
            // Compare all relevant fields for UI updates
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getArtists().equals(newItem.getArtists()) &&
                   oldItem.getThumbnail().equals(newItem.getThumbnail());
        }
    };

    public ChartAdapter(Context context, OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    public List<Music> getMusicList() {
        return getCurrentList();
    }

    @NonNull
    @Override
    public ChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_chart, parent, false);
        return new ChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChartViewHolder holder, int position) {
        Music music = getItem(position);
        holder.bind(music, position, listener);
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
