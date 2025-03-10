package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.SMS;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BlacklistService;

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder> {
    private List<SMS> smsList;
    private OnSmsItemClickListener listener;

    public interface OnSmsItemClickListener {
        void onSmsClick(SMS sms);
        void onReply(SMS sms);
        void onAddToBlacklist(String sender);
    }

    public SMSAdapter(List<SMS> smsList) {
        this.smsList = smsList;
    }

    public void setOnSmsItemClickListener(OnSmsItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SMSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sms, parent, false);
        return new SMSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SMSViewHolder holder, int position) {
        SMS sms = smsList.get(position);
        holder.senderName.setText(sms.getSender());
        holder.messagePreview.setText(sms.getMessage());

        // Set message time (mock for now)
        holder.messageTime.setText("12:30 PM");

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSmsClick(sms);
            }
        });

        // Set click listener for reply button
        holder.btnReply.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReply(sms);
            } else {
                // Default action - open SMS app
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + sms.getSender()));
                context.startActivity(intent);
            }
        });

        // Set long click listener to add sender to blacklist
        holder.itemView.setOnLongClickListener(v -> {
            String sender = sms.getSender();

            // Option 1: Use listener if available
            if (listener != null) {
                listener.onAddToBlacklist(sender);
                return true;
            }

            // Option 2: Direct service call
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, BlacklistService.class);
            intent.setAction(BlacklistService.ACTION_ADD_TO_BLACKLIST);
            intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, sender);
            context.startService(intent);

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public static class SMSViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, messagePreview, messageTime;
        ImageView imgContact;
        ImageButton btnReply;

        public SMSViewHolder(View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sender_name);
            messagePreview = itemView.findViewById(R.id.message_preview);
            messageTime = itemView.findViewById(R.id.message_time);
            imgContact = itemView.findViewById(R.id.img_contact);
            btnReply = itemView.findViewById(R.id.btn_reply);
        }
    }
}