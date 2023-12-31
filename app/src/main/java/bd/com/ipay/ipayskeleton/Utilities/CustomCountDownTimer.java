package bd.com.ipay.ipayskeleton.Utilities;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;


public abstract class CustomCountDownTimer {

    //Millis since epoch when alarm should stop.
    private final long mMillisInFuture;
    //The interval in millis that the user receives callbacks
    private final long mCountdownInterval;
    private long mStopTimeInFuture;
    private long mNextTime;
    private static final int MSG = 1;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    //Cancel the countdown.
    public final void cancel() {
        mHandler.removeMessages(MSG);
    }

    //Start the countdown.
    public synchronized final CustomCountDownTimer start() {
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }

        mNextTime = SystemClock.uptimeMillis();
        mStopTimeInFuture = mNextTime + mMillisInFuture;
        mNextTime += mCountdownInterval;
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG), mNextTime);
        return this;
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    //Callback fired when the time is up.
    public abstract void onFinish();

    // handles counting down
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (CustomCountDownTimer.this) {
                final long millisLeft = mStopTimeInFuture - SystemClock.uptimeMillis();

                if (millisLeft <= 0) {
                    onFinish();
                } else {
                    onTick(millisLeft);
                    // Calculate next tick by adding the countdown interval from the original start time
                    // If user's onTick() took too long, skip the intervals that were already missed
                    long currentTime = SystemClock.uptimeMillis();
                    do {
                        mNextTime += mCountdownInterval;
                    } while (currentTime > mNextTime);
                    // Make sure this interval doesn't exceed the stop time
                    if (mNextTime < mStopTimeInFuture)
                        sendMessageAtTime(obtainMessage(MSG), mNextTime);
                    else
                        sendMessageAtTime(obtainMessage(MSG), mStopTimeInFuture);
                }
            }
        }
    };
}