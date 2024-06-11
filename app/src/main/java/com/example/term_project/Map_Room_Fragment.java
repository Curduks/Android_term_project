package com.example.term_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Map_Room_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Map_Room_Fragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DatabaseReference mDatabase;
    String userID;
    Firebase firebase;
    List<String> roomList_inv;

    ListView invited_room;
    public Map_Room_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Map_Room_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Map_Room_Fragment newInstance(String param1, String param2) {
        Map_Room_Fragment fragment = new Map_Room_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map__room_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btn_room_create = (Button) view.findViewById(R.id.room_create);
        Button btn_room_invite = (Button) view.findViewById(R.id.room_invite);
        Button btn_room_exit = (Button) view.findViewById(R.id.room_exit);
        TextView room_Status = (TextView) view.findViewById(R.id.room_status);
        invited_room = (ListView) view.findViewById(R.id.invited_room);
        ListView room_users = (ListView) view.findViewById(R.id.room_users_list);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebase = new Firebase(mDatabase);
        Bundle bundle = getArguments();
        userID = bundle.getString("userID");

        firebase.checkIfUserRoomExists(userID, new Firebase.checkIfUserRoomCallback() {
            @Override
            public void oncheckIfUserRoomResult(boolean success) {
                if(success){
                    btn_room_create.setEnabled(false);
                    btn_room_invite.setEnabled(true);
                    invited_room.setEnabled(false);
                    btn_room_exit.setEnabled(true);
                    room_Status.setText("방에 속해 있습니다.");
                }else{
                    btn_room_create.setEnabled(true);
                    btn_room_invite.setEnabled(false);
                    invited_room.setEnabled(true);
                    btn_room_exit.setEnabled(false);
                    room_Status.setText("방을 만들어주세요.");
                }
            }
        });

        btn_room_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase.RoomExit(userID);
                ArrayAdapter<String> adapter;
                List<String> userList = new ArrayList();
                userList.clear();
                adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, userList);
                room_users.setAdapter(adapter);
            }
        });

        btn_room_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase.createRoom(userID);
            }
        });

        btn_room_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Map_friend_invite_list.class);
                intent.putExtra("userID",userID);
                startActivity(intent);
            }
        });

        firebase.getRoomNotifications(userID, new Firebase.RoomNotificationsCallback() {
            @Override
            public void onCallback(List<String> roomList) {
                if(getActivity() != null){
                    ArrayAdapter<String> adapter;
                    roomList_inv = roomList;
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, roomList);
                    invited_room.setAdapter(adapter);
                }
            }
        });

        invited_room.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                alertdialog(roomList_inv.get(i));
            }
        });

        firebase.getRoomUsers(userID, new Firebase.RoomUsersCallback() {
            @Override
            public void onCallback(List<String> userList) {
                if(getActivity() != null){
                    ArrayAdapter<String> adapter;
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, userList);
                    room_users.setAdapter(adapter);
                }
            }
        });

    }

    private void alertdialog(String roomId){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("초대 수락").setMessage(roomId+" 방에 입장하시겠습니까?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getContext(), roomId+" 방에 입장하였습니다.", Toast.LENGTH_SHORT).show();
                firebase.acceptRoomRequest(userID,roomId);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                firebase.declineRoomRequest(userID,roomId);
            }
        });

        firebase.getRoomNotifications(userID, new Firebase.RoomNotificationsCallback() {
            @Override
            public void onCallback(List<String> roomList) {
                if(getActivity() != null){
                    ArrayAdapter<String> adapter;
                    roomList_inv = roomList;
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, roomList);
                    invited_room.setAdapter(adapter);
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}