package bd.com.ipay.ipayskeleton.SourceOfFund;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class AddSponsorAsSourceOfFundFragment extends IpayAbstractSpecificSourceOfFundListFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetSponsorAsyncTask;
    private CustomProgressDialog mProgressDialog;

    private ArrayList<Sponsor> sponsorArrayList;


    @Override
    public void setNoDataText(String text) {
        noDataTextView.setText(text);

    }

    @Override
    public void setFragmentTitle(String title) {
        titleTextView.setText(getString(R.string.ipay_user));
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
                bundle.putString(Constants.TYPE, Constants.SPONSOR);
                ((SourceOfFundActivity) getActivity()).switchToHelpLayout(bundle);
            }
        });
    }

    @Override
    public void setType() {
        type = Constants.SPONSOR;
    }

    @Override
    public void setRecyclerViewLayoutId() {
        recyclerViewLayoutId = R.layout.list_source_of_fund;
    }

    @Override
    public void getSourceOfFundList() {

        if (mGetSponsorAsyncTask != null) {
            return;
        } else {

            mProgressDialog = new CustomProgressDialog(getContext());
            mGetSponsorAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SPONSOR_LIST, Constants.BASE_URL_MM + Constants.URL_GET_SPONSOR,
                    getContext(), this, false);
            mGetSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

    }

    private void removeRejectedEntriesForSponsors(ArrayList<Sponsor> sponsors) {
        sponsorArrayList.clear();
        for (int i = 0; i < sponsors.size(); i++) {
            if (!sponsors.get(i).getStatus().equals("REJECTED")) {
                sponsorArrayList.add(sponsors.get(i));
            }
        }
        parentSponsorArrayList = sponsorArrayList;
        HomeActivity.mSponsorList = sponsorArrayList;
        if (parentSponsorArrayList == null || parentSponsorArrayList.size() == 0) {
            noDataTextView.setVisibility(View.VISIBLE);
            setAddNewResourceButtonVisibility(View.VISIBLE);
        } else {
            setAddNewResourceButtonVisibility(View.GONE);
        }
        sourceOfFundListAdapter.notifyDataSetChanged();

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetSponsorAsyncTask = null;
            mProgressDialog.dismiss();
            return;
        } else {
            mProgressDialog.dismiss();
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                GetSponsorListResponse getSponsorListResponse = new Gson().
                        fromJson(result.getJsonString(), GetSponsorListResponse.class);
                ArrayList<Sponsor> allSponsorArrayList = getSponsorListResponse.getSponsor();
                if (allSponsorArrayList == null || allSponsorArrayList.size() == 0) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    setNoDataText(getString(R.string.no_sponsor_text));
                    parentSponsorArrayList = allSponsorArrayList;
                    sourceOfFundListAdapter.notifyDataSetChanged();
                    setAddNewResourceButtonVisibility(View.VISIBLE);
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    sponsorArrayList = new ArrayList<>();
                    removeRejectedEntriesForSponsors(allSponsorArrayList);
                }
            }
            mGetSponsorAsyncTask = null;
        }
    }
}
