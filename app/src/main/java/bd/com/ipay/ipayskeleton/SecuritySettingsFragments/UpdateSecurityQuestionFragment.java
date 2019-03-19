package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.PreviousSecurityQuestionClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.UpdateSecurityAnswerRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.UpdateSecurityAnswerResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Security.UpdateSecurityQuestionAnswerClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class UpdateSecurityQuestionFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPutAsyncTask mUpdateSecurityAnswerTask = null;
    private UpdateSecurityAnswerResponse mUpdateSecurityAnswerResponse;

    private CustomProgressDialog mProgressDialog;

    private List<String> mAllSecurityQuestionClassList, mTempSecurityQuestionClassList;

    private List<PreviousSecurityQuestionClass> mPreviousQuestionClassList;
    private List<UpdateSecurityQuestionAnswerClass> mUpdateQuestionAnswerClassList;

    private CustomSelectorDialog questionClassResourceSelectorDialog;

    private EditText mQuestionEditText;
    private EditText mAnswerEditText;
    private EditText mPasswordEditText;
    private Button mUpdateButton;

    private int mQuestionID;
    private String mAnswer;
    private String mPassword;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_update_security_question));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_update_security_question, container, false);

        setTitle();

        Bundle bundle = getArguments();

        mQuestionID = bundle.getInt(Constants.QUESTION_ID);
        mPreviousQuestionClassList = bundle.getParcelableArrayList(Constants.PREVIOUS_QUESTION);
        mAllSecurityQuestionClassList = bundle.getStringArrayList(Constants.All_QUESTIONS);

        mQuestionEditText = (EditText) v.findViewById(R.id.question);
        mAnswerEditText = (EditText) v.findViewById(R.id.answer);
        mPasswordEditText = (EditText) v.findViewById(R.id.password);
        mUpdateButton = (Button) v.findViewById(R.id.button_update);

        mProgressDialog = new CustomProgressDialog(getActivity());

        setQuestion();
        setQuestionAdapter();

        mQuestionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionClassResourceSelectorDialog.show();
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyQuestionAnswers()) {

                    UpdateSecurityQuestionAnswerClass updateSecurityAnswerClass = new UpdateSecurityQuestionAnswerClass();
                    updateSecurityAnswerClass.setId(mPreviousQuestionClassList.get(mQuestionID).getId());
                    updateSecurityAnswerClass.setQuestion(mQuestionEditText.getText().toString());
                    updateSecurityAnswerClass.setAnswer(mAnswerEditText.getText().toString());

                    mUpdateQuestionAnswerClassList = new ArrayList<>();
                    mUpdateQuestionAnswerClassList.add(updateSecurityAnswerClass);

                    attemptSaveSecurityAnswers(mPassword);
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setTitle() {
        getActivity().setTitle(R.string.update_security_questions);
    }

    private void setQuestion() {
        mQuestionEditText.setText(mPreviousQuestionClassList.get(mQuestionID).getQuestion());
    }

    private void setQuestionAdapter() {

        questionClassResourceSelectorDialog = new CustomSelectorDialog(getActivity(),
                getString(R.string.select_a_question), removeOtherSelectedQuestions(), mQuestionID);
        questionClassResourceSelectorDialog.
                setOnResourceSelectedListenerWithSelectedPosition(new CustomSelectorDialog.OnResourceSelectedListenerWithPosition() {
                    @Override
                    public void onResourceSelectedWithPosition(int id, String question, int position) {

                        mQuestionEditText.setText(question);
                    }
                });

    }

    private boolean verifyQuestionAnswers() {
        boolean isValid = true;
        View focusView = null;
        mAnswer = mAnswerEditText.getText().toString().trim();
        mPassword = mPasswordEditText.getText().toString().trim();

        if (mAnswer.isEmpty()) {
            mAnswerEditText.setError(getString(R.string.enter_answer));
            isValid = false;
            focusView = mAnswerEditText;
        } else if (mPassword.isEmpty()) {
            mPasswordEditText.setError(getString(R.string.enter_current_pass));
            isValid = false;
            focusView = mPasswordEditText;
        }

        if (!isValid) {
            focusView.requestFocus();
        }

        return isValid;
    }

    private List<String> removeOtherSelectedQuestions() {
        List<String> selectedOtherQuestionList = new ArrayList<>();

        for (int i = 0; i < mPreviousQuestionClassList.size(); i++) {
            if (i != mQuestionID) {
                if (mPreviousQuestionClassList.get(i).getQuestion() != null &&
                        mAllSecurityQuestionClassList.contains(mPreviousQuestionClassList.get(i).getQuestion())) {
                    selectedOtherQuestionList.add(mPreviousQuestionClassList.get(i).getQuestion());
                }
            }
        }

        mTempSecurityQuestionClassList = new ArrayList<>();
        mTempSecurityQuestionClassList.addAll(mAllSecurityQuestionClassList);
        mTempSecurityQuestionClassList.removeAll(selectedOtherQuestionList);
        return mTempSecurityQuestionClassList;
    }

    private void attemptSaveSecurityAnswers(String password) {
        mProgressDialog.show();

        UpdateSecurityAnswerRequest updateSecurityAnswerRequest = new UpdateSecurityAnswerRequest(mUpdateQuestionAnswerClassList, password);
        Gson gson = new Gson();
        String json = gson.toJson(updateSecurityAnswerRequest, UpdateSecurityAnswerRequest.class);
        mUpdateSecurityAnswerTask = new HttpRequestPutAsyncTask(Constants.COMMAND_UPDATE_SECURITY_ANSWERS,
                Constants.BASE_URL_MM + Constants.URL_SET_SECURITY_ANSWERS, json, getActivity(), false);
        mUpdateSecurityAnswerTask.mHttpResponseListener = this;
        mUpdateSecurityAnswerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mUpdateSecurityAnswerTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_UPDATE_SECURITY_ANSWERS)) {
            try {
                mUpdateSecurityAnswerResponse = gson.fromJson(result.getJsonString(), UpdateSecurityAnswerResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), mUpdateSecurityAnswerResponse.getMessage(), Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null)
                        ((MyApplication) (getActivity().getApplication())).launchLoginPage(mUpdateSecurityAnswerResponse.getMessage());
                    Utilities.sendBlockedEventTracker(mTracker, "Security Question", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mUpdateSecurityAnswerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.security_answer_set_failed, Toast.LENGTH_SHORT).show();
            }

            mUpdateSecurityAnswerTask = null;
            mProgressDialog.dismiss();
        }

    }


}


