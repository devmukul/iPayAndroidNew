package bd.com.ipay.ipayskeleton.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SendMoneyFragments.SendMoneyReviewFragment;

public class SendMoneyReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money_review);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new SendMoneyReviewFragment()).commit();
    }
}
