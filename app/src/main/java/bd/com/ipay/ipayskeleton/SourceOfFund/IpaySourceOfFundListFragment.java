package bd.com.ipay.ipayskeleton.SourceOfFund;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.Activities.AddCardActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.IdentificationDocument;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.IdentificationDocumentConstants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;

public class IpaySourceOfFundListFragment extends Fragment implements View.OnClickListener {
    private View bankView;
    private View iPayBeneficiaryView;
    private View iPaySponsorView;
    private View cardView;
    private TextView nidRequired;

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
        cardView = view.findViewById(R.id.card_layout);
        nidRequired = view.findViewById(R.id.nid_text_link_bank);
        nidRequired.setText(Html.fromHtml("<p><u>"+getContext().getString(R.string.nid_required)+"</u></p>").toString());

        if (!ProfileInfoCacheManager.isIdentificationDocumentUploaded()){
            nidRequired.setVisibility(View.VISIBLE);
        }else {
            nidRequired.setVisibility(View.GONE);
        }

        iPayBeneficiaryView.setOnClickListener(this);
        iPaySponsorView.setOnClickListener(this);
        bankView.setOnClickListener(this);
        cardView.setOnClickListener(this);
        nidRequired.setOnClickListener(this);


        adjustViewAccordingToAcl();

        if (ProfileInfoCacheManager.isBusinessAccount()) {
            iPaySponsorView.setVisibility(View.GONE);
            iPayBeneficiaryView.setVisibility(View.GONE);
            cardView.setVisibility(View.GONE);
        }
        View backButton = view.findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    private void adjustViewAccordingToAcl() {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
            iPayBeneficiaryView.setVisibility(View.GONE);
            iPaySponsorView.setVisibility(View.GONE);
        } else {
            iPayBeneficiaryView.setVisibility(View.VISIBLE);
            iPaySponsorView.setVisibility(View.VISIBLE);
        }
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BANK_ACCOUNTS)) {
            bankView.setVisibility(View.GONE);
        } else {
            bankView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bank_layout:
                if (ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_BANK_ACCOUNTS)) {
                    if(ProfileInfoCacheManager.isIdentificationDocumentUploaded()) {
                        Intent intent = new Intent(getActivity(), ManageBanksActivity.class);
                        startActivity(intent);
                    }else{
                        IdentificationDocument identificationDocument = new IdentificationDocument();
                        identificationDocument.setDocumentTypeTitle(getResources().getString(R.string.national_id));
                        identificationDocument.setDocumentType(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT, identificationDocument);
                        bundle.putBoolean(Constants.IS_FROM_SOURCE_OF_FUND, true);
                        launchEditProfileActivity(Constants.UPLOAD_DOCUMENT, bundle);
                    }
                } else {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                }
                break;
            case R.id.sponsor_layout:
                if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
                    ((SourceOfFundActivity) getActivity()).switchToAddSponsorFragment();
                } else {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                }
                break;
            case R.id.beneficiary_layout:
                if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
                    ((SourceOfFundActivity) getActivity()).switchToAddBeneficiaryFragment();
                } else {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                }
                break;
            case R.id.nid_text_link_bank:
//                Intent intent2 = new Intent(getActivity(), AddCardActivity.class);
//                startActivity(intent2);
                break;
            case R.id.card_layout:
                Intent intent1 = new Intent(getActivity(), AddCardActivity.class);
                startActivity(intent1);
        }
    }

    @ValidateAccess
    private void launchEditProfileActivity(String type, Bundle bundle) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(Constants.TARGET_FRAGMENT, type);
        intent.putExtra(Constants.BUNDLE, bundle);
        startActivity(intent);
        getActivity().finish();
    }
}
