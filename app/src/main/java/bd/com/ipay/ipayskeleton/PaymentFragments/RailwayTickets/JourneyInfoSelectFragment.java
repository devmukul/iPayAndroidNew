package bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets;


import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.RailwayTicketActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.SelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.SelectorDialogWithSearch;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.SpinnerEditTextWithProgressBar;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.GetStationResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Widgets.IPaySnackbar;

public class JourneyInfoSelectFragment extends Fragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetStationFromListAsyncTask = null;
    private GetStationResponse mGetStationFromResponse;

    private HttpRequestGetAsyncTask mGetStationToListAsyncTask = null;
    private GetStationResponse mGetStationToResponse;

    private SpinnerEditTextWithProgressBar mStationFromEditTextProgressBar;
    private SpinnerEditTextWithProgressBar mStationToEditTextProgressBar;
    private SpinnerEditTextWithProgressBar mGenderEditTextProgressBar;
    private SpinnerEditTextWithProgressBar mAdultEditTextProgressBar;
    private SpinnerEditTextWithProgressBar mChildEditTextProgressBar;

    private EditText mStationFromSelection;
    private EditText mStationToSelection;
    private EditText mGenderSelection;
    private EditText mDateSelection;
    private EditText mChildSelection;
    private EditText mAdultSelection;
    private TextView mHelperText;

    private List<String> mStationFromList;
    private List<String> mStationToList;
    private List<String> mGenderList;

    private List<String> mAdultList;
    private List<String> mChildList;

    private SelectorDialogWithSearch stationFromSelectorDialog;
    private SelectorDialogWithSearch stationToSelectorDialog;
    private SelectorDialog genderSelectorDialog;

    private SelectorDialog childSelectorDialog;
    private SelectorDialog adultSelectorDialog;

    private Button mContinueButton;

    private String mSelectedStationFrom = null;
    private String mSelectedStationTo = null;
    private String mSelectedGender = null;
    private int mSelectedDate;
    private int mSelectedAdult = 0;
    private int mSelectedChild = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journey_info_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContinueButton = view.findViewById(R.id.button_send_money);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        mContinueButton = view.findViewById(R.id.button_send_money);
        mHelperText = view.findViewById(R.id.helper_text);
        //String text = "You can purchase max <font color='#00c0af'>4 seats</font> at once and highest <font color='#00c0af'>8 seats</font> per week.";
        //mHelperText.setText(Html.fromHtml(text));
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (verifyUserInputs()) {
                Bundle bundle = new Bundle();
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_FROM, mSelectedStationFrom);
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_TO, mSelectedStationTo);
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_DATE, mSelectedDate);
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, mSelectedAdult);
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, mSelectedChild);
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_GENDER, mSelectedGender);
                ((RailwayTicketActionActivity) getActivity()).
                        switchFragment(new TrainSelectionFragment(), bundle, 2, true);

            }
            }
        });
        ((RailwayTicketActionActivity) getActivity()).setSupportActionBar(toolbar);
        ((RailwayTicketActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle(R.string.railway_ticket_title);


        mStationFromEditTextProgressBar = view.findViewById(R.id.station_from);
        mStationToEditTextProgressBar = view.findViewById(R.id.station_to);
        mGenderEditTextProgressBar = view.findViewById(R.id.gender);

        mAdultEditTextProgressBar = view.findViewById(R.id.adult_seat);
        mChildEditTextProgressBar = view.findViewById(R.id.child_seat);

        mStationFromSelection = mStationFromEditTextProgressBar.getEditText();
        mStationToSelection = mStationToEditTextProgressBar.getEditText();
        mGenderSelection = mGenderEditTextProgressBar.getEditText();

        mAdultSelection = mAdultEditTextProgressBar.getEditText();
        mChildSelection = mChildEditTextProgressBar.getEditText();

        mDateSelection = view.findViewById(R.id.journey_date);

        mDateSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar calendar = Calendar.getInstance();
                final int mYear = calendar.get(Calendar.YEAR);
                final int mMonth = calendar.get(Calendar.MONTH);
                final int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        mSelectedDate = date;
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, month, date, 0, 0, 0);
                        Date chosenDate = cal.getTime();
                        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
                        String formattedDate = df.format(chosenDate);
                        mDateSelection.setText(formattedDate);
                    }
                },mYear,mMonth,mDay);

                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                calendar.add(Calendar.DATE, Constants.MAX_TICKET_PURCHASE_DAY);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                datePickerDialog.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mStationFromSelection.setText("");
        mStationToSelection.setText("");
        mAdultSelection.setText("");
        mChildSelection.setText("");
        mGenderSelection.setText("");
        getStationFromList();
        getAdultLIst();
        setAdultAdapter(mAdultList);
        getGenderLIst();
        setGenderAdapter(mGenderList);
    }

    private void getStationFromList() {
        if (mGetStationFromListAsyncTask != null) {
            return;
        }
        mStationFromEditTextProgressBar.showProgressBar();

        mGetStationFromListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_STATION_FROM,
                Constants.BASE_URL_CNS + Constants.URL_ORIGINATING_STATION, getContext(), this, true);
        mGetStationFromListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getStationToList(String originatingStation) {
        if (mGetStationToListAsyncTask != null) {
            return;
        }
        mStationToEditTextProgressBar.showProgressBar();

        mGetStationToListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_STATION_TO,
                Constants.BASE_URL_CNS + Constants.URL_ORIGINATING_STATION_TO + originatingStation, getContext(), this, true);
        mGetStationToListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setStationFromAdapter(List<String> stationList) {
        stationFromSelectorDialog = new SelectorDialogWithSearch(getContext(), getContext().getString(R.string.select_a_station), mStationFromList);
        stationFromSelectorDialog.setOnResourceSelectedListener(new SelectorDialogWithSearch.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                mStationFromSelection.setError(null);
                mStationFromSelection.setText(name);
                mStationToSelection.setError(null);
                mSelectedStationFrom = name;

                mStationToList = null;
                mSelectedStationTo = null;
                mStationToSelection.setText(getContext().getString(R.string.loading));
                getStationToList(mSelectedStationFrom);
            }
        });

        mStationFromSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationFromSelectorDialog.show();
            }
        });
    }

    private void setStationToAdapter(List<String> thanaList) {
        stationToSelectorDialog = new SelectorDialogWithSearch(getContext(), getContext().getString(R.string.select_a_station), thanaList);
        stationToSelectorDialog.setOnResourceSelectedListener(new SelectorDialogWithSearch.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                mStationToSelection.setError(null);
                mStationToSelection.setText(name);
                mSelectedStationTo = name;
            }
        });

        mStationToSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationToSelectorDialog.show();
            }
        });
    }

    private void setAdultAdapter(List<String> classList) {
        adultSelectorDialog = new SelectorDialog(getContext(), getContext().getString(R.string.select_no_of_adult), classList);
        adultSelectorDialog.setOnResourceSelectedListener(new SelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                mAdultSelection.setError(null);
                mAdultSelection.setText(name);
                mSelectedAdult = Integer.valueOf(name);
                mChildSelection.setError(null);

                if (Integer.valueOf(mSelectedAdult) < Constants.MAX_TICKET && Integer.valueOf(mSelectedAdult) >0 ) {
                    mChildList = new ArrayList<>();
                    for (int i = 0; i <= Constants.MAX_TICKET - Integer.valueOf(mSelectedAdult); i++) {
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                        mChildList.add(String.valueOf(numberFormat.format(i)));
                    }
                    setChildAdapter(mChildList);

                    if(Integer.valueOf(mSelectedAdult) == 1){
                        mGenderEditTextProgressBar.setVisibility(View.VISIBLE);
                    }else{
                        mGenderEditTextProgressBar.setVisibility(View.GONE);
                    }
                }
            }

        });

        mAdultSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adultSelectorDialog.show();
            }
        });
    }

    private void setChildAdapter(List<String> classList) {
        childSelectorDialog = new SelectorDialog(getContext(), getContext().getString(R.string.select_no_of_child), classList);
        childSelectorDialog.setOnResourceSelectedListener(new SelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                mChildSelection.setError(null);
                mChildSelection.setText(name);
                mSelectedChild = Integer.valueOf(name);

                if((Integer.valueOf(mSelectedAdult) + Integer.valueOf(mSelectedChild) )== 1){
                    mGenderEditTextProgressBar.setVisibility(View.VISIBLE);
                }else{
                    mGenderEditTextProgressBar.setVisibility(View.GONE);
                }
            }
        });

        mChildSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childSelectorDialog.show();
            }
        });
    }

    private void setGenderAdapter(List<String> classList) {
        genderSelectorDialog = new SelectorDialog(getContext(), getContext().getString(R.string.select_a_gender), classList);
        genderSelectorDialog.setOnResourceSelectedListener(new SelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                mGenderSelection.setError(null);
                mGenderSelection.setText(name);
                if(name.equalsIgnoreCase(getString(R.string.male)))
                {
                    mSelectedGender = "M";
                }else if(name.equalsIgnoreCase(getString(R.string.female))){
                    mSelectedGender = "F";
                }else {
                    mSelectedGender = null;
                }
            }
        });

        mGenderSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderSelectorDialog.show();
            }
        });
    }

    protected void showErrorMessage(String errorMessage) {
        if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
            IPaySnackbar.error(mContinueButton, errorMessage, IPaySnackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetStationFromListAsyncTask = null;
            mGetStationToListAsyncTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_STATION_FROM)) {
            try {
                mGetStationFromResponse = gson.fromJson(result.getJsonString(), GetStationResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mStationFromList = mGetStationFromResponse.getStationList();
                    mStationFromEditTextProgressBar.hideProgressBar();
                    setStationFromAdapter(mStationFromList);
                    mStationFromSelection.setText(mSelectedStationFrom);
                    if (TextUtils.isEmpty(mSelectedStationFrom)) {
                        getStationToList(mSelectedStationFrom);
                    }

                } else {
                    if (getContext() != null)
                        Toaster.makeText(getContext(), R.string.failed_loading_station_list, Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.failed_loading_station_list, Toast.LENGTH_LONG);
            }

            mGetStationFromListAsyncTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_STATION_TO)) {
            try {
                mGetStationToResponse = gson.fromJson(result.getJsonString(), GetStationResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mStationToList = mGetStationToResponse.getStationList();
                    mStationToEditTextProgressBar.hideProgressBar();
                    mStationToSelection.setText("");
                    setStationToAdapter(mStationToList);
                    mStationToSelection.setText(mSelectedStationTo);

                } else {
                    if (getContext() != null)
                        Toaster.makeText(getContext(), R.string.failed_loading_station_list, Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.failed_loading_station_list, Toast.LENGTH_LONG);
            }

            mGetStationToListAsyncTask = null;
        }

    }
    public boolean verifyUserInputs() {
        boolean cancel = false;
        View focusedView = null;
        mStationFromSelection.setError(null);
        mStationToSelection.setError(null);
        mDateSelection.setError(null);
        mAdultSelection.setError(null);
        mChildSelection.setError(null);
        mGenderSelection.setError(null);

        if (mDateSelection.getText().toString().trim().length() == 0) {
            mDateSelection.setError(getContext().getString(R.string.invalid_journey_date));
            focusedView = mDateSelection;
            cancel = true;
        } else if (mStationFromSelection.getText().toString().trim().length() == 0) {
            mStationFromSelection.setError(getContext().getString(R.string.invalid_start_station));
            focusedView = mStationFromSelection;
            cancel = true;
        } else if (mStationToSelection.getText().toString().trim().length() == 0) {
            mStationToSelection.setError(getContext().getString(R.string.invalid_start_station));
            focusedView = mStationToSelection;
            cancel = true;
        } else if (mAdultSelection.getText().toString().trim().length() == 0) {
            mAdultSelection.setError(getContext().getString(R.string.invalid_no_of_ticket));
            focusedView = mAdultSelection;
            cancel = true;
        } else if ((TextUtils.isEmpty(mAdultSelection.getText().toString()) || Integer.valueOf(mAdultSelection.getText().toString())== 1)) {

            if ( (TextUtils.isEmpty(mChildSelection.getText().toString()) || Integer.valueOf(mChildSelection.getText().toString())== 0)) {

                if ((TextUtils.isEmpty(mGenderSelection.getText().toString()) || mGenderSelection.getText().toString().trim().length() == 0)) {
                    mGenderSelection.setError(getContext().getString(R.string.invalid_gender));
                    focusedView = mGenderSelection;
                    cancel = true;
                }
            }
        }

        if (cancel) {
            focusedView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void getAdultLIst(){
        mAdultList = new ArrayList<>();
        for(int i=1; i<=Constants.MAX_TICKET; i++){
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            mAdultList.add(String.valueOf(numberFormat.format(i)));
        }
    }

    public void getGenderLIst(){
        mGenderList = new ArrayList<>();
        mGenderList.add(getString(R.string.male));
        mGenderList.add(getString(R.string.female));
    }
}
