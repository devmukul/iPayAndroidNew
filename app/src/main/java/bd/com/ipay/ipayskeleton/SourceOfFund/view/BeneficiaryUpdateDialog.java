package bd.com.ipay.ipayskeleton.SourceOfFund.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AcceptOrRejectBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AddBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class BeneficiaryUpdateDialog implements HttpResponseListener {
    private EditText monthlyLimitEditText;
    private View headerView;
    private View bodyView;
    private Context context;

    private CustomProgressDialog mProgressDialog;

    private ImageView cancelImageView;

    private Button updateButton;

    private String mobileNumber;
    private String relationship;

    private AlertDialog updateBeneficiaryPermissionDialog;

    private HttpRequestPostAsyncTask mAddBeneficiaryTask;
    private long id;
    private String name;

    private HttpRequestPutAsyncTask mUpdateBeneficiaryAsyckTask;
    private BeneficiaryAddSuccessListener beneficiaryAddSuccessListener;

    private TextView instructionTextView;

    public BeneficiaryUpdateDialog(Context context, long id, String name) {
        this.context = context;
        this.id = id;
        this.name = name;
        createView();
    }

    public BeneficiaryUpdateDialog(Context context, long id) {
        this.context = context;
        this.id = id;
        createView();
    }

    public BeneficiaryUpdateDialog(Context context, String name, String relationship, String mobileNumber, BeneficiaryAddSuccessListener beneficiaryAddSuccessListener) {
        this.context = context;
        this.id = -1;
        this.relationship = relationship;
        this.mobileNumber = mobileNumber;
        this.name = name;
        this.beneficiaryAddSuccessListener = beneficiaryAddSuccessListener;
        createView();
    }

    public void setBeneficiaryAddSuccessListener(BeneficiaryAddSuccessListener beneficiaryAddSuccessListener) {
        this.beneficiaryAddSuccessListener = beneficiaryAddSuccessListener;
    }

    private void createView() {
        headerView = LayoutInflater.from(context).inflate(R.layout.header_sponsor_dialog, null, false);
        bodyView = (LayoutInflater.from(context).inflate(R.layout.body_beneficiar_update_dialog, null, false));
        ((TextView) headerView.findViewById(R.id.title)).setText("Edit Permission");
        cancelImageView = (ImageView) headerView.findViewById(R.id.cancel);
        instructionTextView = (TextView) bodyView.findViewById(R.id.instruction);
        mProgressDialog = new CustomProgressDialog(context);
        monthlyLimitEditText = (EditText) bodyView.findViewById(R.id.amount);
        updateButton = (Button) bodyView.findViewById(R.id.update);
        String instructionText = instructionTextView.getText().toString();
        instructionText = instructionText.replace("Hasan Masud", name);
        instructionTextView.setText(instructionText);
        if (id == -1) {
            updateButton.setText("Add");
        } else {
            updateButton.setText("Update");
        }
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyInput()) {
                    if (id != -1) {
                        attemptUpdateBeneficiaryWithPinCheck();
                    } else {
                        attemptAddBeneficiaryWithPinCheck();
                    }
                }
            }
        });
        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBeneficiaryPermissionDialog.dismiss();
            }
        });
        updateBeneficiaryPermissionDialog = new AlertDialog.Builder(context)
                .setCustomTitle(headerView)
                .setView(bodyView)
                .setCancelable(false)
                .create();
        updateBeneficiaryPermissionDialog.show();
    }

    private void attemptUpdateBeneficiaryWithPinCheck() {
        new CustomPinCheckerWithInputDialog(context, new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
            @Override
            public void ifPinCheckedAndAdded(String pin) {
                attemptUpdateBeneficiary(id, pin);
            }
        });
    }

    private void attemptAddBeneficiaryWithPinCheck() {
        new CustomPinCheckerWithInputDialog(context, new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
            @Override
            public void ifPinCheckedAndAdded(String pin) {
                attemptAddBeneficiary(id, pin);
            }
        });
    }

    private boolean verifyInput() {
        Editable monthlyLimit;
        monthlyLimit = monthlyLimitEditText.getText();
        if (monthlyLimit == null) {
            return false;
        } else {
            if (monthlyLimit.toString() == null || monthlyLimit.toString().equals("")) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void attemptAddBeneficiary(long id, String pin) {
        if (mAddBeneficiaryTask != null) return;
        else {
            AddBeneficiaryRequest addBeneficiaryRequest = new AddBeneficiaryRequest(mobileNumber,
                    Long.parseLong(monthlyLimitEditText.getText().toString()), pin, relationship);
            mAddBeneficiaryTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_BENEFICIARY,
                    Constants.BASE_URL_MM + Constants.URL_ADD_BENEFICIARY,
                    new Gson().toJson(addBeneficiaryRequest), context, this, false);
            mAddBeneficiaryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private void attemptUpdateBeneficiary(long id, String pin) {
        if (mUpdateBeneficiaryAsyckTask != null) {
            return;
        } else {
            AcceptOrRejectBeneficiaryRequest acceptOrRejectBeneficiaryRequest = new AcceptOrRejectBeneficiaryRequest
                    (Long.parseLong(monthlyLimitEditText.getText().toString()), pin, "APPROVED");
            mUpdateBeneficiaryAsyckTask = new HttpRequestPutAsyncTask(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY,
                    Constants.BASE_URL_MM +
                            Constants.URL_ACCEPT_OR_REJECT_SOURCE_OF_FUND + id,
                    new Gson().toJson(acceptOrRejectBeneficiaryRequest), context, this, false);
            mUpdateBeneficiaryAsyckTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, context, null)) {
            mUpdateBeneficiaryAsyckTask = null;
            mProgressDialog.dismiss();
            return;
        } else {
            mProgressDialog.dismiss();
            try {
                if (result.getApiCommand().equals(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY)) {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                            new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                        updateBeneficiaryPermissionDialog.dismiss();

                    } else {
                        Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mUpdateBeneficiaryAsyckTask = null;
                } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_BENEFICIARY)) {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                        updateBeneficiaryPermissionDialog.dismiss();
                        beneficiaryAddSuccessListener.onBeneficiaryAdded();
                    } else {
                        Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mAddBeneficiaryTask = null;
                }
            } catch (Exception e) {
                mUpdateBeneficiaryAsyckTask = null;
                Toast.makeText(context, context.getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
            }
        }
    }

    public interface BeneficiaryAddSuccessListener {
        void onBeneficiaryAdded();
    }
}

