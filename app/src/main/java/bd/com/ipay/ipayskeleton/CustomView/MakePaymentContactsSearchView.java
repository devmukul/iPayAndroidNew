package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.BusinessContact;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class MakePaymentContactsSearchView extends FrameLayout {

    private SearchView mCustomAutoCompleteView;
//    private BusinessContactListAdapter mBusinessContactsAdapter;

    private List<BusinessContact> mBusinessContactList;
    private String mQuery = "";
    private String mImageURL = "";
    private String mName = "";
    private String mAddress = "";
    private String mThanaDistrict = "";
    private String mOutlet = "";

    private Context mContext;
//    private CustomFocusListener mCustomFocusListener;
//
//    private CustomTextChangeListener customTextChangeListener;

    public MakePaymentContactsSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public MakePaymentContactsSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MakePaymentContactsSearchView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_payment_search, this, true);

        mCustomAutoCompleteView = (SearchView) view.findViewById(R.id.search_business);

//        mCustomAutoCompleteView.addTextChangedListener(new CustomAutoCompleteTextChangedListener());
//
//        mCustomAutoCompleteView.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (mCustomFocusListener != null)
//                    mCustomFocusListener.onFocusChange(v, hasFocus);
//                if (!hasFocus) {
//                    String inputString = mCustomAutoCompleteView.getText().toString().trim();
//
//                    if (mName.isEmpty() && mImageURL.isEmpty())
//                        customTextChangeListener.onTextChange(inputString);
//                    else
//                        customTextChangeListener.onTextChange(inputString, mName, mImageURL, mAddress, mThanaDistrict, mOutlet);
//                }
//            }
//        });
//
//        mBusinessContactList = new ArrayList<>();
//        setBusinessContactAdapter(mBusinessContactList);
    }

//    public void setOnCustomFocusChangeListener(CustomFocusListener mCustomFocusListener) {
//        this.mCustomFocusListener = mCustomFocusListener;
//    }
//
//    public class CustomAutoCompleteTextChangedListener implements TextWatcher {
//
//        CustomAutoCompleteTextChangedListener() {
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence userInput, int start, int before, int count) {
//            mQuery = userInput.toString();
//
//            if (!mQuery.matches("[0-9+]+") && userInput.length() > 2) {
//                try {
//                    // Query the database based on the user input
//                    readBusinessContactsFromDB();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                mBusinessContactList.clear();
//                setBusinessContactAdapter(mBusinessContactList);
//            }
//        }
//    }
//
//    public void setCustomTextChangeListener(CustomTextChangeListener customTextChangeListener) {
//        this.customTextChangeListener = customTextChangeListener;
//    }
//
//    public interface CustomTextChangeListener {
//        void onTextChange(String inputText);
//
//        void onTextChange(String inputText, String name, String imageURL, String address, String thanaDistrict, String outlet);
//    }
//
//    public void clearSelectedData() {
//        mName = "";
//        mImageURL = "";
//        mAddress = "";
//        mThanaDistrict = "";
//        mOutlet = "";
//    }
//
//    public Editable getText() {
//        return mCustomAutoCompleteView.getText();
//    }
//
//
//    public void setError(String error) {
//        mCustomAutoCompleteView.setError(error);
//    }
//
//    public void setText(String text) {
//        mCustomAutoCompleteView.setText(text);
//        mCustomAutoCompleteView.setSelection(text.length());
//        mCustomAutoCompleteView.setError(null);
//
//        hideSuggestionList();
//    }
//
//    public void hideSuggestionList() {
//        mCustomAutoCompleteView.dismissDropDown();
//    }
//
//    private List<BusinessContact> getBusinessContactList(Cursor cursor) {
//        List<BusinessContact> mBusinessContacts;
//        int businessNameIndex;
//        int phoneNumberIndex;
//        int profilePictureUrlIndex;
//        int businessTypeIndex;
//        int businessAddressIndex;
//        int businessThanaIndex;
//        int businessDistrictIndex;
//        int businessOutletIndex;
//
//
//        mBusinessContacts = new ArrayList<>();
//
//        if (cursor != null) {
//            mBusinessContacts.clear();
//            businessNameIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_NAME);
//            phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
//            profilePictureUrlIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_PROFILE_PICTURE);
//            businessTypeIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_TYPE);
//            businessAddressIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_ADDRESS);
//            businessThanaIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_THANA);
//            businessDistrictIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_DISTRICT);
//            businessOutletIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_OUTLET);
//
//            if (cursor.moveToFirst())
//                do {
//                    String businessName = cursor.getString(businessNameIndex);
//                    String mobileNumber = cursor.getString(phoneNumberIndex);
//                    String profilePictureUrl = cursor.getString(profilePictureUrlIndex);
//                    int businessTypeID = cursor.getInt(businessTypeIndex);
//                    String businessAddress = cursor.getString(businessAddressIndex);
//                    String businessThana = cursor.getString(businessThanaIndex);
//                    String businessDistrict = cursor.getString(businessDistrictIndex);
//                    String businessOutlet = cursor.getString(businessOutletIndex);
//
//                    BusinessContact businessContact = new BusinessContact();
//                    businessContact.setBusinessName(businessName);
//                    businessContact.setMobileNumber(mobileNumber);
//                    businessContact.setProfilePictureUrl(profilePictureUrl);
//                    businessContact.setAddressString(businessAddress);
//                    businessContact.setThanaString(businessThana);
//                    businessContact.setDistrictString(businessDistrict);
//                    businessContact.setOutletString(businessOutlet);
//
//                    if (CommonData.getBusinessTypes() != null) {
//                        BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
//                        if (businessType != null)
//                            businessContact.setBusinessType(businessType.getName());
//                    }
//
//                    mBusinessContacts.add(businessContact);
//
//                } while (cursor.moveToNext());
//        }
//
//        return mBusinessContacts;
//    }
//
//    private void readBusinessContactsFromDB() {
//        Cursor mCursor;
//        DataHelper dataHelper = DataHelper.getInstance(mContext);
//        mCursor = dataHelper.searchBusinessAccounts(mQuery);
//
//        try {
//            if (mCursor != null) {
//                mBusinessContactList = getBusinessContactList(mCursor);
//                setBusinessContactAdapter(mBusinessContactList);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (mCursor != null) {
//                mCursor.close();
//            }
//        }
//    }
//
//    private void setBusinessContactAdapter(List<BusinessContact> businessContactList) {
//        mBusinessContactsAdapter = new BusinessContactListAdapter(mContext, businessContactList);
//        mCustomAutoCompleteView.setAdapter(mBusinessContactsAdapter);
//    }
//
//    public class BusinessContactListAdapter extends ArrayAdapter<BusinessContact> {
//        private LayoutInflater inflater;
//
//        private TextView businessNameView;
//        private TextView businessTypeView;
//        private ProfileImageView profilePictureView;
//        private TextView businessAddressView;
//
//        BusinessContactListAdapter(Context context, List<BusinessContact> objects) {
//            super(context, 0, objects);
//            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        }
//
//        @Override
//        @NonNull
//        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//            View view = convertView;
//
//            if (view == null)
//                view = inflater.inflate(R.layout.list_item_business_contact, parent, false);
//
//            businessNameView = (TextView) view.findViewById(R.id.business_name);
//            businessTypeView = (TextView) view.findViewById(R.id.business_type);
//            profilePictureView = (ProfileImageView) view.findViewById(R.id.profile_picture);
//            businessAddressView = (TextView) view.findViewById(R.id.business_address);
//
//            return bindView(view, position);
//        }
//
//        public View bindView(View view, int position) {
//            BusinessContact businessContact = getItem(position);
//
//            if (businessContact == null) {
//                return view;
//            }
//
//            final String businessName = businessContact.getBusinessName();
//            final String mobileNumber = businessContact.getMobileNumber();
//            final String businessType = businessContact.getBusinessType();
//            final String profilePictureUrl = businessContact.getProfilePictureUrl();
//            final String businessAddress = businessContact.getAddressString();
//            final String businessThana = businessContact.getThanaString();
//            final String businessDistrict = businessContact.getDistrictString();
//            final String businessOutlet = businessContact.getOutletString();
//
//            if (businessName != null && !businessName.isEmpty())
//                businessNameView.setText(businessName);
//
//            if (businessType != null) {
//                businessTypeView.setText(businessType);
//                businessTypeView.setVisibility(VISIBLE);
//            }
//            if (businessAddress != null && !businessAddress.isEmpty()) {
//                businessAddressView.setText(businessAddress);
//            }
//            profilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + profilePictureUrl, false);
//
//            view.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    setText(mobileNumber);
//
//                    mName = businessName;
//                    mImageURL = profilePictureUrl;
//                    mAddress = businessAddress;
//                    mThanaDistrict = businessThana + ", " + businessDistrict;
//                    mOutlet = businessOutlet;
//                    mCustomAutoCompleteView.clearFocus();
//                    Utilities.hideKeyboard(mContext, mCustomAutoCompleteView);
//                }
//            });
//
//            return view;
//        }
//    }
//
//    public interface CustomFocusListener {
//        void onFocusChange(View v, boolean hasFocus);
//    }
}

