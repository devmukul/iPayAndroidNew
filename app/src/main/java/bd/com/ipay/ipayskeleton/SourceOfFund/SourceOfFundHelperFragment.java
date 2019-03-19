package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class SourceOfFundHelperFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_source_of_fund, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView descriptionTextView = view.findViewById(R.id.description);
        TextView helper = view.findViewById(R.id.description_line_3);
        Bundle bundle = getArguments();
        String type = bundle.getString(Constants.TYPE);
        if (type.equals(Constants.BENEFICIARY)) {
            descriptionTextView.setText(getString(R.string.beneficiary_helper_text));
            helper.setText(getString(R.string.beneficiary_helper_line_3));
        } else {
            descriptionTextView.setText(getString(R.string.source_of_fund_helper_text));
            helper.setText(getString(R.string.sponsor_helper_line_3));
        }
    }
}
