package bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractAmountFragment;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;

public abstract class IPayAbstractBankTransactionAmountInputFragment extends IPayAbstractAmountFragment {

	protected BankAccountList bankAccountList;
	protected boolean isInstant;
	protected long flatRate;
	protected long variableRate;
	protected long maxRate;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
			isInstant = getArguments().getBoolean("IS_INSTANT", false);
			flatRate = getArguments().getLong("FLAT_FEE", 0);
			variableRate = getArguments().getLong("VARIABLE_FEE", 0);
			maxRate = getArguments().getLong("MAX_FEE", 0);
		}
	}

	@Override
	protected InputFilter getInputFilter() {
		return new DecimalDigitsInputFilter() {
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
						if (result > Integer.MAX_VALUE)
							return "";
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return super.filter(source, start, end, dest, dstart, dend);
			}
		};
	}
}
