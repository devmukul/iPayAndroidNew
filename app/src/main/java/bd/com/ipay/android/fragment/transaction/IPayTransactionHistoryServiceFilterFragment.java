package bd.com.ipay.android.fragment.transaction;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Locale;

import bd.com.ipay.android.model.TransactionServiceFilterOption;
import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.android.utility.TransactionUtilities;
import bd.com.ipay.android.viewmodel.TransactionHistoryViewModel;
import bd.com.ipay.ipayskeleton.R;

public class IPayTransactionHistoryServiceFilterFragment extends BottomSheetDialogFragment {

	private TransactionHistoryType transactionHistoryType;
	private ArrayList<TransactionServiceFilterOption> filterOptionArrayList;

	private TransactionHistoryViewModel transactionHistoryViewModel;

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

		filterOptionArrayList = TransactionUtilities
				.getTransactionFilterList(transactionHistoryType, getContext());
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater
				.inflate(R.layout.fragment_ipay_transaction_history_service_filter, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final RadioGroup serviceFilterRadioGroup = view.findViewById(R.id.service_filter_radio_group);

		populateServiceFilter(serviceFilterRadioGroup);

		serviceFilterRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				serviceFilterRadioGroup.setOnCheckedChangeListener(null);
				serviceFilterRadioGroup.clearCheck();
				serviceFilterRadioGroup.setOnCheckedChangeListener(this);
				transactionHistoryViewModel
						.filterTransactionHistory(TransactionHistoryViewModel.SearchType.SERVICE, checkedId);
				dismiss();
			}
		});
	}

	private String getTransactionHistoryViewModelKey(
			TransactionHistoryType transactionHistoryType) {
		return String.format(Locale.getDefault(), "%s:%s", transactionHistoryType.toString(),
				TransactionHistoryViewModel.class.getCanonicalName());
	}

	private void populateServiceFilter(final RadioGroup serviceFilterRadioGroup) {
		serviceFilterRadioGroup.removeAllViews();
		final int padding = getResources()
				.getDimensionPixelSize(R.dimen.activity_horizontal_margin);
		for (TransactionServiceFilterOption
				transactionServiceFilterOption : filterOptionArrayList) {

			final RadioButton radioButton = new RadioButton(getContext());

			RadioGroup.LayoutParams layoutParams = new RadioGroup
					.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,
					RadioGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(padding, padding, padding, 0);
			radioButton.setLayoutParams(layoutParams);
			radioButton.setId(transactionServiceFilterOption.getServiceId());
			radioButton.setText(transactionServiceFilterOption.getServiceName());
			radioButton.setTextSize(16);
			radioButton.setBackgroundResource(R.drawable.background_filter_radio_button);

			if (getActivity() != null) {
				radioButton.setTextColor(ResourcesCompat.getColor(getResources(),
						R.color.colorTextPrimary, getActivity().getTheme()));
			} else {
				radioButton.setTextColor(ResourcesCompat.getColor(getResources(),
						R.color.colorTextPrimary, null));
			}

			serviceFilterRadioGroup.addView(radioButton);
		}
	}
}
