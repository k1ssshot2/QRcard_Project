package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FriendProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, departmentTextView, positionTextView;
    private Button editButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend_profile);

        nameTextView = findViewById(R.id.editName);
        emailTextView = findViewById(R.id.editEmail);
        departmentTextView = findViewById(R.id.editDepartment);
        positionTextView = findViewById(R.id.editPosition);
        editButton = findViewById(R.id.btnEdit);

        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        Friend friend = (Friend) intent.getSerializableExtra("friend");

        if (friend != null) {
            nameTextView.setText(friend.getName());
            emailTextView.setText(friend.getEmail());
            departmentTextView.setText(friend.getDepartment());
            positionTextView.setText(friend.getPosition());
        }

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, EditProfileActivity.class);
            editIntent.putExtra("friend", friend);
            startActivity(editIntent);
        });
    }
}

