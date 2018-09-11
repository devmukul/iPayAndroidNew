package bd.com.ipay.ipayskeleton.HomeFragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayHereActivity;
import bd.com.ipay.ipayskeleton.Activities.InviteFriendActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.AddMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.QRCodePaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SendMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TopUpActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.WithdrawMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.QRCodeViewerActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.RefreshBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.DashboardProfileCompletionPOJO;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistoryRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistoryResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class HomeFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mRefreshBalanceTask = null;
    private HttpRequestGetAsyncTask mTransactionHistoryTask = null;
    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private ProgressDialog mProgressDialog;
    private TextView balanceView;
    private int historyPageCount = 1;

    // Transaction History
    private TextView mTransactionDescriptionView;
    private TextView mTimeView;
    private TextView mReceiverView;
    private TextView mBalanceTextView;
    private TextView mNetAmountView;
    private ImageView mOtherImageView;
    private ProfileImageView mProfileImageView;
    private ImageView mStatusIconView;
    private TextView mTitleView;

    private View mTransactionHistoryView;
    private View mAddMoneyButton;
    private View mWithdrawMoneyButton;
    private LinearLayout mSendMoneyButton;
    private LinearLayout mRequestMoneyButton;
    private LinearLayout mPayByQRCodeButton;
    private LinearLayout mInviteFriendButton;
    private LinearLayout mTopUpButton;
    private LinearLayout mIPayHereButton;
    public static ImageView refreshBalanceButton;
    private ImageView mShowQRCodeButton;
    private BottomSheetBehavior mBottomSheetBehavior;
    private View mBottomSheet;
    private ImageView mUpArrow;
    private TextView mUpArrowText;

    private RecyclerView mProfileCompletionRecyclerView;
    private ProfileCompletionAdapter mProfileCompletionAdapter;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBarTransaction;

    private TransactionHistoryBroadcastReceiver transactionHistoryBroadcastReceiver;

    private final BroadcastReceiver mProfileCompletionInfoUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getProfileCompletionStatus();
        }
    };

    private final BroadcastReceiver mBalanceUpdateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshBalance();
        }
    };

    private class TransactionHistoryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ProfileInfoCacheManager.isAccountVerified()) {
                getTransactionHistory();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        balanceView = (TextView) v.findViewById(R.id.balance);
        mProgressDialog = new ProgressDialog(getActivity());
        refreshBalanceButton = (ImageView) v.findViewById(R.id.refresh_balance_button);
        mAddMoneyButton = v.findViewById(R.id.button_add_money);
        mWithdrawMoneyButton = v.findViewById(R.id.button_withdraw_money);
        mSendMoneyButton = (LinearLayout) v.findViewById(R.id.button_send_money);
        mRequestMoneyButton = (LinearLayout) v.findViewById(R.id.button_request_money);
        mPayByQRCodeButton = (LinearLayout) v.findViewById(R.id.button_pay_by_qr_code);
        mInviteFriendButton = (LinearLayout) v.findViewById(R.id.button_invite_friend);
        mTopUpButton = (LinearLayout) v.findViewById(R.id.button_topup);
        mIPayHereButton = (LinearLayout) v.findViewById(R.id.button_ipay_here);
        mShowQRCodeButton = (ImageView) v.findViewById(R.id.show_qr_code_button);

        mTransactionDescriptionView = (TextView) v.findViewById(R.id.activity_description);
        mTitleView = (TextView) v.findViewById(R.id.title_view);
        mTimeView = (TextView) v.findViewById(R.id.time);
        mReceiverView = (TextView) v.findViewById(R.id.receiver);
        mBalanceTextView = (TextView) v.findViewById(R.id.amount);
        mNetAmountView = (TextView) v.findViewById(R.id.net_amount);
        mStatusIconView = (ImageView) v.findViewById(R.id.status_description_icon);
        mProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mOtherImageView = (ImageView) v.findViewById(R.id.other_image);

        mTransactionHistoryView = v.findViewById(R.id.transaction_view);
        mProfileCompletionRecyclerView = v.findViewById(R.id.profile_completion);
        mProgressBarTransaction = v.findViewById(R.id.progress_bar_transaction);

        // find container view
        mBottomSheet = v.findViewById(R.id.bottom_sheet);
        mUpArrow = v.findViewById(R.id.up_arrow);
        mUpArrowText = v.findViewById(R.id.up_arrow_text);

        initializeBottomSheet();

        mAddMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_BANK) || ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD)) {
                    PinChecker addMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                        @Override
                        public void ifPinAdded() {
                            Intent intent = new Intent(getActivity(), AddMoneyActivity.class);
                            startActivity(intent);
                        }
                    });
                    addMoneyPinChecker.execute();
                } else {
                    DialogUtils.showServiceNotAllowedDialog(getActivity());
                }
            }
        });
        if (SharedPrefManager.getUserBalance() != null) {
            if (SharedPrefManager.getUserBalance().equals("0.0")) {
                balanceView.setText("Loading…");
            } else {
                balanceView.setText(Utilities.takaWithComma(Double.parseDouble(SharedPrefManager.getUserBalance())) + " " + getString(R.string.bdt));
            }
        }

        mWithdrawMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess({ServiceIdConstants.WITHDRAW_MONEY})
            public void onClick(View v) {
                PinChecker withdrawMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), WithdrawMoneyActivity.class);
                        startActivity(intent);
                    }
                });
                withdrawMoneyPinChecker.execute();
            }
        });

        mSendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess({ServiceIdConstants.SEND_MONEY})
            public void onClick(View v) {
                PinChecker sendMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                        startActivity(intent);
                    }
                });
                sendMoneyPinChecker.execute();
            }
        });

        mRequestMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestMoneyActivityIntent = new Intent(getActivity(), RequestMoneyActivity.class);
                startActivity(requestMoneyActivityIntent);
            }
        });

        mInviteFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess({ServiceIdConstants.MAKE_PAYMENT})
            public void onClick(View v) {
                Intent inviteActivityIntent = new Intent(getActivity(), InviteFriendActivity.class);
                startActivity(inviteActivityIntent);
            }
        });

        mPayByQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PinChecker payByQCPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent;
                        intent = new Intent(getActivity(), QRCodePaymentActivity.class);
                        startActivity(intent);
                    }
                });
                payByQCPinChecker.execute();
            }
        });

        mTopUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess({ServiceIdConstants.MAKE_PAYMENT})
            public void onClick(View v) {
                if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.TOP_UP)) {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                    return;
                }
                PinChecker pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), TopUpActivity.class);
                        startActivity(intent);
                    }
                });
                pinChecker.execute();
            }
        });

        mIPayHereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestMoneyActivityIntent = new Intent(getActivity(), IPayHereActivity.class);
                startActivity(requestMoneyActivityIntent);
            }
        });

        refreshBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.BALANCE)
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    refreshBalance();
                }
            }
        });

        mShowQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), QRCodeViewerActivity.class);
                intent.putExtra(Constants.STRING_TO_ENCODE, ProfileInfoCacheManager.getMobileNumber());
                intent.putExtra(Constants.ACTIVITY_TITLE, "My QR Code to Share");
                startActivity(intent);
            }
        });

        // Refresh balance each time home_activity page appears
        if (Utilities.isConnectionAvailable(getActivity())) {
            if (!ProfileInfoCacheManager.isAccountVerified() && ProfileInfoCacheManager.getAccountType()== Constants.PERSONAL_ACCOUNT_TYPE) {
                getProfileCompletionStatus();
            }else{
                getTransactionHistory();
            }
        }

        if (!SharedPrefManager.getUserCountry().equals("BD")) {
            DialogUtils.showDialogForCountyNotSupported(getContext());
        }

        transactionHistoryBroadcastReceiver = new TransactionHistoryBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(transactionHistoryBroadcastReceiver,
                new IntentFilter(Constants.COMPLETED_TRANSACTION_HISTORY_UPDATE_BROADCAST));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBalanceUpdateBroadcastReceiver,
                new IntentFilter(Constants.BALANCE_UPDATE_BROADCAST));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileCompletionInfoUpdateBroadcastReceiver,
                new IntentFilter(Constants.PROFILE_COMPLETION_UPDATE_BROADCAST));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        transactionHistoryBroadcastReceiver = new TransactionHistoryBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(transactionHistoryBroadcastReceiver,
                new IntentFilter(Constants.COMPLETED_TRANSACTION_HISTORY_UPDATE_BROADCAST));

        // TODO we should refresh the balance only based on push notification, no need to fetch it
        // from the server every time someone navigates to the home activity. Once push is implemented
        // properly, move it to onCreate.
        refreshBalance();
        if (!ProfileInfoCacheManager.isAccountVerified() && ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE) {
            getProfileCompletionStatus();
        }else{
            getTransactionHistory();
        }
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_home));
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(transactionHistoryBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBalanceUpdateBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileCompletionInfoUpdateBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.action_search_contacts) != null)
            menu.findItem(R.id.action_search_contacts).setVisible(false);
        if (menu.findItem(R.id.action_filter_by_service) != null)
            menu.findItem(R.id.action_filter_by_service).setVisible(false);
        if (menu.findItem(R.id.action_filter_by_date) != null)
            menu.findItem(R.id.action_filter_by_date).setVisible(false);
    }

    private void promptForProfileCompletion() {
            mTransactionHistoryView.setVisibility(View.GONE);
            mProgressBarTransaction.setVisibility(View.GONE);
            mProfileCompletionRecyclerView.setVisibility(View.VISIBLE);
            List<DashboardProfileCompletionPOJO> requiredInfo = mProfileCompletionStatusResponse.dashboardProfileCompletionData();
            mProfileCompletionAdapter = new ProfileCompletionAdapter(requiredInfo);
            mLayoutManager = new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.HORIZONTAL, false);
            mProfileCompletionRecyclerView.setLayoutManager(mLayoutManager);
            mProfileCompletionRecyclerView.setAdapter(mProfileCompletionAdapter);
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(mProfileCompletionRecyclerView);
    }

    private void refreshBalance() {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.BALANCE)) {
            balanceView.setText(R.string.not_available);
            return;
        }
        if (mRefreshBalanceTask != null || getActivity() == null)
            return;

        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
        rotation.setRepeatCount(Animation.INFINITE);
        refreshBalanceButton.startAnimation(rotation);

        mRefreshBalanceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_BALANCE,
                Constants.BASE_URL_SM + Constants.URL_REFRESH_BALANCE, null, getActivity(), true);
        mRefreshBalanceTask.mHttpResponseListener = this;
        mRefreshBalanceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getProfileCompletionStatus() {
        if (mGetProfileCompletionStatusTask != null) {
            return;
        }

        mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this, true);
        mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getTransactionHistory() {
        if (mTransactionHistoryTask != null) {
            return;
        }
        String url = TransactionHistoryRequest.generateUri(null,
                null, null, historyPageCount, 1 , null);

        mTransactionHistoryTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRANSACTION_HISTORY,
                url, getActivity(), false);
        mTransactionHistoryTask.mHttpResponseListener = this;
        mTransactionHistoryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadTransactionHistory(final TransactionHistory transactionHistory) {
        mTransactionHistoryView.setVisibility(View.VISIBLE);
        mProgressBarTransaction.setVisibility(View.GONE);
        mProfileCompletionRecyclerView.setVisibility(View.GONE);
        final String description = transactionHistory.getShortDescription();
        final String receiver = transactionHistory.getReceiver();
        String responseTime = Utilities.formatDayMonthYear(transactionHistory.getTime());
        final String netAmountWithSign = String.valueOf(Utilities.formatTakaFromString(transactionHistory.getNetAmountFormatted()));
        final Integer statusCode = transactionHistory.getStatusCode();
        final Double balance = transactionHistory.getAccountBalance();
        if (balance != null) {
            mBalanceTextView.setText(Utilities.formatTakaWithComma(balance));
        }

        mNetAmountView.setText(netAmountWithSign);

        switch (statusCode) {
            case Constants.TRANSACTION_STATUS_ACCEPTED: {
                mNetAmountView.setPaintFlags(mNetAmountView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                mStatusIconView.setImageDrawable(getResources().getDrawable(R.drawable.transaction_tick_sign));
                break;
            }
            case Constants.TRANSACTION_STATUS_CANCELLED: {
                mNetAmountView.setPaintFlags(mNetAmountView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mStatusIconView.setImageDrawable(getResources().getDrawable(R.drawable.transaction_cross_sign));
                break;
            }
            case Constants.TRANSACTION_STATUS_REJECTED: {
                mNetAmountView.setPaintFlags(mNetAmountView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mStatusIconView.setImageDrawable(getResources().getDrawable(R.drawable.transaction_cross_sign));
                break;
            }
            case Constants.TRANSACTION_STATUS_FAILED: {
                mNetAmountView.setPaintFlags(mNetAmountView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mStatusIconView.setImageDrawable(getResources().getDrawable(R.drawable.transaction_cross_sign));
                break;
            }
        }

        mTransactionDescriptionView.setText(description);

        if (receiver != null && !receiver.equals("")) {
            mReceiverView.setVisibility(View.VISIBLE);
            mReceiverView.setText(receiver);
        } else mReceiverView.setVisibility(View.GONE);

        if (DateUtils.isToday(transactionHistory.getTime())) {
            responseTime = "Today, " + Utilities.formatTimeOnly(transactionHistory.getTime());
        }
        mTimeView.setText(responseTime);

        if (transactionHistory.getAdditionalInfo().getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_USER)) {
            String imageUrl = transactionHistory.getAdditionalInfo().getUserProfilePic();
            mOtherImageView.setVisibility(View.INVISIBLE);
            mProfileImageView.setVisibility(View.VISIBLE);
            mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
        } else {
            int iconId = transactionHistory.getAdditionalInfo().getImageWithType(getContext());
            mProfileImageView.setVisibility(View.INVISIBLE);
            mOtherImageView.setVisibility(View.VISIBLE);
            mOtherImageView.setImageResource(iconId);
        }

        mTransactionHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.TRANSACTION_DETAILS)
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TransactionDetailsActivity.class);
                intent.putExtra(Constants.TRANSACTION_DETAILS, transactionHistory);
                startActivity(intent);
            }
        });

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mRefreshBalanceTask = null;
            mGetProfileCompletionStatusTask = null;
            mTransactionHistoryTask = null;
            refreshBalanceButton.clearAnimation();
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_REFRESH_BALANCE)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    RefreshBalanceResponse mRefreshBalanceResponse = gson.fromJson(result.getJsonString(), RefreshBalanceResponse.class);
                    String balance = mRefreshBalanceResponse.getBalance() + "";
                    if (balance != null) {
                        if (isAdded())
                            balanceView.setText(Utilities.takaWithComma(Double.parseDouble(balance)) + " " + getString(R.string.bdt));
                        SharedPrefManager.setUserBalance(balance);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.balance_update_failed, Toast.LENGTH_LONG);
                }
            } else {
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.balance_update_failed, Toast.LENGTH_LONG);
            }

            mRefreshBalanceTask = null;
            refreshBalanceButton.clearAnimation();

        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS)) {
            try {
                mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (!ProfileInfoCacheManager.isAccountVerified() && ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE) {
                        promptForProfileCompletion();
                    }else{
                        getTransactionHistory();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mGetProfileCompletionStatusTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_TRANSACTION_HISTORY)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    TransactionHistoryResponse mTransactionHistoryResponse = gson.fromJson(result.getJsonString(), TransactionHistoryResponse.class);
                    loadTransactionHistory(mTransactionHistoryResponse.getTransactions().get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.transaction_history_get_failed, Toast.LENGTH_LONG).show();
                }
            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.transaction_history_get_failed, Toast.LENGTH_LONG).show();
            }
            mTransactionHistoryTask = null;
        }
    }

    private void initializeBottomSheet() {

        // init the bottom sheet behavior
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        // change the state of the bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // change the state of the bottom sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // set callback for changes
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mUpArrowText.setText("SWIPE UP TO SEE MORE");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mUpArrowText.setText("SWIPE DOWN TO CLOSE");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (isAdded()) {
                    transitionBottomSheetBackgroundColor(slideOffset);
                    animateBottomSheetArrows(slideOffset);
                }
            }
        });
    }

    private void transitionBottomSheetBackgroundColor(float slideOffset) {
        int colorFrom = getResources().getColor(R.color.colorTransparent);
        int colorTo = getResources().getColor(R.color.colorBlackAlpha60);
        mBottomSheet.setBackgroundColor(interpolateColor(slideOffset,
                colorFrom, colorTo));
    }

    private void animateBottomSheetArrows(float slideOffset) {
        mUpArrow.setRotation(slideOffset * -180);
    }

    // Helper method to interpolate colors
    private int interpolateColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;
        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    private class ProfileCompletionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<DashboardProfileCompletionPOJO> requiredInfo;

        public ProfileCompletionAdapter(List<DashboardProfileCompletionPOJO> requiredInfo) {
            this.requiredInfo = requiredInfo;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTitleView;
            private final TextView mSubTitleView;
            private final TextView mNumberView;
            private final ImageView mImageView;

            public ViewHolder(final View itemView) {
                super(itemView);
                mTitleView = (TextView) itemView.findViewById(R.id.profile_completion_msg_view);
                mSubTitleView = (TextView) itemView.findViewById(R.id.profile_completion_subtitle_view);
                mNumberView = (TextView) itemView.findViewById(R.id.number_view);
                mImageView = (ImageView) itemView.findViewById(R.id.other_image);
            }

            public void bindView(int pos) {
                final DashboardProfileCompletionPOJO profileCompletionData = requiredInfo.get(pos);

                mTitleView.setText(profileCompletionData.getTitle());
                mSubTitleView.setText(profileCompletionData.getSubTitle());
                mImageView.setImageResource(profileCompletionData.getImgDrawable());
                mNumberView.setText((pos+1)+"/"+requiredInfo.size());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (profileCompletionData.getProperty().equals(ProfileCompletionPropertyConstants.PROFILE_PICTURE) && ProfileInfoCacheManager.isAccountVerified()) {
                            DialogUtils.showProfilePictureUpdateRestrictionDialog(getContext());
                        } else {
                            Intent i = new Intent(getActivity(), ProfileActivity.class);
                            i.putExtra(Constants.TARGET_FRAGMENT, profileCompletionData.getProperty());
                            startActivity(i);

                        }
                    }
                });
            }
        }

        // Now define the view holder for Normal list item
        class NormalViewHolder extends ViewHolder {
            NormalViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do whatever you want on clicking the normal items
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dashboard_profile_completion, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                NormalViewHolder vh = (NormalViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return requiredInfo.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

    }

}