package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FriendProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editDepartment, editPosition;
    private Button editButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editDepartment = findViewById(R.id.editDepartment);
        editPosition = findViewById(R.id.editPosition);
        editButton = findViewById(R.id.btnEdit);

        // 인텐트에서 데이터 가져오기
        Intent intent = getIntent();
        Friend friend = (Friend) intent.getSerializableExtra("friend");

        if (friend != null) {
            editName.setText(friend.getName());
            editEmail.setText(friend.getEmail());
            editDepartment.setText(friend.getDepartment());
            editPosition.setText(friend.getPosition());
        }

        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, EditProfileActivity.class);
            editIntent.putExtra("friend", friend);
            startActivity(editIntent);
        });

        editPosition.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editPosition.getWindowToken(), 0);
                }
                editPosition.clearFocus();
                return true;
            }
            return false;
        });

    }
}

