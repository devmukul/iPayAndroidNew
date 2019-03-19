package bd.com.ipay.android.utility;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import bd.com.ipay.android.model.TransactionServiceFilterOption;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;

public class TransactionUtilities {
	private TransactionUtilities() {

	}

	private static final ArrayList<TransactionServiceFilterOption> PENDING_TRANSACTION_FILTER_LIST
			= new ArrayList<>();
	private static final ArrayList<TransactionServiceFilterOption> COMPLETED_TRANSACTION_FILTER_LIST
			= new ArrayList<>();

	@Nullable
	public static ArrayList<TransactionServiceFilterOption>
	getTransactionFilterList(TransactionHistoryType transactionHistoryType,
	                         Context context) {
		switch (transactionHistoryType) {
			case COMPLETED:
				if (COMPLETED_TRANSACTION_FILTER_LIST.isEmpty()) {
					populateCompletedTransactionFilterList(context);
				}
				return COMPLETED_TRANSACTION_FILTER_LIST;
			case PENDING:
				if (PENDING_TRANSACTION_FILTER_LIST.isEmpty()) {
					populatePendingTransactionFilterList(context);
				}
				return PENDING_TRANSACTION_FILTER_LIST;
			default:
				return null;
		}
	}

	private static void populateCompletedTransactionFilterList(Context context) {
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.OPENING_BALANCE,
						context.getString(R.string.opening_balance)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.SEND_MONEY,
						context.getString(R.string.send_money)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.ADD_MONEY_BY_BANK,
						context.getString(R.string.add_money_from_bank)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY,
						context.getString(R.string.add_money_from_bank_instantly)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD,
						context.getString(R.string.add_money_from_credit_or_debit_card)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.WITHDRAW_MONEY,
						context.getString(R.string.withdraw_money)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.TOP_UP,
						context.getString(R.string.top_up)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.REQUEST_MONEY,
						context.getString(R.string.request_money)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.MAKE_PAYMENT,
						context.getString(R.string.make_payment)));
		if (ProfileInfoCacheManager.isBusinessAccount())
			COMPLETED_TRANSACTION_FILTER_LIST
					.add(new TransactionServiceFilterOption(
							ServiceIdConstants.REQUEST_PAYMENT,
							context.getString(R.string.request_payment)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.OFFER,
						context.getString(R.string.offer)));
		COMPLETED_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.UTILITY_BILL_PAYMENT,
						context.getString(R.string.bill_pay)));
	}

	private static void populatePendingTransactionFilterList(Context context) {
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.ADD_MONEY_BY_BANK,
						context.getString(R.string.add_money_from_bank)));
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD,
						context.getString(R.string.add_money_from_credit_or_debit_card)));
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.WITHDRAW_MONEY,
						context.getString(R.string.withdraw_money)));
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.TOP_UP,
						context.getString(R.string.top_up)));
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.REQUEST_MONEY,
						context.getString(R.string.request_money)));
		PENDING_TRANSACTION_FILTER_LIST
				.add(new TransactionServiceFilterOption(
						ServiceIdConstants.REQUEST_PAYMENT,
						context.getString(R.string.request_payment)));

	}
}
