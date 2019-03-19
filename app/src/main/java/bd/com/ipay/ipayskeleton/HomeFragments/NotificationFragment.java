package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.NotificationActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.PendingIntroducerReviewDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRoles.GetPendingRoleManagerInvitationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
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
import bd.com.ipay.ipayskeleton.SourceOfFund.EditPermissionSourceOfFundBottomSheetFragment;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AcceptOrRejectBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.AcceptOrRejectSponsorRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Beneficiary;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetBeneficiaryListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.SourceOfFund.view.BeneficiaryUpdateDialog;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class NotificationFragment extends ProgressFragment implements bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener, BeneficiaryUpdateDialog.BeneficiaryAddSuccessListener {

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

	private HttpRequestGetAsyncTask mGetBeneficiaryAsyncTask;
	private GetBeneficiaryListResponse getBeneficiaryListResponse;
	private List<Beneficiary> beneficiaryList;
	private List<Beneficiary> beneficiaryPendingList;

	private HttpRequestGetAsyncTask mGetSponsorAsyncTask;
	private GetSponsorListResponse getSponsorListResponse;
	private List<Sponsor> sponsorList;
	private List<Sponsor> sponsorPendingList;

	private HttpRequestPutAsyncTask acceptOrRejectBeneficiaryAsyncTask;

	private RecyclerView mNotificationsRecyclerView;
	private NotificationListAdapter mNotificationListAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	private CustomSwipeRefreshLayout mSwipeRefreshLayout;
	private CustomProgressDialog mProgressDialog;
	private TextView mEmptyListTextView;

	private List<Notification> mNotifications;
	private List<MoneyAndPaymentRequest> mMoneyAndPaymentRequests;
	private List<IntroductionRequestClass> mIntroductionRequests;
	private List<PendingIntroducer> mPendingIntroducerList;
	private List<BusinessRoleManagerInvitation> mBusinessRoleManagerRequestsList;

	// These variables hold the information needed to populate the review dialog
	private List<InvoiceItem> mInvoiceItemList;
	private BigDecimal mAmount;
	private int mStatus;
	private BigDecimal mServiceCharge;
	private String mReceiverName;
	private String mReceiverMobileNumber;
	private String mPhotoUri;
	private long mMoneyRequestId;
	private String mTitle;
	private String mDescriptionOfRequest;
	public BottomSheetBehavior<RelativeLayout> bottomSheetBehavior;

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
		mProgressDialog = new CustomProgressDialog(getActivity());
		mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
		mEmptyListTextView.setText(getString(R.string.nothing_to_show_right_now));
		getNotificationLists(getActivity());
		mNotificationListAdapter = new NotificationListAdapter();
		mLayoutManager = new LinearLayoutManager(getActivity());
		mNotificationsRecyclerView.setLayoutManager(mLayoutManager);

		final RelativeLayout relativeLayout = v.findViewById(R.id.test_bottom_sheet_layout);
		bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);

		relativeLayout.findViewById(R.id.test_bottom_sheet_layout).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

					}
				}
		);
		mNotificationsRecyclerView.setAdapter(mNotificationListAdapter);

		mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshNotificationLists(getActivity());
			}
		});

		return v;
	}

	public void onResume() {
		super.onResume();

		notificationBroadcastReceiver = new NotificationBroadcastReceiver();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(notificationBroadcastReceiver,
				new IntentFilter(Constants.NOTIFICATION_UPDATE_BROADCAST));
		refreshNotificationLists(getActivity());

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
		getPendingBeneficiaryListResponse(context);
		getPendingSponsorListResponse(context);
	}


	private void getPendingInvitationRequestsForRoleManager(Context context) {
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BUSINESS_ROLE_INVITATION_REQUEST))
			return;
		if (mGetPendingRoleManagerRequestTask != null)
			return;
		else {
			mGetPendingRoleManagerRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ROLE_MAANGER_REQUESTS,
					Constants.BASE_URL_MM + Constants.URL_GET_ROLE_MANAGER_REQUESTS, context, this, true);
			mGetPendingRoleManagerRequestTask.mHttpResponseListener = this;
			mGetPendingRoleManagerRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void getPendingBeneficiaryListResponse(Context context) {

		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
			return;
		}

		if (mGetBeneficiaryAsyncTask != null) {
			return;
		} else {
			String uri = Constants.BASE_URL_MM + Constants.URL_GET_BENEFICIARY;
			mGetBeneficiaryAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BENEFICIARY, uri,
					context, this, false);
			mGetBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void getPendingSponsorListResponse(Context context) {
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
			return;
		}

		if (mGetSponsorAsyncTask != null) {
			return;
		} else {
			String uri = Constants.BASE_URL_MM + Constants.URL_GET_SPONSOR;
			mGetSponsorAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SPONSOR_LIST, uri,
					context, this, true);
			mGetSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}


	public void refreshNotificationLists(Context context) {
		refreshIntroductionRequestList(context);
		refreshMoneyAndPaymentRequestList(context);
		refreshPendingIntroducerList(context);
		refreshBusinessRoleManagerList(context);
		refreshSourceOfFundBeneficiaryList(context);
		refreshSourceOfFundSponsorList(context);
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
				Constants.BASE_URL_SM + Constants.URL_GET_All_NOTIFICATIONS, json, context, this, true);
		mGetMoneyAndPaymentRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getIntroductionRequestList(Context context) {
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_INTRODUCERS))
			return;

		if (mGetIntroductionRequestTask != null) {
			return;
		}

		mGetIntroductionRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_RECOMMENDATION_REQUESTS,
				Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_NOT_APPROVED_INTRODUCTION_REQUESTS, context, this, true);
		mGetIntroductionRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getPendingIntroducersList(Context context) {
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_INTRODUCERS))
			return;

		if (mGetPendingIntroducerListTask != null) {
			return;
		}

		mGetPendingIntroducerListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PENDING_INTRODUCER_LIST,
				Constants.BASE_URL_MM + Constants.URL_GET_PENDING_INTRODUCER, context, this, false);
		mGetPendingIntroducerListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void attemptGetServiceCharge(int serviceId) {

		if (mServiceChargeTask != null) {
			return;
		}

		mProgressDialog.show();

		int accountType = ProfileInfoCacheManager.getAccountType();
		int accountClass = Constants.DEFAULT_USER_CLASS;

		GetServiceChargeRequest mServiceChargeRequest = new GetServiceChargeRequest(serviceId, accountType, accountClass);
		Gson gson = new Gson();
		String json = gson.toJson(mServiceChargeRequest);
		mServiceChargeTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_SERVICE_CHARGE,
				Constants.BASE_URL_SM + Constants.URL_SERVICE_CHARGE, json, getActivity(), true);
		mServiceChargeTask.mHttpResponseListener = this;
		mServiceChargeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void refreshMoneyAndPaymentRequestList(Context context) {
		mMoneyAndPaymentRequests = null;
		getMoneyAndPaymentRequest(context);

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

	private void refreshSourceOfFundBeneficiaryList(Context context) {
		if (Utilities.isConnectionAvailable(context)) {
			beneficiaryPendingList = null;
			getPendingBeneficiaryListResponse(context);
		}
	}

	private void refreshSourceOfFundSponsorList(Context context) {
		if (Utilities.isConnectionAvailable(context)) {
			sponsorPendingList = null;
			getPendingSponsorListResponse(context);
		}
	}

	private void refreshPendingIntroducerList(Context context) {
		if (Utilities.isConnectionAvailable(context)) {
			mPendingIntroducerList = null;
			getPendingIntroducersList(context);
		}
	}

	private boolean isAllNotificationsLoaded() {
		return mGetMoneyAndPaymentRequestTask == null && mGetIntroductionRequestTask == null
				&& mGetPendingRoleManagerRequestTask == null && mGetBeneficiaryAsyncTask ==
				null && mGetSponsorAsyncTask == null;
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
		if (beneficiaryPendingList != null) {
			notifications.addAll(beneficiaryPendingList);
		}
		if (sponsorPendingList != null) {
			notifications.addAll(sponsorPendingList);
		}

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
		bundle.putInt(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_RECEIVED_REQUEST);
		bundle.putString(Constants.AMOUNT, mAmount.toString());
		bundle.putString(Constants.RECEIVER_MOBILE_NUMBER, ContactEngine.formatMobileNumberBD(mReceiverMobileNumber));
		bundle.putString(Constants.DESCRIPTION, mDescriptionOfRequest);
		bundle.putLong(Constants.MONEY_REQUEST_ID, mMoneyRequestId);
		bundle.putString(Constants.NAME, mReceiverName);
		bundle.putString(Constants.PHOTO_URI, mPhotoUri);
		bundle.putInt(Constants.STATUS, mStatus);
		bundle.putBoolean(Constants.IS_IN_CONTACTS, new ContactSearchHelper(getActivity()).searchMobileNumber(mReceiverMobileNumber));
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

	private void getOnlyPendingEntriesSourceOfFund() {
		beneficiaryPendingList = new ArrayList<>();
		for (int i = 0; i < beneficiaryList.size(); i++) {
			if (beneficiaryList.get(i).getStatus().equals("PENDING") && !beneficiaryList.get(i).getInitiatedBy().equals("SPONSOR")) {
				Beneficiary beneficiary = beneficiaryList.get(i);
				beneficiary.setType(Constants.BENEFICIARY);
				beneficiaryPendingList.add(beneficiary);
			}
		}

	}

	private void getOnlyPendingSponsorEntries() {
		sponsorPendingList = new ArrayList<>();
		for (int i = 0; i < sponsorList.size(); i++) {
			if (sponsorList.get(i).getStatus().equals("PENDING") && !sponsorList.get(i).getInitiatedBy().equals("BENEFICIARY")) {
				Sponsor sponsor = sponsorList.get(i);
				sponsor.setType(Constants.SPONSOR);
				sponsorPendingList.add(sponsor);
			}
		}
	}

	public boolean onBackPressed() {
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBeneficiaryAdded() {
		NotificationFragment.this.refreshNotificationLists(getContext());
	}

	public interface OnNotificationUpdateListener {
		void onNotificationUpdate(List<Notification> notifications);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		try {

			if (HttpErrorHandler.isErrorFound(result, getActivity(), mProgressDialog)) {
				mGetMoneyAndPaymentRequestTask = null;
				mServiceChargeTask = null;
				mGetIntroductionRequestTask = null;
				mGetPendingIntroducerListTask = null;
				mGetPendingRoleManagerRequestTask = null;
				acceptOrRejectBeneficiaryAsyncTask = null;
				mGetSponsorAsyncTask = null;
				mGetBeneficiaryAsyncTask = null;
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				setContentShown(true);

				if (isAdded()) {
					mSwipeRefreshLayout.setRefreshing(false);
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

				case Constants.COMMAND_GET_BENEFICIARY:
					try {
						getBeneficiaryListResponse = gson.fromJson(result.getJsonString(), GetBeneficiaryListResponse.class);
						if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
							beneficiaryList = getBeneficiaryListResponse.getBeneficiary();
							getOnlyPendingEntriesSourceOfFund();
						} else {
							Toast.makeText(getContext(), getBeneficiaryListResponse.getMessage(), Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
					}
					mGetBeneficiaryAsyncTask = null;
					postProcessNotificationList();
					break;

				case Constants.COMMAND_GET_SPONSOR_LIST:
					try {
						getSponsorListResponse = gson.fromJson(result.getJsonString(), GetSponsorListResponse.class);
						if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
							sponsorList = getSponsorListResponse.getSponsor();
							getOnlyPendingSponsorEntries();
						} else {
							Toast.makeText(getContext(), getSponsorListResponse.getMessage(), Toast.LENGTH_LONG).show();

						}
					} catch (Exception e) {
						e.printStackTrace();
						Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
					}
					mGetSponsorAsyncTask = null;
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

				case Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY:
					try {
						mProgressDialog.dismiss();
						GenericResponseWithMessageOnly responseWithMessageOnly = gson.fromJson(result.getJsonString(),
								GenericResponseWithMessageOnly.class);
						if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
							refreshNotificationLists(getContext());
							Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
						}

					} catch (Exception e) {

					}
					acceptOrRejectBeneficiaryAsyncTask = null;
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
		} catch (Exception e) {
			mGetMoneyAndPaymentRequestTask = null;
			mServiceChargeTask = null;
			mGetIntroductionRequestTask = null;
			mGetPendingIntroducerListTask = null;
			mGetPendingRoleManagerRequestTask = null;
			acceptOrRejectBeneficiaryAsyncTask = null;

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
				if (!(notification instanceof BusinessRoleManagerInvitation) &&
						!(notification instanceof Beneficiary) &&
						!(notification instanceof Sponsor)) {

					mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + notification.getImageUrl(), false);
					mNameView.setText(notification.getName());

					mTimeView.setText(Utilities.formatDateWithTime(notification.getTime()));

					if (notification.getNotificationTitle() != null && !notification.getNotificationTitle().equals("")) {
						mTitleView.setVisibility(View.VISIBLE);
						if (notification instanceof BusinessRoleManagerInvitation)
							mTitleView.setText(Html.fromHtml(notification.getNotificationTitle()));
						else
							mTitleView.setText(notification.getNotificationTitle());

					} else {
						mTitleView.setVisibility(View.GONE);
					}
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
				final String descriptionOfRequest = moneyAndPaymentRequest.getDescriptionOfRequest();
				final String title = moneyAndPaymentRequest.getTitle();
				final long id = moneyAndPaymentRequest.getId();
				final BigDecimal amount = moneyAndPaymentRequest.getAmount();
				final int serviceID = moneyAndPaymentRequest.getServiceID();
				final List<InvoiceItem> itemList = moneyAndPaymentRequest.getItemList();
				final int status = moneyAndPaymentRequest.getStatus();

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
						mStatus = status;
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

		public class SourceOfFundBeneficiaryViewHolder extends NotificationViewHolder {
			private RoundedImageView profileImageView;
			private TextView timeTextView;
			private TextView descriptionTextView;
			private TextView acceptTextView;
			private TextView rejectTextView;
			private TextView titleTextView;


			public SourceOfFundBeneficiaryViewHolder(View itemView) {
				super(itemView);
				profileImageView = (RoundedImageView) itemView.findViewById(R.id.profile_image);
				timeTextView = (TextView) itemView.findViewById(R.id.time);
				descriptionTextView = (TextView) itemView.findViewById(R.id.description);
				titleTextView = (TextView) itemView.findViewById(R.id.helper);
				acceptTextView = (TextView) itemView.findViewById(R.id.accept);
				rejectTextView = (TextView) itemView.findViewById(R.id.reject);
			}

			@Override
			public void bindView(int pos) {
				super.bindView(pos);
				final int position = pos;
				if (mNotifications.get(pos) instanceof Beneficiary) {
					titleTextView.setText(getString(R.string.add_beneficiary_question));
					final Beneficiary beneficiary = (Beneficiary) mNotifications.get(pos);
					Glide.with(getContext())
							.load(Constants.BASE_URL_FTP_SERVER + beneficiary.getImageUrl())
							.centerCrop()
							.error(R.drawable.user_brand_bg)
							.into(profileImageView);
					timeTextView.setText(Utilities.formatDateWithTime(beneficiary.getUpdatedAt()));

					String description = "";
					description = getString(R.string.beneficiary_request_message, beneficiary.getName());
					descriptionTextView.setText(description);
					acceptTextView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {

							new PinChecker(getContext(), new PinChecker.PinCheckerListener() {
								@Override
								public void ifPinAdded() {
									Bundle bundle = new Bundle();
									bundle.putSerializable(Constants.BENEFICIARY, beneficiary);
									bundle.putString(Constants.TO_DO, Constants.UPDATE_STATUS);
									EditPermissionSourceOfFundBottomSheetFragment editPermissionSourceOfFundBottomSheetFragment
											= new EditPermissionSourceOfFundBottomSheetFragment();

									editPermissionSourceOfFundBottomSheetFragment.setArguments(bundle);
									getChildFragmentManager().beginTransaction().
											replace(R.id.test_fragment_container, editPermissionSourceOfFundBottomSheetFragment).commit();
									bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

									bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
										@Override
										public void onStateChanged(@NonNull View view, int i) {
											if (i == BottomSheetBehavior.STATE_COLLAPSED) {
												Utilities.hideKeyboard(getActivity());
											}
										}

										@Override
										public void onSlide(@NonNull View view, float v) {

										}
									});

									editPermissionSourceOfFundBottomSheetFragment.setHttpResponseListener(new EditPermissionSourceOfFundBottomSheetFragment.HttpResponseListener() {
										@Override
										public void onSuccess() {
											bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
											Utilities.hideKeyboard(getActivity());
											refreshNotificationLists(getContext());
										}
									});
								}
							}).execute();

						}

					});

					rejectTextView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							attemptAcceptOrRejectBeneficiary(Constants.BENEFICIARY,
									beneficiary.getId(), "REJECTED");
						}
					});
				} else if (mNotifications.get(pos) instanceof Sponsor) {
					final Sponsor sponsor = (Sponsor) mNotifications.get(pos);
					titleTextView.setText(getString(R.string.add_sponsor_question));
					Glide.with(getContext())
							.load(Constants.BASE_URL_FTP_SERVER + sponsor.getImageUrl())
							.centerCrop()
							.error(R.drawable.user_brand_bg)
							.into(profileImageView);
					timeTextView.setText(Utilities.formatDateWithTime(sponsor.getUpdatedAt()));
					descriptionTextView.setText(getString(R.string.sponsor_request_string, sponsor.getName()));
					acceptTextView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							new AlertDialog.Builder(getContext())
									.setMessage(getString(R.string.accept_request))
									.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											attemptAddBeneficiaryOrSponsor(Constants.SPONSOR, null, sponsor.getId(), "APPROVED");
										}
									}).setNegativeButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.dismiss();
								}
							}).show();

						}
					});
					rejectTextView.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							attemptAcceptOrRejectBeneficiary(Constants.SPONSOR, sponsor.getId(), "REJECTED");
						}
					});
				}
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
						new PendingIntroducerReviewDialog(getActivity(), pendingIntroducer)
								.setActionCheckerListener(
										new PendingIntroducerReviewDialog.ActionCheckerListener() {
											@Override
											public void ifFinishNeeded() {
												refreshNotificationLists(getActivity());
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
				v = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.list_item_introduction_requests_notification, parent,
						false);
				return new IntroductionRequestViewHolder(v);
			} else if (viewType == Constants.NOTIFICATION_TYPE_PENDING_INTRODUCER_REQUEST) {
				v = LayoutInflater.from(parent.getContext()).inflate
						(R.layout.list_item_introduction_requests_notification, parent,
								false);
				return new PendingIntroductionListViewHolder(v);
			} else if (viewType == Constants.NOTIFICATION_TYPE_SOURCE_OF_FUND_BENEFICIARIES) {
				v = LayoutInflater.from(parent.getContext()).inflate
						(R.layout.list_item_get_beneficiaries,
								parent, false);
				return new SourceOfFundBeneficiaryViewHolder(v);
			} else if (viewType == Constants.NOTIFICATION_TYPE_SOURCE_OF_FUND_SPONSORS) {
				v = LayoutInflater.from(parent.getContext()).inflate
						(R.layout.list_item_get_beneficiaries,
								parent, false);
				return new SourceOfFundBeneficiaryViewHolder(v);
			} else {
				v = LayoutInflater.from(parent.getContext()).inflate
						(R.layout.list_item_money_and_make_payment_request, parent,
								false);
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

	private void attemptAcceptOrRejectBeneficiary(final String type, final long id, final String action) {
		if (action.equals("REJECTED")) {
			new AlertDialog.Builder(getContext()).setMessage("Do you want to reject the request ?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							attemptAddBeneficiaryOrSponsor(type, "", id, action);
						}
					}).show();

		} else if (action.equals("APPROVED")) {
			new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
				@Override
				public void ifPinCheckedAndAdded(String pin) {
					attemptAddBeneficiaryOrSponsor(type, pin, id, action);
				}
			});
		}

	}

	private void attemptAddBeneficiaryOrSponsor(String type, String pin, long id, String action) {
		if (acceptOrRejectBeneficiaryAsyncTask != null) {
			return;
		} else {
			AcceptOrRejectBeneficiaryRequest acceptOrRejectBeneficiaryRequest = null;
			AcceptOrRejectSponsorRequest acceptOrRejectSponsorRequest = null;
			if (type.equals(Constants.BENEFICIARY)) {
				acceptOrRejectBeneficiaryRequest = new AcceptOrRejectBeneficiaryRequest
						(Constants.DEFAULT_CREDIT_LIMIT, pin, action);
				acceptOrRejectBeneficiaryAsyncTask = new HttpRequestPutAsyncTask(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY,
						Constants.BASE_URL_MM +
								Constants.URL_ACCEPT_OR_REJECT_SOURCE_OF_FUND + "beneficiary/" + id,
						new Gson().toJson(acceptOrRejectBeneficiaryRequest), getContext(), this, false);
				acceptOrRejectBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				mProgressDialog.show();

			} else {
				acceptOrRejectSponsorRequest = new AcceptOrRejectSponsorRequest
						(action);
				acceptOrRejectBeneficiaryAsyncTask = new HttpRequestPutAsyncTask(Constants.COMMAND_ACCEPT_OR_REJECT_BENEFICIARY,
						Constants.BASE_URL_MM +
								Constants.URL_ACCEPT_OR_REJECT_SOURCE_OF_FUND + "sponsor/" + id,
						new Gson().toJson(acceptOrRejectSponsorRequest), getContext(), this, false);
				acceptOrRejectBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				mProgressDialog.show();
			}

		}
	}

	private class NotificationBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshNotificationLists(context);
		}
	}
}
