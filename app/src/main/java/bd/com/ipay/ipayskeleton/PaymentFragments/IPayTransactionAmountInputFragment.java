package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.SourceOfFund.view.SponsorSelectorDialog;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

import static android.view.View.GONE;

public class IPayTransactionAmountInputFragment extends Fragment implements View.OnClickListener {

    public MandatoryBusinessRules mMandatoryBusinessRules;
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private TextView mAmountTextView;
    private Button mContinueButton;

    private int transactionType;
    private String name;
    private String mobileNumber;
    private int operatorType;
    private String operatorCode;
    private String profilePicture;
    private LinearLayout mTopUpDefaultAmountLayout;
    private TextView mTakaFiftyTextView;
    private TextView mTakaHundredTextView;
    private TextView mTakaTwoHundredTextView;
    private TextView mTakaFiveHundredTextView;
    private View sponsorView;
    private TextView sponsorNameTextView;
    private ImageView cancelSponsorImageView;
    private SponsorSelectorDialog sponsorSelectorDialog;
    private Sponsor selectedSponsor;

    private EditText mAmountDummyEditText;

    private ArrayList<Sponsor> sponsorList;

    private String mAddressString;
    private Long mOutletId = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY);
                name = getArguments().getString(Constants.NAME);
                mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
                profilePicture = getArguments().getString(Constants.PHOTO_URI);
                operatorCode = getArguments().getString(Constants.OPERATOR_CODE);
                operatorType = getArguments().getInt(Constants.OPERATOR_TYPE);
                mAddressString = getArguments().getString(Constants.ADDRESS);
                sponsorList = HomeActivity.mSponsorList;
                if (getArguments().containsKey(Constants.OUTLET_ID)) {
                    mOutletId = getArguments().getLong(Constants.OUTLET_ID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumIntegerDigits(2);
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBusinessRuleUpdateBroadcastReceiver, new IntentFilter(Constants.BUSINESS_RULE_UPDATE_BROADCAST));
        mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(BusinessRuleCacheManager.getTag(transactionType));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipay_transaction_amount_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAmountTextView = view.findViewById(R.id.amount_text_view);

        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        final TextView transactionDescriptionTextView = view.findViewById(R.id.transaction_description_text_view);
        final TextView nameTextView = view.findViewById(R.id.name_text_view);
        final TextView addressTextView = view.findViewById(R.id.address_text_view);
        final RoundedImageView profileImageView = view.findViewById(R.id.transaction_image_view);
        final EditText amountDummyEditText = view.findViewById(R.id.amount_dummy_edit_text);
        mAmountDummyEditText = view.findViewById(R.id.amount_dummy_edit_text);
        final TextView ipayBalanceTextView = view.findViewById(R.id.ipay_balance_text_view);
        mContinueButton = view.findViewById(R.id.button_send_money);
        final View balanceInfoLayout = view.findViewById(R.id.balance_info_layout);
        sponsorView = view.findViewById(R.id.source_of_fund_view);
        sponsorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sponsorSelectorDialog = new SponsorSelectorDialog(getContext(), HomeActivity.mSponsorList, new SponsorSelectorDialog.SponsorSelectorListener() {
                    @Override
                    public void onSponsorSelected(Sponsor sponsor) {
                        if (sponsor.getUser().getAccountStatus() == Constants.BLOCKED) {
                            IPaySnackbar.error(mContinueButton, getString(R.string.sponsor_blocked), IPaySnackbar.LENGTH_LONG).show();
                        } else {
                            selectedSponsor = sponsor;
                            sponsorNameTextView.setText(getString(R.string.pay_by) + sponsor.getUser().getName());
                            cancelSponsorImageView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        mTopUpDefaultAmountLayout = (LinearLayout) view.findViewById(R.id.default_top_up_amount);
        mTakaFiftyTextView = (TextView) view.findViewById(R.id.text_view_taka_fifty);
        mTakaHundredTextView = (TextView) view.findViewById(R.id.text_view_taka_hundred);
        mTakaTwoHundredTextView = (TextView) view.findViewById(R.id.text_view_taka_two_hundred);
        mTakaFiveHundredTextView = (TextView) view.findViewById(R.id.text_view_taka_five_hundred);

        mTakaFiveHundredTextView.setOnClickListener(this);
        mTakaTwoHundredTextView.setOnClickListener(this);
        mTakaHundredTextView.setOnClickListener(this);
        mTakaFiftyTextView.setOnClickListener(this);

        sponsorNameTextView = (TextView) view.findViewById(R.id.name);
        cancelSponsorImageView = (ImageView) view.findViewById(R.id.cancel);

        if (selectedSponsor != null) {
            sponsorNameTextView.setText(getString(R.string.pay_by) + selectedSponsor.getUser().getName());
            cancelSponsorImageView.setVisibility(View.VISIBLE);
        }

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            getActivity().setTitle(R.string.empty_string);
        }

        switch (transactionType) {
            case IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY:
                transactionDescriptionTextView.setText(R.string.request_money_from);
                balanceInfoLayout.setVisibility(View.GONE);
                mTopUpDefaultAmountLayout.setVisibility(View.GONE);
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY:
                transactionDescriptionTextView.setText(R.string.send_money_to);
                balanceInfoLayout.setVisibility(View.VISIBLE);
                mTopUpDefaultAmountLayout.setVisibility(View.GONE);
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT:
                transactionDescriptionTextView.setVisibility(GONE);
                balanceInfoLayout.setVisibility(View.VISIBLE);
                mTopUpDefaultAmountLayout.setVisibility(View.GONE);
                if (sponsorList != null) {
                    if (sponsorList.size() > 0) {
                        sponsorView.setVisibility(View.VISIBLE);
                        cancelSponsorImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sponsorNameTextView.setText(getString(R.string.tap_to_pay_from_other_source));
                                cancelSponsorImageView.setVisibility(GONE);
                                selectedSponsor = null;
                            }
                        });

                    }
                }
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_TOP_UP:
                transactionDescriptionTextView.setText(R.string.top_up_to);
                balanceInfoLayout.setVisibility(View.VISIBLE);
                mTopUpDefaultAmountLayout.setVisibility(View.VISIBLE);
                mAmountDummyEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID:
            default:
                transactionDescriptionTextView.setText(R.string.empty_string);
                mTopUpDefaultAmountLayout.setVisibility(View.GONE);
                break;
        }
        if (name != null && !name.equals("")) {
            nameTextView.setText(name);
        } else {
            if (transactionType == ServiceIdConstants.TOP_UP) {
                nameTextView.setText(ContactEngine.formatMobileNumberBD(mobileNumber));
            }
        }

        if (!TextUtils.isEmpty(mAddressString)) {
            addressTextView.setVisibility(View.VISIBLE);
            addressTextView.setText(mAddressString);
        } else {
            addressTextView.setVisibility(GONE);
        }
        profileImageView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(profilePicture)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .transform(new CircleTransform(getContext()))
                .into(profileImageView);

        ipayBalanceTextView.setText(getString(R.string.balance_holder, numberFormat.format(Double.valueOf(SharedPrefManager.getUserBalance()))));

        mAmountDummyEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null) {
                    try {
                        String formattedSource = source.subSequence(start, end).toString();

                        String destPrefix = dest.subSequence(0, dstart).toString();

                        String destSuffix = dest.subSequence(dend, dest.length()).toString();

                        String resultString = destPrefix + formattedSource + destSuffix;

                        resultString = resultString.replace(",", ".");

                        double result = Double.valueOf(resultString);
                        if (result > 999999.99)
                            return "";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return super.filter(source, start, end, dest, dstart, dend);
            }
        }});
        mAmountDummyEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmountDummyEditText.setSelection(mAmountDummyEditText.getText().length());
            }
        });
        mAmountDummyEditText.addTextChangedListener(new TextWatcher() {
            String beforeString;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (transactionType == ServiceIdConstants.TOP_UP) {
                    beforeString = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                double result = 0;
                String addSuffix = "";
                final String resultString;
                if (charSequence != null) {
                    resultString = charSequence.toString();
                    if (transactionType == ServiceIdConstants.TOP_UP) {
                        if (beforeString.equals("500") && charSequence.toString().equals("50")) {
                            setUpDefaultTopUpAmountTextViewColorsUnselected(new TextView(getContext()));
                        } else if (!resultString.equals("50") && !resultString.equals("100") &&
                                !resultString.equals("200") && !resultString.equals("500")) {
                            setUpDefaultTopUpAmountTextViewColorsUnselected(new TextView(getContext()));
                        }
                    }
                } else
                    resultString = null;
                if (resultString != null && resultString.length() > 0) {
                    if (resultString.matches("[0]+")) {
                        mAmountDummyEditText.setText("");
                    }

                    if (resultString.charAt(0) != '.' || resultString.length() > 1)
                        result = Double.valueOf(resultString);
                    if (resultString.endsWith(".") || resultString.endsWith(".0") || resultString.endsWith(".00"))
                        addSuffix = resultString.substring(resultString.indexOf('.'), resultString.length());
                    else if (resultString.matches("[0-9]*\\.[1-9]0"))
                        addSuffix = "0";
                }
                mAmountTextView.setText(String.format("%s%s", numberFormat.format(result), addSuffix));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInputs()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
                    bundle.putString(Constants.NAME, name);
                    bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
                    bundle.putString(Constants.PHOTO_URI, profilePicture);
                    bundle.putInt(Constants.OPERATOR_TYPE, operatorType);
                    bundle.putString(Constants.OPERATOR_CODE, operatorCode);
                    bundle.putString(Constants.ADDRESS, mAddressString);
                    if (selectedSponsor != null) {
                        bundle.putLong(Constants.SPONSOR_ACCOUNT_ID, selectedSponsor.getUser().getAccountId());
                        bundle.putString(Constants.SPONSOR_NAME, selectedSponsor.getName());
                        bundle.putString(Constants.SPONSOR_PROFILE_PICTURE, selectedSponsor.getImageUrl());
                    }
                    if (mOutletId != null)
                        bundle.putLong(Constants.OUTLET_ID, mOutletId);
                    final BigDecimal amount = new BigDecimal(mAmountTextView.getText().toString().replaceAll("[^\\d.]", ""));
                    bundle.putSerializable(Constants.AMOUNT, amount);
                    if (getActivity() instanceof IPayTransactionActionActivity) {
                        ((IPayTransactionActionActivity) getActivity()).switchToTransactionConfirmationFragment(bundle);
                    }
                }
            }
        });
    }

    private boolean isValidInputs() {
        if (!Utilities.isValueAvailable(mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                || !Utilities.isValueAvailable(mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
            DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
            return false;
        } else if (mMandatoryBusinessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
            DialogUtils.showDialogVerificationRequired(getActivity());
            return false;
        }

        String errorMessage;

        if (selectedSponsor != null) {
            if (TextUtils.isEmpty(mAmountDummyEditText.getText())) {
                errorMessage = getString(R.string.please_enter_amount);
            } else if (!InputValidator.isValidDigit(mAmountDummyEditText.getText().toString().trim())) {
                errorMessage = getString(R.string.please_enter_amount);
            } else {
                final BigDecimal minimumAmount = mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                final BigDecimal maximumAmount;
                final BigDecimal amount = new BigDecimal(mAmountDummyEditText.getText().toString().replaceAll("[^\\d.]", ""));
                final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
                if (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY
                        || transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_WITHDRAW_MONEY) {
                    maximumAmount = mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
                } else {
                    maximumAmount = mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT();
                }
                errorMessage = InputValidator.isValidAmount(getActivity(), amount, minimumAmount, maximumAmount);
            }
        } else {
            if (SharedPrefManager.ifContainsUserBalance()) {
                if (TextUtils.isEmpty(mAmountDummyEditText.getText())) {
                    errorMessage = getString(R.string.please_enter_amount);
                } else if (!InputValidator.isValidDigit(mAmountDummyEditText.getText().toString().trim())) {
                    errorMessage = getString(R.string.please_enter_amount);
                } else {
                    final BigDecimal amount = new BigDecimal(mAmountDummyEditText.getText().toString().replaceAll("[^\\d.]", ""));
                    final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
                    if (((transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY) || (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_TOP_UP) || (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT)) && amount.compareTo(balance) > 0) {
                        errorMessage = getString(R.string.insufficient_balance);
                    } else {
                        final BigDecimal minimumAmount = mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                        final BigDecimal maximumAmount;
                        if (transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY
                                || transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_WITHDRAW_MONEY) {
                            maximumAmount = mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
                        } else {
                            maximumAmount = mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT();
                        }
                        errorMessage = InputValidator.isValidAmount(getActivity(), amount, minimumAmount, maximumAmount);
                    }
                }
            } else {
                errorMessage = getString(R.string.balance_not_available);
            }
        }
        if (errorMessage != null) {
            IPaySnackbar.error(mContinueButton, errorMessage, IPaySnackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null)
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBusinessRuleUpdateBroadcastReceiver);
    }

    private final BroadcastReceiver mBusinessRuleUpdateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(BusinessRuleCacheManager.SERVICE_ID_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID) == transactionType)
                mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(BusinessRuleCacheManager.getTag(transactionType));
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.text_view_taka_fifty:
                mAmountDummyEditText.setText("50");
                mTakaFiftyTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount_selected));
                mTakaFiftyTextView.setTextColor(getResources().getColor(R.color.colorTopUpAmount));
                Utilities.hideKeyboard(getActivity());
                setUpDefaultTopUpAmountTextViewColorsUnselected(mTakaFiftyTextView);
                break;
            case R.id.text_view_taka_hundred:
                mTakaHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount_selected));
                mTakaHundredTextView.setTextColor(getResources().getColor(R.color.colorTopUpAmount));
                mAmountDummyEditText.setText("100");
                setUpDefaultTopUpAmountTextViewColorsUnselected(mTakaHundredTextView);
                Utilities.hideKeyboard(getActivity());
                break;
            case R.id.text_view_taka_two_hundred:
                mTakaTwoHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount_selected));
                mTakaTwoHundredTextView.setTextColor(getResources().getColor(R.color.colorTopUpAmount));
                mAmountDummyEditText.setText("200");
                setUpDefaultTopUpAmountTextViewColorsUnselected(mTakaTwoHundredTextView);
                Utilities.hideKeyboard(getActivity());
                break;
            case R.id.text_view_taka_five_hundred:
                mTakaFiveHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount_selected));
                mTakaFiveHundredTextView.setTextColor(getResources().getColor(R.color.colorTopUpAmount));
                mAmountDummyEditText.setText("500");
                setUpDefaultTopUpAmountTextViewColorsUnselected(mTakaFiveHundredTextView);
                Utilities.hideKeyboard(getActivity());
                break;

        }
    }

    private void setUpDefaultTopUpAmountTextViewColorsUnselected(TextView textView) {
        if (textView.getId() == R.id.text_view_taka_fifty) {
            mTakaHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaTwoHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaTwoHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiveHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiveHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

        } else if (textView.getId() == R.id.text_view_taka_hundred) {

            mTakaFiftyTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiftyTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaTwoHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaTwoHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiveHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiveHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        } else if (textView.getId() == R.id.text_view_taka_two_hundred) {

            mTakaHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiftyTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiftyTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiveHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiveHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        } else if (textView.getId() == R.id.text_view_taka_five_hundred) {

            mTakaHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaTwoHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaTwoHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiftyTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiftyTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            mTakaFiftyTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiftyTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaTwoHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaTwoHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));

            mTakaFiveHundredTextView.setBackground(getResources().getDrawable(R.drawable.background_default_top_up_amount));
            mTakaFiveHundredTextView.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }
}
