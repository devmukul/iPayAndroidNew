package bd.com.ipay.ipayskeleton.Utilities.CacheManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.BulkSignUp.GetUserDetailsResponse;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class BulkSignupUserDetailsCacheManager {

    private static SharedPreferences pref;
    private static Context context;

    public static void initialize(Context context) {
        BulkSignupUserDetailsCacheManager.context = context;
        pref = context.getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);
    }

    public static void updateBulkSignupUserInfoCache(GetUserDetailsResponse profileInfo) {
        if (profileInfo != null) {
            setIsBankInfoChecked(profileInfo.getBankInfoChecked());
            setBankAccountName(profileInfo.getBankAccountName());
            setBankAccountNumber(profileInfo.getBankAccountNumber());
            setBankName(profileInfo.getBankName());
            setBasicInfoChecked(profileInfo.getBasicInfoChecked());
            setBranchName(profileInfo.getBranchName());
            setDistrict(profileInfo.getDistrict());
            setDob(profileInfo.getDob());
            setEmail(profileInfo.getEmail());
            setFatherMobile(profileInfo.getFatherMobile());
            setFatherName(profileInfo.getFatherName());
            setGender(profileInfo.getGender());
            setMobileNumber(profileInfo.getMobileNumber());
            setMotherMobile(profileInfo.getMotherMobile());
            setMotherName(profileInfo.getMotherName());
            setNid(profileInfo.getNid());
            setOccupation(profileInfo.getOccupation());
            setOrganizationName(profileInfo.getOrganizationName());
            setPassport(profileInfo.getPassport());
            setPermanentAddress(profileInfo.getPermanentAddress());
            setPresentAddress(profileInfo.getPresentAddress());
            setStatus(profileInfo.getStatus());
            setThana(profileInfo.getThana());
        }
    }

    public static boolean isBankInfoChecked(boolean defaultValue) {
        return pref.getBoolean(SharedPrefConstants.BULK_SIGNUP_IS_BANK_INFO_CHECKED, defaultValue);
    }

    public static void setIsBankInfoChecked(boolean value) {
        pref.edit().putBoolean(SharedPrefConstants.BULK_SIGNUP_IS_BANK_INFO_CHECKED, value).apply();
    }

    public static String getBankAccountName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_BANK_ACOUNT_NAME, defaultValue);
    }

    public static void setBankAccountName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_BANK_ACOUNT_NAME, value).apply();
    }

    public static String getBankAccountNumber(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_BANK_ACOUNT_NUMBER, defaultValue);
    }

    public static void setBankAccountNumber(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_BANK_ACOUNT_NUMBER, value).apply();
    }

    public static String getBankName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_BANK_NAME, defaultValue);
    }

    public static void setBankName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_BANK_NAME, value).apply();
    }

    public static boolean isBasicInfoChecked(boolean defaultValue) {
        return pref.getBoolean(SharedPrefConstants.BULK_SIGNUP_IS_BASIC_INFO_CHECKED, defaultValue);
    }

    public static void setBasicInfoChecked(boolean value) {
        pref.edit().putBoolean(SharedPrefConstants.BULK_SIGNUP_IS_BASIC_INFO_CHECKED, value).apply();
    }

    public static String getBranchName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_BANK_BRANCH_NAME, defaultValue);
    }

    public static void setBranchName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_BANK_BRANCH_NAME, value).apply();
    }

    public static String getDistrict(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_DISTRICT, defaultValue);
    }

    public static void setDistrict(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_DISTRICT, value).apply();
    }

    public static String getDob(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_DOB, defaultValue);
    }

    public static void setDob(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_DOB, value).apply();
    }

    public static String getEmail(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_EMAIL, defaultValue);
    }

    public static void setEmail(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_EMAIL, value).apply();
    }

    public static String getFatherMobile(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_FATHERS_MOBILE_NO, defaultValue);
    }

    public static void setFatherMobile(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_FATHERS_MOBILE_NO, value).apply();
    }

    public static String getFatherName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_FATHERS_NAME, defaultValue);
    }

    public static void setFatherName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_FATHERS_NAME, value).apply();
    }

    public static String getGender(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_BULK_USER_GENDER, defaultValue);
    }

    public static void setGender(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_BULK_USER_GENDER, value).apply();
    }

    public static String getMobileNumber(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_MOBILE_NUMBER, defaultValue);
    }

    public static void setMobileNumber(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_MOBILE_NUMBER, value).apply();
    }

    public static String getMotherMobile(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_MOTHERS_MOBILE_NO, defaultValue);
    }

    public static void setMotherMobile(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_MOTHERS_MOBILE_NO, value).apply();
    }

    public static String getMotherName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_MOTHERS_NAME, defaultValue);
    }

    public static void setMotherName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_MOTHERS_NAME, value).apply();
    }

    public static String getNid(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_NID, defaultValue);
    }

    public static void setNid(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_NID, value).apply();
    }

    public static String getOccupation(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_OCCUPATION, defaultValue);
    }

    public static void setOccupation(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_OCCUPATION, value).apply();
    }

    public static String getOrganizationName(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_ORGANIZATION_NAME, defaultValue);
    }

    public static void setOrganizationName(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_ORGANIZATION_NAME, value).apply();
    }

    public static String getPassport(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_PASSPORT, defaultValue);
    }

    public static void setPassport(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_PASSPORT, value).apply();
    }

    public static String getPermanentAddress(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_PERMANENT_ADDRESS, defaultValue);
    }

    public static void setPermanentAddress(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_PERMANENT_ADDRESS, value).apply();
    }

    public static String getPresentAddress(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_PRESENT_ADDRESS, defaultValue);
    }

    public static void setPresentAddress(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_PRESENT_ADDRESS, value).apply();
    }

    public static String getStatus(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_STATUS, defaultValue);
    }

    public static void setStatus(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_STATUS, value).apply();
    }

    public static String getThana(String defaultValue) {
        return pref.getString(SharedPrefConstants.BULK_SIGNUP_THANA, defaultValue);
    }

    public static void setThana(String value) {
        pref.edit().putString(SharedPrefConstants.BULK_SIGNUP_THANA, value).apply();
    }


}

