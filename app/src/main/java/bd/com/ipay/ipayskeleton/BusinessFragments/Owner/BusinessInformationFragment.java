package bd.com.ipay.ipayskeleton.BusinessFragments.Owner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadProfilePictureAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetBusinessTypesAsyncTask;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.PhotoSelectionHelperDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Employee.GetBusinessInformationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.GetUserAddressResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfilePictureResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.District;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.DistrictRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetDistrictResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetOccupationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetThanaResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Occupation;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.OccupationRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Thana;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.ThanaRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;
import bd.com.ipay.ipayskeleton.camera.utility.CameraAndImageUtilities;

public class BusinessInformationFragment extends ProgressFragment implements HttpResponseListener {
	private static final int REQUEST_CODE_PERMISSION = 1001;
	private final int ACTION_PICK_PROFILE_PICTURE = 100;

	private HttpRequestGetAsyncTask mGetBusinessInformationAsyncTask;
	private GetBusinessInformationResponse mGetBusinessInformationResponse;

	private GetBusinessTypesAsyncTask mGetBusinessTypesAsyncTask;

	private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
	private GetProfileInfoResponse mGetProfileInfoResponse;

	private HttpRequestGetAsyncTask mGetOccupationTask = null;
	private GetOccupationResponse mGetOccupationResponse;

	private HttpRequestGetAsyncTask mGetUserAddressTask = null;
	private GetUserAddressResponse mGetUserAddressResponse = null;

	private HttpRequestGetAsyncTask mGetThanaListAsyncTask = null;
	private GetThanaResponse mGetThanaResponse;

	private HttpRequestGetAsyncTask mGetDistrictListAsyncTask = null;
	private GetDistrictResponse mGetDistrictResponse;

	private UploadProfilePictureAsyncTask mUploadBusinessContactProfilePictureAsyncTask = null;
	private SetProfilePictureResponse mSetBusinessContactProfilePictureResponse;

	private TextView mBusinessNameView;
	private TextView mCompanyNameView;
	private TextView mBusinessMobileNumberView;
	private TextView mBusinessTypeView;

	private TextView mNameView;
	private TextView mMobileNumberView;
	private TextView mOccupationView;
	private TextView mOrganizationNameView;
	private TextView mVerificationStatusView;
	private TextView mSignUpTimeView;

	private TextView mPresentAddressView;
	private View mPresentAddressHolder;
	private AddressClass mPresentAddress;

	private View mBusinessContactProfilePictureHolderView;
	private ProfileImageView mBusinessContactProfilePictureView;

	private List<String> mOptionsForImageSelectionList;
	private int mSelectedOptionForImage = -1;

	private ImageButton mPresentAddressEditButton;
	private ImageButton mContactInfoEditButton;
	private ImageButton mOfficeInfoEditButton;

	private String mName = "";
	private String mMobileNumber = "";
	private String mProfileImageUrl = "";
	private String mDateOfBirth = "";
	private String mBusinessContactProfilePictureUrl = "";
	private String mSelectedImagePath = "";

	private int mOccupation = 0;
	private String mOrganizationName = "";
	private String mGender;
	private String mSignUpTime = "";
	private String mVerificationStatus = null;

	private List<Thana> mThanaList;
	private List<District> mDistrictList;
	private List<BusinessType> mBusinessTypes;
	private List<Occupation> mOccupationList;

	private View mBusinessInformationViewHolder;
	private View mBusinessAddressViewHolder;

	private CustomProgressDialog mProgressDialog;
	private MaterialDialog.Builder mProfilePictureErrorDialogBuilder;
	private MaterialDialog mProfilePictureErrorDialog;
	private PhotoSelectionHelperDialog photoSelectionHelperDialog;

	private TextView mBusinessInfoServiceNotAllowedTextView;
	private TextView mBusinessAddressServiceNotAllowedTextView;
	private Tracker mTracker;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTracker = Utilities.getTracker(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_information));
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_business_information, container, false);
		getActivity().setTitle(R.string.basic_info);

		mBusinessNameView = (TextView) view.findViewById(R.id.textview_business_name);
		mCompanyNameView = (TextView) view.findViewById(R.id.textview_company_name);
		mBusinessMobileNumberView = (TextView) view.findViewById(R.id.textview_business_mobile_number);
		mBusinessTypeView = (TextView) view.findViewById(R.id.textview_business_type);
		mNameView = (TextView) view.findViewById(R.id.textview_name);
		mOccupationView = (TextView) view.findViewById(R.id.textview_occupation);
		mOrganizationNameView = (TextView) view.findViewById(R.id.textview_organization_name);
		mMobileNumberView = (TextView) view.findViewById(R.id.textview_mobile_number);
		mVerificationStatusView = (TextView) view.findViewById(R.id.textview_verification_status);
		mSignUpTimeView = (TextView) view.findViewById(R.id.textview_signup);

		mBusinessContactProfilePictureHolderView = view.findViewById(R.id.business_contact_profile_picture_layout);
		mBusinessContactProfilePictureView = (ProfileImageView) view.findViewById(R.id.business_contact_profile_picture);

		mPresentAddressView = (TextView) view.findViewById(R.id.textview_present_address);
		mPresentAddressHolder = view.findViewById(R.id.present_address_holder);

		mOfficeInfoEditButton = (ImageButton) view.findViewById(R.id.button_edit_office_information);
		mPresentAddressEditButton = (ImageButton) view.findViewById(R.id.button_edit_present_address);
		mContactInfoEditButton = (ImageButton) view.findViewById(R.id.button_edit_contact_information);

		mBusinessInformationViewHolder = view.findViewById(R.id.business_information_view_holder);
		mBusinessAddressViewHolder = view.findViewById(R.id.business_address_view_holder);
		mBusinessInfoServiceNotAllowedTextView = (TextView) view.findViewById(R.id.business_info_service_not_allowed_text_view);
		mBusinessAddressServiceNotAllowedTextView = (TextView) view.findViewById(R.id.business_address_service_not_allowed_text_view);

		mMobileNumber = ProfileInfoCacheManager.getMobileNumber();

		mProgressDialog = new CustomProgressDialog(getActivity());
		mOptionsForImageSelectionList = Arrays.asList(getResources().getStringArray(R.array.upload_picker_action));

		if (ProfileInfoCacheManager.isAccountVerified()) {
			mOfficeInfoEditButton.setVisibility(View.GONE);
			mContactInfoEditButton.setVisibility(View.GONE);
		} else {
			mOfficeInfoEditButton.setVisibility(View.VISIBLE);
			mContactInfoEditButton.setVisibility(View.VISIBLE);
		}

		mOfficeInfoEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.UPDATE_BUSINESS_INFO)
			public void onClick(View v) {
				if (!Utilities.isConnectionAvailable(getContext())) {
					Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					launchEditBusinessInformationFragment();
				}
			}
		});

		mContactInfoEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.UPDATE_BUSINESS_INFO)
			public void onClick(View v) {
				if (!Utilities.isConnectionAvailable(getContext())) {
					Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					launchEditContactInformationFragment();
				}
			}
		});

		setHasOptionsMenu(true);
		setButtonActions();

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setContentShown(false);
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_INFO)) {
			mBusinessInfoServiceNotAllowedTextView.setVisibility(View.VISIBLE);
			mBusinessInformationViewHolder.setVisibility(View.GONE);
			mPresentAddressHolder.setVisibility(View.GONE);
		} else {
			getProfileInfo();
			getBusinessInformation();

		}
	}


	private void loadAddresses() {
		if (mPresentAddress == null) {
			mPresentAddressView.setVisibility(View.GONE);
		} else {
			mPresentAddressHolder.setVisibility(View.VISIBLE);
			mPresentAddressView.setText(mPresentAddress.toString(mThanaList, mDistrictList));
		}

		final Bundle presentAddressBundle = new Bundle();
		presentAddressBundle.putString(Constants.ADDRESS_TYPE, Constants.ADDRESS_TYPE_PRESENT);
		if (mPresentAddress != null) {
			presentAddressBundle.putSerializable(Constants.ADDRESS, mPresentAddress);
		}
		mPresentAddressEditButton.setVisibility(View.VISIBLE);

		mPresentAddressEditButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.MANAGE_ADDRESS)
			public void onClick(View v) {
				if (!Utilities.isConnectionAvailable(getContext())) {
					Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					((ProfileActivity) getActivity()).switchToEditAddressFragment(presentAddressBundle);
				}
			}
		});
	}

	private void getThanaList() {
		mGetThanaListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_THANA_LIST,
				new ThanaRequestBuilder().getGeneratedUri(), getActivity(), this, false);
		mGetThanaListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getDistrictList() {
		mGetDistrictListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_DISTRICT_LIST,
				new DistrictRequestBuilder().getGeneratedUri(), getActivity(), this, false);
		mGetDistrictListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getUserAddress() {
		if (mGetUserAddressTask != null) {
			return;
		}

		mGetUserAddressTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_ADDRESS_REQUEST,
				Constants.BASE_URL_MM + Constants.URL_GET_USER_ADDRESS_REQUEST, getActivity(), this, false);
		mGetUserAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}


	private void launchEditContactInformationFragment() {
		Bundle bundle = new Bundle();

		bundle.putString(Constants.MOBILE_NUMBER, mMobileNumber);
		bundle.putString(Constants.NAME, mName);
		bundle.putString(Constants.DATE_OF_BIRTH, mDateOfBirth);
		bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
		bundle.putString(Constants.GENDER, mGender);
		bundle.putInt(Constants.OCCUPATION, mOccupation);
		bundle.putString(Constants.ORGANIZATION_NAME, mOrganizationName);
		bundle.putParcelableArrayList(Constants.OCCUPATION_LIST, new ArrayList<>(mOccupationList));
		((ProfileActivity) getActivity()).switchToEditBasicInfoFragment(bundle);
	}

	private void setButtonActions() {
		mBusinessContactProfilePictureHolderView.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(ServiceIdConstants.MANAGE_PROFILE_PICTURE)
			public void onClick(View v) {
				if (ProfileInfoCacheManager.isAccountVerified()) {
					DialogUtils.showProfilePictureUpdateRestrictionDialog(getContext());
				} else {
					initProfilePicHelperDialog();
					photoSelectionHelperDialog.show();
				}
			}
		});
	}

	private void setProfileInformation() {
		mMobileNumberView.setText(getString(R.string.phone_number) + ": " + mMobileNumber);
		mNameView.setText(getString(R.string.name) + ": " + mName);
		mSignUpTimeView.setText(getString(R.string.member_since) + ": " + mSignUpTime);

		if (mOrganizationName != null && !mOrganizationName.isEmpty())
			mOrganizationNameView.setText(getString(R.string.organization_name) + ": " + mOrganizationName);
		else
			mOrganizationNameView.setText(getString(R.string.organization_name) + ": " + getString(R.string.not_available));

		if (mVerificationStatus != null) {
			if (mVerificationStatus.equals(Constants.ACCOUNT_VERIFICATION_STATUS_VERIFIED)) {
				mVerificationStatusView.setBackgroundResource(R.drawable.background_verified);
				mVerificationStatusView.setText(R.string.verified);
			} else {
				mVerificationStatusView.setBackgroundResource(R.drawable.background_not_verified);
				mVerificationStatusView.setText(R.string.unverified);
			}
		}

		mBusinessContactProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER +
				mBusinessContactProfilePictureUrl, false);

		ProfileInfoCacheManager.updateProfileInfoCache(mGetProfileInfoResponse);
	}

	private void initProfilePicHelperDialog() {
		if (!ProfileInfoCacheManager.isAccountVerified()) {
			photoSelectionHelperDialog = new PhotoSelectionHelperDialog(getActivity(), getString(R.string.select_an_image),
					mOptionsForImageSelectionList, Constants.TYPE_PROFILE_PICTURE);
			photoSelectionHelperDialog.setOnResourceSelectedListener(new PhotoSelectionHelperDialog.OnResourceSelectedListener() {
				@Override
				public void onResourceSelected(int mActionId, String action) {
					if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS)) {
						selectProfilePictureIntent(mActionId);
					} else {
						mSelectedOptionForImage = mActionId;
						Utilities.requestRequiredPermissions(BusinessInformationFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
					}
				}
			});
		}
	}

	private void selectProfilePictureIntent(int id) {
		Intent imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document),
				id, Constants.CAMERA_FRONT, getString(R.string.profile_picture_temp_file));
		startActivityForResult(imagePickerIntent, ACTION_PICK_PROFILE_PICTURE);
	}

	private boolean isSelectedProfilePictureValid(Uri uri) {
		String selectedImagePath = uri.getPath();
		String result = null;

		try {
			result = CameraAndImageUtilities.validateProfilePicture(getActivity(), selectedImagePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (result == null) {
			return true;
		} else {
			String errorMessage;
			switch (result) {
				case CameraAndImageUtilities.NO_FACE_DETECTED:
					errorMessage = getString(R.string.no_face_detected);
					break;
				case CameraAndImageUtilities.VALID_PROFILE_PICTURE:
					return true;
				case CameraAndImageUtilities.MULTIPLE_FACES:
					errorMessage = getString(R.string.multiple_face_detected);
					break;
				case CameraAndImageUtilities.NOT_AN_IMAGE:
					errorMessage = getString(R.string.not_an_image);
					break;
				default:
					errorMessage = getString(R.string.default_profile_pic_inappropriate_message);
					break;
			}

			showProfilePictureErrorDialog(errorMessage);
			return false;
		}
	}

	private void showProfilePictureErrorDialog(String content) {
		mProfilePictureErrorDialogBuilder = new MaterialDialog.Builder(getActivity())
				.title(R.string.attention)
				.content(content)
				.cancelable(true)
				.positiveText(R.string.try_again)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
						photoSelectionHelperDialog.show();
					}
				});
		mProfilePictureErrorDialog = mProfilePictureErrorDialogBuilder.build();
		mProfilePictureErrorDialog.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_PERMISSION:
				if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS)) {
					selectProfilePictureIntent(mSelectedOptionForImage);
				} else {
					Toast.makeText(getActivity(), R.string.prompt_grant_permission, Toast.LENGTH_LONG).show();
				}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ACTION_PICK_PROFILE_PICTURE:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = DocumentPicker.getDocumentFromResult(getActivity(), resultCode, data, "profile_picture.jpg");
					if (uri == null) {
						if (getActivity() != null)
							Toast.makeText(getActivity(),
									R.string.could_not_load_image,
									Toast.LENGTH_SHORT).show();
					} else {
						// Check for a valid profile picture
						if (isSelectedProfilePictureValid(uri)) {
							mBusinessContactProfilePictureView.setProfilePicture(uri.getPath(), true);
							updateProfilePicture(uri);
						}
					}
				} else if (resultCode == CameraActivity.CAMERA_ACTIVITY_CRASHED) {
					Intent systemCameraOpenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					systemCameraOpenIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(),
							BuildConfig.APPLICATION_ID, DocumentPicker.getTempFile(getActivity(), getString(R.string.profile_picture_temp_file))));
					startActivityForResult(systemCameraOpenIntent, ACTION_PICK_PROFILE_PICTURE);
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void getProfileInfo() {
		if (mGetProfileInfoTask != null) {
			return;
		}
		mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
				Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), this, false);
		mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getOccupationList() {
		if (mGetOccupationTask != null) {
			return;
		}

		mGetOccupationTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_OCCUPATIONS_REQUEST,
				new OccupationRequestBuilder().getGeneratedUri(), getActivity(), this, false);
		mGetOccupationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void launchEditBusinessInformationFragment() {
		Bundle bundle = new Bundle();

		if (mGetBusinessInformationResponse == null || mBusinessTypes == null) {
			Toast.makeText(getActivity(), R.string.please_wait_until_information_loading, Toast.LENGTH_LONG).show();
			return;
		}

		bundle.putString(Constants.BUSINESS_NAME, mGetBusinessInformationResponse.getBusinessName());

		System.out.println("Company Name " + mGetBusinessInformationResponse.getCompanyName());

		bundle.putString(Constants.COMPANY_NAME, mGetBusinessInformationResponse.getCompanyName());
		bundle.putString(Constants.BUSINESS_MOBILE_NUMBER, mGetBusinessInformationResponse.getMobileNumber());
		bundle.putInt(Constants.BUSINESS_TYPE, mGetBusinessInformationResponse.getBusinessType());
		bundle.putParcelableArrayList(Constants.BUSINESS_TYPE_LIST, new ArrayList<>(mBusinessTypes));

		((ProfileActivity) getActivity()).switchToEditBusinessInformationFragment(bundle);
	}

	private void getBusinessInformation() {
		if (mGetBusinessInformationAsyncTask != null)
			return;

		mGetBusinessInformationAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_INFORMATION,
				Constants.BASE_URL_MM + Constants.URL_GET_BUSINESS_INFORMATION, getActivity(), this, false);
		mGetBusinessInformationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void processBusinessInformationResponse() {
		mBusinessNameView.setText(mGetBusinessInformationResponse.getBusinessName());
		mCompanyNameView.setText(mGetBusinessInformationResponse.getCompanyName());
		mBusinessMobileNumberView.setText(getString(R.string.phone_number) + ": " + mGetBusinessInformationResponse.getMobileNumber());
		mBusinessTypeView.setText(R.string.loading);

		// Load business types, then extract the name of the business type from businessTypeId
		mGetBusinessTypesAsyncTask = new GetBusinessTypesAsyncTask(getActivity(), new GetBusinessTypesAsyncTask.BusinessTypeLoadListener() {
			@Override
			public void onLoadSuccess(List<BusinessType> businessTypes) {
				mBusinessTypes = businessTypes;

				for (BusinessType businessType : businessTypes) {
					if (businessType.getId() == mGetBusinessInformationResponse.getBusinessType())
						mBusinessTypeView.setText(businessType.getName());
				}
			}

			@Override
			public void onLoadFailed() {
				mBusinessTypeView.setText(R.string.failed_loading_business_type);
			}
		});
		mGetBusinessTypesAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		ProfileInfoCacheManager.updateBusinessInfoCache(mGetBusinessInformationResponse);
		ProfileInfoCacheManager.saveMainUserBusinessInfo(Utilities.getMainBusinessProfileInfoString(mGetBusinessInformationResponse));
	}

	private void processProfileInfoResponse() {
		try {

			if (mGetProfileInfoResponse.getName() != null)
				mName = mGetProfileInfoResponse.getName();
			if (mGetProfileInfoResponse.getMobileNumber() != null)
				mMobileNumber = mGetProfileInfoResponse.getMobileNumber();

			if (mGetProfileInfoResponse.getDob() != null)
				mDateOfBirth = mGetProfileInfoResponse.getDob();

			if (mGetProfileInfoResponse.getGender() != null)
				mGender = mGetProfileInfoResponse.getGender();

			if (mGetProfileInfoResponse.getSignupTimeFormatted() != null) {
				mSignUpTime = mGetProfileInfoResponse.getSignupTimeFormatted();
			}

			mOccupation = mGetProfileInfoResponse.getOccupation();
			mOrganizationName = mGetProfileInfoResponse.getOrganizationName();
			mVerificationStatus = mGetProfileInfoResponse.getVerificationStatus();

			mProfileImageUrl = Utilities.getImage(mGetProfileInfoResponse.getProfilePictures(), Constants.IMAGE_QUALITY_MEDIUM);
			mBusinessContactProfilePictureUrl = Utilities.getImage(mGetProfileInfoResponse.getBusinessContactProfilePictures(), Constants.IMAGE_QUALITY_MEDIUM);

			setProfileInformation();
			getOccupationList();
			getDistrictList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateProfilePicture(Uri selectedImageUri) {
		mProgressDialog.show();

		mSelectedImagePath = selectedImageUri.getPath();

		mUploadBusinessContactProfilePictureAsyncTask = new UploadProfilePictureAsyncTask(Constants.COMMAND_SET_BUSINESS_CONTACT_PROFILE_PICTURE,
				Constants.URL_SET_BUSINESS_CONTACT_PROFILE_PICTURE, mSelectedImagePath, getActivity());
		mUploadBusinessContactProfilePictureAsyncTask.mHttpResponseListener = this;
		mUploadBusinessContactProfilePictureAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() != null) {
			mProgressDialog.dismiss();
		}
		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mGetBusinessInformationAsyncTask = null;
			mGetProfileInfoTask = null;
			mGetOccupationTask = null;
			mGetUserAddressTask = null;
			mGetDistrictListAsyncTask = null;
			mGetThanaListAsyncTask = null;
			setContentShown(true);
			return;
		}

		Gson gson = new Gson();

		switch (result.getApiCommand()) {
			case Constants.COMMAND_GET_BUSINESS_INFORMATION:
				try {
					mGetBusinessInformationResponse = gson.fromJson(result.getJsonString(), GetBusinessInformationResponse.class);

					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						processBusinessInformationResponse();
					} else {
						if (getActivity() != null) {
							Toaster.makeText(getActivity(), R.string.failed_loading_business_information, Toast.LENGTH_LONG);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();

					if (getActivity() != null) {
						Toaster.makeText(getActivity(), R.string.failed_loading_business_information, Toast.LENGTH_LONG);
					}
				}

				mGetBusinessInformationAsyncTask = null;
				break;

			case Constants.COMMAND_GET_OCCUPATIONS_REQUEST:
				try {
					mGetOccupationResponse = gson.fromJson(result.getJsonString(), GetOccupationResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mOccupationList = mGetOccupationResponse.getOccupations();
						String occupation = mGetOccupationResponse.getOccupation(mOccupation);
						if (occupation != null)
							mOccupationView.setText(getString(R.string.occupation) + ": " + occupation);
						else
							mOccupationView.setText(getString(R.string.occupation) + ": " + getString(R.string.not_available));
					} else {
						mOccupationView.setText(getString(R.string.occupation) + ": " + getString(R.string.not_available));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mOccupationView.setText(getString(R.string.occupation) + ": " + getString(R.string.not_available));
				}

				mGetOccupationTask = null;
				break;

			case Constants.COMMAND_GET_PROFILE_INFO_REQUEST:
				try {
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mGetProfileInfoResponse = gson.fromJson(result.getJsonString(), GetProfileInfoResponse.class);
						processProfileInfoResponse();
					} else {
						if (getActivity() != null)
							Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null)
						Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
				}

				mGetProfileInfoTask = null;
				break;

			case Constants.COMMAND_SET_BUSINESS_CONTACT_PROFILE_PICTURE:
				try {
					mSetBusinessContactProfilePictureResponse = gson.fromJson(result.getJsonString(), SetProfilePictureResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						if (getActivity() != null)
							Toast.makeText(getActivity(), mSetBusinessContactProfilePictureResponse.getMessage(), Toast.LENGTH_LONG).show();

						//Google Analytic event
						if (!TextUtils.isEmpty(ProfileInfoCacheManager.getProfileImageUrl())) {
							Utilities.sendSuccessEventTracker(mTracker, "Business Contact Profile Picture", ProfileInfoCacheManager.getAccountId());
						} else {
							Utilities.sendSuccessEventTracker(mTracker, "Business Contact Profile Picture", ProfileInfoCacheManager.getAccountId());
						}

					} else {
						if (getActivity() != null)
							Toast.makeText(getActivity(), mSetBusinessContactProfilePictureResponse.getMessage(), Toast.LENGTH_SHORT).show();

						//Google Analytic event
						if (!TextUtils.isEmpty(ProfileInfoCacheManager.getProfileImageUrl())) {
							Utilities.sendFailedEventTracker(mTracker, "Business Contact Profile Picture", ProfileInfoCacheManager.getAccountId(), mSetBusinessContactProfilePictureResponse.getMessage());
						} else {
							Utilities.sendFailedEventTracker(mTracker, "Business Contact Profile Picture", ProfileInfoCacheManager.getAccountId(), mSetBusinessContactProfilePictureResponse.getMessage());
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null)
						Toaster.makeText(getActivity(), R.string.profile_picture_set_failed, Toast.LENGTH_SHORT);

					//Google Analytic event
					Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
				}

				mUploadBusinessContactProfilePictureAsyncTask = null;
				break;

			case Constants.COMMAND_GET_USER_ADDRESS_REQUEST:
				try {
					mGetUserAddressResponse = gson.fromJson(result.getJsonString(), GetUserAddressResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mPresentAddress = mGetUserAddressResponse.getPresentAddress();
						loadAddresses();
						setContentShown(true);
					} else {
						if (getActivity() != null) {
							Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
							getActivity().onBackPressed();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null) {
						Toaster.makeText(getActivity(), R.string.profile_info_fetch_failed, Toast.LENGTH_SHORT);
						getActivity().onBackPressed();
					}
				}

				mGetUserAddressTask = null;
				break;

			case Constants.COMMAND_GET_THANA_LIST:
				try {
					mGetThanaResponse = gson.fromJson(result.getJsonString(), GetThanaResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mThanaList = mGetThanaResponse.getThanas();
						if (ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_ADDRESSES)) {
							getUserAddress();
						} else {
							mBusinessAddressServiceNotAllowedTextView.setVisibility(View.VISIBLE);
							mBusinessAddressViewHolder.setVisibility(View.GONE);
							setContentShown(true);
						}

					} else {
						if (getActivity() != null) {
							Toaster.makeText(getActivity(), R.string.failed_loading_thana_list, Toast.LENGTH_LONG);
							getActivity().onBackPressed();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null) {
						Toaster.makeText(getActivity(), R.string.failed_loading_thana_list, Toast.LENGTH_LONG);
						getActivity().onBackPressed();
					}
				}

				mGetThanaListAsyncTask = null;
				break;

			case Constants.COMMAND_GET_DISTRICT_LIST:
				try {
					mGetDistrictResponse = gson.fromJson(result.getJsonString(), GetDistrictResponse.class);

					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mDistrictList = mGetDistrictResponse.getDistricts();
						getThanaList();

					} else {
						if (getActivity() != null) {
							Toaster.makeText(getActivity(), R.string.failed_loading_district_list, Toast.LENGTH_LONG);
							getActivity().onBackPressed();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (getActivity() != null) {
						Toaster.makeText(getActivity(), R.string.failed_loading_district_list, Toast.LENGTH_LONG);
						getActivity().onBackPressed();
					}
				}

				mGetDistrictListAsyncTask = null;
				break;
		}
	}
}
