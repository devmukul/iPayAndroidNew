package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion;

import java.util.HashMap;

import bd.com.ipay.ipayskeleton.R;

public class ProfileCompletionPropertyConstants {

    public static final String PROFILE_COMPLETENESS = "PROFILE_COMPLETENESS";

    public static final String ADD_PIN = "ADD_PIN";
    public static final String TRUSTED_NETWORK = "TRUSTED_NETWORK";
    public static final String TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE = "TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE";
    public static final String INTRODUCER = "INTRODUCER";
    public static final String BASIC_PROFILE = "BASIC_PROFILE";
    public static final String PERSONAL_ADDRESS = "PERSONAL_ADDRESS";
    public static final String PROFILE_PICTURE = "PROFILE_PICTURE";
    public static final String VERIFIED_EMAIL = "VERIFIED_EMAIL";
    public static final String PHOTOID = "PHOTO_ID";
    public static final String VERIFICATION_DOCUMENT = "VERIFICATION_DOCUMENT";
    public static final String VERIFY_BANK_OR_CARD = "VERIFY_BANK_OR_CARD";
    public static final String PARENT = "PARENT";
    public static final String PROFILE_INFO = "PROFILEINFO";
    public static final String ADD_AND_VERIFY_BANK = "ADD_AND_VERIFY_BANK";

    public static final String BUSINESS_ADDRESS = "BUSINESS_ADDRESS";
    public static final String BUSINESS_INFO = "BUSINESS_INFO";
    public static final String BUSINESS_DOCUMENTS = "BUSINESS_DOCUMENTS";

    public static final int TAG_POSITION_PROFILE_PICTURE = 0;
    public static final int TAG_POSITION_IDENTIFICATION = 1;
    public static final int TAG_POSITION_BASIC_INFO = 2;
    public static final int TAG_POSITION_SOURCE_OF_FUND = 3;

    public static final int TAG_POSITION_BUSINESS_INFO = 0;
    public static final int TAG_POSITION_BUSINESS_ADDRESS = 1;
    public static final int TAG_POSITION_BUSINESS_DOCUMENTS = 2;

    public static final HashMap<String, String> PROPERTY_NAME_TO_TITLE_MAP = new HashMap<>();
    public static final HashMap<String, Integer> PROPERTY_NAME_TO_ICON_MAP = new HashMap<>();
    public static final HashMap<String, String> PROPERTY_NAME_TO_ACTION_NAME_MAP = new HashMap<>();
    public static final HashMap<String, Integer> PROPERTY_NAME_TO_SCORE_MAP = new HashMap<>();

    static {
        PROPERTY_NAME_TO_TITLE_MAP.put(ADD_PIN, "Set up PIN");
        PROPERTY_NAME_TO_TITLE_MAP.put(TRUSTED_NETWORK, "Setup trusted network");
        PROPERTY_NAME_TO_TITLE_MAP.put(TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE, "Add people eligible for password recovery");
        PROPERTY_NAME_TO_TITLE_MAP.put(INTRODUCER, "Get Introduced by iPay Users");
        PROPERTY_NAME_TO_TITLE_MAP.put(BASIC_PROFILE, "Complete Basic Information");
        PROPERTY_NAME_TO_TITLE_MAP.put(BUSINESS_INFO, "Complete Basic Business Information");
        PROPERTY_NAME_TO_TITLE_MAP.put(BUSINESS_DOCUMENTS, "Submit Business Documents");
        PROPERTY_NAME_TO_TITLE_MAP.put(PERSONAL_ADDRESS, "Add Addresses");
        PROPERTY_NAME_TO_TITLE_MAP.put(BUSINESS_ADDRESS, "Add Business Address");
        PROPERTY_NAME_TO_TITLE_MAP.put(PROFILE_PICTURE, "Add a Profile Picture");
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFIED_EMAIL, "Add a verified email");
        PROPERTY_NAME_TO_TITLE_MAP.put(PHOTOID, "Submit Document");
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFICATION_DOCUMENT, "Submit Verification Document");
        PROPERTY_NAME_TO_TITLE_MAP.put(VERIFY_BANK_OR_CARD, "Verify Bank or Card");
        PROPERTY_NAME_TO_TITLE_MAP.put(PARENT, "Add Parent's Information");
        PROPERTY_NAME_TO_TITLE_MAP.put(ADD_AND_VERIFY_BANK, "Add and Verify Bank Account");
    }

    static {
        PROPERTY_NAME_TO_ICON_MAP.put(INTRODUCER, R.drawable.ic_introducer_profile);
        PROPERTY_NAME_TO_ICON_MAP.put(BASIC_PROFILE, R.drawable.ic_basicinfo_profile);
        PROPERTY_NAME_TO_ICON_MAP.put(BUSINESS_INFO, R.drawable.ic_basicinfo_bizinfo);
        PROPERTY_NAME_TO_ICON_MAP.put(PERSONAL_ADDRESS, R.drawable.ic_address_profile);
        PROPERTY_NAME_TO_ICON_MAP.put(BUSINESS_ADDRESS, R.drawable.ic_signup_add1);
        PROPERTY_NAME_TO_ICON_MAP.put(PROFILE_PICTURE, R.drawable.ic_signup_personal);
        PROPERTY_NAME_TO_ICON_MAP.put(PHOTOID, R.drawable.ic_photoid);
        PROPERTY_NAME_TO_ICON_MAP.put(VERIFICATION_DOCUMENT, R.drawable.ic_doc);
        PROPERTY_NAME_TO_ICON_MAP.put(BUSINESS_DOCUMENTS, R.drawable.ic_photoid);
        PROPERTY_NAME_TO_ICON_MAP.put(VERIFY_BANK_OR_CARD, R.drawable.ic_bank);
        PROPERTY_NAME_TO_ICON_MAP.put(PARENT, R.drawable.ic_parent);
        PROPERTY_NAME_TO_ICON_MAP.put(ADD_AND_VERIFY_BANK, R.drawable.ic_bank);
    }

    static {
        PROPERTY_NAME_TO_ACTION_NAME_MAP.put(ADD_PIN, "Setup Now");
        PROPERTY_NAME_TO_ACTION_NAME_MAP.put(TRUSTED_NETWORK, "Setup Now");
        PROPERTY_NAME_TO_ACTION_NAME_MAP.put(TRUSTED_NETWORK_AND_PASSWORD_RECOVERY_RULE, "Add Now");
        PROPERTY_NAME_TO_ACTION_NAME_MAP.put(VERIFIED_EMAIL, "Add Now");
    }
}
