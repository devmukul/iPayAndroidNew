package bd.com.ipay.ipayskeleton.Api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.GetThanaResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.Thana;
import bd.com.ipay.ipayskeleton.Model.MMModule.Resource.ThanaRequestBuilder;
import bd.com.ipay.ipayskeleton.Utilities.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

/**
 * Asynchronously loads all supported thanas supported by our systems.
 * Loaded bank accounts are saved into {@link CommonData}.
 */
public class GetThanaAsyncTask extends HttpRequestGetAsyncTask {
    public GetThanaAsyncTask(final Context context) {
        super(Constants.COMMAND_GET_THANA_LIST,
                new ThanaRequestBuilder().getGeneratedUri(),
                context);

        this.mHttpResponseListener = new HttpResponseListener() {
            @Override
            public void httpResponseReceiver(String result) {
                String[] resultArr = result.split(";");
                try {
                    Gson gson = new Gson();
                    GetThanaResponse getThanaResponse = gson.fromJson(resultArr[2],
                            GetThanaResponse.class);

                    List<Thana> thanas = getThanaResponse.getThanas();
                    StringBuilder xml = new StringBuilder();
                    for (Thana thana : thanas) {
                        xml.append("<item>" + thana.getName() + "</item>");
                    }

                    Log.i("Thanas", xml.toString());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}