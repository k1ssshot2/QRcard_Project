package com.example.qrcardproject;

import static android.app.Activity.RESULT_OK;
import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FriendProfileFragment extends Fragment {

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;
    private Button editButton, deleteButton;
    private Friend friend;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        // EditText 초기화
        editName = rootView.findViewById(R.id.editName);
        editEmail = rootView.findViewById(R.id.editEmail);
        editPhone = rootView.findViewById(R.id.editPhone);
        editDepartment = rootView.findViewById(R.id.editDepartment);
        editPosition = rootView.findViewById(R.id.editPosition);

        // 버튼 초기화
        editButton = rootView.findViewById(R.id.btnEdit);
        deleteButton = rootView.findViewById(R.id.btnDelete);

        // 친구 정보가 넘어왔을 때 받아오기
        if (getArguments() != null) {
            friend = (Friend) getArguments().getSerializable("friend");
            if (friend != null) {
                // EditText에 친구 정보 세팅
                editName.setText(friend.getName());
                editEmail.setText(friend.getEmail());
                editPhone.setText(friend.getPhone());
                editDepartment.setText(friend.getDepartment());
                editPosition.setText(friend.getPosition());
            }
        }

        // 수정 버튼 클릭 시
        editButton.setOnClickListener(v -> {
            saveUserProfile(); // 친구 정보 수정 후 저장
        });

        // 삭제 버튼 클릭 시
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle("삭제 확인")
                    .setMessage("이 친구를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("friendEmail", friend.getEmail());
                        getActivity().setResult(RESULT_OK, resultIntent);
                        Toast.makeText(getActivity(), "친구가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        return rootView;
    }

    @SuppressLint("RestrictedApi")
    private void saveUserProfile() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 수정된 값들 가져오기
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String department = editDepartment.getText().toString().trim();
        String position = editPosition.getText().toString().trim();

        // 해당 친구의 Firestore Document Reference 가져오기
        DocumentReference friendRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("friends")
                .document(friend.getEmail());  // 친구의 이메일을 문서 ID로 사용

        // Firestore 업데이트
        friendRef.update(
                "name", name,
                "email", email,
                "phone", phone,
                "department", department,
                "position", position
        ).addOnSuccessListener(aVoid -> {
            // 성공 시, UI에 반영된 값을 바로 보여줌
            editName.setText(name);
            editEmail.setText(email);
            editPhone.setText(phone);
            editDepartment.setText(department);
            editPosition.setText(position);

            Toast.makeText(getContext(), "친구 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "프로필 정보 업데이트 실패: " + e.getMessage());  // 실패한 메시지 출력
            e.printStackTrace();  // 스택 트레이스 출력
            Toast.makeText(getContext(), "프로필 정보 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // 새로운 FriendProfileFragment 인스턴스를 생성하는 메서드
    public static FriendProfileFragment newInstance(Friend friend) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("friend", friend);
        fragment.setArguments(args);
        return fragment;
    }
}





