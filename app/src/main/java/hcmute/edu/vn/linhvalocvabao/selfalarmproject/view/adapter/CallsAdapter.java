package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.CallLogEntry;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.BlacklistService;

public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.ViewHolder> {
    private final List<CallLogEntry> callList;
    private OnCallItemClickListener listener;

    public interface OnCallItemClickListener {
        void onCallClick(String phoneNumber);
        void onAddToBlacklist(String phoneNumber);
    }

    public CallsAdapter(List<CallLogEntry> callList) {
        this.callList = callList;
    }

    public void setOnCallItemClickListener(OnCallItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogEntry call = callList.get(position);
        holder.txtPhoneNumber.setText(call.getPhoneNumber());
        holder.txtCallType.setText(call.getCallType());
        holder.txtCallDate.setText(call.getCallDate());

        // Display duration if available
        String duration = call.getDuration();
        if (duration != null && !duration.isEmpty()) {
            // Đảm bảo txtCallDuration đã được ánh xạ trong ViewHolder
            if (holder.txtCallDuration != null) {
                holder.txtCallDuration.setVisibility(View.VISIBLE);
                holder.txtCallDuration.setText("Duration: " + duration);
            }
        } else {
            // Đảm bảo txtCallDuration đã được ánh xạ trong ViewHolder
            if (holder.txtCallDuration != null) {
                holder.txtCallDuration.setVisibility(View.GONE);
            }
        }

        // Set call type icon and color based on call type
        Context context = holder.itemView.getContext();

        switch (call.getCallType()) {
            case "Incoming":
                holder.imgCallType.setImageResource(R.drawable.ic_call_incoming);
                holder.imgCallType.setColorFilter(Color.GREEN);
                break;
            case "Outgoing":
                holder.imgCallType.setImageResource(R.drawable.ic_call_outgoing);
                holder.imgCallType.setColorFilter(Color.BLUE);
                break;
            case "Missed":
                holder.imgCallType.setImageResource(R.drawable.ic_call_missed);
                holder.imgCallType.setColorFilter(Color.RED);
                break;
            default:
                holder.imgCallType.setImageResource(R.drawable.ic_call);
                break;
        }

        // Set click listener for call back button
        holder.btnCall.setOnClickListener(v -> {
            String phoneNumber = call.getPhoneNumber();
            if (listener != null) {
                listener.onCallClick(phoneNumber);
            } else {
                // Default action - dial number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(intent);
            }
        });

        // Set long click listener to add to blacklist
        holder.itemView.setOnLongClickListener(v -> {
            String phoneNumber = call.getPhoneNumber();

            // Option 1: Use listener if available
            if (listener != null) {
                listener.onAddToBlacklist(phoneNumber);
                return true;
            }

            // Option 2: Direct service call
            Intent intent = new Intent(context, BlacklistService.class);
            intent.setAction(BlacklistService.ACTION_ADD_TO_BLACKLIST);
            intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
            context.startService(intent);

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPhoneNumber, txtCallType, txtCallDate, txtCallDuration; // Thêm txtCallDuration
        ImageView imgCallType;
        ImageButton btnCall;

        public ViewHolder(View itemView) {
            super(itemView);
            txtPhoneNumber = itemView.findViewById(R.id.txt_phone_number);
            txtCallType = itemView.findViewById(R.id.txt_call_type);
            txtCallDate = itemView.findViewById(R.id.txt_call_date);
            txtCallDuration = itemView.findViewById(R.id.txt_call_duration); // Ánh xạ txtCallDuration
            imgCallType = itemView.findViewById(R.id.img_call_type);
            btnCall = itemView.findViewById(R.id.btn_call);
        }
    }
}