package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AcceptOrRejectBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AddBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AddSponsorRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Beneficiary;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.UpdateMonthlyLimitRequest;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class EditPermissionSourceOfFundBottomSheetFragment extends Fragment implements bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener {
    private Beneficiary beneficiary;
    private HttpRequestPutAsyncTask updateBeneficiaryAsyncTask;
    private HttpRequestPutAsyncTask updateMonthlyLimitAsyncTask;

    private HttpRequestPostAsyncTask mAddBeneficiaryAsyncTask;
    private HttpRequestPostAsyncTask mAddSponsorAsyncTask;


    private EditText pinEditText;
    private EditText amountEditText;

    private String name;
    private String mobileNumber;
    private String relation;

    private CustomProgressDialog mProgressDialog;
    public HttpResponseListener httpResponseListener;

    private String action;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bottom_sheet_source_of_fund_edit_permission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountEditText = view.findViewById(R.id.amount);
        pinEditText = view.findViewById(R.id.pin);
        TextView permissionDetailsTextView = view.findViewById(R.id.permission_details);
        Bundle bundle = getArguments();
        action = bundle.getString(Constants.TO_DO);
        if (action.equals(Constants.ADD_SOURCE_OF_FUND_BENEFICIARY)) {
            name = bundle.getString(Constants.NAME);
            mobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
            relation = bundle.getString(Constants.RELATION);
        } else if (action.equals(Constants.ADD_SOURCE_OF_FUND_SPONSOR)) {
            pinEditText.setVisibility(View.GONE);
            name = bundle.getString(Constants.NAME);
            mobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
            permissionDetailsTextView.setText(getString(R.string.please_enter_a_amount_to_use_from_sponsor));
            relation = bundle.getString(Constants.RELATION);
        } else {
            beneficiary = (Beneficiary) bundle.getSerializable(Constants.BENEFICIARY);
            name = beneficiary.getUser().getName();
            long monthlyLimit = beneficiary.getMonthlyCreditLimit();
            String permissionDetailsText = permissionDetailsTextView.getText().toString();
            permissionDetailsText = permissionDetailsText.replaceFirst(getString(R.string.can), getString(R.string.wants_to));
            permissionDetailsText = permissionDetailsText.replace(getString(R.string.a_certain_amount), Long.toString(monthlyLimit) + getString(R.string.tk));
            amountEditText.setText(Long.toString(monthlyLimit));
            permissionDetailsTextView.setText(permissionDetailsText);
        }
        String permissionDetailsText = getString(R.string.permission_details, name, name);
        permissionDetailsTextView.setText(permissionDetailsText);
        Button doneButton = view.findViewById(R.id.done);
        mProgressDialog = new CustomProgressDialog(getContext());
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInput()) {
                    if (action.equals(Constants.EDIT_AMOUNT)) {
                        attemptChangeMonthlyLimit();
                    } else if (action.equals(Constants.ADD_SOURCE_OF_FUND_BENEFICIARY)) {
                        attemptAddBeneficiary();
                    } else if (action.equals(Constants.ADD_SOURCE_OF_FUND_SPONSOR)) {
                        attemptAddSponsor();

                    } else {
                        attemptUpdateBeneficiaryStatus(beneficiary.getId());
                    }
                    Utilities.hideKeyboard(getActivity());
                }
            }
        });

    }

    private void attemptAddSponsor() {
        if (mAddSponsorAsyncTask != null) {
            return;
        } else {
            mProgressDialog.show();
            AddSponsorRequest addSponsorRequest = new AddSponsorRequest(ContactEngine.formatMobileNumberBD(mobileNumber), relation,
                    Long.parseLong(amountEditText.getText().toString()));
            String jsonString = new Gson().toJson(addSponsorRequest);
            String uri = Constants.BASE_URL_MM + Constants.URL_ADD_SPONSOR;
            mAddSponsorAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_SPONSOR, uri,
                    jsonString, getContext(), this, false);
            mAddSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void attemptChangeMonthlyLimit() {
        if (updateMonthlyLimitAsyncTask != null) {
            return;
        } else {
            UpdateMonthlyLimitRequest updateMonthlyLimitRequest = new UpdateMonthlyLimitRequest
                    (Long.parseLong(amountEditText.getText().toString()), pinEditText.getText().toString());
            String jsonString = new Gson().toJson(updateMonthlyLimitRequest);
            String mUri = Constants.BASE_URL_MM + Constants.URI_CHANGE_MONTHLY_LIMIT +
                    Long.toString(beneficiary.getId()) + "/" + "monthly-credit-limit";
            updateMonthlyLimitAsyncTask = new HttpRequestPutAsyncTask
                    (Constants.COMMAND_CHANGE_MONTLY_LIMIT, mUri,
                            jsonString, getContext(), this, false);
            updateMonthlyLimitAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private void attemptAddBeneficiary() {
        if (mAddBeneficiaryAsyncTask != null) {
            return;
        } else {
            AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(
                    ContactEngine.formatMobileNumberBD(mobileNumber),
                    Long.parseLong(amountEditText.getText().toString()), pinEditText.getText().toString(), relation);
            mAddBeneficiaryAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_BENEFICIARY
                    , Constants.BASE_URL_MM + Constants.URL_ADD_BENEFICIARY,
                    new Gson().toJson(addBeneficiaryRequest), getContext(), this, false);
            mProgressDialog.show();
            mAddBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setHttpResponseListener(HttpResponseListener httpResponseListener) {
        this.httpResponseListener = httpResponseListener;
    }

    public boolean verifyUserInput() {
        Editable pinEditable, amountEditable;
        pinEditable = pinEditText.getText();
        amountEditable = amountEditText.getText();
        if (amountEditable == null) {
            Toast.makeText(getContext(), getString(R.string.please_enter_an_amount), Toast.LENGTH_LONG).show();
            return false;
        } else if (amountEditable.toString() == null || amountEditable.toString().equals("")) {
            Toast.makeText(getContext(), getString(R.string.please_enter_an_amount), Toast.LENGTH_LONG).show();
            return false;
        }

        if (pinEditText.getVisibility() == View.VISIBLE && pinEditable == null) {
            Toast.makeText(getContext(), getString(R.string.please_enter_your_pin), Toast.LENGTH_LONG).show();
            return false;
        } else if (pinEditText.getVisibility() == View.VISIBLE &&
                (pinEditable.toString() == null || pinEditable.toString().equals(""))) {
            Toast.makeText(getContext(), getString(R.string.please_enter_your_pin), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void attemptUpdateBeneficiaryStatus(long id) {
        if (updateBeneficiaryAsyncTask != null) {
            return;
        } else {
            AcceptOrRejectBeneficiaryRequest acceptOrRejectBeneficiaryRequest = new AcceptOrRejectBeneficiaryRequest
                    (Long.parseLong(amountEditText.getText().toString()), pinEditText.getText().toString(), "APPROVED");
            updateBeneficiaryAsyncTask = new HttpRequestPutAsyncTask(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY,
                    Constants.BASE_URL_MM +
                            Constants.URL_ACCEPT_OR_REJECT_SOURCE_OF_FUND + "beneficiary/" + id,
                    new Gson().toJson(acceptOrRejectBeneficiaryRequest), getContext(), this, false);
            updateBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mProgressDialog.dismiss();
            updateBeneficiaryAsyncTask = null;
            updateMonthlyLimitAsyncTask = null;
            mAddBeneficiaryAsyncTask = null;
            mAddSponsorAsyncTask = null;
            return;
        } else {
            mProgressDialog.dismiss();
            if (result.getApiCommand().equals(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY)) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                            new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        httpResponseListener.onSuccess();
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
                }
                updateBeneficiaryAsyncTask = null;
            } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_BENEFICIARY)) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                            new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        httpResponseListener.onSuccess();
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
                }

                mAddBeneficiaryAsyncTask = null;

            } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_SPONSOR)) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                            new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        httpResponseListener.onSuccess();
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
                }
                mAddSponsorAsyncTask = null;
            } else if (result.getApiCommand().equals(Constants.COMMAND_CHANGE_MONTLY_LIMIT)) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                            new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        httpResponseListener.onSuccess();
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
                }
                updateMonthlyLimitAsyncTask = null;
            }
        }
    }

    public interface HttpResponseListener {
        public void onSuccess();
    }
}