package bd.com.ipay.ipayskeleton.ProfileCompletionHelperFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Activities.ProfileVerificationHelperActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.IdentificationDocument;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.IdentificationDocumentConstants;

public class OnBoardPhotoIdUploadHelperFragment extends Fragment {

    private ImageView mUploadImageView;
    private Button mUploadButton;
    private Button mSkipButton;
    private ImageView mBackButtonTop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard_identification_document, container, false);
        initializeViews(view);
        setButtonActions();
        return view;
    }

    private void initializeViews(View view) {
        mUploadImageView = view.findViewById(R.id.document_id_helper_image);
        mUploadButton = view.findViewById(R.id.button_upload_photo_id);
        mSkipButton = view.findViewById(R.id.button_skip);
        mBackButtonTop = view.findViewById(R.id.back);
        if (getActivity().getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            mBackButtonTop.setVisibility(View.INVISIBLE);
        }
    }

    private void performUploadDocumentClickAction() {
        IdentificationDocument identificationDocument = new IdentificationDocument();
        identificationDocument.setDocumentTypeTitle(getResources().getString(R.string.national_id));
        identificationDocument.setDocumentType(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT, identificationDocument);
        bundle.putBoolean(Constants.FROM_ON_BOARD, true);
        ((ProfileVerificationHelperActivity) getActivity()).switchToUploadIdentificationDocumentFragment(bundle);
    }

    public void setButtonActions() {

        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ProfileInfoCacheManager.isSwitchedFromSignup()) {
                    ((ProfileVerificationHelperActivity) getActivity()).switchToBasicInfoEditHelperFragment();
                } else {
                    if (!ProfileInfoCacheManager.isBasicInfoAdded()) {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToBasicInfoEditHelperFragment();
                    } else if (!ProfileInfoCacheManager.isSourceOfFundAdded()) {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToSourceOfFundHelperFragment();
                    } else {
                        ((ProfileVerificationHelperActivity) getActivity()).switchToHomeActivity();
                    }
                }
            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performUploadDocumentClickAction();
            }
        });

        mUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performUploadDocumentClickAction();
            }
        });

        mBackButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }
}
