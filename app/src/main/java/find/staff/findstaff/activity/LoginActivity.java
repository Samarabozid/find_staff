package find.staff.findstaff.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import find.staff.findstaff.R;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private TextView forgotpassword;
    private Button sign_in;

    private String email_txt, password_txt;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private CheckBox checkBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        email = findViewById(R.id.email_field);
        password = findViewById(R.id.password_field);
        sign_in = findViewById(R.id.sign_in_btn);
        forgotpassword = findViewById(R.id.forgot_password_txt);
        checkBox = findViewById(R.id.remember_me_checkbox);

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                final String emailAddress = email.getText().toString();

                if (TextUtils.isEmpty(emailAddress)) {
                    Toast.makeText(getApplicationContext(), "please enter your email firstly", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "password reset email has been sent to : " + emailAddress, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        if (saveLogin) {
            email.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });
    }

    private void check() {
        email_txt = email.getText().toString();
        password_txt = password.getText().toString();

        if (TextUtils.isEmpty(email_txt)) {
            Toast.makeText(getApplicationContext(), "please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password_txt)) {
            Toast.makeText(getApplicationContext(), "please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email_txt.equals("admin@admin.com") && password_txt.equals("admin1234")) {

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("Admin Login");
            progressDialog.setMessage("Please Wait Until Admin Login ...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            progressDialog.setCancelable(false);

            AdminLogin(email_txt, password_txt);

            loginPrefsEditor.putBoolean("savepassword", true);
            loginPrefsEditor.putString("pass", password_txt);
            loginPrefsEditor.apply();

            if (checkBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", email_txt);
                loginPrefsEditor.putString("password", password_txt);
                loginPrefsEditor.apply();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.apply();
            }
        } else {

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("Staff Login");
            progressDialog.setMessage("Please Wait Until Login ...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            progressDialog.setCancelable(false);

            StaffLogin(email_txt, password_txt);

            loginPrefsEditor.putBoolean("savepassword", true);
            loginPrefsEditor.putString("pass", password_txt);
            loginPrefsEditor.apply();

            if (checkBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", email_txt);
                loginPrefsEditor.putString("password", password_txt);
                loginPrefsEditor.apply();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.apply();
            }
        }
    }

    private void StaffLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(getApplicationContext(), StaffHomeActivity.class);
                            startActivity(i);
                            progressDialog.dismiss();
                        } else {
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), taskmessage, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void AdminLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getUser().getUid().equals("TbwyB7OEsOQrHaQnlsxvYJSFYCD2")) {

                                Intent i = new Intent(getApplicationContext(), AdminHomeActivity.class);
                                startActivity(i);
                                progressDialog.dismiss();
                            }
                        } else {
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), taskmessage, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}