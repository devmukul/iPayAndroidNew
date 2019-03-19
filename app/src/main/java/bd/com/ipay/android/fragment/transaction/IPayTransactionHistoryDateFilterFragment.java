package bd.com.ipay.android.fragment.transaction;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.android.viewmodel.TransactionHistoryViewModel;
import bd.com.ipay.ipayskeleton.R;

public class IPayTransactionHistoryDateFilterFragment extends BottomSheetDialogFragment {
	private TransactionHistoryType transactionHistoryType;

	private TransactionHistoryViewModel transactionHistoryViewModel;

	private OnDateSelectListener onDateSelectListener;

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionHistoryType = (TransactionHistoryType) getArguments()
					.getSerializable(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_TYPE_KEY);
		}

		if (transactionHistoryType == null) {
			transactionHistoryType = TransactionHistoryType.COMPLETED;
		}

		if (getActivity() != null) {
			transactionHistoryViewModel =
					ViewModelProviders.of(getActivity()).get(
							getTransactionHistoryViewModelKey(transactionHistoryType)
							, TransactionHistoryViewModel.class);
		} else if (getParentFragment() != null) {
			transactionHistoryViewModel =
					ViewModelProviders.of(getParentFragment()).get(
							getTransactionHistoryViewModelKey(transactionHistoryType)
							, TransactionHistoryViewModel.class);
		} else {
			this.dismiss();
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_ipay_transaction_history_date_filter, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (getActivity() == null)
			return;

		final Button fromDateButton = view.findViewById(R.id.from_date_button);
		final Button toDateButton = view.findViewById(R.id.to_date_button);
		final Button dateFilterPerformButton = view.findViewById(R.id.date_filter_perform_button);
		final Calendar fromDate = Calendar.getInstance();
		final Calendar toDate = Calendar.getInstance();

		fromDate.set(Calendar.HOUR_OF_DAY, 0);
		fromDate.set(Calendar.MINUTE, 0);
		fromDate.set(Calendar.SECOND, 0);
		toDate.set(Calendar.HOUR_OF_DAY, 23);
		toDate.set(Calendar.MINUTE, 59);
		toDate.set(Calendar.SECOND, 59);

		toDateButton.setEnabled(false);
		dateFilterPerformButton.setEnabled(false);

		fromDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final DatePickerDialog datePickerDialog = getDatePickerDialog(fromDate
						, 0, Calendar.getInstance().getTimeInMillis());
				if (datePickerDialog == null) {
					return;
				}
				onDateSelectListener = new OnDateSelectListener() {
					@Override
					public void onDateSelect(int year, int monthOfYear, int dayOfMonth) {
						fromDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
						if (fromDate.after(toDate)) {
							final Calendar dateToday = Calendar.getInstance();
							toDate.set(dateToday.get(Calendar.YEAR),
									dateToday.get(Calendar.MONTH),
									dateToday.get(Calendar.DAY_OF_MONTH),
									23,
									59,
									59);
							toDateButton.setText(R.string.to_date);
						}
						toDateButton.setEnabled(true);
						dateFilterPerformButton.setEnabled(true);
						fromDateButton.setText(simpleDateFormat.format(fromDate.getTime()));
					}
				};
				datePickerDialog.show();
			}
		});

		toDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final DatePickerDialog datePickerDialog = getDatePickerDialog(toDate
						, fromDate.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
				if (datePickerDialog == null) {
					return;
				}
				onDateSelectListener = new OnDateSelectListener() {
					@Override
					public void onDateSelect(int year, int monthOfYear, int dayOfMonth) {
						toDate.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
						toDateButton.setText(simpleDateFormat.format(toDate.getTime()));
					}
				};
				datePickerDialog.show();
			}
		});

		dateFilterPerformButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fromDateButton.getText().equals(getString(R.string.from_date))) {
					if (alertDialog == null) {
						alertDialog = new AlertDialog.Builder(getActivity())
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.select_a_from_date_to_filter)
								.setPositiveButton(android.R.string.ok, null)
								.create();
					}
					alertDialog.show();
				} else {
					transactionHistoryViewModel
							.filterTransactionHistory(TransactionHistoryViewModel.SearchType.DATE,
									new Pair<>(fromDate, toDate));
					dismiss();
				}
			}
		});
	}

	private DatePickerDialog getDatePickerDialog(Calendar selectedDate,
	                                             long minDate, long maxDate) {
		if (getActivity() != null) {
			final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
					null, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
					selectedDate.get(Calendar.DAY_OF_MONTH));
			datePickerDialog.getDatePicker().setMinDate(minDate);
			datePickerDialog.getDatePicker().setMaxDate(maxDate);
			datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (onDateSelectListener != null) {
								final int year = datePickerDialog.getDatePicker().getYear();
								final int month = datePickerDialog.getDatePicker().getMonth();
								final int dayOfMonth = datePickerDialog.getDatePicker().getDayOfMonth();
								onDateSelectListener.onDateSelect(year, month, dayOfMonth);
							}
						}
					});
			return datePickerDialog;
		} else {
			return null;
		}
	}

	private AlertDialog alertDialog;

	private String getTransactionHistoryViewModelKey(
			TransactionHistoryType transactionHistoryType) {
		return String.format(Locale.getDefault(), "%s:%s", transactionHistoryType.toString(),
				TransactionHistoryViewModel.class.getCanonicalName());
	}

	private interface OnDateSelectListener {
		void onDateSelect(int year, int monthOfYear, int dayOfMonth);
	}
}
