package com.example.term_project;

public class Room {
    private String roomId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    private String roomName;

    public Room(){

    }

    public Room(String roomId, String roomName){
        this.roomId = roomId;
        this.roomName = roomName;
    }
}
