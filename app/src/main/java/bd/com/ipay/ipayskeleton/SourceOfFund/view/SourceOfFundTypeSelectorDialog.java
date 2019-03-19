package bd.com.ipay.ipayskeleton.SourceOfFund.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class SourceOfFundTypeSelectorDialog implements View.OnClickListener {
    private Context context;
    private View headerView;
    private View bodyView;

    private ImageView cancelButton;

    private AlertDialog sourceOfFundSelectorDialog;


    private SourceOfFundTypeSelectorListener sourceOfFundTypeSelectorListener;

    private LinearLayout sponsorLayout;
    private LinearLayout beneficiaryLayout;
    private RadioButton sponsorRadioButton;
    private RadioButton beneficiaryRadioButton;


    public SourceOfFundTypeSelectorDialog(Context context,
                                          SourceOfFundTypeSelectorListener sourceOfFundTypeSelectorListener) {
        this.context = context;
        this.sourceOfFundTypeSelectorListener = sourceOfFundTypeSelectorListener;
        createViews();

    }

    private void createViews() {
        headerView = LayoutInflater.from(context).inflate(R.layout.header_sponsor_dialog, null, false);
        ((TextView) headerView.findViewById(R.id.title)).setText("Select option");
        bodyView = LayoutInflater.from(context).inflate(R.layout.main_view_source_of_fund_type_selector, null, false);

        sponsorLayout = (LinearLayout) bodyView.findViewById(R.id.sponsor_layout);
        sponsorRadioButton = (RadioButton) bodyView.findViewById(R.id.radio_sponsor);
        beneficiaryLayout = (LinearLayout) bodyView.findViewById(R.id.beneficiary_layout);
        beneficiaryRadioButton = (RadioButton) bodyView.findViewById(R.id.beneficiary_radio);
        sponsorRadioButton.setOnClickListener(this);
        sponsorLayout.setOnClickListener(this);
        beneficiaryRadioButton.setOnClickListener(this);
        beneficiaryLayout.setOnClickListener(this);
        cancelButton = (ImageView) headerView.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceOfFundSelectorDialog != null) {
                    sourceOfFundSelectorDialog.dismiss();
                }

            }
        });
        sourceOfFundSelectorDialog = new AlertDialog.Builder(context)
                .setCustomTitle(headerView)
                .setView(bodyView)
                .setCancelable(false)
                .create();

        sourceOfFundSelectorDialog.show();

    }

    private void switchToDesiredSourceOfFundFragment(String type) {
        switch (type) {
            case Constants.SPONSOR:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sourceOfFundTypeSelectorListener.onSourceOfFundTypeSelected(Constants.SPONSOR);
                        sourceOfFundSelectorDialog.dismiss();
                    }
                }, 500);
                break;
            case Constants.BENEFICIARY:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sourceOfFundTypeSelectorListener.onSourceOfFundTypeSelected(Constants.BENEFICIARY);
                        sourceOfFundSelectorDialog.dismiss();
                    }
                }, 500);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radio_sponsor:
                sponsorRadioButton.setChecked(true);
                switchToDesiredSourceOfFundFragment(Constants.SPONSOR);
                break;
            case R.id.beneficiary_radio:
                beneficiaryRadioButton.setChecked(true);
                switchToDesiredSourceOfFundFragment(Constants.BENEFICIARY);
                break;
            case R.id.sponsor_layout:
                sponsorRadioButton.setChecked(true);
                switchToDesiredSourceOfFundFragment(Constants.SPONSOR);
                break;
            case R.id.beneficiary_layout:
                beneficiaryRadioButton.setChecked(true);
                switchToDesiredSourceOfFundFragment(Constants.BENEFICIARY);
                break;
        }
    }

    public interface SourceOfFundTypeSelectorListener {
        void onSourceOfFundTypeSelected(String type);
    }
}
