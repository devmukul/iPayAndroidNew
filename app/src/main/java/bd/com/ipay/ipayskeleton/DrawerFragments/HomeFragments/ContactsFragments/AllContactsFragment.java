package bd.com.ipay.ipayskeleton.DrawerFragments.HomeFragments.ContactsFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.Friend.FriendNode;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;

public class AllContactsFragment extends BaseContactsFragment {

//    private HttpRequestGetAsyncTask mGetAllContactsTask;
//    private List<FriendNode> mGetAllContactsResponse;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        if (mGetAllContactsResponse == null) {
//            loadContacts();
//            setContentShown(false);
//        }
//        else {
//            setContentShown(true);
//        }

        List<FriendNode> phoneContacts = ContactEngine.getAllContacts(getActivity());
        populateList(phoneContacts);
    }

    @Override
    protected boolean isDialogFragment() {
        return false;
    }

    @Override
    protected boolean shouldShowIPayUserIcon() {
        return true;
    }

//    private void loadContacts() {
//        if (mGetAllContactsTask != null) {
//            return;
//        }
//
//        mGetAllContactsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_FRIENDS,
//                Constants.BASE_URL_FRIEND + Constants.URL_GET_CONTACTS, getActivity(), this);
//        mGetAllContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }
//
//    @Override
//    public void httpResponseReceiver(HttpResponseObject result) {
//        super.httpResponseReceiver(result);
//
//        if (result == null) {
//            mGetAllContactsTask = null;
//            return;
//        }
//
//
//        Gson gson = new Gson();
//
//        if (result.getApiCommand().equals(Constants.COMMAND_GET_FRIENDS)) {
//            try {
//                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
//                    FriendNode[] friendNodeArray = gson.fromJson(result.getJsonString(), FriendNode[].class);
//                    mGetAllContactsResponse = Arrays.asList(friendNodeArray);
//                    populateList(mGetAllContactsResponse);
//                } else {
//                    if (getActivity() != null) {
//                        Toast.makeText(getActivity(), R.string.failed_loading_friends, Toast.LENGTH_LONG).show();
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//
//                if (getActivity() != null) {
//                    Toast.makeText(getActivity(), R.string.failed_loading_friends, Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }
}