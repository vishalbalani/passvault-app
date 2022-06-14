package com.finalproject.passvault.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.finalproject.passvault.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText email, password;
    private long backPressedTime;
    private TextView register, forgotTextLink;
    private Toast back;
    private Button login;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private FirebaseAuth authentication = FirebaseAuth.getInstance();
    private ImageView logo;
    private ProgressDialog progressDialog;
    private ToggleButton visibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        progressDialog = new ProgressDialog(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.login_user));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_person);

        // initializing the buttons and necessary components which will later be used
        logo = findViewById(R.id.iv_logo_login);
        email = findViewById(R.id.tv_email_login);
        password = findViewById(R.id.tv_password_login);
        login = findViewById(R.id.bt_login_login);
        forgotTextLink = findViewById(R.id.forgotPassword);


        login.setOnClickListener(this);

        register = findViewById(R.id.bt_register_main);
        register.setOnClickListener(this);


        visibility = findViewById(R.id.btn_toggle_passvisibility_login);
        visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
        forgotTextLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
                passwordResetDialog.setView(resetMail);
                passwordResetDialog.setPositiveButton(Html.fromHtml("<font color='#009AEE'>"+"Yes"+"</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        if (!mail.isEmpty()){
                        authentication.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset Link Sent To Your Email. Check your spam folder too before try again.", Toast.LENGTH_SHORT).show();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! Reset Link is Not Sent. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });}else {
                            Toast.makeText(Login.this, "Error! Reset Link is Not Sent. The email address is badly formatted" , Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                passwordResetDialog.setNegativeButton(Html.fromHtml("<font color='#009AEE'>"+"No"+"</font>"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                passwordResetDialog.create().show();

            }
        });




        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                login.setTextColor(getResources().getColor(R.color.white));
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_light));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                logo.setImageDrawable(getResources().getDrawable(R.drawable.logo_text_dark));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_login_login) {
            userLogin();
        }
        if (v.getId() == R.id.bt_register_main) {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        }
    }

    private void userLogin() {
        String email_login, password_login;
        email_login = email.getText().toString().toLowerCase().trim();
        password_login = password.getText().toString();

        if (email_login.isEmpty()) {
            email.setError("Email field cannot be empty");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email_login).matches()) {
            email.setError("Please enter a valid email");
            email.requestFocus();
            return;
        }
        if (password_login.isEmpty()) {
            password.setError("Password field cannot be empty");
            password.requestFocus();
            return;
        }


        email.setEnabled(false);
        password.setEnabled(false);
        authentication.signInWithEmailAndPassword(email_login, password_login).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                        email.setEnabled(true);
                        password.setEnabled(true);
                        Intent intent = new Intent(Login.this, VerifyPassword.class);
                        startActivity(intent);
                        progressDialog.dismiss();
                        finish();
                    } else {
                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                        Toast.makeText(Login.this, "Please verify your email address to continue using the app", Toast.LENGTH_SHORT).show();
                        email.setEnabled(true);
                        password.setEnabled(true);
                        progressDialog.dismiss();
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException invalidUserException) {
                        Toast.makeText(Login.this, "Account not found. Please register first", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException invalidCredentialsException) {
                        Toast.makeText(Login.this, "Email and password do not match.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Login.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                    email.setEnabled(true);
                    password.setEnabled(true);
                    progressDialog.dismiss();
                }
            }
        });

    }




    // gets called when the back button on the navigation bar is pressed
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            back.cancel();
            super.onBackPressed();
            return;
        } else {
            back = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            back.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}