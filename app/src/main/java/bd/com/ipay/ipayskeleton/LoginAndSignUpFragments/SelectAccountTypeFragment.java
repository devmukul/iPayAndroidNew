package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SelectAccountTypeFragment extends Fragment {

    private Button buttonAccountTypePersonal;
    private Button buttonAccountTypeBusiness;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_select_account_type_page);
        Utilities.hideKeyboard(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_select_account_type, container, false);

        buttonAccountTypePersonal = (Button) v.findViewById(R.id.button_account_type_personal);
        buttonAccountTypeBusiness = (Button) v.findViewById(R.id.button_account_type_business);

        buttonAccountTypePersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToSignupPersonalStepOneFragment();
            }
        });

        buttonAccountTypeBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToBusinessStepOneFragment();
            }
        });

        return v;
    }
}
