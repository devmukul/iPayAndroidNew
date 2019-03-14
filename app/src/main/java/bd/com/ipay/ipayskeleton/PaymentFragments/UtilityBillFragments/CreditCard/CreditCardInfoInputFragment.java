package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard;


import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCardAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Widget.View.CardNumberEditText;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public class CreditCardInfoInputFragment extends Fragment{
    private Button mContinueButton;
    private CardNumberEditText mCardNumberEditText;
    private EditText mNameEditText;
    private int selectedBankIconId;
    private String selectedBankCode;
    private RoundedImageView transactionImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedBankIconId = getArguments().getInt(IPayUtilityBillPayActionActivity.BANK_ICON, 0);
            selectedBankCode = getArguments().getString(IPayUtilityBillPayActionActivity.BANK_CODE, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card_info_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContinueButton = view.findViewById(R.id.button_send_money);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mCardNumberEditText = view.findViewById(R.id.card_number);
        mNameEditText = view.findViewById(R.id.card_holder_name);
        mContinueButton = view.findViewById(R.id.button_send_money);
        transactionImageView = view.findViewById(R.id.transaction_image_view);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyInput()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(IPayUtilityBillPayActionActivity.CARD_NUMBER_KEY,
                            CardNumberValidator.sanitizeEntry(mCardNumberEditText.getText().toString(), true));
                    bundle.putString(IPayUtilityBillPayActionActivity.CARD_USER_NAME_KEY,
                            mNameEditText.getText().toString());
                    bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);
                    bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, selectedBankIconId);
                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new CreditCardAmountInputFragment(), bundle, 3, true);

                }
            }
        });
        ((IPayUtilityBillPayActionActivity) getActivity()).setSupportActionBar(toolbar);
        ((IPayUtilityBillPayActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle("Credit Card Bill Pay");
        setTransactionImageResource(selectedBankIconId);
    }

    public int getBankIcon(Bank bank) {
        Resources resources = getContext().getResources();
        int resourceId;
        if (bank.getBankCode() != null)
            resourceId = resources.getIdentifier("ic_bank" + bank.getBankCode(), "drawable",
                    getContext().getPackageName());
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    getContext().getPackageName());
        return resourceId;
    }

    protected void showErrorMessage(String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
            IPaySnackbar.error(mContinueButton, errorMessage, IPaySnackbar.LENGTH_SHORT).show();
        }
    }

    public void setTransactionImageResource(int imageResource) {
        if (getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                transactionImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), imageResource, getContext().getTheme()));
            } else {
                Glide.with(getContext()).load(imageResource)
                        .asBitmap()
                        .transform(new CircleTransform(getContext()))
                        .crossFade()
                        .into(transactionImageView);
            }
        }
    }

    public String getCardNumber() {
        if (mCardNumberEditText.getText() != null)
            return mCardNumberEditText.getText().toString();
        else
            return "";
    }

    public String getCardHolderName() {
        if (mNameEditText.getText() != null) {
            return mNameEditText.getText().toString();
        } else {
            return "";
        }
    }

    protected boolean verifyInput() {
        if (TextUtils.isEmpty(getCardNumber())) {
            showErrorMessage(getString(R.string.empty_card_number_message));
            return false;
        } else if (!CardNumberValidator.validateCardNumber(getCardNumber())) {
            showErrorMessage(getString(R.string.invalid_card_number_message));
            return false;
        } else {
            if (TextUtils.isEmpty(getCardHolderName())) {
                showErrorMessage(getString(R.string.enter_a_name));
                return false;
            } else {
                return true;
            }

        }
    }
}
