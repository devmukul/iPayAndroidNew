package bd.com.ipay.ipayskeleton.BroadcastReceivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;


public class SMSReaderBroadcastReceiver extends BroadcastReceiver {

    private OnTextMessageReceivedListener listener;

    public void setOnTextMessageReceivedListener(OnTextMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            String OtpMessage;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get(Constants.SMS_READER_BROADCAST_RECEIVER_PDUS);
                    SmsMessage[] message = new SmsMessage[pdus.length];
                    for(int i = 0; i < message.length; i++){
                        message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        OtpMessage = message[i].getMessageBody();

                        matchOTP(OtpMessage);

                    }
                }
                catch(Exception e) {
                    Logger.logD("Exception caught", e.getMessage());
                }
            }
        }
    }

    private void matchOTP(String message) {

        String makePattern = ".*" + Constants.ApplicationTag +".*(\\d{6}).*";
        Pattern pattern = Pattern.compile(makePattern);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String otp = matcher.group(1);
            if (listener != null)
                listener.onTextMessageReceive(otp);
        }
    }

    public interface OnTextMessageReceivedListener {
        void onTextMessageReceive(String otp);
    }
}