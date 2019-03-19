package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AddBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AddSponsorRequest;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public class AddSourceOfFundFragment extends Fragment implements bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener {

    private String mName;
    private String mMobileNumber;
    private String mProfileImageUrl;

    private EditText mNumberEditText;
    private ImageView mContactImageView;
    private TextView mNameTextView;
    private RoundedImageView profileImageView;
    private ResourceSelectorDialog resourceSelectorDialog;
    private String relationShip;
    private EditText relationShipEditText;

    private Button doneButton;

    private boolean isSelectedFromContact;

    private CustomProgressDialog mProgressDialog;

    private final int PICK_CONTACT_REQUEST = 100;
    private HttpRequestGetAsyncTask mGetProfileInfoTask;
    private HttpRequestPostAsyncTask mAddSponsorAsyncTask;

    private HttpRequestPostAsyncTask mAddBeneficiaryAsyncTask;
    public BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;
    private EditText pinEditText;
    private EditText amountEditText;
    private TextView helperTextView;
    private View divider;

    private String type;

    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        isSelectedFromContact = false;
        mProgressDialog = new CustomProgressDialog(getContext());
        type = getArguments().getString(Constants.TYPE);
        mMobileNumber = "";
        mName = "";
        mProfileImageUrl = "";
        return inflater.inflate(R.layout.fragment_add_source_of_fund, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNumberEditText = view.findViewById(R.id.number_edit_text);
        mNameTextView = view.findViewById(R.id.name);
        pinEditText = view.findViewById(R.id.pin_edit_text);
        amountEditText = view.findViewById(R.id.amount_edit_text);
        doneButton = view.findViewById(R.id.done);
        profileImageView = view.findViewById(R.id.profile_picture);
        helperTextView = view.findViewById(R.id.help);
        divider = view.findViewById(R.id.background3);
        relationShipEditText = view.findViewById(R.id.relationship_edit_text);
        ImageView backButton = view.findViewById(R.id.back);
        final RelativeLayout relativeLayout = view.findViewById(R.id.test_bottom_sheet_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);

        relativeLayout.findViewById(R.id.test_bottom_sheet_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                    }
                }
        );
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        helperTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TYPE, type);
                ((SourceOfFundActivity) getActivity()).switchToHelpLayout(bundle);
            }
        });

        relationShipEditText.setFocusable(false);
        relationShipEditText.setClickable(true);
        relationShipEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resourceSelectorDialog.show();
            }
        });
        resourceSelectorDialog = new ResourceSelectorDialog(getActivity(),
                getString(R.string.relationship), CommonData.getRelationshipList());
        resourceSelectorDialog.setOnResourceSelectedListener(new ResourceSelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(int selectedIndex, String mRelation) {
                relationShipEditText.setError(null);
                relationShipEditText.setText(mRelation);
                relationShip = mRelation;
            }
        });
        new PinChecker(getContext(), new PinChecker.PinCheckerListener() {
            @Override
            public void ifPinAdded() {

            }
        });

        if (type.equals(Constants.SPONSOR)) {
            pinEditText.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else {
            pinEditText.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            amountEditText.setHint(getString(R.string.amount));
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInput()) {
                    if (type.equals(Constants.SPONSOR)) {
                        if (validateInputForSponsor()) {
                            addSponsor();
                        }
                    } else {
                        if (validateInputForBeneficiary()) {
                            addBeneficiary();
                        }
                    }
                }
            }
        });
        TextView titleTextView = view.findViewById(R.id.title);

        if (type.equals(Constants.BENEFICIARY)) {
            titleTextView.setText(getString(R.string.add_benificiary));
        } else {
            titleTextView.setText(getString(R.string.add_sponsor));
        }

        mContactImageView = view.findViewById(R.id.contact_image_view);
        profileImageView = (RoundedImageView) view.findViewById(R.id.profile_image);
        mContactImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                intent.putExtra(Constants.VERIFIED_USERS_ONLY, true);
                intent.putExtra(Constants.PERSONAL_ACCOUNT, true);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });
        mNumberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mNumberEditText.getText().toString().equals("+880-1")) {
                        mNumberEditText.setSelection(6);
                    } else {
                        Selection.setSelection(mNumberEditText.getText(), mNumberEditText.getText().length());
                    }
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
                isSelectedFromContact = false;
                if (s.toString().length() < 6) {
                    mNumberEditText.setText("+880-1");
                    mName = "";
                    mNameTextView.setText("");
                    mNameTextView.setVisibility(View.GONE);
                    profileImageView.setImageResource(R.drawable.user_brand_bg);
                }
                if (s.toString().length() < 15) {
                    mName = "";
                    mMobileNumber = "";
                    mProfileImageUrl = "";
                    mNameTextView.setText("");
                    mNameTextView.setVisibility(View.GONE);
                    profileImageView.setImageResource(R.drawable.user_brand_bg);
                }
                if (s.toString().length() == 15) {
                    String number = s.toString();
                    number = number.replaceAll("[^0-9.]", "");
                    if (InputValidator.isValidNumber(number)) {
                        ContactEngine.ContactData contactData = searchLocalContacts(ContactEngine.formatMobileNumberBD(number));
                        if (contactData == null) {
                            getProfileInfo(number);
                        } else {
                            mProfileImageUrl = contactData.photoUri;
                            mName = contactData.name;
                            mMobileNumber = s.toString();
                            mMobileNumber = mMobileNumber.replaceAll("[^0-9.]", "");
                            mMobileNumber = ContactEngine.formatMobileNumberBD(mMobileNumber);
                            mNameTextView.setText(mName);
                            mNameTextView.setVisibility(View.VISIBLE);
                            if (mProfileImageUrl != null) {
                                Glide.with(getContext())
                                        .load(mProfileImageUrl)
                                        .error(R.drawable.user_brand_bg)
                                        .centerCrop()
                                        .into(profileImageView);
                            }
                        }
                    } else {
                        mName = "";
                        mMobileNumber = "";
                        mProfileImageUrl = "";
                        mNameTextView.setText("");
                        profileImageView.setImageResource(R.drawable.user_brand_bg);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mNumberEditText.getText().toString().equals("+880-1")) {
                    mNumberEditText.setSelection(6);
                } else {
                    mNumberEditText.setSelection(mNumberEditText.getText().length());
                }

            }
        });
    }

    private boolean validateInputForSponsor() {
        if (amountEditText.getText() == null ||
                amountEditText.getText().toString() == null ||
                amountEditText.getText().toString().equals("")) {
            IPaySnackbar.error(doneButton, "Please enter an amount", IPaySnackbar.LENGTH_LONG).show();
            return false;
        } else if (Integer.parseInt(amountEditText.getText().toString()) == 0) {
            IPaySnackbar.error(doneButton, "Please enter a non zero amount", IPaySnackbar.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean validateInputForBeneficiary() {

        if (amountEditText.getText() == null ||
                amountEditText.getText().toString() == null ||
                amountEditText.getText().toString().equals("")) {
            IPaySnackbar.error(doneButton, "Please enter an amount", IPaySnackbar.LENGTH_LONG).show();
            return false;

        } else if (pinEditText.getText() == null ||
                pinEditText.getText().toString() == null ||
                pinEditText.getText().toString().equals("")) {
            IPaySnackbar.error(doneButton, "Please enter your pin", IPaySnackbar.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean verifyUserInput() {
        if (mName == null || mName.equals("")) {
            showErrorMessage("Please enter a valid iPay user's mobile number");
            return false;
        } else if (mMobileNumber == null || mMobileNumber.equals("")) {
            showErrorMessage("Please enter a valid mobile number");
            return false;
        } else if (relationShip == null || relationShip.equals("")) {
            showErrorMessage("Please select a relationship");
            return false;
        }
        return true;
    }

    private void addSponsor() {
        if (mAddSponsorAsyncTask != null) {
            return;
        } else {
            long amount = 5000;
            mProgressDialog.show();
            if (amountEditText.getText() != null) {
                if (amountEditText.getText().toString() != null) {
                    if (!amountEditText.getText().toString().equals("")) {
                        amount = Long.parseLong(amountEditText.getText().toString());
                    }
                }
            }
            AddSponsorRequest addSponsorRequest = new AddSponsorRequest(ContactEngine.formatMobileNumberBD(mMobileNumber), relationShip,
                    amount);
            String jsonString = new Gson().toJson(addSponsorRequest);
            String uri = Constants.BASE_URL_MM + Constants.URL_ADD_SPONSOR;
            mAddSponsorAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_SPONSOR, uri,
                    jsonString, getContext(), this, false);
            mAddSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public void attemptAddSponsor() {

        Bundle bundle = new Bundle();
        bundle.putString(Constants.MOBILE_NUMBER, mMobileNumber);
        bundle.putString(Constants.NAME, mName);
        bundle.putString(Constants.TO_DO, Constants.ADD_SOURCE_OF_FUND_SPONSOR);
        bundle.putString(Constants.RELATION, relationShip);
        EditPermissionSourceOfFundBottomSheetFragment
                editPermissionSourceOfFundBottomSheetFragment =
                new EditPermissionSourceOfFundBottomSheetFragment();
        editPermissionSourceOfFundBottomSheetFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().
                replace(R.id.test_fragment_container, editPermissionSourceOfFundBottomSheetFragment).commit();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        editPermissionSourceOfFundBottomSheetFragment.setHttpResponseListener(new EditPermissionSourceOfFundBottomSheetFragment.HttpResponseListener() {
            @Override
            public void onSuccess() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Utilities.hideKeyboard(getActivity());
                Bundle bundle = new Bundle();
                bundle.putString(Constants.NAME, mName);
                bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
                bundle.putString(Constants.TYPE, Constants.SPONSOR);
                ((SourceOfFundActivity) getActivity()).switchToSourceOfSuccessFragment(bundle);
            }
        });
    }

    public boolean onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        } else {
            return false;
        }
    }

    public void addBeneficiary() {
        if (mAddBeneficiaryAsyncTask != null) {
            return;
        } else {
            long amount = 5000;
            if (amountEditText.getText() != null) {
                if (amountEditText.getText().toString() != null) {
                    if (!amountEditText.getText().toString().equals("")) {
                        amount = Long.parseLong(amountEditText.getText().toString());
                    }
                }
            }
            AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(
                    ContactEngine.formatMobileNumberBD(mMobileNumber),
                    amount, pinEditText.getText().toString(), relationShip);
            mAddBeneficiaryAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_BENEFICIARY
                    , Constants.BASE_URL_MM + Constants.URL_ADD_BENEFICIARY,
                    new Gson().toJson(addBeneficiaryRequest), getContext(), this, false);
            mProgressDialog.show();
            mAddBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void attemptAddBeneficiaryWithPinCheck() {

        if (mAddBeneficiaryAsyncTask != null) {
            return;
        } else {
            new PinChecker(getContext(), new PinChecker.PinCheckerListener() {
                @Override
                public void ifPinAdded() {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.MOBILE_NUMBER, mMobileNumber);
                    bundle.putString(Constants.NAME, mName);
                    bundle.putString(Constants.TO_DO, Constants.ADD_SOURCE_OF_FUND_BENEFICIARY);
                    bundle.putString(Constants.RELATION, relationShip);
                    EditPermissionSourceOfFundBottomSheetFragment
                            editPermissionSourceOfFundBottomSheetFragment =
                            new EditPermissionSourceOfFundBottomSheetFragment();
                    editPermissionSourceOfFundBottomSheetFragment.setArguments(bundle);
                    getChildFragmentManager().beginTransaction().
                            replace(R.id.test_fragment_container, editPermissionSourceOfFundBottomSheetFragment).commit();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    editPermissionSourceOfFundBottomSheetFragment.setHttpResponseListener(new EditPermissionSourceOfFundBottomSheetFragment.HttpResponseListener() {
                        @Override
                        public void onSuccess() {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            Utilities.hideKeyboard(getActivity());
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.NAME, mName);
                            bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
                            bundle.putString(Constants.TYPE, Constants.BENEFICIARY);
                            ((SourceOfFundActivity) getActivity()).switchToSourceOfSuccessFragment(bundle);
                        }
                    });
                }
            }).execute();
        }
    }

    private void attemptAddBeneficiary(String pin) {
        if (mAddBeneficiaryAsyncTask != null) {
            return;
        } else {
            AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(
                    ContactEngine.formatMobileNumberBD(mMobileNumber),
                    Constants.DEFAULT_CREDIT_LIMIT, pin, relationShip);
            mAddSponsorAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_BENEFICIARY
                    , Constants.BASE_URL_MM + Constants.URL_ADD_BENEFICIARY,
                    new Gson().toJson(addBeneficiaryRequest), getContext(), this, false);
            mProgressDialog.show();
            mAddSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    protected void showErrorMessage(String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
            IPaySnackbar.error(doneButton, errorMessage, IPaySnackbar.LENGTH_LONG).show();
        }
    }

    private void getProfileInfo(String mobileNumber) {
        if (mGetProfileInfoTask != null) {
            return;
        }
        mMobileNumber = mobileNumber;
        mProgressDialog.show();
        GetUserInfoRequestBuilder getUserInfoRequestBuilder = new GetUserInfoRequestBuilder(ContactEngine.
                formatMobileNumberBD(mobileNumber));

        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                getUserInfoRequestBuilder.getGeneratedUri(), getContext(), false);
        mGetProfileInfoTask.mHttpResponseListener = this;
        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ContactEngine.ContactData searchLocalContacts(String mobileNumber) {
        DataHelper dataHelper = DataHelper.getInstance(getActivity());
        int nameIndex, originalNameIndex, phoneNumberIndex, profilePictureUrlQualityMediumIndex;
        Cursor cursor = dataHelper.searchPersonalContacts(mobileNumber, true, true, false,
                true, false, false, null);
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
                int accountType = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_ACCOUNT_TYPE));
                return new ContactEngine.ContactData(0, name, "", profilePictureUrl);

            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mMobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
                mName = data.getStringExtra(Constants.NAME);
                mProfileImageUrl = data.getStringExtra(Constants.PROFILE_PICTURE);
                if (mMobileNumber != null) {
                    mMobileNumber = mMobileNumber.substring(0, 4) + "-" + mMobileNumber.substring(4, mMobileNumber.length());
                    mNumberEditText.setText(mMobileNumber);
                }
                if (mName != null) {
                    mNameTextView.setVisibility(View.VISIBLE);
                    mNameTextView.setText(mName);
                }
                if (mProfileImageUrl != null) {
                    if (!mProfileImageUrl.contains("ipay.com")) {
                        mProfileImageUrl = Constants.BASE_URL_FTP_SERVER + mProfileImageUrl;
                    }
                }
                if (mProfileImageUrl != null) {
                    Glide.with(getContext())
                            .load(mProfileImageUrl)
                            .error(R.drawable.user_brand_bg)
                            .centerCrop()
                            .into(profileImageView);
                }
                isSelectedFromContact = true;
            }
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetProfileInfoTask = null;
            mAddSponsorAsyncTask = null;
            mProgressDialog.dismiss();
        } else {
            mProgressDialog.dismiss();
            try {
                mGetProfileInfoTask = null;
                if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_INFO_REQUEST)) {
                    GetUserInfoResponse getUserInfoResponse = new Gson().fromJson(result.getJsonString(), GetUserInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getUserInfoResponse.getAccountType() == Constants.BUSINESS_ACCOUNT_TYPE) {
                            mName = "";
                            mMobileNumber = "";
                            mProfileImageUrl = "";
                            mNameTextView.setText("");
                            mNameTextView.setVisibility(View.GONE);
                            if (type.equals(Constants.SPONSOR)) {
                                Toast.makeText(getContext(), "Business user can't be added as sponsor", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Business user can't be added as beneficiary", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (getUserInfoResponse.getProfilePictures() != null) {
                                if (getUserInfoResponse.getProfilePictures().size() != 0) {
                                    mProfileImageUrl = getUserInfoResponse.getProfilePictures().get(0).getUrl();
                                }
                            }
                            mName = getUserInfoResponse.getName();
                            mNameTextView.setVisibility(View.VISIBLE);
                            mNameTextView.setText(mName);
                            Glide.with(getContext())
                                    .load(Constants.BASE_URL_FTP_SERVER + mProfileImageUrl)
                                    .centerCrop()
                                    .error(R.drawable.user_brand_bg)
                                    .into(profileImageView);
                        }
                    } else {
                        mName = "";
                        mMobileNumber = "";
                        mProfileImageUrl = "";
                        mNameTextView.setText("");
                        mNameTextView.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getUserInfoResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mGetProfileInfoTask = null;
                } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_SPONSOR)) {
                    GenericResponseWithMessageOnly responseWithMessageOnly = new Gson().fromJson
                            (result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Utilities.hideKeyboard(getActivity());
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.NAME, mName);
                        bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
                        bundle.putString(Constants.TYPE, Constants.SPONSOR);
                        Utilities.sendSuccessEventTracker(mTracker, Constants.ADD_SPONSOR, ProfileInfoCacheManager.getAccountId()
                                , Long.parseLong(amountEditText.getText().toString()));
                        ((SourceOfFundActivity) getActivity()).switchToSourceOfSuccessFragment(bundle);
                    } else {
                        Utilities.sendFailedEventTracker(mTracker, Constants.ADD_SPONSOR, ProfileInfoCacheManager.getAccountId()
                                , responseWithMessageOnly.getMessage());
                        Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mAddSponsorAsyncTask = null;
                } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_BENEFICIARY)) {
                    GenericResponseWithMessageOnly responseWithMessageOnly = new Gson().fromJson
                            (result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Utilities.hideKeyboard(getActivity());
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.NAME, mName);
                        bundle.putString(Constants.PROFILE_PICTURE, mProfileImageUrl);
                        bundle.putString(Constants.TYPE, Constants.BENEFICIARY);
                        Utilities.sendSuccessEventTracker(mTracker, Constants.ADD_BENEFICIARY, ProfileInfoCacheManager.getAccountId()
                                , Long.parseLong(amountEditText.getText().toString()));
                        ((SourceOfFundActivity) getActivity()).switchToSourceOfSuccessFragment(bundle);
                    } else {
                        Utilities.sendFailedEventTracker(mTracker, Constants.ADD_BENEFICIARY, ProfileInfoCacheManager.getAccountId()
                                , responseWithMessageOnly.getMessage());
                        Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mAddBeneficiaryAsyncTask = null;
                }

            } catch (Exception e) {
                Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
            }
        }

    }
}

