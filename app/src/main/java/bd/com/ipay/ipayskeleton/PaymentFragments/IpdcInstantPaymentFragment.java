package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public class IpdcInstantPaymentFragment extends Fragment {
    private EditText amountEditText;
    private EditText installmentIdEditText;
    private Button makePaymentButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipdc_instant_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amountEditText = view.findViewById(R.id.amount);
        installmentIdEditText = view.findViewById(R.id.installment_id);
        makePaymentButton = view.findViewById(R.id.payment_button);
        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInputs()){

                }
            }
        });
    }

    protected void showErrorMessage(String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
            IPaySnackbar.error(makePaymentButton, errorMessage, IPaySnackbar.LENGTH_LONG).show();
        }
    }

    private boolean verifyInputs() {
        if (amountEditText.getText() == null) {
            showErrorMessage(getString(R.string.please_enter_an_amount));
            return false;
        } else if (amountEditText.getText().toString() == null || amountEditText.getText().toString().isEmpty()) {
            showErrorMessage(getString(R.string.please_enter_an_amount));
            return false;
        }
        if (installmentIdEditText.getText() == null) {
            showErrorMessage(getString(R.string.please_enter_an_installment_id));
            return false;
        } else if (installmentIdEditText.getText().toString() == null ||
                installmentIdEditText.getText().toString().isEmpty()) {
            showErrorMessage(getString(R.string.please_enter_an_installment_id));
            return false;
        }
        return true;
    }
}
