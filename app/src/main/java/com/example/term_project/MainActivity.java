package com.example.term_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    Firebase firebase;
    EditText edt_name;
    EditText edt_pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebase = new Firebase(mDatabase);
        Button btn_register = (Button) findViewById(R.id.btn_register);
        Button btn_login = (Button) findViewById(R.id.btn_login);
        edt_name = (EditText) findViewById(R.id.edt_name);
        edt_pass = (EditText) findViewById(R.id.edt_pass);

        edt_pass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    edt_pass.setText("");
                }
                return false;
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edt_name.getText().toString();
                String pass = edt_pass.getText().toString();
                String key = name;
                firebase.authenticateUser(name, pass, new Firebase.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationResult(boolean isAuthenticated) {
                        if (isAuthenticated){
                            Toast.makeText(getApplicationContext(),"이미 존재하는 아이디 입니다.",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"아이디가 등록되었습니다.",Toast.LENGTH_SHORT).show();
                            User_data userData = new User_data(name,pass);
                            mDatabase.child("users").child(key).setValue(userData);
                        }
                    }
                });
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase.authenticateUser(edt_name.getText().toString(), edt_pass.getText().toString(), new Firebase.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationResult(boolean isAuthenticated) {
                        if (isAuthenticated){
                            Intent intent2 = new Intent(MainActivity.this, SecondActivity.class);
                            intent2.putExtra("userID",edt_name.getText().toString());
                            startActivity(intent2);
                        }else{
                            Toast.makeText(getApplicationContext(),"존재하지 않거나 틀린 패스워드 입니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

}