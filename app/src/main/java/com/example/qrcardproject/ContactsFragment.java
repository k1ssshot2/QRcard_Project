package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher; // 🔸추가
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // 🔸추가

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

    private EditText searchBar; // 🔸추가

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
        favoriteAdapter = new FriendsAdapter(new ArrayList<>(), this::onFriendClick);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactList = new ArrayList<>();
        contactAdapter = new FriendsAdapter(new ArrayList<>(), this::onFriendClick);
        contactRecyclerView.setAdapter(contactAdapter);

        searchBar = view.findViewById(R.id.searchBar); // 🔸검색창 연결
        searchBar.addTextChangedListener(new TextWatcher() { // 🔸검색 기능 추가
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

        return view;
    }

    private void onFriendClick(Friend friend) {
        FriendProfileFragment profileFragment = new FriendProfileFragment();

        // 데이터를 번들로 전달
        Bundle bundle = new Bundle();
        bundle.putSerializable("friend", friend);  // Friend는 Serializable 구현 필요
        profileFragment.setArguments(bundle);

        // 프래그먼트 전환
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFrameLayout, profileFragment) // 프레임레이아웃 ID 확인
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

                        Log.d("ContactsFragment", "Firestore 친구 문서 개수: " + task.getResult().size());

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Friend friend = doc.toObject(Friend.class);
                            friend.setId(doc.getId());

                            Log.d("ContactsFragment", "가져온 친구: " + friend.getName() + " / 즐겨찾기: " + friend.isFavorite());

                            if (friend.isFavorite()) {
                                favoriteList.add(friend);
                            } else {
                                contactList.add(friend);
                            }
                        }

                        applyFilterToAdapters(); // 🔸검색 필터와 함께 적용
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
        Collections.sort(friends, Comparator.comparing(
                f -> f.getName() != null ? f.getName().toLowerCase() : "", String.CASE_INSENSITIVE_ORDER));

        String lastHeader = "";
        for (Friend friend : friends) {
            String name = friend.getName();
            String header;

            if (name != null && !name.isEmpty()) {
                header = name.substring(0, 1).toUpperCase();
            } else {
                header = "?";  // 이름이 비어 있거나 null이면 '?' 섹션으로
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

        applyFilterToAdapters(); // 🔸삭제 후 필터 다시 적용
    }
}
