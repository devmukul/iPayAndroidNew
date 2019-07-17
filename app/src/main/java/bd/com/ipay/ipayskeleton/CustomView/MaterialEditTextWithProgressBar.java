package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import bd.com.ipay.ipayskeleton.R;

public class MaterialEditTextWithProgressBar extends FrameLayout {

    private TextInputLayout mTextInputLayout;
    private EditText mEditText;
    private ProgressBar mProgressBar;

    private OnClickListener mOnClickListener;

    public MaterialEditTextWithProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    public MaterialEditTextWithProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public MaterialEditTextWithProgressBar(Context context) {
        super(context);
        initView(context, null);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_meterial_edit_text_with_progress_bar, this, true);

        mTextInputLayout = (TextInputLayout) v.findViewById(R.id.textInputLayout);
        mEditText = (EditText) v.findViewById(R.id.edit_text);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditTextWithProgressBar, 0, 0);
            int inputType = a.getInt(R.styleable.MaterialEditTextWithProgressBar_android_inputType, EditorInfo.TYPE_CLASS_TEXT);
            String hint = a.getString(R.styleable.MaterialEditTextWithProgressBar_android_hint);

            if (hint != null)
                mTextInputLayout.setHint(hint);
            mEditText.setInputType(inputType);
        }

    }

    public EditText getEditText() {
        return mEditText;
    }

    public Editable getText() {
        return mEditText.getText();
    }

    public void setText(String text) {
        mEditText.setText(text);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public void setProgressBar(ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        mEditText.addTextChangedListener(textWatcher);
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(GONE);
    }

    public void setError(String error) {
        mEditText.setError(error);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mOnClickListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mOnClickListener != null;
    }
}
