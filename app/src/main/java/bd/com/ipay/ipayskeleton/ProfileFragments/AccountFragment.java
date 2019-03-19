package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadProfilePictureAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.PhotoSelectionHelperDialog;
import bd.com.ipay.ipayskeleton.CustomView.IconifiedTextViewWithButton;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfilePictureResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;
import bd.com.ipay.ipayskeleton.camera.utility.CameraAndImageUtilities;

public class AccountFragment extends BaseFragment implements HttpResponseListener {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final int REQUEST_CODE_PERMISSION_PERSONAL = 1002;
    private final int ACTION_PICK_PROFILE_PICTURE = 100;

    private ProfileImageView mProfilePictureView;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mProfileCompletionStatusView;
    private ImageView mVerificationStatusView;

    private View mProfilePictureHolderView;
    private ImageView mEditProfilePicButton;

    private IconifiedTextViewWithButton mBasicInfo;
    private IconifiedTextViewWithButton mEmail;
    private IconifiedTextViewWithButton mDocuments;
    private IconifiedTextViewWithButton mIntroducer;
    private IconifiedTextViewWithButton mAddress;
    private IconifiedTextViewWithButton mProfileCompleteness;
    private View mDividerManagePeople;

    private String mName = "";
    private String mMobileNumber = "";
    private String mProfilePicture = "";
    private String mSelectedImagePath = "";

    private Uri uri;

    private List<String> mOptionsForImageSelectionList;
    private int mSelectedOptionForImage = -1;

    private UploadProfilePictureAsyncTask mUploadProfilePictureAsyncTask = null;
    private SetProfilePictureResponse mSetProfilePictureResponse;

    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private CustomProgressDialog mProgressDialog;
    private MaterialDialog.Builder mProfilePictureErrorDialogBuilder;
    private MaterialDialog mProfilePictureErrorDialog;
    private PhotoSelectionHelperDialog photoSelectionHelperDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        getActivity().setTitle(R.string.account);

        mProfilePictureView =  view.findViewById(R.id.profile_picture);
        mNameView =  view.findViewById(R.id.textview_name);
        mMobileNumberView =  view.findViewById(R.id.textview_mobile_number);
        mProfileCompletionStatusView =  view.findViewById(R.id.textview_profile_completion_status);
        mVerificationStatusView =  view.findViewById(R.id.textview_verification_status);
        mEditProfilePicButton =  view.findViewById(R.id.button_profile_picture_edit);
        mProfilePictureHolderView = view.findViewById(R.id.profile_picture_layout);

        mBasicInfo =  view.findViewById(R.id.basic_info);
        mEmail =  view.findViewById(R.id.email);
        mAddress =  view.findViewById(R.id.present_address);
        mIntroducer =  view.findViewById(R.id.introducer);
        mDocuments =  view.findViewById(R.id.documents);
        mProfileCompleteness =  view.findViewById(R.id.profile_completion);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mName = ProfileInfoCacheManager.getUserName();
        mMobileNumber = ProfileInfoCacheManager.getMobileNumber();
        mProfilePicture = ProfileInfoCacheManager.getProfileImageUrl();

        mOptionsForImageSelectionList = Arrays.asList(getResources().getStringArray(R.array.upload_picker_action));

        setProfileInformation();
        setVisibilityOfProfilePicUploadButton();
        initProfilePicHelperDialog();
        setButtonActions();
        getProfileCompletionStatus();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_user_account));
    }

    private void setVisibilityOfProfilePicUploadButton() {
        if (ProfileInfoCacheManager.isAccountVerified())
            mEditProfilePicButton.setVisibility(View.GONE);
        else
            mEditProfilePicButton.setVisibility(View.VISIBLE);
    }

    private void setButtonActions() {
        if (this.isAdded()) {
            mProfilePictureHolderView.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.MANAGE_PROFILE_PICTURE)
                public void onClick(View v) {
                    if (!ProfileInfoCacheManager.isAccountVerified()) {
                        if (ProfileInfoCacheManager.isBusinessAccount()) {
                            photoSelectionHelperDialog.show();
                        } else {
                            if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                                Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                                startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                            } else {
                                Utilities.requestRequiredPermissions(AccountFragment.this, REQUEST_CODE_PERMISSION_PERSONAL, DocumentPicker.PROFILE_PICTURE_PERMISSION);
                            }

                        }

                    } else
                        DialogUtils.showProfilePictureUpdateRestrictionDialog(getContext());
                }
            });

            mBasicInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.SEE_PROFILE)
                public void onClick(View view) {
                    if (ProfileInfoCacheManager.isBusinessAccount())
                        ((ProfileActivity) getActivity()).switchToBusinessInfoFragment();
                    else ((ProfileActivity) getActivity()).switchToBasicInfoFragment();
                }
            });

            mEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.SEE_EMAILS)
                public void onClick(View view) {
                    ((ProfileActivity) getActivity()).switchToEmailFragment();
                }
            });

            mAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.SEE_ADDRESSES)
                public void onClick(View view) {
                    ((ProfileActivity) getActivity()).switchToAddressFragment();
                }
            });

            mIntroducer.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.SEE_INTRODUCERS)
                public void onClick(View v) {
                    ((ProfileActivity) getActivity()).switchToIntroducerFragment();
                }
            });

            mDocuments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ProfileInfoCacheManager.isBusinessAccount()) {
                        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_DOCS)) {
                            DialogUtils.showServiceNotAllowedDialog(getContext());
                            return;
                        }
                    } else {
                        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_IDENTIFICATION_DOCS)) {
                            DialogUtils.showServiceNotAllowedDialog(getContext());
                            return;
                        }
                    }
                    ((ProfileActivity) getActivity()).switchToIdentificationDocumentListFragment();
                }
            });

            mProfileCompleteness.setOnClickListener(new View.OnClickListener() {
                @Override
                @ValidateAccess(ServiceIdConstants.SEE_PROFILE_COMPLETION)
                public void onClick(View view) {
                    ((ProfileActivity) getActivity()).switchToProfileCompletionFragment();
                }
            });
        }
    }


    private void initProfilePicHelperDialog() {
        if (!ProfileInfoCacheManager.isAccountVerified()) {
            if (ProfileInfoCacheManager.isBusinessAccount()) {
                photoSelectionHelperDialog = new PhotoSelectionHelperDialog(getActivity(), getString(R.string.select_an_image),
                        mOptionsForImageSelectionList, Constants.TYPE_BUSINESS_LOGO);
            } else {
                photoSelectionHelperDialog = new PhotoSelectionHelperDialog(getActivity(), getString(R.string.select_an_image),
                        mOptionsForImageSelectionList, Constants.TYPE_PROFILE_PICTURE);
            }
            photoSelectionHelperDialog.setOnResourceSelectedListener(new PhotoSelectionHelperDialog.OnResourceSelectedListener() {
                @Override
                public void onResourceSelected(int mActionId, String action) {
                    if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS)) {
                        selectProfilePictureIntent(mActionId);
                    } else {
                        mSelectedOptionForImage = mActionId;
                        Utilities.requestRequiredPermissions(AccountFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                    }
                }
            });
        }
    }

    private void selectProfilePictureIntent(int id) {
        Intent imagePickerIntent;
        if (ProfileInfoCacheManager.isBusinessAccount()) {
            imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document), id,
                    Constants.CAMERA_REAR, getString(R.string.profile_picture_temp_file));
        } else {
            imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document), id,
                    Constants.CAMERA_FRONT, getString(R.string.profile_picture_temp_file));
        }

        startActivityForResult(imagePickerIntent, ACTION_PICK_PROFILE_PICTURE);
    }

    private boolean isSelectedProfilePictureValid(Uri uri) {
        String selectedImagePath = uri.getPath();
        String result = null;

        // Business account doesn't need face detection as the profile picture can be its logo
        if (ProfileInfoCacheManager.isBusinessAccount())
            return true;

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
                        if (ProfileInfoCacheManager.isBusinessAccount()) {
                            photoSelectionHelperDialog.show();
                        } else {
                            if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                                Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                                startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                            } else {
                                Utilities.requestRequiredPermissions(AccountFragment.this, REQUEST_CODE_PERMISSION_PERSONAL, DocumentPicker.PROFILE_PICTURE_PERMISSION);
                            }

                        }
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
                break;
            case REQUEST_CODE_PERMISSION_PERSONAL:
                if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                    Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                    startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
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
                    uri = DocumentPicker.getDocumentFromResult(getActivity(), resultCode, data, "profile_picture.jpg");
                    if (uri == null) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.could_not_load_image, Toast.LENGTH_SHORT).show();
                    } else {
                        // Check for a valid profile picture
                        if (isSelectedProfilePictureValid(uri)) {
                            updateProfilePicture(uri);
                        }
                    }
                } else if (resultCode == CameraActivity.CAMERA_ACTIVITY_CRASHED) {
                    Intent systemCameraOpenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    systemCameraOpenIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID, DocumentPicker.getTempFile(getActivity(), "profile_picture.jpg")));
                    startActivityForResult(systemCameraOpenIntent, ACTION_PICK_PROFILE_PICTURE);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setProfileInformation() {
        Logger.logD("Profile Pic Account", mProfilePicture);
        mMobileNumberView.setText(mMobileNumber);
        mNameView.setText(mName);
        mProfilePictureView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER +
                mProfilePicture, true);

        if (ProfileInfoCacheManager.isAccountVerified()) {
            mVerificationStatusView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_verified_profile));
        } else {
            mVerificationStatusView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_not_verified));
        }
    }

    private void getProfileCompletionStatus() {
        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PROFILE_COMPLETION)) {
            if (mGetProfileCompletionStatusTask != null) {
                return;
            }

            mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
                    Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this, true);
            mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mProfileCompletionStatusView.setVisibility(View.GONE);
        }

    }

    private void updateProfilePicture(Uri selectedImageUri) {
        mProgressDialog.show();

        mSelectedImagePath = selectedImageUri.getPath();

        mUploadProfilePictureAsyncTask = new UploadProfilePictureAsyncTask(Constants.COMMAND_SET_PROFILE_PICTURE, Constants.URL_SET_PROFILE_PICTURE,
                mSelectedImagePath, getActivity());
        mUploadProfilePictureAsyncTask.mHttpResponseListener = this;
        mUploadProfilePictureAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() != null) {
            mProgressDialog.dismiss();
        }

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mUploadProfilePictureAsyncTask = null;
            mGetProfileCompletionStatusTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SET_PROFILE_PICTURE)) {
            try {
                mSetProfilePictureResponse = gson.fromJson(result.getJsonString(), SetProfilePictureResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mProfilePictureView.setAccountPhoto(uri.getPath(), true);
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mSetProfilePictureResponse.getMessage(), Toast.LENGTH_LONG).show();

                    getProfileCompletionStatus();

                    Intent intent = new Intent(Constants.PROFILE_PICTURE_UPDATE_BROADCAST);
                    intent.putExtra(Constants.PROFILE_PICTURE, mSelectedImagePath);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                    //Google Analytic event
                    if (!TextUtils.isEmpty(ProfileInfoCacheManager.getProfileImageUrl())) {
                        Utilities.sendSuccessEventTracker(mTracker, "Profile Picture", ProfileInfoCacheManager.getAccountId());
                    } else {
                        Utilities.sendSuccessEventTracker(mTracker, "Profile Picture", ProfileInfoCacheManager.getAccountId());
                    }

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mSetProfilePictureResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    //Google Analytic event
                    if (!TextUtils.isEmpty(ProfileInfoCacheManager.getProfileImageUrl())) {
                        Utilities.sendFailedEventTracker(mTracker, "Profile Picture", ProfileInfoCacheManager.getAccountId(), mSetProfilePictureResponse.getMessage());
                    } else {
                        Utilities.sendFailedEventTracker(mTracker, "Profile Picture", ProfileInfoCacheManager.getAccountId(), mSetProfilePictureResponse.getMessage());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.profile_picture_set_failed, Toast.LENGTH_SHORT);

                //Google Analytic event
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
            }

            mUploadProfilePictureAsyncTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS)) {
            try {
                mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                    Intent intent = new Intent(Constants.PROFILE_COMPLETION_UPDATE_BROADCAST);
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

                    if (!mProfileCompletionStatusResponse.isCompletedMandatoryFields()) {
                        mProfileCompletionStatusView.setText(getString(R.string.profile_completeness_percentage, mProfileCompletionStatusResponse.getCompletionPercentage()));
                        mProfileCompletionStatusView.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mGetProfileCompletionStatusTask = null;
        }
    }

}
