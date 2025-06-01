package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
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

    private EditText searchBar;

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
                onFriendClick(friend);
            }

            @Override
            public void onFavoriteToggled(Friend friend) {
                toggleFavorite(friend);
            }
        });
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactList = new ArrayList<>();
        contactAdapter = new FriendsAdapter(new ArrayList<>(), new FriendsAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(Friend friend) {
                ContactsFragment.this.onFriendClick(friend);
            }

            @Override
            public void onFavoriteToggled(Friend friend) {
                toggleFavorite(friend);
            }
        });
        contactRecyclerView.setAdapter(contactAdapter);

        searchBar = view.findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        db = FirebaseFirestore.getInstance();
        loadFriendsFromFirestore();
        listenForFriendUpdates();

        return view;
    }

    private void onFriendClick(Friend friend) {
        FriendProfileFragment profileFragment = new FriendProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("friend", friend);
        profileFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrameLayout, profileFragment)
                .addToBackStack(null)
                .commit();
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

                        applyFilterToAdapters();
                    } else {
                        Log.w("ContactsFragment", "Firestore 불러오기 실패", task.getException());
                    }
                });
    }

    private void applyFilterToAdapters() {
        String query = searchBar.getText().toString();
        filterFriends(query);
    }

    private void filterFriends(String query) {
        String lowerCaseQuery = query.toLowerCase();

        // 검색어가 비어있으면 전체 리스트를 표시
        if (lowerCaseQuery.isEmpty()) {
            favoriteAdapter.setData(groupFriendsWithHeaders(favoriteList));
            contactAdapter.setData(groupFriendsWithHeaders(contactList));
            return;
        }

        List<Friend> filteredFavorite = new ArrayList<>();
        List<Friend> filteredContact = new ArrayList<>();

        for (Friend friend : favoriteList) {
            if (friend.getName() != null && friend.getName().toLowerCase().contains(lowerCaseQuery)) {
                filteredFavorite.add(friend);
            }
        }

        for (Friend friend : contactList) {
            if (friend.getName() != null && friend.getName().toLowerCase().contains(lowerCaseQuery)) {
                filteredContact.add(friend);
            }
        }

        favoriteAdapter.setData(groupFriendsWithHeaders(filteredFavorite));
        contactAdapter.setData(groupFriendsWithHeaders(filteredContact));
    }


    private List<FriendListItem> groupFriendsWithHeaders(List<Friend> friends) {
        List<FriendListItem> groupedList = new ArrayList<>();

        // 친구들을 이름 기준으로 오름차순 정렬
        Collections.sort(friends, Comparator.comparing(
                f -> f.getName() != null ? f.getName().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER));

        String lastHeader = "";  // 이전 헤더를 추적

        // 친구들을 알파벳 순서대로 그룹화
        for (Friend friend : friends) {
            String name = friend.getName();
            String header;

            // 이름의 첫 글자를 대문자로 추출
            if (name != null && !name.isEmpty()) {
                header = name.substring(0, 1).toUpperCase();  // 첫 글자를 대문자로
            } else {
                header = "?";  // 이름이 없을 경우 '?'를 헤더로 처리
            }

            // 새로운 헤더가 나오면 추가
            if (!header.equals(lastHeader)) {
                groupedList.add(new SectionHeader(header));  // 새로운 헤더 추가
                lastHeader = header;  // 마지막 헤더 갱신
            }

            // 친구 추가 (헤더 뒤에 해당 친구 추가)
            groupedList.add(friend);
        }

        return groupedList;
    }

    private void toggleFavorite(Friend friend) {
        boolean isNowFavorite = friend.isFavorite();

        if (isNowFavorite) {
            contactList.remove(friend);
            favoriteList.add(friend);
        } else {
            favoriteList.remove(friend);
            contactList.add(friend);
        }

        // Firestore 반영
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(friend.getId())
                .update("favorite", isNowFavorite)
                .addOnSuccessListener(aVoid -> Log.d("ContactsFragment", "즐겨찾기 업데이트 완료"))
                .addOnFailureListener(e -> Log.w("ContactsFragment", "즐겨찾기 업데이트 실패", e));

        applyFilterToAdapters();
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

        applyFilterToAdapters();
    }
    private void listenForFriendUpdates() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w("ContactsFragment", "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                Friend updatedFriend = dc.getDocument().toObject(Friend.class);
                                updatedFriend.setId(dc.getDocument().getId()); // ID 설정
                                showAlertDot(updatedFriend.getId());  // dot 표시 함수 호출
                            }
                        }
                    }
                });
    }
    private void showAlertDot(String friendId) {
        boolean updated = false;

        for (Friend friend : contactList) {
            if (friend.getId().equals(friendId)) {
                friend.setShowAlert(true);  // 🔔 알림 dot 표시
                updated = true;
                break;
            }
        }

        if (!updated) {
            for (Friend friend : favoriteList) {
                if (friend.getId().equals(friendId)) {
                    friend.setShowAlert(true);
                    break;
                }
            }
        }

        applyFilterToAdapters();  // RecyclerView 새로고침
    }


}
