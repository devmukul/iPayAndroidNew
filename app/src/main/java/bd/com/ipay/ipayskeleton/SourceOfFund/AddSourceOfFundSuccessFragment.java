package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AddSourceOfFundSuccessFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.source_of_fund_add_success_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RoundedImageView profileImageView = view.findViewById(R.id.profile_image);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView helperTextView = view.findViewById(R.id.helper_text);
        Button gotoWalletButton = view.findViewById(R.id.go_to_wallet_button);

        Bundle bundle = getArguments();
        String type = getArguments().getString(Constants.TYPE);

        String name = bundle.getString(Constants.NAME);
        String profileImageUrl = bundle.getString(Constants.PROFILE_PICTURE);
        if (profileImageUrl.contains("ipay.com")) {
        } else {
            profileImageUrl = Constants.BASE_URL_FTP_SERVER + profileImageUrl;
        }
        Glide.with(getContext())
                .load(profileImageUrl)
                .centerCrop()
                .error(getContext().getResources().getDrawable(R.drawable.user_brand_bg))
                .into(profileImageView);
        if (type.equals(Constants.SPONSOR)) {
            String setText = getString(R.string.source_of_fund_success, name);
            helperTextView.setText(setText);
        } else if (type.equals(Constants.BENEFICIARY)) {
            String successMessage = getString(R.string.sof_success_message, name);
            helperTextView.setText(successMessage);
        }
        nameTextView.setText(name);
        gotoWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(getActivity());
                getActivity().finish();
            }
        });
    }
}
