package com.debayan.continuousdatacollect;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by debayan on 10/20/17.
 */

public class RegistrationActivity extends Activity {
    private static final String TAG = "Registration";

    @InjectView(R.id.input_name)
    EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.btn_signup)
    Button _signupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

    }


    public void register() {



        Log.d(TAG, "Register");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        new MaterialDialog.Builder(this)
                .title(R.string.terms_conditions)
                .content(R.string.terms_description)
                .positiveText(R.string.consent)
                .negativeText(R.string.disconsent)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this,
                                R.style.AppTheme_Dark_Dialog);

                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("Registering...");
                        progressDialog.show();


                        // TODO: Implement your own signup logic here.

                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        // On complete call either onSignupSuccess or onSignupFailed
                                        // depending on success
                                        onSignupSuccess();
                                        // onSignupFailed();
                                        progressDialog.dismiss();
                                    }
                                }, 3000);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        _signupButton.setEnabled(true);
                    }
                })
                .show();

    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);

        setResult(RESULT_OK, null);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("REGISTERED", true);
        editor.putString("NAME", _nameText.getText().toString());
        editor.putString("EMAIL", _emailText.getText().toString());
        editor.apply();
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Registration failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;
    }





}
