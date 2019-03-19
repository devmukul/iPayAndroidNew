package bd.com.ipay.ipayskeleton.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import bd.com.ipay.android.IPayActivity;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments.OTPVerificationBusinessFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments.SignupBusinessStepOneFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments.SignupBusinessStepThreeFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments.SignupBusinessStepTwoFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.LoginFragments.LoginFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.LoginFragments.OTPVerificationTrustFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.PersonalSignUpFragments.OTPVerificationPersonalFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.PersonalSignUpFragments.SignupPersonalStepOneFragment;
import bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.SelectAccountTypeFragment;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeepLinkAction;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SignupOrLoginActivity extends IPayActivity {

    public static String mBirthday;
    public static String mPassword;
    public static String mName;
    public static String mMobileNumber;
    public static String mPromoCode;
    public static String mGender = "M";
    public static int mAccountType;

    public static String mPasswordBusiness;
    public static String mNameBusiness;
    public static String mBusinessName;
    public static String mCompanyName;
    public static String mMobileNumberBusiness;
    public static String mEmailBusiness;
    public static String mBirthdayBusinessHolder;
    public static String mMobileNumberPersonal;
    public static long mTypeofBusiness;
    public static long otpDuration;

    public static boolean isRememberMe = true;

    public static AddressClass mAddressBusiness;
    public static AddressClass mAddressBusinessHolder;

    private DeepLinkAction mDeepLinkAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_or_login);
        mDeepLinkAction = getIntent().getParcelableExtra(Constants.DEEP_LINK_ACTION);
        isRememberMe = true;

        if (SharedPrefManager.ifContainsUserID()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new LoginFragment()).commit();
        } else if (mDeepLinkAction != null && mDeepLinkAction.getAction().trim().equalsIgnoreCase("signup")) {
            switchToSignupPersonalStepOneFragment();
        } else {
            if (getIntent().hasExtra(Constants.MESSAGE)) {
                String message = getIntent().getStringExtra(Constants.MESSAGE);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                switchToLoginFragment();
            } else if (getIntent().hasExtra(Constants.TARGET_FRAGMENT)) {
                String targetFragment = getIntent().getStringExtra(Constants.TARGET_FRAGMENT);
                if (targetFragment.equals(Constants.SIGN_IN)) {
                    switchToLoginFragment();
                } else if (targetFragment.equals(Constants.SIGN_UP)) {
                    switchToAccountSelectionFragment();
                }
            } else {
                Utilities.hideKeyboard(this);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, new SelectAccountTypeFragment()).commit();
            }
        }
    }

    public void switchToLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment()).commit();
    }

    public void switchToOTPVerificationPersonalFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OTPVerificationPersonalFragment()).addToBackStack(null).commit();
    }

    public void switchToSignupPersonalStepOneFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupPersonalStepOneFragment()).addToBackStack(null).commit();
    }

    public void switchToOTPVerificationBusinessFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OTPVerificationBusinessFragment()).addToBackStack(null).commit();
    }

    public void switchToOTPVerificationTrustedFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OTPVerificationTrustFragment()).addToBackStack(null).commit();
    }

    public void switchToAccountSelectionFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SelectAccountTypeFragment()).commit();
    }

    public void switchToBusinessStepOneFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupBusinessStepOneFragment()).addToBackStack(null).commit();
    }

    public void switchToBusinessStepTwoFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupBusinessStepTwoFragment()).addToBackStack(null).commit();
    }

    public void switchToBusinessStepThreeFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 3) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupBusinessStepThreeFragment()).addToBackStack(null).commit();
    }

    public void switchToHomeActivity() {
        if (mDeepLinkAction != null)
            Utilities.performDeepLinkAction(this, mDeepLinkAction);
        else {
            Intent intent = new Intent(SignupOrLoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            this.finish();
        }
    }

    public void switchToDeviceTrustActivity() {
        Intent intent = new Intent(SignupOrLoginActivity.this, DeviceTrustActivity.class);
        if (mDeepLinkAction != null) {
            intent.putExtra(Constants.DEEP_LINK_ACTION, mDeepLinkAction);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    public void switchToTourActivity() {
        Utilities.hideKeyboard(this);
        Intent intent = new Intent(SignupOrLoginActivity.this, TourActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    public void switchToProfileCompletionHelperActivity() {
        Intent intent = new Intent(SignupOrLoginActivity.this, ProfileVerificationHelperActivity.class);
        if (mDeepLinkAction != null)
            intent.putExtra(Constants.DEEP_LINK_ACTION, mDeepLinkAction);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        if (mDeepLinkAction != null && mDeepLinkAction.getAction().trim().equalsIgnoreCase("signup")) {
            Intent intent = new Intent(SignupOrLoginActivity.this, TourActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            this.finish();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (!SharedPrefManager.ifContainsUserID()) {
                Intent intent = new Intent(SignupOrLoginActivity.this, TourActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                this.finish();
            } else
                this.finish();
        }
    }
}

