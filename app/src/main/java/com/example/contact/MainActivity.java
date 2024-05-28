package com.example.contact;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<ContactModel>arrContacts=new ArrayList<>();
    FloatingActionButton btnOpenDialog;
    RecyclerContactAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView=findViewById(R.id.contact);
        btnOpenDialog=findViewById(R.id.btnOpenDialog);
        btnOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog=new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_update);
                EditText edtName;
                EditText edtNumber;
                Button btnAdd;
                edtName=dialog.findViewById(R.id.edtName);
                edtNumber=dialog.findViewById(R.id.edtNumber);
                btnAdd=dialog.findViewById(R.id.btnAdd);
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name="",number="";
                        if(!edtName.getText().toString().equals("")){
                            name=edtName.getText().toString();

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Enter Valid Name", Toast.LENGTH_SHORT).show();
                        }
                        if(!edtNumber.getText().toString().equals("")){
                            number=edtNumber.getText().toString();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Enter Valid Number", Toast.LENGTH_SHORT).show();
                        }
                        arrContacts.add(new ContactModel(R.drawable.boss,name,number));
                        adapter.notifyItemInserted(arrContacts.size()-1);
                        recyclerView.scrollToPosition(arrContacts.size()-1);
                        dialog.dismiss();
                    }
                });
                dialog.show();


            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        arrContacts.add(new ContactModel(R.drawable.boss,"sameer","9191813897"));
        arrContacts.add(new ContactModel(R.drawable.businesswoman,"Dev","9182819797"));
        arrContacts.add(new ContactModel(R.drawable.model,"sameer","7896548596"));
        arrContacts.add(new ContactModel(R.drawable.groom,"Abhisek","7845693258"));
        arrContacts.add(new ContactModel(R.drawable.man,"Sanjay","7895487596"));
        arrContacts.add(new ContactModel(R.drawable.profile,"Aditya","7485125896"));
        arrContacts.add(new ContactModel(R.drawable.woman,"Meet","7412369589"));
        arrContacts.add(new ContactModel(R.drawable.model,"sanket","7852964152"));
        arrContacts.add(new ContactModel(R.drawable.man,"Punit","7414785874"));
        arrContacts.add(new ContactModel(R.drawable.boss,"Neel","7898789874"));
        arrContacts.add(new ContactModel(R.drawable.model,"Narendra","1425365236"));
        arrContacts.add(new ContactModel(R.drawable.profile,"Deepak","1456985698"));
        arrContacts.add(new ContactModel(R.drawable.groom,"Ankit","7485969685"));
        arrContacts.add(new ContactModel(R.drawable.man,"Raja","7485968574"));
        arrContacts.add(new ContactModel(R.drawable.boss,"sunil","7485968574"));
        arrContacts.add(new ContactModel(R.drawable.profile,"Prashant","7412526363"));



        adapter=new RecyclerContactAdapter(this,arrContacts);
        recyclerView.setAdapter(adapter);


    }
}