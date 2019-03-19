package bd.com.ipay.ipayskeleton.PaymentFragments.TopupFragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CutCopyPasteEditText;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

import static android.content.Context.CLIPBOARD_SERVICE;

public class TopUpEnterNumberFragment extends Fragment {

	private static final String MOBILE_NUMBER_PREFIX = "+880-1";

	private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
	private CutCopyPasteEditText mNumberEditText;
	private TextView mMyNumberTopUpTextView;
	private ImageView mContactImageView;
	private RadioGroup mTypeSelector;
	private final int PICK_CONTACT_REQUEST = 100;

	private Button mContinueButton;
	private RadioGroup operatorRadioGroup;

	private HttpRequestGetAsyncTask mGetProfileInfoTask;

	private CustomProgressDialog mProgressDialog;

	private Object clipboardService;
	private ClipboardManager clipboardManager;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgressDialog = new CustomProgressDialog(getContext());

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_top_up_enter_number, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        clipboardService = getActivity().getSystemService(CLIPBOARD_SERVICE);
        clipboardManager = (ClipboardManager)clipboardService;

        mNumberEditText = view.findViewById(R.id.number_edit_text);
		mMyNumberTopUpTextView = view.findViewById(R.id.my_number_topup_text_view);
		mContactImageView = view.findViewById(R.id.contact_image_view);
		mTypeSelector = view.findViewById(R.id.type_selector);
		mContinueButton = view.findViewById(R.id.button_send_money);
		operatorRadioGroup = view.findViewById(R.id.operator_radio_group);

		mNumberEditText.setText(MOBILE_NUMBER_PREFIX);
		setUpButtonActions(view);
	}

	private void setUpButtonActions(final View v) {
		mMyNumberTopUpTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String mobileNumber = formatLocalContact(ProfileInfoCacheManager.getMobileNumber())
						.replaceFirst("(\\+8801)", "");
				mNumberEditText.setText(String.format(Locale.getDefault(), "+880-1%s", mobileNumber));
				Selection.setSelection(mNumberEditText.getText(), mNumberEditText.getText().length());
			}
		});

		mContactImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
				startActivityForResult(intent, PICK_CONTACT_REQUEST);
			}
		});

		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (verifyUserInputs()) {
					final String formattedMobileNumber;
					try {
						formattedMobileNumber
								= phoneNumberUtil.format(phoneNumberUtil
										.parse(mNumberEditText.getText().toString(), "BD")
								, PhoneNumberUtil.PhoneNumberFormat.E164);
					} catch (NumberParseException e) {
						return;
					}

					if (ProfileInfoCacheManager.getMobileNumber().equals(formattedMobileNumber)) {
						showAmountInputFragment(
								formattedMobileNumber,
								ProfileInfoCacheManager.getUserName(),
								ProfileInfoCacheManager.getProfileImageUrl()
						);
					} else {
						ContactEngine.ContactData contactData
								= searchLocalContacts(ContactEngine.formatMobileNumberBD(formattedMobileNumber));
						if (contactData != null) {
							showAmountInputFragment(
									formattedMobileNumber,
									contactData.name,
									contactData.photoUri);
						} else {
							getProfileInfo(formattedMobileNumber);
						}
					}
				}
			}
		});

		mNumberEditText.setSelection(mNumberEditText.getText().length());
		mNumberEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Selection.setSelection(mNumberEditText.getText(), mNumberEditText.getText().length());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mNumberEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() < 6) {
					mNumberEditText.setText(MOBILE_NUMBER_PREFIX);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.equals(MOBILE_NUMBER_PREFIX, mNumberEditText.getText())) {
					mNumberEditText.setSelection(6);
				}
			}
		});

		mNumberEditText.setOnCutCopyPasteListener(new CutCopyPasteEditText.OnCutCopyPasteListener() {
            @Override
            public void onCut() { }

            @Override
            public void onCopy() { }

            @Override
            public void onPaste() {
                mNumberEditText.clearComposingText();
                ClipData clipData = clipboardManager.getPrimaryClip();
                int itemCount = clipData.getItemCount();
                if(itemCount > 0){
                    ClipData.Item item = clipData.getItemAt(0);
                    String text = item.getText().toString();
                    if(InputValidator.isValidMobileNumberBD(text)) {
                        String mobileNumber = ContactEngine.formatMobileNumberBD(text);
                        mNumberEditText.setText(mobileNumber);
                    }else{
                        Snackbar snackbar = Snackbar.make(v, getString(R.string.error_invalid_mobile_number), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            }
        });
	}

	private void showAmountInputFragment(String mobileNumber) {
		showAmountInputFragment(mobileNumber, null, null);
	}

	private void showAmountInputFragment(String mobileNumber, String name, String profileImageUrl) {
		if (profileImageUrl != null && !profileImageUrl.contains(Constants.BASE_URL_FTP_SERVER)) {
			profileImageUrl = Constants.BASE_URL_FTP_SERVER + profileImageUrl;
		}
		Bundle bundle = new Bundle();
		bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
		if (name != null)
			bundle.putString(Constants.NAME, name);
		if (profileImageUrl != null)
			bundle.putString(Constants.PHOTO_URI, profileImageUrl);
		bundle.putString(Constants.OPERATOR_CODE, getOperatorName(operatorRadioGroup.getCheckedRadioButtonId()));
		bundle.putInt(Constants.OPERATOR_TYPE, getOperatorType());
		bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, ServiceIdConstants.TOP_UP);
		if (getActivity() instanceof IPayTransactionActionActivity) {
			((IPayTransactionActionActivity) getActivity())
					.switchFragment(new IPayTopUpAmountInputFragment(), bundle, 2, true);
		}
	}

	private ContactEngine.ContactData searchLocalContacts(String mobileNumber) {
		DataHelper dataHelper = DataHelper.getInstance(getActivity());
		int nameIndex, originalNameIndex, profilePictureUrlQualityMediumIndex;
		Cursor cursor = dataHelper.searchContacts(mobileNumber, false, false, false,
				false, false, false, null);
		try {
			if (cursor != null) {
				cursor.moveToFirst();
				nameIndex = cursor.getColumnIndex(DBConstants.KEY_NAME);
				originalNameIndex = cursor.getColumnIndex(DBConstants.KEY_ORIGINAL_NAME);
				profilePictureUrlQualityMediumIndex = cursor.getColumnIndex(DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM);
				String name = cursor.getString(originalNameIndex);
				if (name == null || TextUtils.isEmpty(name)) {
					name = cursor.getString(nameIndex);
				}
				String profilePictureUrl = cursor.getString(profilePictureUrlQualityMediumIndex);

				return new ContactEngine.ContactData(0, name, "", profilePictureUrl);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private void showErrorMessage(String errorMessage) {
		if (getActivity() != null && mContinueButton != null) {
			IPaySnackbar.error(mContinueButton, errorMessage, IPaySnackbar.LENGTH_SHORT).show();
		}
	}

	private boolean verifyUserInputs() {
		try {
			final String mobileNumber = mNumberEditText.getText().toString().trim();
			final Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(mobileNumber, "BD");
			if (TextUtils.isEmpty(mobileNumber)) {
				showErrorMessage(getString(R.string.enter_mobile_number));
			} else if (!TextUtils.isDigitsOnly(mobileNumber.replaceAll("\\D", ""))) {
				showErrorMessage(getString(R.string.please_enter_valid_mobile_number));
			} else if (!phoneNumberUtil.isValidNumberForRegion(phoneNumber, "BD")) {
				showErrorMessage(getString(R.string.please_enter_valid_mobile_number));
			} else if (!InputValidator.isValidMobileNumberBD(phoneNumberUtil
					.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164))) {
				showErrorMessage(getString(R.string.please_enter_valid_mobile_number));
			} else if (mTypeSelector.getCheckedRadioButtonId() == -1) {
				showErrorMessage(getString(R.string.please_select_prepaid_postpaid));
			} else if (operatorRadioGroup.getCheckedRadioButtonId() == -1) {
				showErrorMessage(getString(R.string.select_an_operator));
			} else {
				return true;
			}
		} catch (NumberParseException e) {
			showErrorMessage(getString(R.string.please_enter_valid_mobile_number));
		}
		return false;
	}

	private void getProfileInfo(final String mobileNumber) {
		if (mGetProfileInfoTask != null) {
			return;
		}
		GetUserInfoRequestBuilder mGetUserInfoRequestBuilder = new GetUserInfoRequestBuilder(mobileNumber);

		String mUri = mGetUserInfoRequestBuilder.getGeneratedUri();
		mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_INFO,
				mUri, getContext(), new HttpResponseListener() {
			@Override
			public void httpResponseReceiver(GenericHttpResponse result) {
				mProgressDialog.cancel();
				try {
					if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
						mGetProfileInfoTask = null;
						showAmountInputFragment(mobileNumber);
					} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						GetUserInfoResponse getUserInfoResponse =
								new Gson().fromJson(result.getJsonString(), GetUserInfoResponse.class);
						final String profileUrl;
						if (getUserInfoResponse.getProfilePictures() != null) {
							if (getUserInfoResponse.getProfilePictures().size() > 0) {
								profileUrl = getUserInfoResponse.getProfilePictures().get(0).getUrl();
							} else {
								profileUrl = null;
							}
						} else {
							profileUrl = null;
						}
						showAmountInputFragment(mobileNumber, getUserInfoResponse.getName(), profileUrl);
					} else {
						showAmountInputFragment(mobileNumber);
					}
				} catch (Exception e) {
					showAmountInputFragment(mobileNumber);
				}
			}
		}, true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				final String mobileNumber = formatLocalContact(data.getStringExtra(Constants.MOBILE_NUMBER))
						.replaceFirst("(\\+8801)", "");
				mNumberEditText.setText(String.format(Locale.getDefault(), "+880-1%s", mobileNumber));
				Selection.setSelection(mNumberEditText.getText(), mNumberEditText.getText().length());
			}
		}
	}

	private String formatLocalContact(String userMobileNumber) {
		if (userMobileNumber == null)
			return "";
		try {
			return phoneNumberUtil
					.format(phoneNumberUtil.parse(userMobileNumber, "BD"),
							PhoneNumberUtil.PhoneNumberFormat.E164);
		} catch (NumberParseException e) {
			return ContactEngine.formatMobileNumberBD(userMobileNumber);
		}
	}

	private String getOperatorName(final int checkedRadioButtonId) {
		switch (checkedRadioButtonId) {
			case R.id.gp_radio_button:
				return Constants.OPERATOR_CODE_GP;
			case R.id.robi_radio_button:
				return Constants.OPERATOR_CODE_ROBI;
			case R.id.banglalink_radio_button:
				return Constants.OPERATOR_CODE_BANGLALINK;
			case R.id.airtel_radio_button:
				return Constants.OPERATOR_CODE_AIRTEL;
			case R.id.teletalk_radio_button:
				return Constants.OPERATOR_CODE_TELETALK;
			default:
				return "";
		}
	}

	private int getOperatorType() {
		switch (mTypeSelector.getCheckedRadioButtonId()) {
			case R.id.prepaid:
				return 1;
			case R.id.post_paid:
				return 2;
			default:
				return 1;
		}
	}
}
