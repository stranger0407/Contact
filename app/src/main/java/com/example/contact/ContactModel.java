package com.example.contact;

public class ContactModel {
    int img;
    String name;
    String number;
    public ContactModel(int img,String name,String number){
        this.img=img;
        this.number=number;
        this.name=name;
    }
    public ContactModel(String name,String number){
        this.name=name;
        this.number=number;
    }
}
