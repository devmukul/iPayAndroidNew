package bd.com.ipay.ipayskeleton.Utilities;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

/**
 * Validates user inputs (e.g. email, password, date of birth)
 */
public class InputValidator {
	public static final String MOBILE_NUMBER_REGEX = "^(((\\+)?880)?|(0)?)(1[3-9][\\d]{8})$";
	private static final String INVALID_PASSPORT_ID_WITH_INSUFFICIENT_LENGTH_PATTERN = "[A-Z]{2}[0-9]{0,6}|[A-Z]{1}";
	private static final String VALID_PASSPORT_ID_PATTERN = "[A-Z]{2}[0-9]{7}";
	private static final String ALPHA_NUMERIC_PATTERN = "^[a-zA-Z0-9]*$";

	public static String isPasswordValid(String password) {
		// Return empty string if the password is valid
		if (password.length() == 0) return "Enter a password";
		if (password.length() < 8) return "Password must contain minimum 8 characters.";
		if (!password.matches(".*[a-zA-Z]+.*")) return "Password must contain a letter.";
		if (!password.matches(".*[0-9]+.*")) return "Password must contain a digit.";
		return "";
	}

	// TODO return meaningful message based on the error
	public static boolean isDateOfBirthValid(String dob) {
		try {
			String[] dobFields = dob.split("/");

			int day = Integer.parseInt(dobFields[0]);
			int month = Integer.parseInt(dobFields[1]);
			int year = Integer.parseInt(dobFields[2]);

			return (day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 0 && year <= 9999);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidEmail(String email) {
		Pattern emailPattern = Pattern.compile("[a-zA-Z0-9[!#$%&'()*+,/\\-_\\.\"]]+@[a-zA-Z0-9[!#$%&'()*+,/\\-_\"]]+\\.[a-zA-Z0-9[!#$%&'()*+,/\\-_\"\\.]]+");
		Matcher m = emailPattern.matcher(email);
		return m.matches();
	}

	public static boolean isValidName(String name) {
		return !name.matches(".*[0-9]+.*");
	}

	public static boolean isValidNameWithRequiredLength(String name) {
		name = name.replaceAll("\\s+", "");

		return !name.matches(".*[0-9]+.*") && name.length() >= Constants.MIN_VALID_NAME_LENGTH;
	}

	@Nullable
	public static String isValidAmount(Context context, BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
		final String errorMessage;

		if (amount.compareTo(minAmount) == -1) {
			errorMessage = context.getResources().getString(R.string.please_enter_minimum_amount, context.getResources().getString(R.string.balance_holder, Utilities.takaWithComma(minAmount)));
		} else if (minAmount.compareTo(maxAmount) > 0) {
			errorMessage = context.getResources().getString(R.string.insufficient_balance);
		} else if (amount.compareTo(maxAmount) == 1) {
			errorMessage = context.getResources().getString(R.string.please_enter_not_more_than_max_amount, context.getResources().getString(R.string.balance_holder, Utilities.takaWithComma(maxAmount)));
		} else {
			errorMessage = null;
		}
		return errorMessage;
	}

	public static String isValidOTP(Context context, String otp) {
		String errorMessage = null;

		if (otp.length() == 0)
			errorMessage = context.getString(R.string.error_invalid_otp);
		else if (otp.length() != 6)
			errorMessage = context.getString(R.string.error_invalid_otp_with_required_length);
		else if (!otp.matches("^[0-9]*$"))
			errorMessage = context.getString(R.string.invalid_otp_with_characters);
		return errorMessage;
	}

	public static String isValidDocumentID(Context context, String documentID, String documentType) {
		String errorMessage = null;
		switch (documentType) {
			case IdentificationDocumentConstants.DOCUMENT_TYPE_NATIONAL_ID:
				int length = documentID.length();
				if (length < Constants.MINIMUM_REQUIRED_NID_LENGTH)
					errorMessage = context.getString(R.string.invalid_nid_min_length);
				else if (length > Constants.MAXIMUM_REQUIRED_NID_LENGTH)
					errorMessage = context.getString(R.string.invalid_nid_max_length);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_PASSPORT:
				if (documentID.matches(IdentificationDocumentConstants.INVALID_PASSPORT_ID_WITH_INSUFFICIENT_LENGTH_PATTERN))
					errorMessage = context.getString(R.string.invalid_passport_ID_insufficient_length);
				else if (!documentID.matches(IdentificationDocumentConstants.VALID_PASSPORT_ID_PATTERN))
					errorMessage = context.getString(R.string.invalid_passport_ID);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_DRIVING_LICENSE:
				if (!documentID.matches(ALPHA_NUMERIC_PATTERN))
					errorMessage = context.getString(R.string.invalid_driving_license_ID);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_BIRTH_CERTIFICATE:
				if (!documentID.matches(ALPHA_NUMERIC_PATTERN))
					errorMessage = context.getString(R.string.invalid_birth_certificate);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_TIN:
				if (!documentID.matches(ALPHA_NUMERIC_PATTERN))
					errorMessage = context.getString(R.string.invalid_tin);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_BUSINESS_TIN:
				if (documentID.length() != Constants.BUSINESS_TIN_LENGTH)
					errorMessage = context.getString(R.string.invalid_business_tin_wrong_length);
				break;
			case IdentificationDocumentConstants.DOCUMENT_TYPE_VAT_REG_CERT:
				if (documentID.length() != Constants.VAT_REG_CERT_ID_LENGTH)
					errorMessage = context.getString(R.string.invalid_vat_reg_cert_ID_wrong_length);
				break;
		}
		return errorMessage;
	}

	public static boolean isValidNumber(String number) {
		if (number == null)
			return false;

		if (number.length() != 11 && number.length() != 13 && number.length() != 14)
			return false;
		number = number.replaceAll("[^\\d]", "");
		return number.length() == 11 && number.startsWith("0") || number.length() == 13 && number.startsWith("880");
	}

	public static boolean isValidDigit(String number) {
		if (number == null)
			return false;

		return number.matches(".*[0-9].*");
	}

	public static boolean isValidMobileNumberWithCountryCode(String mobileNumber, String countryCode) {
		if (countryCode.equals("BD")) {
			String formattedNumber = ContactEngine.formatMobileNumberInternational(mobileNumber, countryCode);
			return formattedNumber.matches(MOBILE_NUMBER_REGEX);
		}

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			Phonenumber.PhoneNumber phoneNumberWithCountryCode = phoneUtil.parse(mobileNumber, countryCode);
			boolean isValid = phoneUtil.isValidNumber(phoneNumberWithCountryCode);
			return isValid;
		} catch (NumberParseException e) {
			Logger.logD("NumberParseException was thrown: ", e.toString());
			return false;
		}
	}

	public static boolean isValidMobileNumberBD(String mobileNumber) {
		String formattedNumber = ContactEngine.formatMobileNumberBD(mobileNumber);
		return formattedNumber.matches(MOBILE_NUMBER_REGEX);
	}
}