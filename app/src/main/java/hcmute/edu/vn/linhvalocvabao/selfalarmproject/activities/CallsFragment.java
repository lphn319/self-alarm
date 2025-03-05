package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.CallsAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.CallLogEntry;

public class CallsFragment extends Fragment {
    private RecyclerView recyclerViewCalls;
    private CallsAdapter callsAdapter;
    private List<CallLogEntry> callList;
    private FloatingActionButton fabCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        // Ánh xạ RecyclerView
        recyclerViewCalls = view.findViewById(R.id.recycler_view_calls);
        recyclerViewCalls.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dữ liệu giả lập
        callList = new ArrayList<>();
        callList.add(new CallLogEntry("0123456789", "Incoming", "2024-07-09"));
        callList.add(new CallLogEntry("0987654321", "Outgoing", "2024-07-08"));
        callList.add(new CallLogEntry("0765432109", "Missed", "2024-07-07"));

        // Thiết lập Adapter
        callsAdapter = new CallsAdapter(callList);
        recyclerViewCalls.setAdapter(callsAdapter);

        fabCall = view.findViewById(R.id.fab_call);
        if (fabCall != null) {
            Log.d("CallsFragment", "FAB found successfully");
            Log.d("CallsFragment", "FAB visibility trước: " + fabCall.getVisibility());
            fabCall.setVisibility(View.VISIBLE);
            Log.d("CallsFragment", "FAB visibility sau: " + fabCall.getVisibility());
        } else {
            Log.e("CallsFragment", "FAB is null");
        }

        fabCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });


        return view;
    }
    private void makePhoneCall() {
//        String phoneNumber = "0123456789"; // Có thể thay bằng số từ danh sách cuộc gọi
//        Intent intent = new Intent(Intent.ACTION_DIAL);
//        intent.setData(Uri.parse("tel:" + phoneNumber));
//        startActivity(intent);
    }
}
