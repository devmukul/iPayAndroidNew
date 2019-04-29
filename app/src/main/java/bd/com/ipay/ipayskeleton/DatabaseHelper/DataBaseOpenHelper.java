package bd.com.ipay.ipayskeleton.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseOpenHelper extends SQLiteOpenHelper {

    private final int newVersion;
    private final String name;

    public DataBaseOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.newVersion = version;
        this.name = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createContactsTable(db);
        createBusinessAccountsTable(db);
        createContactsForBusinessAccountTable(db);
        createSavedBillTable(db);
    }

    private void createContactsTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " +
                DBConstants.DB_TABLE_CONTACTS +
                "(_id integer primary key autoincrement, " +
                DBConstants.KEY_MOBILE_NUMBER + " text unique not null, " +
                DBConstants.KEY_NAME + " text, " +
                DBConstants.KEY_ORIGINAL_NAME + " text, " +
                DBConstants.KEY_ACCOUNT_TYPE + " integer default 1, " +
                DBConstants.KEY_PROFILE_PICTURE + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH + " text, " +
                DBConstants.KEY_RELATIONSHIP + " text, " +
                DBConstants.KEY_VERIFICATION_STATUS + " integer default 0, " +
                DBConstants.KEY_UPDATE_TIME + " long, " +
                DBConstants.KEY_IS_MEMBER + " integer default 0, " +
                DBConstants.KEY_IS_ACTIVE + " integer default " + DBConstants.ACTIVE + ")");
    }

    private void createContactsForBusinessAccountTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " +
                DBConstants.DB_TABLE_CONTACTS_BUSINESS +
                "(_id integer primary key autoincrement, " +
                DBConstants.KEY_MOBILE_NUMBER + " text unique not null, " +
                DBConstants.KEY_NAME + " text, " +
                DBConstants.KEY_ORIGINAL_NAME + " text, " +
                DBConstants.KEY_ACCOUNT_TYPE + " integer default 1, " +
                DBConstants.KEY_PROFILE_PICTURE + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH + " text, " +
                DBConstants.KEY_RELATIONSHIP + " text, " +
                DBConstants.KEY_VERIFICATION_STATUS + " integer default 0, " +
                DBConstants.KEY_UPDATE_TIME + " long, " +
                DBConstants.KEY_IS_MEMBER + " integer default 0, " +
                DBConstants.KEY_BUSINESS_ACCOUNT_ID + " long, " +
                DBConstants.KEY_IS_ACTIVE + " integer default " + DBConstants.ACTIVE + ")");
    }

    private void createBusinessAccountsTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " +
                DBConstants.DB_TABLE_BUSINESS_ACCOUNTS +
                "(_id integer primary key autoincrement, " +
                DBConstants.KEY_BUSINESS_MOBILE_NUMBER + " text unique not null, " +
                DBConstants.KEY_BUSINESS_NAME + " text, " +
                DBConstants.KEY_COMPANY_NAME + " text, " +
                DBConstants.KEY_BUSINESS_ADDRESS + " text, " +
                DBConstants.KEY_BUSINESS_THANA + " text, " +
                DBConstants.KEY_BUSINESS_DISTRICT + " text, " +
                DBConstants.KEY_BUSINESS_OUTLET + " text, " +
                DBConstants.BUSINESS_EMAIL + " text, " +
                DBConstants.KEY_BUSINESS_TYPE + " integer default 0, " +
                DBConstants.KEY_PROFILE_PICTURE + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_MEDIUM + " text, " +
                DBConstants.KEY_PROFILE_PICTURE_QUALITY_HIGH + " text, " +
                DBConstants.KEY_BUSINESS_ACCOUNT_ID + " integer default 0)");
    }

    private void createSavedBillTable(SQLiteDatabase db) {
        db.execSQL("create table if not exists " +
                DBConstants.DB_TABLE_SAVED_BILL +
                "(_id integer primary key autoincrement, " +
                DBConstants.KEY_BILL_PROVIDER_CODE + " text, " +
                DBConstants.KEY_BILL_PROVIDER_NAME + " text, " +
                DBConstants.KEY_BILL_IS_SAVED + " integer default 0, " +
                DBConstants.KEY_BILL_IS_SCHEDULED + " integer default 0, " +
                DBConstants.KEY_BILL_DATE + " integer default 0, " +
                DBConstants.KEY_BILL_LAST_PAID_DATE + " text, " +
                DBConstants.KEY_BILL_PAID_FOR_OTHERS + "  integer default 0, " +
                DBConstants.KEY_BILL_PARAMS_ID + " text, " +
                DBConstants.KEY_BILL_PARAMS_LABEL + " text, " +
                DBConstants.KEY_BILL_AMOUNT_TYPE + " text, " +
                DBConstants.KEY_BILL_LOCATION_CODE + " text, " +
                DBConstants.KEY_BILL_PARAMS_VALUE + " text unique COLLATE NOCASE not null, " +
                DBConstants.KEY_BILL_AMOUNT + " integer  default 0, " +
                DBConstants.KEY_BILL_META_DATA + " text)");
    }

    private void dropTable(SQLiteDatabase db, String tableName) {
        db.execSQL("drop table if exists " + tableName);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // The last case will contain the break statement only. As the migration will take place one by one.
        // Here's a nice explanation - http://stackoverflow.com/a/26916986/3145960
        switch (oldVersion) {
            case 9:
                createBusinessAccountsTable(db);
            case 10:
                dropTable(db, DBConstants.DB_TABLE_CONTACTS);
                createContactsTable(db);
            case 14:
                dropTable(db, DBConstants.DB_TABLE_BUSINESS_ACCOUNTS);
                createBusinessAccountsTable(db);
            case 15:
                createSavedBillTable(db);
                break;
        }
    }
}