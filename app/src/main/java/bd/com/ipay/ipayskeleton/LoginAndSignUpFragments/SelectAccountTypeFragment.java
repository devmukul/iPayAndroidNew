package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SelectAccountTypeFragment extends BaseFragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_select_account_type, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Button buttonAccountTypePersonal = view.findViewById(R.id.button_account_type_personal);
		final Button buttonAccountTypeBusiness = view.findViewById(R.id.button_account_type_business);

		buttonAccountTypePersonal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity)
					((SignupOrLoginActivity) getActivity()).switchToSignupPersonalStepOneFragment();
			}
		});

		buttonAccountTypeBusiness.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity)
					((SignupOrLoginActivity) getActivity()).switchToBusinessStepOneFragment();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_select_account_type));
	}
}
