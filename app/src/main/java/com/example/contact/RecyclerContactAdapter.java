package com.example.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerContactAdapter extends RecyclerView.Adapter<RecyclerContactAdapter.ViewHolder> {
    @NonNull
    Context context;
    ArrayList<ContactModel>arrContacts;
    int lastPosition=-1;

    RecyclerContactAdapter(Context context,ArrayList<ContactModel> arrContacts){
        this.context=context;
        this.arrContacts=arrContacts;
    }
    @Override


    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(context).inflate(R.layout.contact_row,parent,false);
        ViewHolder viewHolder=new ViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         holder.imgContact.setImageResource(arrContacts.get(position).img);
         holder.txtNumber.setText(arrContacts.get(position).number);
         holder.txtName.setText(arrContacts.get(position).name);
         setAnimation(holder.itemView,position);

         holder.llrow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Dialog dialog=new Dialog(context);
                 dialog.setContentView(R.layout.add_update);

                 EditText edtName=dialog.findViewById(R.id.edtName);
                 EditText edtNumber=dialog.findViewById(R.id.edtNumber);
                 Button btnAdd=dialog.findViewById(R.id.btnAdd);
                 TextView txtdetails=dialog.findViewById(R.id.txtdetails);
                 edtNumber.setText((arrContacts.get(position)).number);
                 edtName.setText((arrContacts.get(position)).name);

                 btnAdd.setText("Update");
                 txtdetails.setText("Update Contact");
                 btnAdd.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         String name="",number="";
                         if(!edtName.getText().toString().equals("")){
                             name=edtName.getText().toString();

                         }
                         else{
                             Toast.makeText(context, "Enter Valid Name", Toast.LENGTH_SHORT).show();
                         }
                         if(!edtNumber.getText().toString().equals("")){
                             number=edtNumber.getText().toString();
                         }
                         else{
                             Toast.makeText(context, "Enter Valid Number", Toast.LENGTH_SHORT).show();
                         }
                         arrContacts.set(position,new ContactModel(arrContacts.get(position).img,name,number));
                         notifyItemChanged(position);

                         dialog.dismiss();
                     }
                 });
                 dialog.show();

             }
         });
         holder.llrow.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View view) {
                 AlertDialog.Builder builder=new AlertDialog.Builder(context)
                         .setTitle("Delete Contact")
                         .setMessage("are you sure want to delete")
                         .setIcon(R.drawable.baseline_delete_24)
                         .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 arrContacts.remove(position);
                                 notifyItemRemoved(position);
                             }
                         })
                         .setNegativeButton("No", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {

                             }
                         });
                builder.show();
                 return true;
             }
         });

    }


    @Override
    public int getItemCount() {

        return arrContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContact;
        TextView txtNumber;
        TextView txtName;
        LinearLayout llrow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgContact=itemView.findViewById(R.id.imgContact);
            txtName=itemView.findViewById(R.id.txtName);
            txtNumber=itemView.findViewById(R.id.txtNumber);
            llrow=itemView.findViewById(R.id.llRow);
        }
    }
    private void setAnimation(View viewToAnimate, int position){
        if(position>lastPosition){
            Animation slide_In= AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(slide_In);
            lastPosition=position;
        }
    }
}
