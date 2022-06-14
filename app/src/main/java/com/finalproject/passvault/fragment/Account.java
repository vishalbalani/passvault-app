package com.finalproject.passvault.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.finalproject.passvault.MainActivity;
import com.finalproject.passvault.R;
import com.finalproject.passvault.activity.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class Account extends Fragment implements View.OnClickListener {

    private Button signout, changepass, delacc, feedback,report_bug;
    private TextView currentemail;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private AppCompatActivity activity;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private ProgressDialog progressDialog;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_account_section, container, false);

        progressDialog = new ProgressDialog(getContext());

        toolbar = view.findViewById(R.id.toolbar);
        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.settings));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_settings);

        signout = view.findViewById(R.id.bt_signout_settings);
        changepass = view.findViewById(R.id.bt_changepass_settings);
        currentemail = view.findViewById(R.id.tv_account_account);
        delacc = view.findViewById(R.id.bt_deleteaccount_settings);
        feedback = view.findViewById(R.id.feedback);
        report_bug = view.findViewById(R.id.reportbug);


        auth = FirebaseAuth.getInstance();
        currentemail.setText(auth.getCurrentUser().getEmail());

        signout.setOnClickListener(this);
        changepass.setOnClickListener(this);
        delacc.setOnClickListener(this);
        feedback.setOnClickListener(this);
        report_bug.setOnClickListener(this);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                changepass.setTextColor(getResources().getColor(R.color.white));
                signout.setTextColor(getResources().getColor(R.color.white));
                currentemail.setTextColor(getResources().getColor(R.color.white));
                feedback.setTextColor(getResources().getColor(R.color.white));
                report_bug.setTextColor(getResources().getColor(R.color.white));

                break;

            case Configuration.UI_MODE_NIGHT_NO:
                changepass.setTextColor(getResources().getColor(R.color.black));
                signout.setTextColor(getResources().getColor(R.color.black));
                currentemail.setTextColor(getResources().getColor(R.color.black));
                feedback.setTextColor(getResources().getColor(R.color.black));
                report_bug.setTextColor(getResources().getColor(R.color.black));

                break;
        }
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.drawable.ic_settings) {
            MainActivity.setRequireVerify(false);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_signout_settings:
                FirebaseAuth.getInstance().signOut();
                progressDialog.setMessage("Logging out...");
                progressDialog.show();
                Toast.makeText(v.getContext(), "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), Login.class);
                progressDialog.dismiss();
                MainActivity.setRequireVerify(false);
                getActivity().finish();
                startActivity(intent);
                break;

            case R.id.bt_changepass_settings:
                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(v.getContext(), "Password reset link has been sent to " +
                                            FirebaseAuth.getInstance().getCurrentUser().getEmail() +
                                            ". Please check your inbox", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, "An error occurred. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;

            case R.id.bt_deleteaccount_settings:
                AlertDialog.Builder dialog_confirm_builder = new AlertDialog.Builder(v.getContext())
                        .setMessage("Do you really want to delete your account? " +
                                "Once deleted, your account and all of its data won't be recoverable.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                database = FirebaseDatabase.getInstance().getReference("Users");
                                database.child(auth.getCurrentUser().getUid())
                                        .removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable @org.jetbrains.annotations.Nullable DatabaseError error,
                                                                   @NonNull @NotNull DatabaseReference ref) {
                                                Intent intent = new Intent(v.getContext(), Login.class);
                                                Toast.makeText(v.getContext(), "Deleted account data",
                                                        Toast.LENGTH_SHORT).show();
                                                intent.putExtra("activity", "finish");
                                                MainActivity.setRequireVerify(false);
                                                getActivity().finish();
                                                startActivity(intent);
                                                dialog.cancel();
                                            }
                                        });
                                auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        Toast.makeText(v.getContext(), "Deleted account successfully", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(v.getContext(), "Account deletion failed. Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog_confirm = dialog_confirm_builder.create();
                dialog_confirm.setTitle("Confirmation");
                dialog_confirm.show();

                Button bt_no_dialog = dialog_confirm.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button bt_yes_dialog = dialog_confirm.getButton(DialogInterface.BUTTON_POSITIVE);
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    bt_no_dialog.setTextColor(getResources().getColor(R.color.white));
                    bt_yes_dialog.setTextColor(getResources().getColor(R.color.white));
                }
                break;

            case R.id.feedback:
                Intent intent1 = new Intent(Intent.ACTION_SENDTO);
                String UriText = "mailto:" + Uri.encode("developer.passvault@hotmail.com") + "?subject=" +
                        Uri.encode("Feedback") + Uri.encode("");
                Uri uri = Uri.parse(UriText);
                intent1.setData(uri);
                getActivity().finish();
                startActivity(Intent.createChooser(intent1, "send email"));
                MainActivity.setRequireVerify(false);
                break;

            case R.id.reportbug:
                Intent intent2 = new Intent(Intent.ACTION_SENDTO);
                String UriText1 = "mailto:" + Uri.encode("developer.passvault@hotmail.com") + "?subject=" +
                        Uri.encode("Report Bug") + Uri.encode("");
                Uri uri1 = Uri.parse(UriText1);
                intent2.setData(uri1);
                getActivity().finish();
                startActivity(Intent.createChooser(intent2, "send email"));
                MainActivity.setRequireVerify(false);
                break;
        }
    }
}