package bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.Activities.RailwayTicketActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TicketSuccessFragment extends IPayAbstractTransactionSuccessFragment {
	private int mSelectedAdult;
	private int mSelectedChild;

	private String mSelectedTrain = null;
	private int mSelectedTrainNo;
	private double mTotalAmount;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mSelectedAdult = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, 0);
			mSelectedChild = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, 0);
			mSelectedTrain = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NAME, "");
			mSelectedTrainNo = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NO, 0);
			mTotalAmount = getArguments().getDouble(RailwayTicketActionActivity.KEY_TICKET_TOTAL_AMOUNT, 0);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.make_payment_success_message_tk, mTotalAmount));
		setSuccessDescription(getString(R.string.train_ticket_success_description));
		setName(getString(R.string.railway_ticket_name));
		setUserName(Utilities.formatJourneyInfoText(getContext(), mSelectedTrain +" - "+mSelectedTrainNo, mSelectedAdult, mSelectedChild));
		setReceiverImage(R.drawable.bd_railway);
	}
}
