package com.example.term_project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SecondActivity extends AppCompatActivity{

    private long backKeyPressedTime = 0;

    private final int Friend_Fragment = 1;
    private final int Map_Fragment = 2;
    private final int Map_Room_Fragment = 3;
    DatabaseReference mDatabase;
    Firebase firebase;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebase = new Firebase(mDatabase);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }



        Button btn_friend = (Button) findViewById(R.id.btn_friend);
        Button btn_map = (Button) findViewById(R.id.btn_map);
        Button btn_room = (Button) findViewById(R.id.btn_room);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        Intent serviceIntent = new Intent(this, GPS_Service.class);

        serviceIntent.putExtra("userID",userID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
            //startForegroundService(serviceIntent);
        }



        FragmentView(Friend_Fragment);

        btn_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentView(Friend_Fragment);
            }
        });

        btn_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentView(Map_Room_Fragment);
            }
        });

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentView(Map_Fragment);
            }
        });

    }

    private void FragmentView(int fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("userID",userID);
        switch (fragment){
            case 1:
                // 첫번 째 프래그먼트 호출
                FriendFragment friendFragment = new FriendFragment();
                transaction.replace(R.id.fragment_container, friendFragment);
                transaction.commit();

                friendFragment.setArguments(bundle);
                break;

            case 2:
                Intent intent2 = new Intent(SecondActivity.this, naverMap_Activity.class);
                intent2.putExtra("userID",userID);
                startActivity(intent2);
                // 두번 째 프래그먼트 호출
//                NaverMapFragment naverMapFragment = new NaverMapFragment();
//                transaction.replace(R.id.fragment_container, naverMapFragment);
//                transaction.commit();
//
//                naverMapFragment.setArguments(bundle);
                break;

            case 3:
                Map_Room_Fragment mapRoomFragment = new Map_Room_Fragment();
                transaction.replace(R.id.fragment_container, mapRoomFragment);
                transaction.commit();

                mapRoomFragment.setArguments(bundle);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this,"\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent serviceIntent = new Intent(this, GPS_Service.class);
        stopService(serviceIntent);
    }
}