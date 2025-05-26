package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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
                            // 로그인 실패
                            Toast.makeText(LoginScreenActivity.this, "로그인 실패: \n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }


                    });
        });
    }
}