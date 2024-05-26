package com.example.term_project;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public FriendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendFragment newInstance(String param1, String param2) {
        FriendFragment fragment = new FriendFragment();
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
        return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    DatabaseReference mDatabase;
    Firebase firebase;
    ListView friendRequestListView;
    List<FriendRequest> friendRequestList;
    FriendRequestAdapter requestAdapter;
    String userID;
    ArrayAdapter<String> adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView userID_View = view.findViewById(R.id.userID);
        TextView Friend_num_view = view.findViewById(R.id.tv_friendNum);
        ListView lv = view.findViewById(R.id.friend_list);

        EditText edt_user_add = (EditText) view.findViewById(R.id.edt_add_user);
        Button btn_user_add = (Button) view.findViewById(R.id.btn_add_user);

        Bundle bundle = getArguments();
        userID = bundle.getString("userID");
        userID_View.setText(userID);

        List<String> friendList = new ArrayList<String>();


        mDatabase = FirebaseDatabase.getInstance().getReference();
        firebase = new Firebase(mDatabase);

        DatabaseReference friendList_ref = FirebaseDatabase.getInstance().getReference("users/"+userID+"/friends");
        friendList_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendUserId = snapshot.getKey();
                    friendList.add(friendUserId);
                    Log.d("test", friendUserId);
                }
                if(getActivity() != null){
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, friendList);
                    lv.setAdapter(adapter);
                    Friend_num_view.setText("친구 "+ lv.getCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        btn_user_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebase.checkIfUserExists(edt_user_add.getText().toString(), new Firebase.UserExistenceCallback() {
                    @Override
                    public void onUserExistenceResult(boolean exists) {
                        if(exists) {
                            firebase.sendFriendRequest(userID, edt_user_add.getText().toString(), new Firebase.RequestCallback() {
                                @Override
                                public void onRequestResult(boolean success) {
                                    if(success){
                                        Toast.makeText(getContext(), "요청 성공", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getContext(),"이미 친구이거나 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        friendRequestListView = view.findViewById(R.id.friendRequestListView);
        friendRequestList = new ArrayList<>();
        requestAdapter = new FriendRequestAdapter(getContext(),friendRequestList,firebase, userID);
        loadFriendRequests();
    }

    private void loadFriendRequests() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("users/" + userID + "/friendRequests");
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendRequestList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String requesterId = snapshot.getKey();
                    Log.d("asad",requesterId);
                    friendRequestList.add(new FriendRequest(requesterId));
                    requestAdapter.notifyDataSetChanged();
                    friendRequestListView.setAdapter(requestAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

}