/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package de.luhmer.stundenplanh_brsimporter.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.luhmer.stundenplanh_brsimporter.app.Adapter.ExamSyncAdapter;
import de.luhmer.stundenplanh_brsimporter.app.Authentication.AccountGeneral;
import de.luhmer.stundenplanh_brsimporter.app.Authentication.AuthenticatorActivity;
import de.luhmer.stundenplanh_brsimporter.app.Parser.ExamParser;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginDialogFragment extends DialogFragment {

    static LoginDialogFragment instance;
    public static LoginDialogFragment getInstance() {
        if(instance == null)
            instance = new LoginDialogFragment();
        return instance;
    }

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	private Activity mActivity;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;
	private String mOc_root_path;
	//private boolean mCbAllowAllSSL;
	private boolean mCbDisableHostnameVerification;

	// UI references.
	@InjectView(R.id.username) EditText mUsernameView;
	@InjectView(R.id.password) EditText mPasswordView;
	@InjectView(R.id.imgView_ShowPassword) ImageView mImageViewShowPwd;

    boolean mPasswordVisible = false;

	ProgressDialog mDialogLogin;

    /*
    @Override
    public void accountAccessGranted(OwnCloudAccount account) {
        mUsernameView.setText(account.getUsername());
        mPasswordView.setText(account.getPassword());
        mOc_root_path_View.setText(account.getUrl());
    }
    */

    public interface LoginSuccessfullListener {
		void LoginSucceeded();
	}
	LoginSuccessfullListener listener;


	public LoginDialogFragment() {

	}

	public void setActivity(Activity mActivity) {
		this.mActivity = mActivity;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(LoginSuccessfullListener listener) {
		this.listener = listener;
	}

    private View.OnClickListener ImgViewShowPasswordListener = new View.OnClickListener() {
        @Override
       public void onClick(View v) {
            mPasswordVisible = !mPasswordVisible;
            if(mPasswordVisible) {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    };

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		//setRetainInstance(true);

        // Build the dialog and set up the button click handlers
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = localInflater.inflate(R.layout.dialog_signin, null);
        ButterKnife.inject(this, view);

	    mImageViewShowPwd.setOnClickListener(ImgViewShowPasswordListener);


        builder.setView(view)
        	/*
        	// Add action buttons
           .setPositiveButton(R.string.action_sign_in_short, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
            	   //attemptLogin();
               }
           })
           .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   LoginDialogFragment.this.getDialog().cancel();
               }
           })*/
           .setTitle(getString(R.string.action_sign_in_short));


    	// Set up the login form.
 		mUsernameView.setText(mUsername);

 		mPasswordView.setText(mPassword);

        builder.setPositiveButton(getString(R.string.action_sign_in_short), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //attemptLogin();
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LoginDialogFragment.this.getDialog().cancel();
                if(mActivity instanceof AuthenticatorActivity)
                    mActivity.finish();
            }
        });

 		//mLoginFormView = view.findViewById(R.id.login_form);
 		//mLoginStatusView = view.findViewById(R.id.login_status);
 		//mLoginStatusMessageView = (TextView) view.findViewById(R.id.login_status_message);

        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    attemptLogin();

                    Boolean wantToCloseDialog = false;
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if(wantToCloseDialog)
                        dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }


	private ProgressDialog BuildPendingDialogWhileLoggingIn()
	{
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog);
		ProgressDialog pDialog = new ProgressDialog(contextThemeWrapper);
        pDialog.setTitle(getString(R.string.login_progress_signing_in));
        return pDialog;
	}




	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString().trim();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}/* else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}*/
		// Check for a valid email address.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} /*else if (!mUsername.contains("@")) {
			mUsernameView.setError(getString(R.string.error_invalid_email));
			focusView = mUsernameView;
			cancel = true;
		}*/


		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.

			//mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			//showProgress(true);
			mAuthTask = new UserLoginTask(mUsername, mPassword);
			mAuthTask.execute((Void) null);

			mDialogLogin = BuildPendingDialogWhileLoggingIn();
     	   	mDialogLogin.show();
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
    /*
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
    */

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		String username;
		String password;
		String exception_message = "";

		public UserLoginTask(String username, String password) {
			this.username = username;
			this.password = password;
		}


        @Override
		protected Boolean doInBackground(Void... params) {

			try {
                ExamParser.Login(username, password);
			} catch (ExamParser.UnauthorizedException e) {
				if(e.getLocalizedMessage() != null)
					exception_message = e.getLocalizedMessage();
				else
					exception_message = getString(R.string.login_dialog_text_something_went_wrong);
				return false;
			} catch (Exception e) {
                e.printStackTrace();
                exception_message = getString(R.string.login_dialog_text_something_went_wrong);
                return false;
            }

            return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mAuthTask = null;
			//showProgress(false);

			mDialogLogin.dismiss();


            if(success) {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                mPrefs.edit().putString(ExamSyncAdapter.EXAM_LIST_CACHED_STRING, "").commit();

                AccountManager mAccountManager = AccountManager.get(mActivity);

                //Remove all accounts first
                Account[] accounts = mAccountManager.getAccounts();
                for (int index = 0; index < accounts.length; index++) {
                    if (accounts[index].type.intern().equals(AccountGeneral.ACCOUNT_TYPE)) {
                        mAccountManager.removeAccount(accounts[index], null, null);
                    }
                }

                //Then add the new account
                Account account = new Account(mUsername, AccountGeneral.ACCOUNT_TYPE);
                mAccountManager.addAccountExplicitly(account, mPassword, null);

                ContentResolver.setIsSyncable(account, AccountGeneral.ACCOUNT_TYPE, 1);
                ContentResolver.setSyncAutomatically(account, AccountGeneral.ACCOUNT_TYPE, true);

                SyncIntervalSelectorActivity.SetAccountSyncInterval(getActivity());


                //Delete cache!
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(ExamSyncAdapter.EXAM_LIST_CACHED_STRING).commit();


                if (listener != null) {
                    listener.LoginSucceeded();
                }

                LoginDialogFragment.this.getDialog().cancel();
                if(mActivity instanceof AuthenticatorActivity) {
                    mActivity.finish();
                }

                Toast.makeText(mActivity, "Please wait. The first sync may take some time", Toast.LENGTH_LONG).show();
            } else {
                ShowAlertDialog("Login failed", exception_message, mActivity);


                //Toast.makeText(getActivity(), exception_message, Toast.LENGTH_LONG).show();
            }
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			//showProgress(false);
		}
	}

	public static void ShowAlertDialog(String title, String text, Activity activity)
	{
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Dialog);

		AlertDialog.Builder aDialog = new AlertDialog.Builder(contextThemeWrapper);
		aDialog.setTitle(title);
		aDialog.setMessage(text);
		aDialog.setPositiveButton(activity.getString(android.R.string.ok) , null);
		aDialog.create().show();
	}
}
