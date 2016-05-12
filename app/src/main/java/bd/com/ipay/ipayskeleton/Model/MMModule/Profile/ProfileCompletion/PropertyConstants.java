package bd.com.ipay.ipayskeleton.Model.MMModule.Profile.ProfileCompletion;

import android.support.v4.app.Fragment;

import java.util.HashMap;

import bd.com.ipay.ipayskeleton.DrawerFragments.BankAccountsFragment;
import bd.com.ipay.ipayskeleton.DrawerFragments.HomeFragments.ProfileFragments.AddressFragment;
import bd.com.ipay.ipayskeleton.DrawerFragments.HomeFragments.ProfileFragments.BasicInfoFragment;
import bd.com.ipay.ipayskeleton.DrawerFragments.HomeFragments.ProfileFragments.DocumentListFragment;
import bd.com.ipay.ipayskeleton.DrawerFragments.RecommendationRequestsFragment;
import bd.com.ipay.ipayskeleton.R;

public class PropertyConstants {

    public static final String PROFILE_COMPLETENESS = "PROFILE_COMPLETENESS";

    public static final String VERIFY_BANK = "VERIFY_BANK";
    public static final String ADD_PIN = "ADD_PIN";
    public static final String TRUSTED_NETWORK = "TRUSTED_NETWORK";
    public static final String TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE = "TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE";
    public static final String INTRODUCER = "INTRODUCER";
    public static final String BASIC_PROFILE = "BASIC_PROFILE";
    public static final String ADDRESS = "ADDRESS";
    public static final String PROFILE_PICTURE = "PROFILE_PICTURE";
    public static final String VERIFIED_EMAIL = "VERIFIED_EMAIL";
    public static final String PHOTOID = "PHOTOID";
    public static final String VERIFICATION_DOCUMENT = "VERIFICATION_DOCUMENT";
    public static final String ADD_BANK = "ADD_BANK";
    public static final String PARENT = "PARENT";

    public static final String TAG_BASIC_INFO = "Basic Info";
    public static final String TAG_ADDRESS = "Address";
    public static final String TAG_IDENTIFICATION = "Identification";
    public static final String TAG_LINK_BANK = "Link Bank";

    public static final int TAG_POSITION_BASIC_INFO = 0;
    public static final int TAG_POSITION_ADDRESS = 1;
    public static final int TAG_POSITION_IDENTIFICATION = 2;
    public static final int TAG_POSITION_LINK_BANK = 3;

    public static final HashMap<String, String> PROPERTY_NAME_TO_TITLE_MAP = new HashMap<>();
    public static final HashMap<String, Integer> PROPERTY_NAME_TO_ICON_MAP = new HashMap<>();

    static {
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFY_BANK, "Verify Bank Account");
        PROPERTY_NAME_TO_TITLE_MAP.put(ADD_PIN, "Set up PIN");
        PROPERTY_NAME_TO_TITLE_MAP.put(TRUSTED_NETWORK, "Setup Trusted Network");
        PROPERTY_NAME_TO_TITLE_MAP.put(TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE, "Add People Eligible for Password Recovery");
        PROPERTY_NAME_TO_TITLE_MAP.put(INTRODUCER, "Get Introduced by Other iPay Users");
        PROPERTY_NAME_TO_TITLE_MAP.put(BASIC_PROFILE, "Complete Basic Profile Information");
        PROPERTY_NAME_TO_TITLE_MAP.put(ADDRESS, "Add Present, Permanent and Office Addresses");
        PROPERTY_NAME_TO_TITLE_MAP.put(PROFILE_PICTURE, "Add a Profile Picture");
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFIED_EMAIL, "Add a Verified Email");
        PROPERTY_NAME_TO_TITLE_MAP.put(PHOTOID, "Submit Photo Identification");
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFICATION_DOCUMENT, "Submit Verification Document");
        PROPERTY_NAME_TO_TITLE_MAP.put(ADD_BANK, "Add Bank Account");
        PROPERTY_NAME_TO_TITLE_MAP.put(PARENT, "Add Parents' Details");
    }

    static {
        PROPERTY_NAME_TO_ICON_MAP.put(VERIFY_BANK, R.drawable.ic_banks);
//        PROPERTY_NAME_TO_ICON_MAP.put(ADD_PIN, "Set up PIN");
//        PROPERTY_NAME_TO_ICON_MAP.put(TRUSTED_NETWORK, "Setup Trusted Network");
//        PROPERTY_NAME_TO_ICON_MAP.put(TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE, "Add People Eligible for Password Recovery");
        PROPERTY_NAME_TO_ICON_MAP.put(INTRODUCER, R.drawable.ic_introducer);
        PROPERTY_NAME_TO_ICON_MAP.put(BASIC_PROFILE, R.drawable.ic_basic_info);
        PROPERTY_NAME_TO_ICON_MAP.put(ADDRESS, R.drawable.ic_address);
        PROPERTY_NAME_TO_ICON_MAP.put(PROFILE_PICTURE, R.drawable.ic_profile_picture);
//        PROPERTY_NAME_TO_ICON_MAP.put(VERIFIED_EMAIL, "Add a Verified Email");
        PROPERTY_NAME_TO_ICON_MAP.put(PHOTOID, R.drawable.ic_photo_id);
        PROPERTY_NAME_TO_ICON_MAP.put(VERIFICATION_DOCUMENT, R.drawable.ic_photo_id);
        PROPERTY_NAME_TO_ICON_MAP.put(ADD_BANK, R.drawable.ic_banks);
        PROPERTY_NAME_TO_ICON_MAP.put(PARENT, R.drawable.ic_parent_info);
    }
}