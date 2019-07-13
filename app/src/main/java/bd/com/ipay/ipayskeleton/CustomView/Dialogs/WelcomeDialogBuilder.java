package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

/**
 * You should call validatePin() first when the positive button is clicked
 */
public class WelcomeDialogBuilder extends android.support.v7.app.AlertDialog {

    Button mContinue;
    Context context;

    public WelcomeDialogBuilder(Context context) {
        super(context);
        this.context = context;
        initializeView();
    }

    private void initializeView() {
        View customView = LayoutInflater.from(context).inflate(R.layout.dialog_welcome, null, false);
        this.setView(customView);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        mContinue = customView.findViewById(R.id.ok_button);

        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefManager.setIsSignup(false);
                dismissDialogue();
            }
        });
    }


    public void showDialog() {
        this.show();
    }

    public void dismissDialogue() {
        if(this.isShowing())
            this.cancel();
    }

}
