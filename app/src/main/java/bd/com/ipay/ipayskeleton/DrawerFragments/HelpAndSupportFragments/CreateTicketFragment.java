package bd.com.ipay.ipayskeleton.DrawerFragments.HelpAndSupportFragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.HelpAndSupportActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadTicketAttachmentAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomDrawable;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomUploadPickerDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.ResourceSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.EditTextWithProgressBar;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Email.GetEmailResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.TicketCategory;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.CommentIdWithDocumentList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.CreateTicketRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.CreateTicketResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.GetTicketCategoriesRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.GetTicketCategoryResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Ticket.TicketWithCommentId;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DocumentPicker;
import bd.com.ipay.ipayskeleton.Utilities.MultipleImagePicker;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class CreateTicketFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mCreateTicketTask = null;
    private CreateTicketResponse mCreateTicketResponse;

    private HttpRequestGetAsyncTask mGetTicketCategoriesTask = null;
    private GetTicketCategoryResponse mGetTicketCategoriesResponse;

    private HttpRequestGetAsyncTask mGetEmailsTask = null;
    private GetEmailResponse mGetEmailResponse;

    private EditText mMessageEditText;
    private EditText mSubjectEditText;
    private EditTextWithProgressBar mCategoryEditText;
    private Button mCreateTicketButton;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mFileAttachmentRecyclerView;

    private View view;
    private CustomProgressDialog mProgressDialog;

    private ArrayList<String> attachedFiles;

    private String mSubject;
    private String mMessage;
    private long mCommentId;
    private int mPickerActionId = -1;


    private String mSelectedCategoryCode;
    private int mSelectedCategoryId;
    private List<TicketCategory> mTicketCategoryList;
    private ResourceSelectorDialog<TicketCategory> ticketCategorySelectorDialog;

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final int REQUEST_CODE_PICK_MULTIPLE_IMAGE = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE_OR_DOCUMENT = 1002;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_create_ticket));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_ticket, container, false);

        setTitle();

        mSubjectEditText = (EditText) view.findViewById(R.id.subject_edit_text);
        mCategoryEditText = (EditTextWithProgressBar) view.findViewById(R.id.category_edit_text);
        mMessageEditText = (EditText) view.findViewById(R.id.message_edit_text);
        mCreateTicketButton = (Button) view.findViewById(R.id.button_create_ticket);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mCreateTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyUserInputs()) {
                    Utilities.hideKeyboard(getActivity());
                    createTicket();
                }
            }
        });

        getTicketCategories();
        setFileAttachmentAdapter();
        return view;
    }

    private void setTitle() {
        getActivity().setTitle(R.string.help);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getEmails();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                    selectDocument(mPickerActionId);
                else
                    Toast.makeText(getActivity(), R.string.prompt_grant_permission, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE_OR_DOCUMENT:
                if (resultCode == Activity.RESULT_OK) {
                    String filePath = DocumentPicker.getFilePathForCameraOrPDFResult(getActivity(), data);

                    if (filePath != null) {
                        Random r = new Random();
                        int fileIndex = r.nextInt(100 - 1) + 1;
                        Uri mSelectedDocumentUri = DocumentPicker.getDocumentUriWithIndexFromResult(getActivity(), resultCode, data, fileIndex);
                        if (mSelectedDocumentUri != null)
                            attachedFiles.add(mSelectedDocumentUri.getPath());
                        else attachedFiles.add(filePath);

                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;

            case REQUEST_CODE_PICK_MULTIPLE_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = (ArrayList<Image>) ImagePicker.getImages(data);
                    if (images != null) setImagePathsFromMultiplePicker(images);
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTicketCategoryAdapter(List<TicketCategory> mTicketCategoryList) {
        ticketCategorySelectorDialog = new ResourceSelectorDialog<>(getContext(), getString(R.string.select_category), mTicketCategoryList, mSelectedCategoryId, true);
        ticketCategorySelectorDialog.setOnResourceSelectedListenerWithStringID(new ResourceSelectorDialog.OnResourceSelectedListenerWithStringID() {
            @Override
            public void onResourceSelectedWithStringID(String code, String name, int selectedIndex) {
                mCategoryEditText.setText(name);
                mSelectedCategoryCode = code;
                mSelectedCategoryId = selectedIndex;
            }
        });

        mCategoryEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ticketCategorySelectorDialog.show();
            }
        });
    }

    private void processGetEmailListResponse(String json) {
        try {
            Gson gson = new Gson();
            mGetEmailResponse = gson.fromJson(json, GetEmailResponse.class);

            if (mGetEmailResponse.getEmailAdressList().isEmpty()) {
                MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
                dialog
                        .title(R.string.no_email_added)
                        .content(R.string.dialog_add_new_email)
                        .cancelable(false)
                        .positiveText(R.string.add_email)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                launchEmailPage();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ((HelpAndSupportActivity) getActivity()).switchToTicketListFragment();
                            }
                        })
                        .show();
            } else {
                String primaryEmail = mGetEmailResponse.getVerifiedEmail();
                if (primaryEmail == null) {
                    MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
                    dialog
                            .title(R.string.no_primary_email)
                            .content(R.string.dialog_verify_email)
                            .positiveText(R.string.verify_email)
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    launchEmailPage();
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ((HelpAndSupportActivity) getActivity()).switchToTicketListFragment();
                                }
                            })
                            .show();
                }
            }

            setContentShown(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchEmailPage() {
        getActivity().onBackPressed();
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(Constants.TARGET_FRAGMENT, ProfileCompletionPropertyConstants.VERIFIED_EMAIL);
        startActivity(intent);
    }

    private void showCreateTicketSuccessDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
        dialog
                .title(R.string.ticket_created)
                .content(R.string.ticket_created_dialog_text)
                .cancelable(false)
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getActivity().onBackPressed();
                    }
                })
                .show();
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;

        mSubject = mSubjectEditText.getText().toString();
        mMessage = mMessageEditText.getText().toString();

        if (mSubject.isEmpty()) {
            cancel = true;
            focusView = mSubjectEditText;
            mSubjectEditText.setError(getString(R.string.failed_empty_subject));
        }
        if (mMessage.isEmpty()) {
            cancel = true;
            focusView = mMessageEditText;
            mMessageEditText.setError(getString(R.string.failed_empty_message));
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void setFileAttachmentAdapter() {
        attachedFiles = new ArrayList<>();
        mFileAttachmentRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mFileAttachmentRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mFileAttachmentRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAttachmentAdapter();
        mFileAttachmentRecyclerView.setAdapter(mAdapter);
    }

    private void uploadMultipleAttachmentsAsyncTask() {
        for (String attachedFile : attachedFiles) {
            if (!attachedFile.isEmpty())
                uploadAttachement(attachedFile);
        }
    }

    private void getTicketCategories() {
        if (mGetTicketCategoriesTask != null)
            return;

        mCategoryEditText.showProgressBar();
        GetTicketCategoriesRequestBuilder mGetTicketCategoriesRequestBuilder = new GetTicketCategoriesRequestBuilder();
        String mUri = mGetTicketCategoriesRequestBuilder.generateUri().toString();

        mGetTicketCategoriesTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TICKET_CATEGORIES,
                mUri, getActivity(), false);
        mGetTicketCategoriesTask.mHttpResponseListener = this;

        mGetTicketCategoriesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void createTicket() {
        if (mCreateTicketTask != null)
            return;

        mProgressDialog.show();

        CreateTicketRequest createTicketRequest = new CreateTicketRequest(mSubject, mSelectedCategoryCode, mMessage);

        Gson gson = new Gson();
        String json = gson.toJson(createTicketRequest);

        mCreateTicketTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CREATE_TICKET,
                Constants.BASE_URL_ADMIN + Constants.URL_CREATE_TICKET, json, getActivity(), this, false);
        mCreateTicketTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getEmails() {
        if (mGetEmailsTask != null) {
            return;
        }

        setContentShown(false);

        mGetEmailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_EMAILS,
                Constants.BASE_URL_MM + Constants.URL_GET_EMAIL, getActivity(), this, true);
        mGetEmailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void uploadAttachement(String filePath) {
        UploadTicketAttachmentAsyncTask mUploadTicketAttachmentAsyncTask = new UploadTicketAttachmentAsyncTask(
                Constants.COMMAND_ADD_ATTACHMENT, filePath, mCommentId, getActivity());
        mUploadTicketAttachmentAsyncTask.mHttpResponseListener = this;
        mUploadTicketAttachmentAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() != null)
            mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mCreateTicketTask = null;
            mGetEmailsTask = null;
            mGetTicketCategoriesTask = null;
            setContentShown(true);
            mCategoryEditText.hideProgressBar();

            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_CREATE_TICKET:
                try {
                    mCreateTicketResponse = gson.fromJson(result.getJsonString(), CreateTicketResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), R.string.ticket_created, Toast.LENGTH_LONG);
                            TicketWithCommentId ticketResponseWithCommentId = mCreateTicketResponse.getResponse();
                            CommentIdWithDocumentList commentIdWithDocumentList = ticketResponseWithCommentId.getTicket();
                            mCommentId = commentIdWithDocumentList.getComment_id();
                            if (attachedFiles.size() > 0) uploadMultipleAttachmentsAsyncTask();

                            showCreateTicketSuccessDialog();
                        }
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_PAYMENT_REQUIRED) {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), R.string.no_email_added, Toast.LENGTH_LONG);
                            launchEmailPage();
                        }
                    } else {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), mCreateTicketResponse.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), mCreateTicketResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                }
                mCreateTicketTask = null;
                break;
            case Constants.COMMAND_GET_EMAILS:
                try {
                    mGetEmailResponse = gson.fromJson(result.getJsonString(), GetEmailResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        processGetEmailListResponse(result.getJsonString());
                    } else {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG);
                            getActivity().onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG);
                        getActivity().onBackPressed();
                    }
                }
                mGetEmailsTask = null;
                break;
            case Constants.COMMAND_GET_TICKET_CATEGORIES:
                try {
                    mGetTicketCategoriesResponse = gson.fromJson(result.getJsonString(), GetTicketCategoryResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mTicketCategoryList = mGetTicketCategoriesResponse.getTicketCategories();

                        setTicketCategoryAdapter(mTicketCategoryList);
                        mCategoryEditText.hideProgressBar();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mGetTicketCategoriesTask = null;
                break;

            default:
                break;
        }
    }

    private class FileAttachmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int ATTACH_NEW_FILE_VIEW = 1;

        public class AttachedFileViewHolder extends RecyclerView.ViewHolder {
            private ImageView mFileView;
            private ImageView mRemoveAttachedFileButton;
            private File mFile;
            private Bitmap mBitmap;
            private String attachFileName;

            public AttachedFileViewHolder(final View itemView) {
                super(itemView);

                mFileView = (ImageView) itemView.findViewById(R.id.attached_file_view);
                mRemoveAttachedFileButton = (ImageView) itemView.findViewById(R.id.button_remove_attached_file);
            }

            public void bindViewAttachedFile(final int pos) {
                if (attachedFiles.size() < Constants.MAX_FILE_ATTACHMENT_LIMIT)
                    attachFileName = attachedFiles.get(pos - 1);
                else attachFileName = attachedFiles.get(pos);

                CustomDrawable.getCustomFileThumbnailView(getActivity(), mFileView, attachFileName);

                mRemoveAttachedFileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (attachedFiles.size() < Constants.MAX_FILE_ATTACHMENT_LIMIT)
                            attachedFiles.remove(pos - 1);
                        else attachedFiles.remove(pos);

                        notifyDataSetChanged();
                    }
                });
            }
        }

        public class AttachNewFileViewHolder extends RecyclerView.ViewHolder {
            private ImageView mAttachNewFileView;
            private CustomUploadPickerDialog customUploadPickerDialog;
            private List<String> mPickerList;

            public AttachNewFileViewHolder(View itemView) {
                super(itemView);
                mAttachNewFileView = (ImageView) itemView.findViewById(R.id.button_select_file);
            }

            public void bindViewAttachNewFile(final int pos) {
                mPickerList = Arrays.asList(getResources().getStringArray(R.array.attach_file_picker_action));

                mAttachNewFileView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customUploadPickerDialog = new CustomUploadPickerDialog(getActivity(), getString(R.string.select_a_document), mPickerList);
                        customUploadPickerDialog.setOnResourceSelectedListener(new CustomUploadPickerDialog.OnResourceSelectedListener() {
                            @Override
                            public void onResourceSelected(int mActionId, String action) {
                                if (!Constants.ACTION_TYPE_SELECT_FROM_GALLERY.equals(action)) {
                                    if (Utilities.isNecessaryPermissionExists(getActivity(), DocumentPicker.DOCUMENT_PICK_PERMISSIONS))
                                        selectDocument(mActionId);
                                    else {
                                        mPickerActionId = mActionId;
                                        Utilities.requestRequiredPermissions(CreateTicketFragment.this, REQUEST_CODE_PERMISSION, DocumentPicker.DOCUMENT_PICK_PERMISSIONS);
                                    }
                                } else {
                                    setMultipleImagePicker();
                                }
                            }
                        });
                        customUploadPickerDialog.show();
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == ATTACH_NEW_FILE_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_attach_new_file, parent, false);
                return new AttachNewFileViewHolder(v);
            }

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file_attachment, parent, false);
            return new AttachedFileViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof AttachedFileViewHolder) {
                    AttachedFileViewHolder vh = (AttachedFileViewHolder) holder;
                    vh.bindViewAttachedFile(position);
                } else if (holder instanceof AttachNewFileViewHolder) {
                    AttachNewFileViewHolder vh = (AttachNewFileViewHolder) holder;
                    vh.bindViewAttachNewFile(position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (attachedFiles == null)
                return 1;
            else if (attachedFiles.size() < Constants.MAX_FILE_ATTACHMENT_LIMIT)
                return attachedFiles.size() + 1;
            return attachedFiles.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (attachedFiles == null || attachedFiles.size() < Constants.MAX_FILE_ATTACHMENT_LIMIT) {
                if (position == 0) {
                    return ATTACH_NEW_FILE_VIEW;
                }
                return super.getItemViewType(position);
            }
            return super.getItemViewType(position);
        }
    }

    private void selectDocument(int id) {
        Intent imagePickerIntent = DocumentPicker.getPickImageOrPDFIntentByID(getActivity(), getString(R.string.select_a_document), id);
        startActivityForResult(imagePickerIntent, REQUEST_CODE_PICK_IMAGE_OR_DOCUMENT);
    }

    private void setImagePathsFromMultiplePicker(List<Image> images) {
        for (int i = 0; i < images.size(); i++)
            attachedFiles.add(images.get(i).getPath());
        mAdapter.notifyDataSetChanged();
    }

    private void setMultipleImagePicker() {
        Intent intent = MultipleImagePicker.getMultipleImagePickerIntent(getActivity(), Constants.MAX_FILE_ATTACHMENT_LIMIT - attachedFiles.size());
        startActivityForResult(intent, REQUEST_CODE_PICK_MULTIPLE_IMAGE);
    }
}
