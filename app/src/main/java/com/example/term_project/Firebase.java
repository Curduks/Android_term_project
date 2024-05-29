package com.example.term_project;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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


}
