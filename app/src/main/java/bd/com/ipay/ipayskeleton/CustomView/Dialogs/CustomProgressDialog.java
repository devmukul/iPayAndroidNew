package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.R;

public class CustomProgressDialog extends android.support.v7.app.AlertDialog {
    private Context context;


    public CustomProgressDialog(Context context) {
        super(context);
        this.context = context;
        createView();
    }

    private void createView() {
        View customView = LayoutInflater.from(context).inflate(R.layout.view_custom_progress_dialog, null, false);
        this.setView(customView);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }

    public void showDialog() {
        this.show();
    }

    public void dismissDialog() {
        this.dismiss();
    }
}

