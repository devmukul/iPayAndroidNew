package bd.com.ipay.ipayskeleton.Utilities;

/**
 * Constant value holder for the ACL related services.
 */
public class ServiceIdConstants {

    // Balance
    public static final int OPENING_BALANCE = 1001;
    public static final int BALANCE = 1002;
    public static final int OFFER = 1100;

    // Send Money
    public static final int SEND_MONEY = 1;

    //Request Money
    public static final int REQUEST_MONEY = 6001;
    public static final int ACCEPT_REQUEST = 1003;
    public static final int CANCEL_REQUEST = 1004;
    public static final int REJECT_REQUEST = 1005;
    public static final int SENT_REQUEST = 1006;
    public static final int RECEIVED_REQUEST = 1007;

    // Top up
    public static final int TOP_UP = 2001;

    // Payment
    public static final int MAKE_PAYMENT = 6002;
    public static final int REQUEST_PAYMENT = 6005;
    public static final int PAYMENT_REVERT = 966002;
    public static final int UTILITY_BILL_PAYMENT = 6010;
    public static final int RAILWAY_TICKET = 6011;

    // Transaction Details
    public static final int ALL_TRANSACTION = 1008;
    public static final int COMPLETED_TRANSACTION = 1009;
    public static final int PENDING_TRANSACTION = 1010;
    public static final int TRANSACTION_DETAILS = 1011;
    public static final int STATEMENT = 1012;

    // Add Money Service
    public static final int ADD_MONEY_BY_BANK = 3001;
    public static final int ADD_MONEY_BY_BANK_INSTANTLY = 3006;
    public static final int ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD = 3011;

    // Withdraw Money Service
    public static final int WITHDRAW_MONEY = 3002;

    // Contact
    public static final int GET_CONTACTS = 5001;
    public static final int ADD_CONTACTS = 5002;
    public static final int UPDATE_CONTACTS = 5003;
    public static final int DELETE_CONTACTS = 5004;
    public static final int SYNC_GOOGLE_CONTACTS = 5005;
    public static final int IMPORT_VCARD = 5006;
    public static final int IMPORT_OUTLOOK_CSV = 5007;


    // Bank Account Manage
    public static final int SEE_BANK_ACCOUNTS = 8001;
    public static final int MANAGE_BANK_ACCOUNTS = 8039;

    // Employee Manage
    public static final int SEE_EMPLOYEE = 8002;
    public static final int MANAGE_EMPLOYEE = 8003;
    public static final int SEE_MANAGERS = 8060;
    public static final int SEE_BUSINESS_ROLES = 8055;


    // Account
    public static final int SEE_EMAILS = 8004;
    public static final int SEE_ADDRESSES = 8005;
    public static final int MANAGE_ADDRESS = 8006;
    public static final int SEE_BUSINESS_INFO = 8007;
    public static final int UPDATE_BUSINESS_INFO = 8008;
    public static final int MANAGE_PROFILE = 8009;
    public static final int SEE_PROFILE_COMPLETION = 8016;
    public static final int MANAGE_EMAILS = 8032;
    public static final int SEE_USER_INFO = 8033;
    public static final int SEE_PROFILE = 8034;
    public static final int SEE_PARENT = 8035;
    public static final int MANAGE_PARENT = 8036;
    public static final int MANAGE_PROFILE_PICTURE = 8037;

    // Introducer Related
    public static final int SEE_INTRODUCERS = 8012;
    public static final int MANAGE_INTRODUCERS = 8013;

    // Invitation Related
    public static final int SEE_INVITATIONS = 8014;
    public static final int MANAGE_INVITATIONS = 8015;

    // Business Related
    public static final int SEE_BUSINESS = 8018;
    public static final int MANAGE_BUSINESS = 8019;

    public static final int SEE_ACTIVITY = 8017;
    public static final int SEE_OTHER_ACTIVITY = 8046;

    //business account switch related
    public static final int SEE_BUSINESS_ROLE_INVITATION_REQUEST = 8057;
    public static final int SEND_BUSINESS_MANAGER_INVITATION = 8058;
    public static final int ACCEPT_REJECT_BUSINESS_MANAGER_INVITATION = 8059;
    public static final int UPDATE_BUSINESS_MANAGER_ROLE = 8061;
    public static final int REMOVE_BUSINESS_MANAGER = 8062;

    // Identification Document Related
    public static final int SEE_BUSINESS_DOCS = 8010;
    public static final int UPLOAD_BUSINESS_DOCS = 8011;
    public static final int SEE_IDENTIFICATION_DOCS = 8020;
    public static final int MANAGE_IDENTIFICATION_DOCS = 8021;

    // Security
    public static final int SEE_TRUSTED_PERSON = 8030;
    public static final int MANAGE_TRUSTED_PERSON = 8031;
    public static final int SEE_TRUSTED_DEVICES = 8040;
    public static final int MANAGE_TRUSTED_DEVICES = 8041;
    public static final int SEE_PIN_EXISTS = 8047;
    public static final int CHANGE_PIN = 8048;
    public static final int CHANGE_PASSWORD = 8049;
    public static final int SEE_SECURITY_QUESTIONS = 8050;
    public static final int MANAGE_SECURITY_QUESTIONS = 8051;
    public static final int SIGN_OUT_FROM_ALL_DEVICES = 8052;


    // Login Related
    public static final int SIGN_IN = 8025;
    public static final int SIGN_OUT = 8026;
    public static final int SIGN_UP_PERSONAL = 8027;
    public static final int SIGN_UP_BUSINESS = 8028;


    public static final int SEE_RESOURCE = 8029;
    public static final int SEE_APP_VERSION = 8038;
    public static final int SEE_ACCESS_TOKEN = 8042;
    public static final int USER_EXISTENCE_CHECK = 8043;
    public static final int SEE_MAX_ALLOWED_INVITATION = 8044;
    public static final int SEE_CONSENT_AGREEMENT = 8045;

    public static final int GET_SOURCE_OF_FUND = 8070;
    public static final int CREATE_SOURCE_OF_FUND = 8071;
    public static final int DESCO = 6015;
    public static final int DPDC = 6016;
}
