package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;


public class FriendProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editDepartment, editPosition, editPhone;
    private Button editButton;

    private ImageButton btnCall;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editDepartment = findViewById(R.id.editDepartment);
        editPosition = findViewById(R.id.editPosition);
        editButton = findViewById(R.id.btnEdit);
        editPhone = findViewById(R.id.editPhone);
        btnCall = findViewById(R.id.btnCall);


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
        if (friend != null) {
            editPhone.setText(friend.getPhone());
        }

        btnCall.setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.fragment_bottom_call, null);
            BottomSheetDialog dialog = new BottomSheetDialog(FriendProfileActivity.this);
            dialog.setContentView(view);

            TextView tvPhoneCall = view.findViewById(R.id.tvPhoneCall);

            tvPhoneCall.setOnClickListener(callView -> {
                String phoneNumber = editPhone.getText().toString();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(FriendProfileActivity.this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (ContextCompat.checkSelfPermission(FriendProfileActivity.this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    ActivityCompat.requestPermissions(FriendProfileActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE}, 1);
                }

                dialog.dismiss();
            });

            dialog.show();
        });




    }
}

