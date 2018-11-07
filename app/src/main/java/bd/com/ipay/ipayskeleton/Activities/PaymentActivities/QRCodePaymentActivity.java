package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.PaymentFragments.QRCodePaymentFragments.ScanQRCodeFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class QRCodePaymentActivity extends AppCompatActivity {

    public static ArrayList<Sponsor> sponsorList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_payment);
        switchToScanQRCodeFragment();
    }

    private void switchToScanQRCodeFragment() {
        Bundle bundle = new Bundle();
        if (sponsorList != null) {
            if (sponsorList.size() > 0) {
                bundle.putSerializable(Constants.SPONSOR_LIST, sponsorList);
                ScanQRCodeFragment scanQRCodeFragment = new ScanQRCodeFragment();
                scanQRCodeFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, scanQRCodeFragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScanQRCodeFragment()).commit();
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ScanQRCodeFragment()).commit();
        }
    }
}
