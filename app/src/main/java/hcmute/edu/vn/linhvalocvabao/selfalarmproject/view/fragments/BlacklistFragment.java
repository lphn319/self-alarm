package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.BlacklistAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.BlacklistContact;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.services.BlacklistService;

public class BlacklistFragment extends Fragment {
    private static final String TAG = "BlacklistFragment";

    private RecyclerView recyclerView;
    private BlacklistAdapter adapter;
    private List<BlacklistContact> blacklist;
    private FloatingActionButton fabAdd;

    private BroadcastReceiver blacklistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String blacklistJson = intent.getStringExtra(BlacklistService.EXTRA_BLACKLIST);
            if (blacklistJson != null) {
                Type type = new TypeToken<ArrayList<BlacklistContact>>() {
                }.getType();
                blacklist = new Gson().fromJson(blacklistJson, type);
                adapter.updateData(blacklist);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_blacklist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        blacklist = new ArrayList<>();
        adapter = new BlacklistAdapter(blacklist);
        adapter.setOnItemClickListener(new BlacklistAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClick(BlacklistContact contact) {
                removeFromBlacklist(contact.getPhoneNumber());
            }

            @Override
            public void onSettingsChanged(BlacklistContact contact, boolean blockCalls, boolean blockMessages) {
                updateBlacklistSettings(contact.getPhoneNumber(), blockCalls, blockMessages);
            }
        });

        recyclerView.setAdapter(adapter);

        fabAdd = view.findViewById(R.id.fab_add_blacklist);
        fabAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register receiver
        getActivity().registerReceiver(
                blacklistReceiver,
                new IntentFilter("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BLACKLIST_RESULT"), Context.RECEIVER_NOT_EXPORTED
        );

        // Request blacklist
        requestBlacklist();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister receiver
        try {
            getActivity().unregisterReceiver(blacklistReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private void requestBlacklist() {
        Intent intent = new Intent(getContext(), BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_GET_BLACKLIST);
        getActivity().startService(intent);
    }

    private void addToBlacklist(String phoneNumber, String name) {
        Intent intent = new Intent(getContext(), BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_ADD_TO_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        intent.putExtra(BlacklistService.EXTRA_CONTACT_NAME, name);
        getActivity().startService(intent);

        // Request updated blacklist after a short delay
        new Handler().postDelayed(this::requestBlacklist, 500);
    }

    private void removeFromBlacklist(String phoneNumber) {
        Intent intent = new Intent(getContext(), BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_REMOVE_FROM_BLACKLIST);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        getActivity().startService(intent);

        // Request updated blacklist after a short delay
        new Handler().postDelayed(this::requestBlacklist, 500);
    }

    private void updateBlacklistSettings(String phoneNumber, boolean blockCalls, boolean blockMessages) {
        Intent intent = new Intent(getContext(), BlacklistService.class);
        intent.setAction(BlacklistService.ACTION_UPDATE_BLACKLIST_ITEM);
        intent.putExtra(BlacklistService.EXTRA_PHONE_NUMBER, phoneNumber);
        intent.putExtra(BlacklistService.EXTRA_BLOCK_CALLS, blockCalls);
        intent.putExtra(BlacklistService.EXTRA_BLOCK_MESSAGES, blockMessages);
        getActivity().startService(intent);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add to Blacklist");

        // Inflate layout for dialog
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_blacklist, null);
        EditText etPhoneNumber = view.findViewById(R.id.et_phone_number);
        EditText etName = view.findViewById(R.id.et_name);
        builder.setView(view);

        // Add buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (!phoneNumber.isEmpty()) {
                addToBlacklist(phoneNumber, name.isEmpty() ? null : name);
            } else {
                Toast.makeText(getContext(), "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show dialog
        builder.create().show();
    }
}