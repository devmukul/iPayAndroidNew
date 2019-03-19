package bd.com.ipay.ipayskeleton.SourceOfFund.models;

public class UpdateMonthlyLimitRequest  {
    private long monthlyCreditLimit;
    private String pin;

    public UpdateMonthlyLimitRequest(long monthlyCreditLimit, String pin) {
        this.monthlyCreditLimit = monthlyCreditLimit;
        this.pin = pin;
    }

    public long getMonthlyCreditLimit() {
        return monthlyCreditLimit;
    }

    public void setMonthlyCreditLimit(long monthlyCreditLimit) {
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
