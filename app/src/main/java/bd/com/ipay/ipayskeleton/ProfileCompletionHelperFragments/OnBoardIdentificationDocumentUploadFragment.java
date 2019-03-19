package bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Arrays;

import bd.com.ipay.ipayskeleton.Activities.ProfileVerificationHelperActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadMultipleIdentifierDocumentAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomUploadPickerDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.IdentificationDocument;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.UploadDocumentResponse;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments.UploadIdentificationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.IdentificationDocumentConstants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;

public class OnBoardIdentificationDocumentUploadFragment extends BaseFragment implements HttpResponseListener {

    private static final int ACTION_UPLOAD_DOCUMENT = 100;
    private static final int REQUEST_CODE_PERMISSION = 1001;

    private UploadMultipleIdentifierDocumentAsyncTask mUploadIdentifierDocumentAsyncTask;

    private IdentificationDocument mSelectedIdentificationDocument;

    private EditText mDocumentNameEditText;
    private EditText mDocumentIdEditText;

    private ImageView mDocumentFrontSideImageView;
    private ImageView mDocumentBackSideImageView;

    private View documentBackSideUploadOptionViewHolder;

    private TextView mDocumentFirstPageErrorTextView;
    private TextView mDocumentSecondPageErrorTextView;

    private int maxDocumentSideCount;
    private boolean mIsOtherTypeDocument;
    private String mDocumentIdEditTextHint;

    private int mPickerActionId;
    private String mSelectedDocumentSide;

    private File mDocumentFirstPageImageFile;
    private File mDocumentSecondPageImageFile;

    private CustomProgressDialog mProgressDialog;

    private ImageView mBackButtonTop;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedIdentificationDocument = getArguments().getParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT);
            if (mSelectedIdentificationDocument != null) {
                maxDocumentSideCount = IdentificationDocumentConstants.getMaxDocumentPageCount(mSelectedIdentificationDocument.getDocumentType());
                mIsOtherTypeDocument = mSelectedIdentificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_OTHER);
                mDocumentIdEditTextHint = getString(IdentificationDocumentConstants.getDocumentIDHintText(mSelectedIdentificationDocument.getDocumentType()));
            }
        }
        mProgressDialog = new CustomProgressDialog(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboard_identification_document_upload, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.upload_document);

        final View documentNameViewHolder = findViewById(R.id.document_name_view_holder);
        final TextInputLayout documentIdTextInputLayout = findViewById(R.id.document_id_text_input_layout);
        final DocumentChooserButtonClickListener documentChooserButtonClickListener = new DocumentChooserButtonClickListener();
        final Button documentFrontSideSelectorButton = findViewById(R.id.document_front_side_selector_button);
        final Button uploadButton = findViewById(R.id.upload_button);
        final Button documentBackSideSelectorButton = findViewById(R.id.document_back_side_selector_button);

        mDocumentFrontSideImageView = findViewById(R.id.document_front_side_image_view);
        mDocumentFirstPageErrorTextView = findViewById(R.id.document_first_page_error_text_view);
        mDocumentSecondPageErrorTextView = findViewById(R.id.document_second_page_error_text_view);
        documentBackSideUploadOptionViewHolder = findViewById(R.id.document_back_side_upload_option_view_holder);
        mDocumentNameEditText = findViewById(R.id.document_name_edit_text);
        mDocumentIdEditText = findViewById(R.id.document_id_edit_text);
        mDocumentBackSideImageView = findViewById(R.id.document_back_side_image_view);

        Utilities.setAppropriateKeyboard(getContext(), mSelectedIdentificationDocument.getDocumentType(), mDocumentIdEditText);

        if (mIsOtherTypeDocument) {
            documentNameViewHolder.setVisibility(View.VISIBLE);
            mDocumentNameEditText.setText(mSelectedIdentificationDocument.getDocumentName());
        } else {
            documentNameViewHolder.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mDocumentIdEditTextHint))
            documentIdTextInputLayout.setHint(mDocumentIdEditTextHint);
        if (mSelectedIdentificationDocument != null) {
            if (!TextUtils.isEmpty(mSelectedIdentificationDocument.getDocumentIdNumber())) {
                mDocumentIdEditText.setText(mSelectedIdentificationDocument.getDocumentIdNumber());
            }
        }

        mDocumentFirstPageErrorTextView.setVisibility(View.INVISIBLE);

        documentFrontSideSelectorButton.setOnClickListener(documentChooserButtonClickListener);
        documentBackSideSelectorButton.setOnClickListener(documentChooserButtonClickListener);
        mDocumentFrontSideImageView.setOnClickListener(documentChooserButtonClickListener);
        mDocumentBackSideImageView.setOnClickListener(documentChooserButtonClickListener);

        documentBackSideUploadOptionViewHolder.setVisibility(View.GONE);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    performIdentificationDocumentUpload();
                }
            }
        });

        mBackButtonTop = view.findViewById(R.id.back);
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            mBackButtonTop.setVisibility(View.INVISIBLE);
        }

        mBackButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        if(!TextUtils.isEmpty(BulkSignupUserDetailsCacheManager.getNid(null))){
            final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                    getString(R.string.bulk_signup_nid_helper_msg));

            bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mDocumentIdEditText.setText(BulkSignupUserDetailsCacheManager.getNid(null));
                    bulkSignUpHelperDialog.cancel();
                }
            });

            bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bulkSignUpHelperDialog.cancel();
                }
            });

            bulkSignUpHelperDialog.show();
        }

    }

    private void performIdentificationDocumentUpload() {
        final String url;
        final String documentName;
        final String documentIdNumber = mDocumentIdEditText.getText().toString();
        final String documentType = mSelectedIdentificationDocument.getDocumentType();
        final String[] files = getUploadFilePaths();

        url = Constants.BASE_URL_MM + Constants.URL_UPLOAD_DOCUMENTS_V2;

        if (mSelectedIdentificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_OTHER)) {
            documentName = mDocumentNameEditText.getText().toString();
        } else {
            documentName = null;
        }

        mUploadIdentifierDocumentAsyncTask = new UploadMultipleIdentifierDocumentAsyncTask(Constants.COMMAND_UPLOAD_DOCUMENT, url, getContext(), documentType, documentIdNumber, documentName, files, OnBoardIdentificationDocumentUploadFragment.this);
        mUploadIdentifierDocumentAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mProgressDialog.show();
    }

    private String[] getUploadFilePaths() {
        final String[] files;
        if (mDocumentSecondPageImageFile == null) {
            files = new String[1];
        } else {
            files = new String[2];
            files[1] = mDocumentSecondPageImageFile.getAbsolutePath();
        }
        files[0] = mDocumentFirstPageImageFile.getAbsolutePath();
        return files;
    }

    private boolean verifyUserInputs() {
        clearAllErrorMessages();
        final boolean isValidInput;
        final View focusableView;
        if (mSelectedIdentificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_OTHER) && TextUtils.isEmpty(mDocumentNameEditText.getText())) {
            mDocumentNameEditText.setError(getString(R.string.please_enter_a_document_name));
            focusableView = mDocumentNameEditText;
            isValidInput = false;
        } else if (TextUtils.isEmpty(mDocumentIdEditText.getText())) {
            mDocumentIdEditText.setError(getString(R.string.please_enter_a_document_id));
            focusableView = mDocumentIdEditText;
            isValidInput = false;
        } else if (InputValidator.isValidDocumentID(getContext(), mDocumentIdEditText.getText().toString(), mSelectedIdentificationDocument.getDocumentType()) != null) {
            mDocumentIdEditText.setError(InputValidator.isValidDocumentID(getContext(), mDocumentIdEditText.getText().toString(), mSelectedIdentificationDocument.getDocumentType()));
            focusableView = mDocumentIdEditText;
            isValidInput = false;
        } else if (mDocumentFirstPageImageFile == null) {
            mDocumentFirstPageErrorTextView.setText(R.string.please_select_a_file_to_upload);
            mDocumentFirstPageErrorTextView.setVisibility(View.VISIBLE);
            focusableView = null;
            isValidInput = false;
        } else if (!mSelectedIdentificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_OTHER) && maxDocumentSideCount > 1 && mDocumentSecondPageImageFile == null) {
            mDocumentSecondPageErrorTextView.setText(R.string.please_select_a_file_to_upload);
            mDocumentSecondPageErrorTextView.setVisibility(View.VISIBLE);
            focusableView = null;
            isValidInput = false;
        } else {
            focusableView = null;
            isValidInput = true;
        }
        if (focusableView != null) {
            focusableView.requestFocus();
        }
        return isValidInput;
    }

    private void clearAllErrorMessages() {
        mDocumentFirstPageErrorTextView.setText("");
        mDocumentFirstPageErrorTextView.setVisibility(View.INVISIBLE);
        mDocumentSecondPageErrorTextView.setText("");
        mDocumentSecondPageErrorTextView.setVisibility(View.INVISIBLE);
        mDocumentNameEditText.setError(null);
        mDocumentIdEditText.setError(null);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ACTION_UPLOAD_DOCUMENT:
                if (resultCode == Activity.RESULT_OK) {
                    performFileSelectAction(resultCode, intent);
                } else if (resultCode == CameraActivity.CAMERA_ACTIVITY_CRASHED) {
                    Intent systemCameraOpenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    systemCameraOpenIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID, DocumentPicker.getTempFile(getActivity(), "document_" + mSelectedDocumentSide + ".jpg")));
                    startActivityForResult(systemCameraOpenIntent, ACTION_UPLOAD_DOCUMENT);
                } else {
                    mPickerActionId = -1;
                    mSelectedDocumentSide = "";
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void performFileSelectAction(int resultCode, Intent intent) {
        String filePath = DocumentPicker.getFilePathFromResult(getActivity(), intent);

        if (filePath != null) {
            String[] temp = filePath.split(File.separator);
            final String mFileName = temp[temp.length - 1];

            Uri mSelectedDocumentUri = DocumentPicker.getDocumentFromResult(getActivity(), resultCode, intent, mFileName);
            final File imageFile = new File(mSelectedDocumentUri.getPath());
            final Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (mSelectedDocumentSide.equals(IdentificationDocumentConstants.DOCUMENT_SIDE_FRONT)) {
                mDocumentFrontSideImageView.setImageBitmap(imageBitmap);
                mDocumentFirstPageImageFile = imageFile;
                if (maxDocumentSideCount > 1) {
                    documentBackSideUploadOptionViewHolder.setVisibility(View.VISIBLE);
                }
                mDocumentFirstPageErrorTextView.setText("");
                mDocumentFirstPageErrorTextView.setVisibility(View.INVISIBLE);
            } else {
                if (mDocumentBackSideImageView != null) {
                    mDocumentBackSideImageView.setImageBitmap(imageBitmap);
                }
                mDocumentSecondPageImageFile = imageFile;
                mDocumentSecondPageErrorTextView.setText("");
                mDocumentSecondPageErrorTextView.setVisibility(View.INVISIBLE);
            }
            mPickerActionId = -1;
            mSelectedDocumentSide = "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                    selectDocument(mPickerActionId, mSelectedDocumentSide);
                else
                    Toast.makeText(getActivity(), R.string.prompt_grant_permission, Toast.LENGTH_LONG).show();
        }
    }

    private void selectDocument(int actionId, String selectedDocumentSide) {
        mPickerActionId = actionId;
        mSelectedDocumentSide = selectedDocumentSide;
        Intent imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document), actionId, Constants.CAMERA_REAR, "document_" + selectedDocumentSide + ".jpg");
        startActivityForResult(imagePickerIntent, ACTION_UPLOAD_DOCUMENT);
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.cancel();
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mUploadIdentifierDocumentAsyncTask = null;
            return;
        }

        switch (result.getApiCommand()) {
            case Constants.COMMAND_UPLOAD_DOCUMENT:
                Gson gson = new GsonBuilder().create();
                UploadDocumentResponse uploadDocumentResponse = gson.fromJson(result.getJsonString(), UploadDocumentResponse.class);
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        Toast.makeText(getActivity(), uploadDocumentResponse.getMessage(), Toast.LENGTH_LONG).show();
                        ProfileInfoCacheManager.uploadIdentificationDocument(true);
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();

                        if (ProfileInfoCacheManager.isSwitchedFromSignup()) {
                            ((ProfileVerificationHelperActivity) getActivity()).switchToBasicInfoEditHelperFragment();
                        } else {
                            if (!ProfileInfoCacheManager.isBasicInfoAdded()) {
                                ((ProfileVerificationHelperActivity) getActivity()).switchToBasicInfoEditHelperFragment();
                            } else {
                                ((ProfileVerificationHelperActivity) getActivity()).switchToHomeActivity();
                            }
                        }
                        break;
                    default:
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), uploadDocumentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            default:
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class DocumentChooserButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            final String documentSide;
            switch (view.getId()) {
                case R.id.document_back_side_selector_button:
                    documentSide = IdentificationDocumentConstants.DOCUMENT_SIDE_BACK;
                    break;
                case R.id.document_front_side_selector_button:
                default:
                    documentSide = IdentificationDocumentConstants.DOCUMENT_SIDE_FRONT;
                    break;

            }

            if (isAdded()) {
                CustomUploadPickerDialog customUploadPickerDialog = new CustomUploadPickerDialog(getActivity(),
                        getActivity().getString(R.string.select_a_document),
                        Arrays.asList(getResources().getStringArray(R.array.upload_picker_action)));
                customUploadPickerDialog.setOnResourceSelectedListener(new CustomUploadPickerDialog.OnResourceSelectedListener() {
                    @Override
                    public void onResourceSelected(int actionId, String action) {
                        if (getString(R.string.take_a_picture_message).equals(action) || getString(R.string.select_from_gallery_message).equals(action))
                            if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                                selectDocument(actionId, documentSide);
                            else {
                                mPickerActionId = actionId;
                                mSelectedDocumentSide = documentSide;
                                Utilities.requestRequiredPermissions(OnBoardIdentificationDocumentUploadFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                            }
                    }
                });
                customUploadPickerDialog.show();
            }
        }
    }
}
