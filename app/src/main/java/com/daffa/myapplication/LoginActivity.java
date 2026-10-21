
package com.daffa.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtMessage = findViewById(R.id.txtMessage);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (username.equals("tes") && password.equals("1234")) {
                Intent intent = new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );
                startActivity(intent);
                finish();
            } else {
                txtMessage.setText("Username atau password salah!");
            }
        });
    }
}
