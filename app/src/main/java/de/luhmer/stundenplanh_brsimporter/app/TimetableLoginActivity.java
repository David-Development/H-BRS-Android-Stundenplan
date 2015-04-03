package de.luhmer.stundenplanh_brsimporter.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.luhmer.stundenplanh_brsimporter.app.Helper.Constants;


/**
 * A login screen that offers login via email/password.

 */
public class TimetableLoginActivity extends Activity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_login);

        ButterKnife.inject(this);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin(false);
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @OnClick(R.id.btn_login) void login() {
        attemptLogin(false);
    }

    @OnClick(R.id.btn_register) void register() {
        attemptLogin(true);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(boolean register) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            if(!register)
                mAuthTask = new UserLoginTask(username, password);
            else
                mAuthTask = new RegisterTask(username, password);


            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
        //return password.length() > 4;
    }



    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        final String mUsername;
        final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(TimetableLoginActivity.this);
            mPrefs.edit()
                    .putString(Constants.PREF_USERNAME_TIMETABLE, null)
                    .putString(Constants.PREF_PASSWORD_TIMETABLE, null)
                    .commit();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=verify_login&username=" + mUsername + "&password=" + mPassword, TimetableLoginActivity.this);
                JSONObject jsonResult = new JSONObject(result);
                return jsonResult.getBoolean("success");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(TimetableLoginActivity.this);
                mPrefs.edit()
                        .putString(Constants.PREF_USERNAME_TIMETABLE, mUsername)
                        .putString(Constants.PREF_PASSWORD_TIMETABLE, mPassword)
                        .commit();

                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends UserLoginTask {

        public RegisterTask(String username, String password) {
            super(username, password);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String result = TimetableImporterActivity.getBodyFromPageGet(Constants.API_URL + "?action=register&username=" + mUsername + "&password=" + mPassword, TimetableLoginActivity.this);
                JSONObject jsonResult = new JSONObject(result);
                return jsonResult.getBoolean("success");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}



