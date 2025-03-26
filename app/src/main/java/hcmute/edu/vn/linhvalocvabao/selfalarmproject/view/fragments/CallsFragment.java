package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.CallsAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.CallLogEntry;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.controller.receivers.CallReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.CallStateManager;

public class CallsFragment extends Fragment {
    private static final String TAG = "CallsFragment";
    private static final int REQUEST_CALL_PERMISSION = 1002;

    private RecyclerView recyclerViewCalls;
    private CallsAdapter callsAdapter;
    private List<CallLogEntry> callList;
    private CallStateManager callStateManager;

    // BroadcastReceiver for call state changes
    private BroadcastReceiver callReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            String phoneNumber = intent.getStringExtra("phoneNumber");

            if (state != null && state.equals("ENDED")) {
                // Call ended, update call log
                String callType = intent.getStringExtra("callType");
                String callDate = intent.getStringExtra("callDate");
                long callDuration = intent.getLongExtra("callDuration", 0);

                // Add to call log list
                addCallLogEntry(phoneNumber, callType, callDate, callDuration);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        // Ánh xạ RecyclerView
        recyclerViewCalls = view.findViewById(R.id.recycler_view_calls);
        recyclerViewCalls.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize call list
        callList = new ArrayList<>();

        // Set up adapter
        callsAdapter = new CallsAdapter(callList);
        recyclerViewCalls.setAdapter(callsAdapter);

        // Initialize CallStateManager
        callStateManager = new CallStateManager(getContext());

        // FAB for making a call
        FloatingActionButton fabCall = view.findViewById(R.id.fab_call);
        fabCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            startActivity(intent);
        });

        // Request permissions if needed
        requestCallPermissions();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register receiver for call broadcasts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(callReceiver,
                    new IntentFilter(CallReceiver.CALL_STATE_CHANGED_ACTION), Context.RECEIVER_NOT_EXPORTED);
        }

        // Start listening for call state changes
        callStateManager.startListening();

        // Load call log
        if (hasRequiredPermissions()) {
            loadCallLog();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister receiver
        try {
            getActivity().unregisterReceiver(callReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }

        // Stop listening for call state changes
        callStateManager.stopListening();
    }

    private void addCallLogEntry(String phoneNumber, String callType, String callDate, long duration) {
        // Format the duration
        String durationStr = formatDuration(duration);

        // Create call log entry with duration
        CallLogEntry entry = new CallLogEntry(phoneNumber, callType, callDate);
        entry.setDuration(durationStr);

        // Add to the top of the list
        callList.add(0, entry);
        callsAdapter.notifyItemInserted(0);
        recyclerViewCalls.scrollToPosition(0);
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " sec";
        } else {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " min " + remainingSeconds + " sec";
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCallPermissions() {
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.PROCESS_OUTGOING_CALLS
                    },
                    REQUEST_CALL_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, load call log
                loadCallLog();
            } else {
                // Permissions denied
                Toast.makeText(getContext(), "Call permissions are required for this feature",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadCallLog() {
        callList.clear();

        // Get call log cursor
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

            Cursor cursor = getActivity().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null, null, null, CallLog.Calls.DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                do {
                    String phoneNumber = cursor.getString(numberIndex);
                    int callTypeCode = cursor.getInt(typeIndex);
                    long callDate = cursor.getLong(dateIndex);
                    long duration = cursor.getLong(durationIndex);

                    // Convert call type code to string
                    String callType;
                    switch (callTypeCode) {
                        case CallLog.Calls.INCOMING_TYPE:
                            callType = "Incoming";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            callType = "Outgoing";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callType = "Missed";
                            break;
                        default:
                            callType = "Unknown";
                            break;
                    }

                    // Format date
                    String formattedDate = dateFormat.format(new Date(callDate));

                    // Format duration
                    String formattedDuration = formatDuration(duration);

                    // Create call log entry
                    CallLogEntry entry = new CallLogEntry(phoneNumber, callType, formattedDate);
                    entry.setDuration(formattedDuration);
                    callList.add(entry);

                } while (cursor.moveToNext());

                cursor.close();
            }

            // Update adapter
            callsAdapter.notifyDataSetChanged();
        }
    }
}