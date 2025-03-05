package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.SMS;

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder> {
    private List<SMS> smsList;

    public SMSAdapter(List<SMS> smsList) {
        this.smsList = smsList;
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
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    public static class SMSViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, messagePreview;

        public SMSViewHolder(View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sender_name);
            messagePreview = itemView.findViewById(R.id.message_preview);
        }
    }
}
