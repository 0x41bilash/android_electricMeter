package com.example.eb_meter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //remove title bar in login screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_login);

        //login btn initialization
        Button loginBtn = findViewById(R.id.loginBtn);

        //open next activity when the login button is clicked
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });
    }

    //function to open Main Activity
    public void openMainActivity() {
        //gets the name and account number from the login page
        EditText edtTxtName = findViewById(R.id.edtTxtUser);
        EditText edtTxtAcc = findViewById(R.id.edtTxtAcc);
        String accNo = edtTxtAcc.getText().toString();
        String userName = edtTxtName.getText().toString();

        //passes the name and account number to the Main Activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", userName);
        intent.putExtra("accountNumber", accNo);
        startActivity(intent);
    }
}