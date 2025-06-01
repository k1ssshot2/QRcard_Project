package com.example.qrcardproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.qrcardproject.databinding.ActivityNaviBinding;

public class NaviActivity extends AppCompatActivity {

    private static final String TAG_CONTACTS = "ContactsFragment";
    private static final String TAG_HOME = "HomeFragment";
    private static final String TAG_MYINFO = "MyInfoFragment";

    private ActivityNaviBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNaviBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 초기 화면 설정 (홈 화면)
        setFragment(TAG_HOME, new HomeFragment());
        binding.navigationView.setSelectedItemId(R.id.homeFragmentGo);

        // 하단 네비게이션 아이템 선택 리스너
        binding.navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.contactsFragmentGo) {
                // 친구 탭 누르면 항상 새 친구 목록 화면 생성
                setFragment(TAG_CONTACTS, new ContactsFragment());
                return true;
            } else if (itemId == R.id.homeFragmentGo) {
                setFragment(TAG_HOME, new HomeFragment());
                return true;
            } else if (itemId == R.id.myinfoFragmentGo) {
                setFragment(TAG_MYINFO, new MyInfoFragment());
                return true;
            }

            return false;
        });
    }

    /**
     * 프래그먼트를 지정된 컨테이너에 교체하는 메서드
     * 기존에 붙어있는 모든 프래그먼트를 제거하고 새 프래그먼트로 교체
     */
    protected void setFragment(String tag, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 기존 프래그먼트 모두 제거
        for (Fragment f : fragmentManager.getFragments()) {
            transaction.remove(f);
        }

        // 새 프래그먼트로 교체
        transaction.replace(R.id.mainFrameLayout, fragment, tag);
        transaction.commitAllowingStateLoss();
    }
}

