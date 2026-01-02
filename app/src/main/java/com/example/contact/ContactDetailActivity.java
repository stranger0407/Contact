package com.example.contact;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class ContactDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int REQUEST_SMS_PERMISSION = 2;

    // Contact data passed from list
    private String contactId;
    private String lookupKey;
    private String contactName;
    private String contactNumber;
    private String contactEmail;
    private String contactPhotoUri;
    private boolean isStarred;
    
    private ImageView imgContact;
    private TextView txtName, txtNumber, txtEmail;
    private ImageButton btnBack, btnFavorite, btnEdit, btnShare;
    private LinearLayout btnCall, btnMessage, btnVideoCall, btnEmailAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        // Get contact info from intent - use passed data directly
        Intent intent = getIntent();
        contactId = intent.getStringExtra("contact_id");
        lookupKey = intent.getStringExtra("contact_lookup_key");
        contactName = intent.getStringExtra("contact_name");
        contactNumber = intent.getStringExtra("contact_number");
        contactEmail = intent.getStringExtra("contact_email");
        contactPhotoUri = intent.getStringExtra("contact_photo");
        isStarred = intent.getBooleanExtra("contact_starred", false);
        
        if (contactId == null || contactNumber == null) {
            Toast.makeText(this, "Contact not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayContactInfo();
        setupClickListeners();
    }

    private void initViews() {
        imgContact = findViewById(R.id.imgContactDetail);
        txtName = findViewById(R.id.txtContactName);
        txtNumber = findViewById(R.id.txtContactNumber);
        txtEmail = findViewById(R.id.txtContactEmail);
        
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnEdit = findViewById(R.id.btnEdit);
        btnShare = findViewById(R.id.btnShare);
        
        // Hide delete button - we'll use system contacts for that
        View btnDelete = findViewById(R.id.btnDelete);
        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }
        
        btnCall = findViewById(R.id.btnCallAction);
        btnMessage = findViewById(R.id.btnMessageAction);
        btnVideoCall = findViewById(R.id.btnVideoCallAction);
        btnEmailAction = findViewById(R.id.btnEmailAction);
    }

    private void displayContactInfo() {
        // Load photo using Glide
        if (contactPhotoUri != null && !contactPhotoUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(contactPhotoUri))
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(imgContact);
        } else {
            imgContact.setImageResource(R.drawable.profile);
        }
        
        txtName.setText(contactName != null ? contactName : "Unknown");
        txtNumber.setText(contactNumber != null ? contactNumber : "");
        
        if (contactEmail != null && !contactEmail.isEmpty()) {
            txtEmail.setText(contactEmail);
            txtEmail.setVisibility(View.VISIBLE);
            if (btnEmailAction != null) {
                btnEmailAction.setVisibility(View.VISIBLE);
            }
        } else {
            txtEmail.setVisibility(View.GONE);
            if (btnEmailAction != null) {
                btnEmailAction.setVisibility(View.GONE);
            }
        }
        
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (isStarred) {
            btnFavorite.setImageResource(R.drawable.baseline_star_24);
        } else {
            btnFavorite.setImageResource(R.drawable.baseline_star_border_24);
        }
    }

    private void setupClickListeners() {
        // Back button - simple finish
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnFavorite.setOnClickListener(v -> {
            openSystemContactForEdit();
        });

        btnEdit.setOnClickListener(v -> openSystemContactForEdit());

        btnShare.setOnClickListener(v -> shareContact());

        btnCall.setOnClickListener(v -> makePhoneCall());

        btnMessage.setOnClickListener(v -> openSmsApp());

        if (btnVideoCall != null) {
            btnVideoCall.setOnClickListener(v -> makeVideoCall());
        }

        if (btnEmailAction != null) {
            btnEmailAction.setOnClickListener(v -> sendEmail());
        }
    }

    private Uri getContactUri() {
        if (contactId != null && lookupKey != null && !lookupKey.isEmpty()) {
            try {
                return android.provider.ContactsContract.Contacts.getLookupUri(
                        Long.parseLong(contactId), lookupKey);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void openSystemContactForEdit() {
        Uri contactUri = getContactUri();
        if (contactUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(contactUri);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot open contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall() {
        if (contactNumber == null || contactNumber.isEmpty()) {
            Toast.makeText(this, "No phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(callIntent);
        }
    }

    private void openSmsApp() {
        if (contactNumber == null || contactNumber.isEmpty()) {
            Toast.makeText(this, "No phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + contactNumber));
        startActivity(smsIntent);
    }

    private void makeVideoCall() {
        if (contactNumber == null || contactNumber.isEmpty()) {
            Toast.makeText(this, "No phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
            whatsappIntent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + contactNumber));
            whatsappIntent.setPackage("com.whatsapp");
            startActivity(whatsappIntent);
        } catch (Exception e) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(dialIntent);
            Toast.makeText(this, "WhatsApp not found, opening dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        if (contactEmail != null && !contactEmail.isEmpty()) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + contactEmail));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        } else {
            Toast.makeText(this, "No email address", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareContact() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("Contact: ").append(contactName != null ? contactName : "").append("\n");
        shareText.append("Phone: ").append(contactNumber != null ? contactNumber : "");
        if (contactEmail != null && !contactEmail.isEmpty()) {
            shareText.append("\nEmail: ").append(contactEmail);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share contact"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CALL_PERMISSION) {
                makePhoneCall();
            }
        }
    }
}
