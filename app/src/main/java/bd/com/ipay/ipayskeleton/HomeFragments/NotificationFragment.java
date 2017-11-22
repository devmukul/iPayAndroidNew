package bd.com.ipay.ipayskeleton.HomeFragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.NotificationActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.PendingIntroducerReviewDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRoles.GetPendingRoleManagerInvitationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.InvoiceItem;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.BusinessRoleManagerInvitation;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.GetMoneyAndPaymentRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.GetMoneyAndPaymentRequestResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.MoneyAndPaymentRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.GetPendingIntroducerListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.PendingIntroducer;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.GetIntroductionRequestsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.IntroductionRequestClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class NotificationFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mGetMoneyAndPaymentRequestTask = null;
    private GetMoneyAndPaymentRequestResponse mGetMoneyAndPaymentRequestResponse;

    private HttpRequestPostAsyncTask mServiceChargeTask = null;
    private GetServiceChargeResponse mGetServiceChargeResponse;

    private HttpRequestGetAsyncTask mGetIntroductionRequestTask = null;
    private GetIntroductionRequestsResponse mIntroductionRequestsResponse;

    private HttpRequestGetAsyncTask mGetPendingIntroducerListTask = null;
    private GetPendingIntroducerListResponse mPendingIntroducerListResponse;

    private HttpRequestGetAsyncTask mGetPendingRoleManagerRequestTask = null;
    private GetPendingRoleManagerInvitationResponse mGetPendingRoleManagerInvitationResponse;

    private RecyclerView mNotificationsRecyclerView;
    private NotificationListAdapter mNotificationListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog mProgressDialog;
    private TextView mEmptyListTextView;

    private List<Notification> mNotifications;
    private List<MoneyAndPaymentRequest> mMoneyAndPaymentRequests;
    private List<IntroductionRequestClass> mIntroductionRequests;
    private List<PendingIntroducer> mPendingIntroducerList;
    private List<BusinessRoleManagerInvitation> mBusinessRoleManagerRequestsList;

    // These variables hold the information needed to populate the review dialog
    private List<InvoiceItem> mInvoiceItemList;
    private BigDecimal mAmount;
    private BigDecimal mVat;
    private BigDecimal mServiceCharge;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mPhotoUri;
    private long mMoneyRequestId;
    private String mTitle;
    private String mDescriptionOfRequest;

    private OnNotificationUpdateListener mOnNotificationUpdateListener;
    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mNotificationsRecyclerView = (RecyclerView) v.findViewById(R.id.list_notification);
        mProgressDialog = new ProgressDialog(getActivity());
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mNotificationListAdapter = new NotificationListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mNotificationsRecyclerView.setLayoutManager(mLayoutManager);
        mNotificationsRecyclerView.setAdapter(mNotificationListAdapter);
        if (getArguments() != null) {
            if (getArguments().getString(Constants.TAG).equals(Constants.RELOAD)) {
                refreshNotificationLists(getActivity());
            }
        }

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    refreshNotificationLists(getActivity());
                }
            }
        });

        return v;
    }

    public void onResume() {
        super.onResume();

        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(notificationBroadcastReceiver,
                new IntentFilter(Constants.NOTIFICATION_UPDATE_BROADCAST));
        if (Utilities.isConnectionAvailable(getActivity())) {
            refreshNotificationLists(getActivity());
        }
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_notifications));
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(notificationBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // We try to fetch all notification lists as soon as the home activity
        // is launched. So when the user navigates to this fragment, it might be possible that
        // all the lists have already been loaded. We have to call postProcessNotificationList
        // to properly update the view.
        postProcessNotificationList();
    }

    public void registerNotificationBroadcastReceiver(Context context) {
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(notificationBroadcastReceiver,
                new IntentFilter(Constants.NOTIFICATION_UPDATE_BROADCAST));
    }

    public void setOnNotificationUpdateListener(OnNotificationUpdateListener listener) {
        this.mOnNotificationUpdateListener = listener;
    }

    public void getNotificationLists(Context context) {
        getMoneyAndPaymentRequest(context);
        getIntroductionRequestList(context);
        getPendingIntroducersList(context);
        getPendingInvitationRequestsForRoleManager(context);
    }

    private void getPendingInvitationRequestsForRoleManager(Context context) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_ROLE_INVITATION_REQUEST))
            return;
        if (mGetPendingRoleManagerRequestTask != null)
            return;
        else {
            mGetPendingRoleManagerRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ROLE_MAANGER_REQUESTS,
                    Constants.BASE_URL_MM + Constants.URL_GET_ROLE_MANAGER_REQUESTS, context, this);
            mGetPendingRoleManagerRequestTask.mHttpResponseListener = this;
            mGetPendingRoleManagerRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public void refreshNotificationLists(Context context) {
        refreshIntroductionRequestList(context);
        refreshMoneyAndPaymentRequestList(context);
        refreshPendingIntroducerList(context);
        refreshBusinessRoleManagerList(context);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.action_search_contacts) != null)
            menu.findItem(R.id.action_search_contacts).setVisible(false);
    }

    private void getMoneyAndPaymentRequest(Context context) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.RECEIVED_REQUEST))
            return;

        if (mGetMoneyAndPaymentRequestTask != null) {
            return;
        }

        GetMoneyAndPaymentRequest mGetMoneyAndPaymentRequest = new GetMoneyAndPaymentRequest();
        // Get only pending requests
        mGetMoneyAndPaymentRequest.setStatus(Constants.MONEY_REQUEST_STATUS_PROCESSING);

        Gson gson = new Gson();
        String json = gson.toJson(mGetMoneyAndPaymentRequest);
        mGetMoneyAndPaymentRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_MONEY_AND_PAYMENT_REQUESTS,
                Constants.BASE_URL_SM + Constants.URL_GET_All_NOTIFICATIONS, json, context, this);
        mGetMoneyAndPaymentRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getIntroductionRequestList(Context context) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_INTRODUCERS))
            return;

        if (mGetIntroductionRequestTask != null) {
            return;
        }

        mGetIntroductionRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_RECOMMENDATION_REQUESTS,
                Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_NOT_APPROVED_INTRODUCTION_REQUESTS, context, this);
        mGetIntroductionRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getPendingIntroducersList(Context context) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_INTRODUCERS))
            return;

        if (mGetPendingIntroducerListTask != null) {
            return;
        }

        mGetPendingIntroducerListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PENDING_INTRODUCER_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_PENDING_INTRODUCER, context, this);
        mGetPendingIntroducerListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptGetServiceCharge(int serviceId) {

        if (mServiceChargeTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();

        int accountType = ProfileInfoCacheManager.getAccountType();
        int accountClass = Constants.DEFAULT_USER_CLASS;

        GetServiceChargeRequest mServiceChargeRequest = new GetServiceChargeRequest(serviceId, accountType, accountClass);
        Gson gson = new Gson();
        String json = gson.toJson(mServiceChargeRequest);
        mServiceChargeTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_SERVICE_CHARGE,
                Constants.BASE_URL_SM + Constants.URL_SERVICE_CHARGE, json, getActivity());
        mServiceChargeTask.mHttpResponseListener = this;
        mServiceChargeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshMoneyAndPaymentRequestList(Context context) {
        if (Utilities.isConnectionAvailable(context)) {
            mMoneyAndPaymentRequests = null;
            getMoneyAndPaymentRequest(context);
        }
    }

    private void refreshIntroductionRequestList(Context context) {
        if (Utilities.isConnectionAvailable(context)) {
            mIntroductionRequests = null;
            getIntroductionRequestList(context);
        }
    }

    private void refreshBusinessRoleManagerList(Context context) {
        if (Utilities.isConnectionAvailable(context)) {
            mBusinessRoleManagerRequestsList = null;
            getPendingInvitationRequestsForRoleManager(context);
        }
    }

    private void refreshPendingIntroducerList(Context context) {
        if (Utilities.isConnectionAvailable(context)) {
            mPendingIntroducerList = null;
            getPendingIntroducersList(context);
        }
    }

    private boolean isAllNotificationsLoaded() {
        return mGetMoneyAndPaymentRequestTask == null && mGetIntroductionRequestTask == null && mGetPendingRoleManagerRequestTask == null;
    }

    private List<Notification> mergeNotificationLists() {
        List<Notification> notifications = new ArrayList<>();
        if (mMoneyAndPaymentRequests != null)
            notifications.addAll(mMoneyAndPaymentRequests);
        if (mIntroductionRequests != null)
            notifications.addAll(mIntroductionRequests);
        if (mPendingIntroducerList != null)
            notifications.addAll(mPendingIntroducerList);
        if (mBusinessRoleManagerRequestsList != null)
            notifications.addAll(mBusinessRoleManagerRequestsList);

        // Date wise sort all notifications
        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification lhs, Notification rhs) {
                if (lhs.getTime() > rhs.getTime())
                    return -1;
                else if (lhs.getTime() < rhs.getTime())
                    return 1;
                else
                    return 0;
            }
        });

        return notifications;
    }

    private void postProcessNotificationList() {
        if (isAllNotificationsLoaded()) {

            mNotifications = mergeNotificationLists();
            if (isAdded()) {
                if (mNotifications.isEmpty()) {
                    mEmptyListTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyListTextView.setVisibility(View.GONE);
                }

                mSwipeRefreshLayout.setRefreshing(false);
                mNotificationListAdapter.notifyDataSetChanged();
                setContentShown(true);
            }

            // We just can't call something like getActivity().onNotificationUpdate.. because
            // getActivity() might return if user hasn't yet navigated to the notification fragment.
            if (mOnNotificationUpdateListener != null && mNotifications != null)
                mOnNotificationUpdateListener.onNotificationUpdate(mNotifications);
        }
    }

    private void launchPaymentRequestsReceivedDetailsFragment() {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.MONEY_REQUEST_ID, mMoneyRequestId);
        bundle.putString(Constants.MOBILE_NUMBER, mReceiverMobileNumber);
        bundle.putString(Constants.NAME, mReceiverName);
        bundle.putInt(Constants.MONEY_REQUEST_SERVICE_ID, Constants.SERVICE_ID_REQUEST_MONEY);
        bundle.putString(Constants.VAT, mVat.toString());
        bundle.putString(Constants.PHOTO_URI, mPhotoUri);
        bundle.putString(Constants.AMOUNT, mAmount.toString());
        bundle.putString(Constants.TITLE, mTitle);
        bundle.putString(Constants.DESCRIPTION, mDescriptionOfRequest);
        bundle.putString(Constants.TAG, Constants.REQUEST_PAYMENT);

        if (mInvoiceItemList != null)
            bundle.putParcelableArrayList(Constants.INVOICE_ITEM_NAME_TAG, new ArrayList<>(mInvoiceItemList));
        else
            bundle.putParcelableArrayList(Constants.INVOICE_ITEM_NAME_TAG, null);

        Intent intent = new Intent(this.getContext(), NotificationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void launchReceivedRequestFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_RECEIVED_REQUEST);
        bundle.putSerializable(Constants.AMOUNT, mAmount);
        bundle.putString(Constants.RECEIVER_MOBILE_NUMBER, ContactEngine.formatMobileNumberBD(mReceiverMobileNumber));
        bundle.putString(Constants.DESCRIPTION_TAG, mDescriptionOfRequest);
        bundle.putLong(Constants.MONEY_REQUEST_ID, mMoneyRequestId);
        bundle.putString(Constants.NAME, mReceiverName);
        bundle.putString(Constants.PHOTO_URI, mPhotoUri);
        bundle.putString(Constants.TAG, Constants.REQUEST);

        Intent intent = new Intent(this.getContext(), NotificationActivity.class);
        intent.putExtras(bundle);
        intent.putExtra(Constants.IS_IN_CONTACTS,
                new ContactSearchHelper(getActivity()).searchMobileNumber(mReceiverMobileNumber));
        startActivity(intent);
    }

    private void launchIntroductionRequestReviewFragment(final IntroductionRequestClass introductionRequest) {
        final long requestID = introductionRequest.getId();

        final String senderName = introductionRequest.getName();
        final String senderMobileNumber = introductionRequest.getSenderMobileNumber();
        final String photoUri = introductionRequest.getImageUrl();

        final AddressClass mAddress = introductionRequest.getPresentAddress();
        final String fathersName = introductionRequest.getFather();
        final String mothersName = introductionRequest.getMother();

        Bundle bundle = new Bundle();
        bundle.putLong(Constants.REQUEST_ID, requestID);
        bundle.putString(Constants.NAME, senderName);
        bundle.putString(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + photoUri);
        bundle.putString(Constants.MOBILE_NUMBER, senderMobileNumber);
        bundle.putString(Constants.FATHERS_NAME, fathersName);
        bundle.putString(Constants.MOTHERS_NAME, mothersName);
        bundle.putSerializable(Constants.ADDRESS, mAddress);
        bundle.putString(Constants.TAG, Constants.RECOMMENDATION);
        bundle.putBoolean(Constants.IS_IN_CONTACTS,
                new ContactSearchHelper(getActivity()).searchMobileNumber(senderMobileNumber));

        Intent intent = new Intent(getActivity(), NotificationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void launchBusinessRoleReviewFragment(final BusinessRoleManagerInvitation businessRoleManagerInvitation) {
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonString = gson.toJson(businessRoleManagerInvitation);
        bundle.putString(Constants.BUSINESS_ROLE_REQUEST, jsonString);
        ((NotificationActivity) getActivity()).switchToBusinessRoleReviewFragment(bundle);

    }

    public interface OnNotificationUpdateListener {
        void onNotificationUpdate(List<Notification> notifications);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mGetMoneyAndPaymentRequestTask = null;
            mServiceChargeTask = null;
            mGetIntroductionRequestTask = null;
            mGetPendingIntroducerListTask = null;
            mGetPendingRoleManagerRequestTask = null;

            if (isAdded()) {
                mSwipeRefreshLayout.setRefreshing(false);
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.fetch_notification_failed, Toast.LENGTH_LONG);
            }

            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_MONEY_AND_PAYMENT_REQUESTS:
                try {
                    mGetMoneyAndPaymentRequestResponse = gson.fromJson(result.getJsonString(), GetMoneyAndPaymentRequestResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        try {
                            mMoneyAndPaymentRequests = mGetMoneyAndPaymentRequestResponse.getAllMoneyAndPaymentRequests();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getActivity() != null)
                                Toaster.makeText(getActivity(), mGetMoneyAndPaymentRequestResponse.getMessage(), Toast.LENGTH_LONG);
                        }

                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), R.string.fetch_notification_failed, Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mGetMoneyAndPaymentRequestTask = null;
                postProcessNotificationList();
                break;

            case Constants.COMMAND_GET_RECOMMENDATION_REQUESTS:
                try {
                    mIntroductionRequestsResponse = gson.fromJson(result.getJsonString(), GetIntroductionRequestsResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mIntroductionRequests = mIntroductionRequestsResponse.getVerificationRequestList();
                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mIntroductionRequestsResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                }

                mGetIntroductionRequestTask = null;
                postProcessNotificationList();
                break;

            case Constants.COMMAND_GET_PENDING_INTRODUCER_LIST:
                try {
                    mPendingIntroducerListResponse = gson.fromJson(result.getJsonString(), GetPendingIntroducerListResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mPendingIntroducerList = mPendingIntroducerListResponse.getWantToBeIntroducers();
                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mIntroductionRequestsResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                }

                mGetPendingIntroducerListTask = null;
                postProcessNotificationList();
                break;

            case Constants.COMMAND_GET_SERVICE_CHARGE:
                mProgressDialog.dismiss();
                try {
                    mGetServiceChargeResponse = gson.fromJson(result.getJsonString(), GetServiceChargeResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (mGetServiceChargeResponse != null) {
                            mServiceCharge = mGetServiceChargeResponse.getServiceCharge(mAmount);

                            if (mServiceCharge.compareTo(BigDecimal.ZERO) < 0) {
                                Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                            } else {
                                launchReceivedRequestFragment();
                            }

                        } else {
                            Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                            return;
                        }
                    } else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                }

                mServiceChargeTask = null;
                break;
            case Constants.COMMAND_GET_ROLE_MAANGER_REQUESTS:
                try {
                    mGetPendingRoleManagerInvitationResponse = gson.fromJson(result.getJsonString(),
                            GetPendingRoleManagerInvitationResponse.class);
                    mBusinessRoleManagerRequestsList = mGetPendingRoleManagerInvitationResponse.getInvitationList();
                } catch (Exception e) {

                }
                mGetPendingRoleManagerRequestTask = null;
                postProcessNotificationList();
                break;
            default:
                break;
        }
    }

    private class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder> {

        public class NotificationViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTitleView;
            private final TextView mNameView;
            private final TextView mTimeView;
            private final ProfileImageView mProfileImageView;

            public NotificationViewHolder(final View itemView) {
                super(itemView);

                mTitleView = (TextView) itemView.findViewById(R.id.textview_title);
                mNameView = (TextView) itemView.findViewById(R.id.textview_description);
                mTimeView = (TextView) itemView.findViewById(R.id.textview_time);
                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
            }

            public void bindView(int pos) {

                Notification notification = mNotifications.get(pos);

                mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + notification.getImageUrl(), false);
                mNameView.setText(notification.getName());

                mTimeView.setText(Utilities.formatDateWithTime(notification.getTime()));

                if (notification.getNotificationTitle() != null && !notification.getNotificationTitle().equals("")) {
                    mTitleView.setVisibility(View.VISIBLE);
                    mTitleView.setText(notification.getNotificationTitle());

                } else {
                    mTitleView.setVisibility(View.GONE);
                }

            }
        }

        public class MoneyAndPaymentRequestViewHolder extends NotificationViewHolder {
            private final TextView mAmountView;

            public MoneyAndPaymentRequestViewHolder(final View itemView) {

                super(itemView);
                mAmountView = (TextView) itemView.findViewById(R.id.textview_amount);

            }

            @Override
            public void bindView(int pos) {
                super.bindView(pos);

                MoneyAndPaymentRequest moneyAndPaymentRequest = (MoneyAndPaymentRequest) mNotifications.get(pos);

                final String imageUrl = moneyAndPaymentRequest.getOriginatorProfile().getUserProfilePicture();
                final String name = moneyAndPaymentRequest.originatorProfile.getUserName();
                final String mobileNumber = moneyAndPaymentRequest.originatorProfile.getUserMobileNumber();
                final String descriptionOfRequest = moneyAndPaymentRequest.getDescriptionofRequest();
                final String title = moneyAndPaymentRequest.getTitle();
                final long id = moneyAndPaymentRequest.getId();
                final BigDecimal amount = moneyAndPaymentRequest.getAmount();
                final int serviceID = moneyAndPaymentRequest.getServiceID();
                final BigDecimal vat = moneyAndPaymentRequest.getVat();
                final List<InvoiceItem> itemList = moneyAndPaymentRequest.getItemList();

                mAmountView.setText(Utilities.formatTaka(amount));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMoneyRequestId = id;
                        mAmount = amount;
                        mReceiverName = name;
                        mReceiverMobileNumber = mobileNumber;
                        mPhotoUri = Constants.BASE_URL_FTP_SERVER + imageUrl;
                        mTitle = title;
                        mDescriptionOfRequest = descriptionOfRequest;
                        mVat = vat;
                        mInvoiceItemList = itemList;

                        if (serviceID == Constants.SERVICE_ID_REQUEST_MONEY)
                            attemptGetServiceCharge(Constants.SERVICE_ID_SEND_MONEY);
                        else {
                            launchPaymentRequestsReceivedDetailsFragment();
                        }
                    }
                });

            }
        }

        public class IntroductionRequestViewHolder extends NotificationViewHolder {

            public IntroductionRequestViewHolder(final View itemView) {
                super(itemView);
            }

            @Override
            public void bindView(final int pos) {
                super.bindView(pos);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_INTRODUCERS)
                    public void onClick(View v) {
                        launchIntroductionRequestReviewFragment((IntroductionRequestClass) mNotifications.get(pos));
                    }
                });
            }

        }

        public class BusinessRoleManagerViewHolder extends NotificationViewHolder {

            public BusinessRoleManagerViewHolder(final View itemView) {
                super(itemView);
            }

            @Override
            public void bindView(int pos) {
                super.bindView(pos);
                final int position = pos;
                BusinessRoleManagerInvitation businessRoleManagerInvitation = (BusinessRoleManagerInvitation) mNotifications.get(pos);
                String businessName = businessRoleManagerInvitation.getBusinessName();
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchBusinessRoleReviewFragment((BusinessRoleManagerInvitation) mNotifications.get(position));
                    }
                });

            }
        }


        public class PendingIntroductionListViewHolder extends NotificationViewHolder {

            public PendingIntroductionListViewHolder(final View itemView) {
                super(itemView);

            }

            @Override
            public void bindView(int pos) {
                super.bindView(pos);

                final PendingIntroducer pendingIntroducer = (PendingIntroducer) mNotifications.get(pos);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_INTRODUCERS)
                    public void onClick(View v) {
                        new PendingIntroducerReviewDialog(getActivity(), pendingIntroducer).setActionCheckerListener(
                                new PendingIntroducerReviewDialog.ActionCheckerListener() {
                                    @Override
                                    public void ifFinishNeeded() {
                                        if (Utilities.isConnectionAvailable(getActivity())) {
                                            refreshNotificationLists(getActivity());
                                        }
                                    }
                                });
                    }
                });
            }

        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            if (viewType == Constants.NOTIFICATION_TYPE_INTRODUCTION_REQUEST) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduction_requests_notification, parent, false);
                return new IntroductionRequestViewHolder(v);
            } else if (viewType == Constants.NOTIFICATION_TYPE_PENDING_INTRODUCER_REQUEST) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduction_requests_notification, parent, false);
                return new PendingIntroductionListViewHolder(v);
            } else if (viewType == Constants.NOTIFICATION_TYPE_PENDING_ROLE_MANAGER_REQUEST) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_business_role_manager_requests_notification,
                        parent, false);
                return new BusinessRoleManagerViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_money_and_make_payment_request, parent, false);
                return new MoneyAndPaymentRequestViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
            try {
                holder.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mNotifications == null)
                return 0;
            else
                return mNotifications.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mNotifications.get(position).getNotificationType();
        }
    }

    private class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshNotificationLists(context);
        }
    }
}
