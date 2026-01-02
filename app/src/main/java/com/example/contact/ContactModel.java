package com.example.contact;

import android.net.Uri;

public class ContactModel {
    private String id;  // Contact ID from system
    private String lookupKey;  // Lookup key for stable reference
    private Uri photoUri;  // Photo URI from system contacts
    private String name;
    private String number;
    private String email;
    private boolean isStarred;  // Starred/favorite status from system

    // Constructor for system contacts
    public ContactModel(String id, String lookupKey, String name, String number, String email, Uri photoUri, boolean isStarred) {
        this.id = id;
        this.lookupKey = lookupKey;
        this.name = name != null ? name : "";
        this.number = number != null ? number : "";
        this.email = email != null ? email : "";
        this.photoUri = photoUri;
        this.isStarred = isStarred;
    }

    // Simple constructor
    public ContactModel(String id, String name, String number) {
        this.id = id;
        this.lookupKey = "";
        this.name = name != null ? name : "";
        this.number = number != null ? number : "";
        this.email = "";
        this.photoUri = null;
        this.isStarred = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public boolean isFavorite() {
        return isStarred;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public void setNumber(String number) {
        this.number = number != null ? number : "";
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public void setStarred(boolean starred) {
        this.isStarred = starred;
    }

    public void setFavorite(boolean favorite) {
        this.isStarred = favorite;
    }

    // Get initials for avatar
    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // Get contact lookup URI for system operations
    public Uri getContactUri() {
        if (id != null && lookupKey != null && !lookupKey.isEmpty()) {
            return android.provider.ContactsContract.Contacts.getLookupUri(Long.parseLong(id), lookupKey);
        }
        return null;
    }

    // Legacy method for img - returns 0 since we use photoUri now
    public int getImg() {
        return 0;
    }
}
