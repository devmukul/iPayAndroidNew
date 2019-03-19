package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
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

    public void setProfilePicture(int photoResourceId) {
        Drawable drawable = context.getResources().getDrawable(photoResourceId);
        mProfilePictureView.setImageDrawable(drawable);
    }

    public void setProfilePicturePlaceHolder() {
        Glide.with(context)
                .load(R.drawable.ic_profile)
                .crossFade()
                .into(mProfilePictureView);
    }

    public void setProfilePicture(String photoURI) {
        try {
            final DrawableTypeRequest<String> glide = Glide.with(context).load(photoURI);

            glide
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            glide.signature(new StringSignature(String.valueOf(System.currentTimeMillis())));
            glide.placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .crossFade()
                    .dontAnimate()
                    .transform(new CircleTransform(context))
                    .into(mProfilePictureView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProfilePicture(String photoUrl, boolean forceLoad) {

        if (photoUrl != null) {
            if (!photoUrl.contains("ipay.com")) {
                photoUrl = Constants.BASE_URL_FTP_SERVER + photoUrl;
            }
        }

        try {
            final DrawableTypeRequest<String> glide = Glide.with(context).load(photoUrl);

            glide
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (forceLoad) {
                glide
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())));
            }

            glide.signature(new StringSignature(String.valueOf(System.currentTimeMillis())));
            glide.placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .crossFade()
                    .dontAnimate()
                    .transform(new CircleTransform(context))
                    .into(mProfilePictureView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBusinessLogoPlaceHolder() {
        mProfilePictureView.setImageResource(R.drawable.ic_business_logo_round);
    }

    public void setBusinessProfilePicture(String photoUri, boolean forceLoad) {
        try {
            final DrawableTypeRequest<String> glide = Glide.with(context).load(photoUri);

            glide
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (forceLoad) {
                glide
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())));
            }

            glide
                    .placeholder(R.drawable.ic_business_logo_round)
                    .error(R.drawable.ic_business_logo_round)
                    .crossFade()
                    .dontAnimate()
                    .transform(new CircleTransform(context))
                    .into(mProfilePictureView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAccountPhoto(String photoUri, boolean forceLoad) {
        try {
            final DrawableTypeRequest<String> glide = Glide.with(context).load(photoUri);

            glide
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            if (forceLoad) {
                glide
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())));
            }

            if (ProfileInfoCacheManager.isBusinessAccount()) {
                glide
                        .placeholder(R.drawable.ic_business_logo_round)
                        .error(R.drawable.ic_business_logo_round);
            } else {
                glide
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile);
            }

            glide
                    .crossFade()
                    .dontAnimate()
                    .transform(new CircleTransform(context))
                    .into(mProfilePictureView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
