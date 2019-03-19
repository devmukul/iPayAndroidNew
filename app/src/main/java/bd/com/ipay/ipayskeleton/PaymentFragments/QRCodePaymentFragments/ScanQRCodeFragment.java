package bd.com.ipay.ipayskeleton.PaymentFragments.QRCodePaymentFragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.BusinessContact;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.Outlets;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.QRScanner.BarcodeCaptureActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ScanQRCodeFragment extends BaseFragment implements HttpResponseListener {

    public static final int REQUEST_CODE_PERMISSION = 1001;

    private HttpRequestGetAsyncTask mGetUserInfoTask;
    private List<BusinessContact> mBusinessContactList;

    private View mRootView;
    private CustomProgressDialog mProgressDialog;

    private String mobileNumber;
    private String brandName;
    private String outletName;
    private String imageUrl;
    private String address;
    private String country;
    private String district;
    private String thana;
    private Long outletId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_scan_qr_code, container, false);
        }
        mProgressDialog = new CustomProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, Constants.RC_BARCODE_CAPTURE);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_scan_qr_code));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    final String result = barcode.displayValue;
                    String[] stringArray = result.split("-");
                    final String mobile = stringArray[0];
                    if (stringArray.length > 1) {
                        try {
                            outletId = Long.parseLong(stringArray[1].trim().replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            outletId = null;
                            e.printStackTrace();
                        }
                    } else {
                        outletId = null;
                    }

                    final Handler mHandler = new Handler();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (InputValidator.isValidNumber(mobile)) {
                                Cursor mCursor;
                                DataHelper dataHelper = DataHelper.getInstance(getContext());
                                mCursor = dataHelper.searchBusinessAccountsByMobile(mobile.replaceAll("[^0-9]", ""));
                                try {
                                    if (mCursor != null && mCursor.getCount() > 0) {
                                        mobileNumber = ContactEngine.formatMobileNumberBD(mobile);
                                        mBusinessContactList = getBusinessContactList(mCursor);
                                        BusinessContact businessContact = mBusinessContactList.get(0);
                                        if (!businessContact.getBusinessName().isEmpty())
                                            brandName = businessContact.getBusinessName();
                                        if (outletId != null) {
                                            if (!businessContact.getOutletString().isEmpty()) {
                                                Outlets[] outlets = new Gson().fromJson(businessContact.getOutletString(), Outlets[].class);
                                                for (Outlets outlet : outlets) {
                                                    if (outlet.getOutletId().equals(outletId)) {
                                                        if (!outlet.getOutletName().isEmpty())
                                                            outletName = outlet.getOutletName();
                                                        if (!outlet.getOutletLogoUrl().isEmpty())
                                                            imageUrl = outlet.getOutletLogoUrl();
                                                        if (!outlet.getAddressString().isEmpty())
                                                            address = outlet.getAddressString();
                                                        if (!outlet.getOutletAddress().getDistrictName().isEmpty())
                                                            district = outlet.getOutletAddress().getDistrictName();
                                                        if (!outlet.getOutletAddress().getThanaName().isEmpty())
                                                            thana = outlet.getOutletAddress().getThanaName();
                                                        break;
                                                    }
                                                }
                                            }

                                        } else {
                                            if (!businessContact.getProfilePictureUrl().isEmpty())
                                                imageUrl = businessContact.getProfilePictureUrl();
                                            if (!businessContact.getAddressString().isEmpty())
                                                address = businessContact.getAddressString();
                                            if (!businessContact.getDistrictString().isEmpty())
                                                district = businessContact.getDistrictString();
                                            if (!businessContact.getThanaString().isEmpty())
                                                thana = businessContact.getThanaString();
                                        }

                                        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.MAKE_PAYMENT)) {
                                            DialogUtils.showServiceNotAllowedDialog(getContext());
                                        } else {
                                            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(district) || TextUtils.isEmpty(thana)) {
                                                fetchUserInfo(mobile);
                                            } else {
                                                switchActivity();
                                            }
                                        }
                                    } else {
                                        fetchUserInfo(mobile);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    fetchUserInfo(mobile);
                                } finally {
                                    if (mCursor != null) {
                                        mCursor.close();
                                    }
                                }
                            } else if (getActivity() != null) {
                                DialogUtils.showDialogForInvalidQRCode(getActivity(), getString(R.string.scan_valid_ipay_qr_code));
                            }
                        }
                    });
                } else {
                    getActivity().finish();
                }
            } else {
                getActivity().finish();
            }
        }
    }


    public void fetchUserInfo(String result) {
        if (Utilities.isConnectionAvailable(getActivity())) {
            mobileNumber = ContactEngine.formatMobileNumberBD(result);
            GetUserInfoRequestBuilder getUserInfoRequestBuilder = new GetUserInfoRequestBuilder(mobileNumber);

            if (mGetUserInfoTask != null) {
                return;
            }

            mProgressDialog.show();
            mGetUserInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_INFO,
                    getUserInfoRequestBuilder.getGeneratedUri(), getActivity(), false);
            mGetUserInfoTask.mHttpResponseListener = ScanQRCodeFragment.this;
            mGetUserInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Toaster.makeText(getActivity(), getResources().getString(
                    R.string.no_internet_connection), Toast.LENGTH_SHORT);
            mProgressDialog.cancel();
            getActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                    startActivityForResult(intent, Constants.RC_BARCODE_CAPTURE);
                } else {
                    getActivity().finish();
                    Toaster.makeText(getActivity(), R.string.error_camera_permission_denied, Toast.LENGTH_LONG);
                }
            }
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mGetUserInfoTask = null;
            return;
        }
        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_USER_INFO:
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    try {
                        Gson gson = new GsonBuilder().create();
                        GetUserInfoResponse getUserInfoResponse = gson.fromJson(result.getJsonString(), GetUserInfoResponse.class);

                        if (!getUserInfoResponse.getName().isEmpty())
                            brandName = getUserInfoResponse.getName();

                        if (outletId != null && getUserInfoResponse.getOutlets() != null && getUserInfoResponse.getOutlets().size() > 0 && getUserInfoResponse.getAccountType() == Constants.BUSINESS_ACCOUNT_TYPE) {
                            for (Outlets outlets : getUserInfoResponse.getOutlets()) {
                                if (outlets.getOutletId().equals(outletId)) {
                                    imageUrl = outlets.getOutletLogoUrl();
                                    address = outlets.getAddressString();
                                    district = outlets.getOutletAddress().getDistrictName();
                                    thana = outlets.getOutletAddress().getThanaName();
                                    outletName = outlets.getOutletName();
                                    break;
                                }
                            }
                        } else {
                            if (!getUserInfoResponse.getProfilePictures().isEmpty())
                                imageUrl = getUserInfoResponse.getProfilePictures().get(0).getUrl();
                            if (getUserInfoResponse.getAddressList() != null) {
                                if (getUserInfoResponse.getAddressList().getOFFICE() != null) {
                                    address = getUserInfoResponse.getAddressList().getOFFICE().get(0).getAddressLine1();
                                    country = getUserInfoResponse.getAddressList().getOFFICE().get(0).getCountry();
                                    district = getUserInfoResponse.getAddressList().getOFFICE().get(0).getDistrict();
                                    thana = getUserInfoResponse.getAddressList().getOFFICE().get(0).getThana();
                                }
                            }
                        }

                        // We will do a check here to know if the account is a personal account or business account.
                        // For Personal Account we have to launch the IPayTransactionActionActivity
                        // For Business Account we have to launch the PaymentActivity with a account status verification check
                        if (getUserInfoResponse.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE) {
                            if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEND_MONEY)) {
                                DialogUtils.showServiceNotAllowedDialog(getContext());
                            } else {
                                Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
                                intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY);
                                intent.putExtra(Constants.MOBILE_NUMBER, mobileNumber);
                                intent.putExtra(Constants.FROM_QR_SCAN, true);
                                intent.putExtra(Constants.NAME, brandName);
                                intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + imageUrl);
                                intent.putExtra(Constants.FROM_QR_SCAN, true);
                                startActivity(intent);
                                if (getActivity() != null)
                                    getActivity().finish();
                            }
                        } else if (getUserInfoResponse.getAccountType() == Constants.BUSINESS_ACCOUNT_TYPE) {
                            if (getUserInfoResponse.getAccountStatus().equals(Constants.ACCOUNT_VERIFICATION_STATUS_VERIFIED)) {
                                if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.MAKE_PAYMENT)) {
                                    DialogUtils.showServiceNotAllowedDialog(getContext());
                                } else {
                                    switchActivity();
                                }
                            } else {
                                DialogUtils.showDialogForInvalidQRCode(getActivity(), getString(R.string.business_account_not_verified));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();

                    }
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                    DialogUtils.showDialogForInvalidQRCode(getActivity(), getString(R.string.scan_valid_ipay_qr_code));
                }
                break;
        }
    }

    private void switchActivity() {
        Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
        intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT);
        intent.putExtra(Constants.MOBILE_NUMBER, mobileNumber);
        intent.putExtra(Constants.FROM_QR_SCAN, true);
        if (TextUtils.isEmpty(outletName))
            intent.putExtra(Constants.NAME, brandName);
        else
            intent.putExtra(Constants.NAME, outletName);
        intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + imageUrl);
        intent.putExtra(Constants.ADDRESS, address);
        if (outletId != null) {
            intent.putExtra(Constants.OUTLET_ID, outletId.longValue());
        }
        startActivity(intent);
        if (getActivity() != null)
            getActivity().finish();
    }

    private List<BusinessContact> getBusinessContactList(Cursor cursor) {
        List<BusinessContact> mBusinessContacts;
        int businessNameIndex;
        int phoneNumberIndex;
        int profilePictureUrlIndex;
        int businessTypeIndex;
        int businessAddressIndex;
        int businessThanaIndex;
        int businessDistrictIndex;
        int businessOutletIndex;

        mBusinessContacts = new ArrayList<>();

        if (cursor != null) {
            mBusinessContacts.clear();
            businessNameIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_NAME);
            phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
            profilePictureUrlIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_PROFILE_PICTURE);
            businessTypeIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_TYPE);
            businessAddressIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_ADDRESS);
            businessThanaIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_THANA);
            businessDistrictIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_DISTRICT);
            businessOutletIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_OUTLET);

            if (cursor.moveToFirst())
                do {
                    String businessName = cursor.getString(businessNameIndex);
                    String mobileNumber = cursor.getString(phoneNumberIndex);
                    String profilePictureUrl = cursor.getString(profilePictureUrlIndex);
                    int businessTypeID = cursor.getInt(businessTypeIndex);
                    String businessAddress = cursor.getString(businessAddressIndex);
                    String businessThana = cursor.getString(businessThanaIndex);
                    String businessDistrict = cursor.getString(businessDistrictIndex);
                    String businessOutlet = cursor.getString(businessOutletIndex);

                    BusinessContact businessContact = new BusinessContact();
                    businessContact.setBusinessName(businessName);
                    businessContact.setMobileNumber(mobileNumber);
                    businessContact.setProfilePictureUrl(profilePictureUrl);
                    businessContact.setAddressString(businessAddress);
                    businessContact.setThanaString(businessThana);
                    businessContact.setDistrictString(businessDistrict);
                    businessContact.setOutletString(businessOutlet);

                    if (CommonData.getBusinessTypes() != null) {
                        BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
                        if (businessType != null)
                            businessContact.setBusinessType(businessType.getName());
                    }

                    mBusinessContacts.add(businessContact);

                } while (cursor.moveToNext());
        }

        return mBusinessContacts;
    }
}
