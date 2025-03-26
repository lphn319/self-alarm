package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.DatabaseHelper;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.Event;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.AlarmScheduler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventDetailFragment extends Fragment {
    private TextView textTitle, textDescription, textDateTime, textLocation, textReminder;
    private Event event;
    private DatabaseHelper dbHelper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public EventDetailFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);

        // Initialize views
        textTitle = view.findViewById(R.id.text_detail_title);
        textDescription = view.findViewById(R.id.text_detail_description);
        textDateTime = view.findViewById(R.id.text_detail_datetime);
        textLocation = view.findViewById(R.id.text_detail_location);
        textReminder = view.findViewById(R.id.text_detail_reminder);
        FloatingActionButton fabEdit = view.findViewById(R.id.fab_edit_event);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Get event ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            long eventId = args.getLong("eventId", -1);
            if (eventId != -1) {
                event = dbHelper.getEvent(eventId);
                displayEventDetails();
            }
        }

        // Set edit button listener
        fabEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("eventId", event.getId());

            AddEditEventFragment addEditEventFragment = new AddEditEventFragment();
            addEditEventFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addEditEventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sử dụng MenuProvider để xử lý menu trong Fragment
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_event_detail, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete) {
                    showDeleteConfirmationDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload event data in case it was edited
        if (event != null) {
            event = dbHelper.getEvent(event.getId());
            displayEventDetails();
        }
    }

    private void displayEventDetails() {
        if (event == null) return;

        textTitle.setText(event.getTitle());

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            textDescription.setText(event.getDescription());
            textDescription.setVisibility(View.VISIBLE);
        } else {
            textDescription.setVisibility(View.GONE);
        }

        // Format date and time
        String dateTimeText = dateFormat.format(event.getStartTime().getTime()) +
                ", " + timeFormat.format(event.getStartTime().getTime()) +
                " - " + timeFormat.format(event.getEndTime().getTime());
        textDateTime.setText(dateTimeText);

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            textLocation.setText(event.getLocation());
            textLocation.setVisibility(View.VISIBLE);
        } else {
            textLocation.setVisibility(View.GONE);
        }

        if (event.isHasAlarm()) {
            int minutes = event.getReminderMinutes();
            String reminderText;

            if (minutes < 60) {
                reminderText = "Nhắc nhở " + minutes + " phút trước khi bắt đầu";
            } else if (minutes == 60) {
                reminderText = "Nhắc nhở 1 giờ trước khi bắt đầu";
            } else if (minutes < 1440) { // Less than 24 hours
                int hours = minutes / 60;
                int remainingMinutes = minutes % 60;
                if (remainingMinutes == 0) {
                    reminderText = "Nhắc nhở " + hours + " giờ trước khi bắt đầu";
                } else {
                    reminderText = "Nhắc nhở " + hours + " giờ " + remainingMinutes + " phút trước khi bắt đầu";
                }
            } else {
                int days = minutes / 1440;
                reminderText = "Nhắc nhở " + days + " ngày trước khi bắt đầu";
            }

            textReminder.setText(reminderText);
            textReminder.setVisibility(View.VISIBLE);
        } else {
            textReminder.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc chắn muốn xóa sự kiện này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteEvent())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteEvent() {
        if (event != null) {
            // Cancel any alarms for this event
            if (event.isHasAlarm()) {
                AlarmScheduler.cancelAlarm(requireContext(), event);
            }

            // Delete from database
            dbHelper.deleteEvent(event.getId());

            // Show confirmation
            showToast();

            // Quay lại màn hình trước
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showToast() {
        Toast.makeText(requireContext(), "Sự kiện đã được xóa", Toast.LENGTH_SHORT).show();
    }
}