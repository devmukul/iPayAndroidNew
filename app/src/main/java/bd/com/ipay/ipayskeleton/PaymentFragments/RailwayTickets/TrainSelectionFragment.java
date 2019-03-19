package bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.RailwayTicketActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.SelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.GetTicketInfoRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.GetTicketInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.GetTrainListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.TrainList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.TicketDetailsDialog;

public class TrainSelectionFragment extends Fragment implements HttpResponseListener {
    private RecyclerView mTrainListRecyclerView;
    private ArrayList<TrainList> mTrainList;

    private HttpRequestGetAsyncTask mGetTrainListAsyncTask = null;
    private GetTrainListResponse mTrainResponse;

    private LinearLayout mProgressLayout;
    private TrainListAdapter trainListAdapter;
    private AnimatedProgressDialog mProgressDialog;

    private HttpRequestPostAsyncTask mGetTrainInfoAsyncTask = null;
    private GetTicketInfoResponse mTicketInfoResponse;
    private GetTicketInfoRequest mTicketInfoRequest;

    private String mSelectedStationFrom = null;
    private String mSelectedStationTo = null;
    private String mSelectedGender = null;
    private static int mSelectedDate;
    private int mSelectedAdult;
    private int mSelectedChild;

    private String mSelectedTrain = null;
    private String mSelectedClass = null;
    private int mSelectedTrainNo;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    TextView journeyInfo;
    TextView monthText;
    private static int selectedPos = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mSelectedStationFrom = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_STATION_FROM, "");
            mSelectedStationTo = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_STATION_TO, "");
            mSelectedGender = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_GENDER, "");
            mSelectedDate = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_DATE, 0);
            mSelectedAdult = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, 0);
            mSelectedChild = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((RailwayTicketActionActivity) getActivity()).setSupportActionBar(toolbar);
        ((RailwayTicketActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle(R.string.railway_ticket_title);
        mProgressDialog = new AnimatedProgressDialog(getActivity());
        mTrainListRecyclerView = view.findViewById(R.id.user_bank_list_recycler_view);
        mProgressLayout = view.findViewById(R.id.progress_layout);
        journeyInfo = view.findViewById(R.id.journey_info_text);
        monthText = view.findViewById(R.id.month_text);

        List<Date> dates = getDates();
        recyclerView = view.findViewById(R.id.date_view);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.scrollToPosition(selectedPos);
        MyRecyclerAdapter adapter = new MyRecyclerAdapter(dates);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        journeyInfo.setText(Utilities.formatJourneyInfoText(getContext(), mSelectedStationFrom + " " + getString(R.string.to_text) + " " + mSelectedStationTo, mSelectedAdult, mSelectedChild));

        trainListAdapter = new TrainListAdapter();
        mTrainListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTrainListRecyclerView.setAdapter(trainListAdapter);
        getTrainList(mSelectedStationFrom, mSelectedStationTo);
    }

    private void getTrainList(String originatingStation, String destinationStation) {
        if (mGetTrainListAsyncTask != null) {
            return;
        }
        mGetTrainListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRAIN_LIST,
                Constants.BASE_URL_CNS + Constants.URL_TRAIN_LIST + "originatingStation=" + originatingStation + "&destinationStation=" + destinationStation, getContext(), this, true);
        mGetTrainListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void performContinueAction(String ticketClass, int trainNumber) {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        } else if (mGetTrainInfoAsyncTask != null) {
            return;
        }
        String jsonBody = new Gson().toJson(new GetTicketInfoRequest(0, ticketClass, mSelectedGender, mSelectedDate,
                mSelectedAdult, mSelectedChild, mSelectedStationFrom, mSelectedStationTo, trainNumber));
        mGetTrainInfoAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_TICKET_INFO,
                Constants.BASE_URL_CNS + Constants.URL_TICKET_QUERY, jsonBody, getContext(), this, false);
        mGetTrainInfoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mProgressDialog.setTitle(R.string.please_wait_no_ellipsis);
        mProgressDialog.showDialog();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetTrainInfoAsyncTask = null;
            mGetTrainListAsyncTask = null;
            mProgressLayout.setVisibility(View.GONE);
            mProgressDialog.dismissDialog();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_TICKET_INFO)) {
            try {
                mGetTrainInfoAsyncTask = null;
                GetTicketInfoResponse getTicketInfoResponse = new Gson().fromJson(result.getJsonString(), GetTicketInfoResponse.class);
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        showTicketInfo(getTicketInfoResponse);
                        break;
                    default:
                        if (!TextUtils.isEmpty(getTicketInfoResponse.getMessage())) {
                            Toaster.makeText(getActivity(), getTicketInfoResponse.getMessage(), Toast.LENGTH_SHORT);
                        } else {
                            Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                        }
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.failed_loading_ticket_info, Toast.LENGTH_LONG);
            }

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            mGetTrainInfoAsyncTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_TRAIN_LIST)) {
            try {
                mTrainResponse = gson.fromJson(result.getJsonString(), GetTrainListResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mProgressLayout.setVisibility(View.GONE);
                    mTrainList = mTrainResponse.getTrainList();
                    mTrainListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mTrainListRecyclerView.setAdapter(trainListAdapter);
                    trainListAdapter.notifyDataSetChanged();

                } else {
                    if (getContext() != null)
                        Toaster.makeText(getContext(), R.string.failed_loading_train_list, Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.failed_loading_train_list, Toast.LENGTH_LONG);
            }

            mGetTrainListAsyncTask = null;
        }
        mProgressDialog.dismissDialog();
    }

    private void showTicketInfo(final GetTicketInfoResponse ticketInfoResponse) {
        if (getActivity() == null)
            return;

        String trainNo = String.valueOf(ticketInfoResponse.getTrainNumber());
        //String className = ticketInfoResponse.getClassName();

        final TicketDetailsDialog billDetailsDialog = new TicketDetailsDialog(getContext());
        billDetailsDialog.setTitle(getString(R.string.ticket_info));
        billDetailsDialog.setTrainName(mSelectedTrain + " - " + trainNo);
        billDetailsDialog.setClassName(mSelectedClass);
        billDetailsDialog.setAdultChild(Utilities.formatJourneyInfoText(getContext(), "", mSelectedAdult, mSelectedChild));
        billDetailsDialog.setDate(ticketInfoResponse.getJourneyDate());
        billDetailsDialog.setFareAmount(ticketInfoResponse.getFare());
        billDetailsDialog.setVatAmount(ticketInfoResponse.getVat());
        billDetailsDialog.setNetAmount(ticketInfoResponse.getTotalFare());

        billDetailsDialog.setCloseButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDetailsDialog.cancel();
            }
        });
        billDetailsDialog.setBuyTicketButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billDetailsDialog.cancel();
                Bundle bundle = new Bundle();
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NAME, mSelectedTrain);
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_CLASS_NAME, mSelectedClass);
                bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_FARE_AMOUNT, ticketInfoResponse.getFare());
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_GENDER, mSelectedGender);
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_DATE, mSelectedDate);
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, Integer.valueOf(mSelectedAdult));
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, Integer.valueOf(mSelectedChild));
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_FROM, mSelectedStationFrom);
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_TO, mSelectedStationTo);
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_TICKET_ID, ticketInfoResponse.getTicketId());
                bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_TOTAL_AMOUNT, ticketInfoResponse.getTotalFare());
                bundle.putString(RailwayTicketActionActivity.KEY_TICKET_MESSAGE_ID, ticketInfoResponse.getMessageId());
                bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NO, ticketInfoResponse.getTrainNumber());
                bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_VAT_AMOUNT, ticketInfoResponse.getVat());
                ((RailwayTicketActionActivity) getActivity()).
                        switchFragment(new TicketAmountInputFragment(), bundle, 3, true);
            }
        });
        billDetailsDialog.show();
    }

    public class TrainListAdapter extends RecyclerView.Adapter<TrainListAdapter.BankViewHolder> {

        @NonNull
        @Override
        public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_train_item, null, false);
            return new BankViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final BankViewHolder holder, final int position) {
            setClassAdapter(holder, position, mTrainList.get(position).getClassList());

            holder.trainTimeTextView.setText(mTrainList.get(position).getDepartureTime());
            holder.trainNameTextView.setText(mTrainList.get(position).getTrainName());
            holder.ticketClassTextView.setText(mTrainList.get(position).getClassList().get(0));

            holder.findTicket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mSelectedTrain = mTrainList.get(position).getTrainName();
                    mSelectedClass = holder.ticketClassTextView.getText().toString();
                    mSelectedTrainNo = mTrainList.get(position).getTrainNumber();
                    performContinueAction(mSelectedClass, mSelectedTrainNo);
                }
            });

        }

        private void setClassAdapter(final BankViewHolder holder, final int position, List<String> classList) {
            final SelectorDialog classSelectorDialog = new SelectorDialog(getContext(), getContext().getString(R.string.select_a_class), classList);
            classSelectorDialog.setOnResourceSelectedListener(new SelectorDialog.OnResourceSelectedListener() {
                @Override
                public void onResourceSelected(String name) {
                    holder.ticketClassTextView.setText(name);
                }
            });

            holder.ticketClassTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    classSelectorDialog.show();
                }
            });
        }


        @Override
        public int getItemCount() {
            if (mTrainList == null)
                return 0;
            return mTrainList.size();
        }

        public class BankViewHolder extends RecyclerView.ViewHolder {
            public TextView trainTimeTextView;
            public TextView trainNameTextView;
            public TextView ticketClassTextView;
            public Button findTicket;
            private View parentView;


            public BankViewHolder(View itemView) {
                super(itemView);
                trainTimeTextView = itemView.findViewById(R.id.train_time);
                trainNameTextView = itemView.findViewById(R.id.train_name);
                ticketClassTextView = itemView.findViewById(R.id.ticket_class);
                findTicket = itemView.findViewById(R.id.find_ticket_button);
                parentView = itemView;
            }
        }
    }

    public class MyRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<Date> listItems;

        public MyRecyclerAdapter(List<Date> listItems) {
            this.listItems = listItems;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_view_list_item, parent, false);
            return new VHItem(v);
        }

        private Date getItem(int position) {
            return listItems.get(position);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            Date currentItem = getItem(position);
            final VHItem VHitem = (VHItem) holder;
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentItem);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
            VHitem.dateText.setText("" + numberFormat.format(calendar.get(Calendar.DAY_OF_MONTH)));
            VHitem.dayText.setText("" + dayFormat.format(currentItem));
            if (mSelectedDate == calendar.get(Calendar.DAY_OF_MONTH)) {
                selectedPos = position;
                monthText.setText(monthFormat.format(currentItem) + ", " + yearFormat.format(currentItem));
                VHitem.dateText.setBackgroundResource(R.drawable.date_selector_background);
                recyclerView.smoothScrollToPosition(8);

            } else {
                VHitem.dateText.setBackgroundResource(R.color.colorTransparent);
            }

            VHitem.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPos = position;
                    mSelectedDate = calendar.get(Calendar.DAY_OF_MONTH);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        class VHItem extends RecyclerView.ViewHolder {

            View root;
            TextView dateText;
            TextView dayText;

            public VHItem(View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.date_text);
                dayText = itemView.findViewById(R.id.day_text);
                root = itemView.findViewById(R.id.root);
            }
        }
    }

    public static List<Date> getDates() {

        final Calendar cal = Calendar.getInstance();
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(cal.getTime());
        cal.add(Calendar.DATE, Constants.MAX_TICKET_PURCHASE_DAY + 1);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(cal.getTime());
        int i = 0;

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            if (calendar.get(Calendar.DAY_OF_MONTH) == mSelectedDate) {
                selectedPos = i;
            }
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
            i++;
        }
        return datesInRange;
    }
}
