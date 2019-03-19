package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.District;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.DistrictRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetDistrictResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetThanaResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Thana;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.ThanaRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CountryList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class AddressInputSignUpView extends FrameLayout implements HttpResponseListener {

	private HttpRequestGetAsyncTask mGetThanaListAsyncTask = null;

	private HttpRequestGetAsyncTask mGetDistrictListAsyncTask = null;

	private List<Thana> mThanaList;
	private List<District> mDistrictList;

	public int mSelectedThanaId = -1;
	public int mSelectedDistrictId = -1;

	private Context context;

	private TextInputLayout mAddressLine1FieldInputLayout;
	private TextInputLayout mAddressLine2FieldInputLayout;
	private EditText mAddressLine1Field;
	private EditText mAddressLine2Field;
	private EditText mThanaSelection;
	private EditText mDistrictSelection;
	private EditText mCountrySelection;
	private EditText mPostalCodeField;
	private EditTextWithProgressBar mDistrictEditTextProgressBar;
	private EditTextWithProgressBar mThanaEditTextProgressBar;

	private ResourceSelectorDialog<District> districtSelectorDialog;
	private ResourceSelectorDialog<Thana> thanaSelectorDialog;

	public AddressInputSignUpView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public AddressInputSignUpView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public AddressInputSignUpView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		this.context = context;

		View v = inflate(context, R.layout.view_input_address_view_signup, null);

		mAddressLine1FieldInputLayout = v.findViewById(R.id.text_input_address_line_1);
		mAddressLine2FieldInputLayout = v.findViewById(R.id.text_input_address_line_2);
		mAddressLine1Field = v.findViewById(R.id.address_line_1);
		mAddressLine2Field = v.findViewById(R.id.address_line_2);
		mCountrySelection = v.findViewById(R.id.country);
		mCountrySelection.setEnabled(false);
		mPostalCodeField = v.findViewById(R.id.postcode);
		mThanaEditTextProgressBar = v.findViewById(R.id.thana);
		mThanaSelection = mThanaEditTextProgressBar.getEditText();
		mDistrictEditTextProgressBar = v.findViewById(R.id.district);
		mDistrictSelection = mDistrictEditTextProgressBar.getEditText();

		addView(v);
		getDistrictList();
	}

	private void getThanaList(int districtId) {
		if (mGetThanaListAsyncTask != null) {
			return;
		}
		mThanaEditTextProgressBar.showProgressBar();

		mGetThanaListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_THANA_LIST,
				new ThanaRequestBuilder(districtId).getGeneratedUri(), context, this, true);
		mGetThanaListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getDistrictList() {
		if (mGetDistrictListAsyncTask != null) {
			return;
		}
		mDistrictEditTextProgressBar.showProgressBar();

		mGetDistrictListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_DISTRICT_LIST,
				new DistrictRequestBuilder().getGeneratedUri(), context, this, true);
		mGetDistrictListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void setHintAddressInput(String addressLine1, String addressLine2) {
		mAddressLine1FieldInputLayout.setHint(addressLine1);
		mAddressLine2FieldInputLayout.setHint(addressLine2);
	}

	private void setThanaAdapter(List<Thana> thanaList) {
		thanaSelectorDialog = new ResourceSelectorDialog<>(context, context.getString(R.string.select_a_thana), thanaList);
		thanaSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
			@Override
			public void onResourceSelected(int id, String name) {
				mThanaSelection.setError(null);
				mThanaSelection.setText(name);
				mSelectedThanaId = id;
			}
		});

		mThanaSelection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				thanaSelectorDialog.show();
			}
		});
	}

	private void setDistrictAdapter(List<District> districtList) {
		districtSelectorDialog = new ResourceSelectorDialog<>(context, context.getString(R.string.select_a_district), districtList);
		districtSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
			@Override
			public void onResourceSelected(int id, String name) {
				mDistrictSelection.setError(null);
				mDistrictSelection.setText(name);
				mSelectedDistrictId = id;

				mThanaList = null;
				mSelectedThanaId = -1;
				mThanaSelection.setText(context.getString(R.string.loading));

				getThanaList(mSelectedDistrictId);
			}
		});

		mDistrictSelection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				districtSelectorDialog.show();
			}
		});
	}

	public void setInformation(AddressClass address) {
		if (address == null)
			return;

		if (address.getAddressLine1() != null)
			mAddressLine1Field.setText(address.getAddressLine1());
		if (address.getAddressLine2() != null)
			mAddressLine2Field.setText(address.getAddressLine2());
		if (address.getPostalCode() != null)
			mPostalCodeField.setText(address.getPostalCode());

		mSelectedDistrictId = address.getDistrictCode();
		mSelectedThanaId = address.getThanaCode();

		if (mDistrictList == null)
			getDistrictList();
		else {
			setDistrictName(mSelectedDistrictId);
			getThanaList(mSelectedDistrictId);
		}

		String countryCode = address.getCountryCode();
		if (countryCode == null)
			countryCode = "BD";
		for (int i = 0; i < CountryList.countryISOcodes.length; i++) {
			if (countryCode.equals(CountryList.countryISOcodes[i])) {
				mCountrySelection.setText(CountryList.countryNames[i]);
				break;
			}
		}
	}

	private void setDistrictName(int district) {
		if (mDistrictList != null && district >= 0) {
			for (int i = 0; i < mDistrictList.size(); i++) {
				if (district == mDistrictList.get(i).getId()) {
					mDistrictSelection.setText(mDistrictList.get(i).getName());
					return;
				}
			}
		}
	}

	private void setThanaName(int thana) {
		if (mThanaList != null) {
			for (int i = 0; i < mThanaList.size(); i++) {
				if (thana == mThanaList.get(i).getId()) {
					mThanaSelection.setText(mThanaList.get(i).getName());
					return;
				}
			}
		}
	}

	public void resetInformation() {
		setInformation(new AddressClass());
	}

	public boolean verifyUserInputs() {
		boolean cancel = false;
		View focusedView = null;
		mAddressLine1Field.setError(null);
		mDistrictSelection.setError(null);
		mThanaSelection.setError(null);
		mPostalCodeField.setError(null);

		if (mAddressLine1Field.getText().toString().trim().length() == 0) {
			mAddressLine1Field.setError(context.getString(R.string.invalid_address_line_1));
			focusedView = mAddressLine1Field;
			cancel = true;
		} else if (mDistrictSelection.getText().toString().trim().length() == 0) {
			mDistrictSelection.setError(context.getString(R.string.invalid_district));
			focusedView = mDistrictSelection;
			cancel = true;
		} else if (mThanaSelection.getText().toString().trim().length() == 0) {
			mThanaSelection.setError(context.getString(R.string.invalid_thana));
			focusedView = mThanaSelection;
			cancel = true;
		} else if (mPostalCodeField.getText().toString().length() < 4) {
			mPostalCodeField.setError(context.getString(R.string.invalid_postcode));
			focusedView = mPostalCodeField;
			cancel = true;
		}

		if (cancel) {
			focusedView.requestFocus();
			return false;
		} else {
			return true;
		}
	}

	public AddressClass getInformation() {
		String addressLine1 = mAddressLine1Field.getText().toString().trim();
		String addressLine2 = mAddressLine2Field.getText().toString().trim();
		String postalCode = mPostalCodeField.getText().toString().trim();
		String country = "BD";
		int thana = mSelectedThanaId;
		int district = mSelectedDistrictId;

		return new AddressClass(addressLine1, addressLine2, country,
				district, thana, postalCode);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
			mGetThanaListAsyncTask = null;
			mGetDistrictListAsyncTask = null;
			return;
		}

		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_GET_DISTRICT_LIST)) {
			try {
				GetDistrictResponse mGetDistrictResponse = gson.fromJson(result.getJsonString(), GetDistrictResponse.class);

				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					mDistrictList = mGetDistrictResponse.getDistricts();
					mDistrictEditTextProgressBar.hideProgressBar();
					setDistrictAdapter(mDistrictList);
					setDistrictName(mSelectedDistrictId);
					if (mSelectedDistrictId >= 0) {
						getThanaList(mSelectedDistrictId);
					}

				} else {
					if (context != null)
						Toaster.makeText(context, R.string.failed_loading_district_list, Toast.LENGTH_LONG);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (context != null)
					Toaster.makeText(context, R.string.failed_loading_district_list, Toast.LENGTH_LONG);
			}

			mGetDistrictListAsyncTask = null;
		} else if (result.getApiCommand().equals(Constants.COMMAND_GET_THANA_LIST)) {
			try {
				GetThanaResponse mGetThanaResponse = gson.fromJson(result.getJsonString(), GetThanaResponse.class);
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					mThanaList = mGetThanaResponse.getThanas();
					mThanaEditTextProgressBar.hideProgressBar();
					mThanaSelection.setText("");
					setThanaAdapter(mThanaList);
					setThanaName(mSelectedThanaId);

				} else {
					if (context != null)
						Toaster.makeText(context, R.string.failed_loading_thana_list, Toast.LENGTH_LONG);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (context != null)
					Toaster.makeText(context, R.string.failed_loading_thana_list, Toast.LENGTH_LONG);
			}

			mGetThanaListAsyncTask = null;
		}
	}
}
