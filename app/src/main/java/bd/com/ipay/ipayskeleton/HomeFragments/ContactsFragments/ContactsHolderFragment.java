package bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import java.util.Locale;

import bd.com.ipay.ipayskeleton.Api.ContactApi.GetContactsAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.GetInviteInfoResponse;
import bd.com.ipay.ipayskeleton.Model.Contact.AddContactRequestBuilder;
import bd.com.ipay.ipayskeleton.QRScanner.BarcodeCaptureActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ContactsHolderFragment extends Fragment implements HttpResponseListener {
	public static final int REQUEST_CODE_PERMISSION = 1001;

	public static GetInviteInfoResponse mGetInviteInfoResponse;
	private HttpRequestGetAsyncTask mGetInviteInfoTask = null;
	private BottomSheetLayout mBottomSheetLayout;

	private RadioGroup mContactTypeRadioGroup;
	private RadioButton mRadioButtonAllContactsSelector;
	private RadioButton mRadioButtonIpayContactsSelector;

	private ContactsFragment miPayAllContactsFragment;
	private ContactsFragment miPayMemberContactsFragment;

	private MaterialDialog.Builder addContactDialog;

	private TextView mContactCount;
	private FloatingActionButton mAddContactButton;

	private HttpRequestPostAsyncTask mAddContactAsyncTask;

	private EditText nameView;
	private EditText mobileNumberView;
	private ImageView buttonScanQRCode;

	private CustomProgressDialog mProgressDialog;

	private EditText mEditTextRelationship;
	private ResourceSelectorDialog mResourceSelectorDialog;

	private String mName;
	private String mMobileNumber;
	private String mRelationship;

	private boolean isAddContactDialogOpen = false;

	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_contact_holder, container, false);

		mBottomSheetLayout = (BottomSheetLayout) v.findViewById(R.id.bottom_sheet);
		mContactCount = (TextView) v.findViewById(R.id.contact_count);
		mAddContactButton = (FloatingActionButton) v.findViewById(R.id.fab_add_contact);

		mContactTypeRadioGroup = (RadioGroup) v.findViewById(R.id.contact_type_radio_group);
		mRadioButtonAllContactsSelector = (RadioButton) v.findViewById(R.id.radio_button_all_contacts);
		mRadioButtonIpayContactsSelector = (RadioButton) v.findViewById(R.id.radio_button_ipay_contacts);

		mContactTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			@ValidateAccess
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.radio_button_all_contacts:
						switchToAllContacts();
						break;
					case R.id.radio_button_ipay_contacts:
						switchToiPayContacts();
						break;
				}
			}
		});

		mAddContactButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.ADD_CONTACTS)
			public void onClick(View v) {
				if (CommonData.getRelationshipList() != null) {
					isAddContactDialogOpen = true;
					showAddContactDialog();
				} else {
					if (!Utilities.isConnectionAvailable(getContext())) {
						Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
					} else {
						DialogUtils.showAlertDialog(getContext(), getString(R.string.add_contact_dialog_not_available));
					}
				}
			}
		});

		mProgressDialog = new CustomProgressDialog(getActivity());
		if (ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_INVITATIONS))
			getInviteInfo();

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_CONTACTS)) {
			mRadioButtonIpayContactsSelector.setChecked(true);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu.findItem(R.id.action_filter_by_service) != null)
			menu.findItem(R.id.action_filter_by_service).setVisible(false);
		if (menu.findItem(R.id.action_filter_by_date) != null)
			menu.findItem(R.id.action_filter_by_date).setVisible(false);
	}

	private void showAddContactDialog() {
		addContactDialog = new MaterialDialog.Builder(getActivity());
		addContactDialog
				.title(R.string.add_a_contact)
				.autoDismiss(false)
				.customView(R.layout.dialog_add_contact, false)
				.positiveText(R.string.add)
				.negativeText(R.string.cancel).dismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				isAddContactDialogOpen = false;
			}
		});

		View dialogView = addContactDialog.build().getCustomView();

		nameView = (EditText) dialogView.findViewById(R.id.edit_text_name);
		mobileNumberView = (EditText) dialogView.findViewById(R.id.edit_text_mobile_number);
		mEditTextRelationship = (EditText) dialogView.findViewById(R.id.edit_text_relationship);
		buttonScanQRCode = (ImageView) dialogView.findViewById(R.id.button_scan_qr_code);
		mRelationship = null;

		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

		mResourceSelectorDialog = new ResourceSelectorDialog(getActivity(), getString(R.string.relationship), CommonData.getRelationshipList());
		mResourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
			@Override
			public void onResourceSelected(int selectedIndex, String mRelation) {
				mEditTextRelationship.setError(null);
				mEditTextRelationship.setText(mRelation);
				mRelationship = mRelation;
			}
		});

		mEditTextRelationship.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utilities.hideKeyboard(getActivity(), nameView);
				Utilities.hideKeyboard(getActivity(), mobileNumberView);
				mResourceSelectorDialog.show();
			}
		});

		buttonScanQRCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utilities.hideKeyboard(getActivity(), nameView);
				Utilities.hideKeyboard(getActivity(), mobileNumberView);
				Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
				startActivityForResult(intent, Constants.RC_BARCODE_CAPTURE);
			}
		});

		addContactDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
				if (verifyUserInputs()) {
					mName = nameView.getText().toString();
					mMobileNumber = mobileNumberView.getText().toString();

					if (mRelationship != null)
						mRelationship = mRelationship.toUpperCase();

					if (new ContactSearchHelper(getActivity()).searchMobileNumber(mMobileNumber))
						Toast.makeText(getContext(), R.string.contact_already_exists, Toast.LENGTH_LONG).show();
					else
						addContact(mName, mMobileNumber, mRelationship);

					Utilities.hideKeyboard(getActivity(), nameView);
					Utilities.hideKeyboard(getActivity(), mobileNumberView);

					dialog.dismiss();
				}
			}
		});

		addContactDialog.onNegative(new MaterialDialog.SingleButtonCallback() {
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
				Utilities.hideKeyboard(getActivity(), nameView);
				Utilities.hideKeyboard(getActivity(), mobileNumberView);

				dialog.dismiss();
			}
		});

		addContactDialog.show();
		nameView.requestFocus();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case REQUEST_CODE_PERMISSION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
					startActivityForResult(intent, Constants.RC_BARCODE_CAPTURE);
				} else {
					Toaster.makeText(getActivity(), R.string.error_camera_permission_denied, Toast.LENGTH_LONG);
				}
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.RC_BARCODE_CAPTURE) {
			if (resultCode == CommonStatusCodes.SUCCESS) {
				if (data != null) {
					Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
					final String result = barcode.displayValue;
					if (result != null) {
						Handler mHandler = new Handler();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (InputValidator.isValidNumber(result)) {
									if (isAddContactDialogOpen)
										mobileNumberView.setText(ContactEngine.formatMobileNumberBD(result));
								} else if (getActivity() != null)
									Toaster.makeText(getActivity(), getResources().getString(
											R.string.scan_valid_qr_code), Toast.LENGTH_SHORT);
							}
						});
					}
				} else {
					getActivity().finish();
				}
			} else {
				getActivity().finish();
			}
		}
	}

	private boolean verifyUserInputs() {
		boolean error = false;

		String name = nameView.getText().toString();
		String mobileNumber = mobileNumberView.getText().toString();

		if (name.isEmpty()) {
			nameView.setError(getString(R.string.error_invalid_name));
			error = true;
		}

		if (!InputValidator.isValidNumber(mobileNumber)) {
			mobileNumberView.setError(getString(R.string.error_invalid_mobile_number));
			error = true;
		}
		return !error;
	}

	private void getInviteInfo() {
		if (mGetInviteInfoTask == null) {
			mGetInviteInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_INVITE_INFO,
					Constants.BASE_URL_MM + Constants.URL_GET_INVITE_INFO, getActivity(), this, false);
			mGetInviteInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void switchToAllContacts() {
		try {
			if (getActivity() != null) {
				if (miPayAllContactsFragment == null) {
					miPayAllContactsFragment = new ContactsFragment();
					miPayAllContactsFragment.setContactLoadFinishListener(new ContactsFragment.ContactLoadFinishListener() {
						@Override
						public void onContactLoadFinish(int contactCount) {
							setContactCount(contactCount);
						}
					});
				}
				miPayAllContactsFragment.setBottomSheetLayout(mBottomSheetLayout);
				getChildFragmentManager().beginTransaction().replace(R.id.fragment_container_contacts, miPayAllContactsFragment).commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void switchToiPayContacts() {
		try {
			if (miPayMemberContactsFragment == null) {
				miPayMemberContactsFragment = new ContactsFragment();
				miPayMemberContactsFragment.setContactLoadFinishListener(new ContactsFragment.ContactLoadFinishListener() {
					@Override
					public void onContactLoadFinish(int contactCount) {
						setContactCount(contactCount);
					}
				});

				Bundle bundle = new Bundle();
				bundle.putBoolean(Constants.IPAY_MEMBERS_ONLY, true);
				miPayMemberContactsFragment.setArguments(bundle);
			}
			miPayMemberContactsFragment.setBottomSheetLayout(mBottomSheetLayout);
			getChildFragmentManager().beginTransaction().replace(R.id.fragment_container_contacts, miPayMemberContactsFragment).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setContactCount(final int contactCount) {
		if (getActivity() != null)
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isAdded())
						mContactCount.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.contacts), contactCount));
				}
			});
	}

	private void addContact(String name, String phoneNumber, String relationship) {
		if (mAddContactAsyncTask != null) {
			return;
		}

		AddContactRequestBuilder addContactRequestBuilder = new
				AddContactRequestBuilder(name, phoneNumber, relationship);

		mAddContactAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_CONTACTS,
				addContactRequestBuilder.generateUri(), addContactRequestBuilder.getAddContactRequest(), getActivity(), this, false);
		mAddContactAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (isAdded()) {
			mProgressDialog.dismiss();
		}

		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mGetInviteInfoTask = null;
			mAddContactAsyncTask = null;
			return;
		}

		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_GET_INVITE_INFO)) {
			if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
				try {
					ContactsHolderFragment.mGetInviteInfoResponse = gson.fromJson(result.getJsonString(), GetInviteInfoResponse.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			mGetInviteInfoTask = null;

		} else if (result.getApiCommand().equals(Constants.COMMAND_ADD_CONTACTS)) {
			try {
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

					if (getActivity() != null) {
						Toaster.makeText(getActivity(), R.string.add_contact_successful, Toast.LENGTH_LONG);
						new GetContactsAsyncTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				} else {
					if (getActivity() != null)
						Toaster.makeText(getActivity(), R.string.failed_add_contact, Toast.LENGTH_LONG);
				}
			} catch (Exception e) {
				e.printStackTrace();

				if (getActivity() != null)
					Toaster.makeText(getActivity(), R.string.failed_add_contact, Toast.LENGTH_LONG);
			}

			mAddContactAsyncTask = null;
		}
	}
}