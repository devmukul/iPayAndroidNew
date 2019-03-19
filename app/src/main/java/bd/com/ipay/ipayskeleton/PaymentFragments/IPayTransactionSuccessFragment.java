package bd.com.ipay.ipayskeleton.PaymentFragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.Rating.Feedback;
import bd.com.ipay.ipayskeleton.Model.Rating.Meta;
import bd.com.ipay.ipayskeleton.Model.Rating.RatingSubmitRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.Rating.RatingSubmitResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

import static android.view.View.GONE;

public class IPayTransactionSuccessFragment extends Fragment implements HttpResponseListener {
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private int transactionType;
    private String name;
    private String mobileNumber;
    private String transactionId;
    private BigDecimal amount;
    private String senderProfilePicture;
    private String receiverProfilePicture;
    private String mAddressString;
    private AnimatedProgressDialog mCustomProgressDialog;


    private RoundedImageView sponsorImageView;

    private String sponsorProfilePictureUrl;
    private String sponsorName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY);
            name = getArguments().getString(Constants.NAME);
            mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
            senderProfilePicture = getArguments().getString(Constants.SENDER_IMAGE_URL);
            receiverProfilePicture = getArguments().getString(Constants.RECEIVER_IMAGE_URL);
            amount = (BigDecimal) getArguments().getSerializable(Constants.AMOUNT);
            mAddressString = getArguments().getString(Constants.ADDRESS);
            sponsorProfilePictureUrl = getArguments().getString(Constants.SPONSOR_PROFILE_PICTURE);
            sponsorName = getArguments().getString(Constants.SPONSOR_NAME);

            if (sponsorProfilePictureUrl != null) {
                if (!sponsorProfilePictureUrl.contains("ipay.com")) {
                    sponsorProfilePictureUrl = Constants.BASE_URL_FTP_SERVER + sponsorProfilePictureUrl;
                }
            }
        }
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumIntegerDigits(1);

        if (getActivity() != null) {
            mCustomProgressDialog = new AnimatedProgressDialog(getActivity());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipay_transaction_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final TextView transactionSuccessMessageTextView = view.findViewById(R.id.transaction_success_message_text_view);
        final TextView nameTextView = view.findViewById(R.id.name_text_view);
        final TextView addressTextView = view.findViewById(R.id.address_text_view);
        final RoundedImageView sponsorImageView = view.findViewById(R.id.sponsor_image_view);
        final RoundedImageView receiverProfilePictureImageView = view.findViewById(R.id.receiver_profile_picture_image_view);
        final RoundedImageView senderProfilePictureImageView = view.findViewById(R.id.sender_profile_picture_image_view);
        final TextView successDescriptionTextView = view.findViewById(R.id.success_description_text_view);
        final Button goToWalletButton = view.findViewById(R.id.go_to_wallet_button);
        final String amountValue = getString(R.string.balance_holder, numberFormat.format(amount));
        final View ratingView = view.findViewById(R.id.rating_layout);
        final RatingBar ratingBar = view.findViewById(R.id.rate_merchant);

        if (sponsorName != null) {
            sponsorImageView.setVisibility(View.VISIBLE);
            Glide.with(getActivity())
                    .load(sponsorProfilePictureUrl)
                    .centerCrop()
                    .error(R.drawable.user_brand_bg)
                    .into(sponsorImageView);
        } else {
            sponsorImageView.setVisibility(GONE);
        }

        switch (transactionType) {
            case IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY:
                updateTransactionDescription(transactionSuccessMessageTextView, getString(R.string.send_money_success_message, amountValue), 18, 18 + amountValue.length());
                successDescriptionTextView.setText(R.string.send_money_success_description);
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT:
                updateTransactionDescription(transactionSuccessMessageTextView, getString(R.string.make_payment_success_message, amountValue), getString(R.string.payment_success_message).length()+1, getString(R.string.payment_success_message).length()+1 + amountValue.length());
                successDescriptionTextView.setText(getString(R.string.make_payment_success_description, name));
                ratingView.setVisibility(View.VISIBLE);
                if (sponsorName != null) {
                    successDescriptionTextView.setText("Successfully paid to " + name + " using " + sponsorName + "'s" +
                            " iPay wallet. " + name + " and " + sponsorName + " will be notified about this transaction." +
                            " See the details of this transaction in your Transaction History.");
                } else {
                    successDescriptionTextView.setText(getString(R.string.make_payment_success_description, name));
                }
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY:
                updateTransactionDescription(transactionSuccessMessageTextView, getString(R.string.request_money_success_message, amountValue), 23, 23 + amountValue.length());
                successDescriptionTextView.setText(R.string.request_money_success_description);
                break;
            case IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID:
            default:
                transactionSuccessMessageTextView.setText(R.string.empty_string);
                successDescriptionTextView.setText(R.string.empty_string);
                break;
        }
        if (name != null) {
            nameTextView.setText(name);
        }

        if (!TextUtils.isEmpty(mAddressString)) {
            addressTextView.setVisibility(View.VISIBLE);
            addressTextView.setText(mAddressString);
        } else {
            addressTextView.setVisibility(GONE);
        }

        if (senderProfilePicture != null) {
            Glide.with(this)
                    .load(senderProfilePicture)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .transform(new CircleTransform(getContext()))
                    .into(senderProfilePictureImageView);
        }
        if (receiverProfilePicture != null) {
            Glide.with(this)
                    .load(receiverProfilePicture)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .transform(new CircleTransform(getContext()))
                    .into(receiverProfilePictureImageView);
        }

        goToWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transactionType == IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT && ratingBar.getRating()>0){
                    submitRating((int) ratingBar.getRating());
                }else {
                    if (getActivity() != null)
                        getActivity().finish();
                }
            }
        });
    }

    private void updateTransactionDescription(TextView textView, String string, int startPoint, int endPoint) {
        final Spannable spannableAmount;
        spannableAmount = new SpannableString(string);
        spannableAmount.setSpan(new StyleSpan(Typeface.BOLD), startPoint, endPoint, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableAmount.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorLightGreenSendMoney)), startPoint, endPoint, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableAmount, TextView.BufferType.SPANNABLE);
    }

    private final Gson gson = new GsonBuilder().create();
    private String requestJson = "{}";
    private HttpRequestPostAsyncTask httpRequestPostAsyncTask;

    private void submitRating(int rating) {
        if (!Utilities.isConnectionAvailable(getContext())) {
            if (getActivity() != null)
                getActivity().finish();
        }
        if (httpRequestPostAsyncTask != null)
            return;

        mCustomProgressDialog.showDialog();
        final String apiCommand;
        final String url;
        apiCommand = Constants.COMMAND_SUBMIT_RATING;
        Meta meta = new Meta();
        meta.setReceiverMobileNumber(mobileNumber);
        meta.setTransactionId(transactionId);
        List<Feedback> feedbackList = new ArrayList<>();
        Feedback feedback = new Feedback();
        feedback.setMeta(meta);
        feedback.setRating(rating);
        feedback.setServiceId(Constants.SERVICE_ID_MAKE_PAYMENT);
        feedbackList.add(feedback);
        RatingSubmitRequestBuilder ratingSubmitRequestBuilder = new RatingSubmitRequestBuilder(feedbackList);
        requestJson = gson.toJson(ratingSubmitRequestBuilder);
        url = Constants.BASE_URL_MM + Constants.URL_FEEDBACK;
        httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(apiCommand, url, requestJson, getContext(), this, false);
        httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            httpRequestPostAsyncTask = null;
            mCustomProgressDialog.dismissDialog();
        } else {
            switch (result.getApiCommand()) {
                case Constants.COMMAND_SUBMIT_RATING:
                    final String apiCommand = result.getApiCommand();
                    httpRequestPostAsyncTask = null;
                    mCustomProgressDialog.dismissDialog();
                    if (getActivity() != null)
                        getActivity().finish();
                    RatingSubmitResponse ratingSubmitResponse = new Gson().fromJson(result.getJsonString(), RatingSubmitResponse.class);
                    switch (result.getStatus()) {
                        case Constants.HTTP_RESPONSE_STATUS_OK:
                            Toast.makeText(getContext(), "Feedback submitted successfully.", Toast.LENGTH_LONG).show();
                            if (getActivity() != null)
                                getActivity().finish();
                            break;
                        case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
                        case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
                        case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
                        default:
                            if (getActivity() != null)
                                getActivity().finish();
                    }
                    break;
            }
        }

    }
}
