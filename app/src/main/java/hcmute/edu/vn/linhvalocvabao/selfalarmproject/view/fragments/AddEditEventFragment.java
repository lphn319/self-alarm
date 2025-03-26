package hcmute.edu.vn.linhvalocvabao.selfalarmproject.view.fragments;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.DatabaseHelper;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.Event;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.AlarmScheduler;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditEventFragment extends Fragment {
    private EditText editTitle, editDescription, editLocation;
    private TextView textStartDate, textStartTime, textEndDate, textEndTime, textReminderValue;
    private CheckBox checkHasAlarm;
    private Slider sliderReminderTime;
    private Button btnSave;

    private Calendar startTime, endTime;
    private DatabaseHelper dbHelper;
    private Event currentEvent;
    private boolean isEditMode = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public AddEditEventFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_event, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo View
        initializeViews(view);

        // Lấy dữ liệu từ arguments
        Bundle args = getArguments();
        if (args != null) {
            long eventId = args.getLong("eventId", -1);
            long selectedDateMillis = args.getLong("selectedDate", -1);

            if (eventId != -1) {
                isEditMode = true;
                currentEvent = dbHelper.getEvent(eventId);
                populateEventData();
            } else {
                currentEvent = new Event();
                setupDefaultTimes(selectedDateMillis);
            }
        }

        setUpListeners();
        return view;
    }

    private void initializeViews(View view) {
        editTitle = view.findViewById(R.id.edit_event_title);
        editDescription = view.findViewById(R.id.edit_event_description);
        editLocation = view.findViewById(R.id.edit_event_location);
        textStartDate = view.findViewById(R.id.text_start_date);
        textStartTime = view.findViewById(R.id.text_start_time);
        textEndDate = view.findViewById(R.id.text_end_date);
        textEndTime = view.findViewById(R.id.text_end_time);
        checkHasAlarm = view.findViewById(R.id.check_has_alarm);
        sliderReminderTime = view.findViewById(R.id.slider_reminder_time);
        textReminderValue = view.findViewById(R.id.text_reminder_value);
        btnSave = view.findViewById(R.id.btn_save_event);
    }

    private void setupDefaultTimes(long selectedDateMillis) {
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        if (selectedDateMillis != -1) {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selectedDateMillis);
            startTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
            startTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
            startTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

            endTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
            endTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
            endTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
        }

        currentEvent.setStartTime(startTime);
        currentEvent.setEndTime(endTime);
        updateDateTimeViews();
    }

    private void populateEventData() {
        if (currentEvent == null) return;

        editTitle.setText(currentEvent.getTitle());
        editDescription.setText(currentEvent.getDescription());
        editLocation.setText(currentEvent.getLocation());

        startTime = currentEvent.getStartTime();
        endTime = currentEvent.getEndTime();

        checkHasAlarm.setChecked(currentEvent.isHasAlarm());
        sliderReminderTime.setValue(currentEvent.getReminderMinutes());

        updateDateTimeViews();
        updateReminderViews();
    }

    private void setUpListeners() {
        textStartDate.setOnClickListener(v -> showDatePicker(true));
        textEndDate.setOnClickListener(v -> showDatePicker(false));

        textStartTime.setOnClickListener(v -> showTimePicker(true));
        textEndTime.setOnClickListener(v -> showTimePicker(false));

        sliderReminderTime.addOnChangeListener((slider, value, fromUser) -> {
            int minutes = (int) value;
            currentEvent.setReminderMinutes(minutes);
            updateReminderText(minutes);
        });

        checkHasAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentEvent.setHasAlarm(isChecked);
            sliderReminderTime.setEnabled(isChecked);
            textReminderValue.setEnabled(isChecked);
        });

        btnSave.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startTime : endTime;

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(calendar.getTimeInMillis())
                .setTitleText(isStartDate ? "Chọn ngày bắt đầu" : "Chọn ngày kết thúc")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTimeInMillis(selection);

            if (isStartDate) {
                startTime.setTime(selectedCal.getTime());
            } else {
                endTime.setTime(selectedCal.getTime());
            }

            updateDateTimeViews();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar calendar = isStartTime ? startTime : endTime;

        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    if (isStartTime) {
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                    } else {
                        endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                    }
                    updateDateTimeViews();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);

        timePicker.show();
    }

    private void updateDateTimeViews() {
        textStartDate.setText(dateFormat.format(startTime.getTime()));
        textStartTime.setText(timeFormat.format(startTime.getTime()));
        textEndDate.setText(dateFormat.format(endTime.getTime()));
        textEndTime.setText(timeFormat.format(endTime.getTime()));

        currentEvent.setStartTime(startTime);
        currentEvent.setEndTime(endTime);
    }

    private void updateReminderViews() {
        sliderReminderTime.setValue(currentEvent.getReminderMinutes());
        updateReminderText(currentEvent.getReminderMinutes());
    }

    @SuppressLint("SetTextI18n")
    private void updateReminderText(int minutes) {
        textReminderValue.setText(minutes + " phút trước");
    }

    private void saveEvent() {
        String title = editTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tiêu đề sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        currentEvent.setTitle(title);
        currentEvent.setDescription(editDescription.getText().toString().trim());
        currentEvent.setLocation(editLocation.getText().toString().trim());

        if (isEditMode) {
            AlarmScheduler.cancelAlarm(requireContext(), currentEvent);
            dbHelper.updateEvent(currentEvent);
        } else {
            long id = dbHelper.addEvent(currentEvent);
            currentEvent.setId(id);
        }

        if (currentEvent.isHasAlarm()) {
            AlarmScheduler.scheduleAlarm(requireContext(), currentEvent);
        }

        Toast.makeText(requireContext(), isEditMode ? "Sự kiện đã cập nhật" : "Sự kiện đã thêm", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}
