package com.example.starlight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.starlight.utils.SupabaseAuthHelper;
import com.example.starlight.utils.DatabaseHelper;
import com.example.starlight.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvError    = findViewById(R.id.tvError);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvGoToSignup).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) { showError("Please enter your email"); return; }
        if (password.isEmpty()) { showError("Please enter your password"); return; }

        // Show loading
        tvError.setVisibility(android.view.View.GONE);
        findViewById(R.id.btnLogin).setAlpha(0.5f);
        findViewById(R.id.btnLogin).setEnabled(false);

        SupabaseAuthHelper.login(email, password,
                new SupabaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(String userName, String userEmail) {
                        runOnUiThread(() -> {
                            session.saveSession(userName, userEmail);
                            Intent i = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            showError(message);
                            findViewById(R.id.btnLogin).setAlpha(1f);
                            findViewById(R.id.btnLogin).setEnabled(true);
                        });
                    }
                });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}