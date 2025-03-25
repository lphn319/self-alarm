package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.models.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "scheduler.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_HAS_ALARM = "has_alarm";
    private static final String COLUMN_REMINDER_MINUTES = "reminder_minutes";
    private static final String COLUMN_LOCATION = "location";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_START_TIME + " INTEGER,"
                + COLUMN_END_TIME + " INTEGER,"
                + COLUMN_HAS_ALARM + " INTEGER,"
                + COLUMN_REMINDER_MINUTES + " INTEGER,"
                + COLUMN_LOCATION + " TEXT"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // CRUD Operations

    // Add new event
    public long addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_START_TIME, event.getStartTime().getTimeInMillis());
        values.put(COLUMN_END_TIME, event.getEndTime().getTimeInMillis());
        values.put(COLUMN_HAS_ALARM, event.isHasAlarm() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(COLUMN_LOCATION, event.getLocation());

        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return id;
    }

    // Get single event
    public Event getEvent(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS, new String[]{
                        COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_START_TIME,
                        COLUMN_END_TIME, COLUMN_HAS_ALARM, COLUMN_REMINDER_MINUTES, COLUMN_LOCATION},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null, null);

        cursor.moveToFirst();

        Event event = cursorToEvent(cursor);
        cursor.close();
        return event;
    }

    // Get all events
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_START_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(cursor);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return eventList;
    }

    // Get events for a specific day
    public List<Event> getEventsForDay(Calendar day) {
        List<Event> eventList = new ArrayList<>();

        Calendar startOfDay = (Calendar) day.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = (Calendar) startOfDay.clone();
        endOfDay.add(Calendar.DAY_OF_MONTH, 1);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null,
                "(" + COLUMN_START_TIME + " >= ? AND " + COLUMN_START_TIME + " < ?) OR " +
                        "(" + COLUMN_END_TIME + " > ? AND " + COLUMN_END_TIME + " <= ?) OR " +
                        "(" + COLUMN_START_TIME + " <= ? AND " + COLUMN_END_TIME + " >= ?)",
                new String[]{
                        String.valueOf(startOfDay.getTimeInMillis()),
                        String.valueOf(endOfDay.getTimeInMillis()),
                        String.valueOf(startOfDay.getTimeInMillis()),
                        String.valueOf(endOfDay.getTimeInMillis()),
                        String.valueOf(startOfDay.getTimeInMillis()),
                        String.valueOf(endOfDay.getTimeInMillis())
                },
                null, null, COLUMN_START_TIME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(cursor);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return eventList;
    }

    // Update event
    public void updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_START_TIME, event.getStartTime().getTimeInMillis());
        values.put(COLUMN_END_TIME, event.getEndTime().getTimeInMillis());
        values.put(COLUMN_HAS_ALARM, event.isHasAlarm() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(COLUMN_LOCATION, event.getLocation());

        int result = db.update(TABLE_EVENTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(event.getId())});
        db.close();
    }

    // Delete event
    public void deleteEvent(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // Helper method to convert cursor to event
    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
        event.setStartTime(startTime);

        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
        event.setEndTime(endTime);

        event.setHasAlarm(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HAS_ALARM)) == 1);
        event.setReminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_MINUTES)));
        event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));

        return event;
    }
}