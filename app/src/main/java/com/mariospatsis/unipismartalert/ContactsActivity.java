package com.mariospatsis.unipismartalert;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Set;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText contact1;
    private EditText contact2;
    private EditText contact3;
    private Button updateBtn;
    String[] contacts = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contact1 = findViewById(R.id.c_input1);
        contact2 = findViewById(R.id.c_input2);
        contact3 = findViewById(R.id.c_input3);

        updateBtn = findViewById(R.id.c_update);
        updateBtn.setOnClickListener(this);
        getContacts();
        if(contacts.length >= 1) contact1.setText(contacts[0]);
        if(contacts.length >= 2) contact2.setText(contacts[1]);
        if(contacts.length >= 3) contact3.setText(contacts[2]);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.c_update:
                Set<String> contacts = new HashSet<String>();

                //Add the numbers to send the text message
                if(!contact1.getText().toString().equals("")) contacts.add(contact1.getText().toString());
                if(!contact2.getText().toString().equals("")) contacts.add(contact2.getText().toString());
                if(!contact3.getText().toString().equals("")) contacts.add(contact3.getText().toString());

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putStringSet("ContactNumbers", contacts);
                editor.apply();
                break;
        }
    }

    private void getContacts(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Set<String> contacts = sharedPref.getStringSet("ContactNumbers",null);
        if(contacts != null && !contacts.isEmpty() )this.contacts = contacts.toArray(new String[contacts.size()]);

    }

}
