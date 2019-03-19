package bd.com.ipay.ipayskeleton.DatabaseHelper;

import android.net.Uri;

import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class DBConstants {

    public static final String TAG = "DataBaseOpenHelper";
    public static final String DB_PATH = "/data/data/" + Constants.ApplicationPackage + "/databases/";
    public static final String DB_IPAY = "iPayDatabase";
    public static final String DB_TABLE_CONTACTS = "contacts";
    public static final String DB_TABLE_BUSINESS_ACCOUNTS = "business";
    public static final String DB_TABLE_CONTACTS_BUSINESS = "business_contacts";
    public static final Uri DB_TABLE_CONTACTS_URI = Uri
            .parse("sqlite://" + Constants.ApplicationPackage + "/" + DB_TABLE_CONTACTS);
    public static final Uri DB_TABLE_BUSINESS_URI = Uri
            .parse("sqlite://" + Constants.ApplicationPackage + "/" + DB_TABLE_BUSINESS_ACCOUNTS);
    public static final Uri DB_TABLE_BUSINESS_CONTWACT_URI = Uri
            .parse("sqlite://" + Constants.ApplicationPackage + "/" + DB_TABLE_CONTACTS_BUSINESS);
    // Subscriber table
    public static final String KEY_MOBILE_NUMBER = "mobile_number";
    public static final String KEY_VERIFICATION_STATUS = "verification_status";
    public static final String KEY_NAME = "name";
    public static final String KEY_ORIGINAL_NAME = "original_name";
    public static final String KEY_ACCOUNT_TYPE = "account_type";
    public static final String KEY_PROFILE_PICTURE = "profile_picture";
    public static final String KEY_PROFILE_PICTURE_QUALITY_MEDIUM = "profile_picture_quality_medium";
    public static final String KEY_PROFILE_PICTURE_QUALITY_HIGH = "profile_picture_quality_high";
    public static final String KEY_RELATIONSHIP = "relationship";
    public static final String KEY_IS_MEMBER = "is_member";
    public static final String KEY_UPDATE_TIME = "update_at";
    public static final String KEY_IS_ACTIVE = "is_active";

    // Business table
    public static final String KEY_BUSINESS_MOBILE_NUMBER = "mobile_number";
    public static final String KEY_BUSINESS_NAME = "business_name";
    public static final String KEY_COMPANY_NAME = "company_name";
    public static final String BUSINESS_EMAIL = "email";
    public static final String KEY_BUSINESS_TYPE = "business_type";
    public static final String KEY_BUSINESS_ACCOUNT_ID = "business_id";
    public static final String KEY_BUSINESS_PROFILE_PICTURE = "profile_picture";
    public static final String KEY_BUSINESS_PROFILE_PICTURE_QUALITY_MEDIUM = "profile_picture_quality_medium";
    public static final String KEY_BUSINESS_PROFILE_PICTURE_QUALITY_HIGH = "profile_picture_quality_high";
    public static final String KEY_BUSINESS_ADDRESS = "business_address";
    public static final String KEY_BUSINESS_THANA = "business_thana";
    public static final String KEY_BUSINESS_DISTRICT = "business_district";
    public static final String KEY_BUSINESS_OUTLET = "business_outlet";

    // Push events table
    public static final String KEY_TAG_NAME = "tag_name";
    public static final String KEY_JSON = "json";

    public static final int VERIFIED_USER = 1;
    public static final int NOT_VERIFIED_USER = 0;

    public static final int BUSINESS_USER = 2;
    public static final int PERSONAL_USER = 1;

    public static final int IPAY_MEMBER = 1;
    public static final int NOT_IPAY_MEMBER = 0;

    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
}
