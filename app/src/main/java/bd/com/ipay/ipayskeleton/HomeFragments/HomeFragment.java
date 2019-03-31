package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.InviteFriendActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.QRCodePaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.QRCodeViewerActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.RefreshBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.DashboardProfileCompletionPOJO;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistoryRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistoryResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionMetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetAvailableCreditCardBanks;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.Bank;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CreditCardSelectDialog;
import bd.com.ipay.ipayskeleton.Widget.View.ISPSelectDialog;

import static android.view.View.GONE;

public class HomeFragment extends BaseFragment implements HttpResponseListener {

	private static final int REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH = 100;

	public static final String BALANCE_KEY = "BALANCE";
	private HttpRequestPostAsyncTask mRefreshBalanceTask = null;

	private CustomProgressDialog mProgressDialog;
	private TextView balanceView;
	public ImageButton refreshBalanceButton;

	private ArrayList<Bank> mBankList;
	private HttpRequestGetAsyncTask mGetBankListAsyncTask;

	private final BroadcastReceiver mBalanceUpdateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(BALANCE_KEY)) {
				BigDecimal balance = (BigDecimal) intent.getSerializableExtra(BALANCE_KEY);
				if (isAdded() && balance != null) {
					balanceView.setText(getString(R.string.balance_holder, Utilities.takaWithComma(new BigDecimal(balance.toString()))));
					SharedPrefManager.setUserBalance(balance.toString());
				}
			} else {
				if (isAdded())
					refreshBalanceButton.performClick();
				else
					refreshBalance();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_home, container, false);
		balanceView = view.findViewById(R.id.balance);
		mProgressDialog = new CustomProgressDialog(getActivity());
		refreshBalanceButton = view.findViewById(R.id.refresh_balance_button);
		View mAddMoneyButton = view.findViewById(R.id.button_add_money);
		View mWithdrawMoneyButton = view.findViewById(R.id.button_withdraw_money);
		LinearLayout mSendMoneyButton = view.findViewById(R.id.button_send_money);
		LinearLayout mRequestMoneyButton = view.findViewById(R.id.button_request_money);
		LinearLayout mPayByQRCodeButton = view.findViewById(R.id.button_pay_by_qr_code);
		LinearLayout mMakePaymentButton = view.findViewById(R.id.button_make_payment);
		LinearLayout mTopUpButton = view.findViewById(R.id.button_topup);
		ImageButton mShowQRCodeButton = view.findViewById(R.id.show_qr_code_button);
		LinearLayout mRequestPaymentButton = view.findViewById(R.id.button_request_paymnet);

		LinearLayout mDescoBillButton = view.findViewById(R.id.button_desco_bill_payment);
		LinearLayout mDpdcBillButton = view.findViewById(R.id.button_dpdc_bill_payment);
		LinearLayout mIspBillButton = view.findViewById(R.id.button_isp_bill_payment);
		LinearLayout mCreditCard = view.findViewById(R.id.button_credit_card);

		if (ProfileInfoCacheManager.isBusinessAccount()) {
			mRequestPaymentButton.setVisibility(View.VISIBLE);
			mCreditCard.setVisibility(GONE);
		} else {
			mRequestPaymentButton.setVisibility(GONE);
			mCreditCard.setVisibility(View.VISIBLE);
		}

        mAddMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(or = {ServiceIdConstants.ADD_MONEY_BY_BANK, ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD, ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY})
            public void onClick(View v) {
                PinChecker addMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
                        intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY);
                        startActivity(intent);
                    }
                });
                addMoneyPinChecker.execute();
            }
        });
        if (SharedPrefManager.getUserBalance().equals("0.0")) {
            balanceView.setText(R.string.loading);
        } else {
            try {
                balanceView.setText(getString(R.string.balance_holder, Utilities.takaWithComma(new BigDecimal(SharedPrefManager.getUserBalance()))));
            } catch (Exception e) {
                mTracker.send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Parsing Error- " + SharedPrefManager.getUserBalance())
                        .build());
            }
        }
		mAddMoneyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess(or = {ServiceIdConstants.ADD_MONEY_BY_BANK, ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD, ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY})
			public void onClick(View v) {
				PinChecker addMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
					@Override
					public void ifPinAdded() {
						Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
						intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY);
						startActivity(intent);
					}
				});
				addMoneyPinChecker.execute();
			}
		});
		if (SharedPrefManager.getUserBalance().equals("0.0")) {
			balanceView.setText(R.string.loading);
		} else {
			try {
				balanceView.setText(getString(R.string.balance_holder, Utilities.takaWithComma(new BigDecimal(SharedPrefManager.getUserBalance()))));
			} catch (Exception e) {
				mTracker.send(new HitBuilders.ExceptionBuilder()
						.setDescription("Parsing Error- " + SharedPrefManager.getUserBalance())
						.build());
			}
		}

		mWithdrawMoneyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.WITHDRAW_MONEY})
			public void onClick(View v) {
				PinChecker withdrawMoneyPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
					@Override
					public void ifPinAdded() {
						Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
						intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_WITHDRAW_MONEY);
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
						Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
						intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_SEND_MONEY);
						startActivity(intent);
					}
				});
				sendMoneyPinChecker.execute();
			}
		});

		mRequestMoneyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
				intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_REQUEST_MONEY);
				startActivity(intent);
			}
		});

		mMakePaymentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.MAKE_PAYMENT)) {
					DialogUtils.showServiceNotAllowedDialog(getContext());
					return;
				}
				PinChecker makePaymentPinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
					@Override
					public void ifPinAdded() {
						Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
						intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT);
						startActivity(intent);
					}
				});
				makePaymentPinChecker.execute();

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
						intent.putExtra(Constants.SPONSOR_LIST, ((HomeActivity) getActivity()).mSponsorList);
						startActivity(intent);
					}
				});
				payByQCPinChecker.execute();
			}
		});

		mTopUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.TOP_UP})
			public void onClick(View v) {
				if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.TOP_UP)) {
					DialogUtils.showServiceNotAllowedDialog(getContext());
					return;
				}
				PinChecker pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
					@Override
					public void ifPinAdded() {
						Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
						intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_TOP_UP);
						startActivity(intent);
					}
				});
				pinChecker.execute();
			}
		});

		mDescoBillButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.DESCO})
			public void onClick(View v) {
				payBill(Constants.DESCO, null);
			}
		});

		mDpdcBillButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.DPDC})
			public void onClick(View v) {
				payBill(Constants.DPDC, null);
			}
		});

		mIspBillButton.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.UTILITY_BILL_PAYMENT})
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), IPayUtilityBillPayActionActivity.class);
				intent.putExtra(Constants.FROM_DASHBOARD, true);
				intent.putExtra(Constants.SERVICE, "ISP");
				getContext().startActivity(intent);

//				final ISPSelectDialog creditCardSelectDialog = new ISPSelectDialog(getContext());
//				creditCardSelectDialog.setTitle(getString(R.string.select_isp));
//				creditCardSelectDialog.setCloseButtonAction(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						creditCardSelectDialog.cancel();
//					}
//				});
//				creditCardSelectDialog.show();
			}
		});

		mCreditCard.setOnClickListener(new View.OnClickListener() {
			@Override
			@ValidateAccess({ServiceIdConstants.UTILITY_BILL_PAYMENT})
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), IPayUtilityBillPayActionActivity.class);
				intent.putExtra(Constants.FROM_DASHBOARD, true);
				intent.putExtra(Constants.SERVICE, "CARD");
				getContext().startActivity(intent);
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
				intent.putExtra(Constants.ACTIVITY_TITLE, getString(R.string.my_qr_code_to_share));
				startActivity(intent);
			}
		});

		mRequestPaymentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.REQUEST_PAYMENT)) {
					DialogUtils.showServiceNotAllowedDialog(getContext());
					return;
				}
				PinChecker pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
					@Override
					public void ifPinAdded() {
						Intent intent;
						intent = new Intent(getActivity(), RequestPaymentActivity.class);
						startActivity(intent);
					}
				});
				pinChecker.execute();
			}
		});

		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBalanceUpdateBroadcastReceiver,
				new IntentFilter(Constants.BALANCE_UPDATE_BROADCAST));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// TODO we should refresh the balance only based on push notification, no need to fetch it
		// from the server every time someone navigates to the home activity. Once push is implemented
		// properly, move it to onCreate.
		refreshBalance();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_home));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (getActivity() != null) {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBalanceUpdateBroadcastReceiver);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu.findItem(R.id.action_filter_by_service) != null)
			menu.findItem(R.id.action_filter_by_service).setVisible(false);
		if (menu.findItem(R.id.action_filter_by_date) != null)
			menu.findItem(R.id.action_filter_by_date).setVisible(false);
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

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mProgressDialog.dismiss();
			mRefreshBalanceTask = null;
			refreshBalanceButton.clearAnimation();
			return;
		}

		Gson gson = new Gson();

		switch (result.getApiCommand()) {
			case Constants.COMMAND_REFRESH_BALANCE:
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					try {
						RefreshBalanceResponse mRefreshBalanceResponse = gson.fromJson(result.getJsonString(), RefreshBalanceResponse.class);
						if (mRefreshBalanceResponse.getBalance() != null) {
							if (isAdded())
								balanceView.setText(getString(R.string.balance_holder, Utilities.takaWithComma(mRefreshBalanceResponse.getBalance())));
							SharedPrefManager.setUserBalance(mRefreshBalanceResponse.getBalance().toString());
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

				break;

			case Constants.COMMAND_GET_BANK_LIST:
				mBankList = new ArrayList<>();
				mBankList.add(new Bank("Lanka Bangla", null));
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					ArrayList<Bank> bankList = new Gson().fromJson(result.getJsonString(), GetAvailableCreditCardBanks.class).getBankList();
					mBankList.addAll(bankList);
				} else {
					Toaster.makeText(getContext(), "Bank List Fetch Failed", Toast.LENGTH_LONG);
				}

				if(mBankList.size()>0){
					final CreditCardSelectDialog creditCardSelectDialog = new CreditCardSelectDialog(getContext(), mBankList);
					creditCardSelectDialog.setTitle(getString(R.string.select_card));
					creditCardSelectDialog.setCloseButtonAction(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							creditCardSelectDialog.cancel();
						}
					});
					creditCardSelectDialog.show();
				}

				mProgressDialog.dismiss();
				mGetBankListAsyncTask = null;
				break;

		}
	}



	private void payBill(final String provider, final String type) {
		PinChecker pinChecker;
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.UTILITY_BILL_PAYMENT)) {
			DialogUtils.showServiceNotAllowedDialog(getContext());
			return;
		}
		pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
			@Override
			public void ifPinAdded() {
				Intent intent;
				switch (provider) {
					case Constants.DESCO:
					case Constants.DPDC:
					case Constants.WASA:
						intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
						intent.putExtra(Constants.SERVICE, provider);
						startActivity(intent);
						break;
					case Constants.CREDIT_CARD:
						intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
						intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.CREDIT_CARD);
						startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
						break;
					case Constants.LANKABANGLA:
						intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
						if (type.equals("CARD"))
							intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_CARD);
						else
							intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_DPS);
						startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
						break;
				}
			}
		});
		pinChecker.execute();
	}

	public void attemptGetBankList() {
		if (mGetBankListAsyncTask != null) {
			return;
		} else {
			mProgressDialog.showDialog();
			mGetBankListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
					Constants.BASE_URL_SM + Constants.URL_GET_BANK_LIST, getContext(), this, false);
			mGetBankListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

}