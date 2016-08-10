package bd.com.ipay.ipayskeleton.PaymentFragments.InvoiceFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.FriendPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.InvoiceActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.QRCodeViewerActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RequestPaymentFragment extends Fragment {

    private final int PICK_CONTACT_REQUEST = 100;
    private static final int REQUEST_CREATE_INVOICE_REVIEW = 101;

    private Button buttonCreateInvoice;
    private ImageView buttonSelectFromContacts;
    private ImageView buttonShowQRCode;
    private EditText mMobileNumberEditText;
    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private EditText mVatEditText;
    private TextView mTotalTextView;
    private ProgressDialog mProgressDialog;

    private BigDecimal mAmount;
    private BigDecimal mVat;
    private BigDecimal mTotal = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request_payment, container, false);
        getActivity().setTitle(R.string.request_payment);

        mMobileNumberEditText = (EditText) v.findViewById(R.id.mobile_number);
        buttonShowQRCode = (ImageView) v.findViewById(R.id.button_show_qr_code);
        buttonSelectFromContacts = (ImageView) v.findViewById(R.id.select_sender_from_contacts);
        buttonCreateInvoice = (Button) v.findViewById(R.id.button_request_money);
        mDescriptionEditText = (EditText) v.findViewById(R.id.description);
        mAmountEditText = (EditText) v.findViewById(R.id.amount);
        mVatEditText = (EditText) v.findViewById(R.id.vat);
        mTotalTextView = (TextView) v.findViewById(R.id.total);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.submitting_request_money));

        mAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mAmount = new BigDecimal(s.toString());
                } else
                    mAmount = BigDecimal.ZERO;

                if (mAmount != null) {
                    mTotal = mAmount;
                }
                if (mVat != null) {
                    mTotal = mAmount.add(mTotal.multiply(mVat.divide(new BigDecimal(100))));
                }
                if (mTotal != null)
                    mTotalTextView.setText(Utilities.formatTaka(mTotal));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mVatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mVat = new BigDecimal(s.toString());
                } else
                    mVat = BigDecimal.ZERO;

                if (mAmount != null) {
                    mTotal = mAmount;
                }

               if (mVat != null) {
                    if (mTotal != null)
                        mTotal = mAmount.add(mTotal.multiply(mVat.divide(new BigDecimal(100))));
                }
                if (mTotal != null)
                    mTotalTextView.setText(Utilities.formatTaka(mTotal));

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        buttonCreateInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    if (verifyUserInputs()) {
                        launchReviewPage();
                    }
                } else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        buttonSelectFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendPickerDialogActivity.class);
                intent.putExtra(Constants.IPAY_MEMBERS_ONLY, true);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        buttonShowQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QRCodeViewerActivity.class);
                String userID = ProfileInfoCacheManager.getMobileNumber().replaceAll("\\D", "");
                intent.putExtra(Constants.STRING_TO_ENCODE, userID);
                intent.putExtra(Constants.ACTIVITY_TITLE, getString(R.string.request_payment));
                startActivity(intent);
            }
        });

        return v;
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;

        String receiver = mMobileNumberEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString();
        String amount = mAmountEditText.getText().toString();
        String vat = mVatEditText.getText().toString();

        // Check for a validation
        if (!(amount.length() > 0 && Double.parseDouble(amount) > 0)) {
            mAmountEditText.setError(getString(R.string.please_enter_amount));
            focusView = mAmountEditText;
            cancel = true;
        }

        if (!(vat.length() > 0 && Double.parseDouble(vat) > 0)) {
            mVatEditText.setError(getString(R.string.please_enter_vat_amount));
            focusView = mVatEditText;
            cancel = true;
        }

        if (description.length() == 0) {
            mDescriptionEditText.setError(getString(R.string.please_add_description));
            focusView = mDescriptionEditText;
            cancel = true;
        }

        if (!ContactEngine.isValidNumber(receiver)) {
            focusView = mMobileNumberEditText;
            mMobileNumberEditText.setError(getString(R.string.please_enter_valid_mobile_number));
            cancel = true;
        } else if (ContactEngine.formatMobileNumberBD(receiver).equals(ProfileInfoCacheManager.getMobileNumber())) {
            focusView = mMobileNumberEditText;
            mMobileNumberEditText.setError(getString(R.string.you_cannot_request_money_from_your_number));
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
        String receiver = mMobileNumberEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString();
        String amount = mAmountEditText.getText().toString();
        String vat = mVatEditText.getText().toString();

        Intent intent = new Intent(getActivity(), RequestPaymentReviewActivity.class);
        intent.putExtra(Constants.INVOICE_DESCRIPTION_TAG, description);
        intent.putExtra(Constants.INVOICE_AMOUNT_TAG, amount);
        intent.putExtra(Constants.INVOICE_RECEIVER_TAG, ContactEngine.formatMobileNumberBD(receiver));

        if (vat != null) intent.putExtra(Constants.VAT, vat);
        else intent.putExtra(Constants.VAT, "0");
        intent.putExtra(Constants.TOTAL, mTotal + "");

        startActivityForResult(intent, REQUEST_CREATE_INVOICE_REVIEW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_CONTACT_REQUEST) {

            if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
                String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
                if (mobileNumber != null) {
                    mMobileNumberEditText.setText(mobileNumber);
                    mMobileNumberEditText.setError(null);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == PICK_CONTACT_REQUEST) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), getString(R.string.no_contact_selected),
                        Toast.LENGTH_SHORT).show();
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CREATE_INVOICE_REVIEW)
            ((InvoiceActivity) getActivity()).switchToInvoicesSentFragment();
    }
}