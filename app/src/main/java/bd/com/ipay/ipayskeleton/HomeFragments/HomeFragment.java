package bd.com.ipay.ipayskeleton.HomeFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.AddMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.MakePaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SendMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TopUpActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.WithdrawMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.Customview.CircularProgressBar;
import bd.com.ipay.ipayskeleton.Customview.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.Customview.Dialogs.AddPinDialogBuilder;
import bd.com.ipay.ipayskeleton.Model.MMModule.Balance.RefreshBalanceRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.Balance.RefreshBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.NewsFeed.GetNewsFeedRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.MMModule.NewsFeed.GetNewsFeedResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.NewsFeed.News;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.TransactionHistory.TransactionHistoryClass;
import bd.com.ipay.ipayskeleton.Model.MMModule.TransactionHistory.TransactionHistoryRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.TransactionHistory.TransactionHistoryResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.TrustedDevice.AddToTrustedDeviceRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.TrustedDevice.AddToTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class HomeFragment extends Fragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mRefreshBalanceTask = null;
    private RefreshBalanceResponse mRefreshBalanceResponse;

    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;
    private AddToTrustedDeviceResponse mAddToTrustedDeviceResponse;

    private HttpRequestGetAsyncTask mGetNewsFeedTask = null;
    private GetNewsFeedResponse mGetNewsFeedResponse;

    private HttpRequestPostAsyncTask mTransactionHistoryTask = null;
    private TransactionHistoryResponse mTransactionHistoryResponse;

    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private SharedPreferences pref;
    private String UUID;
    private String userID;
    private ProgressDialog mProgressDialog;
    private TextView balanceView;
    public static List<News> newsFeedResponsesList;

    private Button mSendMoneyButton;
    private Button mRequestMoneyButton;
    private Button mMobileTopUpButton;
    private Button mMakePaymentButton;

    private ImageView refreshBalanceButton;
    private ImageView addWithdrawMoneyButton;

    private CustomSwipeRefreshLayout mSwipeRefreshLayout;

    private BottomSheetLayout homeBottomSheet;

    private List<TransactionHistoryClass> userTransactionHistoryClasses;
    private RecyclerView.LayoutManager mTransactionHistoryLayoutManager;
    private RecyclerView mTransactionHistoryRecyclerView;
    private TransactionHistoryAndNewsFeedAdapter mTransactionHistoryAndNewsFeedAdapter;

    private View mProfileCompletionPromptView;

    private final int pageCount = 0;

    private static boolean profileCompletionPromptShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        pref = getActivity().getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);

        userID = pref.getString(Constants.USERID, "");

        if (pref.contains(Constants.UUID))
            UUID = pref.getString(Constants.UUID, null);


        homeBottomSheet = (BottomSheetLayout) v.findViewById(R.id.home_bottomsheet);
        mProfileCompletionPromptView = getActivity().getLayoutInflater().inflate(R.layout.sheet_view_profile_completion, null);


        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

        balanceView = (TextView) v.findViewById(R.id.balance);
        mProgressDialog = new ProgressDialog(getActivity());
        refreshBalanceButton = (ImageView) v.findViewById(R.id.refresh_balance_button);
        addWithdrawMoneyButton = (ImageView) v.findViewById(R.id.iv_balance_overflow);

        mSendMoneyButton = (Button) v.findViewById(R.id.send_money_button);
        mRequestMoneyButton = (Button) v.findViewById(R.id.request_money_button);
        mMobileTopUpButton = (Button) v.findViewById(R.id.mobile_recharge_button);
        mMakePaymentButton = (Button) v.findViewById(R.id.make_payment_button);

        mSendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
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

        mMobileTopUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PinChecker topUpPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), TopUpActivity.class);
                        startActivity(intent);
                    }
                });
                topUpPinChecker.execute();
            }
        });

        mTransactionHistoryRecyclerView = (RecyclerView) v.findViewById(R.id.list_transaction_history);

        mTransactionHistoryLayoutManager = new LinearLayoutManager(getActivity());
        mTransactionHistoryAndNewsFeedAdapter = new TransactionHistoryAndNewsFeedAdapter();
        mTransactionHistoryRecyclerView.setLayoutManager(mTransactionHistoryLayoutManager);
        mTransactionHistoryRecyclerView.setAdapter(mTransactionHistoryAndNewsFeedAdapter);

        refreshBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    refreshBalance();
                }
            }
        });

        // Refresh balance each time home_activity page appears
        if (Utilities.isConnectionAvailable(getActivity())) {
            // Check if the news feed is already cleared or not
            if (!HomeActivity.newsFeedLoadedOnce) getNewsFeed();

            refreshBalance();
            getTransactionHistory();
            getProfileCompletionStatus();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    getTransactionHistory();
                }
            }
        });

        // TODO: Place this in HomeActivity
        // Add to trusted device
        if (UUID == null) {
            if (Utilities.isConnectionAvailable(getActivity()))
                addToTrustedDeviceList();
        }

        setButtonActions();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        refreshBalance();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.action_search_contacts) != null)
            menu.findItem(R.id.action_search_contacts).setVisible(false);
    }

    private void setButtonActions() {

        addWithdrawMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.sheet_view_add_withdraw_money, homeBottomSheet, false);

                LinearLayout llAddMoneyButton = (LinearLayout) sheetView.findViewById(R.id.ll_add_money);
                LinearLayout llWithdrawMoneyButton = (LinearLayout) sheetView.findViewById(R.id.ll_withdraw_money);

                llAddMoneyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PinChecker pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                            @Override
                            public void ifPinAdded() {
                                Intent intent = new Intent(getActivity(), AddMoneyActivity.class);
                                startActivity(intent);
                            }
                        });
                        pinChecker.execute();
                    }
                });

                llWithdrawMoneyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PinChecker pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                            @Override
                            public void ifPinAdded() {
                                Intent intent = new Intent(getActivity(), WithdrawMoneyActivity.class);
                                startActivity(intent);
                            }
                        });
                        pinChecker.execute();
                    }
                });


                homeBottomSheet.showWithSheetView(sheetView);
            }
        });
    }

    private void promptForProfileCompletion() {
        mProfileCompletionStatusResponse.analyzeProfileCompletionData();
        if (!mProfileCompletionStatusResponse.isCompletedMandetoryFields()) {
            ((HomeActivity) getActivity()).changeMenuVisibility(R.id.nav_profile_completeness, true);
        }

        if (!profileCompletionPromptShown) {
            profileCompletionPromptShown = true;

            CircularProgressBar progressBar = (CircularProgressBar) mProfileCompletionPromptView.findViewById(R.id.progress_bar);
            TextView profileCompletionMessageView = (TextView) mProfileCompletionPromptView.findViewById(R.id.profile_completion_message);
            Button completeProfileButton = (Button) mProfileCompletionPromptView.findViewById(R.id.complete_profile);
            Button completeLaterButton = (Button) mProfileCompletionPromptView.findViewById(R.id.complete_later);

            if (!mProfileCompletionStatusResponse.isCompletedMandetoryFields()) {

                profileCompletionMessageView.setText("Your profile is " +
                        mProfileCompletionStatusResponse.getCompletionPercentage() + "% "
                        + "complete. Complete your profile to get verified.");

                completeProfileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homeBottomSheet.dismissSheet();

                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);
                    }
                });

                completeLaterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homeBottomSheet.dismissSheet();
                    }
                });

                progressBar.startAnimation(mProfileCompletionStatusResponse.getCompletionPercentage());

                homeBottomSheet.showWithSheetView(mProfileCompletionPromptView);
            } else {
                // "Good to have" properties
                List<ProfileCompletionStatusResponse.PropertyDetails> otherCompletionDetails =
                        mProfileCompletionStatusResponse.getOtherCompletionDetails();
                final List<ProfileCompletionStatusResponse.PropertyDetails> incompleteOtherCompletionDetails = new ArrayList<>();
                for (ProfileCompletionStatusResponse.PropertyDetails propertyDetails : otherCompletionDetails) {
                    if (!propertyDetails.isCompleted()) {
                        incompleteOtherCompletionDetails.add(propertyDetails);
                    }
                }

                if (incompleteOtherCompletionDetails.size() > 0) {
                    Random random = new Random();

                    /**
                     * We want to show the prompt once in every five launch on average.
                     */
                    if (random.nextInt(5) == 0) {
                        int index = random.nextInt(incompleteOtherCompletionDetails.size());
                        final ProfileCompletionStatusResponse.PropertyDetails incompletePropertyDetails = incompleteOtherCompletionDetails.get(index);

                        String profileCompletionMessage = "Your profile is " +
                                mProfileCompletionStatusResponse.getCompletionPercentage() + "% "
                                + "complete.\n"
                                + incompletePropertyDetails.getPropertyTitle()
                                + " to improve your profile";
                        String completeButtonMessage = incompletePropertyDetails.getActionName();

                        profileCompletionMessageView.setText(profileCompletionMessage);
                        completeProfileButton.setText(completeButtonMessage);

                        /**
                         * For ADD_PIN, we show a PIN input dialog to the user.
                         * For other cases, we forward the user to the corresponding fragment
                         * in the ProfileActivity.
                         */
                        completeProfileButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                homeBottomSheet.dismissSheet();

                                if (incompletePropertyDetails.getPropertyName().equals(ProfileCompletionPropertyConstants.ADD_PIN)) {
                                    AddPinDialogBuilder addPinDialogBuilder = new AddPinDialogBuilder(getActivity(), null);
                                    addPinDialogBuilder.show();
                                } else {
                                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                                    intent.putExtra(Constants.TARGET_FRAGMENT, incompletePropertyDetails.getPropertyName());
                                    startActivity(intent);
                                }
                            }
                        });

                        completeLaterButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                homeBottomSheet.dismissSheet();
                            }
                        });

                        progressBar.startAnimation(mProfileCompletionStatusResponse.getCompletionPercentage());

                        homeBottomSheet.showWithSheetView(mProfileCompletionPromptView);
                    }
                }
            }
        }
    }

    private void refreshBalance() {
        if (mRefreshBalanceTask != null) {
            return;
        }

        Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotation);
        rotation.setRepeatCount(Animation.INFINITE);
        refreshBalanceButton.startAnimation(rotation);

        RefreshBalanceRequest mLoginModel = new RefreshBalanceRequest(pref.getString(Constants.USERID, ""));
        Gson gson = new Gson();
        String json = gson.toJson(mLoginModel);
        mRefreshBalanceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_BALANCE,
                Constants.BASE_URL_SM + Constants.URL_REFRESH_BALANCE, json, getActivity());
        mRefreshBalanceTask.mHttpResponseListener = this;
        mRefreshBalanceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void addToTrustedDeviceList() {
        if (mAddTrustedDeviceTask != null) {
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String pushRegistrationID = pref.getString(Constants.PUSH_NOTIFICATION_TOKEN, null);
        String mDeviceID = telephonyManager.getDeviceId();
        String mDeviceName = android.os.Build.MANUFACTURER + "-" + android.os.Build.PRODUCT + " -" + Build.MODEL;

        AddToTrustedDeviceRequest mAddToTrustedDeviceRequest = new AddToTrustedDeviceRequest(mDeviceName,
                Constants.MOBILE_ANDROID + mDeviceID, pushRegistrationID);
        Gson gson = new Gson();
        String json = gson.toJson(mAddToTrustedDeviceRequest);
        mAddTrustedDeviceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_TRUSTED_DEVICE,
                Constants.BASE_URL_MM + Constants.URL_ADD_TRUSTED_DEVICE, json, getActivity());
        mAddTrustedDeviceTask.mHttpResponseListener = this;
        mAddTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getNewsFeed() {
        if (mGetNewsFeedTask != null) {
            return;
        }

        GetNewsFeedRequestBuilder mGetNewsFeedRequestBuilder = new GetNewsFeedRequestBuilder(pageCount);

        String mUri = mGetNewsFeedRequestBuilder.getGeneratedUri();
        mGetNewsFeedTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_NEWS_FEED,
                mUri, getActivity());
        mGetNewsFeedTask.mHttpResponseListener = this;
        mGetNewsFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getTransactionHistory() {
        if (mTransactionHistoryTask != null) {
            return;
        }

        TransactionHistoryRequest mTransactionHistoryRequest = new TransactionHistoryRequest(null, pageCount);
        Gson gson = new Gson();
        String json = gson.toJson(mTransactionHistoryRequest);
        mTransactionHistoryTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_TRANSACTION_HISTORY,
                Constants.BASE_URL_SM + Constants.URL_TRANSACTION_HISTORY, json, getActivity());
        mTransactionHistoryTask.mHttpResponseListener = this;
        mTransactionHistoryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getProfileCompletionStatus() {
        if (mGetProfileCompletionStatusTask != null) {
            return;
        }

        mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this);
        mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showTransactionHistoryDialogue(double amount, double fee, double netAmount,
                                                double balance, String purpose, String time, Integer statusCode, String description, String transactionID) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.transaction_details)
                .customView(R.layout.dialog_transaction_details, true)
                .negativeText(R.string.ok)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

        View view = dialog.getCustomView();
        final TextView descriptionTextView = (TextView) view.findViewById(R.id.description);
        final TextView timeTextView = (TextView) view.findViewById(R.id.time);
        final TextView amountTextView = (TextView) view.findViewById(R.id.amount);
        final TextView feeTextView = (TextView) view.findViewById(R.id.fee);
        final TextView transactionIDTextView = (TextView) view.findViewById(R.id.transaction_id);
        final TextView netAmountTextView = (TextView) view.findViewById(R.id.netAmount);
        final TextView balanceTextView = (TextView) view.findViewById(R.id.balance);
        final TextView purposeTextView = (TextView) view.findViewById(R.id.purpose);
        final TextView statusTextView = (TextView) view.findViewById(R.id.status);
        final LinearLayout purposeLayout = (LinearLayout) view.findViewById(R.id.purpose_layout);

        descriptionTextView.setText(description);
        timeTextView.setText(time);
        amountTextView.setText(Utilities.formatTaka(amount));
        feeTextView.setText(Utilities.formatTaka(fee));
        transactionIDTextView.setText(getString(R.string.transaction_id) + " " + transactionID);
        netAmountTextView.setText(Utilities.formatTaka(netAmount));
        balanceTextView.setText(Utilities.formatTaka(balance));
        if (purpose != null && purpose.length() > 0) purposeTextView.setText(purpose);
        else purposeLayout.setVisibility(View.GONE);

        if (statusCode == Constants.HTTP_RESPONSE_STATUS_OK) {
            statusTextView.setText(getString(R.string.transaction_successful));
            statusTextView.setTextColor(getResources().getColor(R.color.bottle_green));
        } else if (statusCode == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
            statusTextView.setText(getString(R.string.in_progress));
        } else {
            statusTextView.setText(getString(R.string.transaction_failed));
            statusTextView.setTextColor(getResources().getColor(R.color.background_red));
        }

    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) {

        if (result == null) {
            mProgressDialog.dismiss();

            mRefreshBalanceTask = null;
            mGetNewsFeedTask = null;
            mGetProfileCompletionStatusTask = null;
            mTransactionHistoryTask = null;
            mAddTrustedDeviceTask = null;

            mSwipeRefreshLayout.setRefreshing(false);
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG).show();

            refreshBalanceButton.clearAnimation();
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_REFRESH_BALANCE)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mRefreshBalanceResponse = gson.fromJson(result.getJsonString(), RefreshBalanceResponse.class);
                    String balance = mRefreshBalanceResponse.getBalance() + "";
                    if (balance != null)
                        balanceView.setText(balance);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.balance_update_failed, Toast.LENGTH_LONG).show();
                }
            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.balance_update_failed, Toast.LENGTH_LONG).show();
            }

            refreshBalanceButton.clearAnimation();
            mRefreshBalanceTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_NEWS_FEED)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mGetNewsFeedResponse = gson.fromJson(result.getJsonString(), GetNewsFeedResponse.class);

                    if (newsFeedResponsesList == null) {
                        newsFeedResponsesList = mGetNewsFeedResponse.getNewsFeed();
                    } else {
                        List<News> tempUserActivityResponsesList;
                        tempUserActivityResponsesList = mGetNewsFeedResponse.getNewsFeed();
                        newsFeedResponsesList.addAll(tempUserActivityResponsesList);
                    }

                    HomeActivity.newsFeedLoadedOnce = true;
                    // TODO: Handle news feed hasNext in future
                    mTransactionHistoryAndNewsFeedAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.news_feed_get_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.news_feed_get_failed, Toast.LENGTH_LONG).show();
            }


            mGetNewsFeedTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_ADD_TRUSTED_DEVICE)) {

            try {
                mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    String UUID = mAddToTrustedDeviceResponse.getUUID();
                    pref.edit().putString(Constants.UUID, UUID).commit();
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mAddToTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.failed_add_trusted_device, Toast.LENGTH_LONG).show();
            }


            mAddTrustedDeviceTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_TRANSACTION_HISTORY)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mTransactionHistoryResponse = gson.fromJson(result.getJsonString(), TransactionHistoryResponse.class);

                    // Show only last 5 transactions
                    userTransactionHistoryClasses = mTransactionHistoryResponse.getTransactions().subList(
                            0, Math.min(5, mTransactionHistoryResponse.getTransactions().size()));

                    if (userTransactionHistoryClasses.size() == 0)
                        userTransactionHistoryClasses = null;
                    mTransactionHistoryAndNewsFeedAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.transaction_history_get_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.transaction_history_get_failed, Toast.LENGTH_LONG).show();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mTransactionHistoryTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS)) {
            try {
                mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    promptForProfileCompletion();

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mProfileCompletionStatusResponse.getMessage(), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.failed_fetching_profile_completion_status, Toast.LENGTH_LONG).show();
            }

            mGetProfileCompletionStatusTask = null;
        }
    }

    private class TransactionHistoryAndNewsFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TRANSACTION_HISTORY_HEADER_VIEW = 1;
        private static final int WHATS_NEW_VIEW = 2;
        private static final int NEWS_FEED_ITEM_VIEW = 3;

        public class TransactionHistoryViewHolder extends RecyclerView.ViewHolder {
            private View mItemView;

            private TextView mTransactionDescription;
            private TextView mTime;
            private RoundedImageView mPortrait;
            private TextView mAmountTextView;
            private ImageView statusView;

            private ImageView mNewsImage;
            private TextView mNewsHeadLine;
            private TextView mNewsSubDescription;
            private TextView mNewsShortDescription;

            public TransactionHistoryViewHolder(final View itemView) {
                super(itemView);

                mItemView = itemView;

                mTransactionDescription = (TextView) itemView.findViewById(R.id.activity_description);
                mTime = (TextView) itemView.findViewById(R.id.time);
                mAmountTextView = (TextView) itemView.findViewById(R.id.amount);
                statusView = (ImageView) itemView.findViewById(R.id.status);
                mPortrait = (RoundedImageView) itemView.findViewById(R.id.portrait);

                mNewsImage = (ImageView) itemView.findViewById(R.id.news_image);
                mNewsHeadLine = (TextView) itemView.findViewById(R.id.news_title);
                mNewsSubDescription = (TextView) itemView.findViewById(R.id.short_news);
                mNewsShortDescription = (TextView) itemView.findViewById(R.id.short_desc);
            }

            public void bindViewTransactionHistory(int pos) {

                // Decrease pos by 1 as there is a header view now.
                pos = pos - 1;

                double amount = userTransactionHistoryClasses.get(pos).getAmount(userID);

                final double amountWithoutProcessing = userTransactionHistoryClasses.get(pos).getAmount();
                final double fee = userTransactionHistoryClasses.get(pos).getFee();
                final double netAmount = userTransactionHistoryClasses.get(pos).getNetAmount();
                final String transactionID = userTransactionHistoryClasses.get(pos).getTransactionID();
                final String purpose = userTransactionHistoryClasses.get(pos).getPurpose();
                final Integer statusCode = userTransactionHistoryClasses.get(pos).getStatusCode();
                final double balance = userTransactionHistoryClasses.get(pos).getBalance();
                final String description = userTransactionHistoryClasses.get(pos).getDescription();
                final String time = new SimpleDateFormat("EEE, MMM d, ''yy, h:mm a").format(userTransactionHistoryClasses.get(pos).getTime());

                mAmountTextView.setText(Utilities.formatTaka(amount));

                mTransactionDescription.setText(description);
                mTime.setText(time);

                if (userTransactionHistoryClasses.get(pos).getStatusCode() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mAmountTextView.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                    statusView.setColorFilter(Color.GREEN);
                    statusView.setImageResource(R.drawable.ic_check_circle_black_24dp);

                } else if (userTransactionHistoryClasses.get(pos).getStatusCode() == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
                    mAmountTextView.setTextColor(getResources().getColor(R.color.text_gray));
                    statusView.setColorFilter(Color.GRAY);
                    statusView.setImageResource(R.drawable.ic_cached_black_24dp);

                } else {
                    mAmountTextView.setTextColor(getResources().getColor(R.color.background_red));
                    statusView.setColorFilter(Color.RED);
                    statusView.setImageResource(R.drawable.ic_error_black_24dp);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mSwipeRefreshLayout.isRefreshing())
                            showTransactionHistoryDialogue(amountWithoutProcessing, fee, netAmount,
                                    balance, purpose, time, statusCode, description, transactionID);
                    }
                });

                //TODO: remove this when pro pic will come
//                Glide.with(getActivity())
//                        .load(R.drawable.ic_transaction_history)
//                        .into(mPortrait);
            }

            public void bindViewNewsFeed(int pos) {

                if (userTransactionHistoryClasses == null) pos = pos - 1;
                else {
                    if (userTransactionHistoryClasses.size() == 0) pos = pos - 1;
                    else pos = pos - userTransactionHistoryClasses.size() - 2;
                }

                final long newsID = newsFeedResponsesList.get(pos).getId();
                final String description = newsFeedResponsesList.get(pos).getDescription();
                final String title = newsFeedResponsesList.get(pos).getTitle();
                final String subDescription = newsFeedResponsesList.get(pos).getSubDescription();
                final String imageUrl = newsFeedResponsesList.get(pos).getImageUrl();
                final String imageUrlThumbnail = newsFeedResponsesList.get(pos).getImageThumbnailUrl();

                if (title != null) mNewsHeadLine.setText(title);
                if (subDescription != null) mNewsSubDescription.setText(subDescription);
                if (description != null) mNewsShortDescription.setText(description);

                if (imageUrl != null) Glide.with(getActivity())
                        .load(Constants.BASE_URL_IMAGE_SERVER + imageUrl)
                        .crossFade()
                        .placeholder(R.drawable.dummy)
                        .into(mNewsImage);
            }

            public View getItemView() {
                return mItemView;
            }
        }

        public class HeaderViewHolder extends TransactionHistoryViewHolder {
            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class WhatsNewViewHolder extends TransactionHistoryViewHolder {
            public WhatsNewViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class NewsFeedViewHolder extends TransactionHistoryViewHolder {
            public NewsFeedViewHolder(View itemView) {
                super(itemView);
            }
        }

        // Now define the ViewHolder for Normal list item
        public class NormalViewHolder extends TransactionHistoryViewHolder {
            public NormalViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            if (viewType == WHATS_NEW_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_whats_new, parent, false);
                WhatsNewViewHolder vh = new WhatsNewViewHolder(v);
                return vh;

            } else if (viewType == TRANSACTION_HISTORY_HEADER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_header_transaction_histories, parent, false);
                HeaderViewHolder vh = new HeaderViewHolder(v);
                return vh;

            } else if (viewType == NEWS_FEED_ITEM_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news_feed, parent, false);
                NewsFeedViewHolder vh = new NewsFeedViewHolder(v);
                return vh;
            }

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction_history, parent, false);
            NormalViewHolder vh = new NormalViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof NormalViewHolder) {
                    NormalViewHolder vh = (NormalViewHolder) holder;
                    vh.bindViewTransactionHistory(position);
                } else if (holder instanceof WhatsNewViewHolder) {
                    WhatsNewViewHolder vh = (WhatsNewViewHolder) holder;
                } else if (holder instanceof HeaderViewHolder) {
                    HeaderViewHolder vh = (HeaderViewHolder) holder;
                } else if (holder instanceof NewsFeedViewHolder) {
                    NewsFeedViewHolder vh = (NewsFeedViewHolder) holder;
                    vh.bindViewNewsFeed(position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {

            int transactionHistoryListSize = 0;
            int newsFeedListSize = 0;

            if (userTransactionHistoryClasses == null && newsFeedResponsesList == null) return 0;

            if (userTransactionHistoryClasses != null)
                transactionHistoryListSize = userTransactionHistoryClasses.size();
            if (newsFeedResponsesList != null) newsFeedListSize = newsFeedResponsesList.size();

            if (transactionHistoryListSize > 0 && newsFeedListSize > 0)
                return 1 + transactionHistoryListSize + 1 + newsFeedListSize;   // Header, transaction histories, whats new header , news feed list
            else if (transactionHistoryListSize > 0 && newsFeedListSize == 0)
                return 1 + transactionHistoryListSize;                          // Header, transaction histories
            else if (transactionHistoryListSize == 0 && newsFeedListSize > 0)
                return 1 + newsFeedListSize;                                    // whats new header , news feed list
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {

            int transactionHistoryListSize = 0;
            int newsFeedListSize = 0;

            if (userTransactionHistoryClasses == null && newsFeedResponsesList == null)
                return super.getItemViewType(position);

            if (userTransactionHistoryClasses != null)
                transactionHistoryListSize = userTransactionHistoryClasses.size();
            if (newsFeedResponsesList != null) newsFeedListSize = newsFeedResponsesList.size();

            if (transactionHistoryListSize > 0 && newsFeedListSize > 0) {
                if (position == 0) return TRANSACTION_HISTORY_HEADER_VIEW;
                else if (position == transactionHistoryListSize + 1) return WHATS_NEW_VIEW;
                else if (position > transactionHistoryListSize + 1) return NEWS_FEED_ITEM_VIEW;
                else return super.getItemViewType(position);

            } else if (transactionHistoryListSize > 0 && newsFeedListSize == 0) {
                if (position == 0) return TRANSACTION_HISTORY_HEADER_VIEW;
                else return super.getItemViewType(position);

            } else if (transactionHistoryListSize == 0 && newsFeedListSize > 0) {
                if (position == 0) return WHATS_NEW_VIEW;
                else return NEWS_FEED_ITEM_VIEW;
            }

            return super.getItemViewType(position);
        }
    }
}