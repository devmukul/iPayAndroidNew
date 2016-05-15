package bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.FriendPickerActivity;
import bd.com.ipay.ipayskeleton.Activities.QRCodeViewerActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestMoneyReviewActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RequestMoneyFragment extends Fragment {

    private final int PICK_CONTACT_REQUEST = 100;
    private final int REQUEST_MONEY_REVIEW_REQUEST = 101;

    private Button buttonRequest;
    private ImageView buttonSelectFromContacts;
    private Button buttonShowQRCode;
    private EditText mMobileNumberEditText;
    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private EditText mTitleEditText;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request_money, container, false);
        mMobileNumberEditText = (EditText) v.findViewById(R.id.mobile_number);
        buttonShowQRCode = (Button) v.findViewById(R.id.button_show_qr_code);
        buttonSelectFromContacts = (ImageView) v.findViewById(R.id.select_sender_from_contacts);
        buttonRequest = (Button) v.findViewById(R.id.button_request_money);
        mDescriptionEditText = (EditText) v.findViewById(R.id.description);
        mAmountEditText = (EditText) v.findViewById(R.id.amount);

        if (getActivity().getIntent().hasExtra(Constants.MOBILE_NUMBER)) {
            mMobileNumberEditText.setText(getActivity().getIntent().getStringExtra(Constants.MOBILE_NUMBER));
        }

        mTitleEditText = (EditText) v.findViewById(R.id.title_request);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.submitting_request_money));

        buttonSelectFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendPickerActivity.class);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        buttonShowQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QRCodeViewerActivity.class);
                startActivity(intent);
            }
        });

        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity()) && verifyUserInputs())
                    launchReviewPage();
                else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        return v;
    }

    private boolean verifyUserInputs() {
        mAmountEditText.setError(null);
        mMobileNumberEditText.setError(null);

        boolean cancel = false;
        View focusView = null;

        String mobileNumber = mMobileNumberEditText.getText().toString().trim();

        if (!(mAmountEditText.getText().toString().trim().length() > 0)) {
            focusView = mAmountEditText;
            mAmountEditText.setError(getString(R.string.please_enter_amount));
            cancel = true;
        }

        if (!ContactEngine.isValidNumber(mobileNumber)) {
            focusView = mMobileNumberEditText;
            mMobileNumberEditText.setError(getString(R.string.please_enter_valid_mobile_number));
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void launchReviewPage() {
        String receiver = mMobileNumberEditText.getText().toString().trim();
        BigDecimal amount = new BigDecimal(mAmountEditText.getText().toString().trim());
        String title = mTitleEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();

        Intent intent = new Intent(getActivity(), RequestMoneyReviewActivity.class);
        intent.putExtra(Constants.AMOUNT, amount);
        intent.putExtra(Constants.RECEIVER, ContactEngine.formatMobileNumberBD(receiver));
        intent.putExtra(Constants.DESCRIPTION, description);
        intent.putExtra(Constants.TITLE, title);

        startActivityForResult(intent, REQUEST_MONEY_REVIEW_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
            if (mobileNumber != null)
                mMobileNumberEditText.setText(mobileNumber);
        } else if (requestCode == REQUEST_MONEY_REVIEW_REQUEST && resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
    }

}