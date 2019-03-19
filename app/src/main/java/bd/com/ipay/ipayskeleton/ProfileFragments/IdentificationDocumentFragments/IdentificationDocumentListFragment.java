package bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.GetIdentificationDocumentResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.IdentificationDocument;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.IdentificationDocumentConstants;

public class IdentificationDocumentListFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestAsyncTask mGetIdentificationDocumentsTask;

    private DocumentListAdapter mDocumentListAdapter;

    private List<IdentificationDocument> mUserIdentificationDocumentList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUserIdentificationList();
    }

    private void createUserIdentificationList() {
        final String[] documentTypeName;
        final String[] documentType;

        if (ProfileInfoCacheManager.isBusinessAccount()) {
            documentTypeName = getResources().getStringArray(R.array.business_document_id_name);
            documentType = IdentificationDocumentConstants.BUSINESS_DOCUMENT_TYPES;
        } else {
            documentTypeName = getResources().getStringArray(R.array.personal_document_id_name);
            documentType = IdentificationDocumentConstants.PERSONAL_DOCUMENT_TYPES;
        }
        mUserIdentificationDocumentList = new ArrayList<>();

        for (int i = 0; i < documentTypeName.length; i++) {
            IdentificationDocument identificationDocument = new IdentificationDocument();
            identificationDocument.setDocumentTypeTitle(documentTypeName[i]);
            identificationDocument.setDocumentType(documentType[i]);
            mUserIdentificationDocumentList.add(identificationDocument);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_identification_document_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.profile_documents);

        final RecyclerView identificationDocumentRecyclerView = findViewById(R.id.identification_document_recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        final String url;

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        identificationDocumentRecyclerView.setLayoutManager(linearLayoutManager);
        mDocumentListAdapter = new DocumentListAdapter(getContext(), mUserIdentificationDocumentList);
        identificationDocumentRecyclerView.setAdapter(mDocumentListAdapter);


        if (ProfileInfoCacheManager.isBusinessAccount())
            url = Constants.BASE_URL_MM + Constants.URL_GET_BUSINESS_DOCUMENTS_v2;
        else
            url = Constants.BASE_URL_MM + Constants.URL_GET_DOCUMENTS_v2;

        mGetIdentificationDocumentsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_IDENTIFICATION_DOCUMENTS_REQUEST, url, getContext(), this, false);
        mGetIdentificationDocumentsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        setContentShown(false);
    }

    private void updateIdentificationDocumentList(List<IdentificationDocument> serverIdentificationDocumentList) {
        for (int i = 0; i < serverIdentificationDocumentList.size(); i++) {
            for (int j = 0; j < mUserIdentificationDocumentList.size(); j++) {
                if (serverIdentificationDocumentList.get(i).getDocumentType().equals(mUserIdentificationDocumentList.get(j).getDocumentType())) {
                    mUserIdentificationDocumentList.get(j).setDocumentPages(serverIdentificationDocumentList.get(i).getDocumentPages());
                    mUserIdentificationDocumentList.get(j).setDocumentVerificationStatus(serverIdentificationDocumentList.get(i).getDocumentVerificationStatus());
                    mUserIdentificationDocumentList.get(j).setDocumentIdNumber(serverIdentificationDocumentList.get(i).getDocumentIdNumber());
                    if (serverIdentificationDocumentList.get(i).getDocumentName() != null) {
                        mUserIdentificationDocumentList.get(j).setDocumentName(serverIdentificationDocumentList.get(i).getDocumentName());
                    }
                    break;
                }
            }
        }
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    private void performOnItemClickAction(final IdentificationDocument identificationDocument) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT, identificationDocument);
        if (identificationDocument.getDocumentVerificationStatus() == null) {
            ((ProfileActivity) getActivity()).switchToUploadIdentificationDocumentFragment(bundle);
        } else {
            ((ProfileActivity) getActivity()).switchToPreviewIdentificationDocumentFragment(bundle);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        try {
            if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
                mGetIdentificationDocumentsTask = null;
                setContentShown(true);
                return;
            }

            switch (result.getApiCommand()) {
                case Constants.COMMAND_GET_IDENTIFICATION_DOCUMENTS_REQUEST:
                    Gson gson = new GsonBuilder().create();
                    GetIdentificationDocumentResponse identificationDocumentResponse = gson.fromJson(result.getJsonString(), GetIdentificationDocumentResponse.class);
                    switch (result.getStatus()) {
                        case Constants.HTTP_RESPONSE_STATUS_OK:
                            updateIdentificationDocumentList(identificationDocumentResponse.getDocuments());
                            mDocumentListAdapter.updateIdentificationDocumentList();
                            mDocumentListAdapter.notifyDataSetChanged();
                            setContentShown(true);
                            break;
                        default:
                            // OTP has not been expired yet
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), identificationDocumentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                default:
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }catch (Exception e){
            mGetIdentificationDocumentsTask = null;
        }

    }


    class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.DocumentViewHolder> {

        private LayoutInflater mLayoutInflater;
        private Context mContext;
        private final List<IdentificationDocument> mIdentificationDocumentList;

        DocumentListAdapter(Context context, List<IdentificationDocument> identificationDocumentList) {
            this.mContext = context;


            if (ProfileInfoCacheManager.isBusinessAccount()) {
                this.mIdentificationDocumentList = new ArrayList<>(identificationDocumentList);
            } else {
                this.mIdentificationDocumentList = new ArrayList();
                for (IdentificationDocument identificationDocument: identificationDocumentList){
                    if(identificationDocument.getDocumentVerificationStatus()!=null ||
                            identificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID)){
                        this.mIdentificationDocumentList.add(identificationDocument);
                    }
                }
            }

            mLayoutInflater = LayoutInflater.from(this.mContext);
        }

        @Override
        public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DocumentViewHolder(mLayoutInflater.inflate(R.layout.list_item_new_document, parent, false));
        }

        @Override
        public void onBindViewHolder(final DocumentViewHolder holder, int position) {
            final IdentificationDocument identificationDocument = mIdentificationDocumentList.get(position);

            // Showing the name of the Document
            holder.documentTypeNameTextView.setText(identificationDocument.getDocumentTypeTitle());

            // Showing the submitted document id number
            if (identificationDocument.getDocumentIdNumber() == null) {
                holder.documentIdTextView.setText(R.string.not_submitted);
            } else {
                holder.documentIdTextView.setText(identificationDocument.getDocumentIdNumber());
            }

            // Showing if the document is already verified or not.
            if (identificationDocument.getDocumentVerificationStatus() == null) {
                holder.verificationStatusImageView.setVisibility(View.GONE);
            } else if (identificationDocument.getDocumentVerificationStatus().equals(IdentificationDocumentConstants.DOCUMENT_VERIFICATION_STATUS_NOT_VERIFIED)) {
                holder.verificationStatusImageView.setVisibility(View.VISIBLE);
                holder.verificationStatusImageView.setImageResource(R.drawable.ic_workinprogress);
            } else if (identificationDocument.getDocumentVerificationStatus().equals(IdentificationDocumentConstants.DOCUMENT_VERIFICATION_STATUS_VERIFIED)) {
                holder.verificationStatusImageView.setVisibility(View.VISIBLE);
                holder.verificationStatusImageView.setImageResource(R.drawable.ic_verified);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performOnItemClickAction(getItem(holder.getAdapterPosition()));
                }
            });
        }

        public void updateIdentificationDocumentList() {
            this.mIdentificationDocumentList.clear();
            addItems(mUserIdentificationDocumentList);
        }

        public void addItems(List<IdentificationDocument> identificationDocumentList) {
            if (ProfileInfoCacheManager.isBusinessAccount()) {
                this.mIdentificationDocumentList.addAll(identificationDocumentList);
            } else {
                for (IdentificationDocument identificationDocument: identificationDocumentList){
                    if(identificationDocument.getDocumentVerificationStatus()!=null ||
                            identificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID)){
                        this.mIdentificationDocumentList.add(identificationDocument);
                    }
                }
            }
        }

        public void addItem(IdentificationDocument identificationDocument) {
            this.mIdentificationDocumentList.add(identificationDocument);
        }

        public IdentificationDocument getItem(int position) {
            return this.mIdentificationDocumentList.get(position);
        }

        @Override
        public int getItemCount() {
            if (mIdentificationDocumentList == null || mIdentificationDocumentList.isEmpty())
                return 0;
            else
                return mIdentificationDocumentList.size();
        }

        class DocumentViewHolder extends RecyclerView.ViewHolder {

            private TextView documentTypeNameTextView;
            private TextView documentIdTextView;
            private ImageView verificationStatusImageView;

            DocumentViewHolder(View itemView) {
                super(itemView);
                documentTypeNameTextView = findViewById(R.id.document_type_name_text_view);
                documentIdTextView = findViewById(R.id.document_id_text_view);
                verificationStatusImageView = findViewById(R.id.verification_status_image_view);
            }

            public <T extends View> T findViewById(@IdRes int id) {
                //noinspection unchecked
                return (T) itemView.findViewById(id);
            }
        }
    }
}
