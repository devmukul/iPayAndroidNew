package bd.com.ipay.ipayskeleton.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAvailableBankAsyncTask;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Bank;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class IPayBankListViewModel extends AndroidViewModel {

	public Bank getBankAccount(int position) {
		if (userBankAccountListLiveData.getValue() != null)
			return userBankAccountListLiveData.getValue().get(position);
		return null;
	}

	private final MutableLiveData<List<Bank>> userBankAccountListLiveData = new MutableLiveData<>();


	public IPayBankListViewModel(@NonNull Application application) {
		super(application);
	}

	public MutableLiveData<List<Bank>> getUserBankAccountListLiveData() {
		return userBankAccountListLiveData;
	}

	public void fetchBankList() {
		GetAvailableBankAsyncTask mGetAvailableBankAsyncTask = new GetAvailableBankAsyncTask(getApplication(),
				new GetAvailableBankAsyncTask.BankLoadListener() {
					@Override
					public void onLoadSuccess() {
						userBankAccountListLiveData.postValue(CommonData.getAvailableBanks());
					}

					@Override
					public void onLoadFailed() {
						Toaster.makeText(getApplication(), R.string.failed_available_bank_list_loading, Toast.LENGTH_LONG);
					}
				});
		mGetAvailableBankAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}


	private boolean isVerifiedBankAdded(List<BankAccountList> bankAccountList) {
		boolean result = bankAccountList != null;
		if (result) {
			result = false;
			for (BankAccountList bank : bankAccountList) {
				result |= bank.getVerificationStatus().equals(Constants.BANK_ACCOUNT_STATUS_VERIFIED);
			}
		}
		return result;
	}
}
