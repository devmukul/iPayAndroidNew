
package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;

public class ProfilePictureHelperDialog extends AlertDialog {
    private static final String MALE_GENDER = "M";
    private static final String FEMALE_GENDER = "F";
    private Context context;
    private OnResourceSelectedListener onResourceSelectedListener;
    private LayoutInflater inflater;
    private View selectImageHeaderView;
    private ImageView mProfilePictureHelperView;
    private TextView selectImageHeaderTitle;
    private ListView imageSelectorOptionsListView;

    public ProfilePictureHelperDialog(Context context, String mTitle, List<String> resources) {
        super(context);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.dialog_profile_picture_helper, null);
        mProfilePictureHelperView = (ImageView) v.findViewById(R.id.pro_pic_helper_view);
        selectImageHeaderView = inflater.inflate(R.layout.dialog_selector_header, null);
        selectImageHeaderTitle = (TextView) selectImageHeaderView.findViewById(R.id.textviewTitle);
        selectImageHeaderTitle.setText(mTitle);

        setAppropriateProfilePictureHelperPhoto();

        this.setCustomTitle(selectImageHeaderView);
        this.setView(v);

        setItems(resources, v);
    }

    private void setAppropriateProfilePictureHelperPhoto() {
        if (ProfileInfoCacheManager.getGender() != null) {
            if (ProfileInfoCacheManager.getGender().equalsIgnoreCase(MALE_GENDER))
                mProfilePictureHelperView.setImageDrawable(context.getResources().getDrawable(R.drawable.profile_pic_helper_male));
            else if (ProfileInfoCacheManager.getGender().equalsIgnoreCase(FEMALE_GENDER))
                mProfilePictureHelperView.setImageDrawable(context.getResources().getDrawable(R.drawable.profile_pic_helper_female));
        }
    }

    public void setItems(final List<String> resources, final View view) {

        imageSelectorOptionsListView = (ListView) view.findViewById(R.id.image_selection_option_list);

        SelectorAdapter adapter = new SelectorAdapter(context, resources);
        imageSelectorOptionsListView.setAdapter(adapter);

        imageSelectorOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = resources.get(i);

                if (onResourceSelectedListener != null)
                    onResourceSelectedListener.onResourceSelected(i, name);
                dismiss();
            }
        });
    }

    public void setOnResourceSelectedListener(OnResourceSelectedListener onResourceSelectedListener) {
        this.onResourceSelectedListener = onResourceSelectedListener;
    }

    public interface OnResourceSelectedListener {
        void onResourceSelected(int id, String name);
    }

    private class SelectorAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;

        public SelectorAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String mSelectorName = getItem(position);
            View view = convertView;

            if (view == null)
                view = inflater.inflate(R.layout.custom_selector_list_item_centered, null);

            TextView selectorView = (TextView) view.findViewById(R.id.textViewSelectorName);
            selectorView.setText(mSelectorName);
            return view;
        }
    }
}