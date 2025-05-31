package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class FriendProfileActivity extends AppCompatActivity {

    private static final String TAG = "FriendProfileActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile); // activity_friend_profile.xml 내에 FrameLayout이 있어야 함 (id: fragment_container)

        // Intent에서 Friend 객체 가져오기
        Intent intent = getIntent();
        Friend friend = (Friend) intent.getSerializableExtra("friend");

        // Friend 객체가 null인 경우, 에러 로그 출력하고 종료
        if (friend == null) {
            Log.e(TAG, "Friend is null. Cannot load profile.");
            Toast.makeText(this, "친구 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();  // 안전하게 종료
            return;
        }

        // Friend 객체가 null이 아니면 정보 로깅
        Log.d(TAG, "Friend loaded: " + friend.getName());  // Friend 객체의 이름을 로그에 출력

        // savedInstanceState가 null일 경우에만 Fragment를 새로 생성하여 추가
        if (savedInstanceState == null) {
            // Friend 객체를 인자로 하는 FriendProfileFragment 인스턴스 생성
            FriendProfileFragment fragment = FriendProfileFragment.newInstance(friend);

            // FragmentTransaction을 사용하여 FriendProfileFragment를 화면에 추가
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);  // FrameLayout에 FriendProfileFragment 교체
            transaction.commit();
        }
    }
}




