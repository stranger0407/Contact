package com.example.contact;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecyclerContactAdapter extends RecyclerView.Adapter<RecyclerContactAdapter.ViewHolder> {

    private static final int REQUEST_CALL_PERMISSION = 100;
    
    private Context context;
    private ArrayList<ContactModel> arrContacts;
    private int lastPosition = -1;

    public RecyclerContactAdapter(Context context, ArrayList<ContactModel> arrContacts) {
        this.context = context;
        this.arrContacts = arrContacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contact = arrContacts.get(position);
        
        // Set contact image from system or show default
        if (contact.getPhotoUri() != null) {
            Glide.with(context)
                    .load(contact.getPhotoUri())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(holder.imgContact);
        } else {
            holder.imgContact.setImageResource(R.drawable.profile);
        }
        
        holder.txtName.setText(contact.getName());
        holder.txtNumber.setText(contact.getNumber());
        
        // Show favorite/starred icon
        if (contact.isStarred()) {
            holder.imgFavorite.setVisibility(View.VISIBLE);
        } else {
            holder.imgFavorite.setVisibility(View.GONE);
        }
        
        // Set animation
        setAnimation(holder.itemView, position);

        // Call button click listener
        holder.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    makeDirectCall(arrContacts.get(currentPosition).getNumber());
                }
            }
        });

        // Message button click listener
        holder.btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    openSmsApp(arrContacts.get(currentPosition).getNumber());
                }
            }
        });

        // Row click listener - Open contact detail
        holder.llrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    openContactDetail(arrContacts.get(currentPosition));
                }
            }
        });

        // Long press listener - Open system contact
        holder.llrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    openSystemContact(arrContacts.get(currentPosition));
                }
                return true;
            }
        });
    }

    private void makeDirectCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(callIntent);
        } else {
            // Request permission or open dialer
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            }
            // Fallback to dialer
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(dialIntent);
        }
    }

    private void openSmsApp(String phoneNumber) {
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
        context.startActivity(smsIntent);
    }

    private void openContactDetail(ContactModel contact) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra("contact_id", contact.getId());
        intent.putExtra("contact_lookup_key", contact.getLookupKey());
        intent.putExtra("contact_name", contact.getName());
        intent.putExtra("contact_number", contact.getNumber());
        intent.putExtra("contact_email", contact.getEmail());
        intent.putExtra("contact_starred", contact.isStarred());
        if (contact.getPhotoUri() != null) {
            intent.putExtra("contact_photo", contact.getPhotoUri().toString());
        }
        context.startActivity(intent);
    }

    private void openSystemContact(ContactModel contact) {
        // Open system contact app to view/edit
        Uri contactUri = contact.getContactUri();
        if (contactUri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(contactUri);
            context.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return arrContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContact, imgFavorite;
        TextView txtNumber;
        TextView txtName;
        LinearLayout llrow;
        ImageButton btnCall, btnMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgContact = itemView.findViewById(R.id.imgContact);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            txtName = itemView.findViewById(R.id.txtName);
            txtNumber = itemView.findViewById(R.id.txtNumber);
            llrow = itemView.findViewById(R.id.llRow);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(slideIn);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
