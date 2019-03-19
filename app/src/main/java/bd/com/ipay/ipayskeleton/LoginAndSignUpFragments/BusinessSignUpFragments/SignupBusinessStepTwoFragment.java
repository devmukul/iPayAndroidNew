package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetBusinessTypesAsyncTask;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.AddressInputSignUpView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SignupBusinessStepTwoFragment extends BaseFragment {

	private EditText mBusinessType;

	private EditText mBusinessNameView;
	private EditText mCompanyNameView;

	private ResourceSelectorDialog<BusinessType> businessTypeResourceSelectorDialog;

	private AddressInputSignUpView mBusinessAddressView;

	private CustomProgressDialog mProgressDialog;
	private int mSelectedBusinessTypeId = -1;
	public int mSelectedThanaId = -1;
	public int mSelectedDistrictId = -1;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_signup_business_step_two, container, false);
		mProgressDialog = new CustomProgressDialog(getActivity());
		mBusinessNameView = v.findViewById(R.id.business_name);
		mCompanyNameView = v.findViewById(R.id.company_name);
		mBusinessType = v.findViewById(R.id.business_type);
		Button mNextButton = v.findViewById(R.id.business_again_next_button);

		mBusinessAddressView = v.findViewById(R.id.business_address);

		mBusinessAddressView.setHintAddressInput(getString(R.string.business_address_line_1),
				getString(R.string.business_address_line_2));

		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utilities.isConnectionAvailable(getActivity())) attemptGoNextPage();
				else if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
			}
		});

		//business type dialog
		mBusinessType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				businessTypeResourceSelectorDialog.show();
			}
		});

		// Asynchronously load business types into the spinner
		GetBusinessTypesAsyncTask getBusinessTypesAsyncTask =
				new GetBusinessTypesAsyncTask(getActivity(), businessTypeLoadListener);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		getBusinessTypesAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		mSelectedDistrictId = mBusinessAddressView.mSelectedDistrictId;
		mSelectedThanaId = mBusinessAddressView.mSelectedThanaId;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBusinessAddressView.mSelectedDistrictId = mSelectedDistrictId;
		mBusinessAddressView.mSelectedThanaId = mSelectedThanaId;
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_signup_step_2));
	}

	private void attemptGoNextPage() {
		// Store values at the time of the login attempt.
		SignupOrLoginActivity.mBusinessName = mBusinessNameView.getText().toString().trim();
		String companyName = mCompanyNameView.getText().toString().trim();
		if (!TextUtils.isEmpty(companyName)) {
			SignupOrLoginActivity.mCompanyName = companyName;
		}
		SignupOrLoginActivity.mAccountType = Constants.BUSINESS_ACCOUNT_TYPE;
		SignupOrLoginActivity.mTypeofBusiness = mSelectedBusinessTypeId;

		boolean cancel = false;
		View focusView = null;

		if (mBusinessNameView.getText().toString().trim().length() == 0) {
			mBusinessNameView.setError(getString(R.string.invalid_business_name));
			focusView = mBusinessNameView;
			cancel = true;

		} else if (mSelectedBusinessTypeId == -1) {
			mBusinessType.setError(getString(R.string.invalid_business_type));
			focusView = mBusinessType;
			cancel = true;
		} else if (!mBusinessAddressView.verifyUserInputs())
			cancel = true;

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			if (focusView != null) focusView.requestFocus();
		} else {
			SignupOrLoginActivity.mAddressBusiness = mBusinessAddressView.getInformation();
			if (getActivity() instanceof SignupOrLoginActivity) {
				((SignupOrLoginActivity) getActivity()).switchToBusinessStepThreeFragment();
			}
		}
	}

	private final GetBusinessTypesAsyncTask.BusinessTypeLoadListener businessTypeLoadListener =
			new GetBusinessTypesAsyncTask.BusinessTypeLoadListener() {
				@Override
				public void onLoadSuccess(List<BusinessType> businessTypes) {
					//set adapter to load the types
					setTypeAdapter(businessTypes);
					mProgressDialog.dismiss();
				}

				@Override
				public void onLoadFailed() {
					if (getActivity() != null) {
						Toast.makeText(getActivity(), R.string.error_loading_data, Toast.LENGTH_SHORT).show();
						getActivity().finish();
						mProgressDialog.dismiss();
					}
				}
			};


	private void setTypeAdapter(List<BusinessType> businessTypeList) {
		businessTypeResourceSelectorDialog = new ResourceSelectorDialog<>(getActivity(), getString(R.string.business_type), businessTypeList);
		businessTypeResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
			@Override
			public void onResourceSelected(int id, String name) {
				mBusinessType.setError(null);
				mBusinessType.setText(name);
				mSelectedBusinessTypeId = id;
			}
		});
	}
}


