package bd.com.ipay.ipayskeleton.AddParticipantsFragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.Model.MMModule.Events.GetAllParticipantsResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Events.EventClasses.Participant;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.GetPendingMoneyRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class SelectParticipantsFromListFragment extends Fragment implements HttpResponseListener {


    private HttpRequestPostAsyncTask mGetAllParticipantsTask = null;
    private GetAllParticipantsResponse mGetAllParticipantsResponse;

    private ProgressDialog mProgressDialog;
    private RecyclerView mParticipantsListRecyclerView;
    private ParticipantsListAdapter mParticipantsListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Participant> listOfParticipants;
    private ArrayList<Participant> selectedParticipants;
    private LinearLayout mAddNewParticipant;

    private int pageCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_all_participants, container, false);
        mProgressDialog = new ProgressDialog(getActivity());
        mParticipantsListRecyclerView = (RecyclerView) v.findViewById(R.id.list_all_participants);
        mAddNewParticipant = (LinearLayout) v.findViewById(R.id.add_new_participant_layout);
        selectedParticipants = new ArrayList<>();

        mParticipantsListAdapter = new ParticipantsListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mParticipantsListRecyclerView.setLayoutManager(mLayoutManager);
        mParticipantsListRecyclerView.setAdapter(mParticipantsListAdapter);

        mAddNewParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewParticipantDialog();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utilities.isConnectionAvailable(getActivity())) {
            getParticipantsList();
        }
    }

    private void showAddNewParticipantDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.add_new_participant)
                .customView(R.layout.dialog_add_new_participant, true)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

        View view = dialog.getCustomView();
        final EditText mMobileNumberEditText = (EditText) view.findViewById(R.id.mobile_number);
        final EditText mParticipantName = (EditText) view.findViewById(R.id.name);

        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                if (mMobileNumberEditText.getText().toString().trim().length() != 14) {
                    mMobileNumberEditText.setError(getString(R.string.error_invalid_mobile_number));
                    Toast.makeText(getActivity(), R.string.error_invalid_mobile_number, Toast.LENGTH_LONG).show();

                } else if (mParticipantName.getText().toString().trim().length() == 0) {
                    mParticipantName.setError(getString(R.string.error_invalid_name));
                    Toast.makeText(getActivity(), R.string.error_invalid_name, Toast.LENGTH_LONG).show();

                } else {

                    String mobileNumber = mMobileNumberEditText.getText().toString().trim();
                    String name = mParticipantName.getText().toString().trim();
                    Participant mParticipant = new Participant();
                    mParticipant.setParticipantName(name);
                    mParticipant.setParticipantMobileNumber(mobileNumber);

                    if (listOfParticipants.contains(mParticipant)) {
                        listOfParticipants.add(mParticipant);
                        selectedParticipants.add(mParticipant);

                        mParticipantsListAdapter.notifyDataSetChanged();

                    } else
                        Toast.makeText(getActivity(), R.string.participant_already_added, Toast.LENGTH_LONG).show();

                    dialog.dismiss();
                }
            }
        });

    }

    private void getParticipantsList() {
        if (mGetAllParticipantsTask != null) {
            return;
        }

        GetPendingMoneyRequest mUserActivityRequest = new GetPendingMoneyRequest(pageCount, 1); //TODO: Change this request
        Gson gson = new Gson();
        String json = gson.toJson(mUserActivityRequest);
        mGetAllParticipantsTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_ALL_PARTICIPANTS_LIST,
                Constants.BASE_URL_SM + Constants.URL_GET_ALL_PARTICIPANTS_LIST, json, getActivity());
        mGetAllParticipantsTask.mHttpResponseListener = this;
        mGetAllParticipantsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) {

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            mGetAllParticipantsTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.fetch_participants_failed, Toast.LENGTH_LONG).show();
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_ALL_PARTICIPANTS_LIST)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {

                    mGetAllParticipantsResponse = gson.fromJson(result.getJsonString(), GetAllParticipantsResponse.class);

                    if (listOfParticipants == null) {
                        listOfParticipants = mGetAllParticipantsResponse.getParticipants();
                    } else {
                        List<Participant> tempParticipantsList;
                        tempParticipantsList = mGetAllParticipantsResponse.getParticipants();
                        listOfParticipants.addAll(tempParticipantsList);
                    }

                    mParticipantsListAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.fetch_participants_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.fetch_participants_failed, Toast.LENGTH_LONG).show();
            }

            mGetAllParticipantsTask = null;

        }
    }

    public class ParticipantsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public ParticipantsListAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView mParticipantMobileNumber;
            private TextView mParticipantName;
            private TextView mParticipantDetails;
            private CheckBox mSelectCheckbox;

            public ViewHolder(final View itemView) {
                super(itemView);

                mParticipantMobileNumber = (TextView) itemView.findViewById(R.id.event_participant_number);
                mParticipantName = (TextView) itemView.findViewById(R.id.event_participant_name);
                mParticipantDetails = (TextView) itemView.findViewById(R.id.event_participant_details);
                mSelectCheckbox = (CheckBox) itemView.findViewById(R.id.participant_select_checkbox);
            }

            public void bindView(int pos) {

                final String id = listOfParticipants.get(pos).getId() + "";
                final String participantNumber = listOfParticipants.get(pos).getParticipantMobileNumber();
                final String participantName = listOfParticipants.get(pos).getParticipantName();
                final String participantDetail = listOfParticipants.get(pos).getParticipantDetailedInformation();

                mParticipantMobileNumber.setText(participantNumber);

                if (participantName.length() > 0) {
                    mParticipantName.setVisibility(View.VISIBLE);
                    mParticipantName.setText(participantName);
                }

                if (participantDetail.length() > 0) {
                    mParticipantDetails.setVisibility(View.VISIBLE);
                    mParticipantDetails.setText(participantDetail);
                }

                final Participant mParticipant = new Participant();
                mParticipant.setParticipantName(participantName);
                mParticipant.setParticipantMobileNumber(participantNumber);
                if (selectedParticipants.contains(mParticipant)) mSelectCheckbox.setChecked(true);
                else mSelectCheckbox.setChecked(false);

                mSelectCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectCheckbox.isChecked()) {
                            mSelectCheckbox.setChecked(false);
                            selectedParticipants.remove(mParticipant);
                        } else {
                            mSelectCheckbox.setChecked(true);
                            selectedParticipants.add(mParticipant);
                        }
                    }
                });

            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event_participant, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            try {
                ViewHolder vh = (ViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (listOfParticipants != null)
                return listOfParticipants.size();
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }
}
