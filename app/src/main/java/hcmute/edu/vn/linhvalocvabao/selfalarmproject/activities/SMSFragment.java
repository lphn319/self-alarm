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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    private ProgressBar progressBar;
    private static final int BATCH_SIZE = 50;

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

        // Initialize progress bar
        progressBar = view.findViewById(R.id.progress_bar);

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
        progressBar.setVisibility(View.VISIBLE);
        smsList.clear();

        new AsyncTask<Void, List<SMS>, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                List<SMS> batchList = new ArrayList<>();

                try (Cursor cursor = getActivity().getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        null, null, null, "date DESC")) {

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            int addressIndex = cursor.getColumnIndex("address");
                            int bodyIndex = cursor.getColumnIndex("body");

                            if (addressIndex != -1 && bodyIndex != -1) {
                                String address = cursor.getString(addressIndex);
                                String body = cursor.getString(bodyIndex);

                                String sender = address;
                                // Only resolve contact name if we have a valid phone number
                                if (android.telephony.PhoneNumberUtils.isGlobalPhoneNumber(address)) {
                                    String contactName = getContactName(address);
                                    if (contactName != null) {
                                        sender = contactName;
                                    }
                                }

                                batchList.add(new SMS(sender, body));

                                if (batchList.size() >= BATCH_SIZE) {
                                    publishProgress(new ArrayList<>(batchList));
                                    batchList.clear();
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading SMS messages", e);
                }

                if (!batchList.isEmpty()) {
                    publishProgress(batchList);
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(List<SMS>... values) {
                if (values[0] != null) {
                    smsList.addAll(values[0]);
                    smsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @SuppressLint("Range")
    private String getContactName(String phoneNumber) {
        String contactName = null;

        try (Cursor cursor = getActivity().getContentResolver().query(
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(phoneNumber)),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact name", e);
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