package bd.com.ipay.ipayskeleton.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import bd.com.ipay.android.IPayActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class LanguageActivity extends IPayActivity {

    private LinearLayout mBanglaView;
    private LinearLayout mEnglishView;
    private TextView mBanglaText;
    private TextView mEnglishText;
    private ImageView mBanglaImage;
    private ImageView mEnglishImage;
    private Button mContinueButton;

    String selectedLang = "bn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mBanglaView = findViewById(R.id.bangla);
        mEnglishView = findViewById(R.id.english);
        mBanglaText = findViewById(R.id.bangla_text);
        mEnglishText = findViewById(R.id.english_text);
        mBanglaImage = findViewById(R.id.bangla_image);
        mEnglishImage = findViewById(R.id.english_image);
        mContinueButton = findViewById(R.id.button_continue);

        mBanglaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLang = "bn";
                mBanglaText.setTextColor(getResources().getColor(R.color.colorWhite));
                mBanglaImage.setColorFilter(getResources().getColor(R.color.colorWhite));
                mEnglishText.setTextColor(Color.parseColor("#5e5e5e"));
                mEnglishImage.setColorFilter(Color.parseColor("#5e5e5e"));
            }
        });

        mEnglishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLang = "en";
                mEnglishText.setTextColor(getResources().getColor(R.color.colorWhite));
                mEnglishImage.setColorFilter(getResources().getColor(R.color.colorWhite));
                mBanglaText.setTextColor(Color.parseColor("#5e5e5e"));
                mBanglaImage.setColorFilter(Color.parseColor("#5e5e5e"));
            }
        });

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLang.equals("en")) {
                    if (!Constants.APP_LANGUAGE_ENGLISH.equals(SharedPrefManager.getAppLanguage())) {
                        SharedPrefManager.setAppLanguage(Constants.APP_LANGUAGE_ENGLISH);
                    }
                } else {
                    if (!Constants.APP_LANGUAGE_BENGALI.equals(SharedPrefManager.getAppLanguage())) {
                        SharedPrefManager.setAppLanguage(Constants.APP_LANGUAGE_BENGALI);
                    }
                }

                Intent intent = new Intent(LanguageActivity.this, TourActivity.class);
                intent.putExtra(Constants.TARGET_FRAGMENT, Constants.SIGN_UP);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        selectedLang = SharedPrefManager.getAppLanguage();
        if (selectedLang.equals("en")) {
            mEnglishView.performClick();
        } else {
            mBanglaView.performClick();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onResume();
    }
}