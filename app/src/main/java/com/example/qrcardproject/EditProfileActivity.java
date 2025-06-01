package com.example.qrcardproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etDepartment, etPosition;
    private Button btnEdit;
    private FirebaseFirestore db;
    private Friend friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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

        btnEdit.setOnClickListener(v -> {
            String updatedName = etName.getText().toString();
            String updatedEmail = etEmail.getText().toString();
            String updatedPhone = etPhone.getText().toString();
            String updatedDepartment = etDepartment.getText().toString();
            String updatedPosition = etPosition.getText().toString();

            if (friend.getId() != null) {
                db.collection("friends").document(friend.getId())
                        .update(
                                "name", updatedName,
                                "email", updatedEmail,
                                "phone", updatedPhone,
                                "department", updatedDepartment,
                                "position", updatedPosition
                        )
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
