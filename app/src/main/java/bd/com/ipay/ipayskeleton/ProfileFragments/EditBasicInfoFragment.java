package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfileInfoRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Occupation;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;

import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;

public class EditBasicInfoFragment extends BaseFragment implements HttpResponseListener, DatePickerDialog.OnDateSetListener {

	private HttpRequestPostAsyncTask mSetProfileInfoTask = null;
	private ResourceSelectorDialog<Occupation> mOccupationTypeResourceSelectorDialog;

	private EditText mNameEditText;
	private EditText mDateOfBirthEditText;
	private EditText mOccupationEditText;
	private EditText mOrganizationNameEditText;
	private RadioGroup genderSelectionRadioGroup;
	private CustomProgressDialog mProgressDialog;
	private String mName = "";
	private DatePickerDialog mDatePickerDialog;
	private String mDateOfBirth = "";
	private String mGender = null;
	private String mOrganizationName;
	private int mOccupation = -1;
	private List<Occupation> mOccupationList;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_edit_basic_info, container, false);

		if (getActivity() != null) {
			if (ProfileInfoCacheManager.isBusinessAccount())
				getActivity().setTitle(getString(R.string.edit_contact_info));
			else
				getActivity().setTitle(getString(R.string.edit_basic_info));
		}
		Bundle bundle = getArguments();
		if (bundle != null) {
			mName = bundle.getString(Constants.NAME);
			mDateOfBirth = bundle.getString(Constants.DATE_OF_BIRTH);
			mGender = bundle.getString(Constants.GENDER);
			mOccupation = bundle.getInt(Constants.OCCUPATION);
			mOccupationList = bundle.getParcelableArrayList(Constants.OCCUPATION_LIST);
			mOrganizationName = bundle.getString(Constants.ORGANIZATION_NAME);
		}

		Button infoSaveButton = view.findViewById(R.id.button_save);
		mNameEditText = view.findViewById(R.id.name);
		mDateOfBirthEditText = view.findViewById(R.id.birthdayEditText);
		mOccupationEditText = view.findViewById(R.id.occupationEditText);
		mOrganizationNameEditText = view.findViewById(R.id.organizationNameEditText);
		genderSelectionRadioGroup = view.findViewById(R.id.gender_selection_radio_group);
		mProgressDialog = new CustomProgressDialog(getActivity());

		Date date = Utilities.formatDateFromString(mDateOfBirth);
		mDatePickerDialog = Utilities.getDatePickerDialog(getActivity(), date, this, true);

		if (ProfileInfoCacheManager.isAccountVerified())
			mNameEditText.setEnabled(false);
		else {
			mNameEditText.setEnabled(true);
		}

		if (ProfileInfoCacheManager.isAccountVerified())
			mDateOfBirthEditText.setEnabled(false);
		else {
			mNameEditText.setEnabled(true);
			mDateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDatePickerDialog.show();
				}
			});
		}

		genderSelectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

			}
		});

		infoSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (verifyUserInputs() && getActivity() != null) {
					Utilities.hideKeyboard(getActivity());
					attemptSaveBasicInfo();
				}
			}
		});

		setOccupationAdapter();
		setOccupation();

        if(!BulkSignupUserDetailsCacheManager.isBasicInfoChecked(true)){
			final String cacheOrganizationName = BulkSignupUserDetailsCacheManager.getOrganizationName(null);

			if(!TextUtils.isEmpty(cacheOrganizationName)) {

				final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
						getString(R.string.bulk_signup_basic_info_helper_msg));

				bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mOrganizationNameEditText.setText(cacheOrganizationName);
						bulkSignUpHelperDialog.cancel();
					}
				});

				bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						bulkSignUpHelperDialog.cancel();
						bulkSignUpHelperDialog.setCheckedResponse("BasicInfo");
					}
				});

				bulkSignUpHelperDialog.show();
			}
        }

        return view;
    }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setProfileInformation();
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_basic_info_edit));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);

	}

	private boolean verifyUserInputs() {
		boolean cancel = false;
		View focusView = null;

		mName = mNameEditText.getText().toString().trim();
		mOrganizationName = mOrganizationNameEditText.getText().toString().trim();

		if (mOrganizationName.isEmpty())
			mOrganizationName = null;

		if (genderSelectionRadioGroup.getCheckedRadioButtonId() != -1 && getView() != null)
			mGender = GenderList.genderNameToCodeMap.get(((RadioButton) getView().findViewById(genderSelectionRadioGroup.getCheckedRadioButtonId())).getText().toString());

		if (mOccupation < 0) {
			mOccupationEditText.setError(getString(R.string.please_enter_occupation));
			return false;
		}

		if (mName.isEmpty()) {
			mNameEditText.setError(getString(R.string.error_invalid_first_name));
			focusView = mNameEditText;
			cancel = true;
		} else if (!InputValidator.isValidNameWithRequiredLength(mName)) {
			mNameEditText.setError(getString(R.string.error_invalid_name_with_required_length));
			focusView = mNameEditText;
			cancel = true;
		}

		if (!InputValidator.isDateOfBirthValid(mDateOfBirth)) {
			focusView = mDateOfBirthEditText;
			cancel = true;
			mDateOfBirthEditText.setError(getString(R.string.please_enter_valid_date_of_birth));
		}

		if (cancel) {
			focusView.requestFocus();
			return false;
		} else {
			return true;
		}
	}

	private void attemptSaveBasicInfo() {
		mProgressDialog.show();

		Gson gson = new Gson();

		SetProfileInfoRequest setProfileInfoRequest = new SetProfileInfoRequest(mName, mGender, mDateOfBirth,
				mOccupation, mOrganizationName);

		String profileInfoJson = gson.toJson(setProfileInfoRequest);
		mSetProfileInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_PROFILE_INFO_REQUEST,
				Constants.BASE_URL_MM + Constants.URL_SET_PROFILE_INFO_REQUEST, profileInfoJson, getActivity(), this, false);
		mSetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void setProfileInformation() {
		mNameEditText.setText(mName);
		mDateOfBirthEditText.setText(mDateOfBirth);
		mOrganizationNameEditText.setText(mOrganizationName);

		String[] genderArray = GenderList.genderCodes;

		if (mGender != null && getView() != null) {
			if (mGender.equals(genderArray[0])) {

				((RadioButton) getView().findViewById(R.id.male_radio_button)).setChecked(true);
			} else if (mGender.equals(genderArray[1])) {
				((RadioButton) getView().findViewById(R.id.female_radio_button)).setChecked(true);
			} else if (mGender.equals(genderArray[2])) {
				((RadioButton) getView().findViewById(R.id.other_radio_button)).setChecked(true);
			}
		}
	}

	public void httpResponseReceiver(GenericHttpResponse result) {
		mProgressDialog.dismiss();
		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mSetProfileInfoTask = null;
			return;
		}

		Gson gson = new Gson();

		switch (result.getApiCommand()) {
			case Constants.COMMAND_SET_PROFILE_INFO_REQUEST:

				try {
					SetProfileInfoResponse mSetProfileInfoResponse = gson.fromJson(result.getJsonString(), SetProfileInfoResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						if (getActivity() != null) {
							Toast.makeText(getActivity(), mSetProfileInfoResponse.getMessage(), Toast.LENGTH_LONG).show();
							if (mGender != null)
								ProfileInfoCacheManager.setGender(GenderList.genderCodeToNameMap.get(mGender));

							//Google Analytic event
							Utilities.sendSuccessEventTracker(mTracker, "Basic Info Update", ProfileInfoCacheManager.getAccountId());
							getActivity().onBackPressed();
						}
					} else {
						if (getActivity() != null)
							Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();

						//Google Analytic event
						Utilities.sendFailedEventTracker(mTracker, "Basic Info Update", ProfileInfoCacheManager.getAccountId(), mSetProfileInfoResponse.getMessage());
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null)
						Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();

					//Google Analytic event
					Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
				}

				mSetProfileInfoTask = null;
				break;
		}
	}

	private void setOccupation() {

		for (int i = 0; i < mOccupationList.size(); i++) {
			if (mOccupationList.get(i).getId() == mOccupation) {
				String occupation = mOccupationList.get(i).getName();
				if (occupation != null) {
					mOccupationEditText.setText(occupation);
				}

				break;
			}
		}
	}

	private void setOccupationAdapter() {
		mOccupationTypeResourceSelectorDialog = new ResourceSelectorDialog<>(getActivity(), getString(R.string.select_an_occupation), mOccupationList);
		mOccupationTypeResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
			@Override
			public void onResourceSelected(int id, String name) {
				mOccupationEditText.setText(name);
				mOccupation = id;
			}
		});

		mOccupationEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOccupationTypeResourceSelectorDialog.show();
			}
		});
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

		final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		mDateOfBirth = String.format(Locale.US, "%s/%s/%s", getFormattedDate(dayOfMonth),
				getFormattedDate(month+1), Integer.toString(year));
		final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE, dd MMMM, yyyy", Locale.getDefault());
		mDateOfBirthEditText.setError(null);
		mDateOfBirthEditText.setText(simpleDateFormat.format(calendar.getTime()));
	}

	private String getFormattedDate(int value) {
		if(value<10) {
			return String.format(Locale.US,"0%d",value);
		} else {
			return Integer.toString(value);
		}
	}
}
