package bd.com.ipay.ipayskeleton.Activities.DrawerActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BusinessFragments.Owner.BusinessInformationFragment;
import bd.com.ipay.ipayskeleton.BusinessFragments.Owner.EditBusinessInformationFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.AccountFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.AddressFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.BasicInfoFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.EditAddressFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.EditBasicInfoFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.EditParentInfoFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.EmailFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments.IdentificationDocumentListFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments.PreviewIdentificationDocumentFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments.UploadIdentificationFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationHolderFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.ProfileCompletionFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.RecommendationReviewFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.SourceOfFundDashBoardFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.BASIC_PROFILE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.BUSINESS_ADDRESS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.BUSINESS_DOCUMENTS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.BUSINESS_INFO;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.INTRODUCER;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PARENT;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PERSONAL_ADDRESS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PHOTOID;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROFILE_COMPLETENESS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROFILE_INFO;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROFILE_PICTURE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TRUSTED_NETWORK;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.VERIFICATION_DOCUMENT;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.VERIFIED_EMAIL;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.VERIFY_BANK_OR_CARD;

public class ProfileActivity extends BaseActivity {


    private final String STARTED_FROM_PROFILE_ACTIVITY = "started_from_profile_activity";
    PreviewIdentificationDocumentFragment mPreviewIdentificationDocumentFragment;
    EmailFragment emailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mPreviewIdentificationDocumentFragment = new PreviewIdentificationDocumentFragment();
        String targetFragment = getIntent().getStringExtra(Constants.TARGET_FRAGMENT);
        Bundle bundle = getIntent().getBundleExtra(Constants.BUNDLE);

        if (targetFragment != null) {
            Bundle args;
            if(bundle != null)
                args = bundle;
            else
                args = setBundle(targetFragment);
            switchToFragment(targetFragment, args, false);
        } else {
            switchToProfileFragment();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utilities.hideKeyboard(this);
            if (mPreviewIdentificationDocumentFragment.isVisible()) {
                if (mPreviewIdentificationDocumentFragment.isDocumentViewOpen()) {
                    mPreviewIdentificationDocumentFragment.hideDocumentPreview();
                } else {
                    getSupportFragmentManager().popBackStackImmediate();
                }
            } else {
                if (emailFragment != null && !emailFragment.isContentShown) {
                    emailFragment.setContentShown(true);
                    emailFragment.isContentShown = true;
                    emailFragment.mSwipeRefreshLayout.setRefreshing(false);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                    getSupportFragmentManager().popBackStack();
                else
                    super.onBackPressed();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPreviewIdentificationDocumentFragment.isVisible()) {
            if (mPreviewIdentificationDocumentFragment.isDocumentViewOpen()) {
                mPreviewIdentificationDocumentFragment.hideDocumentPreview();
            } else {
                getSupportFragmentManager().popBackStackImmediate();
            }
        } else {
            if (emailFragment != null && !emailFragment.isContentShown) {
                emailFragment.setContentShown(true);
                emailFragment.isContentShown = true;
                emailFragment.mSwipeRefreshLayout.setRefreshing(false);
            } else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else
                super.onBackPressed();
        }
    }

    private Bundle setBundle(String targetFragment) {
        Bundle args = new Bundle();
        switch (targetFragment) {
            case VERIFY_BANK_OR_CARD:
                args.putBoolean(STARTED_FROM_PROFILE_ACTIVITY, true);
                break;
            case Constants.UPLOAD_DOCUMENT:
                args.putBoolean(STARTED_FROM_PROFILE_ACTIVITY, true);
                break;
            default:
                args = null;
                break;
        }

        return args;
    }

    @ValidateAccess
    public void switchToFragment(String targetFragment, Bundle bundle, boolean addToBackStack) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        switch (targetFragment) {
            case Constants.VERIFY_BANK:
                launchIntendedActivity(new ManageBanksActivity(), Constants.BANK_ACCOUNT);
                break;
            case Constants.ADD_BANK:
                launchIntendedActivity(new ManageBanksActivity(), Constants.ADD_BANK);
                break;
            case TRUSTED_NETWORK:
            case TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE:
                launchIntendedActivity(new SecuritySettingsActivity(), Constants.ADD_TRUSTED_PERSON);
                break;
            case BASIC_PROFILE:
                if (ProfileInfoCacheManager.isBusinessAccount())
                    fragment = new BusinessInformationFragment();
                else
                    fragment = new BasicInfoFragment();
                break;
            case BUSINESS_INFO:
                fragment = new BusinessInformationFragment();
                break;
            case PROFILE_PICTURE:
                fragment = new AccountFragment();
                break;
            case PARENT:
                fragment = new BasicInfoFragment();
                break;
            case INTRODUCER:
                fragment = new IdentificationHolderFragment();
                break;
            case PERSONAL_ADDRESS:
                if (ProfileInfoCacheManager.isBusinessAccount())
                    fragment = new BusinessInformationFragment();
                else
                    fragment = new AddressFragment();
                break;
            case BUSINESS_ADDRESS:
                fragment = new AddressFragment();
                break;
            case VERIFIED_EMAIL:
                fragment = new EmailFragment();

                break;
            case BUSINESS_DOCUMENTS:
            case VERIFICATION_DOCUMENT:
            case PHOTOID:
                fragment = new IdentificationDocumentListFragment();
                break;
            case PROFILE_COMPLETENESS:
                fragment = new ProfileCompletionFragment();
                break;
            case VERIFY_BANK_OR_CARD:
                fragment = new SourceOfFundDashBoardFragment();
                break;
            case Constants.UPLOAD_DOCUMENT:
                fragment = new UploadIdentificationFragment();
                break;
            case PROFILE_INFO:
                fragment = new AccountFragment();
                break;
            default:
                fragment = new AccountFragment();
        }
        if (fragment == null) {
            finish();
        } else {
            if (bundle != null)
                fragment.setArguments(bundle);

            ft.replace(R.id.fragment_container, fragment);
            if (addToBackStack)
                ft.addToBackStack(null);

            ft.commitAllowingStateLoss();
        }
    }

    public void switchToProfileFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AccountFragment()).commit();
    }

    public void switchToBasicInfoFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BasicInfoFragment()).addToBackStack(null).commit();
    }

    public void switchToBusinessInfoFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BusinessInformationFragment()).addToBackStack(null).commit();
    }

    public void switchToEditBusinessInformationFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2)
            getSupportFragmentManager().popBackStackImmediate();

        Fragment editBusinessInformationFragment = new EditBusinessInformationFragment();
        if (bundle != null)
            editBusinessInformationFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editBusinessInformationFragment).addToBackStack(null).commit();
    }

    public void switchToProfileCompletionFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileCompletionFragment()).addToBackStack(null).commit();
    }

    public void switchToEditBasicInfoFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2)
            getSupportFragmentManager().popBackStackImmediate();

        EditBasicInfoFragment editBasicInfoFragment = new EditBasicInfoFragment();
        editBasicInfoFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editBasicInfoFragment).addToBackStack(null).commit();
    }

    public void switchToEditParentInfoFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2)
            getSupportFragmentManager().popBackStackImmediate();

        EditParentInfoFragment editParentInfoFragment = new EditParentInfoFragment();
        editParentInfoFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editParentInfoFragment).addToBackStack(null).commit();
    }

    public void switchToAddressFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddressFragment()).addToBackStack(null).commit();
    }

    public void switchToEditAddressFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2)
            getSupportFragmentManager().popBackStackImmediate();

        EditAddressFragment editAddressFragment = new EditAddressFragment();
        editAddressFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editAddressFragment).addToBackStack(null).commit();
    }

    public void switchToEmailFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();

        emailFragment = new EmailFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, emailFragment).addToBackStack(null).commit();
    }

    public void switchToIdentificationDocumentListFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new IdentificationDocumentListFragment()).addToBackStack(null).commit();
    }

    public void switchToPreviewIdentificationDocumentFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 3)
            getSupportFragmentManager().popBackStackImmediate();
        mPreviewIdentificationDocumentFragment = new PreviewIdentificationDocumentFragment();
        mPreviewIdentificationDocumentFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mPreviewIdentificationDocumentFragment).addToBackStack(null).commit();
    }

    public void switchToUploadIdentificationDocumentFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 4)
            getSupportFragmentManager().popBackStackImmediate();
        UploadIdentificationFragment uploadIdentificationFragment = new UploadIdentificationFragment();
        uploadIdentificationFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, uploadIdentificationFragment).addToBackStack(null).commit();
    }

    public void switchToIntroducerFragment() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new IdentificationHolderFragment()).addToBackStack(null).commit();
    }

    public void switchToRecommendationReviewFragment(Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 2)
            getSupportFragmentManager().popBackStackImmediate();

        RecommendationReviewFragment recommendationReviewFragment = new RecommendationReviewFragment();
        recommendationReviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, recommendationReviewFragment).addToBackStack(null).commit();
    }

    public void launchIntendedActivity(Activity activity, String intendedFragment) {
        Intent intent = new Intent(this, activity.getClass());
        intent.putExtra(Constants.INTENDED_FRAGMENT, intendedFragment);
        startActivity(intent);
    }

    @Override
    public Context setContext() {
        return ProfileActivity.this;
    }
}
