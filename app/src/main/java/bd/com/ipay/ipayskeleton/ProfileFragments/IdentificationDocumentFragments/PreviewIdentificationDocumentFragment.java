package bd.com.ipay.ipayskeleton.ProfileFragments.IdentificationDocumentFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.DocumentPreviewImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.DocumentPage;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.IdentificationDocument;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.IdentificationDocumentConstants;

public class PreviewIdentificationDocumentFragment extends BaseFragment {

    private IdentificationDocument mSelectedIdentificationDocument;
    private DocumentPreviewImageView documentPreviewImageView;
    private CustomProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedIdentificationDocument = getArguments().getParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT);
        }
        mProgressDialog = new CustomProgressDialog(getContext());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_identification_document, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mSelectedIdentificationDocument != null)
            getActivity().setTitle(mSelectedIdentificationDocument.getDocumentTypeTitle());

        final TextView documentTypeNameTextView = findViewById(R.id.document_type_name_text_view);
        final TextView documentIdTextView = findViewById(R.id.document_id_text_view);
        final TextView documentTypePreviewTextView = findViewById(R.id.document_type_preview_text_view);
        final RecyclerView documentPreviewRecyclerView = findViewById(R.id.document_preview_recycler_view);
        final ImageButton documentInformationEditImageButton = findViewById(R.id.document_information_edit_image_button);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        documentPreviewImageView = findViewById(R.id.document_preview_image_view);

        documentPreviewImageView.setVisibility(View.GONE);
        documentPreviewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentPreviewImageView.setVisibility(View.GONE);
            }
        });

        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        documentPreviewRecyclerView.setLayoutManager(linearLayoutManager);

        if (mSelectedIdentificationDocument != null) {
            documentTypeNameTextView.setText(mSelectedIdentificationDocument.getDocumentTypeTitle());
            documentIdTextView.setText(mSelectedIdentificationDocument.getDocumentIdNumber());
            documentTypePreviewTextView.setText(String.format(Locale.getDefault(), "%s "+ getString(R.string.preview), getString(R.string.document)));
            if (mSelectedIdentificationDocument.getDocumentVerificationStatus().equals(IdentificationDocumentConstants.DOCUMENT_VERIFICATION_STATUS_VERIFIED) ||
                    ( !mSelectedIdentificationDocument.getDocumentType().equals(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID) && !ProfileInfoCacheManager.isBusinessAccount())) {
                documentInformationEditImageButton.setVisibility(View.GONE);
            } else {
                documentInformationEditImageButton.setVisibility(View.VISIBLE);
                documentInformationEditImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.SELECTED_IDENTIFICATION_DOCUMENT, mSelectedIdentificationDocument);
                        if (getActivity() instanceof ProfileActivity) {
                            ((ProfileActivity) getActivity()).switchToUploadIdentificationDocumentFragment(bundle);
                        }
                    }
                });
            }
            final DocumentPreviewAdapter documentPreviewAdapter = new DocumentPreviewAdapter(getContext(), mSelectedIdentificationDocument.getDocumentPages());
            documentPreviewRecyclerView.setAdapter(documentPreviewAdapter);
        }
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    public boolean isDocumentViewOpen() {
        return documentPreviewImageView.getVisibility() == View.VISIBLE;
    }

    public void hideDocumentPreview() {
        documentPreviewImageView.setVisibility(View.GONE);
    }

    class DocumentPreviewAdapter extends RecyclerView.Adapter<DocumentPreviewAdapter.DocumentPreviewViewPager> {

        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private List<DocumentPage> mDocumentPageList;

        private static final int FIRST_PAGE = 0;
        private static final int SECOND_PAGE = 1;

        public DocumentPreviewAdapter(Context context, List<DocumentPage> documentPageList) {
            this.mContext = context;
            this.mDocumentPageList = new ArrayList<>(documentPageList);
            mLayoutInflater = LayoutInflater.from(this.mContext);
        }

        @Override
        public DocumentPreviewViewPager onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DocumentPreviewViewPager(mLayoutInflater.inflate(R.layout.list_item_document_preview, parent, false));
        }

        @Override
        public void onBindViewHolder(final DocumentPreviewViewPager holder, int position) {
            final DocumentPage documentPage = mDocumentPageList.get(position);
            final @DrawableRes int previewImageResId;
            switch (position) {
                case SECOND_PAGE:
                    previewImageResId = R.drawable.icon_id_card_back;
                    break;
                case FIRST_PAGE:
                default:
                    previewImageResId = R.drawable.icon_id_card_front;
                    break;
            }
            holder.documentImageView.setImageResource(previewImageResId);
            if (!TextUtils.isEmpty(documentPage.getUrl())) {
                Glide.with(this.mContext).load(Constants.BASE_URL_FTP_SERVER + documentPage.getUrl())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                holder.documentImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mProgressDialog.setCancelable(false);
                                        mProgressDialog.show();
                                        Glide.with(PreviewIdentificationDocumentFragment.this).load(Constants.BASE_URL_FTP_SERVER + documentPage.getUrl()).listener(new RequestListener<String, GlideDrawable>() {
                                            @Override
                                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                                mProgressDialog.cancel();
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                                mProgressDialog.cancel();
                                                documentPreviewImageView.setVisibility(View.VISIBLE);
                                                return false;
                                            }
                                        }).into(documentPreviewImageView.getImageView());
                                    }
                                });
                                return false;
                            }
                        }).crossFade().into(holder.documentImageView);
            }
        }

        @Override
        public int getItemCount() {
            if (mDocumentPageList == null || mDocumentPageList.isEmpty())
                return 0;
            else
                return mDocumentPageList.size();
        }

        class DocumentPreviewViewPager extends RecyclerView.ViewHolder {

            private ImageView documentImageView;

            public DocumentPreviewViewPager(View itemView) {
                super(itemView);
                documentImageView = findViewById(R.id.document_image_view);
            }

            public <T extends View> T findViewById(@IdRes int id) {
                //noinspection unchecked,ConstantConditions
                return (T) itemView.findViewById(id);
            }
        }
    }
}
