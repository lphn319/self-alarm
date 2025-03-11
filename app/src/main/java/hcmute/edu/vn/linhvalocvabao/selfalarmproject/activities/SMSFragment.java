package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
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

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.SMSAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.SMS;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.SMSReceiver;

public class SMSFragment extends Fragment {
    private static final String TAG = "SMSFragment";
    private static final int REQUEST_SMS_PERMISSION = 1001;

    private RecyclerView recyclerViewSMS;
    private SMSAdapter smsAdapter;
    private List<SMS> smsList;
    private FloatingActionButton fabNewMessage;

    // BroadcastReceiver for new SMS messages
    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract SMS data from intent
            String sender = intent.getStringExtra("sender");
            String message = intent.getStringExtra("message");

            // Update UI with new message
            addNewMessage(sender, message);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);

        // Ánh xạ RecyclerView
        recyclerViewSMS = view.findViewById(R.id.recycler_view_sms);
        recyclerViewSMS.setLayoutManager(new LinearLayoutManager(getContext()));

        // FAB for new message
        fabNewMessage = view.findViewById(R.id.fab_call);
        fabNewMessage.setOnClickListener(v -> openNewMessageActivity());

        // Initialize SMS list
        smsList = new ArrayList<>();

        // Set up adapter
        smsAdapter = new SMSAdapter(smsList);
        recyclerViewSMS.setAdapter(smsAdapter);

        // Request permissions if needed
        requestSmsPermissions();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register receiver for SMS broadcasts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().registerReceiver(smsReceiver,
                    new IntentFilter(SMSReceiver.SMS_RECEIVED_ACTION), Context.RECEIVER_NOT_EXPORTED);
        }

        // Load SMS messages
        if (hasRequiredPermissions()) {
            loadSmsMessages();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister receiver
        try {
            getActivity().unregisterReceiver(smsReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
    }

    private boolean hasRequiredPermissions() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS
                    },
                    REQUEST_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, load SMS
                loadSmsMessages();
            } else {
                // Permissions denied
                Toast.makeText(getContext(), "SMS permissions are required for this feature",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSmsMessages() {
        smsList.clear();

        // Use ContentResolver to query SMS inbox
        Cursor cursor = getActivity().getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                null, null, null, "date DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int addressIndex = cursor.getColumnIndex("address");
                int bodyIndex = cursor.getColumnIndex("body");

                if (addressIndex != -1 && bodyIndex != -1) {
                    String address = cursor.getString(addressIndex);
                    String body = cursor.getString(bodyIndex);

                    // Try to resolve contact name
                    String contactName = getContactName(address);
                    String sender = contactName != null ? contactName : address;

                    // Add to list
                    smsList.add(new SMS(sender, body));
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Notify adapter of data change
        smsAdapter.notifyDataSetChanged();
    }

    @SuppressLint("Range")
    private String getContactName(String phoneNumber) {
        String contactName = null;

        // Query the contacts for the phone number
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = getActivity().getContentResolver().query(
                uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }

        return contactName;
    }

    private void addNewMessage(String sender, String message) {
        // Add new message to the top of the list
        smsList.add(0, new SMS(sender, message));
        smsAdapter.notifyItemInserted(0);
        recyclerViewSMS.scrollToPosition(0);
    }

    private void openNewMessageActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"));
        startActivity(intent);
    }
}