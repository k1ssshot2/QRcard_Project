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

        db = FirebaseFirestore.getInstance();
        loadFriendsFromFirestore();

        return view;
    }

    private void onFriendClick(Friend friend) {
        Intent intent = new Intent(getContext(), FriendProfileFragment.class);
        intent.putExtra("friend", friend);
        startActivityForResult(intent, REQUEST_VIEW_FRIEND);
    }
    private void loadFriendsFromFirestore() {
        db.collection("friends")
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

                        List<FriendListItem> groupedFavoriteList = groupFriendsWithHeaders(favoriteList);
                        List<FriendListItem> groupedContactList = groupFriendsWithHeaders(contactList);

                        favoriteAdapter.setData(groupedFavoriteList);
                        contactAdapter.setData(groupedContactList);
                    } else {
                        Log.w("ContactsFragment", "Error getting documents.", task.getException());
                    }
                });
    }

    private List<FriendListItem> groupFriendsWithHeaders(List<Friend> friends) {
        List<FriendListItem> groupedList = new ArrayList<>();
        Collections.sort(friends, Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER));

        String lastHeader = "";
        for (Friend friend : friends) {
            String header = friend.getName().substring(0, 1).toUpperCase();
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

        List<FriendListItem> groupedFavoriteList = groupFriendsWithHeaders(favoriteList);
        List<FriendListItem> groupedContactList = groupFriendsWithHeaders(contactList);

        favoriteAdapter.setData(groupedFavoriteList);
        contactAdapter.setData(groupedContactList);
    }
}


