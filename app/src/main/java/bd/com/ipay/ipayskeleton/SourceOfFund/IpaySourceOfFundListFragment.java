package bd.com.ipay.ipayskeleton.SourceOfFund;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;

public class IpaySourceOfFundListFragment extends Fragment implements View.OnClickListener {
    private View bankView;
    private View iPayBeneficiaryView;
    private View iPaySponsorView;
    private View divider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_source_of_fund_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bankView = view.findViewById(R.id.bank_layout);
        iPaySponsorView = view.findViewById(R.id.sponsor_layout);
        iPayBeneficiaryView = view.findViewById(R.id.beneficiary_layout);
        divider = view.findViewById(R.id.divider2);
        iPayBeneficiaryView.setOnClickListener(this);
        iPaySponsorView.setOnClickListener(this);
        bankView.setOnClickListener(this);

        if (ProfileInfoCacheManager.isBusinessAccount()) {
            iPaySponsorView.setVisibility(View.GONE);
            iPayBeneficiaryView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        View backButton = view.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bank_layout:
                Intent intent = new Intent(getActivity(), ManageBanksActivity.class);
                startActivity(intent);
                break;
            case R.id.sponsor_layout:
                ((SourceOfFundActivity) getActivity()).switchToAddSponsorFragment();
                break;
            case R.id.beneficiary_layout:
                ((SourceOfFundActivity) getActivity()).switchToAddBeneficiaryFragment();
                break;
        }
    }
}
