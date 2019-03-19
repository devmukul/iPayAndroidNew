package bd.com.ipay.ipayskeleton.SourceOfFund;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpDeleteWithBodyAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Beneficiary;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetBeneficiaryListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.RemoveSponsorOrBeneficiaryRequest;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.SourceOfFund.view.BeneficiaryUpdateDialog;
import bd.com.ipay.ipayskeleton.SourceOfFund.view.SourceOfFundTypeSelectorDialog;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

import static android.view.View.GONE;

public class SourceOfFundListShowFragment extends Fragment implements HttpResponseListener {

    private RecyclerView sourceOfFundListRecyclerView;
    private ImageView backButton;
    private TextView addNewTextView;

    private ArrayList<Sponsor> sponsorArrayList;

    private ArrayList<Beneficiary> beneficiaryArrayList;

    private HttpRequestGetAsyncTask getSponsorListAsyncTask;
    private HttpRequestGetAsyncTask getBeneficiaryListAsyncTask;

    private HttpDeleteWithBodyAsyncTask deleteSponsorAsyncTask;

    private CustomProgressDialog mProgressDialog;

    private SourceOfFundListAdapter sponsorAdapter;
    private SourceOfFundListAdapter beneficiaryAdapter;

    private TextView noSourceOfFundTextView;
    private TextView titleTextView;

    private TextView titleBeneficiaryTextView;
    private RecyclerView beneficiaryRecyclerView;

    private boolean isSponsorApiCalled;
    private boolean isBeneficiaryApiCalled;

    private FloatingActionButton addNewButton;

    private SourceOfFundTypeSelectorDialog sourceOfFundTypeSelectorDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_source_of_fund_list_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        backButton = view.findViewById(R.id.back);
        addNewButton = (FloatingActionButton) view.findViewById(R.id.add_new_button);
        isBeneficiaryApiCalled = false;
        isSponsorApiCalled = false;
        beneficiaryArrayList = new ArrayList<>();
        sponsorArrayList = new ArrayList<>();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        titleBeneficiaryTextView = view.findViewById(R.id.title_me_as_ipay_source);
        beneficiaryRecyclerView = view.findViewById(R.id.beneficiary_recycler_view);
        addNewTextView = view.findViewById(R.id.add_new);
        titleTextView = view.findViewById(R.id.title);
        noSourceOfFundTextView = (TextView) view.findViewById(R.id.no_source_of_fund);
        noSourceOfFundTextView.setVisibility(GONE);
        attemptGetSponsorList();

        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceOfFundTypeSelectorDialog = new SourceOfFundTypeSelectorDialog(getContext(), new SourceOfFundTypeSelectorDialog.SourceOfFundTypeSelectorListener() {
                    @Override
                    public void onSourceOfFundTypeSelected(String type) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.TYPE, type);
                        ((SourceOfFundActivity) (getActivity())).switchToAddSourceOfFundFragment(bundle);
                    }
                });
            }
        });

        sourceOfFundListRecyclerView = view.findViewById(R.id.source_of_fund_list_recycler_view);
        sourceOfFundListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void removeRejectedEntriesForSponsors(ArrayList<Sponsor> sponsors) {
        sponsorArrayList.clear();
        for (int i = 0; i < sponsors.size(); i++) {
            if (!sponsors.get(i).getStatus().equals("REJECTED") && !sponsors.get(i).getStatus().equals("PENDING")) {
                sponsorArrayList.add(sponsors.get(i));
            }
        }
    }

    private void removeRejectedEntriesForBeneficiaries(ArrayList<Beneficiary> beneficiaries) {
        beneficiaryArrayList.clear();
        for (int i = 0; i < beneficiaries.size(); i++) {
            if (!beneficiaries.get(i).getStatus().equals("REJECTED") && !beneficiaries.get(i).getStatus().equals("PENDING")) {
                beneficiaryArrayList.add(beneficiaries.get(i));
            }
        }

    }

    private void attemptGetSponsorList() {
        if (getSponsorListAsyncTask != null) {
            return;
        } else {
            mProgressDialog = new CustomProgressDialog(getContext());
            getSponsorListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SPONSOR_LIST, Constants.BASE_URL_MM + Constants.URL_GET_SPONSOR,
                    getContext(), this, false);
            getSponsorListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();

        }
    }

    private void attemptGetBeneficiaryList() {
        if (getBeneficiaryListAsyncTask != null) {
            return;
        } else {
            mProgressDialog = new CustomProgressDialog(getContext());
            getBeneficiaryListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BENEFICIARY_LIST, Constants.BASE_URL_MM + Constants.URL_GET_BENEFICIARY,
                    getContext(), this, false);
            getBeneficiaryListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();

        }
    }

    private int ifNoBeneficiaryAndSponsor() {
        int beneficiaryLength = 0;
        int sponsorLength = 0;
        if (beneficiaryArrayList != null) {
            beneficiaryLength = beneficiaryArrayList.size();
        }
        if (sponsorArrayList != null) {
            sponsorLength = sponsorArrayList.size();
        }
        return beneficiaryLength + sponsorLength;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            getSponsorListAsyncTask = null;
            mProgressDialog.dismiss();
            return;
        } else {
            mProgressDialog.dismiss();
            GetSponsorListResponse getSponsorListResponse = null;
            try {
                if (result.getApiCommand().equals(Constants.COMMAND_GET_SPONSOR_LIST)) {
                    attemptGetBeneficiaryList();
                    mProgressDialog.dismiss();
                    isSponsorApiCalled = true;
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getSponsorListResponse = new Gson().fromJson(result.getJsonString(), GetSponsorListResponse.class);
                        ArrayList<Sponsor> sponsors = getSponsorListResponse.getSponsor();
                        removeRejectedEntriesForSponsors(sponsors);
                        if (sponsorArrayList.size() > 0) {
                            sponsorAdapter = new SourceOfFundListAdapter("SPONSOR");
                            sourceOfFundListRecyclerView.setAdapter(sponsorAdapter);
                            noSourceOfFundTextView.setVisibility(GONE);
                            titleTextView.setVisibility(View.VISIBLE);
                            sourceOfFundListRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            titleTextView.setVisibility(GONE);
                            sourceOfFundListRecyclerView.setVisibility(GONE);

                            if (isBeneficiaryApiCalled) {
                                if (ifNoBeneficiaryAndSponsor() == 0) {
                                    noSourceOfFundTextView.setVisibility(View.VISIBLE);
                                } else {
                                    noSourceOfFundTextView.setVisibility(GONE);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), getSponsorListResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    getSponsorListAsyncTask = null;

                } else if (result.getApiCommand().equals(Constants.COMMAND_GET_BENEFICIARY_LIST)) {
                    mProgressDialog.dismiss();
                    isBeneficiaryApiCalled = true;
                    GetBeneficiaryListResponse getBeneficiaryListResponse = new Gson().fromJson(result.getJsonString(),
                            GetBeneficiaryListResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ArrayList<Beneficiary> beneficiaryList = getBeneficiaryListResponse.getBeneficiary();
                        removeRejectedEntriesForBeneficiaries(beneficiaryList);
                        if (beneficiaryArrayList.size() > 0) {
                            noSourceOfFundTextView.setVisibility(GONE);
                            titleBeneficiaryTextView.setVisibility(View.VISIBLE);
                            beneficiaryAdapter = new SourceOfFundListAdapter("BENEFICIARY");
                            beneficiaryRecyclerView.setAdapter(beneficiaryAdapter);
                            beneficiaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        } else {
                            titleBeneficiaryTextView.setVisibility(GONE);
                            beneficiaryRecyclerView.setVisibility(GONE);
                            if (isSponsorApiCalled) {
                                if (ifNoBeneficiaryAndSponsor() == 0) {
                                    noSourceOfFundTextView.setVisibility(View.VISIBLE);
                                } else {
                                    noSourceOfFundTextView.setVisibility(GONE);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), getBeneficiaryListResponse.getMessage(), Toast.LENGTH_LONG).show();

                    }
                    getBeneficiaryListAsyncTask = null;

                } else if (result.getApiCommand().equals(Constants.COMMAND_REMOVE_SPONSOR_OR_BENEFICIARY)) {
                    GenericResponseWithMessageOnly responseWithMessageOnly = new Gson().fromJson
                            (result.getJsonString(), GenericResponseWithMessageOnly.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                        attemptGetSponsorList();
                    } else {
                        Toast.makeText(getContext(), responseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    deleteSponsorAsyncTask = null;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), getSponsorListResponse.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    public void attemptRemoveSponsorOrBeneficiary(long id, String pin) {
        if (deleteSponsorAsyncTask != null) {
            return;
        } else {
            RemoveSponsorOrBeneficiaryRequest removeSponsorOrBeneficiaryRequest = new RemoveSponsorOrBeneficiaryRequest(pin);
            deleteSponsorAsyncTask = new HttpDeleteWithBodyAsyncTask(Constants.COMMAND_REMOVE_SPONSOR_OR_BENEFICIARY,
                    Constants.BASE_URL_MM + Constants.URL_DELETE_SPONSOR + id, new Gson().toJson
                    (removeSponsorOrBeneficiaryRequest),
                    getContext(), this, false);
            deleteSponsorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    public class SourceOfFundListAdapter extends RecyclerView.Adapter<SourceOfFundListAdapter.SourceOfFundViewHolder> {
        private String type;

        public SourceOfFundListAdapter(String type) {
            this.type = type;
        }

        @NonNull
        @Override
        public SourceOfFundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SourceOfFundViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_source_of_fund, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SourceOfFundViewHolder holder, final int position) {
            if (type.equals("SPONSOR")) {
                bindSponsorView(holder, sponsorArrayList.get(position));
            } else {
                bindBeneficiaryView(holder, beneficiaryArrayList.get(position));
            }
        }

        public void bindSponsorView(SourceOfFundViewHolder holder, final Sponsor sponsor) {
            holder.profileImageView.setProfilePicture(sponsor.getUser().getProfilePictureUrl(), false);
            holder.nameTextView.setText(sponsor.getUser().getName());
            holder.numberTextView.setText(sponsor.getUser().getMobileNumber());
            holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sponsor.getStatus().equals("PENDING")) {
                        new AlertDialog.Builder(getContext())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        attemptRemoveSponsorOrBeneficiary(sponsor.getId(), "");
                                    }
                                }).setMessage("Do you want to remove this sponsor?")
                                .show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        attemptRemoveSponsorWithPinCheck(sponsor.getId());
                                    }
                                }).setMessage("Do you want to remove this sponsor?")
                                .show();
                    }

                }
            });
        }

        public void bindBeneficiaryView(SourceOfFundViewHolder holder, final Beneficiary beneficiary) {
            holder.profileImageView.setProfilePicture(beneficiary.getUser().getProfilePictureUrl(), false);
            holder.nameTextView.setText(beneficiary.getUser().getName());
            holder.numberTextView.setText(beneficiary.getUser().getMobileNumber());
            if (beneficiary.getStatus().equals("APPROVED")) {
                holder.editImageView.setVisibility(View.VISIBLE);
                holder.editImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BeneficiaryUpdateDialog beneficiaryUpdateDialog = new
                                BeneficiaryUpdateDialog(getContext(),
                                beneficiary.getId(), beneficiary.getName());
                    }
                });
            } else {
                holder.editImageView.setVisibility(GONE);

            }
            holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (beneficiary.getStatus().equals("PENDING")) {
                        new AlertDialog.Builder(getContext())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        attemptRemoveSponsorOrBeneficiary(beneficiary.getId(), "");
                                    }
                                }).setMessage("Do you want to remove this beneficiary?")
                                .show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        attemptRemoveSponsorWithPinCheck(beneficiary.getId());
                                    }
                                }).setMessage("Do you want to remove this beneficiary?")
                                .show();
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            if (type.equals("SPONSOR")) {
                return sponsorArrayList.size();
            } else {
                return beneficiaryArrayList.size();
            }
        }

        public class SourceOfFundViewHolder extends RecyclerView.ViewHolder {

            private ProfileImageView profileImageView;
            private TextView nameTextView;
            private TextView numberTextView;
            private ImageView deleteImageView;
            private ImageView editImageView;

            public SourceOfFundViewHolder(View itemView) {
                super(itemView);
                nameTextView = (TextView) itemView.findViewById(R.id.name);
                numberTextView = (TextView) itemView.findViewById(R.id.number);
                profileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                deleteImageView = (ImageView) itemView.findViewById(R.id.delete);
                editImageView = (ImageView) itemView.findViewById(R.id.edit);
            }
        }
    }

    private void attemptRemoveSponsorWithPinCheck(final long id) {
        new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
            @Override
            public void ifPinCheckedAndAdded(String pin) {
                attemptRemoveSponsorOrBeneficiary(id, pin);
            }
        });
    }
}
