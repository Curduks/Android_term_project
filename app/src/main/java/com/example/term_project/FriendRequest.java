package com.example.term_project;

public class FriendRequest {
    private String requesterId;


    public FriendRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
    }

    public FriendRequest(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

}