package com.example.term_project;

import android.app.LauncherActivity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class FriendRequestAdapter extends BaseAdapter {
    private Context context;
    private List<FriendRequest> friendRequestList;
    private Firebase friendRequestHandler;
    private String currentUserId;

    public FriendRequestAdapter(Context context, List<FriendRequest> friendRequestList, Firebase friendRequestHandler , String currentUserId) {
        this.context = context;
        this.friendRequestList = friendRequestList;
        this.friendRequestHandler = friendRequestHandler;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getCount() {
        return friendRequestList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendRequestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_request_item, parent, false);
        }

        FriendRequest friendRequest = friendRequestList.get(position);

        TextView requesterNameTextView = convertView.findViewById(R.id.requesterNameTextView);
        Button acceptButton = convertView.findViewById(R.id.acceptButton);

        requesterNameTextView.setText(friendRequest.getRequesterId());
        Log.d("test", "Setting requester name: "+ friendRequest.getRequesterId());
        acceptButton.setOnClickListener(v -> {
            friendRequestHandler.acceptFriendRequest(friendRequest.getRequesterId(), currentUserId, success -> {
                if (success) {
                    friendRequestList.remove(position);
                    Toast.makeText(context,"친구요청을 수락하셨습니다.",Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }else{
                    Toast.makeText(context,"존재하지 않는 사용자입니다.",Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }
}