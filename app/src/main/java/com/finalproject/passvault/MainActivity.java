package com.finalproject.passvault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;

import com.finalproject.passvault.activity.VerifyPassword;
import com.finalproject.passvault.fragment.Account;
import com.finalproject.passvault.fragment.Generator;
import com.finalproject.passvault.fragment.PasswordList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {

    private Toast back;
    private long backPressedTime;
    private BottomNavigationView bottomnav;
    private Handler handler = new Handler();
    private int timeout;
    private long end;
    private int[] timeouts_value = {60000, 300000, 600000, 300000, 1200000, 1800000, 999};
    private static boolean requireVerify;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    ConstraintLayout mMainLayout;


    public static boolean isRequireVerify() {
        return requireVerify;
    }

    public static void setRequireVerify(boolean requireVerify) {
        MainActivity.requireVerify = requireVerify;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainLayout = findViewById(R.id.main_layout);
        bottomnav = findViewById(R.id.bottom_navigation);
        bottomnav.setOnNavigationItemSelectedListener(navigation);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        setTimeout(timeouts_value[sharedPreferences.getInt("timeout", 6)]);
        setEnd(System.currentTimeMillis() + getTimeout());


        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Device Dosn't have Fingerprint Sensor", Toast.LENGTH_SHORT).show();
                mMainLayout.setVisibility(View.VISIBLE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Not Working", Toast.LENGTH_SHORT).show();
                mMainLayout.setVisibility(View.VISIBLE);
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No Fingerprint Assigned", Toast.LENGTH_SHORT).show();
                mMainLayout.setVisibility(View.VISIBLE);
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Verified", Toast.LENGTH_SHORT).show();
                mMainLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Pass Vault")
                .setDescription("Use Fingerprint to Login").setDeviceCredentialAllowed(true).build();

        biometricPrompt.authenticate(promptInfo);





        lock.run();
        check_timeout.run();

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                break;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new PasswordList()).commit();
        }
    }

    private Runnable check_timeout = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
            if (timeouts_value[sharedPreferences.getInt("timeout", 6)] != getTimeout()) {
                setTimeout(timeouts_value[sharedPreferences.getInt("timeout", 6)]);
                setEnd(System.currentTimeMillis() + getTimeout());
            }
            handler.postDelayed(this, 500);
        }
    };

    private Runnable lock = new Runnable() {
        @Override
        public void run() {
            if (getTimeout() != 999) {
                if (System.currentTimeMillis() > getEnd()) {
                    Toast.makeText(MainActivity.this, "Vault timed out. Please log in again", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), VerifyPassword.class);
                    finish();
                    startActivity(intent);
                    stopRunnable();
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }
    };

    private Runnable verify = new Runnable() {
        @Override
        public void run() {
            setRequireVerify(true);
            handler.postDelayed(this, 500);
        }
    };

    private void stopRunnable() {
        handler.removeCallbacks(lock);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequireVerify(false);
            verify.run();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequireVerify(false);
            verify.run();
        }
    }

    @Override
    public void onPause() {
        if (getTimeout() == 999 && isRequireVerify()) {
            Intent intent = new Intent(MainActivity.this, VerifyPassword.class);
            startActivity(intent);
            finish();
        }
        setRequireVerify(true);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.removeCallbacks(verify);
        setRequireVerify(true);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigation = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragment = new PasswordList();
                    break;
                case R.id.nav_generator:
                    fragment = new Generator();
                    break;
                case R.id.nav_account:
                    fragment = new Account();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, fragment).commit();
            return true;
        }
    };

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