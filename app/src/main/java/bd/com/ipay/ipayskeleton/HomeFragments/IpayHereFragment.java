package bd.com.ipay.ipayskeleton.HomeFragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.Coordinate;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.IPayHereRequestUrlBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.IPayHereResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.NearbyBusinessResponseList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import de.hdodenhof.circleimageview.CircleImageView;

public class IpayHereFragment extends BaseFragment implements PlaceSelectionListener, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, HttpResponseListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener {

    private static final int REQUEST_LOCATION = 1;
    public static final int LOCATION_SETTINGS_PERMISSION_CODE = 9876;

    private List<NearbyBusinessResponseList> mNearByBusinessResponse;
    private HttpRequestGetAsyncTask mIPayHereTask = null;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;

    private IPayHereResponse mIPayHereResponse;
    private LocationManager locationManager;
    private String mLatitude = "23.780879";
    private String mLongitude = "90.400956";
    private boolean isStartedMoving = false;
    private CardView searchLocationView;
    private Button searchLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_ipay_here, container, false);
        if (getActivity() != null)
            getActivity().setTitle(R.string.ipay_here);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry("BD")
                .build();
        // Register a listener to receive callbacks when a place has been selected or an error ha occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setFilter(autocompleteFilter);
        searchLocationView = (CardView) v.findViewById(R.id.search_this_place);
        searchLocation = (Button) v.findViewById(R.id.seach_this_place_btn);

        getLocationPermission();

        searchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng initialLoc = mMap.getCameraPosition().target;
                searchLocationView.setVisibility(View.INVISIBLE);

                if (mMap != null && initialLoc != null) {
                    mMap.clear();
                    isStartedMoving = false;
                    mLatitude = String.valueOf(initialLoc.latitude);
                    mLongitude = String.valueOf(initialLoc.longitude);
                    startDemo();
                    fetchNearByBusiness(mLatitude, mLongitude);
                }

            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
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
                    mNearByBusinessResponse = new ArrayList<>();
                    mIPayHereResponse = gson.fromJson(result.getJsonString(), IPayHereResponse.class);
                    mNearByBusinessResponse = mIPayHereResponse.getNearbyBusinessResponseList();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        readItems();
                    } else {
                        Toast.makeText(getContext(), mIPayHereResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), mIPayHereResponse.getMessage(), Toast.LENGTH_LONG).show();
                }

                mIPayHereTask = null;
                break;
        }

    }

    @Override
    public void onCameraIdle() {
        if (isStartedMoving)
            searchLocationView.setVisibility(View.VISIBLE);
        else
            searchLocationView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            isStartedMoving = true;
        }
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(getContext(), "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            getLocationWithoutPermision();
                        } else {
                            getLocationPermission();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_SETTINGS_PERMISSION_CODE) {
            getLocationPermission();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startDemo();
    }

    @Override
    public void onPlaceSelected(Place place) {
        LatLng attributions = place.getLatLng();
        if (attributions != null) {
            mMap.clear();
            this.mLatitude = String.valueOf(attributions.latitude);
            this.mLongitude = String.valueOf(attributions.longitude);
            startDemo();
            fetchNearByBusiness(this.mLatitude, this.mLongitude);

        }
    }

    private void getLocationsettings() {

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            getLocation();
        }
    }

    private void getLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Utilities.isNecessaryPermissionExists(getContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            } else {
                getLocationsettings();
            }
        } else {
            getLocationsettings();
        }
    }

    private void getLocation() {
        Location location;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mLatitude = String.valueOf(latitude);
            mLongitude = String.valueOf(longitude);
            setUpMap();
            fetchNearByBusiness(this.mLatitude, this.mLongitude);
        } else {
            setUpMap();
            fetchNearByBusiness(this.mLatitude, this.mLongitude);
        }
    }

    private void getLocationWithoutPermision() {
        setUpMap();
        fetchNearByBusiness(this.mLatitude, this.mLongitude);
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

    private void setUpMap() {
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void readItems() {
        for (int i = 0; i < mNearByBusinessResponse.size(); i++) {

            NearbyBusinessResponseList iPayHereResponse = mNearByBusinessResponse.get(i);
            Coordinate cc = mNearByBusinessResponse.get(i).getCoordinate();
            Marker mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cc.getLatitude(), cc.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ipay_here_marker))
                    .title(mNearByBusinessResponse.get(i).getBusinessName()));

            mMarker.setTag(iPayHereResponse);
        }
    }

    void startDemo() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(mLatitude), Double.valueOf(mLongitude)), 15f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(false);
        } else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
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
                        getLocationWithoutPermision();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;
        private Marker marker;
        boolean not_first_time_showing_info_window = false;
        private CircleImageView businessProfileImageView;
        private TextView businessNameTextView;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.ipay_here_info_window_map,
                    null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            if (this.marker != null
                    && this.marker.isInfoWindowShown()) {
                this.marker.hideInfoWindow();
                this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            this.marker = marker;
            NearbyBusinessResponseList infoWindowData = (NearbyBusinessResponseList) marker.getTag();
            businessProfileImageView = (CircleImageView) view.findViewById(R.id.profile_picture);
            businessNameTextView = (TextView) view.findViewById(R.id.textview_name);
            String title = infoWindowData.getBusinessName();
            businessNameTextView.setText(title);
            if (infoWindowData.getImageUrl() != null) {
                String imageUrl = Constants.BASE_URL_FTP_SERVER + infoWindowData.getImageUrl();
                if (not_first_time_showing_info_window) {
                    not_first_time_showing_info_window = false;
                    Glide.with(getActivity())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_business_logo_round)
                            .error(R.drawable.ic_business_logo_round)
                            .into(businessProfileImageView);
                } else {
                    not_first_time_showing_info_window = true;
                    Glide.with(getActivity()).load(imageUrl)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    marker.showInfoWindow();
                                    return false;
                                }
                            }).crossFade().placeholder(R.drawable.ic_business_logo_round)
                            .error(R.drawable.ic_business_logo_round).into(businessProfileImageView);

                }
            }
            return view;
        }
    }


}
