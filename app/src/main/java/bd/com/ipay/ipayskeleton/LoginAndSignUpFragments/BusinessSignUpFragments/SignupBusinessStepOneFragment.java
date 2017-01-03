package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.Model.MMModule.LoginAndSignUp.CheckPromoCodeRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.LoginAndSignUp.CheckPromoCodeResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class SignupBusinessStepOneFragment extends Fragment {

    private HttpRequestPostAsyncTask mCheckPromoCodeTask = null;
    private CheckPromoCodeResponse mCheckPromoCodeResponse;

    private EditText mBusinessEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mBusinessMobileNumberView;
    private Button mNextButton;
    private Button mLoginButton;
    private ImageView mCrossButton;

    private String mDeviceID;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_signup_business_page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_signup_business_step_one, container, false);

        mPasswordView = (EditText) v.findViewById(R.id.password);
        mConfirmPasswordView = (EditText) v.findViewById(R.id.confirm_password);
        mBusinessEmailView = (EditText) v.findViewById(R.id.email);
        mBusinessMobileNumberView = (EditText) v.findViewById(R.id.business_mobile_number);

        mNextButton = (Button) v.findViewById(R.id.business_next_button);
        mCrossButton = (ImageView) v.findViewById(R.id.button_cross);
        mLoginButton = (Button) v.findViewById(R.id.button_log_in);

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());

        mBusinessMobileNumberView.requestFocus();

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) attemptCheckPromoCode();
                else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mCrossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToTourActivity();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToLoginFragment();
            }
        });


        return v;
    }

    private void attemptCheckPromoCode() {
        if (mCheckPromoCodeTask != null) {
            return;
        }

        // Reset errors.
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        SignupOrLoginActivity.mPasswordBusiness = mPasswordView.getText().toString().trim();
        SignupOrLoginActivity.mEmailBusiness = mBusinessEmailView.getText().toString().trim();
        SignupOrLoginActivity.mMobileNumberBusiness = ContactEngine.formatMobileNumberBD(
                mBusinessMobileNumberView.getText().toString().trim());  // TODO: change Bangladesh
        SignupOrLoginActivity.mAccountType = Constants.BUSINESS_ACCOUNT_TYPE;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        String passwordValidationMsg = InputValidator.isPasswordValid(SignupOrLoginActivity.mPasswordBusiness);

        if (!ContactEngine.isValidNumber(SignupOrLoginActivity.mMobileNumberBusiness)) {
            mBusinessMobileNumberView.setError(getString(R.string.error_invalid_mobile_number));
            focusView = mBusinessMobileNumberView;
            cancel = true;

        } else if (passwordValidationMsg.length() > 0) {
            mPasswordView.setError(passwordValidationMsg);
            focusView = mPasswordView;
            cancel = true;

        } else if (!mConfirmPasswordView.getText().toString().trim().equals(SignupOrLoginActivity.mPasswordBusiness) && mConfirmPasswordView.getVisibility() == View.VISIBLE) {
            mConfirmPasswordView.setError(getString(R.string.confirm_password_not_matched));
            focusView = mConfirmPasswordView;
            cancel = true;

        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) focusView.requestFocus();
        } else {
            ((SignupOrLoginActivity) getActivity()).switchToBusinessStepTwoFragment();
        }
    }

}




