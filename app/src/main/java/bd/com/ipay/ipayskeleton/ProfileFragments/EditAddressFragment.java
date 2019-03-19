package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.AddressInputView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.SetUserAddressRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.SetUserAddressResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.BulkSignUpHelperDialog;

public class EditAddressFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSetUserAddressTask = null;

    private AddressClass mAddress;
    private AddressClass mPresentAddress;
    private String mAddressType;

    private AddressInputView mAddressInputView;

    private CheckBox mSameAsPresentAddressCheckbox;

    private CustomProgressDialog mProgressDialog;
    private EditText mAddressLine1Field;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_address, container, false);

        getActivity().setTitle(getString(R.string.edit_address));

        mProgressDialog = new CustomProgressDialog(getActivity());
        mAddressInputView = (AddressInputView) v.findViewById(R.id.input_address);
        View mSameAsPresentAddressCheckboxHolder = v.findViewById(R.id.holder_same_as_present_address);
        mSameAsPresentAddressCheckbox = (CheckBox) v.findViewById(R.id.checkbox_same_as_present_address);
        Button mSaveButton = (Button) v.findViewById(R.id.button_save);

        mAddress = (AddressClass) getArguments().getSerializable(Constants.ADDRESS);
        mAddressType = getArguments().getString(Constants.ADDRESS_TYPE);

        mAddressLine1Field = (EditText) mAddressInputView.findViewById(R.id.address_line_1);

        if (!TextUtils.isEmpty(mAddressType) && mAddressType.equals(Constants.ADDRESS_TYPE_PERMANENT)) {
            mSameAsPresentAddressCheckboxHolder.setVisibility(View.VISIBLE);
            mPresentAddress = (AddressClass) getArguments().getSerializable(Constants.PRESENT_ADDRESS);
        }

        if (mAddress != null)
            mAddressInputView.setInformation(mAddress);

        mSameAsPresentAddressCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSameAsPresentAddressCheckbox.isChecked()) {
                    if (mPresentAddress != null)
                        mAddressInputView.setInformation(mPresentAddress);
                }

            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddressInputView.verifyUserInputs()) {
                    Utilities.hideKeyboard(getActivity());
                    mAddress = mAddressInputView.getInformation();
                    setUserAddress();
                }
            }
        });

        if(!BulkSignupUserDetailsCacheManager.isBasicInfoChecked(true)){
            final String cachePresentAddress = BulkSignupUserDetailsCacheManager.getPresentAddress(null);
            final String cacheParmanentAddress = BulkSignupUserDetailsCacheManager.getPermanentAddress(null);

            if (mAddressType.equals(Constants.ADDRESS_TYPE_PRESENT)) {
                if(!TextUtils.isEmpty(cachePresentAddress)) {
                    final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                            getString(R.string.bulk_signup_basic_info_helper_msg));

                    bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAddressLine1Field.setText(cachePresentAddress);
                            bulkSignUpHelperDialog.cancel();
                        }
                    });

                    bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bulkSignUpHelperDialog.cancel();
                            bulkSignUpHelperDialog.setCheckedResponse("BasicInfo");
                        }
                    });

                    bulkSignUpHelperDialog.show();
                }
            } else {
                if(!TextUtils.isEmpty(cacheParmanentAddress)) {
                    final BulkSignUpHelperDialog bulkSignUpHelperDialog = new BulkSignUpHelperDialog(getContext(),
                            getString(R.string.bulk_signup_basic_info_helper_msg));

                    bulkSignUpHelperDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAddressLine1Field.setText(cacheParmanentAddress);
                            bulkSignUpHelperDialog.cancel();
                        }
                    });

                    bulkSignUpHelperDialog.setNegativeButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bulkSignUpHelperDialog.cancel();
                            bulkSignUpHelperDialog.setCheckedResponse("BasicInfo");
                        }
                    });

                    bulkSignUpHelperDialog.show();
                }
            }
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_address_edit));
    }


    private void setUserAddress() {
        mProgressDialog.show();

        SetUserAddressRequest userAddressRequest = new SetUserAddressRequest(mAddressType, mAddress);

        Gson gson = new Gson();
        String addressJson = gson.toJson(userAddressRequest, SetUserAddressRequest.class);
        mSetUserAddressTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_USER_ADDRESS_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_SET_USER_ADDRESS_REQUEST, addressJson, getActivity(), this, false);
        mSetUserAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mSetUserAddressTask = null;
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SET_USER_ADDRESS_REQUEST)) {

            try {
                SetUserAddressResponse mSetUserAddressResponse = gson.fromJson(result.getJsonString(), SetUserAddressResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), mSetUserAddressResponse.getMessage(), Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();

                    //Google Analytic event
                    Utilities.sendSuccessEventTracker(mTracker, "Address Edit", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mSetUserAddressResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    //Google Analytic event
                    Utilities.sendFailedEventTracker(mTracker, "Address Edit", ProfileInfoCacheManager.getAccountId(), mSetUserAddressResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.profile_info_save_failed, Toast.LENGTH_SHORT).show();

                //Google Analytic event
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
            }

            mSetUserAddressTask = null;
            mProgressDialog.dismiss();
        }
    }
}