package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.GetTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.RemoveTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.TrustedDevice;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TrustedDeviceFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetTrustedDeviceTask = null;
    private GetTrustedDeviceResponse mGetTrustedDeviceResponse = null;

    private HttpRequestDeleteAsyncTask mRemoveTrustedDeviceTask = null;
    private RemoveTrustedDeviceResponse mRemoveTrustedDeviceResponse = null;

    private CustomSwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<TrustedDevice> mTrustedDeviceList;
    private TrustedDeviceAdapter mTrustedDeviceAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mTrustedDevicesRecyclerView;

    private CustomProgressDialog mProgressDialog;
    private Tracker mTracker;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_trusted_devices));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trusted_devices, container, false);
        setTitle();

        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mTrustedDevicesRecyclerView = (RecyclerView) view.findViewById(R.id.list_trusted_devices);
        mProgressDialog = new CustomProgressDialog(getActivity());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentShown(false);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity()))
                    getTrustedDeviceList();
            }
        });
        getTrustedDeviceList();
    }

    private void showTrustedDeviceRemoveConfirmationDialog(final long id) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog
                .setMessage(getString(R.string.confirmation_remove_trusted_device))
                .setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTrustedDevice(id);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void getTrustedDeviceList() {
        if (mGetTrustedDeviceTask != null) {
            return;
        }

        mGetTrustedDeviceTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRUSTED_DEVICES,
                Constants.BASE_URL_MM + Constants.URL_GET_TRUSTED_DEVICES, getActivity(), this,false);
        mGetTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void removeTrustedDevice(long id) {
        if (mRemoveTrustedDeviceTask != null)
            return;
        mProgressDialog.show();

        mRemoveTrustedDeviceTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_REMOVE_TRUSTED_DEVICE,
                Constants.BASE_URL_MM + Constants.URL_REMOVE_TRUSTED_DEVICE + id, getActivity(), this,false);
        mRemoveTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setTitle() {
        getActivity().setTitle(R.string.browsers_and_apps);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mGetTrustedDeviceTask = null;
            mRemoveTrustedDeviceTask = null;
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_TRUSTED_DEVICES)) {

            try {
                mGetTrustedDeviceResponse = gson.fromJson(result.getJsonString(), GetTrustedDeviceResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    processTrustedDeviceList(result.getJsonString());
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mGetTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), mGetTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mProgressDialog.dismiss();
            mGetTrustedDeviceTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_REMOVE_TRUSTED_DEVICE)) {

            try {
                mRemoveTrustedDeviceResponse = gson.fromJson(result.getJsonString(), RemoveTrustedDeviceResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.success_device_removed, Toast.LENGTH_LONG).show();
                    }
                    mProgressDialog.show();

                    getTrustedDeviceList();
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mRemoveTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), mRemoveTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            mProgressDialog.cancel();
            mRemoveTrustedDeviceTask = null;
        }

    }

    private void processTrustedDeviceList(String json) {
        Gson gson = new Gson();
        mGetTrustedDeviceResponse = gson.fromJson(json, GetTrustedDeviceResponse.class);
        mTrustedDeviceList = (ArrayList<TrustedDevice>) mGetTrustedDeviceResponse.getDevices();

        mTrustedDeviceAdapter = new TrustedDeviceAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTrustedDevicesRecyclerView.setLayoutManager(mLayoutManager);
        mTrustedDevicesRecyclerView.setAdapter(mTrustedDeviceAdapter);

        setContentShown(true);
    }

    public class TrustedDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public TrustedDeviceAdapter() {
        }

        public class TrustedDeviceViewHolder extends RecyclerView.ViewHolder {
            private final ImageView mDeviceImageView;
            private final TextView mDeviceNameView;
            private final TextView mGrantTimeView;
            private final TextView mThisDeviceView;

            private List<String> mTrustedDeviceActionList;
            private int ACTION_REMOVE = 0;
            private CustomSelectorDialog mCustomSelectorDialog;


            public TrustedDeviceViewHolder(final View itemView) {
                super(itemView);

                mDeviceImageView = (ImageView) itemView.findViewById(R.id.trusted_device_imageView);
                mDeviceNameView = (TextView) itemView.findViewById(R.id.textview_device_name);
                mGrantTimeView = (TextView) itemView.findViewById(R.id.textview_time);
                mThisDeviceView = (TextView) itemView.findViewById(R.id.textview_this_device);
            }

            public void bindView(int pos) {

                final TrustedDevice trustedDevice = mTrustedDeviceList.get(pos);

                //Setting the correct image based on trusted device type
                int[] images = {
                        R.drawable.ic_browser3x,
                        R.drawable.ic_android3x,
                };

                final String deviceID = trustedDevice.getDeviceId();
                String Android = getString(R.string.android);
                String IOS = getString(R.string.ios);
                String Computer = getString(R.string.browser);

                if (deviceID.toLowerCase().contains(Android.toLowerCase()))
                    mDeviceImageView.setImageResource(images[1]);

                else if (deviceID.toLowerCase().contains(IOS.toLowerCase()))
                    mDeviceImageView.setImageResource(images[1]);

                else if (deviceID.toLowerCase().contains(Computer.toLowerCase()))
                    mDeviceImageView.setImageResource(images[0]);

                final String myDeviceID = getString(R.string.mobile_android).concat(DeviceInfoFactory.getDeviceId(getActivity()));

                mDeviceNameView.setText(trustedDevice.getDeviceName());
                mGrantTimeView.setText(trustedDevice.getCreatedTimeString());

                if (myDeviceID.equals(deviceID)) {
                    mDeviceNameView.setText(trustedDevice.getDeviceName());
                    mThisDeviceView.setVisibility(View.VISIBLE);
                    mDeviceNameView.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    mThisDeviceView.setVisibility(View.GONE);
                    mDeviceNameView.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_TRUSTED_DEVICES)
                    public void onClick(View v) {
                        if (!myDeviceID.equals(deviceID)) {
                            mTrustedDeviceActionList = Arrays.asList(getResources().getStringArray(R.array.trusted_device_or_network_action));
                            mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), trustedDevice.getDeviceName(), mTrustedDeviceActionList);
                            mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                                @Override
                                public void onResourceSelected(int selectedIndex, String mName) {
                                    if (selectedIndex == ACTION_REMOVE) {
                                        showTrustedDeviceRemoveConfirmationDialog(
                                                trustedDevice.getId());
                                    }
                                }
                            });
                            mCustomSelectorDialog.show();
                        }
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trusted_device,
                    parent, false);

            return new TrustedDeviceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                TrustedDeviceViewHolder vh = (TrustedDeviceViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mTrustedDeviceList != null)
                return mTrustedDeviceList.size();
            else return 0;
        }

    }
}
