package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ProfileCompletionFragment extends ProgressFragment implements HttpResponseListener {
	private RecyclerView mProfileCompletionRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private ProfileCompletionAdapter mProfileCompletionAdapter;

	private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
	private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

	private TextView mProfileCompletionStatusView;
	private ProgressBar mProfileCompletionStatusProgressBar;
	private TextView mProfileCompletionInformationView;
	private Tracker mTracker;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTracker = Utilities.getTracker(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_profile_completion));
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_profile_completion, container, false);

		getActivity().setTitle(getString(R.string.profile_completeness));

		mProfileCompletionRecyclerView = v.findViewById(R.id.list_profile_completion);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mProfileCompletionRecyclerView.setLayoutManager(mLayoutManager);

		mProfileCompletionStatusView = v.findViewById(R.id.textview_profile_completion_status);
		mProfileCompletionInformationView = v.findViewById(R.id.textview_profile_completion_title);
		mProfileCompletionStatusProgressBar = v.findViewById(R.id.progress_bar_profile_completion_status);
		getProfileCompletionStatus();

		return v;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setContentShown(false);
	}

	private void populateView() {
		mProfileCompletionStatusResponse.analyzeProfileCompletionData();
		mProfileCompletionAdapter = new ProfileCompletionAdapter();
		mProfileCompletionRecyclerView.setAdapter(mProfileCompletionAdapter);

		mProfileCompletionStatusView.setText(getString(R.string.profile_completeness_percentage, mProfileCompletionStatusResponse.getCompletionPercentage()));
		mProfileCompletionStatusProgressBar.setProgress(mProfileCompletionStatusResponse.getCompletionPercentage());
		if (!mProfileCompletionStatusResponse.getAnalyzedProfileVerificationMessage().isEmpty()) {

			mProfileCompletionInformationView.setText(getString(R.string.profile_completeness_information,mProfileCompletionStatusResponse.getAnalyzedProfileVerificationMessage()));
		} else mProfileCompletionInformationView.setVisibility(View.GONE);

		if (this.isAdded()) setContentShown(true);
	}

	private void getProfileCompletionStatus() {
		if (mGetProfileCompletionStatusTask != null) {
			return;
		}

		mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
				Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this, false);
		mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
			mGetProfileCompletionStatusTask = null;
			setContentShown(true);
			return;
		}


		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS)) {
			try {
				mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					populateView();
				} else {
					if (getActivity() != null)
						Toaster.makeText(getActivity(), mProfileCompletionStatusResponse.getMessage(), Toast.LENGTH_LONG);
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.failed_fetching_profile_completion_status, Toast.LENGTH_LONG).show();
			}

			mGetProfileCompletionStatusTask = null;
		}
	}

	private class ProfileCompletionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private static final int VIEW_TYPE_BASIC_INFO = 0;
		private static final int VIEW_TYPE_ADDRESS = 1;
		private static final int VIEW_TYPE_IDENTIFICATION = 2;
		private static final int VIEW_TYPE_ADD_BANK = 3;
		private static final int VIEW_TYPE_HEADER = 100;

		public abstract class ProfileCompletionViewHolder extends RecyclerView.ViewHolder {
			private final View itemView;
			private final ImageView profileCompletionIcon;
			private final TextView titleView;
			private final TextView currentStatusView;
			private final ImageView verificationStatus;

			public ProfileCompletionViewHolder(final View itemView) {
				super(itemView);

				this.itemView = itemView;
				profileCompletionIcon = itemView.findViewById(R.id.profile_completion_icon);
				titleView = itemView.findViewById(R.id.textview_title);
				currentStatusView = itemView.findViewById(R.id.textview_current_status);
				verificationStatus = itemView.findViewById(R.id.verification_status);
			}

			public abstract void bindViewProfileCompletion(int position);

			public void bindViewProfileCompletion(final ProfileCompletionStatusResponse.PropertyDetails propertyDetails) {
				if (propertyDetails.getPropertyIcon() != null) {
					profileCompletionIcon.setImageDrawable(getResources().getDrawable(propertyDetails.getPropertyIcon()));
				}
				titleView.setText(propertyDetails.getPropertyTitle());

				if (!propertyDetails.isCompleted() && propertyDetails.getThreshold() > 1) {
					currentStatusView.setText(propertyDetails.getValue() + "/" + propertyDetails.getThreshold() + " Completed");
					currentStatusView.setVisibility(View.VISIBLE);
				} else {
					currentStatusView.setVisibility(View.GONE);
				}

				if (propertyDetails.isCompleted()) {
					verificationStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_tick));
					verificationStatus.setVisibility(View.VISIBLE);
				} else {
					titleView.setTextColor(getResources().getColor(R.color.colorTextPrimary));
					verificationStatus.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_invite));
					verificationStatus.setVisibility(View.GONE);
				}

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (propertyDetails.getPropertyName().equals(ProfileCompletionPropertyConstants.PROFILE_PICTURE) && ProfileInfoCacheManager.isAccountVerified()) {
							DialogUtils.showProfilePictureUpdateRestrictionDialog(getContext());
						} else {
							((ProfileActivity) getActivity()).switchToFragment(propertyDetails.getPropertyName(), null, true);
						}
					}
				});
			}
		}

		public class BasicInfoViewHolder extends ProfileCompletionViewHolder {

			public BasicInfoViewHolder(View itemView) {
				super(itemView);
			}

			@Override
			public void bindViewProfileCompletion(int position) {
				bindViewProfileCompletion(mProfileCompletionStatusResponse.getBasicInfoCompletionDetails().get(position));
			}
		}

		public class AddressViewHolder extends ProfileCompletionViewHolder {

			public AddressViewHolder(View itemView) {
				super(itemView);
			}

			@Override
			public void bindViewProfileCompletion(int position) {
				bindViewProfileCompletion(mProfileCompletionStatusResponse.getAddressCompletionDetails().get(position));
			}
		}

		public class IdentificationViewHolder extends ProfileCompletionViewHolder {

			public IdentificationViewHolder(View itemView) {
				super(itemView);
			}

			@Override
			public void bindViewProfileCompletion(int position) {
				bindViewProfileCompletion(mProfileCompletionStatusResponse.getIdentificationCompletionDetails().get(position));
			}
		}

		public class AddBankViewHolder extends ProfileCompletionViewHolder {

			public AddBankViewHolder(View itemView) {
				super(itemView);
			}

			@Override
			public void bindViewProfileCompletion(int position) {
				bindViewProfileCompletion(mProfileCompletionStatusResponse.getAddBankCompletionDetails().get(position));
			}
		}

		public class HeaderViewHolder extends RecyclerView.ViewHolder {
			private final TextView headerView;

			public HeaderViewHolder(final View itemView) {
				super(itemView);

				headerView = itemView.findViewById(R.id.textview_header);
			}

			public void bindViewHeader(String title) {
				headerView.setText(title);
			}
		}

		@Override
		public int getItemViewType(int position) {
			int tempPosition = 0;

			// Basic Info Header
			tempPosition += 1;
			if (position < tempPosition) {
				return VIEW_TYPE_HEADER;
			}

			// Basic Info list items
			tempPosition += mProfileCompletionStatusResponse.getBasicInfoCompletionDetails().size();
			if (position < tempPosition) {
				return VIEW_TYPE_BASIC_INFO;
			}

			// Address Header
			tempPosition += 1;
			if (position < tempPosition) {
				return VIEW_TYPE_HEADER;
			}

			// Address list items
			tempPosition += mProfileCompletionStatusResponse.getAddressCompletionDetails().size();
			if (position < tempPosition) {
				return VIEW_TYPE_ADDRESS;
			}

			// Identification Header
			tempPosition += 1;
			if (position < tempPosition) {
				return VIEW_TYPE_HEADER;
			}

			// Identification list items
			tempPosition += mProfileCompletionStatusResponse.getIdentificationCompletionDetails().size();
			if (position < tempPosition) {
				return VIEW_TYPE_IDENTIFICATION;
			}

			// Add Bank Header
			tempPosition += 1;
			if (position < tempPosition) {
				return VIEW_TYPE_HEADER;
			}

			// Add Bank list items
			tempPosition += mProfileCompletionStatusResponse.getAddBankCompletionDetails().size();
			if (position < tempPosition) {
				return VIEW_TYPE_ADD_BANK;
			}

			return super.getItemViewType(position);
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v;

			if (viewType == VIEW_TYPE_HEADER) {
				v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_header, parent, false);
				return new HeaderViewHolder(v);
			} else {
				v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_profile_completion, parent, false);
				RecyclerView.ViewHolder vh;

				if (viewType == VIEW_TYPE_BASIC_INFO) {
					vh = new BasicInfoViewHolder(v);
				} else if (viewType == VIEW_TYPE_ADDRESS) {
					vh = new AddressViewHolder(v);
				} else if (viewType == VIEW_TYPE_IDENTIFICATION) {
					vh = new IdentificationViewHolder(v);
				} else {
					vh = new AddBankViewHolder(v);
				}

				return vh;
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			int tempPosition = 0;

			// Basic Info Header
			tempPosition += 1;
			if (position < tempPosition) {
				((HeaderViewHolder) holder).bindViewHeader(mProfileCompletionStatusResponse.getTagList().get(ProfileCompletionPropertyConstants.TAG_POSITION_PROFILE_PICTURE));
				return;
			}

			// Basic Info list items
			tempPosition += mProfileCompletionStatusResponse.getBasicInfoCompletionDetails().size();
			if (position < tempPosition) {
				((BasicInfoViewHolder) holder).bindViewProfileCompletion(
						(position - (tempPosition - mProfileCompletionStatusResponse.getBasicInfoCompletionDetails().size())));
				return;
			}

			// Address Header
			tempPosition += 1;
			if (position < tempPosition) {
				((HeaderViewHolder) holder).bindViewHeader(mProfileCompletionStatusResponse.getTagList().get(ProfileCompletionPropertyConstants.TAG_POSITION_IDENTIFICATION));
				return;
			}

			// Address list items
			tempPosition += mProfileCompletionStatusResponse.getAddressCompletionDetails().size();
			if (position < tempPosition) {
				((AddressViewHolder) holder).bindViewProfileCompletion(
						(position - (tempPosition - mProfileCompletionStatusResponse.getAddressCompletionDetails().size())));
				return;
			}

			// Identification Header
			tempPosition += 1;
			if (position < tempPosition) {
				((HeaderViewHolder) holder).bindViewHeader(mProfileCompletionStatusResponse.getTagList().get(ProfileCompletionPropertyConstants.TAG_POSITION_BASIC_INFO));
				return;
			}

			// Identification list items
			tempPosition += mProfileCompletionStatusResponse.getIdentificationCompletionDetails().size();
			if (position < tempPosition) {
				((IdentificationViewHolder) holder).bindViewProfileCompletion(
						(position - (tempPosition - mProfileCompletionStatusResponse.getIdentificationCompletionDetails().size())));
				return;
			}

			// Add Bank Header
			tempPosition += 1;
			if (position < tempPosition) {
				((HeaderViewHolder) holder).bindViewHeader(mProfileCompletionStatusResponse.getTagList().get(ProfileCompletionPropertyConstants.TAG_POSITION_SOURCE_OF_FUND));
				return;
			}

			// Add Bank list items
			tempPosition += mProfileCompletionStatusResponse.getAddBankCompletionDetails().size();
			if (position < tempPosition) {
				((AddBankViewHolder) holder).bindViewProfileCompletion(
						(position - (tempPosition - mProfileCompletionStatusResponse.getAddBankCompletionDetails().size())));
			}

		}

		@Override
		public int getItemCount() {
			return 1 + mProfileCompletionStatusResponse.getBasicInfoCompletionDetails().size() +
					1 + mProfileCompletionStatusResponse.getAddressCompletionDetails().size() +
					1 + mProfileCompletionStatusResponse.getIdentificationCompletionDetails().size() +
					1 + mProfileCompletionStatusResponse.getAddBankCompletionDetails().size();
		}
	}
}
