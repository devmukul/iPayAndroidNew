package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpDeleteWithBodyAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.PasswordInputDialogBuilder;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.GetTrustedPersonsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.RemoveTrustedPersonResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.TrustedNetwork.TrustedPerson;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TrustedNetworkFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetTrustedPersonsTask = null;
    private GetTrustedPersonsResponse mGetTrustedPersonsResponse = null;

    private HttpDeleteWithBodyAsyncTask mRemoveTrustedPersonTask = null;
    private RemoveTrustedPersonResponse mRemoveTrustedPersonResponse = null;

    private List<TrustedPerson> mTrustedPersons;
    private TrustedPersonListAdapter mTrustedPersonListAdapter;

    private FloatingActionButton mAddTrustedPersonButton;

    private RecyclerView mTrustedPersonListRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mEmptyListTextView;
    private CustomProgressDialog mProgressDialog;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trusted_network, container, false);
        setTitle();

        mAddTrustedPersonButton = (FloatingActionButton) v.findViewById(R.id.fab_add_trusted_person);
        mTrustedPersonListRecyclerView = (RecyclerView) v.findViewById(R.id.list_trusted_person);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
        mProgressDialog = new CustomProgressDialog(getActivity());

        mTrustedPersonListAdapter = new TrustedPersonListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mTrustedPersonListRecyclerView.setLayoutManager(mLayoutManager);
        mTrustedPersonListRecyclerView.setAdapter(mTrustedPersonListAdapter);

        mAddTrustedPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.MANAGE_TRUSTED_PERSON)
            public void onClick(View v) {
                if (CommonData.getRelationshipList() != null) {
                    ((SecuritySettingsActivity) getActivity()).switchToAddTrustedPerson();
                } else {
                    Toast.makeText(getContext(), "Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    getTrustedPersons();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getTrustedPersons();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utilities.isConnectionAvailable(getActivity())) {
            getTrustedPersons();
        }
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_trusted_network));
    }

    public void setTitle() {
        getActivity().setTitle(R.string.trusted_person);
    }

    private void processGetTrustedPersonList(String json) {
        try {
            Gson gson = new Gson();
            mGetTrustedPersonsResponse = gson.fromJson(json, GetTrustedPersonsResponse.class);

            mTrustedPersons = mGetTrustedPersonsResponse.getTrustedPersons();
            mTrustedPersonListAdapter.notifyDataSetChanged();

            setContentShown(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTrustedDeviceRemoveConfirmationDialog(final long personID) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog
                .setMessage(getString(R.string.confirmation_remove_trusted_person))
                .setPositiveButton(getString(R.string.remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final PasswordInputDialogBuilder passwordInputDialogBuilder = new PasswordInputDialogBuilder(getActivity());
                        passwordInputDialogBuilder.onSubmit(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                removeTrustedPerson(personID, passwordInputDialogBuilder.getPassword());
                            }
                        });
                        passwordInputDialogBuilder.build().show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }


    private void getTrustedPersons() {
        if (mGetTrustedPersonsTask != null) {
            return;
        }

        setContentShown(false);

        mGetTrustedPersonsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRUSTED_PERSONS,
                Constants.BASE_URL_MM + Constants.URL_GET_TRUSTED_PERSONS, getActivity(), this, false);
        mGetTrustedPersonsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void removeTrustedPerson(long personID, String password) {
        if (mRemoveTrustedPersonTask != null)
            return;

        mProgressDialog.show();

        DeleteTrustedPersonRequest deleteTrustedPersonRequest = new DeleteTrustedPersonRequest(personID,
                password);
        Gson gson = new Gson();
        String json = gson.toJson(deleteTrustedPersonRequest);

        mRemoveTrustedPersonTask = new HttpDeleteWithBodyAsyncTask(Constants.COMMAND_REMOVE_TRUSTED_PERSON,
                Constants.BASE_URL_MM + Constants.URL_REMOVE_TRUSTED_PERSON, json, getActivity(), this, false);
        mRemoveTrustedPersonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mGetTrustedPersonsTask = null;
            mRemoveTrustedPersonTask = null;
            setContentShown(true);
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_TRUSTED_PERSONS)) {
            try {
                mGetTrustedPersonsResponse = gson.fromJson(result.getJsonString(), GetTrustedPersonsResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    processGetTrustedPersonList(result.getJsonString());
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mGetTrustedPersonsResponse.getMessage(), Toast.LENGTH_LONG).show();
                    ((SecuritySettingsActivity) getActivity()).launchHomeActivity();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.failed_loading_trusted_person_list, Toast.LENGTH_LONG).show();
                ((SecuritySettingsActivity) getActivity()).switchToAccountSettingsFragment();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mGetTrustedPersonsTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_REMOVE_TRUSTED_PERSON)) {

            try {
                mRemoveTrustedPersonResponse = gson.fromJson(result.getJsonString(), RemoveTrustedPersonResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.success_remove, Toast.LENGTH_LONG).show();
                    }
                    mProgressDialog.show();

                    getTrustedPersons();
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null)
                        ((MyApplication) getActivity().getApplication()).launchLoginPage(mRemoveTrustedPersonResponse.getMessage());
                    Utilities.sendBlockedEventTracker(mTracker, "Trusted Network", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mRemoveTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), mRemoveTrustedPersonResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            mProgressDialog.cancel();
            mRemoveTrustedPersonTask = null;

        }
        if (mTrustedPersons.isEmpty()) {
            mEmptyListTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListTextView.setVisibility(View.GONE);
        }
    }


    public class TrustedPersonListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;
        private static final int TRUSTED_LIST_ITEM_VIEW = 2;
        private int ACTION_REMOVE = 0;

        public TrustedPersonListAdapter() {
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trusted_network_footer, parent, false);
                return new FooterViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trusted_network, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof ViewHolder) {
                    ViewHolder vh = (ViewHolder) holder;
                    vh.bindView(position);
                } else if (holder instanceof FooterViewHolder) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mTrustedPersons != null && mTrustedPersons.size() > 0)
                return mTrustedPersons.size() + 1;
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return FOOTER_VIEW;
            } else {
                return TRUSTED_LIST_ITEM_VIEW;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View divider;
            private TextView mNameView;
            private TextView mMobileNumberView;
            private TextView mRelationshipView;
            private List<String> mTrustedPersonActionList;
            private CustomSelectorDialog mCustomSelectorDialog;

            public ViewHolder(final View itemView) {
                super(itemView);

                mNameView = (TextView) itemView.findViewById(R.id.textview_name);
                mMobileNumberView = (TextView) itemView.findViewById(R.id.textview_mobile_number);
                mRelationshipView = (TextView) itemView.findViewById(R.id.textview_relationship);
                divider = itemView.findViewById(R.id.divider);
            }

            public void bindView(int pos) {

                final TrustedPerson trustedPerson = mTrustedPersons.get(pos);

                mNameView.setText(trustedPerson.getName());
                mMobileNumberView.setText(trustedPerson.getMobileNumber());
                mRelationshipView.setText(trustedPerson.getRelationship());
                if (pos == mTrustedPersons.size() - 1)
                    divider.setVisibility(View.GONE);
                else
                    divider.setVisibility(View.VISIBLE);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_TRUSTED_PERSON)
                    public void onClick(View v) {
                        mTrustedPersonActionList = Arrays.asList(getResources().getStringArray(R.array.trusted_device_or_network_action));
                        mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), trustedPerson.getName(), mTrustedPersonActionList);
                        mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                            @Override
                            public void onResourceSelected(int selectedIndex, String mName) {
                                if (selectedIndex == ACTION_REMOVE) {
                                    showTrustedDeviceRemoveConfirmationDialog(
                                            trustedPerson.getPersonId());
                                }
                            }
                        });
                        mCustomSelectorDialog.show();

                    }
                });
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);
            }

        }
    }
}
