package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthServicesListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthServicesListWithOTPRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthSettingsSaveResponse;
import bd.com.ipay.ipayskeleton.Model.TwoFA.TwoFactorAuthService;
import bd.com.ipay.ipayskeleton.Model.TwoFA.TwoFactorAuthServiceGroup;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class ImplementTwoFactorAuthenticationSettingsFragment extends Fragment implements HttpResponseListener {

    private RecyclerView mRecyclerView;
    private CustomProgressDialog mProgressDialog;

    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private HttpRequestGetAsyncTask mGetTwoFactorAuthSettingsAsyncTask;
    private TwoFactorAuthServicesListResponse mTwoFaServiceResponse;

    private HttpRequestPutAsyncTask mPutTwoFactorAuthSettingsAsyncTask;

    private int mCurrentSettings = 0;
    private int mChangedSettings = 0;
    private int mTotalServices = 0;

    List<TwoFactorAuthServiceGroup> mTwoFaServiceList;
    List<TwoFactorAuthService> mChangedList;

    private String mUri;
    private String mJsonString;

    private SparseArray<TwoFactorAuthService> mServiceIDMap = new SparseArray<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2fa_implement, container, false);
        mProgressDialog = new CustomProgressDialog(getActivity());
        getTwoFactorAuthSettings();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyler_view_two_factor_auth);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChangedList = new ArrayList<>();
        getActivity().setTitle(R.string.two_fa_settings);
        return view;
    }

    private void getTwoFactorAuthSettings() {
        if (mGetTwoFactorAuthSettingsAsyncTask != null) {
            return;
        }
        String API_COMMAND = Constants.COMMAND_GET_TWO_FACTOR_AUTH_SETTINGS;
        String mUri = Constants.BASE_URL_MM + Constants.URL_TWO_FACTOR_AUTH_SETTINGS;
        mGetTwoFactorAuthSettingsAsyncTask = new HttpRequestGetAsyncTask(API_COMMAND, mUri, getActivity(), false);
        mGetTwoFactorAuthSettingsAsyncTask.mHttpResponseListener = this;
        mGetTwoFactorAuthSettingsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mProgressDialog.show();
    }

    int setBit(int N, int pos) {
        return N | (1 << pos);
    }

    int resetBit(int N, int pos) {
        return N & ~(1 << pos);
    }

    boolean check(int N, int pos) {
        return (N & (1 << pos)) != 0;
    }

    private void setCurrentSettings() {
        int pos = 0;
        for (int i = 0; i < mTwoFaServiceList.size(); i++) {
            for (int j = 0; j < mTwoFaServiceList.get(i).getServices().size(); j++) {
                if (mTwoFaServiceList.get(i).getServices().get(j).getIsEnabled()) {
                    mServiceIDMap.put(pos, mTwoFaServiceList.get(i).getServices().get(j));
                    mCurrentSettings = setBit(mCurrentSettings, pos);
                    pos++;
                } else {
                    mServiceIDMap.put(pos, mTwoFaServiceList.get(i).getServices().get(j));
                    mCurrentSettings = resetBit(mCurrentSettings, pos);
                    pos++;
                }
                mTotalServices++;
            }
        }
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void getChangedSettings() {
        int pos = 0;
        for (int i = 0; i < mTwoFaServiceList.size(); i++) {
            for (int j = 0; j < mTwoFaServiceList.get(i).getServices().size(); j++) {
                if (mTwoFaServiceList.get(i).getServices().get(j).getIsEnabled()) {
                    mChangedSettings = setBit(mChangedSettings, pos);
                    pos++;
                } else {
                    mChangedSettings = resetBit(mChangedSettings, pos);
                    pos++;
                }
            }
        }
    }

    public void attemptSaveTwoFactorAuthSettings(List<TwoFactorAuthService> mChangedList) {
        if (mPutTwoFactorAuthSettingsAsyncTask != null)
            return;
        Gson gson = new Gson();
        String API_COMMAND = Constants.COMMAND_PUT_TWO_FACTOR_AUTH_SETTINGS;
        mUri = Constants.BASE_URL_MM + Constants.URL_TWO_FACTOR_AUTH_SETTINGS;
        TwoFactorAuthServicesListWithOTPRequest twoFactorAuthServicesListWithOtpRequest = new TwoFactorAuthServicesListWithOTPRequest(null, mChangedList);
        mJsonString = gson.toJson(twoFactorAuthServicesListWithOtpRequest);
        mPutTwoFactorAuthSettingsAsyncTask = new HttpRequestPutAsyncTask(API_COMMAND, mUri, mJsonString, getActivity(), false);
        mPutTwoFactorAuthSettingsAsyncTask.mHttpResponseListener = this;
        mPutTwoFactorAuthSettingsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mProgressDialog.show();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        Gson gson = new Gson();
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mGetTwoFactorAuthSettingsAsyncTask = null;
            mPutTwoFactorAuthSettingsAsyncTask = null;
            return;
        } else {
            if (result.getApiCommand().equals(Constants.COMMAND_GET_TWO_FACTOR_AUTH_SETTINGS)) {
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            mTwoFaServiceResponse = gson.fromJson(result.getJsonString(), TwoFactorAuthServicesListResponse.class);
                            mTwoFaServiceList = mTwoFaServiceResponse.getResponse();
                            ImplementTwoFactorAuthAdapter adapter = new ImplementTwoFactorAuthAdapter(mTwoFaServiceList);
                            mRecyclerView.setAdapter(adapter);
                            setCurrentSettings();
                        }
                    } else {
                        if (getActivity() != null) {
                            mProgressDialog.dismiss();
                            Toaster.makeText(getActivity(), mTwoFaServiceResponse.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
                } catch (Exception e) {
                    Toaster.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
                }
                mGetTwoFactorAuthSettingsAsyncTask = null;
                mProgressDialog.dismiss();

            } else if (result.getApiCommand().equals(Constants.COMMAND_PUT_TWO_FACTOR_AUTH_SETTINGS)) {
                TwoFactorAuthSettingsSaveResponse twoFactorAuthSettingsSaveResponse = gson.fromJson(result.getJsonString(), TwoFactorAuthSettingsSaveResponse.class);
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mProgressDialog.dismiss();
                        Toaster.makeText(getActivity(), twoFactorAuthSettingsSaveResponse.getMessage(), Toast.LENGTH_SHORT);
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        }
                        getTwoFactorAuthSettings();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        if (getActivity() != null) {
                            SecuritySettingsActivity.otpDuration = twoFactorAuthSettingsSaveResponse.getOtpValidFor();
                            Toaster.makeText(getActivity(), twoFactorAuthSettingsSaveResponse.getMessage(), Toast.LENGTH_LONG);
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), mJsonString,
                                    Constants.COMMAND_PUT_TWO_FACTOR_AUTH_SETTINGS, mUri, Constants.METHOD_PUT);
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
                        }
                    } else {
                        Toaster.makeText(getActivity(), twoFactorAuthSettingsSaveResponse.getMessage(), Toast.LENGTH_SHORT);
                        if (twoFactorAuthSettingsSaveResponse.getMessage().toLowerCase().contains("wrong")) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                        } else if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        }
                    }
                } catch (Exception e) {
                    Toaster.makeText(getActivity(), twoFactorAuthSettingsSaveResponse.getMessage(), Toast.LENGTH_LONG);
                }
                mPutTwoFactorAuthSettingsAsyncTask = null;
                mProgressDialog.dismiss();
            }
        }
    }

    public class ImplementTwoFactorAuthAdapter extends RecyclerView.Adapter<ImplementTwoFactorAuthAdapter.ViewHolder> {
        private static final int IMPLEMENT_TWO_FACTOR_AUTH_HEADER_VIEW = 1;
        private static final int IMPLEMENT_TWO_FACTOR_AUTH_ITEM_VIEW = 2;
        private static final int FOOTER_VIEW = 3;

        private List<TwoFactorAuthServiceGroup> mTwoFactorAuthServiceGroupList;
        private List<Integer> headerPositionList;
        private int footerPosition;
        private int itemCount;

        ImplementTwoFactorAuthAdapter(List<TwoFactorAuthServiceGroup> twoFactorAuthServiceGroupList) {
            this.mTwoFactorAuthServiceGroupList = twoFactorAuthServiceGroupList;
            this.headerPositionList = new ArrayList<>();
            footerPosition = 0;

            itemCount = 0;
            headerPositionList = new ArrayList<>();
            if (this.mTwoFactorAuthServiceGroupList != null) {
                for (TwoFactorAuthServiceGroup twoFactorAuthServiceGroup : this.mTwoFactorAuthServiceGroupList) {
                    headerPositionList.add(itemCount);
                    itemCount++;
                    if (twoFactorAuthServiceGroup != null && twoFactorAuthServiceGroup.getServices() != null) {
                        itemCount += twoFactorAuthServiceGroup.getServices().size();
                    }
                }
            }
            footerPosition = itemCount;
            itemCount++;
        }

        public abstract class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(final View itemView) {
                super(itemView);

            }

            public <T extends View> T findViewById(@IdRes int id) {
                //noinspection unchecked
                return (T) itemView.findViewById(id);
            }

            public abstract void bindView(int position);
        }

        class HeaderViewHolder extends ViewHolder {
            private TextView mHeaderTextView;

            HeaderViewHolder(View itemView) {
                super(itemView);
                mHeaderTextView = findViewById(R.id.header_text_view);
            }

            @Override
            public void bindView(int position) {
                mHeaderTextView.setText(mTwoFactorAuthServiceGroupList.get(headerPositionList.indexOf(position)).getGroupName());
            }
        }

        class ItemViewHolder extends ViewHolder {
            private TextView mDescriptionTextView;
            private Switch mSwitch;

            int twoFactorAuthSettingsGroupIndex = 0;
            int selectedHeaderPosition = 0;
            int desiredPosition = 0;

            ItemViewHolder(View itemView) {
                super(itemView);
                mDescriptionTextView = findViewById(R.id.description_text_view);
                mSwitch = findViewById(R.id.switch_list_item);
            }

            @Override
            public void bindView(int position) {
                for (int i = 0; i < headerPositionList.size(); i++) {
                    if (position > headerPositionList.get(i)) {
                        if (i < headerPositionList.size() - 1) {
                            if (position < headerPositionList.get(i + 1)) {
                                twoFactorAuthSettingsGroupIndex = i;
                                selectedHeaderPosition = headerPositionList.get(i);
                                break;
                            }
                        } else if (i == headerPositionList.size() - 1) {
                            twoFactorAuthSettingsGroupIndex = i;
                            selectedHeaderPosition = headerPositionList.get(i);
                            break;
                        }
                    }
                }
                final TwoFactorAuthService twoFactorAuthService = mTwoFactorAuthServiceGroupList.get(twoFactorAuthSettingsGroupIndex).getServices().get(position - selectedHeaderPosition - 1);
                desiredPosition = position - selectedHeaderPosition - 1;
                mDescriptionTextView.setText(twoFactorAuthService.getServiceName());
                mSwitch.setChecked(twoFactorAuthService.getIsEnabled());

                mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mTwoFactorAuthServiceGroupList.get(twoFactorAuthSettingsGroupIndex).getServices().get(desiredPosition).setIsEnabled(mSwitch.isChecked());
                    }
                });
            }
        }

        class FooterViewHolder extends ViewHolder {
            private Button mButtonSave;

            FooterViewHolder(View itemView) {
                super(itemView);
                mButtonSave = findViewById(R.id.button_footer_save);
            }


            @Override
            public void bindView(int position) {
                mButtonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mChangedList.clear();
                        getChangedSettings();
                        if (mChangedSettings != mCurrentSettings) {
                            for (int i = 0; i < mTotalServices; i++) {
                                if ((check(mChangedSettings, i) && !check(mCurrentSettings, i)) || !check(mChangedSettings, i) && check(mCurrentSettings, i)) {
                                    mChangedList.add(mServiceIDMap.get(i));
                                }
                            }
                            attemptSaveTwoFactorAuthSettings(mChangedList);
                        } else {
                            MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                                    .content(R.string.settings_not_changed)
                                    .negativeText(R.string.cancel)
                                    .build();
                            materialDialog.show();
                        }
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case IMPLEMENT_TWO_FACTOR_AUTH_HEADER_VIEW:
                    return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header_2fa, parent, false));
                case IMPLEMENT_TWO_FACTOR_AUTH_ITEM_VIEW:
                    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_2fa, parent, false));
                case FOOTER_VIEW:
                    return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_button_two_fa_footer, parent, false));
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        @Override
        public int getItemViewType(int position) {
            int headerPositionIndex = headerPositionList.indexOf(position);
            if (headerPositionIndex != -1) {
                return IMPLEMENT_TWO_FACTOR_AUTH_HEADER_VIEW;
            } else if (position == footerPosition) {
                return FOOTER_VIEW;
            } else {
                return IMPLEMENT_TWO_FACTOR_AUTH_ITEM_VIEW;
            }
        }
    }
}
