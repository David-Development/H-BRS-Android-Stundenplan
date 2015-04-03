package de.luhmer.stundenplanh_brsimporter.app.Authentication;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import de.luhmer.stundenplanh_brsimporter.app.ExamsFragment;
import de.luhmer.stundenplanh_brsimporter.app.LoginDialogFragment;
import de.luhmer.stundenplanh_brsimporter.app.R;

public class AuthenticatorActivity extends FragmentActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        StartLoginFragment(this);
    }


    public static void StartLoginFragment(final FragmentActivity activity)
    {
        LoginDialogFragment dialog = LoginDialogFragment.getInstance();
        dialog.setActivity(activity);
        dialog.setListener(new LoginDialogFragment.LoginSuccessfullListener() {

            @Override
            public void LoginSucceeded() {
                ExamsFragment.RequestSync(activity);
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "NoticeDialogFragment");
    }
}
