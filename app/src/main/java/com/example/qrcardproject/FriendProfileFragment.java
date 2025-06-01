package com.example.qrcardproject;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;  // 추가된 import
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class FriendProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView, phoneTextView, departmentTextView, positionTextView;
    private Button editButton, deleteButton;
    private Friend friend;

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        nameTextView = rootView.findViewById(R.id.editName);
        emailTextView = rootView.findViewById(R.id.editEmail);
        phoneTextView = rootView.findViewById(R.id.editPhone);
        departmentTextView = rootView.findViewById(R.id.editDepartment);
        positionTextView = rootView.findViewById(R.id.editPosition);
        editButton = rootView.findViewById(R.id.btnEdit);
        deleteButton = rootView.findViewById(R.id.btnDelete);

        if (getArguments() != null) {
            friend = (Friend) getArguments().getSerializable("friend");
            if (friend != null) {
                nameTextView.setText(friend.getName());
                emailTextView.setText(friend.getEmail());
                phoneTextView.setText(friend.getPhone());
                departmentTextView.setText(friend.getDepartment());
                positionTextView.setText(friend.getPosition());
            }
        }

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(getActivity(), EditProfileActivity.class);
            editIntent.putExtra("friend", friend);
            startActivity(editIntent);
        });

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


    public static FriendProfileFragment newInstance(Friend friend) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("friend", friend);
        fragment.setArguments(args);
        return fragment;
    }
}


