package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.BlacklistContact;

public class BlacklistAdapter extends RecyclerView.Adapter<BlacklistAdapter.ViewHolder> {
    private List<BlacklistContact> blacklist;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRemoveClick(BlacklistContact contact);

        void onSettingsChanged(BlacklistContact contact, boolean blockCalls, boolean blockMessages);
    }

    public BlacklistAdapter(List<BlacklistContact> blacklist) {
        this.blacklist = blacklist;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<BlacklistContact> newBlacklist) {
        this.blacklist = newBlacklist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blacklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BlacklistContact contact = blacklist.get(position);

        // Set contact info
        holder.tvPhoneNumber.setText(contact.getPhoneNumber());

        if (contact.getName() != null && !contact.getName().isEmpty()) {
            holder.tvName.setText(contact.getName());
            holder.tvName.setVisibility(View.VISIBLE);
        } else {
            holder.tvName.setVisibility(View.GONE);
        }

        // Set date added
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateAdded = sdf.format(new Date(contact.getDateAdded()));
        holder.tvDateAdded.setText("Added: " + dateAdded);

        // Set switches
        holder.switchBlockCalls.setChecked(contact.isBlockCalls());
        holder.switchBlockMessages.setChecked(contact.isBlockMessages());

        // Set listeners
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(contact);
            }
        });

        holder.switchBlockCalls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() && listener != null) {
                contact.setBlockCalls(isChecked);
                listener.onSettingsChanged(contact, isChecked, contact.isBlockMessages());
            }
        });

        holder.switchBlockMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() && listener != null) {
                contact.setBlockMessages(isChecked);
                listener.onSettingsChanged(contact, contact.isBlockCalls(), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return blacklist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber, tvName, tvDateAdded;
        SwitchMaterial switchBlockCalls, switchBlockMessages;
        ImageButton btnRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDateAdded = itemView.findViewById(R.id.tv_date_added);
            switchBlockCalls = itemView.findViewById(R.id.switch_block_calls);
            switchBlockMessages = itemView.findViewById(R.id.switch_block_messages);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
