package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        Button btnLogin = findViewById(R.id.btnLogin);
        EditText emailEditText = findViewById(R.id.login_id);
        EditText passwordEditText = findViewById(R.id.login_pw);

        ImageButton toggleButton = findViewById(R.id.buttonShowPassword);

        toggleButton.setOnClickListener(v -> {
            togglePasswordVisibility(passwordEditText, toggleButton);
        });



        btnLogin.setOnClickListener(v -> {

            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) { //로그인 성공시 firestore에 존재하는지 확인하는 코드
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();

                            FirebaseFirestore.getInstance().collection("users")
                                    .get()
                                    .addOnSuccessListener(documentSnapshots -> {
                                        if (!documentSnapshots.isEmpty()) { // 유저 정보 있음 -> 홈화면 이동
                                            Intent intent = new Intent(LoginScreenActivity.this, NaviActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else { // 유저 정보 없음 → 회원가입이 안 되어 있음
                                            FirebaseAuth.getInstance().signOut(); // 로그인 취소
                                            Toast.makeText(LoginScreenActivity.this, "회원가입된 계정이 아닙니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Exception exception = task.getException();

                            // 인터넷 연결 실패 시
                            if (exception instanceof FirebaseAuthException && "ERROR_NETWORK_REQUEST_FALIED".equals(((FirebaseAuthException) exception).getErrorCode())) {
                                Toast.makeText(LoginScreenActivity.this, "네트워크 오류입니다. 확인 후 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                            } else if (exception instanceof FirebaseAuthInvalidUserException) { //이메일 오류시
                                Toast.makeText(LoginScreenActivity.this, "존재하지 않는 이메일입니다. ", Toast.LENGTH_SHORT).show();
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginScreenActivity.this, "비밀번호가 올바르지 않습니다. ", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginScreenActivity.this, "로그인에 실패했습니다. ", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
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
}