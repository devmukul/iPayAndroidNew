package bd.com.ipay.ipayskeleton.Api;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.Model.Friend.AddFriendRequest;
import bd.com.ipay.ipayskeleton.Model.Friend.AddFriendResponse;
import bd.com.ipay.ipayskeleton.Model.Friend.FriendInfo;
import bd.com.ipay.ipayskeleton.Model.Friend.InfoAddFriend;
import bd.com.ipay.ipayskeleton.Model.Friend.InfoUpdateFriend;
import bd.com.ipay.ipayskeleton.Model.Friend.UpdateFriendRequest;
import bd.com.ipay.ipayskeleton.Model.Friend.UpdateFriendResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;

public class SyncContactsAsyncTask extends AsyncTask<String, Void, ContactEngine.ContactDiff> implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAddFriendAsyncTask;
    private AddFriendResponse mAddFriendResponse;

    private HttpRequestPostAsyncTask mUpdateFriendAsyncTask;
    private UpdateFriendResponse mUpdateFriendResponse;

    private static boolean contactsSyncedOnce;

    private final Context context;
    private final List<FriendInfo> serverContacts;

    public SyncContactsAsyncTask(Context context, List<FriendInfo> serverContacts) {
        this.context = context;
        this.serverContacts = serverContacts;
    }

    @Override
    protected ContactEngine.ContactDiff doInBackground(String... params) {

        // Save the friend list fetched from the server into the database
        DataHelper dataHelper = DataHelper.getInstance(context);
        dataHelper.createFriends(serverContacts);

        // IMPORTANT: Perform this check only after saving all server contacts into the database!
        if (contactsSyncedOnce)
            return null;
        else
            contactsSyncedOnce = true;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            // Read phone contacts
            List<FriendInfo> phoneContacts = ContactEngine.getAllContacts(context);

            // Calculate the difference between phone contacts and server contacts
            ContactEngine.ContactDiff contactDiff = ContactEngine.getContactDiff(phoneContacts, serverContacts);

            Log.i("New Contacts", contactDiff.newContacts.toString());
            Log.i("Updated Contacts", contactDiff.updatedContacts.toString());

            return contactDiff;
        } else {
            return null;
        }

    }

    @Override
    protected void onPostExecute(ContactEngine.ContactDiff contactDiff) {
        if (contactDiff != null) {
            addFriends(contactDiff.newContacts);
            updateFriends(contactDiff.updatedContacts);
        }

    }

    private void addFriends(List<FriendInfo> friends) {
        if (mAddFriendAsyncTask != null) {
            return;
        }

        if (friends.isEmpty())
            return;

        List<InfoAddFriend> newFriends = new ArrayList<>();
        for (FriendInfo friendNode : friends) {
            newFriends.add(new InfoAddFriend(friendNode.getMobileNumber(), friendNode.getName()));
        }

        AddFriendRequest addFriendRequest = new AddFriendRequest(newFriends);
        Gson gson = new Gson();
        String json = gson.toJson(addFriendRequest);

        mAddFriendAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_FRIENDS,
                Constants.BASE_URL_FRIEND + Constants.URL_ADD_CONTACTS, json, context, this);
        mAddFriendAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void updateFriends(List<FriendInfo> friends) {
        if (mUpdateFriendResponse != null) {
            return;
        }

        if (friends.isEmpty())
            return;

        List<InfoUpdateFriend> updateFriends = new ArrayList<>();
        for (FriendInfo friend : friends) {
            InfoUpdateFriend infoUpdateFriend = new InfoUpdateFriend(
                    friend.getMobileNumber(), friend.getName());
            updateFriends.add(infoUpdateFriend);
        }

        UpdateFriendRequest updateFriendRequest = new UpdateFriendRequest(updateFriends);
        Gson gson = new Gson();
        String json = gson.toJson(updateFriendRequest);

        mUpdateFriendAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_UPDATE_FRIENDS,
                Constants.BASE_URL_FRIEND + Constants.URL_UPDATE_CONTACTS, json, context, this);
        mUpdateFriendAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mAddFriendAsyncTask = null;
            mUpdateFriendAsyncTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_FRIENDS)) {
            try {
                mAddFriendResponse = gson.fromJson(result.getJsonString(), AddFriendResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    // Server contacts updated, download contacts again
                    Log.i("Friend", "Create friend successful");
                    new GetFriendsAsyncTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    if (mAddFriendResponse != null)
                        Log.e(context.getString(R.string.failed_add_friend), mAddFriendResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mAddFriendAsyncTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_UPDATE_FRIENDS)) {
            try {
                mUpdateFriendResponse = gson.fromJson(result.getJsonString(), UpdateFriendResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Log.i("Friend", "Update friend successful");
                    // Maybe we should download contacts again?
                } else {
                    Log.e(context.getString(R.string.failed_update_friend), mUpdateFriendResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mUpdateFriendAsyncTask = null;
        }
    }
}