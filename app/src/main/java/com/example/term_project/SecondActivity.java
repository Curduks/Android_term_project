package com.example.term_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

public class SecondActivity extends AppCompatActivity{

    private long backKeyPressedTime = 0;

    private final int Friend_Fragment = 1;
    private final int Map_Fragment = 2;
    private final int Map_Room_Fragment = 3;
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

        Button btn_friend = (Button) findViewById(R.id.btn_friend);
        Button btn_map = (Button) findViewById(R.id.btn_map);
        Button btn_room = (Button) findViewById(R.id.btn_room);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

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
                // 두번 째 프래그먼트 호출
                MapFragment mapFragment = new MapFragment();
                transaction.replace(R.id.fragment_container, mapFragment);
                transaction.commit();

                mapFragment.setArguments(bundle);
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


}