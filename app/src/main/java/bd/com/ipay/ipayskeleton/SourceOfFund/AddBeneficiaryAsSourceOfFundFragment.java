package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Beneficiary;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetBeneficiaryListResponse;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class AddBeneficiaryAsSourceOfFundFragment extends IpayAbstractSpecificSourceOfFundListFragment implements HttpResponseListener {
    private HttpRequestGetAsyncTask mGetBeneficiaryAsyncTask;
    private CustomProgressDialog mProgressDialog;

    private ArrayList<Beneficiary> beneficiaryArrayList;

    @Override
    public void setType() {
        type = Constants.BENEFICIARY;
    }

    @Override
    public void setRecyclerViewLayoutId() {
        recyclerViewLayoutId = R.layout.list_source_of_fund;
    }

    @Override
    public void getSourceOfFundList() {

        if (mGetBeneficiaryAsyncTask != null) {
            return;
        } else {

            mProgressDialog = new CustomProgressDialog(getContext());
            mGetBeneficiaryAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BENEFICIARY_LIST, Constants.BASE_URL_MM + Constants.URL_GET_BENEFICIARY,
                    getContext(), this, false);
            mGetBeneficiaryAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    @Override
    public void setNoDataText(String text) {
        noDataTextView.setText(text);
    }

    @Override
    public void setFragmentTitle(String title) {
        titleTextView.setText(getString(R.string.beneficiary_ipay_user));
    }

    @Override
    public void setBackButtonAction() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

    }

    @Override
    public void setHelpAction() {
        helpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TYPE, Constants.BENEFICIARY);
                ((SourceOfFundActivity) getActivity()).switchToHelpLayout(bundle);
            }
        });

    }

    private void removeRejectedEntriesForBeneficiaries(ArrayList<Beneficiary> beneficiaries) {
        beneficiaryArrayList.clear();
        for (int i = 0; i < beneficiaries.size(); i++) {
            if (!beneficiaries.get(i).getStatus().equals("REJECTED")) {
                beneficiaryArrayList.add(beneficiaries.get(i));
            }
        }
        parentBeneficiaryArrayList = beneficiaryArrayList;
        if (parentBeneficiaryArrayList == null || parentBeneficiaryArrayList.size() == 0) {
            setNoDataText(getString(R.string.no_beneficiary_text));
            setAddNewResourceButtonVisibility(View.VISIBLE);
            noDataTextView.setVisibility(View.VISIBLE);
        } else {
            setAddNewResourceButtonVisibility(View.GONE);
        }
        sourceOfFundListAdapter.notifyDataSetChanged();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetBeneficiaryAsyncTask = null;
            mProgressDialog.dismiss();
            return;
        } else {
            mProgressDialog.dismiss();
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                GetBeneficiaryListResponse getBeneficiaryListResponse = new Gson().
                        fromJson(result.getJsonString(), GetBeneficiaryListResponse.class);
                ArrayList<Beneficiary> allBeneficiaryArrayList = getBeneficiaryListResponse.getBeneficiary();
                if (allBeneficiaryArrayList == null || allBeneficiaryArrayList.size() == 0) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    setNoDataText(getString(R.string.no_beneficiary_text));
                    parentBeneficiaryArrayList = allBeneficiaryArrayList;
                    sourceOfFundListAdapter.notifyDataSetChanged();
                    setAddNewResourceButtonVisibility(View.VISIBLE);
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    beneficiaryArrayList = new ArrayList<>();
                    removeRejectedEntriesForBeneficiaries(allBeneficiaryArrayList);
                }
            }
            mGetBeneficiaryAsyncTask = null;
        }
    }
}
