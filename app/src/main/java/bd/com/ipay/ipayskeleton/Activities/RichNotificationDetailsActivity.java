package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;


public class RichNotificationDetailsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_rich_notification_body);
        TextView readMoreButton = findViewById(R.id.read_more);
        String title = getIntent().getStringExtra(Constants.TITLE);
        String description = getIntent().getStringExtra(Constants.DESCRIPTION);
        setTitle("iPay");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final String forwardingUrl = getIntent().getStringExtra(Constants.DEEP_LINK);
        final String imageUrl = getIntent().getStringExtra(Constants.IMAGE_URL);
        ImageView notificationImageView = findViewById(R.id.notification_image);
        TextView titleTextView = findViewById(R.id.notification_title);
        TextView descriptionTextView = findViewById(R.id.notification_description);
        Glide.with(this)
                .load(Constants.BASE_URL_FTP_SERVER + imageUrl)
                .error(R.drawable.notification_dedault_image)
                .into(notificationImageView);
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RichNotificationDetailsActivity.this,
                        WebViewActivity.class);
                intent.putExtra("url", forwardingUrl);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Context setContext() {
        return RichNotificationDetailsActivity.this;
    }
}
