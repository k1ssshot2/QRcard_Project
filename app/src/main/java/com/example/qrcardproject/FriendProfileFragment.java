package com.example.qrcardproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendProfileFragment extends Fragment {

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;
    private Button editButton, deleteButton;
    private ImageButton btnCall;

    private Friend friend;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        // View 초기화
        editName = rootView.findViewById(R.id.editName);
        editEmail = rootView.findViewById(R.id.editEmail);
        editPhone = rootView.findViewById(R.id.editPhone);
        editDepartment = rootView.findViewById(R.id.editDepartment);
        editPosition = rootView.findViewById(R.id.editPosition);
        editButton = rootView.findViewById(R.id.btnEdit);
        deleteButton = rootView.findViewById(R.id.btnDelete);
        btnCall = rootView.findViewById(R.id.btnCall);

        db = FirebaseFirestore.getInstance();

        // 전달받은 friend 정보 설정
        if (getArguments() != null) {
            friend = (Friend) getArguments().getSerializable("friend");
            setFriendInfo(friend);
        }

        // 수정 버튼 클릭
        editButton.setOnClickListener(v -> {
            if (friend == null) return;

            Friend updatedFriend = new Friend();
            updatedFriend.setId(friend.getId());
            updatedFriend.setName(editName.getText().toString());
            updatedFriend.setEmail(editEmail.getText().toString());
            updatedFriend.setPhone(editPhone.getText().toString());
            updatedFriend.setDepartment(editDepartment.getText().toString());
            updatedFriend.setPosition(editPosition.getText().toString());

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .document(updatedFriend.getId())
                    .set(updatedFriend)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "프로필 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                        friend = updatedFriend;  // UI에 반영
                        setFriendInfo(friend);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // 삭제 버튼 클릭
        deleteButton.setOnClickListener(v -> {
            if (friend == null || friend.getId() == null) {
                Toast.makeText(getContext(), "삭제할 친구 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("삭제 확인")
                    .setMessage("이 친구를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        db.collection("users")
                                .document(currentUser.getUid())
                                .collection("friends")
                                .document(friend.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "친구가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FriendProfileFragment", "삭제 실패", e);
                                });
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 전화 걸기 버튼
        btnCall.setOnClickListener(v -> {
            if (friend != null && friend.getPhone() != null && !friend.getPhone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + friend.getPhone()));
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

    public static FriendProfileFragment newInstance(Friend friend) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("friend", friend);
        fragment.setArguments(args);
        return fragment;
    }
}
