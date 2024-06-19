package com.example.term_project;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class naverMap_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    Firebase firebase = new Firebase(mDatabase);
    String userID;
    List<Marker> markerList = new ArrayList<>();

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_naver_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");



        //지도 객체 생성하기
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음.
        mapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        firebase.getRoomUsers_loc(userID, new Firebase.RoomUsersLocCallback() {
            @Override
            public void onCallback(Map<String, String> userList) {
                int i = 0;
                for(String key: userList.keySet()){
                    markerList.add(new Marker());
                    String location = userList.get(key);
                    double lat = Double.parseDouble(location.substring(0, location.indexOf(",")));
                    double lng = Double.parseDouble(location.substring(location.indexOf(",")+1));
                    setMark(markerList.get(i++),key,lat,lng);
                }
            }
        });
    }

    private void setMark(Marker marker,String name ,double lat, double lng){
        marker.setIconPerspectiveEnabled(true);
        marker.setPosition(new LatLng(lat, lng));
        marker.setCaptionText(name);
        marker.setZIndex(10);
        marker.setMap(mNaverMap);
    }

    private void setMarkPos(Marker marker, double lat, double lng){
        marker.setPosition(new LatLng(lat, lng));
        marker.setMap(mNaverMap);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {


        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);
        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        firebase.getRoomUsers_loc(userID, new Firebase.RoomUsersLocCallback() {
            @Override
            public void onCallback(Map<String, String> userList) {
                int i = 0;
                for(String key: userList.keySet()){
                    String location = userList.get(key);
                    double lat = Double.parseDouble(location.substring(0, location.indexOf(",")));
                    double lng = Double.parseDouble(location.substring(location.indexOf(",")+1));
                    setMarkPos(markerList.get(i++),lat,lng);
                }
            }
        });

//        Marker marker = new Marker();
//        marker.setPosition(new LatLng(locationSource.getLastLocation().getLatitude(),locationSource.getLastLocation().getLongitude()));
//        marker.setMap(mNaverMap);

//        CameraPosition cameraPosition = new CameraPosition(
//                new LatLng(33.38,126.55),9
//        );
//
//        mNaverMap.setCameraPosition(cameraPosition);

        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }
}