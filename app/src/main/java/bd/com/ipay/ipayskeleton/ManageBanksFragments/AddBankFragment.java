package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAvailableBankAsyncTask;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomUploadPickerDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.EditTextWithProgressBar;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.AccountName;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Bank;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BankBranch;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BankBranchRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.GetBankBranchesResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;
import bd.com.ipay.ipayskeleton.camera.CameraActivity;

public class AddBankFragment extends BaseFragment implements HttpResponseListener {

    private static final int ACTION_UPLOAD_CHEQUEBOOK_COVER = 100;
    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final String STARTED_FROM_PROFILE_ACTIVITY = "started_from_profile_activity";
    private HttpRequestGetAsyncTask mGetBankTask = null;
    private HttpRequestGetAsyncTask mGetBankBranchesTask = null;

    private CustomProgressDialog mProgressDialog;

    // Contains a list of bank branch corresponding to each district
    private Map<String, ArrayList<BankBranch>> bankDistrictToBranchMap;
    private ArrayList<String> mDistrictNames;
    private ArrayList<BankBranch> mBranches;
    private ArrayList<String> mBranchNames;
    private List<AccountName> accounNames = new ArrayList<>();

    private EditText mBankListSelection;
    private EditText mDistrictSelection;
    private EditText mBankBranchSelection;
    private EditText mAccountNameEditText;
    private EditText mAccountNumberEditText;
    private EditTextWithProgressBar mBankBranchEditTextProgressBar;

    private ResourceSelectorDialog<Bank> bankSelectorDialog;
    private ResourceSelectorDialog<AccountName> accountNameDialog;

    private CustomSelectorDialog districtSelectorDialog;
    private CustomSelectorDialog bankBranchSelectorDialog;

    private String mSelectedBankName;
    private int mSelectedBranchId = -1;
    private int mSelectedBankId = -1;
    private int mSelectedDistrictId = -1;

    private boolean startedFromProfileCompletion = false;
    private boolean isSwitchedFromOnBoard = false;

    private ImageView mChequebookCoverImageView;
    private int mPickerActionId;
    private File mChequebookCoverImageFile;
    private Button mChequebookCoverSelectorButton;
    private ChequebookCoverSelectorButtonClickListener chequebookCoverSelectorButtonClickListener;
    private TextView mChequebookCoverPageErrorTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_bank, container, false);
        getActivity().setTitle(R.string.link_bank);
        getActivity().setTitleColor(Color.WHITE);
        isSwitchedFromOnBoard = false;

        Bundle args = getArguments();
        if (args != null)
            startedFromProfileCompletion = args.getBoolean(STARTED_FROM_PROFILE_ACTIVITY);
        if (args != null) {
            if (args.getBoolean(Constants.FROM_ON_BOARD, false)) {
                isSwitchedFromOnBoard = args.getBoolean(Constants.FROM_ON_BOARD, false);
            }
        }

        mProgressDialog = new CustomProgressDialog(getActivity());
        mSelectedBankId = -1;
        mDistrictNames = new ArrayList<>();
        mBranches = new ArrayList<>();
        mBranchNames = new ArrayList<>();
        List<Bank> bankNames = new ArrayList<>();

        chequebookCoverSelectorButtonClickListener = new ChequebookCoverSelectorButtonClickListener();
        mChequebookCoverSelectorButton = (Button) v.findViewById(R.id.chequebook_cover_selector_button);

        mBankListSelection = (EditText) v.findViewById(R.id.default_bank_accounts);
        mDistrictSelection = (EditText) v.findViewById(R.id.branch_districts);
        mAccountNameEditText = (EditText) v.findViewById(R.id.bank_account_name);
        mAccountNumberEditText = (EditText) v.findViewById(R.id.bank_account_number);
        Button addBank = (Button) v.findViewById(R.id.button_add_bank);
        mBankBranchEditTextProgressBar = (EditTextWithProgressBar) v.findViewById(R.id.editText_with_progressBar_branch);
        mBankBranchSelection = mBankBranchEditTextProgressBar.getEditText();

        mChequebookCoverImageView = (ImageView) v.findViewById(R.id.chequebook_cover_image_view);
        mChequebookCoverSelectorButton.setOnClickListener(chequebookCoverSelectorButtonClickListener);

        mChequebookCoverPageErrorTextView = (TextView) v.findViewById(R.id.chequebook_cover_error_text_view);
        mChequebookCoverPageErrorTextView.setVisibility(View.INVISIBLE);

        if (!CommonData.isAvailableBankListLoaded()) {
            attemptRefreshAvailableBankNames();
        } else {
            bankNames.addAll(CommonData.getAvailableBanks());
            setBankAdapter(bankNames);
        }

        mAccountNameEditText.setText(ProfileInfoCacheManager.getUserName());
        if (!ProfileInfoCacheManager.isBusinessAccount()) {
            mAccountNameEditText.setFocusable(true);
        } else {
            mAccountNameEditText.setFocusable(false);
            if(!TextUtils.isEmpty(ProfileInfoCacheManager.getCompanyName()) && !ProfileInfoCacheManager.getCompanyName().trim().equalsIgnoreCase(ProfileInfoCacheManager.getUserName().trim())){
                accounNames.add(new AccountName(ProfileInfoCacheManager.getUserName()));
                accounNames.add(new AccountName(ProfileInfoCacheManager.getCompanyName()));
                setBankAccountNameAdapter(accounNames);
            }
        }

        addBank.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.MANAGE_BANK_ACCOUNTS)
            public void onClick(View v) {
                verifyUserInputs();
            }
        });

        if(!BulkSignupUserDetailsCacheManager.isBankInfoChecked(true)){
            final String cacheAccountName = BulkSignupUserDetailsCacheManager.getBankAccountName(null);
            final String cacheAccountNumber = BulkSignupUserDetailsCacheManager.getBankAccountNumber(null);

            if(!TextUtils.isEmpty(cacheAccountName) || !TextUtils.isEmpty(cacheAccountNumber)) {
                final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                        getString(R.string.bulk_signup_bank_helper_msg));

                bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAccountNameEditText.setText(cacheAccountName);
                        mAccountNumberEditText.setText(cacheAccountNumber);
                        bulkSignUpHelperDialog.cancel();
                    }
                });

                bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bulkSignUpHelperDialog.cancel();
                        bulkSignUpHelperDialog.setCheckedResponse("Bank");
                    }
                });

                bulkSignUpHelperDialog.show();
            }
        }


        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((ManageBanksActivity) getActivity()).mSelectedBankId = mSelectedBankId;
        ((ManageBanksActivity) getActivity()).mSelectedDistrictId = mSelectedDistrictId;
        ((ManageBanksActivity) getActivity()).mSelectedBranchId = mSelectedBranchId;
        ((ManageBanksActivity) getActivity()).mDistrictNames = mDistrictNames;
        ((ManageBanksActivity) getActivity()).mBranches = mBranches;
        ((ManageBanksActivity) getActivity()).mBranchNames = mBranchNames;
    }

    @Override
    public void onResume() {
        super.onResume();

        mSelectedBankId = ((ManageBanksActivity) getActivity()).mSelectedBankId;
        mSelectedDistrictId = ((ManageBanksActivity) getActivity()).mSelectedDistrictId;
        mSelectedBranchId = ((ManageBanksActivity) getActivity()).mSelectedBranchId;
        mDistrictNames = ((ManageBanksActivity) getActivity()).mDistrictNames;
        mBranches = ((ManageBanksActivity) getActivity()).mBranches;
        mBranchNames = ((ManageBanksActivity) getActivity()).mBranchNames;
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_add_bank));
    }

    private void attemptRefreshAvailableBankNames() {
        GetAvailableBankAsyncTask mGetAvailableBankAsyncTask = new GetAvailableBankAsyncTask(getActivity(),
                new GetAvailableBankAsyncTask.BankLoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        mProgressDialog.dismiss();
                        setBankAdapter(CommonData.getAvailableBanks());

                    }

                    @Override
                    public void onLoadFailed() {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), R.string.failed_available_bank_list_loading, Toast.LENGTH_LONG);
                            getActivity().finish();
                        }
                    }
                });
        mProgressDialog.show();
        mGetAvailableBankAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setBankAdapter(List<Bank> bankList) {

        bankSelectorDialog = new ResourceSelectorDialog<>(getContext(), getString(R.string.select_a_bank), bankList);
        bankSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mBankListSelection.setError(null);
                mBankListSelection.setText(name);
                mSelectedBankId = id;
                mSelectedDistrictId = -1;
                mSelectedBankName = name;
                getBankBranches(mSelectedBankId);
            }
        });

        mBankListSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankSelectorDialog.show();
            }
        });
    }

    private void setBankAccountNameAdapter(List<AccountName> accounyNameList) {

        accountNameDialog = new ResourceSelectorDialog<>(getContext(), getString(R.string.select_account_name), accounyNameList);
        accountNameDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mAccountNameEditText.setError(null);
                mAccountNameEditText.setText(name);
            }
        });

        mAccountNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountNameDialog.show();
            }
        });
    }

    private void setDistrictAdapter(List<String> districtList) {

        districtSelectorDialog = new CustomSelectorDialog(getContext(), getString(R.string.select_a_district), districtList);
        districtSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mDistrictSelection.setError(null);
                mDistrictSelection.setText(name);
                mSelectedDistrictId = id;
                mSelectedBranchId = -1;

                mBranches = new ArrayList<>();
                if (bankDistrictToBranchMap.containsKey(name)) {
                    mBranches.addAll(bankDistrictToBranchMap.get(name));
                }

                mBranchNames.clear();
                for (BankBranch bankBranch : mBranches) {
                    mBranchNames.add(bankBranch.getName());
                }
                setBankBranchAdapter(mBranchNames);

            }
        });

        mDistrictSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                districtSelectorDialog.show();
            }
        });
    }

    private void setBankBranchAdapter(List<String> bankBranchList) {

        bankBranchSelectorDialog = new CustomSelectorDialog(getContext(), getString(R.string.bank_branch), bankBranchList);
        bankBranchSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int id, String name) {
                mBankBranchSelection.setError(null);
                mBankBranchSelection.setText(name);
                mSelectedBranchId = id;
            }
        });

        mBankBranchSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankBranchSelectorDialog.show();
            }
        });
    }

    @SuppressLint("StringFormatInvalid")
    private void verifyUserInputs() {
        // The first position is "Select One"
        View focusView;

        clearAllErrorMessages();
        if (mSelectedBankId < 0) {
            mBankListSelection.setError(getString(R.string.please_select_a_bank));
        } else if (mSelectedDistrictId < 0) {
            mDistrictSelection.setError(getString(R.string.please_select_a_district));
        } else if (mSelectedBranchId < 0) {
            mBankBranchSelection.setError(getString(R.string.please_select_a_branch));
        } else if (mAccountNameEditText.getText().toString().trim().length() == 0) {
            mAccountNameEditText.setError(getString(R.string.please_enter_an_account_name));
            focusView = mAccountNameEditText;
            focusView.requestFocus();
        } else if (mAccountNumberEditText.getText().toString().trim().length() == 0) {
            mAccountNumberEditText.setError(getString(R.string.please_enter_an_account_number));
            focusView = mAccountNumberEditText;
            focusView.requestFocus();
        } else if (mAccountNumberEditText.getText().toString().trim().length() < 10) {
            mAccountNumberEditText.setError(getString(R.string.please_enter_an_account_number_of_minimum_digit));
            focusView = mAccountNumberEditText;
            focusView.requestFocus();
        } else if (mChequebookCoverImageFile != null && mChequebookCoverImageFile.length() > Constants.MAX_FILE_BYTE_SIZE) {
            mChequebookCoverPageErrorTextView.setText(getString(R.string.please_select_max_file_size_message, Constants.MAX_FILE_MB_SIZE));
            mChequebookCoverPageErrorTextView.setVisibility(View.VISIBLE);
            focusView = null;
        } else {
            Utilities.hideKeyboard(getActivity());
            launchAddBankAgreementPage();
        }
    }

    private void clearAllErrorMessages() {
        mChequebookCoverPageErrorTextView.setText("");
        mChequebookCoverPageErrorTextView.setVisibility(View.INVISIBLE);
    }

    private void launchAddBankAgreementPage() {
        BankBranch bankBranch = mBranches.get(mSelectedBranchId);
        String bankAccountNumber = mAccountNumberEditText.getText().toString().trim();
        String accountName = mAccountNameEditText.getText().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.BANK_NAME, mSelectedBankName);
        bundle.putString(Constants.BANK_BRANCH_NAME, bankBranch.getName());
        bundle.putString(Constants.BANK_BRANCH_ROUTE_NO, bankBranch.getRoutingNumber());
        bundle.putBoolean(Constants.FROM_ON_BOARD, isSwitchedFromOnBoard);
        bundle.putString(Constants.BANK_ACCOUNT_NAME, accountName);
        bundle.putString(Constants.BANK_ACCOUNT_NUMBER, bankAccountNumber);
        bundle.putBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION, startedFromProfileCompletion);
        bundle.putBoolean(Constants.IS_STARTED_FROM_UNCONCENTED_LIST, false);
        if (mChequebookCoverImageFile != null) {
            bundle.putString(Constants.DOCUMENT_TYPE, "cheque");
            bundle.putStringArray(Constants.PHOTO_URI, getUploadFilePaths());
        }

        ((ManageBanksActivity) getActivity()).switchToAddBankAgreementFragment(bundle);
    }

    private void getBankBranches(long bankID) {
        if (mGetBankBranchesTask != null)
            return;

        mBankBranchEditTextProgressBar.showProgressBar();
        BankBranchRequestBuilder mBankBranchRequestBuilder = new BankBranchRequestBuilder(bankID);

        String mUri = mBankBranchRequestBuilder.getGeneratedUri();
        mGetBankBranchesTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_BRANCH_LIST,
                mUri, getActivity(), false);
        mGetBankBranchesTask.mHttpResponseListener = this;

        mGetBankBranchesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBankList() {
        if (mGetBankTask != null) {
            return;
        }
        mProgressDialog.show();
        mGetBankTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_BANK, getActivity(), false);
        mGetBankTask.mHttpResponseListener = this;

        mGetBankTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("png")
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
            } else {
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

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BANK_BRANCH_LIST:

                try {
                    GetBankBranchesResponse getBankBranchesResponse = gson.fromJson(result.getJsonString(), GetBankBranchesResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mDistrictNames.clear();

                        bankDistrictToBranchMap = new HashMap<>();

                        for (BankBranch branch : getBankBranchesResponse.getAvailableBranches()) {
                            if (!bankDistrictToBranchMap.containsKey(branch.getDistrict())) {
                                bankDistrictToBranchMap.put(branch.getDistrict(), new ArrayList<BankBranch>());
                                mDistrictNames.add(branch.getDistrict());
                            }
                            bankDistrictToBranchMap.get(branch.getDistrict()).add(branch);
                        }

                        setDistrictAdapter(mDistrictNames);
                        mBankBranchEditTextProgressBar.hideProgressBar();

                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), getBankBranchesResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.failed_to_fetch_branch, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mGetBankBranchesTask = null;

                break;

            default:
                break;
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
                                Utilities.requestRequiredPermissions(AddBankFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                            }
                    }
                });
                customUploadPickerDialog.show();
            }
        }
    }

}
