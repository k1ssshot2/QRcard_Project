package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsFragment extends Fragment {

    private RecyclerView favoriteRecyclerView;
    private RecyclerView contactRecyclerView;
    private FriendsAdapter favoriteAdapter;
    private FriendsAdapter contactAdapter;
    private List<Friend> favoriteList;
    private List<Friend> contactList;
    private FirebaseFirestore db;
    private static final int REQUEST_VIEW_FRIEND = 1001;

    public ContactsFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        loadFriendsFromFirestore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteList = new ArrayList<>();
        favoriteAdapter = new FriendsAdapter(new ArrayList<>(), new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(Friend friend) {
                onFriendClickImpl(friend);
            }

            @Override
            public void onFavoriteClick(Friend friend) {

            }

            @Override
            public void onFavoriteClick(Friend friend, boolean isNowFavorite) {
                updateFavoriteInFirestore(friend, isNowFavorite);
            }
        });
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactList = new ArrayList<>();
        contactAdapter = new FriendsAdapter(new ArrayList<>(), new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(Friend friend) {
                onFriendClickImpl(friend);
            }

            @Override
            public void onFavoriteClick(Friend friend) {

            }

            @Override
            public void onFavoriteClick(Friend friend, boolean isNowFavorite) {
                updateFavoriteInFirestore(friend, isNowFavorite);
            }
        });
        contactRecyclerView.setAdapter(contactAdapter);

        db = FirebaseFirestore.getInstance();
        loadFriendsFromFirestore();

        return view;
    }

    private void onFriendClickImpl(Friend friend) {
        // 친구 객체를 Intent로 전달
        Intent intent = new Intent(getContext(), FriendProfileActivity.class);
        intent.putExtra("friend", friend);  // Friend 객체를 Intent에 추가
        startActivityForResult(intent, REQUEST_VIEW_FRIEND);
    }


    private void loadFriendsFromFirestore() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ContactsFragment", "현재 사용자 UID: " + currentUserId);

        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteList.clear();
                        contactList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Friend friend = doc.toObject(Friend.class);
                            friend.setId(doc.getId());

                            if (friend.isFavorite()) {
                                favoriteList.add(friend);
                            } else {
                                contactList.add(friend);
                            }
                        }

                        favoriteAdapter.setData(groupFriendsWithHeaders(favoriteList));
                        contactAdapter.setData(groupFriendsWithHeaders(contactList));
                    } else {
                        Log.w("ContactsFragment", "Firestore 불러오기 실패", task.getException());
                    }
                });
    }

    private void updateFavoriteInFirestore(Friend friend, boolean isNowFavorite) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(friend.getId())  // 친구 ID로 해당 친구의 문서 참조
                .update("favorite", isNowFavorite)  // 'favorite' 필드 업데이트
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firestore 업데이트가 성공했을 때
                        Log.d("ContactsFragment", "즐겨찾기 상태가 성공적으로 업데이트되었습니다.");
                        // UI 갱신을 위한 코드 추가 (옵션)
                    } else {
                        // Firestore 업데이트 실패 시
                        Log.w("ContactsFragment", "즐겨찾기 상태 업데이트 실패", task.getException());
                    }
                });
    }




    private List<FriendListItem> groupFriendsWithHeaders(List<Friend> friends) {
        List<FriendListItem> groupedList = new ArrayList<>();
        Collections.sort(friends, Comparator.comparing(
                f -> f.getName() != null ? f.getName().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER));

        String lastHeader = "";
        for (Friend friend : friends) {
            String name = friend.getName();
            String header;

            if (name != null && !name.isEmpty()) {
                header = name.substring(0, 1).toUpperCase();
            } else {
                header = "?";
            }

            if (!header.equals(lastHeader)) {
                groupedList.add(new SectionHeader(header));
                lastHeader = header;
            }

            groupedList.add(friend);
        }

        return groupedList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIEW_FRIEND && resultCode == getActivity().RESULT_OK && data != null) {
            String deletedEmail = data.getStringExtra("friendEmail");
            if (deletedEmail != null) {
                removeFriendByEmail(deletedEmail);
            }
        }
    }

    private void removeFriendByEmail(String email) {
        boolean removed = false;

        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getEmail().equals(email)) {
                contactList.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            for (int i = 0; i < favoriteList.size(); i++) {
                if (favoriteList.get(i).getEmail().equals(email)) {
                    favoriteList.remove(i);
                    break;
                }
            }
        }

        favoriteAdapter.setData(groupFriendsWithHeaders(favoriteList));
        contactAdapter.setData(groupFriendsWithHeaders(contactList));
    }
}

