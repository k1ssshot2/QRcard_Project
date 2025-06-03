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
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Friend> friendMap = new HashMap<>();
    private boolean isInitialLoadDone = false;


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
                ContactsFragment.this.onFriendClick(friend);
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
        if (friend == null || friend.getId() == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(friend.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Friend fullFriend = documentSnapshot.toObject(Friend.class);
                    if (fullFriend != null) {
                        openFriendProfile(fullFriend);
                    } else {
                        Toast.makeText(requireContext(), "친구 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "데이터 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openFriendProfile(Friend friend) {
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
                        friendMap.clear(); // 기존 맵도 초기화

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Friend friend = doc.toObject(Friend.class);
                            friend.setId(doc.getId());

                            friendMap.put(friend.getId(), friend); // ✅ 초기 데이터 friendMap 저장

                            if (friend.isFavorite()) {
                                favoriteList.add(friend);
                            } else {
                                contactList.add(friend);
                            }

                        }
                        Log.d("ContactsFragment", "favoriteList size: " + favoriteList.size());
                        Log.d("ContactsFragment", "contactList size: " + contactList.size());

                        applyFilterToAdapters();   // ✅ RecyclerView 갱신
                        isInitialLoadDone = true;  // ✅ dot 표시 감지 시작

                    } else {
                        Log.w("ContactsFragment", "Firestore 불러오기 실패", task.getException());
                    }
                });

    }

    private void applyFilterToAdapters() {
        String query = searchBar.getText().toString();
        filterFriends(query);
        Log.d("ContactsFragment", "favoriteAdapter item count: " + favoriteAdapter.getItemCount());
        Log.d("ContactsFragment", "contactAdapter item count: " + contactAdapter.getItemCount());
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

    private boolean safeEquals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    private boolean hasImportantChange(Friend oldF, Friend newF) {
        return !safeEquals(oldF.getName(), newF.getName()) ||
                !safeEquals(oldF.getEmail(), newF.getEmail()) ||
                !safeEquals(oldF.getPhone(), newF.getPhone()) ||
                !safeEquals(oldF.getDepartment(), newF.getDepartment()) ||
                !safeEquals(oldF.getPosition(), newF.getPosition());
    }


    private void listenForFriendUpdates() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ContactsFragment", "listenForFriendUpdates 호출, userId: " + currentUserId);

        db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w("ContactsFragment", "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        Log.d("ContactsFragment", "문서 변경 개수: " + querySnapshot.getDocumentChanges().size());

                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            Log.d("ContactsFragment", "문서 변경 타입: " + dc.getType() + ", ID: " + dc.getDocument().getId());

                            if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                Friend updatedFriend = dc.getDocument().toObject(Friend.class);
                                String friendId = dc.getDocument().getId();
                                updatedFriend.setId(friendId);

                                Friend oldFriend = friendMap.get(friendId);

                                boolean showDot = false;

                                if (isInitialLoadDone && oldFriend != null) {
                                    showDot = hasImportantChange(oldFriend, updatedFriend);
                                    Log.d("ContactsFragment", "중요 정보 변경 여부 (dot 표시): " + showDot);
                                }

                                // 최신 데이터로 friendMap 갱신
                                friendMap.put(friendId, updatedFriend);

                                // 리스트 업데이트
                                favoriteList.removeIf(friend -> friend.getId().equals(friendId));
                                contactList.removeIf(friend -> friend.getId().equals(friendId));

                                if (updatedFriend.isFavorite()) {
                                    favoriteList.add(updatedFriend);
                                } else {
                                    contactList.add(updatedFriend);
                                }

                                favoriteList.sort(Comparator.comparing(Friend::getName));
                                contactList.sort(Comparator.comparing(Friend::getName));
                                applyFilterToAdapters();

                                if (!showDot) {
                                    Log.d("ContactsFragment", "Dot 생략 (중요 정보 변경 없음)");
                                    continue;
                                }

                                Log.d("ContactsFragment", "Dot ON → 알림 표시 대상: " + updatedFriend.getName());
                                showAlertDot(friendId);
                            }
                        }
                    } else {
                        Log.d("ContactsFragment", "querySnapshot is null");
                    }
                });
    }





    private void showAlertDot(String friendId) {
        boolean updated = false;

        for (Friend friend : contactList) {
            if (friend.getId().equals(friendId)) {
                friend.setShowAlert(true);
                updated = true;
                Log.d("ContactsFragment", "Dot ON (contact): " + friend.getName());
                break;
            }
        }

        if (!updated) {
            for (Friend friend : favoriteList) {
                if (friend.getId().equals(friendId)) {
                    friend.setShowAlert(true);
                    Log.d("ContactsFragment", "Dot ON (favorite): " + friend.getName());
                    Log.d("ContactsFragment", "Friend ID: " + friend.getId() + ", showAlert: " + friend.shouldShowAlert());
                    break;
                }
            }
        }

        applyFilterToAdapters();
    }
}
