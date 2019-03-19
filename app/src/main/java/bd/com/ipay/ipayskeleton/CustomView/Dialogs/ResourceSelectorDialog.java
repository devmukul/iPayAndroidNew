package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Resource;
import bd.com.ipay.ipayskeleton.R;

public class ResourceSelectorDialog<E extends Resource> extends AlertDialog {

    private final List<E> resources;
    private final Context context;

    private List<Integer> ids;
    private List<String> stringIds;
    private List<String> names;
    private int selectedItemId;

    private OnResourceSelectedListener onResourceSelectedListener;
    private OnResourceSelectedListenerWithStringID onResourceSelectedListenerWithStringID;
    private ArrayAdapter<String> arrayAdapter;

    private LayoutInflater inflater;
    private View view, viewTitle;
    private TextView textViewTitle;
    private ListView popUpList;

    public ResourceSelectorDialog(Context context, String mTitle, List<E> resources) {
        super(context);
        this.context = context;
        this.resources = resources;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewTitle = inflater.inflate(R.layout.dialog_selector_header, null);
        textViewTitle = (TextView) viewTitle.findViewById(R.id.textviewTitle);
        textViewTitle.setText(mTitle);
        this.setCustomTitle(viewTitle);

        view = inflater.inflate(R.layout.dialog_custom_listview, null);
        this.setView(view);

        setItems(resources);
    }

    public ResourceSelectorDialog(Context context, String mTitle, List<E> resources, int selectedItemId, boolean isStringID) {
        super(context);
        this.context = context;
        this.resources = resources;
        this.selectedItemId = selectedItemId;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewTitle = inflater.inflate(R.layout.dialog_selector_header, null);
        textViewTitle = (TextView) viewTitle.findViewById(R.id.textviewTitle);
        textViewTitle.setText(mTitle);
        this.setCustomTitle(viewTitle);

        view = inflater.inflate(R.layout.dialog_custom_listview, null);
        this.setView(view);

        if (isStringID)
            setItemsWithStringID(resources);
        else setItems(resources);
    }


    private void setItems(List<E> resources) {
        ids = new ArrayList<>();
        names = new ArrayList<>();

        if(resources != null) {
            for (Resource resource : resources) {
                ids.add(resource.getId());
                names.add(resource.getName());
            }
        }
        popUpList = (ListView) view.findViewById(R.id.custom_list);
        CustomAdapter adapter = new CustomAdapter(context, names);
        popUpList.setAdapter(adapter);
        popUpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = names.get(i);
                int id = ids.get(i);

                if (onResourceSelectedListener != null)
                    onResourceSelectedListener.onResourceSelected(id, name);
                dismiss();
            }
        });
    }

    private void setItemsWithStringID(List<E> resources) {
        stringIds = new ArrayList<>();
        names = new ArrayList<>();

        for (Resource resource : resources) {
            stringIds.add(resource.getStringId());
            names.add(resource.getName());
        }
        popUpList = (ListView) view.findViewById(R.id.custom_list);
        CustomAdapter adapter = new CustomAdapter(context, names);
        popUpList.setAdapter(adapter);
        popUpList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = names.get(i);
                String stringID = stringIds.get(i);

                if (onResourceSelectedListenerWithStringID != null)
                    onResourceSelectedListenerWithStringID.onResourceSelectedWithStringID(stringID, name, selectedItemId);
                dismiss();
            }
        });
    }

    public void setOnResourceSelectedListener(OnResourceSelectedListener onResourceSelectedListener) {
        this.onResourceSelectedListener = onResourceSelectedListener;
    }

    public void setOnResourceSelectedListenerWithStringID(OnResourceSelectedListenerWithStringID onResourceSelectedListenerWithStringID) {
        this.onResourceSelectedListenerWithStringID = onResourceSelectedListenerWithStringID;
    }

    public interface OnResourceSelectedListener {
        void onResourceSelected(int id, String name);
    }

    public interface OnResourceSelectedListenerWithStringID {
        void onResourceSelectedWithStringID(String id, String name, int selectedIndex);
    }

    private class CustomAdapter extends ArrayAdapter<String> {

        private LayoutInflater inflater;


        public CustomAdapter(Context context, List<String> objects) {
            super(context, 0, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final String mSelectorName = names.get(position);
            View view = convertView;

            if (view == null)
                view = inflater.inflate(R.layout.list_item_custom_selector, null);

            TextView selectorView = (TextView) view.findViewById(R.id.textViewSelectorName);
            selectorView.setText(mSelectorName);
            return view;
        }
    }
}
