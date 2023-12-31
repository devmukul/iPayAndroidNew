package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.IconifiedTextViewWithButton;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PasswordRecoveryFragment extends BaseFragment {

    private IconifiedTextViewWithButton securityQuestionHeader;
    private IconifiedTextViewWithButton trustedPersonHeader;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_password_recovery, container, false);
        setTitle();

        securityQuestionHeader = (IconifiedTextViewWithButton) v.findViewById(R.id.security_question);
        trustedPersonHeader = (IconifiedTextViewWithButton) v.findViewById(R.id.trusted_person);

        securityQuestionHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.SEE_SECURITY_QUESTIONS)
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToSecurityQuestionFragment();
            }
        });

        trustedPersonHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.SEE_TRUSTED_PERSON)
            public void onClick(View v) {
                ((SecuritySettingsActivity) getActivity()).switchToTrustedPersonFragment();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_password_recovery));
    }

    public void setTitle() {
        getActivity().setTitle(R.string.password_recovery);
    }

}