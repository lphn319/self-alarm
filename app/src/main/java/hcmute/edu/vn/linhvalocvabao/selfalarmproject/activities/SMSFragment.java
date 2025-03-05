package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.SMSAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.SMS;

public class SMSFragment extends Fragment {
    private RecyclerView recyclerViewSMS;
    private SMSAdapter smsAdapter;
    private List<SMS> smsList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);

        // Ánh xạ RecyclerView
        recyclerViewSMS = view.findViewById(R.id.recycler_view_sms);
        recyclerViewSMS.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo dữ liệu mẫu
        smsList = new ArrayList<>();
        smsList.add(new SMS("Nguyễn Văn A", "Xin chào!"));
        smsList.add(new SMS("Trần Thị B", "Bạn có khỏe không?"));
        smsList.add(new SMS("Lê Văn C", "Nhớ trả bài tập nhé!"));

        // Gán adapter cho RecyclerView
        smsAdapter = new SMSAdapter(smsList);
        recyclerViewSMS.setAdapter(smsAdapter);

        return view;
    }
}
