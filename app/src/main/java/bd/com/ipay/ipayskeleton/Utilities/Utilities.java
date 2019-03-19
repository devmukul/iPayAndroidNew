package bd.com.ipay.ipayskeleton.Utilities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.PaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.Model.AddMoneyOption;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Employee.GetBusinessInformationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.UserProfilePictureClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.TokenParserClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

public class Utilities {

    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat timeFormat;
    private static SimpleDateFormat DATE_FORMAT_WITH_TIME;
    private static SimpleDateFormat DATE_FORMAT_WITHOUT_TIME;
    private static SimpleDateFormat DATE_FORMAT_FROM_STRING;

    public static void updateLocale() {
        dateFormat = new SimpleDateFormat("dd/MM/yyyy, h:mm a", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        DATE_FORMAT_WITH_TIME = new SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.getDefault());
        DATE_FORMAT_WITHOUT_TIME = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        DATE_FORMAT_FROM_STRING = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    }

    public static void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private static final String TAG = Utilities.class.getSimpleName();

    public static boolean isConnectionAvailable(Context context) {
        if (context == null) return false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } else {
            return false;
        }
    }

    public static void setAppropriateKeyboard(Context c, String documentType, EditText editText) {
        if (documentType.equals(IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID) ||
                documentType.equals(IdentificationDocumentConstants.DOCUMENT_TYPE_BUSINESS_TIN) ||
                documentType.equals(IdentificationDocumentConstants.DOCUMENT_TYPE_VAT_REG_CERT) ||
                documentType.equals(IdentificationDocumentConstants.DOCUMENT_TYPE_TRADE_LICENSE)) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    public static boolean isTabletDevice(Context context) {
        boolean large = (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
        boolean xlarge = (context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return large || xlarge;
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @return address or empty string
     * @paramipv4 true=return ipv4, false=return ipv6
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    /**
     * Convert byte array to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (byte aByte : bytes) {
            int intVal = aByte & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     *
     * @param str
     * @return array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     *
     * @param filename
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN = 1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ignored) {
            // For now eat exceptions
        }
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    public static String streamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static String extractDate(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.group() : null;
    }

    public static float getFontSize(int position) {
        switch (position) {
            case 0:
                return 12.0f;
            case 1:
                return 14.0f;
            case 2:
                return 18.0f;
            case 3:
                return 22.0f;
            default:
                return 14.0f;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setFontSize(View view, float fontSize) {
        try {
            Class[] paramFloat = new Class[1];
            paramFloat[0] = Float.TYPE;

            Class clas = Class.forName("android.widget.TextView");
            Method method = clas.getDeclaredMethod("setTextSize", paramFloat);

            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View child = group.getChildAt(i);
                    setFontSize(child, fontSize);
                }
            } else if (view instanceof TextView) {
                method.invoke(view, fontSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String splitCamelCase(String camelCaseWord) {
        return camelCaseWord.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        ).toLowerCase();
    }

    public static String getErrorMessageForInvalidInput(String[] errorFieldNames, String errorMessage) {

        String errorMessageWithFieldNames = errorMessage + " in ";
        for (int i = 0; i < errorFieldNames.length; i++) {
            if (i != errorFieldNames.length - 1)
                errorMessageWithFieldNames += splitCamelCase(errorFieldNames[i]) + " , ";
            else
                errorMessageWithFieldNames += splitCamelCase(errorFieldNames[i]);
        }
        return errorMessageWithFieldNames;
    }

    public static long getTimeFromBase64Token(String base64) {

        long timeForTokenExpiration = 0;

        // Token divided in three parts. Middle portion of the token contains expiration time
        String[] tokensArray = base64.split("\\.");
        base64 = tokensArray[1];
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        try {
            String parsedToken = new String(data, "UTF-8");
            Logger.logD(Constants.PARSED_TOKEN, Constants.PARSED_TOKEN + parsedToken);
            Gson gson = new Gson();
            TokenParserClass mTokenParserClass = gson.fromJson(parsedToken, TokenParserClass.class);

            // Returns time is second. Multiply by 1000 to get the time in milli-seconds
            timeForTokenExpiration = (mTokenParserClass.getExp() - mTokenParserClass.getIat()) * 1000;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return timeForTokenExpiration;
    }

    public static String getFilePathFromData(Context context, Uri uri) {
        String[] projection = new String[]{"_data"};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        String filePath = null;
        int columnIndex = cursor.getColumnIndex(projection[0]);
        if (cursor != null && cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return filePath;
    }

    public static void setLayoutAnim_slideDown(ViewGroup panel) {

        AnimationSet set = new AnimationSet(true);

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(
                set, 0.25f);
        panel.setLayoutAnimation(controller);
    }

    public static void setUpNonScrollableListView(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static boolean isValueAvailable(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static BigDecimal getMinPossibleBalance(BigDecimal valueOne) {
        return valueOne.max(BigDecimal.ZERO);
    }

    public static String formatTakaWithComma(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return String.format("\u09F3%s", numberFormat.format(amount));
    }

    public static String takaWithComma(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(amount);
    }

    public static String formatTaka(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return String.format("\u09F3%s", numberFormat.format(amount));
    }

    public static String formatTaka(double amount) {
        return String.format(Locale.getDefault(), "\u09F3%.2f", amount);
    }

    public static String formatTakaFromString(String amount) {
        String sign = "";
        if (amount.charAt(0) == '+' || amount.charAt(0) == '-') {
            StringBuilder stringBuilder = new StringBuilder(amount);
            sign += amount.charAt(0);
            stringBuilder.deleteCharAt(0);
            amount = stringBuilder.toString();
        }
        double amountDouble = Double.parseDouble(amount);
        return String.format(Locale.getDefault(), "%s\u09F3%.2f", sign, amountDouble);
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(Context context, View v) {
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void setKeyboardHide(Activity activity) {
        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    public static void showKeyboard(Context context) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void showKeyboard(Context context, final EditText editText) {

        final InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (!editText.hasFocus()) {
            editText.requestFocus();
        }

        editText.post(new Runnable() {
            @Override
            public void run() {
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    public static String formatDateWithTime(long time) {
        return DATE_FORMAT_WITH_TIME.format(time);
    }

    public static String formatDateWithoutTime(long time) {
        return DATE_FORMAT_WITHOUT_TIME.format(time);
    }

    public static String formatDayMonthYear(long time) {
        return dateFormat.format(time);
    }

    public static String formatTimeOnly(long time) {
        return timeFormat.format(time);
    }

    public static Date formatDateFromString(String dateString) {
        Date newDate = null;
        try {
            newDate = DATE_FORMAT_FROM_STRING.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return newDate;
    }

    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);

        return resultCode == ConnectionResult.SUCCESS;
    }

    public static String getExtension(String filePath) {
        if (filePath != null && filePath.lastIndexOf('.') >= 0)
            return filePath.substring(filePath.lastIndexOf('.') + 1, filePath.length()).toLowerCase();
        else
            return "";
    }

    public static String getImage(List<UserProfilePictureClass> profilePictureClasses, String quality) {
        String imageQualityHigh = null;
        String imageQualityMedium = null;
        String imageQualityLow = null;

        for (UserProfilePictureClass profilePicture : profilePictureClasses) {
            switch (profilePicture.getQuality()) {
                case Constants.IMAGE_QUALITY_HIGH:
                    imageQualityHigh = profilePicture.getUrl();
                    break;
                case Constants.IMAGE_QUALITY_MEDIUM:
                    imageQualityMedium = profilePicture.getUrl();
                    break;
                case Constants.IMAGE_QUALITY_LOW:
                    imageQualityLow = profilePicture.getUrl();
                    break;
            }
        }

        switch (quality) {
            case Constants.IMAGE_QUALITY_HIGH:
                if (imageQualityHigh != null)
                    return imageQualityHigh;
                else if (imageQualityMedium != null)
                    return imageQualityMedium;
                else if (imageQualityLow != null)
                    return imageQualityLow;
                break;
            case Constants.IMAGE_QUALITY_MEDIUM:
                if (imageQualityMedium != null)
                    return imageQualityMedium;
                else if (imageQualityHigh != null)
                    return imageQualityHigh;
                else if (imageQualityLow != null)
                    return imageQualityLow;
                break;
            case Constants.IMAGE_QUALITY_LOW:
                if (imageQualityLow != null)
                    return imageQualityLow;
                else if (imageQualityHigh != null)
                    return imageQualityHigh;
                else if (imageQualityMedium != null)
                    return imageQualityMedium;
                break;
        }
        return "";
    }

    public static com.tsongkha.spinnerdatepicker.DatePickerDialog getDatePickerDialog(Context context, Date date, com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener onDateSetListener, boolean showDay) {
        final com.tsongkha.spinnerdatepicker.DatePickerDialog datePickerDialog = initDatePickerDialog(context, date, onDateSetListener, showDay);

        return datePickerDialog;
    }

    public static com.tsongkha.spinnerdatepicker.DatePickerDialog initDatePickerDialog(Context context, Date date, com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener onDateSetListener, boolean showDay) {
        final Calendar calendar = Calendar.getInstance();
        int year, month, day;
        int minYear = calendar.get(Calendar.YEAR) - Constants.MIN_AGE_LIMIT;
        int minMonth = calendar.get(Calendar.MONTH);
        int minDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (date == null) {
            year = calendar.get(Calendar.YEAR) - Constants.MIN_AGE_LIMIT;
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            calendar.setTime(date);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        SpinnerDatePickerDialogBuilder spinnerDatePickerDialogBuilder = new SpinnerDatePickerDialogBuilder()
                .context(context)
                .spinnerTheme(R.style.NumberPickerStyle)
                .showTitle(true)
                .callback(onDateSetListener)
                .showDaySpinner(true)
                .defaultDate(year, month, day)
                .showDaySpinner(showDay)
                .maxDate(minYear, minMonth, minDay);
        return spinnerDatePickerDialogBuilder.build();
    }

    private static void setCalenderWithAgeLimit(Calendar calendar) {
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        int minYear = currentYear - Constants.MIN_AGE_LIMIT;
        int minMonth = currentMonth;
        int minDay = currentDay;

        calendar.set(minYear, minMonth, minDay);
    }

    private static void setLimitInDatePickerDialog(DatePickerDialog datePickerDialog) {
        final Calendar calendar = Calendar.getInstance();

        setCalenderWithAgeLimit(calendar);
        long minDateInMilliSeconds = calendar.getTimeInMillis();

        // Set 10 years from today as max limit of date picker
        datePickerDialog.getDatePicker().setMaxDate(minDateInMilliSeconds);
        datePickerDialog.setTitle(null);
    }

    public static void setActionBarTitle(Activity activity, String title) {
        activity.getActionBar().setTitle(title);
    }

    public static BigDecimal bigDecimalPercentage(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(new BigDecimal(100));
    }

    public static int getRandomNumber() {
        Random r = new Random();
        int number = r.nextInt(100 - 1) + 1;
        return number;
    }

    public static void goToiPayInAppStore(Context mContext) {
        final String appPackageName = mContext.getPackageName();
        try {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void finishLauncherActivity(Activity activity) {
        Intent intent = new Intent();
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }


    public static boolean isNecessaryPermissionExists(Context context, String... permissionList) {
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public static void requestRequiredPermissions(Fragment fragment, int permissionCode, String[] permissionList) {
        List<String> requiredPermissions = new ArrayList<>();
        for (String permission : permissionList) {
            if (!isNecessaryPermissionExists(fragment.getActivity(), permission))
                requiredPermissions.add(permission);
        }

        fragment.requestPermissions(requiredPermissions.toArray(new String[requiredPermissions.size()]), permissionCode);
    }

    public static void requestRequiredPermissions(Activity activity, int permissionCode, String[] permissionList) {
        List<String> requiredPermissions = new ArrayList<>();
        for (String permission : permissionList) {
            if (!isNecessaryPermissionExists(activity, permission))
                requiredPermissions.add(permission);
        }

        ActivityCompat.requestPermissions(activity, requiredPermissions.toArray(new String[requiredPermissions.size()]), permissionCode);
    }

    public static boolean isValidTokenWindowTime() {
        return currentTime() - TokenManager.getLastRefreshTokenFetchTime() > Constants.MIN_REQUIRED_REFRESH_TOKEN_TIME;
    }

    public static long currentTime() {
        return System.currentTimeMillis();
    }

    public static Tracker getTracker(Activity activity) {
        Tracker mTracker;
        MyApplication application = (MyApplication) activity.getApplication();
        mTracker = application.getDefaultTracker();
        return mTracker;
    }

    public static void sendScreenTracker(Tracker mTracker, String screenName) {
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendSuccessEventTracker(Tracker tracker, String category, int accountId) {
        sendSuccessEventTracker(tracker, category, accountId, 0);
    }

    public static void sendSuccessEventTracker(Tracker tracker, String category, int accountId, long value) {
        if (accountId == Constants.INVALID_ACCOUNT_ID) {
            sendEventTracker(tracker,
                    category,
                    "Success",
                    String.format(Locale.getDefault(), "At %s", formatDateWithoutTime(System.currentTimeMillis())),
                    value);
        } else {
            sendEventTracker(tracker,
                    category,
                    "Success",
                    String.format(Locale.getDefault(), "ACCOUNT_ID:%d at %s", accountId, formatDateWithoutTime(System.currentTimeMillis())),
                    value);
        }
    }

    public static void sendBlockedEventTracker(Tracker tracker, String category, int accountId) {
        sendBlockedEventTracker(tracker, category, accountId, 0);
    }

    public static void sendBlockedEventTracker(Tracker tracker, String category, int accountId, long value) {
        if (accountId == Constants.INVALID_ACCOUNT_ID) {
            sendEventTracker(tracker,
                    category,
                    "Blocked",
                    String.format(Locale.getDefault(), "At %s", formatDateWithoutTime(System.currentTimeMillis())),
                    value);
        } else {
            sendEventTracker(tracker,
                    category,
                    "Blocked",
                    String.format(Locale.getDefault(), "ACCOUNT_ID:%d at %s", accountId, formatDateWithoutTime(System.currentTimeMillis())),
                    value);
        }

    }

    public static void sendFailedEventTracker(Tracker tracker, String category, int accountId, String serverErrorMessage) {
        sendFailedEventTracker(tracker, category, accountId, serverErrorMessage, 0);
    }

    public static void sendFailedEventTracker(Tracker tracker, String category, int accountId, String serverErrorMessage, long value) {
        if (accountId == Constants.INVALID_ACCOUNT_ID) {
            sendEventTracker(tracker,
                    category,
                    "Failed",
                    String.format(Locale.getDefault(), "At %s, SERVER_MESSAGE:%s", formatDateWithoutTime(System.currentTimeMillis()), serverErrorMessage),
                    value);
        } else {
            sendEventTracker(tracker,
                    category,
                    "Failed",
                    String.format(Locale.getDefault(), "ACCOUNT_ID:%d at %s, SERVER_MESSAGE:%s", accountId, formatDateWithoutTime(System.currentTimeMillis()), serverErrorMessage),
                    value);
        }
    }

    public static void sendExceptionTracker(Tracker tracker, int accountId, String exceptionMessage) {
        if (accountId == Constants.INVALID_ACCOUNT_ID) {
            tracker.send(new HitBuilders.ExceptionBuilder().
                    setDescription(String.format(Locale.getDefault(), "EXCEPTION_MESSAGE:%s at %s, DEVICE_NAME:%s", exceptionMessage, formatDateWithoutTime(System.currentTimeMillis()), DeviceInfoFactory.getDeviceName()))
                    .setFatal(true).build());
        } else {
            tracker.send(new HitBuilders.ExceptionBuilder().
                    setDescription(String.format(Locale.getDefault(), "ACCOUNT_ID:%d at %s, EXCEPTION_MESSAGE:%s, DEVICE_NAME:%s", accountId, formatDateWithoutTime(System.currentTimeMillis()), exceptionMessage, DeviceInfoFactory.getDeviceName()))
                    .setFatal(true).build());
        }
    }


    public static void sendEventTracker(Tracker mTracker, String category, String action, String label, long value) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public static long getBytesToMegaBytes(long length) {
        return length / 1000000;
    }

    private static int[] ADD_MONEY_OPTION_SERVICE_ID;
    private static final String[] ADD_MONEY_OPTION_TITLE = {Constants.ADD_MONEY_BY_BANK_TITLE, Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_TITLE};
    private static final int[] ADD_MONEY_OPTION_ICON = {R.drawable.ic_bank111, R.drawable.basic_card};

    public static List<AddMoneyOption> getAddMoneyOptions() {
        final List<AddMoneyOption> addMoneyOptionList = new ArrayList<>();

        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_BANK)) {
            addMoneyOptionList.add(new AddMoneyOption(ServiceIdConstants.ADD_MONEY_BY_BANK,
                    R.drawable.ic_bank_icon,
                    R.string.money_from_bank, R.string.add_money_bank_option_message));
        }
        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD)) {
            addMoneyOptionList.add(new AddMoneyOption(ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD,
                    R.drawable.ic_debit_credit_card_icon,
                    R.string.debit_credit_card, R.string.add_money_card_option_message));
        }
        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY)) {
            addMoneyOptionList.add(new AddMoneyOption(ServiceIdConstants.ADD_MONEY_BY_BANK_INSTANTLY,
                    R.drawable.ic_instant_money_icon,
                    R.string.instant_add_money, R.string.add_money_instant_option_message));
        }
        return addMoneyOptionList;
    }

    public static String getMainUserProfileInfoString(GetProfileInfoResponse profileInfo) {
        Gson gson = new Gson();
        String profileInfoJson = gson.toJson(profileInfo);
        return profileInfoJson;
    }

    public static GetProfileInfoResponse getMainUserInfoFromJsonString(String profileInfoJsonString) {
        Gson gson = new Gson();
        GetProfileInfoResponse getProfileInfoResponse = gson.fromJson(profileInfoJsonString, GetProfileInfoResponse.class);
        return getProfileInfoResponse;
    }

    public static String getMainBusinessProfileInfoString(GetBusinessInformationResponse businessInformationResponse) {
        if (businessInformationResponse != null) {
            Gson gson = new Gson();
            String businessProfileInfoString = gson.toJson(businessInformationResponse);
            return businessProfileInfoString;
        } else {
            return null;
        }
    }

    public static GetBusinessInformationResponse getMainBusinessInfo(String businessInfoString) {
        if (businessInfoString == null) {
            return null;
        } else {
            Gson gson = new Gson();
            GetBusinessInformationResponse getBusinessInformationResponse = gson.
                    fromJson(businessInfoString, GetBusinessInformationResponse.class);
            return getBusinessInformationResponse;
        }
    }

    public static final int LOCATION_SETTINGS_PERMISSION_CODE = 9876;
    public static final int LOCATION_SETTINGS_RESULT_CODE = 9875;
    public static final int LOCATION_SOURCE_SETTINGS_RESULT_CODE = 9874;

    public static boolean hasForcedLocationPermission(Fragment fragment) {
        if (Utilities.isNecessaryPermissionExists(fragment.getActivity(), Constants.LOCATION_PERMISSIONS)) {
            String locationProviders = Settings.Secure.getString(fragment.getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(locationProviders)) {
                showGPSDisabledDialog(fragment);
            } else {
                return true;
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRationaleDialog(fragment);
        } else {
            showLocationPermissionDialog(fragment);
        }
        return false;
    }

    private static void showRationaleDialog(final Fragment fragment) {
        AlertDialog alertDialog = new AlertDialog.Builder(fragment.getActivity())
                .setMessage(R.string.make_payment_location_permission_required_rationale_message)
                .setPositiveButton(R.string.continue_make_payment, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.requestRequiredPermissions(fragment, LOCATION_SETTINGS_PERMISSION_CODE, Constants.LOCATION_PERMISSIONS);
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.getActivity().finish();
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }

    private static void showGPSDisabledDialog(final Fragment fragment) {
        AlertDialog alertDialog = new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.gps_disabled)
                .setMessage(R.string.make_payment_location_settings_on_message)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SOURCE_SETTINGS_RESULT_CODE);
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.getActivity().finish();
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }

    public static void showGPSHighAccuracyDialog(final Fragment fragment) {
        AlertDialog alertDialog = new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.location_mode)
                .setMessage(R.string.make_payment_location_mode_on_message)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SOURCE_SETTINGS_RESULT_CODE);
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.getActivity().finish();
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }

    private static void showLocationPermissionDialog(final Fragment fragment) {
        AlertDialog alertDialog = new AlertDialog.Builder(fragment.getActivity())
                .setMessage(R.string.make_payment_location_permission_required_message)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", fragment.getActivity().getPackageName(), null);
                        intent.setData(uri);
                        fragment.startActivityForResult(intent, LOCATION_SETTINGS_RESULT_CODE);
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fragment.getActivity().finish();
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }

    public static DeepLinkAction parseUriForDeepLinkingAction(Uri uri) {
        DeepLinkAction deepLinkAction = new DeepLinkAction();
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 && pathSegments.size() != 0) {
            if (pathSegments.get(0).toLowerCase().contains("promotions")) {
                deepLinkAction.setAction(pathSegments.get(0));
                if (uri.getQueryParameter("link") != null) {
                    deepLinkAction.setQueryParameter(uri.getQueryParameter("link"));
                }
                return deepLinkAction;
            } else {
                return null;
            }
        } else if (pathSegments.size() == 2 && pathSegments.get(0).contains("signup")) {
            System.out.println("Test Invite " + pathSegments.get(0) + " " + uri.getQueryParameter("code"));
            deepLinkAction.setAction(pathSegments.get(0));
            deepLinkAction.setQueryParameter(uri.getQueryParameter("code"));
        } else if (pathSegments.size() == 2) {
            if (pathSegments.get(0).contains("app")) {
                deepLinkAction.setAction(pathSegments.get(1));
            } else {
                deepLinkAction.setAction(pathSegments.get(0));
            }
            if (uri.getQueryParameter("link") != null) {
                deepLinkAction.setQueryParameter(uri.getQueryParameter("link"));
            }
        } else {
            deepLinkAction.setAction(pathSegments.get(1));
            deepLinkAction.setOrderId(pathSegments.get(2));
        }
        return deepLinkAction;
    }

    public static void performDeepLinkAction(Activity activity, DeepLinkAction deepLinkAction) {
        Intent intent;
        switch (deepLinkAction.getAction()) {
            case "pay":
                intent = new Intent(activity, PaymentActivity.class);
                intent.putExtra(Constants.ORDER_ID, deepLinkAction.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finishAffinity();
                break;
            case "promotions":
                if (deepLinkAction.getQueryParameter() != null) {
                    intent = new Intent(activity, WebViewActivity.class);
                    intent.putExtra("url", "https://www.ipay.com.bd/promotions?link=" + deepLinkAction.getQueryParameter());
                    activity.startActivity(intent);
                    activity.finish();
                } else {
                    intent = new Intent(activity, HomeActivity.class);
                    intent.putExtra(Constants.PATH, deepLinkAction.getAction());
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            default:
                intent = new Intent(activity, HomeActivity.class);
                activity.startActivity(intent);
                activity.finish();
                break;
        }
    }

    private static final SimpleDateFormat promotionDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static Date parsePromotionDate(String date) {
        try {
            return promotionDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatAmberItCID(String customerID) {

        String customerIdStr = customerID.trim().toUpperCase();
        customerIdStr = customerIdStr.replaceAll("[^a-zA-Z0-9]", "");

        if (customerIdStr.startsWith("CID") || customerIdStr.startsWith("cid"))
            return customerIdStr;
        else
            return "CID" + customerIdStr;
    }

    public static String formatJourneyInfoText(Context c, String infoText, int adults, int child) {

        if (adults > 1) {
            if (child > 1) {
                return infoText + " " + c.getString(R.string.train_ticket_msg_adults_childs, adults, child);
            } else if (child == 0) {
                return infoText + " " + c.getString(R.string.train_ticket_msg_adults, adults);
            } else {
                return infoText + " " + c.getString(R.string.train_ticket_msg_adults_child, adults, child);
            }
        } else {
            if (child > 1) {

                return infoText + " " + c.getString(R.string.train_ticket_msg_adult_childs, adults, child);
            } else if (child == 0) {
                return infoText + " " + c.getString(R.string.train_ticket_msg_adult, adults);
            } else {
                return infoText + " " + c.getString(R.string.train_ticket_msg_adult_child, adults, child);
            }
        }
    }
}