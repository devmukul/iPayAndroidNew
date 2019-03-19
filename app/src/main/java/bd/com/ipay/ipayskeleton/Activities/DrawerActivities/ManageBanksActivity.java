package bd.com.ipay.ipayskeleton.Activities.DrawerActivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.ManageBanksFragments.AddBankFragment;
import bd.com.ipay.ipayskeleton.ManageBanksFragments.BankAccountsFragment;
import bd.com.ipay.ipayskeleton.ManageBanksFragments.ConsentAgreementForBankFragment;
import bd.com.ipay.ipayskeleton.ManageBanksFragments.PreviewChequebookCoverFragment;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BankBranch;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ManageBanksActivity extends BaseActivity {

    private boolean switchedFromBankVerification = false;
    private boolean switchedFromOnBoard = false;

    public FloatingActionButton mFabAddNewBank;

    public ArrayList<String> mDistrictNames;
    public ArrayList<BankBranch> mBranches;
    public ArrayList<String> mBranchNames;

    public int mSelectedBranchId = -1;
    public int mSelectedBankId = -1;
    public int mSelectedDistrictId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_banks);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDistrictNames = new ArrayList<>();
        mBranches = new ArrayList<>();
        mBranchNames = new ArrayList<>();
        Intent intent = getIntent();
        mFabAddNewBank = (FloatingActionButton) findViewById(R.id.fab_add_new_bank);
        mFabAddNewBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToAddNewBankFragment();
            }
        });
        switchedFromOnBoard = intent.getBooleanExtra(Constants.FROM_ON_BOARD, false);
        if (intent != null && switchedFromOnBoard) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.FROM_ON_BOARD, switchedFromOnBoard);
            mFabAddNewBank.setVisibility(View.GONE);
            switchToAddNewBankFragment(bundle);
        } else if (getIntent().getStringExtra(Constants.INTENDED_FRAGMENT) != null) {
            if (getIntent().getStringExtra(Constants.INTENDED_FRAGMENT).equals(Constants.BANK_ACCOUNT)) {
                switchToBankAccountsFragment();
            } else if (getIntent().getStringExtra(Constants.INTENDED_FRAGMENT).equals(Constants.ADD_BANK)) {
                switchToAddNewBankFragment();
            }
        } else {
            switchedFromBankVerification = getIntent().getBooleanExtra(Constants.SWITCHED_FROM_BANK_VERIFICATION, false);
            if (switchedFromBankVerification)
                switchToAddNewBankFragment();
            else
                switchToBankAccountsFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utilities.hideKeyboard(this);
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!switchedFromBankVerification) {
            // If back to bank account fragment then set the visibility of add bank button
            if (getSupportFragmentManager().getBackStackEntryCount() == 1 && !switchedFromOnBoard) {
                mFabAddNewBank.setVisibility(View.VISIBLE);
            }
        }

        // Clear the fragment back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            super.onBackPressed();
    }

    public void switchToBankAccountsFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new BankAccountsFragment()).commit();

        mFabAddNewBank.setVisibility(View.VISIBLE);
    }

    public void switchToPreviewChequebookCoverFragment(Bundle bundle) {
        PreviewChequebookCoverFragment mPreviewIdentificationDocumentFragment = new PreviewChequebookCoverFragment();
        mPreviewIdentificationDocumentFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mPreviewIdentificationDocumentFragment).addToBackStack(null).commit();
        mFabAddNewBank.setVisibility(View.GONE);
    }

    public void switchToAddNewBankFragment() {
        if (!switchedFromBankVerification) {
            while (getSupportFragmentManager().getBackStackEntryCount() > 1)
                getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddBankFragment()).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddBankFragment()).commit();
        }

        mFabAddNewBank.setVisibility(View.GONE);
    }

    public void switchToAddNewBankFragment(Bundle bundle) {

        AddBankFragment addBankFragment = new AddBankFragment();
        addBankFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addBankFragment).commit();

    }

    public void switchToAddBankAgreementFragment(Bundle bundle) {
        // If started from manage bank or profile completion page
        if (!switchedFromBankVerification) {
            while (getSupportFragmentManager().getBackStackEntryCount() > 2)
                getSupportFragmentManager().popBackStackImmediate();
        }
        // If switched from bank validator of add or withdraw money
        else {
            while (getSupportFragmentManager().getBackStackEntryCount() > 1)
                getSupportFragmentManager().popBackStackImmediate();
        }

        ConsentAgreementForBankFragment consentAgreementForBankFragment = new ConsentAgreementForBankFragment();
        consentAgreementForBankFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, consentAgreementForBankFragment).addToBackStack(null).commit();
        mFabAddNewBank.setVisibility(View.GONE);
    }

    @Override
    public Context setContext() {
        return ManageBanksActivity.this;
    }
}



