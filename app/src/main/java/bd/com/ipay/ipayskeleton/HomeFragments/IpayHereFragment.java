package bd.com.ipay.ipayskeleton.HomeFragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Fragments.IPaySupportPlaceAutocompleteFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.V2.IPayHereRequestUrlBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.V2.IPayHereResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.V2.NearbyBusinessResponseList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

import static bd.com.ipay.ipayskeleton.Utilities.Utilities.LOCATION_SETTINGS_PERMISSION_CODE;

public class IpayHereFragment extends ProgressFragment implements PlaceSelectionListener,
        HttpResponseListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_LOCATION = 1;
    private static final long UPDATE_INTERVAL = 5000; // = 5 seconds
    private static final long FASTEST_INTERVAL = 5000; // = 5 seconds
    private final static int REQUEST_CHECK_SETTINGS = 2000;

    private List<NearbyBusinessResponseList> mNearByBusinessResponse;
    private HttpRequestGetAsyncTask mIPayHereTask = null;
    private BusinessContactListAdapter mTransactionHistoryAdapter;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location location;

    private boolean isLoading = false;
    private boolean clearListAfterLoading;
    private static final String mDefaultLatitude = "23.781381";
    private static final String mDefaultLongitude = "90.4121439";
    private String mUserLocationLatitude = null;
    private String mUserLocationLongitude = null;

    private RecyclerView mTransactionHistoryRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CustomProgressDialog mProgressDialog;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyListTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mUserLocationLatitude = null;
        mUserLocationLongitude = null;

        googleApiClient = new GoogleApiClient
                .Builder(getContext(), this, this)
                .addApi(LocationServices.API).build();

        if(!ifPermossionGranted()){
            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }else {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void startLocationUpdates() {
        clearListAfterLoading = true;
        if(!ifPermossionGranted()){
            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            locationUpdate();
        }
    }

    private boolean ifPermossionGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Utilities.isNecessaryPermissionExists(getContext(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return true;
    }


    private void locationUpdate(){
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                            fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
                            return;
                        }
                        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                        if (location != null) {
                            mUserLocationLatitude = String.valueOf(location.getLatitude());
                            mUserLocationLongitude = String.valueOf(location.getLongitude());
                            fetchNearByBusiness(mUserLocationLatitude, mUserLocationLongitude);
                        }else{
                            fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        buildAlertMessageNoGps();
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
                        break;
                }
            }
        });
    }

    protected void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Location Service Disabled")
                .setMessage("iPay needs to access your location to show iPay enabled outlets near you.")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SETTINGS_PERMISSION_CODE);
                    }
                })
                .setNegativeButton("Continue Anyway", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS_PERMISSION_CODE) {
            if(!googleApiClient.isConnected())
                googleApiClient.connect();
            else
                startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
                        } else {
                            if(!googleApiClient.isConnected())
                                googleApiClient.connect();
                            else
                                startLocationUpdates();
                        }
                    }
                }
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_ipay_here, container, false);
        if (getActivity() != null)
            getActivity().setTitle(R.string.ipay_here);

        IPaySupportPlaceAutocompleteFragment autocompleteFragment = new IPaySupportPlaceAutocompleteFragment();
        android.support.v4.app.FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, autocompleteFragment);
        ft.commit();

        autocompleteFragment.setOnSearchClearListener(new IPaySupportPlaceAutocompleteFragment.OnSearchClearListener() {
            @Override
            public void onClear() {
                mProgressDialog.show();
                clearListAfterLoading = true;
                if(!googleApiClient.isConnected())
                    googleApiClient.connect();
                else
                    startLocationUpdates();
            }
        });


        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry("BD")
                .build();
        // Register a listener to receive callbacks when a place has been selected or an error ha occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setFilter(autocompleteFilter);

        mProgressDialog = new CustomProgressDialog(getContext());
        mProgressDialog.setCancelable(false);

        mTransactionHistoryRecyclerView = v.findViewById(R.id.address_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mTransactionHistoryRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout);
        mEmptyListTextView = v.findViewById(R.id.empty_list_text);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity()) && mIPayHereTask == null) {
                    refreshTransactionHistory();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        setupRecyclerView();

        return v;
    }

    private void refreshTransactionHistory() {
        clearListAfterLoading = true;
        if (TextUtils.isEmpty(mUserLocationLatitude) || TextUtils.isEmpty(mUserLocationLongitude)){
            fetchNearByBusiness(mDefaultLatitude, mDefaultLongitude);
        }else{
            fetchNearByBusiness(mUserLocationLatitude, mUserLocationLongitude);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext())) {
            mIPayHereTask = null;
            return;
        }
        Gson gson = new Gson();

        switch (result.getApiCommand()) {

            case Constants.COMMAND_GET_NEREBY_BUSSINESS:

                try {
                    IPayHereResponse iPayHereResponse = gson.fromJson(result.getJsonString(), IPayHereResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        loadTransactionHistory(iPayHereResponse.getNearbyBusinessResponseList());
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), iPayHereResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "Could not fetch data..", Toast.LENGTH_LONG).show();
                }

                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                mSwipeRefreshLayout.setRefreshing(false);
                mIPayHereTask = null;
                if (this.isAdded()) setContentShown(true);
        }

    }

    @Override
    public void onPlaceSelected(Place place) {
        LatLng attributions = place.getLatLng();
        if (attributions != null) {
            mProgressDialog.show();
            String mLatitude = String.valueOf(attributions.latitude);
            String mLongitude = String.valueOf(attributions.longitude);
            clearListAfterLoading = true;
            fetchNearByBusiness(mLatitude, mLongitude);

        }
    }

    @Override
    public void onError(Status status) {

    }

    private void fetchNearByBusiness(String lattitude, String longitude) {
        if (mIPayHereTask != null)
            return;

        String url = IPayHereRequestUrlBuilder.generateUri(lattitude, longitude);
        mIPayHereTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_NEREBY_BUSSINESS,
                url, getContext(), false);
        mIPayHereTask.mHttpResponseListener = this;
        mIPayHereTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void setupRecyclerView() {
        mTransactionHistoryAdapter = new BusinessContactListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTransactionHistoryRecyclerView.setLayoutManager(mLayoutManager);
        mTransactionHistoryRecyclerView.setAdapter(mTransactionHistoryAdapter);
    }

    private void loadTransactionHistory(List<NearbyBusinessResponseList> transactionHistories) {
        if (clearListAfterLoading || mNearByBusinessResponse == null || mNearByBusinessResponse.size() == 0) {
            mNearByBusinessResponse = transactionHistories;
            clearListAfterLoading = false;
        } else {
            List<NearbyBusinessResponseList> tempTransactionHistories;
            tempTransactionHistories = transactionHistories;
            mNearByBusinessResponse.addAll(tempTransactionHistories);
        }
        if (mNearByBusinessResponse != null && mNearByBusinessResponse.size() > 0)
            mEmptyListTextView.setVisibility(View.GONE);
        else
            mEmptyListTextView.setVisibility(View.VISIBLE);

        if (isLoading)
            isLoading = false;
        mTransactionHistoryAdapter.notifyDataSetChanged();
        setContentShown(true);
    }

    private class BusinessContactListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView businessNameView;
            private TextView outletNameView;
            private TextView distanceView;
            private ProfileImageView profilePictureView;
            private TextView businessAddressView;
            private View directionView;

            public ViewHolder(final View itemView) {
                super(itemView);
                businessNameView = itemView.findViewById(R.id.business_name);
                outletNameView = itemView.findViewById(R.id.outlet_name);
                distanceView = itemView.findViewById(R.id.distance);
                profilePictureView = itemView.findViewById(R.id.profile_picture);
                businessAddressView = itemView.findViewById(R.id.business_address);
                directionView = itemView.findViewById(R.id.direction);
            }

            public void bindView(int pos) {
                final NearbyBusinessResponseList businessContact = mNearByBusinessResponse.get(pos);
                final String businessName = businessContact.getBusinessName();
                final String mobileNumber = businessContact.getMobileNumber();
                final String profilePictureUrl = businessContact.getImageUrl();
                final String businessAddress = businessContact.getAddressString();
                final String businessOutlet = businessContact.getOutletName();
                final Long businessOutletId = businessContact.getOutletId();
                final double lat = businessContact.getCoordinate().getLatitude();
                final double lon = businessContact.getCoordinate().getLongitude();

                if (businessName != null && !businessName.isEmpty())
                    businessNameView.setText(businessName);

                if (businessOutlet != null && !businessOutlet.isEmpty()) {
                    outletNameView.setText(businessOutlet);
                    outletNameView.setVisibility(View.VISIBLE);
                } else {
                    outletNameView.setVisibility(View.GONE);
                }

                if (businessAddress != null && !businessAddress.isEmpty()) {
                    businessAddressView.setText(businessAddress);
                    businessAddressView.setVisibility(View.VISIBLE);
                } else {
                    businessAddressView.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(mUserLocationLatitude) || TextUtils.isEmpty(mUserLocationLongitude)){
                    distanceView.setText("Distance : N/A");
                }else{

                    float[] result = new float[1];
                    Location.distanceBetween(Double.parseDouble(mUserLocationLatitude), Double.parseDouble(mUserLocationLongitude), lat, lon, result);
                    distanceView.setText("Distance : " + distanceText(result[0]));
                }

                profilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + profilePictureUrl, false);

                directionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lon);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!mSwipeRefreshLayout.isRefreshing()) {
                            Intent intent = new Intent(getActivity(), IPayTransactionActionActivity.class);
                            intent.putExtra(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_MAKE_PAYMENT);
                            intent.putExtra(Constants.MOBILE_NUMBER, mobileNumber);
                            intent.putExtra(Constants.FROM_QR_SCAN, true);
                            intent.putExtra(Constants.NAME, businessName);
                            intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + profilePictureUrl);
                            intent.putExtra(Constants.ADDRESS, businessAddress);
                            if (businessOutletId != null) {
                                intent.putExtra(Constants.OUTLET_ID, businessOutletId.longValue());
                            }
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        public String distanceText(float distance) {
            String distanceString;
            if (distance < 1000) {
                if (distance < 1) {
                    distanceString = String.format(Locale.getDefault(), "%dm", 1);
                }
                else {
                    distanceString = String.format(Locale.getDefault(), "%dm", Math.round(distance));
                }
            }
            else if (distance > 10000) {
                distanceString = String.format(Locale.getDefault(), "%dkm", Math.round(distance / 1000));
            }
            else {
                distanceString = String.format(Locale.getDefault(), "%.2fkm", distance / 1000);
            }

            return distanceString;
        }

        class NormalViewHolder extends BusinessContactListAdapter.ViewHolder {
            NormalViewHolder(final View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ipay_here, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                NormalViewHolder vh = (NormalViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mNearByBusinessResponse != null && !mNearByBusinessResponse.isEmpty())
                return mNearByBusinessResponse.size();
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

    }

}