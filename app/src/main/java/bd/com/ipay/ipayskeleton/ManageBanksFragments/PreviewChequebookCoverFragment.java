package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;

import java.io.File;
import java.util.Arrays;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadChequebookCoverAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomUploadPickerDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.UploadDocumentResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;

public class PreviewChequebookCoverFragment extends BaseFragment implements HttpResponseListener {

    private static final int ACTION_UPLOAD_CHEQUEBOOK_COVER = 100;
    private static final int REQUEST_CODE_PERMISSION = 1001;

    private BankAccountList mSelectedChequebookCover;
    private TextView mBankName;
    private TextView mBankAccountNumber;
    private TextView mBranchName;
    private ImageView bankIcon;

    private ImageView mChequebookCoverImageView;
    private int mPickerActionId;
    private File mChequebookCoverImageFile;
    private Button mChequebookCoverSelectorButton;
    private ChequebookCoverSelectorButtonClickListener chequebookCoverSelectorButtonClickListener;
    private Button mAgreeButton;

    private UploadChequebookCoverAsyncTask mUploadCheckbookCovorAsyncTask;
    private CustomProgressDialog mProgressDialog;
    private String[] mImageUrl;
    private View uploadImageView;
    private TextView mChequebookCoverPageErrorTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedChequebookCover = getArguments().getParcelable(Constants.SELECTED_CHEQUEBOOK_COVER);
        }
        mProgressDialog = new CustomProgressDialog(getContext());


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_chequebook_cover, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mSelectedChequebookCover != null)
            getActivity().setTitle(mSelectedChequebookCover.getBankName());

        mBankAccountNumber = (TextView) findViewById(R.id.bank_account_number);
        mBankName = (TextView) findViewById(R.id.bank_name);
        mBranchName = (TextView) findViewById(R.id.bank_branch_name);
        bankIcon = (ImageView) findViewById(R.id.portrait);

        chequebookCoverSelectorButtonClickListener = new ChequebookCoverSelectorButtonClickListener();
        mChequebookCoverSelectorButton = (Button) findViewById(R.id.chequebook_cover_selector_button);
        mChequebookCoverImageView = (ImageView) findViewById(R.id.document_image_view);
        mAgreeButton = (Button) view.findViewById(R.id.button_add_bank);
        mChequebookCoverSelectorButton.setOnClickListener(chequebookCoverSelectorButtonClickListener);
        uploadImageView = findViewById(R.id.chequebook_cover_upload_option_view_holder);
        mChequebookCoverPageErrorTextView = findViewById(R.id.chequebook_cover_error_text_view);
        mChequebookCoverPageErrorTextView.setVisibility(View.INVISIBLE);

        if (mSelectedChequebookCover != null) {
            mBankName.setText(mSelectedChequebookCover.getBankName());
            mBranchName.setText(mSelectedChequebookCover.getBranchName());
            mBankAccountNumber.setText(mSelectedChequebookCover.getAccountNumber());
            Drawable icon = getResources().getDrawable(mSelectedChequebookCover.getBankIcon(getContext()));
            bankIcon.setImageDrawable(icon);
            mChequebookCoverImageView.setImageResource(R.drawable.cheque);
            if (mSelectedChequebookCover.getBankDocuments() != null && mSelectedChequebookCover.getBankDocuments().size() > 0) {
                if (!TextUtils.isEmpty(mSelectedChequebookCover.getBankDocuments().get(0).getDocumentPages().get(0).getUrl())) {
                    Glide.with(getContext()).load(Constants.BASE_URL_FTP_SERVER + mSelectedChequebookCover.getBankDocuments().get(0).getDocumentPages().get(0).getUrl())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                    return false;
                                }
                            }).crossFade().into(mChequebookCoverImageView);
                }

                if (mSelectedChequebookCover.getBankDocuments().get(0).getDocumentVerificationStatus().equalsIgnoreCase("VERIFIED")) {
                    uploadImageView.setVisibility(View.GONE);
                } else {
                    uploadImageView.setVisibility(View.VISIBLE);
                }
            }


            mAgreeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (verifyUserInputs()) {
                        performIdentificationDocumentUpload();
                    }
                }
            });
        }
    }


    @SuppressLint("StringFormatInvalid")
    private boolean verifyUserInputs() {
        clearAllErrorMessages();
        final boolean isValidInput;
        final View focusableView;
        if (mChequebookCoverImageFile == null) {
            mChequebookCoverPageErrorTextView.setText(R.string.please_select_a_file_to_upload);
            mChequebookCoverPageErrorTextView.setVisibility(View.VISIBLE);
            focusableView = null;
            isValidInput = false;
        } else if (mChequebookCoverImageFile != null && mChequebookCoverImageFile.length() > Constants.MAX_FILE_BYTE_SIZE) {
            mChequebookCoverPageErrorTextView.setText(getString(R.string.please_select_max_file_size_message, Constants.MAX_FILE_MB_SIZE));
            mChequebookCoverPageErrorTextView.setVisibility(View.VISIBLE);
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
        mChequebookCoverPageErrorTextView.setText("");
        mChequebookCoverPageErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void performIdentificationDocumentUpload() {
        final String url;
        url = Constants.BASE_URL_MM + Constants.URL_CHECKBOOK_COVOR_UPLOAD;
        mImageUrl = getUploadFilePaths();
        mUploadCheckbookCovorAsyncTask = new UploadChequebookCoverAsyncTask(Constants.COMMAND_UPLOAD_DOCUMENT, url, getContext(), "cheque", mImageUrl, PreviewChequebookCoverFragment.this, mSelectedChequebookCover.getBankAccountId());
        mUploadCheckbookCovorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mProgressDialog.show();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ACTION_UPLOAD_CHEQUEBOOK_COVER:
                if (resultCode == Activity.RESULT_OK) {
                    performFileSelectAction(resultCode, intent);
                } else if (resultCode == CameraActivity.CAMERA_ACTIVITY_CRASHED) {
                    Intent systemCameraOpenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    systemCameraOpenIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID, DocumentPicker.getTempFile(getActivity(), "checkbook_front.jpg")));
                    startActivityForResult(systemCameraOpenIntent, ACTION_UPLOAD_CHEQUEBOOK_COVER);
                } else {
                    mPickerActionId = -1;
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private void performFileSelectAction(int resultCode, Intent intent) {
        String filePath = DocumentPicker.getFilePathFromResult(getActivity(), intent);

        if (filePath != null) {
            String type = filePath.substring(filePath.lastIndexOf(".") + 1);

            if(type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("png")
                    || type.equalsIgnoreCase("jpeg")) {

                String[] temp = filePath.split(File.separator);
                final String mFileName = temp[temp.length - 1];

                Uri mSelectedDocumentUri = DocumentPicker.getDocumentFromResult(getActivity(), resultCode, intent, mFileName);
                final File imageFile = new File(mSelectedDocumentUri.getPath());
                final Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                if (mChequebookCoverImageView != null) {
                    mChequebookCoverImageView.setImageBitmap(imageBitmap);
                }
                mChequebookCoverPageErrorTextView.setText("");
                mChequebookCoverPageErrorTextView.setVisibility(View.INVISIBLE);
                mChequebookCoverImageFile = imageFile;
                mPickerActionId = -1;

            }else {
                Toaster.makeText(getActivity(), R.string.invalid_image_type, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                    selectDocument(mPickerActionId);
                else
                    Toast.makeText(getActivity(), R.string.prompt_grant_permission, Toast.LENGTH_LONG).show();
        }
    }

    private void selectDocument(int actionId) {
        mPickerActionId = actionId;
        Intent imagePickerIntent = DocumentPicker.getPickerIntentByID(getActivity(), getString(R.string.select_a_document), actionId, Constants.CAMERA_REAR, "checkbook_front.jpg");
        startActivityForResult(imagePickerIntent, ACTION_UPLOAD_CHEQUEBOOK_COVER);
    }

    private String[] getUploadFilePaths() {
        final String[] files;
        files = new String[1];
        files[0] = mChequebookCoverImageFile.getAbsolutePath();
        return files;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_UPLOAD_DOCUMENT)) {

            mProgressDialog.dismiss();

            UploadDocumentResponse uploadDocumentResponse = gson.fromJson(result.getJsonString(), UploadDocumentResponse.class);
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), getString(R.string.cheque_uploaded), Toast.LENGTH_LONG);
            } else {
                mProgressDialog.dismiss();
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), uploadDocumentResponse.getMessage(), Toast.LENGTH_SHORT);
            }


            ((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
        }
    }


    class ChequebookCoverSelectorButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (isAdded()) {
                CustomUploadPickerDialog customUploadPickerDialog = new CustomUploadPickerDialog(getActivity(),
                        getActivity().getString(R.string.select_a_document),
                        Arrays.asList(getResources().getStringArray(R.array.upload_picker_action)));
                customUploadPickerDialog.setOnResourceSelectedListener(new CustomUploadPickerDialog.OnResourceSelectedListener() {
                    @Override
                    public void onResourceSelected(int actionId, String action) {
                        if (Constants.ACTION_TYPE_TAKE_PICTURE.equals(action) || Constants.ACTION_TYPE_SELECT_FROM_GALLERY.equals(action))
                            if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                                selectDocument(actionId);
                            else {
                                mPickerActionId = actionId;
                                Utilities.requestRequiredPermissions(PreviewChequebookCoverFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                            }
                    }
                });
                customUploadPickerDialog.show();
            }
        }
    }
}
