package com.example.qrcard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);

        Button btnLogin = findViewById(R.id.btnlogin);
        btnLogin.setOnClickListener(new view.OnclickListener() {
            @Override
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });
    }
}
