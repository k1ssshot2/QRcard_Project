package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private List<Friend> friends = new ArrayList<>();
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 예시 데이터 추가 (나중에 QR 스캔으로 받은 친구 데이터로 대체)
        friends.add(new Friend("홍길동", "hong@email.com", "010-1234-5678", "컴공", "학생", "kakao_id", "insta_id"));

        adapter = new FriendsAdapter(friends, this::onFriendClick);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // 친구 클릭 시 상세 정보 액티비티로 이동
    private void onFriendClick(Friend friend) {
        Intent intent = new Intent(getContext(), FriendDetailActivity.class);
        intent.putExtra("friend", friend); // Friend는 Serializable 또는 Parcelable 구현 필요
        startActivity(intent);
    }
}
