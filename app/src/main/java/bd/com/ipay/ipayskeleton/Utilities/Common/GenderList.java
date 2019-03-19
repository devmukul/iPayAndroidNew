package bd.com.ipay.ipayskeleton.Utilities.Common;


import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import bd.com.ipay.ipayskeleton.R;


public class GenderList {

	public static final String[] genderCodes = {
			"M", "F", "O"
	};

	public static String[] genderNames;

	public static void initialize(Context context) {
		genderNames = context.getResources().getStringArray(R.array.gender_array);
		for (int i = 0; i < genderCodes.length; i++) {
			genderCodeToNameMap.put(genderCodes[i], genderNames[i]);
			genderNameToCodeMap.put(genderNames[i], genderCodes[i]);
		}
	}

	public static final Map<String, String> genderCodeToNameMap = new HashMap<>();
	public static final Map<String, String> genderNameToCodeMap = new HashMap<>();
}
