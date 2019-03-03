package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Desco;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public class DescoBillInfoFragment extends Fragment {
    private TextView mAccountIDTextView;
    private TextView mDueDateTextView;
    private TextView mVatAmountTextView;
    private TextView mTotalAmountTextView;
    private TextView mStampAmountTextView;
    private TextView mZoneCodeTextView;
    private TextView mTransactionIdTextView;
    private TextView mlpcAmountTextView;
    private TextView mBillNumberTextView;
    private TextView mBillAmountTextView;

    private Bundle bundle;

    private String totalAmountString;

    private Number totalAmount;
    private Number vatAmount;
    private Number stampAmount;
    private Number billAmount;
    private Number lpcAmount;
    private Number zoneCode;
    private Number accountId;
    private Number billNumber;


    private String vatAmountString;
    private String stampAmountString;
    private String lpcAmountString;
    private String billAmountString;
    private String zoneCodeString;
    private String accountIdString;
    private String billNumberString;
    private String dueDate;
    private Button payBillButton;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bundle = getArguments();
        totalAmount = (Number) bundle.getSerializable(Constants.TOTAL_AMOUNT);
        vatAmount = (Number) bundle.getSerializable(Constants.VAT_AMOUNT);
        stampAmount = (Number) bundle.getSerializable(Constants.STAMP_AMOUNT);
        zoneCode = (Number) bundle.getSerializable(Constants.ZONE_CODE);
        lpcAmount = (Number) bundle.getSerializable(Constants.LPC_AMOUNT);
        billAmount = (Number) bundle.getSerializable(Constants.BILL_AMOUNT);

        totalAmountString = numberFormat.format(totalAmount);
        vatAmountString = numberFormat.format(vatAmount);
        lpcAmountString = numberFormat.format(lpcAmount);
        zoneCodeString = numberFormat.format(zoneCode);
        billAmountString = numberFormat.format(billAmount);
        billNumberString = bundle.getString(Constants.BILL_NUMBER);
        accountIdString = bundle.getString(Constants.ACCOUNT_ID);
        if (stampAmount != null) {
            stampAmountString = numberFormat.format(stampAmount);
        } else {
            stampAmountString = null;
        }

        dueDate = bundle.getString(Constants.DUE_DATE);
        return inflater.inflate(R.layout.fragment_show_desco_bill_info, container, false);
    }

    private String getFormattedDate(String date) {
        Date formattedDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        try {
            formattedDate = new SimpleDateFormat("dd-MMM-yy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String s = sdf.format(formattedDate);
        return s;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAccountIDTextView = view.findViewById(R.id.account_number_view);
        mVatAmountTextView = view.findViewById(R.id.vat_amount_view);
        mTotalAmountTextView = view.findViewById(R.id.total_amount_view);
        mlpcAmountTextView = view.findViewById(R.id.lpc_amount_view);
        mBillNumberTextView = view.findViewById(R.id.bill_number_view);
        mDueDateTextView = view.findViewById(R.id.due_date_view);
        mStampAmountTextView = view.findViewById(R.id.stamp_amount_view);
        mBillAmountTextView = view.findViewById(R.id.bill_amount_view);
        mZoneCodeTextView = view.findViewById(R.id.zone_code_view);
        View divider = view.findViewById(R.id.divider);
        payBillButton = view.findViewById(R.id.continue_button);
        View stampView = view.findViewById(R.id.stamp_view);


        mBillNumberTextView.setText(billNumberString);
        mBillAmountTextView.setText(getString(R.string.tk) + " " + billAmountString);
        mVatAmountTextView.setText(getString(R.string.tk) + " " + vatAmountString);

        if (stampAmount != null) {
            mStampAmountTextView.setText(getString(R.string.tk) + " " + stampAmountString);
        } else {
            stampView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }

        mTotalAmountTextView.setText(getString(R.string.tk) + " " + totalAmountString);
        mlpcAmountTextView.setText(getString(R.string.tk) + " " + lpcAmountString);

        mZoneCodeTextView.setText(zoneCodeString);
        mDueDateTextView.setText(getFormattedDate(dueDate));
        mAccountIDTextView.setText(accountIdString);

        payBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBalance()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.BILL_NUMBER, billNumberString);
                    bundle.putSerializable(Constants.TOTAL_AMOUNT, totalAmount);
                    bundle.putString(Constants.ACCOUNT_ID, accountIdString);
                    ((UtilityBillPaymentActivity) getActivity()).switchToDescoBillConfirmationFragment(bundle);
                }
            }
        });
    }

    private boolean checkBalance() {
        String errorMessage;
        if (SharedPrefManager.ifContainsUserBalance()) {
            final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
            BigDecimal amount = new BigDecimal(totalAmount.toString());
            if (amount.compareTo(balance) > 0) {
                errorMessage = getString(R.string.insufficient_balance);
                IPaySnackbar.error(payBillButton, errorMessage,
                        Snackbar.LENGTH_LONG).show();
                return false;
            }
        } else {
            IPaySnackbar.error(payBillButton, getString(R.string.balance_not_available),
                    Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
