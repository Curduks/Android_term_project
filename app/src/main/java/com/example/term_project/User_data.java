package com.example.term_project;

public class User_data {
    private String name;
    private String password;

    public User_data() { // 새로추가

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setCode(String code) {
        this.password = code;
    }

    public User_data(String name, String code) {
        this.name = name;
        this.password = code;
    }

}
