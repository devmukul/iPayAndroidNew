package bd.com.ipay.ipayskeleton.SourceOfFund.view;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.Utilities.Constants;


public class SponsorSelectorDialog {
    private Context context;
    private View headerView;
    private View bodyView;

    private ImageView cancelButton;

    private AlertDialog sponsorSelectorDialog;

    private ArrayList<Sponsor> sponsorArrayList;

    private SponsorSelectorListener sponsorSelectorListener;

    private RecyclerView sponsorListRecyclerView;


    public SponsorSelectorDialog(Context context, ArrayList<Sponsor> sponsorArrayList, SponsorSelectorListener sponsorSelectorListener) {
        this.context = context;
        this.sponsorArrayList = sponsorArrayList;
        this.sponsorSelectorListener = sponsorSelectorListener;
        createViews();

    }

    private void createViews() {
        headerView = LayoutInflater.from(context).inflate(R.layout.header_sponsor_dialog, null, false);
        bodyView = LayoutInflater.from(context).inflate(R.layout.main_view_sponsor_dialog, null, false);
        sponsorListRecyclerView = bodyView.findViewById(R.id.sponsor_list);
        sponsorListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        sponsorListRecyclerView.setAdapter(new SponsorListAdapter());
        cancelButton = (ImageView) headerView.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sponsorSelectorDialog != null) {
                    sponsorSelectorDialog.dismiss();
                }

            }
        });
        sponsorSelectorDialog = new AlertDialog.Builder(context)
                .setCustomTitle(headerView)
                .setView(bodyView)
                .setCancelable(false)
                .create();

        sponsorSelectorDialog.show();

    }

    public class SponsorListAdapter extends RecyclerView.Adapter<SponsorListAdapter.SponsorViewHolder> {

        @NonNull
        @Override
        public SponsorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SponsorViewHolder(LayoutInflater.from
                    (context).inflate(R.layout.list_sponsor_dialog, null, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final SponsorViewHolder holder, final int position) {
            holder.numberView.setText(sponsorArrayList.get(position).getUser().getMobileNumber());
            holder.nameView.setText(sponsorArrayList.get(position).getUser().getName());
            String profileImageUri = sponsorArrayList.get(position).getUser().getProfilePictureUrl();
            if (profileImageUri != null) {
                if (profileImageUri.contains("ipay.com")) {
                } else {
                    profileImageUri = Constants.BASE_URL_FTP_SERVER + profileImageUri;
                }
            }
            Glide.with(context)
                    .load(profileImageUri)
                    .centerCrop()
                    .error(context.getResources().getDrawable(R.drawable.user_brand_bg))
                    .into(holder.profilePictureImageView);
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.selectorRadioButton.setChecked(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sponsorSelectorListener.onSponsorSelected(sponsorArrayList.get(position));
                            SponsorSelectorDialog.this.sponsorSelectorDialog.dismiss();
                        }
                    }, 500);

                }
            });
            holder.selectorRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.selectorRadioButton.setChecked(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sponsorSelectorListener.onSponsorSelected(sponsorArrayList.get(position));
                            SponsorSelectorDialog.this.sponsorSelectorDialog.dismiss();
                        }
                    }, 500);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (sponsorArrayList != null) {
                return sponsorArrayList.size();
            } else {
                return 0;
            }
        }

        public class SponsorViewHolder extends RecyclerView.ViewHolder {
            private RoundedImageView profilePictureImageView;
            private TextView nameView;
            private TextView numberView;
            private RadioButton selectorRadioButton;
            private View parentView;

            public SponsorViewHolder(View itemView) {
                super(itemView);
                profilePictureImageView = (RoundedImageView) itemView.findViewById(R.id.profile_image);
                nameView = (TextView) itemView.findViewById(R.id.name);
                numberView = (TextView) itemView.findViewById(R.id.number);
                selectorRadioButton = (RadioButton) itemView.findViewById(R.id.radio);
                parentView = itemView;
            }
        }
    }

    public interface SponsorSelectorListener {
        void onSponsorSelected(Sponsor sponsor);
    }
}
