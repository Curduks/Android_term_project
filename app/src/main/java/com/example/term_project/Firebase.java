package com.example.term_project;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Firebase {
    DatabaseReference mDatabase;

    public Firebase(DatabaseReference mDatabase){
        this.mDatabase = mDatabase;
    }

    // 사용자의 이름과 비밀번호를 확인하는 메소드
    public void authenticateUser(final String name, final String password, final AuthenticationCallback callback) {
        Query query = mDatabase.child("users");
        // 데이터베이스에서 모든 사용자 검색
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean authenticated = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User_data userData = snapshot.getValue(User_data.class);
                    String dbName = userData.getName();
                    String dbPassword = userData.getPassword();

                    if (dbName != null && dbName.equals(name) && dbPassword != null && dbPassword.equals(password)) {
                        authenticated = true;
                        break;
                    }
                }
                callback.onAuthenticationResult(authenticated);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onAuthenticationResult(false);
            }
        });
    }

    // 콜백 인터페이스
    public interface AuthenticationCallback {
        void onAuthenticationResult(boolean isAuthenticated);
    }

    // 사용자의 이름이 존재하는지 확인하는 메소드
    public void checkIfUserExists(final String name, final UserExistenceCallback callback) {
        Query query = mDatabase.child("users");
        // 데이터베이스에서 모든 사용자 검색
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userExists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dbName = snapshot.child("name").getValue(String.class);

                    if (dbName != null && dbName.equals(name)) {
                        userExists = true;
                        break;
                    }
                }
                callback.onUserExistenceResult(userExists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onUserExistenceResult(false);
            }
        });
    }

    // 콜백 인터페이스
    public interface UserExistenceCallback {
        void onUserExistenceResult(boolean exists);
    }

    // 친구 요청 보내기 메소드
    public void sendFriendRequest(String fromUserId, String toUserId, final RequestCallback callback) {
        DatabaseReference fromUserFriendsRef = mDatabase.child("users").child(fromUserId).child("friends").child(toUserId);
        fromUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() || fromUserId.equalsIgnoreCase(toUserId)) {
                    // 이미 친구인 상태
                    callback.onRequestResult(false);
                } else {
                    // 친구가 아닌 상태, 요청 보내기
                    DatabaseReference requestRef = mDatabase.child("users").child(toUserId).child("friendRequests").child(fromUserId);
                    requestRef.setValue(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onRequestResult(true);
                        } else {
                            callback.onRequestResult(false);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onRequestResult(false);
            }
        });
    }

    // 친구 요청 수락 메소드
    public void acceptFriendRequest(String fromUserId, String toUserId, final RequestCallback callback) {
        DatabaseReference fromUserRef = mDatabase.child("users").child(fromUserId).child("friends").child(toUserId);
        DatabaseReference toUserRef = mDatabase.child("users").child(toUserId).child("friends").child(fromUserId);

        fromUserRef.setValue(true).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                toUserRef.setValue(true).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        // 요청 삭제
                        mDatabase.child("users").child(toUserId).child("friendRequests").child(fromUserId).removeValue();
                        callback.onRequestResult(true);
                    } else {
                        callback.onRequestResult(false);
                    }
                });
            } else {
                callback.onRequestResult(false);
            }
        });
    }

    // 콜백 인터페이스
    public interface RequestCallback {
        void onRequestResult(boolean success);
    }

    //-------------방 관련 -------------------//
    public void createRoom(String UserID){
        DatabaseReference roomsRef = mDatabase.child("rooms");

        String roomName = "Room " + UserID;
        Room newRoom = new Room(roomName, roomName);
        roomsRef.child(roomName).setValue(newRoom).addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               addUserToRoom(UserID);
           }
        });
    }

    private void addUserToRoom(String currentUserId) {
        String roomId = "Room " + currentUserId;
        DatabaseReference participantsRef = mDatabase.child("rooms").child(roomId).child("participants");
        participantsRef.child(currentUserId).setValue(true);

        DatabaseReference userRoomsRef = mDatabase.child("users").child(currentUserId).child("joinedRooms");
        userRoomsRef.child(roomId).setValue(true);
    }

    public void InviteFriend(String currentUserId, String friendId){
        String roomId = "Room " + currentUserId;
        DatabaseReference room_Ref = mDatabase.child("rooms").child(roomId).child("invites");
        room_Ref.child(friendId).setValue("pending");

        DatabaseReference target_Ref = mDatabase.child("users").child(friendId).child("room_notifications");
        target_Ref.child(roomId).setValue("pending");

    }

    public void checkIfUserRoomExists(String name,final checkIfUserRoomCallback callback) {
        DatabaseReference check_Ref = mDatabase.child("users").child(name).child("joinedRooms");

        check_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    callback.oncheckIfUserRoomResult(true);
                }else{
                    callback.oncheckIfUserRoomResult(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // 콜백 인터페이스
    public interface checkIfUserRoomCallback {
        void oncheckIfUserRoomResult(boolean success);
    }

    public void listRoomRequest(String userId){

    }


    // 방 초대 요청 수락 메소드
    public void acceptRoomRequest(String userId, String roomId) {
        DatabaseReference UserRef = mDatabase.child("users").child(userId).child("room_notifications");
        DatabaseReference UserRoomRef = mDatabase.child("users").child(userId).child("joinedRooms");
        DatabaseReference roomRef = mDatabase.child("rooms").child(roomId).child("participants");

        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equalsIgnoreCase(roomId)){
                        snapshot.getRef().removeValue();
                        roomRef.child(userId).setValue(true);
                        UserRoomRef.child(roomId).setValue(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 방 초대 거절 수락 메소드
    public void declineRoomRequest(String userId, String roomId) {
        DatabaseReference UserRef = mDatabase.child("users").child(userId).child("room_notifications");
        DatabaseReference roomRef = mDatabase.child("rooms").child(roomId).child("participants");

        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equalsIgnoreCase(roomId)){
                        snapshot.getRef().removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 방 나가기 메소드
    public void RoomExit(String userId) {
        DatabaseReference UserRef = mDatabase.child("users").child(userId).child("joinedRooms");

        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String roomId = snapshot.getKey();
                    if(roomId.equalsIgnoreCase("Room "+ userId)){ // 방장이면
                        DatabaseReference roomRef = mDatabase.child("rooms").child(roomId).child("participants");
                        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                for(DataSnapshot snapshot1 : dataSnapshot1.getChildren()){
                                    DatabaseReference allRef = mDatabase.child("users").child(snapshot1.getKey()).child("joinedRooms");
                                    allRef.removeValue();
                                }
                                DatabaseReference allRef2 = mDatabase.child("rooms").child(roomId);
                                allRef2.removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else{
                        UserRef.removeValue();
                        DatabaseReference roomRef = mDatabase.child("rooms").child(roomId).child("participants");
                        roomRef.child(userId).removeValue();
                    }
                    //roomRef.child(userId).removeValue();
                    //UserRef.child(roomId).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getRoomNotifications(String userId, final RoomNotificationsCallback callback) {
        // 특정 유저의 room_notifications 경로 참조
        DatabaseReference notificationsRef = mDatabase.child("users").child(userId).child("room_notifications");

        // 해당 경로에서 방 목록을 가져옴
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<String> roomList = new ArrayList<>();

                    // 모든 자식 노드를 탐색하여 방 이름을 리스트에 추가
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String room = snapshot.getKey();
                        if (room != null) {
                            roomList.add(room);
                        }
                    }

                    // 콜백을 통해 방 목록 반환
                    callback.onCallback(roomList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Firebase", "loadRoomNotifications:onCancelled", databaseError.toException());
            }
        });
    }

    // 방 목록을 반환하기 위한 콜백 인터페이스
    public interface RoomNotificationsCallback {
        void onCallback(List<String> roomList);
    }

    public void getRoomUsers(String userId, final RoomUsersCallback callback) {
        // 특정 유저의 room_notifications 경로 참조
        DatabaseReference joinedRoomRef = mDatabase.child("users").child(userId).child("joinedRooms");

        // 해당 경로에서 방 목록을 가져옴
        joinedRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //List<String> userList = new ArrayList<>();

                // 모든 자식 노드를 탐색하여 방 이름을 리스트에 추가
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String room = snapshot.getKey();
                    if (room != null) {
                        DatabaseReference roomRef = mDatabase.child("rooms").child(room).child("participants");
                        roomRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                if(dataSnapshot1.exists()){
                                    List<String> userList = new ArrayList<>();
                                    for(DataSnapshot snapshot1 : dataSnapshot1.getChildren()){
                                        String users = snapshot1.getKey();
                                        userList.add(users);
                                    }
                                    callback.onCallback(userList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                // 콜백을 통해 방 목록 반환
                //callback.onCallback(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Firebase", "loadRoomNotifications:onCancelled", databaseError.toException());
            }
        });
    }

    public interface RoomUsersCallback {
        void onCallback(List<String> userList);
    }

    public void add_user_location(String userId, Location location){
        DatabaseReference joinedRoomRef = mDatabase.child("users").child(userId).child("joinedRooms");

        joinedRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //List<String> userList = new ArrayList<>();

                // 모든 자식 노드를 탐색하여 방 이름을 리스트에 추가
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String room = snapshot.getKey();
                    if (room != null) {
                        DatabaseReference roomRef = mDatabase.child("rooms").child(room).child("location");
                        String userlocation = location.getLatitude() + "," + location.getLongitude();
                        roomRef.child(userId).setValue(userlocation);
                    }
                }
                // 콜백을 통해 방 목록 반환
                //callback.onCallback(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getRoomUsers_loc(String userId, final RoomUsersLocCallback callback) {
        // 특정 유저의 room_notifications 경로 참조
        DatabaseReference joinedRoomRef = mDatabase.child("users").child(userId).child("joinedRooms");

        // 해당 경로에서 방 목록을 가져옴
        joinedRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //List<String> userList = new ArrayList<>();

                // 모든 자식 노드를 탐색하여 방 이름을 리스트에 추가
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String room = snapshot.getKey();
                    if (room != null) {
                        DatabaseReference roomRef = mDatabase.child("rooms").child(room).child("location");
                        roomRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                if(dataSnapshot1.exists()){
                                    HashMap<String, String> userList = new HashMap<>();
                                    for(DataSnapshot snapshot1 : dataSnapshot1.getChildren()){
                                        String users_name = snapshot1.getKey();
                                        if(!users_name.equalsIgnoreCase(userId)) {
                                            String users_loc = snapshot1.getValue(String.class);
                                            userList.put(users_name, users_loc);
                                        }
                                    }
                                    callback.onCallback(userList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                // 콜백을 통해 방 목록 반환
                //callback.onCallback(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Firebase", "loadRoomNotifications:onCancelled", databaseError.toException());
            }
        });
    }

    public interface RoomUsersLocCallback {
        void onCallback(Map<String,String> userList);
    }



}
