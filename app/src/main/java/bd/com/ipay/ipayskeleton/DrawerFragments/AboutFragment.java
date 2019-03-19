package bd.com.ipay.ipayskeleton.DrawerFragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.AboutActivity;
import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.IconifiedTextViewWithButton;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AboutFragment extends BaseFragment {

	private TextView mBuildNumberView;
	private IconifiedTextViewWithButton mContactView;
	private IconifiedTextViewWithButton mTermView;
	private IconifiedTextViewWithButton mPrivacyView;
	private TextView mCopyRightTextView;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		mBuildNumberView = view.findViewById(R.id.text_view_build_number);
		mContactView = view.findViewById(R.id.text_view_contact);
		mTermView = view.findViewById(R.id.text_view_terms_of_service);
		mPrivacyView = view.findViewById(R.id.text_view_privacy);
		mCopyRightTextView = view.findViewById(R.id.text_view_copyright);

		setButtonActions();
		setAppVersionView();
		setCopyRightFooterView();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_ipay_about));
	}

	private void setButtonActions() {
		mContactView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((AboutActivity) getActivity()).switchToAboutContactsFragment();
			}
		});

		mTermView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Intent intent = new Intent(getActivity(), WebViewActivity.class);
					intent.putExtra("url", getString(R.string.term_link));
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(getContext(), R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
				}
			}
		});

		mPrivacyView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Intent intent = new Intent(getActivity(), WebViewActivity.class);
					intent.putExtra("url", getString(R.string.privacy_link));
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(getContext(), R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void setAppVersionView() {
		if (getActivity() == null)
			return;
		try {
			PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			String version = pInfo.versionName;
			mBuildNumberView.setText(String.format(Locale.getDefault(), "%s: %s", getString(R.string.version), version));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setCopyRightFooterView() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		mCopyRightTextView.setText(String.format(Locale.getDefault(), "%s %d %s", getString(R.string.copyright), year, getString(R.string.iPay_system)));
	}

}
