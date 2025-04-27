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

        // 초기 화면 설정
        setFragment(TAG_HOME, new HomeFragment());
        binding.navigationView.setSelectedItemId(R.id.homeFragmentGo);

        // 네비게이션 선택 리스너
        binding.navigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.contactsFragmentGo) {
                setFragment(TAG_CONTACTS, new ContactsFragment());
            } else if (itemId == R.id.homeFragmentGo) {
                setFragment(TAG_HOME, new HomeFragment());
            } else if (itemId == R.id.myinfoFragmentGo) {
                setFragment(TAG_MYINFO, new MyInfoFragment());
            }

            return true;
        });

    }

    protected void setFragment(String tag, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(tag) == null) {
            fragmentTransaction.add(R.id.mainFrameLayout, fragment, tag);
        }

        Fragment contacts = fragmentManager.findFragmentByTag(TAG_CONTACTS);
        Fragment home = fragmentManager.findFragmentByTag(TAG_HOME);
        Fragment myinfo = fragmentManager.findFragmentByTag(TAG_MYINFO);

        if (contacts != null) fragmentTransaction.hide(contacts);
        if (home != null) fragmentTransaction.hide(home);
        if (myinfo != null) fragmentTransaction.hide(myinfo);

        Fragment showFragment = fragmentManager.findFragmentByTag(tag);
        if (showFragment != null) {
            fragmentTransaction.show(showFragment);
        }

        fragmentTransaction.commitAllowingStateLoss();
    }
}
