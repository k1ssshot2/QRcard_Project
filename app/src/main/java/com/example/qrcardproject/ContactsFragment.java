package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private RecyclerView favoriteRecyclerView;
    private RecyclerView contactRecyclerView;
    private FriendsAdapter favoriteAdapter;
    private FriendsAdapter contactAdapter;
    private List<Friend> favoriteList;
    private List<Friend> contactList;
    private FirebaseFirestore db;

    public ContactsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // 즐겨찾기 RecyclerView
        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView);
        favoriteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoriteList = new ArrayList<>();
        favoriteAdapter = new FriendsAdapter(favoriteList, this::onFriendClick);
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        // 일반 친구 RecyclerView
        contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactList = new ArrayList<>();
        contactAdapter = new FriendsAdapter(contactList, this::onFriendClick);
        contactRecyclerView.setAdapter(contactAdapter);

        db = FirebaseFirestore.getInstance();
        loadFriendsFromFirestore();

        return view;
    }

    private void onFriendClick(Friend friend) {
        Intent intent = new Intent(getContext(), FriendProfileActivity.class);
        intent.putExtra("friend", friend);
        startActivity(intent);
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

                        favoriteAdapter.notifyDataSetChanged();
                        contactAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("ContactsFragment", "Error getting documents.", task.getException());
                    }
                });
    }
}
