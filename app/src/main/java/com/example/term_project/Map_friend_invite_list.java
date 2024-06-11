package com.example.term_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Map_friend_invite_list extends AppCompatActivity {

    Firebase firebase;
    DatabaseReference mDatabase;
    String userID;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_friend_invite_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv = (ListView) findViewById(R.id.friend_invite_list);

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebase = new Firebase(mDatabase);

        DatabaseReference friendList_ref = FirebaseDatabase.getInstance().getReference("users/"+userID+"/friends");
        List<String> friendList = new ArrayList<String>();
        friendList_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendUserId = snapshot.getKey();
                    friendList.add(friendUserId);
                    Log.d("test", friendUserId);
                }
                if(getApplication() != null){
                    adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, friendList);
                    lv.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                alertdialog(friendList.get(i));
            }
        });

    }

    private void alertdialog(String friend){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("친구 초대").setMessage(friend+"님을 초대하시겠습니까?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(Map_friend_invite_list.this, friend+"님이 초대되었습니다.", Toast.LENGTH_SHORT).show();
                String roomName = "Room " + userID;
                firebase.InviteFriend(userID,friend);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}