package bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.ProfileVerificationHelperActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadProfilePictureAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomUploadPickerDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.SetProfilePictureResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;
import bd.com.ipay.ipayskeleton.camera.utility.CameraAndImageUtilities;

public class OnBoardProfilePictureUploadHelperFragment extends Fragment implements HttpResponseListener {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private final int ACTION_PICK_PROFILE_PICTURE = 100;

    private SetProfilePictureResponse mSetProfilePictureResponse;
    private MaterialDialog.Builder mProfilePictureErrorDialogBuilder;
    private MaterialDialog mProfilePictureErrorDialog;
    private CustomUploadPickerDialog profilePictureHelperDialog;
    private UploadProfilePictureAsyncTask mUploadProfilePictureAsyncTask = null;

    private Button mUploadPhotoButton;
    private Button mSelectPhotoButton;
    private CustomProgressDialog mProgressDialog;
    private ProfileImageView mUploadImageView;
    private TextView mDocumentHelperTextView;

    private List<String> mOptionsForImageSelectionList;
    private int mSelectedOptionForImage = -1;
    private String mSelectedImagePath = "";
    private Uri mUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard_profile_picture, container, false);

        initializeViews(view);
        initProfilePicHelperDialog();
        setButtonActions();

        mUri = ((ProfileVerificationHelperActivity) getActivity()).mProfilePhotoUri;
        if (ProfileInfoCacheManager.isProfilePictureUploaded()) {
            mUploadImageView.setProfilePicture(mUri.getPath(), true);

        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializeViews(View view) {
        mProgressDialog = new CustomProgressDialog(getActivity());
        mUploadPhotoButton = view.findViewById(R.id.button_upload_profile_pic);
        mSelectPhotoButton = view.findViewById(R.id.button_select_profile_pic);
        mOptionsForImageSelectionList = Arrays.asList(getResources().getStringArray(R.array.upload_picker_action_for_profile_picture));
        mUploadImageView = view.findViewById(R.id.profile_image_view);
        mUploadImageView.setProfilePicture(R.drawable.ic_onboard_profile_pic_upload_helper);
        mDocumentHelperTextView = view.findViewById(R.id.profile_pic_upload_helper_title);

        if (mUri == null) {
            mSelectPhotoButton.setVisibility(View.VISIBLE);
            mUploadPhotoButton.setVisibility(View.GONE);
            mDocumentHelperTextView.setText(getString(R.string.onboard_photo_upload_title));
        } else {
            mUploadPhotoButton.setVisibility(View.VISIBLE);
            mSelectPhotoButton.setVisibility(View.GONE);
            mDocumentHelperTextView.setText(getString(R.string.onboard_nice_profile_photo));
        }
    }

    public void setButtonActions() {

        mUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ProfileInfoCacheManager.isProfilePictureUploaded()) {
                    showRepeatedPhotoSelectAlertDialog();
                } else {
                    if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                        Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                        startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                    } else {
                        Utilities.requestRequiredPermissions(OnBoardProfilePictureUploadHelperFragment.this,
                                REQUEST_CODE_PERMISSION, DocumentPicker.PROFILE_PICTURE_PERMISSION);
                    }
                }
            }
        });

        mSelectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ProfileInfoCacheManager.isProfilePictureUploaded()) {
                    showRepeatedPhotoSelectAlertDialog();
                } else {
                    if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                        Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                        startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                    } else {
                        Utilities.requestRequiredPermissions(OnBoardProfilePictureUploadHelperFragment.this,
                                REQUEST_CODE_PERMISSION, DocumentPicker.PROFILE_PICTURE_PERMISSION);
                    }
                }
            }
        });

        mUploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUri != null) {
                    updateProfilePicture(mUri);
                }
            }
        });
    }

    private void showRepeatedPhotoSelectAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.upload_profile_photo_again)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                            Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                            startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                        } else {
                            Utilities.requestRequiredPermissions(OnBoardProfilePictureUploadHelperFragment.this,
                                    REQUEST_CODE_PERMISSION, DocumentPicker.PROFILE_PICTURE_PERMISSION);
                        }
                    }
                }).show();
    }

    private void initProfilePicHelperDialog() {
        profilePictureHelperDialog = new CustomUploadPickerDialog(getActivity(), getString(R.string.select_an_image), mOptionsForImageSelectionList);
        profilePictureHelperDialog.setOnResourceSelectedListener(new CustomUploadPickerDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int mActionId, String action) {
                if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS)) {
                    selectProfilePictureIntent(mActionId);
                } else {
                    mSelectedOptionForImage = mActionId;
                    Utilities.requestRequiredPermissions(OnBoardProfilePictureUploadHelperFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                }
            }
        });
    }

    private void selectProfilePictureIntent(int id) {
        Intent imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document), id, Constants.CAMERA_FRONT, "profile_picture.jpg");
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
                        if (Utilities.isNecessaryPermissionExists(getContext(), DocumentPicker.PROFILE_PICTURE_PERMISSION)) {
                            Intent intent = DocumentPicker.createCameraIntent(getContext(), Constants.CAMERA_FRONT, "profile_picture.jpg");
                            startActivityForResult(intent, ACTION_PICK_PROFILE_PICTURE);
                        } else {
                            Utilities.requestRequiredPermissions(OnBoardProfilePictureUploadHelperFragment.this,
                                    REQUEST_CODE_PERMISSION, DocumentPicker.PROFILE_PICTURE_PERMISSION);
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
                    mUri = DocumentPicker.getDocumentFromResult(getActivity(), resultCode, data, "profile_picture.jpg");
                    if (mUri == null) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.could_not_load_image, Toast.LENGTH_SHORT).show();
                    } else {
                        // Check for a valid profile picture
                        if (isSelectedProfilePictureValid(mUri)) {
                            mUploadImageView.setProfilePicture(mUri.getPath());
                            mDocumentHelperTextView.setText("Nice Profile Photo");
                            mSelectPhotoButton.setVisibility(View.GONE);
                            mUploadPhotoButton.setVisibility(View.VISIBLE);
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
            return;
        }
        Gson gson = new Gson();
        if (result.getApiCommand().equals(Constants.COMMAND_SET_PROFILE_PICTURE)) {
            try {

                mSetProfilePictureResponse = gson.fromJson(result.getJsonString(), SetProfilePictureResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mSetProfilePictureResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    ((ProfileVerificationHelperActivity) getActivity()).mProfilePhotoUri = mUri;
                    ProfileInfoCacheManager.uploadProfilePicture(true);
                    getActivity().getSupportFragmentManager().popBackStack();
                    if (ProfileInfoCacheManager.isSwitchedFromSignup()) {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToPhotoIdUploadHelperFragment();
                    } else {
                        if (!ProfileInfoCacheManager.isIdentificationDocumentUploaded()) {
                            ((ProfileVerificationHelperActivity) getActivity()).switchToPhotoIdUploadHelperFragment();
                        } else if (!ProfileInfoCacheManager.isBasicInfoAdded()) {
                            ((ProfileVerificationHelperActivity) getActivity()).switchToBasicInfoEditHelperFragment();
                        } else {
                            ((ProfileVerificationHelperActivity) getActivity()).switchToHomeActivity();
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mSetProfilePictureResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toaster.makeText(getActivity(), R.string.profile_picture_set_failed, Toast.LENGTH_SHORT);
                }
            }

            mUploadProfilePictureAsyncTask = null;

        }


    }
}
