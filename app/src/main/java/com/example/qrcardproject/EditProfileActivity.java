package com.example.qrcardproject;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etDepartment, etPosition;
    private Button btnEdit;
    private FirebaseFirestore db;
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend_profile);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.editName);
        etEmail = findViewById(R.id.editEmail);
        etPhone = findViewById(R.id.editPhone);
        etDepartment = findViewById(R.id.editDepartment);
        etPosition = findViewById(R.id.editPosition);
        btnEdit = findViewById(R.id.btnEdit);

        friend = (Friend) getIntent().getSerializableExtra("friend");

        if (friend != null) {
            etName.setText(friend.getName());
            etEmail.setText(friend.getEmail());
            etPhone.setText(friend.getPhone());
            etDepartment.setText(friend.getDepartment());
            etPosition.setText(friend.getPosition());
        }

        // 수정 완료 버튼 클릭 시
        btnEdit.setOnClickListener(v -> {
            // 1) 수정된 Friend 객체 생성
            Friend updatedFriend = new Friend();
            updatedFriend.setId(friend.getId());  // **ID 꼭 넣어야 Firestore 문서 덮어쓰기 가능**
            updatedFriend.setName(etName.getText().toString());
            updatedFriend.setEmail(etEmail.getText().toString());
            updatedFriend.setPhone(etPhone.getText().toString());
            updatedFriend.setDepartment(etDepartment.getText().toString());
            updatedFriend.setPosition(etPosition.getText().toString());

            // 2) Firestore 업데이트
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("users")
                    .document(currentUserId)
                    .collection("friends")
                    .document(updatedFriend.getId())  // 수정 대상 문서 ID
                    .set(updatedFriend)
                    .addOnSuccessListener(aVoid -> {
                        // 수정 성공시
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedFriend", updatedFriend);
                        Toast.makeText(EditProfileActivity.this, "프로필 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

    }
}
