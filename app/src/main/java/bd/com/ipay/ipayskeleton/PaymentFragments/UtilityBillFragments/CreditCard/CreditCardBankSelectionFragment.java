package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard;


import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetAvailableCreditCardBanks;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class CreditCardBankSelectionFragment extends Fragment implements HttpResponseListener {
    private RecyclerView mBankListRecyclerView;
    private Button mContinueButton;
    private ArrayList<Bank> mBankList;
    private HttpRequestGetAsyncTask mGetBankListAsyncTask;
    private LinearLayout mProgressLayout;
    private BankListAdapter bankListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card_bank_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContinueButton = view.findViewById(R.id.continue_button);
        mBankListRecyclerView = view.findViewById(R.id.user_bank_list_recycler_view);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        bankListAdapter = new BankListAdapter();
        mBankListRecyclerView.setAdapter(bankListAdapter);
    }

    public int getBankIcon(Bank bank) {
        Resources resources = getContext().getResources();
        int resourceId;
        if (bank.getBankCode() != null)
            resourceId = resources.getIdentifier("ic_bank" + bank.getBankCode(), "drawable",
                    getContext().getPackageName());
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    getContext().getPackageName());
        return resourceId;
        //return resources.getDrawable(resourceId);
    }

    public void attemptGetBankList() {
        if (mGetBankListAsyncTask != null) {
            return;
        } else {
            mGetBankListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                    Constants.BASE_URL_SM + Constants.URL_GET_BANK_LIST, getContext(), this, false);
            mGetBankListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        try {
            if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
                return;
            } else {
                if (result.getApiCommand().equals(Constants.COMMAND_GET_BANK_LIST)) {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mProgressLayout.setVisibility(View.GONE);
                        mBankList = new Gson().fromJson(result.getJsonString(), GetAvailableCreditCardBanks.class).getBankList();
                        mBankListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
                        mBankListRecyclerView.setAdapter(bankListAdapter);
                        bankListAdapter.notifyDataSetChanged();
                    } else {
                        Toaster.makeText(getContext(), "Bank List Fetch Failed", Toast.LENGTH_LONG);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.BankViewHolder> {
        @NonNull
        @Override
        public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_bank_item, null, false);
            return new BankViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BankViewHolder holder, int position) {
            holder.bankNameTextView.setText(mBankList.get(position).getBankName());
            holder.bankIconImageView.setImageResource(getBankIcon(mBankList.get(position)));
        }

        @Override
        public int getItemCount() {
            return mBankList.size();
        }

        public class BankViewHolder extends RecyclerView.ViewHolder {
            public TextView bankNameTextView;
            private ImageView bankIconImageView;


            public BankViewHolder(View itemView) {
                super(itemView);
                bankIconImageView = (ImageView) itemView.findViewById(R.id.bank_icon);
                bankNameTextView = (TextView) itemView.findViewById(R.id.bank_name);
            }
        }
    }
}
