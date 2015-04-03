package de.luhmer.stundenplanh_brsimporter.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.sufficientlysecure.donations.DonationsFragment;


public class DonationsActivity extends ActionBarActivity {

    /**
     * Google
     */
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAviUG/93Gro66l2c9A6Jz+nucJiCbfFH9OdWBPlu/OAr2aEH4suSwJkzmds7FkiFgcakHqNkIN8PhlNmRd/lk5Zh0+8vX1l578xyCdYDO8V6dDtEvhhbUPplcQ9WyQm60K6TQ+H23uj6uYFMCDMZ65LyLdpQpdRgTq3WyHlPgHCag8Cx7dMpa8yhVnR6uWorKIjw8PxUMNVyKSn6Eowcm2BbLX4X8sxjGGG/Z/dtlls82XA3mB4QfngLfKOVK4f2oE+JYN5ol8Z11ocX8dNafbqILMzu+IaF6JI0/mA9uks9zFyc2Wcdjogk/Y0puTQ/CiIgID+9tGDOlV8hoRZJc2wIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"stundenplan.hbrs.donation.1",
            "stundenplan.hbrs.donation.2", "stundenplan.hbrs.donation.3", "stundenplan.hbrs.donation.5", "stundenplan.hbrs.donation.8",
            "stundenplan.hbrs.donation.13"};

    /**
     * PayPal
     */
    private static final String PAYPAL_USER = "ebay@luhmer-maschinenbau.de";
    private static final String PAYPAL_CURRENCY_CODE = "EUR";

    /**
     * Flattr
     */
    private static final String FLATTR_PROJECT_URL = "";//"https://github.com/dschuermann/android-donations-lib/";
    // FLATTR_URL without http:// !
    private static final String FLATTR_URL = "flattr.com/thing/1727842/david-dev-stundenplan-h-brs";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donations);

        android.app.ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            DonationsFragment donationsFragment;
            //donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
            donationsFragment = DonationsFragment.newInstance(false, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
                    getResources().getStringArray(R.array.donation_google_catalog_values), true, PAYPAL_USER,
                    PAYPAL_CURRENCY_CODE, getString(R.string.donation_paypal_item), true, FLATTR_PROJECT_URL, FLATTR_URL);

            ft.replace(R.id.container, donationsFragment, "donationsFragment");
            ft.commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
