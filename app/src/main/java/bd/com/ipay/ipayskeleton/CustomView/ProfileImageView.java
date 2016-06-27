package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonDrawableList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class ProfileImageView extends FrameLayout {
    private Context context;

    private TextView mProfileFirstLetterView;
    private RoundedImageView mProfilePictureView;

    public ProfileImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public ProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProfileImageView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;

        View v = inflate(context, R.layout.profile_image_view, null);

        mProfileFirstLetterView = (TextView) v.findViewById(R.id.portraitTxt);
        mProfilePictureView = (RoundedImageView) v.findViewById(R.id.portrait);

        addView(v);
    }

    public void setProfilePicture(String photoUri) {
//        mProfilePictureView.setVisibility(View.VISIBLE);
//        mProfileFirstLetterView.setVisibility(View.GONE);

        Glide.with(context)
                .load(photoUri)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mProfilePictureView);
    }

    public void setProfilePicture(int photoResourceId) {
//        mProfilePictureView.setVisibility(View.VISIBLE);
//        mProfileFirstLetterView.setVisibility(View.GONE);

        Drawable drawable = context.getResources().getDrawable(photoResourceId);
        mProfilePictureView.setImageDrawable(drawable);
    }

    public void setProfilePicturePlaceHolder() {
        Glide.with(context)
                .load(R.drawable.ic_person)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mProfilePictureView);
    }

    public void setProfileFirstLetter(String name) {
        if (name != null && name.length() > 0) {
//            mProfilePictureView.setVisibility(View.GONE);
//            mProfileFirstLetterView.setVisibility(View.VISIBLE);

            mProfileFirstLetterView.setText(String.valueOf(name.toUpperCase().charAt(0)));

            int backgroundDrawable = CommonDrawableList.getProfilePictureBackgroundBasedOnName(name);
            mProfileFirstLetterView.setBackgroundResource(backgroundDrawable);
        }
    }

    public void setInformation(String photoUri, String name) {
//        if (photoUri != null) {
//            setProfilePicture(photoUri);
//        } else {
//            setProfileFirstLetter(name);
//        }

        if (name != null && !name.isEmpty()) {
            setProfileFirstLetter(name);
            setProfilePicture(photoUri);

        } else if (photoUri != null){
            setProfileFirstLetter(photoUri);
        } else {
            setProfilePicturePlaceHolder();
        }
    }

    /**
     * Try to set an already downloaded profile picture for this phone number.
     * Returns true on success.
     */
    public boolean setProfilePictureFromDisk(String phoneNumber) {
        File dir = new File(Environment.getExternalStorageDirectory().getPath()
                + Constants.PICTURE_FOLDER);
        if (!dir.exists()) dir.mkdir();

        Uri imageUri = null;
        if (phoneNumber != null) {
            File file = new File(dir, phoneNumber.replaceAll("[^0-9]", "") + ".jpg");
            if (file.exists()) {
                imageUri = Uri.fromFile(file);

                if (imageUri != null) {
                    setProfilePicture(imageUri.toString());
                    return true;
                }
            }
        }

        return false;
    }

    public void setInformation(String photoUri, String name, String phoneNumber)
    {
        Log.w("info", photoUri + " " + name + " " + phoneNumber);
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath()
                    + Constants.PICTURE_FOLDER);
            if (!dir.exists()) dir.mkdir();

            Uri imageUri = null;
            if (phoneNumber != null) {
                File file = new File(dir, phoneNumber.replaceAll("[^0-9]", "") + ".jpg");
                if (file.exists())
                    imageUri = Uri.fromFile(file);
            }

            if(imageUri != null) {
                Log.w("Image Uri", imageUri.toString());
                setInformation(imageUri.toString(), name);

            } else {
                setInformation(Constants.BASE_URL_FTP_SERVER + photoUri, name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
