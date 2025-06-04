package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editEmail, editPassword, editConfirmPassword, editName, editPhone, editDepartment, editPosition;
    private ImageButton buttonShowPassword, buttonShowConfirmPassword;
    private Button buttonRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmpassword);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDepartment = findViewById(R.id.editDepartment);
        editPosition = findViewById(R.id.editPosition);
        buttonShowPassword = findViewById(R.id.buttonShowPassword);
        buttonShowConfirmPassword = findViewById(R.id.buttonShowConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(editPassword, buttonShowPassword);
            }
        });

        buttonShowConfirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(editConfirmPassword, buttonShowConfirmPassword);
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        editPosition.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                // 키보드 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editPosition.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageButton imageButton) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageButton.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageButton.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void registerUser() {
        final String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        final String name = editName.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();
        final String department = editDepartment.getText().toString().trim();
        final String position = editPosition.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("이메일은 필수입니다.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("비밀번호는 필수입니다.");
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("비밀번호가 일치하지 않습니다.");
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            editName.setError("이름은 필수입니다.");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            editPhone.setError("전화번호는 필수입니다.");
            return;
        }

        if (TextUtils.isEmpty(department)) {
            editDepartment.setError("부서는 필수입니다.");
            return;
        }

        if (TextUtils.isEmpty(position)) {
            editPosition.setError("직책은 필수입니다.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            String userId = mAuth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("department", department);
                            user.put("position", position);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "사용자 문서가 성공적으로 작성되었습니다");

                                            db.collection("users").document(userId).collection("friends")
                                                    .document("placeholder")
                                                    .set(new HashMap<>())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "'friends' 컬렉션이 성공적으로 생성되었습니다");
                                                            Toast.makeText(RegisterActivity.this, "회원가입 성공",
                                                                    Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(RegisterActivity.this, LoginScreenActivity.class));
                                                            finish();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "'friends' 컬렉션 생성 오류", e);
                                                            Toast.makeText(RegisterActivity.this, "Firestore 'friends' 컬렉션 생성 오류: " + e.getMessage(),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "사용자 문서 작성 오류", e);
                                            Toast.makeText(RegisterActivity.this, "Firestore 사용자 정보 저장 오류: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                            if (mAuth.getCurrentUser() != null) {
                                                mAuth.getCurrentUser().delete();
                                            }
                                        }
                                    });

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "인증 실패: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}