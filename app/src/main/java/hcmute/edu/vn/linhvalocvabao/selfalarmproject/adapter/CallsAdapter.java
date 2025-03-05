package hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.CallLogEntry;

public class CallsAdapter extends RecyclerView.Adapter<CallsAdapter.ViewHolder> {
    private final List<CallLogEntry> callList;

    public CallsAdapter(List<CallLogEntry> callList) {
        this.callList = callList;
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
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPhoneNumber, txtCallType, txtCallDate;

        public ViewHolder(View itemView) {
            super(itemView);
            txtPhoneNumber = itemView.findViewById(R.id.txt_phone_number);
            txtCallType = itemView.findViewById(R.id.txt_call_type);
            txtCallDate = itemView.findViewById(R.id.txt_call_date);
        }
    }
}
