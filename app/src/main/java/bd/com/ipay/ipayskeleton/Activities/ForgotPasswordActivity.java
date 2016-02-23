package bd.com.ipay.ipayskeleton.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import bd.com.ipay.ipayskeleton.ForgotPasswordFragments.OTPVerificationForgotPasswordFragment;
import bd.com.ipay.ipayskeleton.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new OTPVerificationForgotPasswordFragment()).commit();

    }
}

