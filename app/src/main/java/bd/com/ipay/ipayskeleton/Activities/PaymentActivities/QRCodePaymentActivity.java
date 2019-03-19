package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.QRCodePaymentFragments.ScanQRCodeFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;

public class QRCodePaymentActivity extends AppCompatActivity {

    public static ArrayList<Sponsor> sponsorList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_payment);
        try {
            sponsorList = HomeActivity.mSponsorList;
        } catch (Exception e) {

        }
        switchToScanQRCodeFragment();
    }

    private void switchToScanQRCodeFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScanQRCodeFragment()).commit();
    }
}
