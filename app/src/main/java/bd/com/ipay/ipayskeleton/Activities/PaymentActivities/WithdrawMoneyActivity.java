package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Model.MMModule.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.WithdrawMoneyFragment;

public class WithdrawMoneyActivity extends BaseActivity {

    public static MandatoryBusinessRules mMandatoryBusinessRules = new MandatoryBusinessRules();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_out);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new WithdrawMoneyFragment()).commit();

    }

    @Override
    public Context setContext() {
        return WithdrawMoneyActivity.this;
    }
}

