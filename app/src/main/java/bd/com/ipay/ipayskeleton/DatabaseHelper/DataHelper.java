package bd.com.ipay.ipayskeleton.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.Contact.ContactNode;
import bd.com.ipay.ipayskeleton.Model.SqLiteDatabase.BusinessAccountEntry;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;

public class DataHelper {

    private static final int DATABASE_VERSION = 16;

    private final Context context;
    private static DataHelper instance = null;
    private static DataBaseOpenHelper dOpenHelper;

    private DataHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized DataHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DataHelper(context);
            dOpenHelper = new DataBaseOpenHelper(context, DBConstants.DB_IPAY,
                    DATABASE_VERSION);
        }
        return instance;
    }

    public void closeDbOpenHelper() {
        if (dOpenHelper != null) dOpenHelper.close();
        instance = null;
    }

    public void createContacts(List<ContactNode> contactList) {
        if (contactList != null && !contactList.isEmpty()) {

            SQLiteDatabase db = dOpenHelper.getWritableDatabase();
            db.beginTransaction();

            try {
                for (ContactNode contactNode : contactList) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY_MOBILE_NUMBER, contactNode.getMobileNumber());
                    values.put(DBConstants.KEY_NAME, contactNode.getName());
                    values.put(DBConstants.KEY_ORIGINAL_NAME, contactNode.getOriginalName());
                    values.put(DBConstants.KEY_ACCOUNT_TYPE, contactNode.getAccountType());
                    values.put(DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM, contactNode.getProfilePictureUrlMedium());
                    values.put(DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH, contactNode.getProfilePictureUrlHigh());
                    values.put(DBConstants.KEY_RELATIONSHIP, contactNode.getRelationship());
                    values.put(DBConstants.KEY_VERIFICATION_STATUS, contactNode.isVerified() ?
                            DBConstants.VERIFIED_USER : DBConstants.NOT_VERIFIED_USER);
                    values.put(DBConstants.KEY_UPDATE_TIME, contactNode.getUpdateTime());
                    values.put(DBConstants.KEY_IS_MEMBER, contactNode.isMember() ?
                            DBConstants.IPAY_MEMBER : DBConstants.NOT_IPAY_MEMBER);
                    values.put(DBConstants.KEY_IS_ACTIVE, contactNode.isActive() ?
                            DBConstants.ACTIVE : DBConstants.INACTIVE);

                    db.insertWithOnConflict(DBConstants.DB_TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            context.getContentResolver().notifyChange(DBConstants.DB_TABLE_CONTACTS_URI, null);

            Logger.logI("Contacts", "Inserted into the database");
        }
    }

    public void createBills(List<RecentBill> billList) {
        if (billList != null && !billList.isEmpty()) {

            SQLiteDatabase db = dOpenHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (RecentBill recentBill : billList) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY_BILL_PROVIDER_CODE, recentBill.getProviderCode());
                    values.put(DBConstants.KEY_BILL_PROVIDER_NAME, recentBill.getShortName());
                    values.put(DBConstants.KEY_BILL_IS_SAVED, recentBill.getSaved() ? 1 : 0 );
                    values.put(DBConstants.KEY_BILL_IS_SCHEDULED, recentBill.getScheduledToo() ? 1 : 0 );
                    values.put(DBConstants.KEY_BILL_DATE, recentBill.getDateOfBillPayment());
                    values.put(DBConstants.KEY_BILL_LAST_PAID_DATE, recentBill.getLastPaid());
                    values.put(DBConstants.KEY_BILL_PAID_FOR_OTHERS, recentBill.getPaidForOthers() ? 1 : 0 );
                    values.put(DBConstants.KEY_BILL_PARAMS_ID, recentBill.getParamId());
                    values.put(DBConstants.KEY_BILL_PARAMS_LABEL, recentBill.getParamLabel());
                    values.put(DBConstants.KEY_BILL_PARAMS_VALUE, recentBill.getParamValue() );
                    values.put(DBConstants.KEY_BILL_AMOUNT, recentBill.getAmount() );
                    values.put(DBConstants.KEY_BILL_AMOUNT_TYPE, recentBill.getAmountType());
                    values.put(DBConstants.KEY_BILL_META_DATA, recentBill.getMetaData());

                    db.insertWithOnConflict(DBConstants.DB_TABLE_SAVED_BILL, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            context.getContentResolver().notifyChange(DBConstants.DB_TABLE_CONTACTS_URI, null);

            Logger.logI("Contacts", "Inserted into the database");
        }
    }

    public List<RecentBill> getBills(String provider) {

        List<RecentBill> recentBills = new ArrayList<>();

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_SAVED_BILL
                    + " WHERE " + DBConstants.KEY_BILL_PROVIDER_CODE + " LIKE '%" + provider+ "%'"
                    +" ORDER BY " + DBConstants.KEY_BILL_LAST_PAID_DATE +" DESC, "
                    +"_id DESC";

            Logger.logW("Query", queryString);

            Cursor cursor = db.rawQuery(queryString, null);

            if (cursor.moveToFirst()) {
                do {

                    RecentBill recentBill = new RecentBill();
                    recentBill.setProviderCode(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PROVIDER_CODE)));
                    recentBill.setShortName(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PROVIDER_NAME)));
                    int isSaved = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_IS_SAVED));
                    if(isSaved==1)
                        recentBill.setSaved(true);
                    else
                        recentBill.setSaved(false);
                    int isSchedule = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_IS_SCHEDULED));
                    if(isSchedule==1)
                        recentBill.setScheduledToo(true);
                    else
                        recentBill.setScheduledToo(false);

                    int isOther = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_PAID_FOR_OTHERS));
                    if(isOther==1)
                        recentBill.setPaidForOthers(true);
                    else
                        recentBill.setPaidForOthers(false);
                    recentBill.setDateOfBillPayment(Integer.valueOf(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_DATE))));
                    recentBill.setLastPaid(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_LAST_PAID_DATE)));
                    recentBill.setParamId(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_ID)));
                    recentBill.setParamLabel(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_LABEL)));
                    recentBill.setParamValue(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_VALUE)));
                    recentBill.setAmount(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_AMOUNT)));
                    recentBill.setAmountType(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_AMOUNT_TYPE)));
                    recentBill.setLocationCode(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_LOCATION_CODE)));
                    recentBill.setMetaData(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_META_DATA)));
                    recentBills.add(recentBill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recentBills;
    }

    public List<RecentBill> getBills(String provider1, String provider2) {

        List<RecentBill> recentBills = new ArrayList<>();

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_SAVED_BILL
                    + " WHERE (" + DBConstants.KEY_BILL_PROVIDER_CODE + " LIKE '%" + provider1+ "%'"
                    + " OR " + DBConstants.KEY_BILL_PROVIDER_CODE + " LIKE '%" + provider2+ "%'"
                    +") ORDER BY " + DBConstants.KEY_BILL_LAST_PAID_DATE +" DESC, "
                    +"_id DESC";

            Logger.logW("Query", queryString);

            Cursor cursor = db.rawQuery(queryString, null);

            if (cursor.moveToFirst()) {
                do {

                    RecentBill recentBill = new RecentBill();
                    recentBill.setProviderCode(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PROVIDER_CODE)));
                    recentBill.setShortName(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PROVIDER_NAME)));
                    int isSaved = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_IS_SAVED));
                    if(isSaved==1)
                        recentBill.setSaved(true);
                    else
                        recentBill.setSaved(false);
                    int isSchedule = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_IS_SCHEDULED));
                    if(isSchedule==1)
                        recentBill.setScheduledToo(true);
                    else
                        recentBill.setScheduledToo(false);

                    int isOther = cursor.getInt(cursor.getColumnIndex(DBConstants.KEY_BILL_PAID_FOR_OTHERS));
                    if(isOther==1)
                        recentBill.setPaidForOthers(true);
                    else
                        recentBill.setPaidForOthers(false);
                    recentBill.setDateOfBillPayment(Integer.valueOf(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_DATE))));
                    recentBill.setLastPaid(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_LAST_PAID_DATE)));
                    recentBill.setParamId(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_ID)));
                    recentBill.setParamLabel(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_LABEL)));
                    recentBill.setParamValue(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_PARAMS_VALUE)));
                    recentBill.setAmount(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_AMOUNT)));
                    recentBill.setAmountType(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_AMOUNT_TYPE)));
                    recentBill.setLocationCode(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_LOCATION_CODE)));
                    recentBill.setMetaData(cursor.getString(cursor.getColumnIndex(DBConstants.KEY_BILL_META_DATA)));
                    recentBills.add(recentBill);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recentBills;
    }

    public void createBusinessContacts(List<ContactNode> contactList) {
        if (contactList != null && !contactList.isEmpty()) {

            SQLiteDatabase db = dOpenHelper.getWritableDatabase();
            db.beginTransaction();

            try {
                for (ContactNode contactNode : contactList) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY_MOBILE_NUMBER, contactNode.getMobileNumber());
                    values.put(DBConstants.KEY_NAME, contactNode.getName());
                    values.put(DBConstants.KEY_ORIGINAL_NAME, contactNode.getOriginalName());
                    values.put(DBConstants.KEY_ACCOUNT_TYPE, contactNode.getAccountType());
                    values.put(DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM, contactNode.getProfilePictureUrlMedium());
                    values.put(DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH, contactNode.getProfilePictureUrlHigh());
                    values.put(DBConstants.KEY_RELATIONSHIP, contactNode.getRelationship());
                    values.put(DBConstants.KEY_VERIFICATION_STATUS, contactNode.isVerified() ?
                            DBConstants.VERIFIED_USER : DBConstants.NOT_VERIFIED_USER);
                    values.put(DBConstants.KEY_UPDATE_TIME, contactNode.getUpdateTime());
                    values.put(DBConstants.KEY_IS_MEMBER, contactNode.isMember() ?
                            DBConstants.IPAY_MEMBER : DBConstants.NOT_IPAY_MEMBER);
                    values.put(DBConstants.KEY_IS_ACTIVE, contactNode.isActive() ?
                            DBConstants.ACTIVE : DBConstants.INACTIVE);
                    values.put(DBConstants.KEY_BUSINESS_ACCOUNT_ID, TokenManager.getOnAccountId());

                    db.insertWithOnConflict(DBConstants.DB_TABLE_CONTACTS_BUSINESS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            context.getContentResolver().notifyChange(DBConstants.DB_TABLE_BUSINESS_CONTACT_URI, null);

            Logger.logI("Contacts", "Inserted into the database");
        }
    }

    public void createBusinessAccountsList(List<BusinessAccountEntry> businessAccountEntries) {
        if (businessAccountEntries != null && !businessAccountEntries.isEmpty()) {

            SQLiteDatabase db = dOpenHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                db.execSQL("DELETE FROM " + DBConstants.DB_TABLE_BUSINESS_ACCOUNTS);
                for (BusinessAccountEntry mBusinessAccountEntry : businessAccountEntries) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY_BUSINESS_MOBILE_NUMBER, mBusinessAccountEntry.getMobileNumber());
                    values.put(DBConstants.KEY_BUSINESS_NAME, mBusinessAccountEntry.getBusinessName());
                    values.put(DBConstants.KEY_COMPANY_NAME, mBusinessAccountEntry.getCompanyName());
                    values.put(DBConstants.BUSINESS_EMAIL, mBusinessAccountEntry.getEmail());
                    values.put(DBConstants.KEY_BUSINESS_TYPE, mBusinessAccountEntry.getBusinessType());
                    values.put(DBConstants.KEY_BUSINESS_PROFILE_PICTURE, mBusinessAccountEntry.getProfilePictureUrl());
                    values.put(DBConstants.KEY_BUSINESS_PROFILE_PICTURE_QUALITY_MEDIUM, mBusinessAccountEntry.getProfilePictureUrlMedium());
                    values.put(DBConstants.KEY_BUSINESS_PROFILE_PICTURE_QUALITY_HIGH, mBusinessAccountEntry.getProfilePictureUrlHigh());
                    values.put(DBConstants.KEY_BUSINESS_ACCOUNT_ID, mBusinessAccountEntry.getBusinessId());
                    values.put(DBConstants.KEY_BUSINESS_ADDRESS, mBusinessAccountEntry.getAddressString());
                    values.put(DBConstants.KEY_BUSINESS_THANA, mBusinessAccountEntry.getThanaString());
                    values.put(DBConstants.KEY_BUSINESS_DISTRICT, mBusinessAccountEntry.getDistrictString());
                    values.put(DBConstants.KEY_BUSINESS_OUTLET, mBusinessAccountEntry.getOutLetString());

                    db.insertWithOnConflict(DBConstants.DB_TABLE_BUSINESS_ACCOUNTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            context.getContentResolver().notifyChange(DBConstants.DB_TABLE_BUSINESS_URI, null);

            Logger.logI("Business", "Inserted into the database");
        }
    }

    public Cursor searchContacts(String query, boolean memberOnly, boolean businessMemberOnly, boolean nonMemberOnly,
                                 boolean verifiedOnly, boolean invitedOnly, boolean nonInvitedOnly, List<String> invitees) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_CONTACTS
                    + " WHERE (" + DBConstants.KEY_NAME + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_MOBILE_NUMBER + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_ORIGINAL_NAME + " LIKE '%" + query + "%')";

            // Get Verified Users
            if (verifiedOnly)
                queryString += " AND " + DBConstants.KEY_VERIFICATION_STATUS + " = " + DBConstants.VERIFIED_USER;

            // Get iPay Users
            if (memberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER;

            // Get iPay Business Users
            if (businessMemberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER + " AND " + DBConstants.KEY_ACCOUNT_TYPE + " = " + DBConstants.BUSINESS_USER;

            // Get Non-iPay Users
            if (nonMemberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " != " + DBConstants.IPAY_MEMBER;

            if (invitees != null) {
                List<String> quotedInvitees = new ArrayList<>();
                for (String invitee : invitees) {
                    quotedInvitees.add("'" + invitee + "'");
                }

                String inviteeListStr = "(" + TextUtils.join(", ", quotedInvitees) + ")";

                if (invitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " IN " + inviteeListStr;
                }

                if (nonInvitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " NOT IN " + inviteeListStr;
                }
            }
            // Select only active contacts
            queryString += " AND " + DBConstants.KEY_IS_ACTIVE + " = " + DBConstants.ACTIVE;

            // If original name exists, then user original name as the sorting parameter.
            // Otherwise use normal name as the sorting parameter.
            queryString += " ORDER BY CASE "
                    + "WHEN (" + DBConstants.KEY_ORIGINAL_NAME + " IS NULL OR " + DBConstants.KEY_ORIGINAL_NAME + " = '')"
                    + " THEN " + DBConstants.KEY_NAME
                    + " ELSE "
                    + DBConstants.KEY_ORIGINAL_NAME + " END COLLATE NOCASE";

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public Cursor searchBusinessContacts(String query, boolean memberOnly, boolean businessMemberOnly, boolean nonMemberOnly,
                                         boolean verifiedOnly, boolean invitedOnly, boolean nonInvitedOnly, List<String> invitees, Long onAccountId) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_CONTACTS_BUSINESS
                    + " WHERE (" + DBConstants.KEY_NAME + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_MOBILE_NUMBER + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_ORIGINAL_NAME + " LIKE '%" + query + "%'"
                    + " AND " + DBConstants.KEY_BUSINESS_ACCOUNT_ID + " = " + onAccountId + " )";

            // Get Verified Users
            if (verifiedOnly)
                queryString += " AND " + DBConstants.KEY_VERIFICATION_STATUS + " = " + DBConstants.VERIFIED_USER;

            // Get iPay Users
            if (memberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER;

            // Get iPay Business Users
            if (businessMemberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER + " AND " + DBConstants.KEY_ACCOUNT_TYPE + " = " + DBConstants.BUSINESS_USER;

            // Get Non-iPay Users
            if (nonMemberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " != " + DBConstants.IPAY_MEMBER;

            if (invitees != null) {
                List<String> quotedInvitees = new ArrayList<>();
                for (String invitee : invitees) {
                    quotedInvitees.add("'" + invitee + "'");
                }

                String inviteeListStr = "(" + TextUtils.join(", ", quotedInvitees) + ")";

                if (invitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " IN " + inviteeListStr;
                }

                if (nonInvitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " NOT IN " + inviteeListStr;
                }
            }
            // Select only active contacts
            queryString += " AND " + DBConstants.KEY_IS_ACTIVE + " = " + DBConstants.ACTIVE;

            // If original name exists, then user original name as the sorting parameter.
            // Otherwise use normal name as the sorting parameter.
            queryString += " ORDER BY CASE "
                    + "WHEN (" + DBConstants.KEY_ORIGINAL_NAME + " IS NULL OR " + DBConstants.KEY_ORIGINAL_NAME + " = '')"
                    + " THEN " + DBConstants.KEY_NAME
                    + " ELSE "
                    + DBConstants.KEY_ORIGINAL_NAME + " END COLLATE NOCASE";

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public Cursor searchBusinessAccounts(String query) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_BUSINESS_ACCOUNTS
                    + " WHERE (" + DBConstants.KEY_BUSINESS_NAME + " LIKE '%" + query + "%'"
                    + ")" + " ORDER BY " + DBConstants.KEY_BUSINESS_NAME
                    + " COLLATE NOCASE";

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.getCount();
                Logger.logW("Query", cursor.getCount() + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public Cursor searchPersonalContacts(String query, boolean memberOnly, boolean personalUserOnly, boolean nonMemberOnly,
                                         boolean verifiedOnly, boolean invitedOnly, boolean nonInvitedOnly, List<String> invitees) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_CONTACTS
                    + " WHERE (" + DBConstants.KEY_NAME + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_MOBILE_NUMBER + " LIKE '%" + query + "%'"
                    + " OR " + DBConstants.KEY_ORIGINAL_NAME + " LIKE '%" + query + "%')";

            // Get Verified Users
            if (verifiedOnly)
                queryString += " AND " + DBConstants.KEY_VERIFICATION_STATUS + " = " + DBConstants.VERIFIED_USER;

            // Get iPay Users
            if (memberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER;

            // Get iPay Business Users
            if (personalUserOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " = " + DBConstants.IPAY_MEMBER + " AND " + DBConstants.KEY_ACCOUNT_TYPE + " = " + DBConstants.PERSONAL_USER;

            // Get Non-iPay Users
            if (nonMemberOnly)
                queryString += " AND " + DBConstants.KEY_IS_MEMBER + " != " + DBConstants.IPAY_MEMBER;

            if (invitees != null) {
                List<String> quotedInvitees = new ArrayList<>();
                for (String invitee : invitees) {
                    quotedInvitees.add("'" + invitee + "'");
                }

                String inviteeListStr = "(" + TextUtils.join(", ", quotedInvitees) + ")";

                if (invitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " IN " + inviteeListStr;
                }

                if (nonInvitedOnly) {
                    // Get invited users
                    queryString += " AND " + DBConstants.KEY_MOBILE_NUMBER + " NOT IN " + inviteeListStr;
                }
            }
            // Select only active contacts
            queryString += " AND " + DBConstants.KEY_IS_ACTIVE + " = " + DBConstants.ACTIVE;

            // If original name exists, then user original name as the sorting parameter.
            // Otherwise use normal name as the sorting parameter.
            queryString += " ORDER BY CASE "
                    + "WHEN (" + DBConstants.KEY_ORIGINAL_NAME + " IS NULL OR " + DBConstants.KEY_ORIGINAL_NAME + " = '')"
                    + " THEN " + DBConstants.KEY_NAME
                    + " ELSE "
                    + DBConstants.KEY_ORIGINAL_NAME + " END COLLATE NOCASE";

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.getCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }


    public Cursor searchBusinessAccountsByMobile(String query) {
        Cursor cursor = null;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT * FROM " + DBConstants.DB_TABLE_BUSINESS_ACCOUNTS
                    + " WHERE (" + DBConstants.KEY_BUSINESS_MOBILE_NUMBER + " LIKE '%" + query + "%'"
                    + ")" + " ORDER BY " + DBConstants.KEY_BUSINESS_NAME
                    + " COLLATE NOCASE";

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.getCount();
                Logger.logW("Query", cursor.getCount() + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public int getLastAddedBusinessId() {
        Cursor cursor;
        int columnIndexForMaxBusinessId = 0;

        try {
            SQLiteDatabase db = dOpenHelper.getReadableDatabase();

            String queryString = "SELECT MAX(" + DBConstants.KEY_BUSINESS_ACCOUNT_ID +
                    ") FROM " + DBConstants.DB_TABLE_BUSINESS_ACCOUNTS;

            Logger.logW("Query", queryString);

            cursor = db.rawQuery(queryString, null);

            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getInt(columnIndexForMaxBusinessId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}