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

public class CustomSelectorDialogWithIcon extends AlertDialog {
    private Context context;

    private List<String> resources;
    private String mTitle;

    private OnResourceSelectedListener onResourceSelectedListener;

    private LayoutInflater inflater;
    private View view, viewTitle;
    private TextView textViewTitle;
    private ListView popUpList;
    private int[] mIconList;


    public CustomSelectorDialogWithIcon(Context context, String mTitle, List<String> resources, int[] mIconList) {
        super(context);

        this.context = context;
        this.resources = resources;
        this.mTitle = mTitle;
        this.mIconList = mIconList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewTitle = inflater.inflate(R.layout.dialog_selector_header, null);
        textViewTitle = (TextView) viewTitle.findViewById(R.id.textviewTitle);
        textViewTitle.setText(mTitle);
        this.setCustomTitle(viewTitle);

        view = inflater.inflate(R.layout.dialog_custom_listview, null);
        this.setView(view);

        setItems(resources);

    }

    public void setItems(final List<String> resources) {

        popUpList = (ListView) view.findViewById(R.id.custom_list);
        SelectorAdapter adapter = new SelectorAdapter(context, resources);
        popUpList.setAdapter(adapter);
        popUpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                view = inflater.inflate(R.layout.list_item_with_icon, null);

            ImageView imageView = (ImageView) view.findViewById(R.id.icon_imageView);
            TextView selectorView = (TextView) view.findViewById(R.id.textview_name);

            selectorView.setText(mSelectorName);
            imageView.setImageResource(mIconList[position]);
            return view;
        }
    }
}