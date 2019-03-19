package bd.com.ipay.ipayskeleton.SourceOfFund;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SourceOfFundActivity extends BaseActivity {
	public BottomSheetBehavior<LinearLayout> bottomSheetBehavior;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_source_of_fund);
		Button helperBottomSheetDismissButton = findViewById(R.id.helper_bottom_sheet_dismiss_button);
		helperBottomSheetDismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
					bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
				}
			}
		});
		LinearLayout bottomSheet = findViewById(R.id.help_bottom_sheet_layout);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		switchToSourceOfFundListFragment();
	}


	public void switchToHelpLayout(Bundle bundle) {
		SourceOfFundHelperFragment sourceOfFundHelperFragment = new SourceOfFundHelperFragment();
		sourceOfFundHelperFragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().
				replace(R.id.help_fragment_container,
						sourceOfFundHelperFragment).commit();
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

	}

	public void switchToSourceOfFundListFragment() {
		while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new IpaySourceOfFundListFragment()).commit();
	}

	public void switchToAddSourceOfFundFragment(Bundle bundle) {
		while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
			getSupportFragmentManager().popBackStack();
		}
		AddSourceOfFundFragment addSourceOfFundFragment = new AddSourceOfFundFragment();
		addSourceOfFundFragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_enter,
				R.anim.right_to_left_exit, R.anim.left_to_right_enter, R.anim.left_to_right_exit).replace
				(R.id.fragment_container, addSourceOfFundFragment).addToBackStack(null).commit();
	}

	@Override
	protected Context setContext() {
		return SourceOfFundActivity.this;
	}

	@Override
	public void onBackPressed() {
		Utilities.hideKeyboard(this);
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment instanceof AddBeneficiaryAsSourceOfFundFragment) {
					IpayAbstractSpecificSourceOfFundListFragment abstractSpecificSourceOfFundListFragment =
							(IpayAbstractSpecificSourceOfFundListFragment) fragment;
					if (abstractSpecificSourceOfFundListFragment.onBackPressed()) {
						return;
					}
				} else if (fragment instanceof AddSourceOfFundFragment) {
					AddSourceOfFundFragment addSourceOfFundFragment = (AddSourceOfFundFragment) fragment;
					if (addSourceOfFundFragment.onBackPressed()) {
						return;
					}
				}
			}

			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				getSupportFragmentManager().popBackStack();
			} else {
				super.onBackPressed();
			}
		}
	}

	public void switchToSourceOfSuccessFragment(Bundle bundle) {
		while (getSupportFragmentManager().getBackStackEntryCount() > 3) {
			getSupportFragmentManager().popBackStack();
		}
		AddSourceOfFundSuccessFragment addSourceOfFundSuccessFragment = new AddSourceOfFundSuccessFragment();
		addSourceOfFundSuccessFragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_enter,
				R.anim.right_to_left_exit, R.anim.left_to_right_enter, R.anim.left_to_right_exit).replace
				(R.id.fragment_container, addSourceOfFundSuccessFragment).addToBackStack(null).commit();
	}

	public void switchToAddSponsorFragment() {
		while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_enter,
				R.anim.right_to_left_exit, R.anim.left_to_right_enter, R.anim.left_to_right_exit).replace
				(R.id.fragment_container, new AddSponsorAsSourceOfFundFragment()).addToBackStack(null).commit();
	}

	public void switchToAddBeneficiaryFragment() {
		while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		}
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_enter,
				R.anim.right_to_left_exit, R.anim.left_to_right_enter, R.anim.left_to_right_exit).replace
				(R.id.fragment_container, new AddBeneficiaryAsSourceOfFundFragment()).addToBackStack(null).commit();
	}
}