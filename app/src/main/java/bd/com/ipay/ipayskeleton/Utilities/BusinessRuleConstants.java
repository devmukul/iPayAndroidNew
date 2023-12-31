package bd.com.ipay.ipayskeleton.Utilities;

public class BusinessRuleConstants {
    public static final String SERVICE_RULE_SEND_MONEY_MIN_AMOUNT_PER_PAYMENT = "SENDMONEY_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_SEND_MONEY_MAX_AMOUNT_PER_PAYMENT = "SENDMONEY_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_SEND_MONEY_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_SEND_MONEY_PIN_REQUIRED = "PIN_REQUIRED";

    public static final String SERVICE_RULE_REQUEST_MONEY_MIN_AMOUNT_PER_PAYMENT = "REQUESTMONEY_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_REQUEST_MONEY_MAX_AMOUNT_PER_PAYMENT = "REQUESTMONEY_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_REQUEST_MONEY_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_REQUEST_MONEY_PIN_REQUIRED = "PIN_REQUIRED";


    public static final String SERVICE_RULE_ADD_MONEY_MIN_AMOUNT_PER_PAYMENT = "ADDMONEY_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_ADD_MONEY_MAX_AMOUNT_PER_PAYMENT = "ADDMONEY_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_ADD_MONEY_VERIFICATION_REQUIRED = "ADDMONEY_VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_ADD_MONEY_PIN_REQUIRED = "PIN_REQUIRED";


    public static final String SERVICE_RULE_TOP_UP_MIN_AMOUNT_PER_PAYMENT = "TOPUP_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_TOP_UP_MAX_AMOUNT_PER_PAYMENT = "TOPUP_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_TOP_UP_VERIFICATION_REQUIRED = "TOPUP_VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_TOP_UP_PIN_REQUIRED = "PIN_REQUIRED";

    public static final String SERVICE_RULE_WITHDRAW_MONEY_MIN_AMOUNT_PER_PAYMENT = "WITHDRAW_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_WITHDRAW_MONEY_MAX_AMOUNT_PER_PAYMENT = "WITHDRAW_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_WITHDRAW_MONEY_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_WITHDRAW_MONEY_PIN_REQUIRED = "PIN_REQUIRED";


    public static final String SERVICE_RULE_MAKE_PAYMENT_MIN_AMOUNT_PER_PAYMENT = "PAYMENT_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_MAKE_PAYMENT_MAX_AMOUNT_PER_PAYMENT = "PAYMENT_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_MAKE_PAYMENT_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_MAKE_PAYMENT_PIN_REQUIRED = "PIN_REQUIRED";
    public static final String SERVICE_RULE_MAKE_PAYMENT_LOCATION_REQUIRED = "LOCATION_REQUIRED";


    public static final String SERVICE_RULE_MAKE_PAYMENT_MIN_AMOUNT_PER_PAYMENT_V3 = "MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_MAKE_PAYMENT_MAX_AMOUNT_PER_PAYMENT_V3 = "MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_MAKE_PAYMENT_VERIFICATION_REQUIRED_V3 = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_MAKE_PAYMENT_PIN_REQUIRED_V3 = "PIN_REQUIRED";
    public static final String SERVICE_RULE_MAKE_PAYMENT_LOCATION_REQUIRED_V3 = "LOCATION_REQUIRED";


    public static final String SERVICE_RULE_REQUEST_PAYMENT_MIN_AMOUNT_PER_PAYMENT = "REQUEST_PAYMENT_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_REQUEST_PAYMENT_MAX_AMOUNT_PER_PAYMENT = "REQUEST_PAYMENT_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_REQUEST_PAYMENT_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_REQUEST_PAYMENT_PIN_REQUIRED = "PIN_REQUIRED";
    public static final String SERVICE_RULE_REQUEST_PAYMENT_LOCATION_REQUIRED = "LOCATION_REQUIRED";


    public static final String SERVICE_RULE_ADD_CARDMONEY_MIN_AMOUNT_SINGLE = "ADD_CARDMONEY_MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_ADD_CARDMONEY_MAX_AMOUNT_SINGLE = "ADD_CARDMONEY_MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_ADD_CARDMONEY_VERIFICATION_REQUIRED = "ADD_CARDMONEY_VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_PAYMENT_REVERT_PIN_REQUIRED = "PIN_REQUIRED";


    public static final String SERVICE_RULE_UTILITY_BILL_PAYMENT_MIN_AMOUNT_PER_PAYMENT = "MIN_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_UTILITY_BILL_PAYMENT_MAX_AMOUNT_PER_PAYMENT = "MAX_AMOUNT_SINGLE";
    public static final String SERVICE_RULE_UTILITY_BILL_PAYMENT_VERIFICATION_REQUIRED = "VERIFICATION_REQUIRED";
    public static final String SERVICE_RULE_UTILITY_BILL_PAYMENT_PIN_REQUIRED = "PIN_REQUIRED";


    // Send Money Default values
    public static final int DEFAULT_VALUE_SEND_MONEY_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_SEND_MONEY_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_SEND_MONEY_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_SEND_MONEY_PIN_REQUIRED = true;

    // Request Money Default values
    public static final int DEFAULT_VALUE_REQUEST_MONEY_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_REQUEST_MONEY_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_REQUEST_MONEY_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_REQUEST_MONEY_PIN_REQUIRED = true;

    // Add Money by Bank Default values
    public static final int DEFAULT_VALUE_ADD_MONEY_BY_BANK_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_ADD_MONEY_BY_BANK_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_ADD_MONEY_BY_BANK_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_ADD_MONEY_BY_BANK_PIN_REQUIRED = true;

    // Add Money by Card Default values
    public static final int DEFAULT_VALUE_ADD_MONEY_BY_CARD_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_ADD_MONEY_BY_CARD_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_ADD_MONEY_BY_CARD_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_ADD_MONEY_BY_CARD_PIN_REQUIRED = true;

    // Withdraw Money Default values
    public static final int DEFAULT_VALUE_WITHDRAW_MONEY_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_WITHDRAW_MONEY_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_WITHDRAW_MONEY_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_WITHDRAW_MONEY_PIN_REQUIRED = true;

    // Make Payment Default values
    public static final int DEFAULT_VALUE_MAKE_PAYMENT_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_MAKE_PAYMENT_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_MAKE_PAYMENT_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_MAKE_PAYMENT_PIN_REQUIRED = true;

    // Request Payment Default values
    public static final int DEFAULT_VALUE_REQUEST_PAYMENT_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_REQUEST_PAYMENT_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_REQUEST_PAYMENT_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_REQUEST_PAYMENT_PIN_REQUIRED = true;

    // Top Up Default values
    public static final int DEFAULT_VALUE_TOP_UP_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_TOP_UP_MAX_AMOUNT_PER_PAYMENT = 10000;
    public static final boolean DEFAULT_VALUE_TOP_UP_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_TOP_UP_PIN_REQUIRED = true;

    // Request Payment Default values
    public static final int DEFAULT_VALUE_PAYMENT_REVERT_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_PAYMENT_REVERT_MAX_AMOUNT_PER_PAYMENT = 10000;
    public static final boolean DEFAULT_VALUE_PAYMENT_REVERT_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_PAYMENT_REVERT_PIN_REQUIRED = true;

    // Make Utilities Default values
    public static final int DEFAULT_VALUE_UTILITY_BILL_PAYMENT_MIN_AMOUNT_PER_PAYMENT = 0;
    public static final int DEFAULT_VALUE_UTILITY_BILL_PAYMENT_MAX_AMOUNT_PER_PAYMENT = 50000;
    public static final boolean DEFAULT_VALUE_UTILITY_BILL_PAYMENT_VERIFICATION_REQUIRED = false;
    public static final boolean DEFAULT_VALUE_UTILITY_BILL_PAYMENT_PIN_REQUIRED = true;

    // Location Required Default for all services
    public static final boolean DEFAULT_VALUE_LOCATION_REQUIRED = false;

    public static String[] SERVICE_BUSINESS_RULE_TAGS = new String[]{
            Constants.SEND_MONEY,
            Constants.REQUEST_MONEY,
            Constants.ADD_MONEY_BY_BANK,
            Constants.ADD_MONEY_BY_CARD,
            Constants.WITHDRAW_MONEY,
            Constants.MAKE_PAYMENT,
            Constants.REQUEST_PAYMENT,
            Constants.TOP_UP,
            Constants.PAYMENT_REVERT,
            Constants.UTILITY_BILL_PAYMENT
    };

    public static String SET_IS_DEFAULT_BUSINESS_RULES = "SET_IS_DEFAULT_BUSINESS_RULES";
}
