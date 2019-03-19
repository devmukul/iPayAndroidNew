package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.CheckIfUserExistsRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.CheckIfUserExistsResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class SignupBusinessStepOneFragment extends BaseFragment implements HttpResponseListener {

	private HttpRequestPostAsyncTask mCheckIfUserExistsTask = null;

	private EditText mBusinessEmailView;
	private EditText mPasswordView;
	private EditText mConfirmPasswordView;
	private EditText mBusinessMobileNumberView;

	private CustomProgressDialog mProgressDialog;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_signup_business_step_one, container, false);

		mProgressDialog = new CustomProgressDialog(getActivity());

		mPasswordView = v.findViewById(R.id.password);
		mConfirmPasswordView = v.findViewById(R.id.confirm_password);
		mBusinessEmailView = v.findViewById(R.id.email);
		mBusinessMobileNumberView = v.findViewById(R.id.business_mobile_number);

		Button mNextButton = v.findViewById(R.id.business_next_button);
		ImageView mCrossButton = v.findViewById(R.id.button_cross);
		Button mLoginButton = v.findViewById(R.id.button_log_in);
		mBusinessMobileNumberView.requestFocus();
		mBusinessMobileNumberView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (!b) {
					if (mBusinessMobileNumberView.getText() != null) {
						if (mBusinessMobileNumberView.getText() != null) {
							String number = mBusinessMobileNumberView.getText().toString();
							if (number.length() == 10 && number.startsWith("1")) {
								String firstPart = number.substring(2, 6);
								String secondPart = number.substring(6, 10);
								mBusinessMobileNumberView.setText(String
										.format(Locale.US, "%s-%s-%s",
												number.substring(0, 2), firstPart, secondPart));
							} else if (number.length() == 11 && number.startsWith("0")) {
								String firstPart = number.substring(3, 7);
								String secondPart = number.substring(7, 11);
								mBusinessMobileNumberView.setText(String
										.format(Locale.US, "%s-%s-%s",
												number.substring(0, 3), firstPart, secondPart));
							}
						}
					}
				} else {
					if (mBusinessMobileNumberView.getText() != null) {
						if (mBusinessMobileNumberView.getText() != null) {
							String number = mBusinessMobileNumberView.getText().toString();
							number = number.replaceAll("[^0-9]", "");
							mBusinessMobileNumberView.setText(number);
						}
					}
				}
			}
		});

		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utilities.isConnectionAvailable(getActivity())) verifyUserInputs();
				else if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
			}
		});

		mCrossButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity) {
					((SignupOrLoginActivity) getActivity()).switchToTourActivity();
				}
			}
		});

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity) {
					((SignupOrLoginActivity) getActivity()).switchToLoginFragment();
				}
			}
		});


		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_signup_step_1));
	}

	private void verifyUserInputs() {
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

		if (!InputValidator.isValidNumber(SignupOrLoginActivity.mMobileNumberBusiness)) {
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
			proceedToNextIfUserNotExists();
		}
	}

	private void proceedToNextIfUserNotExists() {
		mProgressDialog.show();

		CheckIfUserExistsRequestBuilder checkIfUserExistsRequestBuilder = new CheckIfUserExistsRequestBuilder(SignupOrLoginActivity.mMobileNumberBusiness);
		String mUri = checkIfUserExistsRequestBuilder.getGeneratedUri();
		mCheckIfUserExistsTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CHECK_IF_USER_EXISTS,
				mUri, null, getActivity(), false);
		mCheckIfUserExistsTask.mHttpResponseListener = this;
		mCheckIfUserExistsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mProgressDialog.dismiss();
			mCheckIfUserExistsTask = null;
			return;
		}

		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_CHECK_IF_USER_EXISTS)) {

			String message;
			try {
				CheckIfUserExistsResponse mCheckIfUserExistsResponse = gson.fromJson(result.getJsonString(), CheckIfUserExistsResponse.class);
				message = mCheckIfUserExistsResponse.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				message = getString(R.string.server_down);
			}

			if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
				// Proceed to next page in case user not exists
				if (getActivity() instanceof SignupOrLoginActivity) {
					((SignupOrLoginActivity) getActivity()).switchToBusinessStepTwoFragment();
				}
			} else {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
			mCheckIfUserExistsTask = null;
		}
	}
}




