package bd.com.ipay.ipayskeleton.Utilities;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import bd.com.ipay.ipayskeleton.R;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class IdentificationDocumentConstants {

    public static final String DOCUMENT_TYPE_NATIONAL_ID = "national_id";
    public static final String DOCUMENT_TYPE_VAT_REG_CERT = "vat_reg_certificate";
    public static final String DOCUMENT_TYPE_TRADE_LICENSE = "trade_license";
    public static final String DOCUMENT_TYPE_BUSINESS_TIN = "business_tin";
    public static final String DOCUMENT_TYPE_DRIVING_LICENSE = "driving_license";
    public static final String DOCUMENT_TYPE_TIN = "tin";
    public static final String DOCUMENT_TYPE_BIRTH_CERTIFICATE = "birth_certificate";
    public static final String DOCUMENT_SIDE_FRONT = "front_side";
    public static final String DOCUMENT_SIDE_BACK = "back_side";

    public static final String DOCUMENT_TYPE_OTHER = "other";
    public static final String DOCUMENT_TYPE_PASSPORT = "passport";

    public static final String DOCUMENT_VERIFICATION_STATUS_VERIFIED = "VERIFIED";
    public static final String DOCUMENT_VERIFICATION_STATUS_NOT_VERIFIED = "NOT_VERIFIED";

    public static final String[] BUSINESS_DOCUMENT_TYPES = {
            DOCUMENT_TYPE_NATIONAL_ID,
            DOCUMENT_TYPE_BUSINESS_TIN,
            DOCUMENT_TYPE_TRADE_LICENSE,
            DOCUMENT_TYPE_VAT_REG_CERT,
            DOCUMENT_TYPE_DRIVING_LICENSE,
            DOCUMENT_TYPE_PASSPORT,
            DOCUMENT_TYPE_OTHER
    };
    public static final String[] PERSONAL_DOCUMENT_TYPES = {
            DOCUMENT_TYPE_NATIONAL_ID,
            DOCUMENT_TYPE_PASSPORT,
            DOCUMENT_TYPE_DRIVING_LICENSE,
            DOCUMENT_TYPE_OTHER
    };

    private static final Map<String, Integer> DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP;
    private static final Map<String, Integer> DOCUMENT_ID_MAX_PAGE_COUNT_MAP;

    static final String INVALID_PASSPORT_ID_WITH_INSUFFICIENT_LENGTH_PATTERN = "[A-Z]{2}[0-9]{0,6}|[A-Z]{1}";
    static final String VALID_PASSPORT_ID_PATTERN = "[A-Z]{2}[0-9]{7}";
    static final String INVALID_DRIVING_LICENSE_ID_WITH_INSUFFICIENT_LENGTH_PATTERN = "[A-Z]{2}[0-9]{0,7}|[A-Z]{1}|[A-Z]{2}[0-9]{7}[A-Z]{1,2}|" +
            "[A-Z]{2}[0-9]{7}[A-Z]{1}[0-9]{1,4}|[A-Z]{2}[0-9]{7}[A-Z]{2}[0-9]{1,3}";
    static final String VALID_DRIVING_LICENSE_ID_PATTERN = "[A-Z]{2}[0-9]{7}[A-Z]{1}[0-9]{5}|[A-Z]{2}[0-9]{7}[A-Z]{2}[0-9]{4}";


    static {
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP = new HashMap<>();

        //Common Document
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_NATIONAL_ID, R.string.national_id_no);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_OTHER, R.string.other_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_DRIVING_LICENSE, R.string.driving_license_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_PASSPORT, R.string.passport_id);

        //Personal Account Documents
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_BIRTH_CERTIFICATE, R.string.birth_certificate_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_TIN, R.string.personal_tin_id);

        //Business Account Documents
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_BUSINESS_TIN, R.string.business_tin_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_TRADE_LICENSE, R.string.trade_license_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_VAT_REG_CERT, R.string.vat_registration_certificate_id);
        DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.put(DOCUMENT_TYPE_TRADE_LICENSE, R.string.trade_license_id);
    }

    static {
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP = new HashMap<>();

        //Common Document
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_NATIONAL_ID, 2);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_OTHER, 2);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_DRIVING_LICENSE, 2);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_PASSPORT, 2);

        //Personal Account Documents
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_BIRTH_CERTIFICATE, 1);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_TIN, 1);

        //Business Account Documents
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_BUSINESS_TIN, 1);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_TRADE_LICENSE, 1);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_VAT_REG_CERT, 1);
        DOCUMENT_ID_MAX_PAGE_COUNT_MAP.put(DOCUMENT_TYPE_TRADE_LICENSE, 1);
    }

    public static int getMaxDocumentPageCount(@DocumentType String documentType) {
        return DOCUMENT_ID_MAX_PAGE_COUNT_MAP.get(documentType);
    }

    public static int getDocumentIDHintText(@DocumentType String documentType) {
        return DOCUMENT_ID_TO_EDIT_TEXT_HINT_MAP.get(documentType);
    }

    @Retention(SOURCE)
    @Target({PARAMETER})
    @StringDef(value = {DOCUMENT_TYPE_NATIONAL_ID,
            DOCUMENT_TYPE_OTHER,
            DOCUMENT_TYPE_DRIVING_LICENSE,
            DOCUMENT_TYPE_PASSPORT,
            DOCUMENT_TYPE_BIRTH_CERTIFICATE,
            DOCUMENT_TYPE_TIN,
            DOCUMENT_TYPE_BUSINESS_TIN,
            DOCUMENT_TYPE_TRADE_LICENSE,
            DOCUMENT_TYPE_VAT_REG_CERT,
            DOCUMENT_TYPE_TRADE_LICENSE})
    private @interface DocumentType {

    }
}
