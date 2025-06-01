package com.example.qrcardproject;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;  // 추가된 import
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendProfileFragment extends Fragment {

    private static final int REQUEST_EDIT_FRIEND = 2001;

    private EditText editName, editEmail, editDepartment, editPosition, editPhone;
    private Button editButton, deleteButton;
    private Friend friend;
    private ImageButton btnCall;

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        editName = rootView.findViewById(R.id.editName);
        editEmail = rootView.findViewById(R.id.editEmail);
        editPhone = rootView.findViewById(R.id.editPhone);
        editDepartment = rootView.findViewById(R.id.editDepartment);
        editPosition = rootView.findViewById(R.id.editPosition);
        editButton = rootView.findViewById(R.id.btnEdit);
        deleteButton = rootView.findViewById(R.id.btnDelete);
        btnCall = rootView.findViewById(R.id.btnCall);

        if (getArguments() != null) {
            friend = (Friend) getArguments().getSerializable("friend");
            setFriendInfo(friend);
        }

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(getActivity(), EditProfileActivity.class);
            editIntent.putExtra("friend", friend);
            startActivityForResult(editIntent, REQUEST_EDIT_FRIEND);
        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle("삭제 확인")
                    .setMessage("이 친구를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(currentUserId)
                                .collection("friends")
                                .document(friend.getId()) // friend의 문서 id
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "친구가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("friendEmail", friend.getEmail());
                                    getActivity().setResult(RESULT_OK, resultIntent);
                                    getActivity().finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        btnCall.setOnClickListener(v -> {
            if (friend != null && friend.getPhone() != null && !friend.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:" + friend.getPhone()));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void setFriendInfo(Friend friend) {
        if (friend != null) {
            editName.setText(friend.getName());
            editEmail.setText(friend.getEmail());
            editPhone.setText(friend.getPhone());
            editDepartment.setText(friend.getDepartment());
            editPosition.setText(friend.getPosition());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_FRIEND && resultCode == RESULT_OK && data != null) {
            Friend updatedFriend = (Friend) data.getSerializableExtra("updatedFriend");
            if (updatedFriend != null) {
                this.friend = updatedFriend;
                setFriendInfo(friend);
            }
        }
    }

    public static FriendProfileFragment newInstance(Friend friend) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("friend", friend);
        fragment.setArguments(args);
        return fragment;
    }
}
