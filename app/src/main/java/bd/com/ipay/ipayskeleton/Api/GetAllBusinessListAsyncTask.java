package bd.com.ipay.ipayskeleton.Api;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.GetAllBusinessContactResponse;
import bd.com.ipay.ipayskeleton.Model.SqLiteDatabase.BusinessAccountEntry;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class GetAllBusinessListAsyncTask extends HttpRequestGetAsyncTask implements HttpResponseListener {
    GetAllBusinessContactResponse mBusinessContactResponse;
    List<BusinessAccountEntry> mBusinessContacts;
    private Context context;

    public GetAllBusinessListAsyncTask(Context context, String mUri) {
        super(Constants.COMMAND_GET_ALL_BUSINESS_LIST, mUri, context);
        mHttpResponseListener = this;
        this.context = context;
    }


    @Override
    public void httpResponseReceiver(HttpResponseObject result) {
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            if (getContext() != null) {
                Toast.makeText(getContext(), R.string.contacts_sync_failed, Toast.LENGTH_LONG).show();
                return;
            }
        }
        try {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                Gson gson = new Gson();
                mBusinessContactResponse = gson.fromJson(result.getJsonString(), GetAllBusinessContactResponse.class);
                mBusinessContacts = mBusinessContactResponse.getBusinessContacts();

                // Save the list fetched from the server into the database
                DataHelper dataHelper = DataHelper.getInstance(context);
                dataHelper.createBusinessAccounts(mBusinessContacts);
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.contacts_sync_failed, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (getContext() != null) {
                Toast.makeText(getContext(), R.string.contacts_sync_failed, Toast.LENGTH_LONG).show();
            }
        }

    }
}
