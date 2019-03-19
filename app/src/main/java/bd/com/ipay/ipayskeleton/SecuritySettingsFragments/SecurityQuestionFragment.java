package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.AddSecurityQuestionAnswerClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.GetAllSecurityQuestionRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.GetAllSecurityQuestionResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.GetPreviousSelectedSecurityQuestionRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.GetPreviousSelectedSecurityQuestionResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.PreviousSecurityQuestionClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.SecurityQuestionValidationClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.SetSecurityAnswerRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.SetSecurityAnswerResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SecurityQuestionFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetAllSecurityQuestionTask = null;
    private GetAllSecurityQuestionResponse mAllSecurityQuestionResponse;

    private HttpRequestGetAsyncTask mGetPreviousSelectedSecurityQuestionTask = null;
    private GetPreviousSelectedSecurityQuestionResponse mPreviousSelectedSecurityQuestionResponse;

    private HttpRequestPostAsyncTask mSetSecurityAnswerTask = null;
    private SetSecurityAnswerResponse mSetSecurityAnswerResponse;

    private CustomProgressDialog mProgressDialog;

    private RecyclerView mSecurityQuesRecyclerView;
    private SecurityQuestionAdapter mSecurityQuestionAnswerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<String> mAllSecurityQuestionClassList, mTempSecurityQuestionClassList;

    private List<AddSecurityQuestionAnswerClass> mAddSecurityQuestionAnswerClassList;
    private List<PreviousSecurityQuestionClass> mPreviousQuestionClassList;
    private List<SecurityQuestionValidationClass> mSecurityQuestionAnswerValidationClassList;

    private CustomSelectorDialog questionClassSelectorDialog;

    private TextView mEmptyListTextView;

    private int mRequiredQuestions;

    private String mPassword;

    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_set_security_question, container, false);

        setTitle();
        mSecurityQuesRecyclerView = (RecyclerView) v.findViewById(R.id.list_security_questions);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mSecurityQuestionAnswerAdapter = new SecurityQuestionAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSecurityQuesRecyclerView.setLayoutManager(mLayoutManager);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mPreviousQuestionClassList = new ArrayList<>();
        getAllSecurityQuestions();

        return v;
    }

    private void setTitle() {
        getActivity().setTitle(R.string.security_questions);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mPreviousQuestionClassList.isEmpty() || mPreviousQuestionClassList.size() != 0)
            getAllSecurityQuestions();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_security_question));
    }

    private void getAllSecurityQuestions() {
        if (mGetAllSecurityQuestionTask != null) {
            return;
        }
        GetAllSecurityQuestionRequestBuilder getSecurityQuestionBuilder = new GetAllSecurityQuestionRequestBuilder();
        String url = getSecurityQuestionBuilder.getGeneratedUri();
        mGetAllSecurityQuestionTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ALL_SECURITY_QUESTIONS,
                url, getActivity(), false);
        mGetAllSecurityQuestionTask.mHttpResponseListener = this;
        mGetAllSecurityQuestionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getPreviousSelectedSecurityQuestions() {
        if (mGetPreviousSelectedSecurityQuestionTask != null) {
            return;
        }
        GetPreviousSelectedSecurityQuestionRequestBuilder getSecurityQuestionBuilder = new GetPreviousSelectedSecurityQuestionRequestBuilder();
        String url = getSecurityQuestionBuilder.getGeneratedUri();
        mGetPreviousSelectedSecurityQuestionTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SELECTED_SECURITY_QUESTIONS,
                url, getActivity(), true);
        mGetPreviousSelectedSecurityQuestionTask.mHttpResponseListener = this;
        mGetPreviousSelectedSecurityQuestionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setSecurityQuestionAnswerList() {
        mSecurityQuestionAnswerValidationClassList = new ArrayList<>();
        mAddSecurityQuestionAnswerClassList = new ArrayList<>();


        for (int mIndex = 0; mIndex < mRequiredQuestions; mIndex++) {
            mSecurityQuestionAnswerValidationClassList.add(new SecurityQuestionValidationClass());
            mAddSecurityQuestionAnswerClassList.add(new AddSecurityQuestionAnswerClass());
        }

        mSecurityQuesRecyclerView.setAdapter(mSecurityQuestionAnswerAdapter);
    }

    private boolean verifyQuestionAnswers() {
        boolean isValid = true;
        for (int mIndex = 0; mIndex < mSecurityQuestionAnswerValidationClassList.size(); mIndex++) {
            if (mSecurityQuestionAnswerValidationClassList.get(mIndex).getQuestion() == null) {
                isValid = false;
                mSecurityQuestionAnswerValidationClassList.get(mIndex).setQuestionAvailable(false);

            }
            if (mAddSecurityQuestionAnswerClassList.get(mIndex).getAnswer() == null) {
                isValid = false;
                mSecurityQuestionAnswerValidationClassList.get(mIndex).setAnswerAvailable(false);
            }
        }
        if (!isValid) {
            mSecurityQuestionAnswerAdapter.notifyDataSetChanged();
        }
        return isValid;

    }

    private void attemptSaveSecurityQuestionAnswers(String password) {
        mProgressDialog.show();

        SetSecurityAnswerRequest setSecurityAnswerRequest = new SetSecurityAnswerRequest(mAddSecurityQuestionAnswerClassList, password);
        Gson gson = new Gson();
        String json = gson.toJson(setSecurityAnswerRequest, SetSecurityAnswerRequest.class);
        mSetSecurityAnswerTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SET_SECURITY_ANSWERS,
                Constants.BASE_URL_MM + Constants.URL_SET_SECURITY_ANSWERS, json, getActivity(), false);
        mSetSecurityAnswerTask.mHttpResponseListener = this;
        mSetSecurityAnswerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private List<String> removeOtherSelectedQuestions(int index) {
        List<String> selectedOtherQuestionList = new ArrayList<>();

        for (int i = 0; i < mSecurityQuestionAnswerValidationClassList.size(); i++) {
            if (i != index) {
                if (mSecurityQuestionAnswerValidationClassList.get(i).getQuestion() != null &&
                        mAllSecurityQuestionClassList.contains(mSecurityQuestionAnswerValidationClassList.get(i).getQuestion())) {
                    selectedOtherQuestionList.add(mSecurityQuestionAnswerValidationClassList.get(i).getQuestion());
                }
            }
        }

        mTempSecurityQuestionClassList = new ArrayList<>();
        mTempSecurityQuestionClassList.addAll(mAllSecurityQuestionClassList);
        mTempSecurityQuestionClassList.removeAll(selectedOtherQuestionList);
        return mTempSecurityQuestionClassList;
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mGetAllSecurityQuestionTask = null;
            mGetPreviousSelectedSecurityQuestionTask = null;
            mSetSecurityAnswerTask = null;
            setContentShown(true);
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_ALL_SECURITY_QUESTIONS)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mAllSecurityQuestionResponse = gson.fromJson(result.getJsonString(), GetAllSecurityQuestionResponse.class);
                    mAllSecurityQuestionClassList = mAllSecurityQuestionResponse.getList();
                    mRequiredQuestions = mAllSecurityQuestionResponse.getRequired();
                    if (mRequiredQuestions > 0) {
                        getPreviousSelectedSecurityQuestions();
                    } else mEmptyListTextView.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.security_question_get_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.security_question_get_failed, Toast.LENGTH_LONG).show();
            }

            mGetAllSecurityQuestionTask = null;
        }
        if (result.getApiCommand().equals(Constants.COMMAND_GET_SELECTED_SECURITY_QUESTIONS)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mPreviousSelectedSecurityQuestionResponse = gson.fromJson(result.getJsonString(), GetPreviousSelectedSecurityQuestionResponse.class);
                    mPreviousQuestionClassList = mPreviousSelectedSecurityQuestionResponse.getList();
                    if (mPreviousQuestionClassList != null || !mPreviousQuestionClassList.isEmpty())
                        setSecurityQuestionAnswerList();
                    mSecurityQuestionAnswerAdapter.notifyDataSetChanged();
                    setContentShown(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.security_question_get_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.security_question_get_failed, Toast.LENGTH_LONG).show();
            }

            mGetPreviousSelectedSecurityQuestionTask = null;
        }

        if (result.getApiCommand().equals(Constants.COMMAND_SET_SECURITY_ANSWERS)) {
            try {
                mSetSecurityAnswerResponse = gson.fromJson(result.getJsonString(), SetSecurityAnswerResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), mSetSecurityAnswerResponse.getMessage(), Toast.LENGTH_LONG).show();
                    getPreviousSelectedSecurityQuestions();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null)
                        ((MyApplication) (getActivity().getApplication())).launchLoginPage(mSetSecurityAnswerResponse.getMessage());
                    Utilities.sendBlockedEventTracker(mTracker, "Updating Security Question", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mSetSecurityAnswerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.security_answer_set_failed, Toast.LENGTH_SHORT).show();
            }

            mSetSecurityAnswerTask = null;
            mProgressDialog.dismiss();
        }

    }

    public class SecurityQuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int PASSWORD_VIEW = 2;
        private static final int FOOTER_VIEW = 3;
        private static final int SECURITY_QUESTION_ONLY_VIEW = 4;
        private static final int SECURITY_QUESTION_ANSWER_LIST_ITEM_VIEW = 1;

        private TextView mQuestionTextView;
        private EditText mPasswordView;
        private Button mSaveButton;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            if (viewType == SECURITY_QUESTION_ONLY_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_security_question_only, parent, false);
                return new SecurityQuestionListHolder(v);
            } else if (viewType == FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_security_question_footer, parent, false);
                return new SecurityQuestionAdapter.FooterViewHolder(v);
            } else if (viewType == PASSWORD_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_security_question_password, parent, false);
                return new SecurityQuestionAdapter.HeaderViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_security_question, parent, false);
                SecurityQuestionAnswerListHolder vh = new SecurityQuestionAnswerListHolder(v, new CustomWatcher());
                return vh;
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            try {
                if (holder instanceof SecurityQuestionAnswerListHolder) {
                    SecurityQuestionAnswerListHolder vh = (SecurityQuestionAnswerListHolder) holder;
                    // So that it knows what item in data set to update
                    vh.customWatcher.updatePosition(holder.getAdapterPosition());
                    vh.bindSecurityQuestionAnswerView(position);
                } else if (holder instanceof SecurityQuestionAdapter.FooterViewHolder) {
                    SecurityQuestionAdapter.FooterViewHolder vh = (SecurityQuestionAdapter.FooterViewHolder) holder;
                    vh.bindView();
                } else if (holder instanceof SecurityQuestionListHolder) {
                    SecurityQuestionListHolder vh = (SecurityQuestionListHolder) holder;
                    vh.bindSecurityQuestionView(position);
                } else if (holder instanceof SecurityQuestionAdapter.HeaderViewHolder) {
                    SecurityQuestionAdapter.HeaderViewHolder vh = (SecurityQuestionAdapter.HeaderViewHolder) holder;
                    vh.bindView();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mPreviousQuestionClassList.isEmpty() || mPreviousQuestionClassList.size() == 0) {
                if (mSecurityQuestionAnswerValidationClassList.size() == 0)
                    return 0;
                else
                    return mSecurityQuestionAnswerValidationClassList.size() + 2;
            } else return mRequiredQuestions;
        }

        @Override
        public int getItemViewType(int position) {

            if (mPreviousQuestionClassList.isEmpty() || mPreviousQuestionClassList.size() == 0) {
                if (position == getItemCount() - 1) {
                    return FOOTER_VIEW;
                } else if (position == getItemCount() - 2) {
                    return PASSWORD_VIEW;
                } else {
                    return SECURITY_QUESTION_ANSWER_LIST_ITEM_VIEW;
                }
            } else
                return SECURITY_QUESTION_ONLY_VIEW;
        }

        public class SecurityQuestionAnswerListHolder extends RecyclerView.ViewHolder {
            private final EditText mQuestionEditText;
            private final EditText mAnswerEditText;
            private CustomWatcher customWatcher;

            public SecurityQuestionAnswerListHolder(final View itemView, CustomWatcher customWatcher) {
                super(itemView);

                mQuestionEditText = (EditText) itemView.findViewById(R.id.question);
                mAnswerEditText = (EditText) itemView.findViewById(R.id.answer);
                this.customWatcher = customWatcher;
                this.mAnswerEditText.addTextChangedListener(customWatcher);
            }

            public void bindSecurityQuestionAnswerView(final int pos) {

                if (!mSecurityQuestionAnswerValidationClassList.get(pos).isQuestionAvailable())
                    mQuestionEditText.setError(getString(R.string.enter_question));
                else
                    mQuestionEditText.setError(null);

                if (!mSecurityQuestionAnswerValidationClassList.get(pos).isAnswerAvailable())
                    mAnswerEditText.setError(getString(R.string.enter_answer));
                else
                    mAnswerEditText.setError(null);

                if (mSecurityQuestionAnswerValidationClassList.get(pos).getQuestion() != null)
                    mQuestionEditText.setText(mSecurityQuestionAnswerValidationClassList.get(pos).getQuestion());
                else
                    mQuestionEditText.setText("");

                if (mAddSecurityQuestionAnswerClassList.get(pos).getAnswer() != null)
                    mAnswerEditText.setText(mAddSecurityQuestionAnswerClassList.get(pos).getAnswer());
                else
                    mAnswerEditText.setText("");

                mQuestionEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        questionClassSelectorDialog = new CustomSelectorDialog(getActivity(),
                                getString(R.string.select_a_question), removeOtherSelectedQuestions(pos), pos);
                        questionClassSelectorDialog.
                                setOnResourceSelectedListenerWithSelectedPosition(new CustomSelectorDialog.OnResourceSelectedListenerWithPosition() {
                                    @Override
                                    public void onResourceSelectedWithPosition(int id, String question, int position) {
                                        mSecurityQuestionAnswerValidationClassList.get(position).setQuestion(question);
                                        mSecurityQuestionAnswerValidationClassList.get(position).setQuestionAvailable(true);

                                        mAddSecurityQuestionAnswerClassList.get(position).setQuestion(question);
                                        mSecurityQuestionAnswerAdapter.notifyDataSetChanged();
                                    }
                                });
                        questionClassSelectorDialog.show();
                    }
                });
            }

        }

        public class SecurityQuestionListHolder extends RecyclerView.ViewHolder {

            public SecurityQuestionListHolder(final View itemView) {
                super(itemView);

                mQuestionTextView = (TextView) itemView.findViewById(R.id.questionview);
            }

            public void bindSecurityQuestionView(final int pos) {

                if (mPreviousQuestionClassList.get(pos).getQuestion() != null)
                    mQuestionTextView.setText(mPreviousQuestionClassList.get(pos).getQuestion());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_SECURITY_QUESTIONS)
                    public void onClick(View v) {

                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.QUESTION_ID, pos);
                        bundle.putParcelableArrayList(Constants.PREVIOUS_QUESTION, new ArrayList<>(mPreviousQuestionClassList));
                        bundle.putStringArrayList(Constants.All_QUESTIONS, new ArrayList<>(mAllSecurityQuestionClassList));

                        ((SecuritySettingsActivity) getActivity()).switchToUpdateSecurityQuestionFragment(bundle);
                    }
                });
            }

        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {

            public HeaderViewHolder(View itemView) {
                super(itemView);

                mPasswordView = (EditText) itemView.findViewById(R.id.password);
            }

            public void bindView() {
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);

                mSaveButton = (Button) itemView.findViewById(R.id.button_save);
            }

            public void bindView() {

                mSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @ValidateAccess(ServiceIdConstants.MANAGE_SECURITY_QUESTIONS)
                    public void onClick(View v) {
                        if (mPasswordView.getText().toString().isEmpty())
                            mPasswordView.setError(getString(R.string.enter_current_pass));

                        if (verifyQuestionAnswers() && !mPasswordView.getText().toString().isEmpty()) {
                            Utilities.hideKeyboard(getActivity());
                            mPassword = mPasswordView.getText().toString();
                            attemptSaveSecurityQuestionAnswers(mPassword);
                        }
                    }
                });

            }
        }

        public class CustomWatcher implements TextWatcher {

            private int position;

            public CustomWatcher() {
            }

            public void updatePosition(int position) {
                this.position = position;
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mAddSecurityQuestionAnswerClassList.get(position).setAnswer(editable.toString());
                    mSecurityQuestionAnswerValidationClassList.get(position).setAnswerAvailable(true);
                } else {
                    mAddSecurityQuestionAnswerClassList.get(position).setAnswer(null);
                    mSecurityQuestionAnswerValidationClassList.get(position).setAnswerAvailable(false);
                }

            }
        }
    }


}
