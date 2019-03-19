package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.ShortcutSelectionRadioGroup;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public abstract class IPayAbstractAmountFragment extends Fragment {
    private TextView transactionDescriptionTextView;
    private TextView amountTextView;
    private TextView nameTextView;
    private TextView userNameTextView;
    private EditText amountDummyEditText;
    private RoundedImageView transactionImageView;
    private View balanceInfoLayout;
    private TextView ipayBalanceTextView;
    private TextView balanceInfoTitleTextView;
    private Button continueButton;
    private List<ShortCutOption> shortCutOptionList;
    private ShortcutSelectionRadioGroup shortcutSelectionRadioGroup;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private final NumberFormat balanceBreakDownFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private TextView originalBalanceTitleTextView;
    private TextView debitableBalanceTitleTextView;
    private TextView finalBalanceTitleTextView;
    private TextView finalBalanceTextView;
    private TextView debitableBalanceTextView;
    private TextView originalBalanceTextView;
    private ImageButton balanceBreakDownloadImageButton;
    protected MandatoryBusinessRules businessRules;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumIntegerDigits(1);

        balanceBreakDownFormat.setMinimumFractionDigits(2);
        balanceBreakDownFormat.setMaximumFractionDigits(2);
        businessRules = BusinessRuleCacheManager.getBusinessRules(BusinessRuleCacheManager.getTag(getServiceId()));

        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBusinessRuleUpdateBroadcastReceiver, new IntentFilter(Constants.BUSINESS_RULE_UPDATE_BROADCAST));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (amountDummyEditText != null && amountDummyEditText.isFocused() && getContext() != null)
            Utilities.showKeyboard(getContext(), amountDummyEditText);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (amountDummyEditText != null && amountDummyEditText.isFocused() && getContext() != null)
            Utilities.hideKeyboard(getContext(), amountDummyEditText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBusinessRuleUpdateBroadcastReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipay_transaction_amount_input_new, container, false);
    }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);


        continueButton = view.findViewById(R.id.button_send_money);
        amountDummyEditText = view.findViewById(R.id.amount_dummy_edit_text);
        nameTextView = view.findViewById(R.id.name_text_view);
        userNameTextView = view.findViewById(R.id.user_name_text_view);
        transactionDescriptionTextView = view.findViewById(R.id.transaction_description_text_view);
        transactionImageView = view.findViewById(R.id.transaction_image_view);
        amountTextView = view.findViewById(R.id.amount_text_view);
        shortcutSelectionRadioGroup = view.findViewById(R.id.shortcut_selection_radio_group);
        balanceInfoLayout = view.findViewById(R.id.balance_info_layout);
        ipayBalanceTextView = view.findViewById(R.id.ipay_balance_text_view);
        balanceInfoTitleTextView = view.findViewById(R.id.balance_info_title_text_view);
        balanceBreakDownloadImageButton = view.findViewById(R.id.balance_break_download_image_button);
        setupBalanceBreakDownDialog();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            getActivity().setTitle(R.string.empty_string);
        }

        amountDummyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (amountDummyEditText.getText() != null)
                    amountDummyEditText.setSelection(amountDummyEditText.getText().length());
            }
        });
        amountDummyEditText.setFilters(new InputFilter[]{getInputFilter()});

        amountDummyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                double result = 0;

                String addSuffix = "";
                final String resultString;
                if (charSequence != null)
                    resultString = charSequence.toString();
                else
                    resultString = null;
                if (resultString != null && resultString.length() > 0) {
                    if (resultString.matches("[0]+")) {
                        amountDummyEditText.setText("");
                    }

                    if (resultString.charAt(0) != '.' || resultString.length() > 1)
                        result = Double.valueOf(resultString);
                    if (resultString.endsWith(".") || resultString.endsWith(".0") || resultString.endsWith(".00"))
                        addSuffix = resultString.substring(resultString.indexOf('.'), resultString.length());
                    else if (resultString.matches("[0-9]*\\.[1-9]0"))
                        addSuffix = "0";
                }
                shouldRemoveCheck(result);
                setAmount(result, addSuffix);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        shortcutSelectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (group.findViewById(checkedId) != null && !((RadioButton) group.findViewById(checkedId)).isChecked()) {
                    return;
                }

                int index = shortCutOptionList.indexOf(new ShortCutOption(checkedId));
                if (index == -1)
                    return;
                amountDummyEditText.setText(String.valueOf(shortCutOptionList.get(index).amountValue));
                Utilities.hideKeyboard(getActivity(), amountDummyEditText);
            }
        });

        setAmountFieldEnabled(true);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyInput()) {
                    Utilities.hideKeyboard(getContext(), amountDummyEditText);
                    performContinueAction();
                }
            }
        });

        setBalanceType(BalanceType.MAIN_BALANCE);


        setAmount(0, "");
        setupViewProperties();
    }

    private void setupBalanceBreakDownDialog() {
        if (getContext() != null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            final View dialogTitleView = inflater.inflate(R.layout.layout_dialog_custom_title, null, false);
            final View dialogBodyView = inflater.inflate(R.layout.layout_dialog_balance_break_down, null, false);

            final TextView titleTextView = dialogTitleView.findViewById(R.id.title_text_view);
            final ImageButton closeButton = dialogTitleView.findViewById(R.id.close_button);

            originalBalanceTitleTextView = dialogBodyView.findViewById(R.id.original_balance_title_text_view);
            debitableBalanceTitleTextView = dialogBodyView.findViewById(R.id.debitable_balance_title_text_view);
            finalBalanceTitleTextView = dialogBodyView.findViewById(R.id.final_balance_title_text_view);
            finalBalanceTextView = dialogBodyView.findViewById(R.id.final_balance_text_view);
            debitableBalanceTextView = dialogBodyView.findViewById(R.id.debitable_balance_text_view);
            originalBalanceTextView = dialogBodyView.findViewById(R.id.original_balance_text_view);
            final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setCustomTitle(dialogTitleView)
                    .setView(dialogBodyView)
                    .create();

            balanceBreakDownloadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.show();
                }
            });

            titleTextView.setText(R.string.balance_break_down);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });
        }
    }

    protected abstract void setupViewProperties();

    private void shouldRemoveCheck(double result) {
        int checkedId = shortcutSelectionRadioGroup.getCheckedRadioButtonId();
        if (shortCutOptionList != null) {
            int index = shortCutOptionList.indexOf(new ShortCutOption(checkedId));
            if (index != -1) {
                ShortCutOption shortCutOption = shortCutOptionList.get(index);
                if (shortCutOption.amountValue != result) {
                    shortcutSelectionRadioGroup.clearCheck();
                }
            }
        }
    }

    protected void setAmountFieldEnabled(boolean isEnabled) {
        this.amountDummyEditText.setFocusable(isEnabled);
        amountTextView.setFocusable(isEnabled);
        amountTextView.setOnClickListener(isEnabled ? amountFieldClickAction : null);
    }

    protected void setInputType(int inputType) {
        if (inputType == InputType.TYPE_CLASS_NUMBER || inputType == (InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER))
            amountDummyEditText.setInputType(inputType);
    }

    private void setAmount(double result, String addSuffix) {
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.super_script_size));
        final Spannable spannable = new SpannableString(getString(R.string.amount_holder, numberFormat.format(result), addSuffix));
        spannable.setSpan(superscriptSpan, spannable.length() - 2, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(absoluteSizeSpan, spannable.length() - 2, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        amountTextView.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    protected void setBalanceInfoLayoutVisibility(int visibility) {
        balanceInfoLayout.setVisibility(visibility);
    }

    protected void setName(CharSequence name) {
        nameTextView.setText(name, TextView.BufferType.SPANNABLE);
    }

    protected void setUserName(CharSequence userName) {
        userNameTextView.setVisibility(View.VISIBLE);
        userNameTextView.setText(userName, TextView.BufferType.SPANNABLE);
    }

    protected void setBalanceInfoTitle(CharSequence name) {
        balanceInfoTitleTextView.setText(name, TextView.BufferType.SPANNABLE);
    }

    protected void setBalanceInfo(BigDecimal balance) {
        ipayBalanceTextView.setText(getString(R.string.balance_holder, balanceBreakDownFormat.format(balance)));
    }

    public void setBalanceType(@NonNull BalanceType balanceType) {
        switch (balanceType) {
            case MAIN_BALANCE:
                if (!TextUtils.isEmpty(SharedPrefManager.getUserBalance())) {
                    setBalanceInfoTitle(getString(R.string.your_ipay_balance));
                    setBalanceInfo(new BigDecimal(SharedPrefManager.getUserBalance()));
                }
                break;
            case CREDIT_BALANCE:
                setBalanceInfoTitle(getString(R.string.your_ipay_credit_balance));
                ipayBalanceTextView.setText(R.string.fetching_balance);
                fetchCreditBalance(balanceType);
                break;
            case SETTLED_BALANCE:
                setBalanceInfoTitle(getString(R.string.your_ipay_balance));
                ipayBalanceTextView.setText(R.string.fetching_balance);
                fetchCreditBalance(balanceType);
                break;
        }
    }

    private final Gson gson = new Gson();

    private void fetchCreditBalance(final BalanceType balanceType) {
        final HttpRequestGetAsyncTask getCreditBalanceRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BANK_INSTANTLY_BALANCE,
                Constants.BASE_URL_SM + Constants.URL_ADD_MONEY_FROM_BANK_INSTANTLY_BALANCE, getContext(), new HttpResponseListener() {
            @Override
            public void httpResponseReceiver(GenericHttpResponse result) {
                if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK && !TextUtils.isEmpty(result.getJsonString())) {
                    CreditBalanceResponse creditBalanceResponse = gson.fromJson(result.getJsonString(), CreditBalanceResponse.class);
                    if (creditBalanceResponse != null) {
                        setupBalanceBreakDownInfo(creditBalanceResponse, balanceType);
                        SharedPrefManager.setCreditBalance(creditBalanceResponse);
                        if (balanceType == BalanceType.SETTLED_BALANCE) {
                            final BigDecimal userBalance = new BigDecimal(SharedPrefManager.getUserBalance());
                            final BigDecimal unsettledBalance = creditBalanceResponse.getCreditLimit().subtract(creditBalanceResponse.getAvailableCredit());
                            final BigDecimal settledBalance = userBalance.subtract(unsettledBalance);
                            setBalanceInfo(settledBalance.compareTo(BigDecimal.ZERO) >= 0 ?
                                    settledBalance : BigDecimal.ZERO);
                        } else if (balanceType == BalanceType.CREDIT_BALANCE) {
                            setBalanceInfo(creditBalanceResponse.getAvailableCredit());
                        }
                    } else {
                        showDefaultBalance(balanceType);
                    }
                } else {
                    showDefaultBalance(balanceType);
                }
            }
        }, true);
        getCreditBalanceRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showDefaultBalance(BalanceType balanceType) {
        if (balanceType == BalanceType.SETTLED_BALANCE) {
            setBalanceInfo(new BigDecimal(SharedPrefManager.getUserBalance()));
        } else if (balanceType == BalanceType.CREDIT_BALANCE) {
            setBalanceInfo(BigDecimal.ZERO);
        }
    }

    private void setupBalanceBreakDownInfo(CreditBalanceResponse creditBalanceResponse, BalanceType balanceType) {
        if (balanceType == BalanceType.MAIN_BALANCE) {
            return;
        } else if (balanceType == BalanceType.SETTLED_BALANCE && creditBalanceResponse.getAvailableCredit().equals(creditBalanceResponse.getCreditLimit())) {
            return;
        }

        if (balanceType == BalanceType.CREDIT_BALANCE) {
            originalBalanceTitleTextView.setText(R.string.total_credit_balance);
            originalBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(creditBalanceResponse.getCreditLimit())));

            debitableBalanceTitleTextView.setText(R.string.unsettled_balance);
            debitableBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(Utilities.getMinPossibleBalance(
                            creditBalanceResponse.getCreditLimit()
                                    .subtract(creditBalanceResponse.getAvailableCredit())))));

            finalBalanceTitleTextView.setText(R.string.available_balance);
            finalBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(creditBalanceResponse.getAvailableCredit())));
        } else if (balanceType == BalanceType.SETTLED_BALANCE) {
            final BigDecimal userBalance = new BigDecimal(SharedPrefManager.getUserBalance());
            final BigDecimal unSettledBalance = creditBalanceResponse.getCreditLimit()
                    .subtract(creditBalanceResponse.getAvailableCredit());
            originalBalanceTitleTextView.setText(R.string.current_balance);
            originalBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(userBalance)));

            debitableBalanceTitleTextView.setText(R.string.unsettled_balance);
            debitableBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(Utilities.getMinPossibleBalance(unSettledBalance))));

            finalBalanceTitleTextView.setText(R.string.available_balance);
            finalBalanceTextView.setText(getString(R.string.balance_holder,
                    balanceBreakDownFormat.format(Utilities.getMinPossibleBalance(
                            userBalance.subtract(unSettledBalance)))));
        }
        balanceBreakDownloadImageButton.setVisibility(View.VISIBLE);
    }


    protected void addShortCutOption(int id, String title, int value) {
        shortcutSelectionRadioGroup.setVisibility(View.VISIBLE);

        if (shortCutOptionList == null) {
            shortCutOptionList = new ArrayList<>();
        }

        if (shortcutSelectionRadioGroup.findViewById(id) != null) {
            return;
        }
        final Spannable spannable;
        if (title.isEmpty()) {
            spannable = new SpannableString(getString(R.string.balance_holder, numberFormat.format(value).trim()));
        } else {
            spannable = new SpannableString(String.format("%s\n%s", title, getString(R.string.balance_holder, numberFormat.format(value).trim())));
        }
        spannable.setSpan(new StyleSpan(Typeface.BOLD), title.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ShortCutOption shortCutOption = new ShortCutOption(id, spannable, value);

        if (!shortCutOptionList.contains(shortCutOption))
            shortCutOptionList.add(shortCutOption);

        if (getContext() == null)
            return;

        final RadioButton radioButton = new RadioButton(getContext());
        radioButton.setTypeface(ResourcesCompat.getFont(getContext(), R.font.open_sans));
        radioButton.setId(shortCutOption.id);
        radioButton.setGravity(Gravity.CENTER);

        radioButton.setText(shortCutOption.title, TextView.BufferType.SPANNABLE);
        shortcutSelectionRadioGroup.addView(radioButton);
    }

    protected void setTransactionDescription(String transactionDescription) {
        transactionDescriptionTextView.setText(transactionDescription);
    }

    protected void hideTransactionDescription() {
        transactionDescriptionTextView.setVisibility(View.GONE);
    }

    protected void hideBalance() {
        balanceInfoLayout.setVisibility(View.GONE);
    }

    protected void hideName() {
        nameTextView.setVisibility(View.GONE);
    }

    protected void setTransactionImageResource(int imageResource) {
        if (getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                transactionImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), imageResource, getContext().getTheme()));
            } else {
                Glide.with(getContext()).load(imageResource)
                        .asBitmap()
                        .transform(new CircleTransform(getContext()))
                        .crossFade()
                        .error(R.drawable.ic_profile)
                        .placeholder(R.drawable.ic_profile)
                        .into(transactionImageView);
            }
        }
    }

    protected void setTransactionImage(String imageUrl) {
        Glide.with(getContext()).load(imageUrl)
                .transform(new CircleTransform(getContext()))
                .crossFade().placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .placeholder(R.drawable.ic_profile)
                .into(transactionImageView);
    }

    protected void showErrorMessage(String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
            IPaySnackbar.error(continueButton, errorMessage, IPaySnackbar.LENGTH_LONG).show();
        }
    }

    @Nullable
    public Number getAmount() {
        if (TextUtils.isEmpty(amountTextView.getText())) {
            return null;
        } else {
            try {
                return numberFormat.parse(amountTextView.getText().toString());
            } catch (ParseException e) {
                return 0.0;
            }
        }
    }

    protected void setAmount(String amount) {
        amountDummyEditText.setText(amount);
    }

    protected abstract InputFilter getInputFilter();

    protected abstract boolean verifyInput();

    protected abstract void performContinueAction();

    protected abstract int getServiceId();

    class ShortCutOption {
        private final int id;
        private final CharSequence title;
        private final int amountValue;

        ShortCutOption(int id) {
            this(id, null, 0);
        }

        ShortCutOption(int id, CharSequence title, int amountValue) {
            this.id = id;
            this.title = title;
            this.amountValue = amountValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ShortCutOption that = (ShortCutOption) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @NonNull
        @Override
        public String toString() {
            return "ShortCutOption{" + "id=" + id + ", title=" + title + ", amountValue=" + amountValue + '}';
        }
    }

    private final View.OnClickListener amountFieldClickAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.clearFocus();
            if (getContext() != null)
                Utilities.showKeyboard(getContext(), amountDummyEditText);
            if (amountDummyEditText.getText() != null)
                amountDummyEditText.setSelection(amountDummyEditText.getText().length());
        }
    };

    private final BroadcastReceiver mBusinessRuleUpdateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(BusinessRuleCacheManager.SERVICE_ID_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID) == getServiceId())
                businessRules = BusinessRuleCacheManager.getBusinessRules(BusinessRuleCacheManager.getTag(getServiceId()));
        }
    };

    public enum BalanceType {
        MAIN_BALANCE, CREDIT_BALANCE, SETTLED_BALANCE
    }
}
