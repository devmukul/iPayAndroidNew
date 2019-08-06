package bd.com.ipay.ipayskeleton.CustomView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.QRCodePaymentActivity;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DBConstants;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.BusinessContact;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.CustomBusinessContact;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.Outlets;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class MakePaymentContactsSearchView extends RelativeLayout implements SearchView.OnQueryTextListener{

    private SearchView mCustomAutoCompleteView;

    private RecyclerView mTransactionHistoryRecyclerView;
    private BusinessContactListAdapter mTransactionHistoryAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<CustomBusinessContact> userTransactionHistories;

    private List<BusinessContact> mBusinessContactList;
    private String mQuery = "";
    private String mImageURL = "";
    private String mName = "";
    private String mMobileNumber = "";
    private Long mOutletId = null;
    private String mAddress = "";
    private String mThanaDistrict = "";
    private String mOutlet = "";

    private CustomItemClickListener customItemClickListener;
    private CustomBillPaymentClickListener customBillPaymentClickListener;
    private static final String NUMERIC_PATTERN_WITH_SPACE = "^[0-9\\s\\+\\-!@#$%^&*(),.?\":{}|<>]*$";


    private  View mPayByQCView;
    private PinChecker pinChecker;

    private Context mContext;

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

    private void resetSearchKeyword() {
        if (mCustomAutoCompleteView != null && !mQuery.isEmpty()) {
            Logger.logD("Loader", "Resetting.. Previous query: " + mQuery);

            mQuery = "";
            mCustomAutoCompleteView.setQuery("", false);
        }
    }

    private void initView(Context context) {
        this.mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_payment_search, this, true);


        mPayByQCView = view.findViewById(R.id.qr_scan);
        mCustomAutoCompleteView = view.findViewById(R.id.search_business);
        mCustomAutoCompleteView.setIconified(false);
        mCustomAutoCompleteView.setOnQueryTextListener(this);
        mCustomAutoCompleteView.clearFocus();

        mTransactionHistoryRecyclerView = view.findViewById(R.id.address_recycler_view);
        mLayoutManager = new LinearLayoutManager(mContext);

        mCustomAutoCompleteView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    readBusinessContactsFromDB();
                    mTransactionHistoryRecyclerView.setVisibility(VISIBLE);
                }
                else {
                    if(mCustomAutoCompleteView.getQuery().toString().equals(""))
                        mTransactionHistoryRecyclerView.setVisibility(GONE);
                }
            }
        });

        mPayByQCView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinChecker = new PinChecker(mContext, new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent;
                        intent = new Intent(mContext, QRCodePaymentActivity.class);
                        mContext.startActivity(intent);
                        ((Activity)mContext).finish();
                    }
                });
                pinChecker.execute();
            }
        });

        resetSearchKeyword();
        mTransactionHistoryRecyclerView.setLayoutManager(mLayoutManager);
    }

    private List<CustomBusinessContact> getBusinessContactList(Cursor cursor) {
        List<CustomBusinessContact> mBusinessContacts;
        int businessNameIndex;
        int phoneNumberIndex;
        int profilePictureUrlIndex;
        int businessTypeIndex;
        int businessAddressIndex;
        int businessThanaIndex;
        int businessDistrictIndex;
        int businessOutletIndex;
        
        mBusinessContacts = new ArrayList<>();

        mBusinessContacts.clear();
        //Add Bill Provider
        mBusinessContacts.add(new CustomBusinessContact(Constants.AMBERIT, "Bill_Pay", mContext.getString(R.string.amberIT), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.BEXCOM, "Bill_Pay", mContext.getString(R.string.akash_dth), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.BLION, "Bill_Pay", mContext.getString(R.string.banglalion), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.BRILLIANT, "Bill_Pay", mContext.getString(R.string.brilliant), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.CARNIVAL, "Bill_Pay", mContext.getString(R.string.carnival), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.CREDIT_CARD, "Bill_Pay", mContext.getString(R.string.credit_card_title), mContext.getString(R.string.bill_pay), ""));

        mBusinessContacts.add(new CustomBusinessContact(Constants.CNS, "Bill_Pay", mContext.getString(R.string.cns), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.COLBD, "Bill_Pay", mContext.getString(R.string.colbd), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.DPDC, "Bill_Pay", mContext.getString(R.string.dpdc), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.DESCO, "Bill_Pay", mContext.getString(R.string.desco), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.LANKABANGLA, "Bill_Pay", mContext.getString(R.string.lanka_bangla_card), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.LANKABANGLA, "Bill_Pay", mContext.getString(R.string.lanka_bangla_dps), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.LINK3, "Bill_Pay", mContext.getString(R.string.link_three), mContext.getString(R.string.bill_pay), ""));
        mBusinessContacts.add(new CustomBusinessContact(Constants.RAILWAY_TICKET, "Bill_Pay", mContext.getString(R.string.railway_ticket), mContext.getString(R.string.buy_ticket), ""));


        if (cursor != null) {
            businessNameIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_NAME);
            phoneNumberIndex = cursor.getColumnIndex(DBConstants.KEY_MOBILE_NUMBER);
            profilePictureUrlIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_PROFILE_PICTURE);
            businessTypeIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_TYPE);
            businessAddressIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_ADDRESS);
            businessThanaIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_THANA);
            businessDistrictIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_DISTRICT);
            businessOutletIndex = cursor.getColumnIndex(DBConstants.KEY_BUSINESS_OUTLET);

            if (cursor.moveToFirst())
                do {
                    String businessName = cursor.getString(businessNameIndex);
                    String mobileNumber = cursor.getString(phoneNumberIndex);
                    String profilePictureUrl = cursor.getString(profilePictureUrlIndex);
                    int businessTypeID = cursor.getInt(businessTypeIndex);
                    String businessAddress = cursor.getString(businessAddressIndex);
                    String businessThana = cursor.getString(businessThanaIndex);
                    String businessDistrict = cursor.getString(businessDistrictIndex);
                    String businessOutlet = cursor.getString(businessOutletIndex);



                    if(!TextUtils.isEmpty(businessOutlet)) {
                        Outlets[] outlets = new Gson().fromJson(businessOutlet, Outlets[].class);
                        if(outlets.length>0) {
                            for (Outlets outlet : outlets) {
                                CustomBusinessContact businessContact = new CustomBusinessContact();
                                businessContact.setTypeInList("Outlet");
                                businessContact.setBusinessName(businessName);
                                businessContact.setMobileNumber(mobileNumber);

                                if (CommonData.getBusinessTypes() != null) {
                                    BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
                                    if (businessType != null)
                                        businessContact.setBusinessType(businessType.getName());
                                }
                                businessContact.setProfilePictureUrl(outlet.getOutletLogoUrl());
                                businessContact.setAddressString(outlet.getAddressString());
                                businessContact.setThanaString(outlet.getOutletAddress().getThanaName());
                                businessContact.setDistrictString(outlet.getOutletAddress().getDistrictName());
                                businessContact.setOutletName(outlet.getOutletName());
                                businessContact.setOutletId(outlet.getOutletId());
                                mBusinessContacts.add(businessContact);
                            }
                        }else {

                            CustomBusinessContact businessContact = new CustomBusinessContact();
                            businessContact.setTypeInList("Business");
                            businessContact.setBusinessName(businessName);
                            businessContact.setMobileNumber(mobileNumber);
                            businessContact.setProfilePictureUrl(profilePictureUrl);
                            businessContact.setAddressString(businessAddress);
                            businessContact.setThanaString(businessThana);
                            businessContact.setDistrictString(businessDistrict);

                            if (CommonData.getBusinessTypes() != null) {
                                BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
                                if (businessType != null)
                                    businessContact.setBusinessType(businessType.getName());
                            }

                            mBusinessContacts.add(businessContact);
                        }
                    }else {
                        CustomBusinessContact businessContact = new CustomBusinessContact();
                        businessContact.setTypeInList("Business");
                        businessContact.setBusinessName(businessName);
                        businessContact.setMobileNumber(mobileNumber);
                        businessContact.setProfilePictureUrl(profilePictureUrl);
                        businessContact.setAddressString(businessAddress);
                        businessContact.setThanaString(businessThana);
                        businessContact.setDistrictString(businessDistrict);

                        if (CommonData.getBusinessTypes() != null) {
                            BusinessType businessType = CommonData.getBusinessTypeById(businessTypeID);
                            if (businessType != null)
                                businessContact.setBusinessType(businessType.getName());
                        }

                        mBusinessContacts.add(businessContact);
                    }

                } while (cursor.moveToNext());
        }

        return mBusinessContacts;
    }

    private void readBusinessContactsFromDB() {
        Cursor mCursor;
        DataHelper dataHelper = DataHelper.getInstance(mContext);
        mCursor = dataHelper.searchBusinessAccounts(mQuery);

        try {
            if (mCursor != null) {
                userTransactionHistories = getBusinessContactList(mCursor);
                setBusinessContactAdapter(userTransactionHistories);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
    }

    private void setBusinessContactAdapter(List<CustomBusinessContact> businessContactList) {
        mTransactionHistoryAdapter = new BusinessContactListAdapter(mContext, businessContactList);
        mTransactionHistoryRecyclerView.setAdapter(mTransactionHistoryAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if(query.matches(NUMERIC_PATTERN_WITH_SPACE)){
            query = query.replaceAll("[^0-9.]", "");
        }
        if(mTransactionHistoryAdapter != null && query!=null && !TextUtils.isEmpty(query)){
            mTransactionHistoryAdapter.getFilter().filter(query);
            return true;
        }
        return false;
    }

    private class BusinessContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

        Context c;
        List<CustomBusinessContact> userTransactionHistories;
        List<CustomBusinessContact> mFilteredOutlets;

        public BusinessContactListAdapter(Context c, List<CustomBusinessContact> userTransactionHistories) {
            this.c = c;
            this.userTransactionHistories = userTransactionHistories;
            this.mFilteredOutlets = userTransactionHistories;
        }

        @Override
        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        mFilteredOutlets = userTransactionHistories;
                    } else {
                        List<CustomBusinessContact> filteredList = new ArrayList<>();

                        for (CustomBusinessContact outletsList : userTransactionHistories) {
                            if (!TextUtils.isEmpty(outletsList.getBusinessName()) && outletsList.getBusinessName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(outletsList);
                            }else if(!TextUtils.isEmpty(outletsList.getAddressString()) && outletsList.getAddressString().toLowerCase().contains(charString.toLowerCase())){
                                filteredList.add(outletsList);
                            }else if(!TextUtils.isEmpty(outletsList.getOutletName()) && outletsList.getOutletName().toLowerCase().contains(charString.toLowerCase())){
                                filteredList.add(outletsList);
                            }else if(!TextUtils.isEmpty(outletsList.getMobileNumber()) && outletsList.getMobileNumber().toLowerCase().contains(charString.toLowerCase())){
                                filteredList.add(outletsList);
                            }
                        }

                        mFilteredOutlets = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mFilteredOutlets;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    mFilteredOutlets = (List<CustomBusinessContact>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView businessNameView;
            private TextView outletNameView;
            private TextView businessTypeView;
            private ProfileImageView profilePictureView;
            private TextView businessAddressView;



            public ViewHolder(final View itemView) {
                super(itemView);
                businessNameView = itemView.findViewById(R.id.business_name);
                outletNameView = itemView.findViewById(R.id.outlet_name);
                businessTypeView = itemView.findViewById(R.id.business_type);
                profilePictureView = itemView.findViewById(R.id.profile_picture);
                businessAddressView = itemView.findViewById(R.id.business_address);
            }

            public void bindView(final int pos) {

                final CustomBusinessContact businessContact = mFilteredOutlets.get(pos);

                final String typeInList = businessContact.getTypeInList();
                final String businessName = businessContact.getBusinessName();
                final String mobileNumber = businessContact.getMobileNumber();
                final String businessType = businessContact.getBusinessType();
                final String profilePictureUrl = businessContact.getProfilePictureUrl();
                final String businessAddress = businessContact.getAddressString();
                final String businessOutlet = businessContact.getOutletName();
                final Long businessOutletId = businessContact.getOutletId();

                if (businessName != null && !businessName.isEmpty())
                    businessNameView.setText(businessName);

                if (typeInList.equals("Outlet") && businessOutlet != null && !businessOutlet.isEmpty()) {
                    outletNameView.setText(businessOutlet);
                    outletNameView.setVisibility(VISIBLE);
                }else {
                    outletNameView.setVisibility(GONE);
                }

                if (businessType != null) {
                    businessTypeView.setText(businessType);
                    businessTypeView.setVisibility(VISIBLE);
                }else {
                    businessTypeView.setVisibility(GONE);
                }

                if(typeInList.equals("Bill_Pay")){
                    businessAddressView.setText("");
                }else{
                    if (businessAddress != null && !businessAddress.isEmpty()) {
                        businessAddressView.setText(businessAddress);
                    }
                }

                if(!TextUtils.isEmpty(profilePictureUrl)){
                    profilePictureView.setBusinessProfilePicture(Constants.BASE_URL_FTP_SERVER + profilePictureUrl, false);
                }
                else {
                    if (businessName.equalsIgnoreCase(mContext.getString(R.string.amberIT)))
                        profilePictureView.setProfilePicture(R.drawable.ic_amber_it);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.akash_dth)))
                        profilePictureView.setProfilePicture(R.drawable.akash);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.banglalion)))
                        profilePictureView.setProfilePicture(R.drawable.banglalion);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.brilliant)))
                        profilePictureView.setProfilePicture(R.drawable.brilliant_logo);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.carnival)))
                        profilePictureView.setProfilePicture(R.drawable.ic_carnival);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.credit_card_title)))
                        profilePictureView.setProfilePicture(R.drawable.credit_card);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.cns)))
                        profilePictureView.setProfilePicture(R.drawable.cns);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.colbd)))
                        profilePictureView.setProfilePicture(R.drawable.colbd);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.desco)))
                        profilePictureView.setProfilePicture(R.drawable.desco);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.dpdc)))
                        profilePictureView.setProfilePicture(R.drawable.dpdc);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.lanka_bangla_card)))
                        profilePictureView.setProfilePicture(R.drawable.lbf_credit_card);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.lanka_bangla_dps)))
                        profilePictureView.setProfilePicture(R.drawable.lbf_credit_card_dps);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.link_three)))
                        profilePictureView.setProfilePicture(R.drawable.link_three_logo);
                    else if (businessName.equalsIgnoreCase(mContext.getString(R.string.railway_ticket)))
                        profilePictureView.setProfilePicture(R.drawable.bd_railway);
                    else {
                        profilePictureView.setBusinessLogoPlaceHolder();
                    }
                }


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(businessContact.getTypeInList().equals("Bill_Pay")){
                            customBillPaymentClickListener.onItemClick(businessName, businessContact.getBusinessId());
                        }else {
                            mName = businessName;
                            mImageURL = Constants.BASE_URL_FTP_SERVER + profilePictureUrl;
                            mAddress = businessAddress;
                            mOutlet = businessOutlet;
                            mMobileNumber = mobileNumber;
                            customItemClickListener.onItemClick(mName, mobileNumber, mImageURL, mAddress, businessOutletId);
                        }
                        resetSearchKeyword();
                        mCustomAutoCompleteView.setQuery(null, false);
                        mCustomAutoCompleteView.clearFocus();
                        Utilities.hideKeyboard(mContext, mCustomAutoCompleteView);
                    }
                });
            }
        }


        // Now define the view holder for Normal list item
        class NormalViewHolder extends BusinessContactListAdapter.ViewHolder {
            NormalViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do whatever you want on clicking the normal items


                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BusinessContactListAdapter.NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_business_search, parent, false));

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                BusinessContactListAdapter.NormalViewHolder vh = (BusinessContactListAdapter.NormalViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mFilteredOutlets == null)
                return 0;
            else
                return mFilteredOutlets.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }



    }

    public void setCustomItemClickListener(CustomItemClickListener customItemClickListener) {
        this.customItemClickListener = customItemClickListener;
    }

    public interface CustomItemClickListener {
        void onItemClick (String name, String mobileNumber, String imageURL, String address, Long outletId) ;
    }

    public void setCustomBillPaymentClickListener(CustomBillPaymentClickListener customBillPaymentClickListener) {
        this.customBillPaymentClickListener = customBillPaymentClickListener;
    }

    public interface CustomBillPaymentClickListener {
        void onItemClick (String name, String id) ;
    }



}

