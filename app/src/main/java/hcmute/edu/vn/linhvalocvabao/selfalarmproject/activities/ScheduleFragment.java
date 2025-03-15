package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapter.EventAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.DatabaseHelper;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment  {
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private DatabaseHelper dbHelper;
    private Calendar selectedDate;

    public ScheduleFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        // Initialize components
        calendarView = view.findViewById(R.id.calendar_view);
        recyclerView = view.findViewById(R.id.recycler_events);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_event);

        // Set up database
        dbHelper = new DatabaseHelper(requireContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize with current date
        selectedDate = Calendar.getInstance();
        loadEventsForDate(selectedDate);

        // Set calendar listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            loadEventsForDate(selectedDate);
        });

        // Set FAB listener
        fab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("selectedDate", selectedDate.getTimeInMillis());

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

        // Sử dụng MenuProvider để xử lý menu
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_today) {
                    // Nhảy đến ngày hôm nay
                    selectedDate = Calendar.getInstance();
                    calendarView.setDate(selectedDate.getTimeInMillis());
                    loadEventsForDate(selectedDate);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning to this fragment
        loadEventsForDate(selectedDate);
    }

    private void loadEventsForDate(Calendar date) {
        List<Event> events = dbHelper.getEventsForDay(date);

        if (adapter == null) {
            adapter = new EventAdapter(requireContext(), events);
            adapter.setOnItemClickListener(event -> {
                Bundle bundle = new Bundle();
                bundle.putLong("eventId", event.getId());

                EventDetailFragment eventDetailFragment = new EventDetailFragment();
                eventDetailFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventDetailFragment)
                        .addToBackStack(null)
                        .commit();
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(events);
        }
    }
}