package com.example.starlight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.starlight.utils.DatabaseHelper;
import com.example.starlight.utils.SupabaseAuthHelper;
import com.example.starlight.utils.SessionManager;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private TextView tvError;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Already logged in — go straight to app
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_signup);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvError           = findViewById(R.id.tvError);

        findViewById(R.id.btnSignup).setOnClickListener(v -> attemptSignup());
        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String name     = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

        if (name.isEmpty()) { showError("Please enter your name"); return; }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            showError("Please enter a valid email"); return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters"); return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match"); return;
        }

        // Show loading
        tvError.setVisibility(android.view.View.GONE);
        findViewById(R.id.btnSignup).setAlpha(0.5f);
        findViewById(R.id.btnSignup).setEnabled(false);

        SupabaseAuthHelper.signup(name, email, password,
                new SupabaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(String userName, String userEmail) {
                        runOnUiThread(() -> {
                            session.saveSession(userName, userEmail);
                            Intent i = new Intent(SignupActivity.this,
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
                            findViewById(R.id.btnSignup).setAlpha(1f);
                            findViewById(R.id.btnSignup).setEnabled(true);
                        });
                    }
                });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}