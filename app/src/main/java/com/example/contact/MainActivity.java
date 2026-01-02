package com.example.contact;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 100;
    private static final String PREFS_NAME = "ContactsCache";
    private static final String KEY_CONTACTS = "saved_contacts";
    private static final String KEY_LAST_SYNC = "last_sync_time";

    ArrayList<ContactModel> arrContacts = new ArrayList<>();
    ArrayList<ContactModel> allContacts = new ArrayList<>();
    FloatingActionButton btnOpenDialog;
    RecyclerContactAdapter adapter;
    EditText searchBar;
    RecyclerView recyclerView;
    View emptyView;
    SwipeRefreshLayout swipeRefreshLayout;
    
    private ExecutorService executor;
    private Handler mainHandler;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize views
        recyclerView = findViewById(R.id.contact);
        btnOpenDialog = findViewById(R.id.btnOpenDialog);
        searchBar = findViewById(R.id.searchBar);
        emptyView = findViewById(R.id.emptyView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Setup SwipeRefreshLayout for manual refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light
            );
            swipeRefreshLayout.setOnRefreshListener(this::refreshFromSystem);
        }

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        adapter = new RecyclerContactAdapter(this, arrContacts);
        recyclerView.setAdapter(adapter);

        setupSearch();

        btnOpenDialog.setOnClickListener(view -> openAddContactIntent());

        // Load contacts - from storage first, then from system if needed
        loadContacts();
    }

    private void loadContacts() {
        // First, try to load from saved storage (instant)
        ArrayList<ContactModel> savedContacts = loadFromStorage();
        
        if (!savedContacts.isEmpty()) {
            // Show saved contacts instantly
            allContacts.clear();
            allContacts.addAll(savedContacts);
            arrContacts.clear();
            arrContacts.addAll(savedContacts);
            adapter.notifyDataSetChanged();
            updateEmptyView();
            return;
        }

        // No saved data - load from system (first time only)
        if (checkContactsPermission()) {
            loadFromSystemAndSave();
        }
    }

    private boolean checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFromSystemAndSave();
            } else {
                Toast.makeText(this, "Permission denied. Cannot load contacts.", Toast.LENGTH_LONG).show();
                updateEmptyView();
            }
        }
    }

    private void refreshFromSystem() {
        if (!checkContactsPermission()) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        
        loadFromSystemAndSave();
    }

    private void loadFromSystemAndSave() {
        if (isLoading) return;
        isLoading = true;

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        executor.execute(() -> {
            ArrayList<ContactModel> contacts = loadContactsFromSystem();

            mainHandler.post(() -> {
                isLoading = false;
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                // Save to storage for future instant loading
                saveToStorage(contacts);

                // Update UI
                allContacts.clear();
                allContacts.addAll(contacts);
                arrContacts.clear();
                arrContacts.addAll(contacts);
                adapter.notifyDataSetChanged();
                updateEmptyView();

                Toast.makeText(MainActivity.this, 
                    "Loaded " + contacts.size() + " contacts", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private ArrayList<ContactModel> loadContactsFromSystem() {
        ArrayList<ContactModel> contacts = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        String[] projection = {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.Contacts.STARRED
        };

        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1",
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String lookupKey = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String photoUriStr = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                int starred = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED));

                Uri photoUri = photoUriStr != null ? Uri.parse(photoUriStr) : null;
                String phoneNumber = getPhoneNumber(contentResolver, contactId);
                String email = getEmail(contentResolver, contactId);

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    ContactModel contact = new ContactModel(
                            contactId, lookupKey,
                            name != null ? name : "",
                            phoneNumber, email, photoUri,
                            starred == 1
                    );
                    contacts.add(contact);
                }
            }
            cursor.close();
        }

        // Sort: favorites first, then alphabetically
        Collections.sort(contacts, (c1, c2) -> {
            if (c1.isStarred() && !c2.isStarred()) return -1;
            if (!c1.isStarred() && c2.isStarred()) return 1;
            return c1.getName().compareToIgnoreCase(c2.getName());
        });

        return contacts;
    }

    private String getPhoneNumber(ContentResolver contentResolver, String contactId) {
        String phoneNumber = null;
        Cursor phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null
        );
        if (phoneCursor != null) {
            if (phoneCursor.moveToFirst()) {
                phoneNumber = phoneCursor.getString(0);
            }
            phoneCursor.close();
        }
        return phoneNumber;
    }

    private String getEmail(ContentResolver contentResolver, String contactId) {
        String email = null;
        Cursor emailCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contactId}, null
        );
        if (emailCursor != null) {
            if (emailCursor.moveToFirst()) {
                email = emailCursor.getString(0);
            }
            emailCursor.close();
        }
        return email;
    }

    // Save contacts to SharedPreferences as JSON
    private void saveToStorage(ArrayList<ContactModel> contacts) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ContactModel contact : contacts) {
                JSONObject obj = new JSONObject();
                obj.put("id", contact.getId());
                obj.put("lookupKey", contact.getLookupKey());
                obj.put("name", contact.getName());
                obj.put("number", contact.getNumber());
                obj.put("email", contact.getEmail());
                obj.put("photoUri", contact.getPhotoUri() != null ? contact.getPhotoUri().toString() : "");
                obj.put("starred", contact.isStarred());
                jsonArray.put(obj);
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(KEY_CONTACTS, jsonArray.toString())
                    .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                    .apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Load contacts from SharedPreferences
    private ArrayList<ContactModel> loadFromStorage() {
        ArrayList<ContactModel> contacts = new ArrayList<>();
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_CONTACTS, null);
        
        if (jsonStr == null || jsonStr.isEmpty()) {
            return contacts;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                String photoUriStr = obj.optString("photoUri", "");
                Uri photoUri = !photoUriStr.isEmpty() ? Uri.parse(photoUriStr) : null;
                
                ContactModel contact = new ContactModel(
                        obj.getString("id"),
                        obj.optString("lookupKey", ""),
                        obj.getString("name"),
                        obj.getString("number"),
                        obj.optString("email", ""),
                        photoUri,
                        obj.optBoolean("starred", false)
                );
                contacts.add(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contacts;
    }

    private void openAddContactIntent() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivity(intent);
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterContacts(String query) {
        arrContacts.clear();
        
        if (query.isEmpty()) {
            arrContacts.addAll(allContacts);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ContactModel contact : allContacts) {
                if (contact.getName().toLowerCase().contains(lowerQuery) ||
                    contact.getNumber().contains(query)) {
                    arrContacts.add(contact);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (arrContacts.isEmpty() && !isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
