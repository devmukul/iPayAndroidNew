package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.DashboardProfileCompletionPOJO;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;

import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.BASIC_PROFILE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PERSONAL_ADDRESS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PHOTOID;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROFILE_PICTURE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROPERTY_NAME_TO_ACTION_NAME_MAP;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROPERTY_NAME_TO_ICON_MAP;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROPERTY_NAME_TO_SCORE_MAP;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.PROPERTY_NAME_TO_TITLE_MAP;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_BASIC_INFO;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_BUSINESS_ADDRESS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_BUSINESS_DOCUMENTS;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_BUSINESS_INFO;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_IDENTIFICATION;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_PROFILE_PICTURE;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.TAG_POSITION_SOURCE_OF_FUND;
import static bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants.VERIFY_BANK_OR_CARD;

public class ProfileCompletionStatusResponse {

    private String message;
    private List<CompletionStatus> completionStatusList;
    private List<String> tagList;
    private List<Integer> tagwiseScorePercentage;
    private int completionPercentage;
    private boolean completedMandetoryFields;

    private int basicInfoItemCount = 0;
    private int addressItemCount = 0;
    private int identificationItemCount = 0;
    private int sourceOfFundItemCount = 0;

    private double basicInfoCompletionSum = 0;
    private double addressCompletionSum = 0;
    private double identificationCompletionSum = 0;
    private double sourceOfFundSum = 0;

    private final List<PropertyDetails> basicInfoCompletionDetails = new ArrayList<>();
    private final List<PropertyDetails> addressCompletionDetails = new ArrayList<>();
    private final List<PropertyDetails> identificationCompletionDetails = new ArrayList<>();
    private final List<PropertyDetails> sourceOfFundDetails = new ArrayList<>();
    private final List<PropertyDetails> otherCompletionDetails = new ArrayList<>();

    public String getMessage() {
        return message;
    }


    public List<PropertyDetails> getBasicInfoCompletionDetails() {
        return basicInfoCompletionDetails;
    }

    public List<PropertyDetails> getAddressCompletionDetails() {
        return addressCompletionDetails;
    }

    public List<PropertyDetails> getIdentificationCompletionDetails() {
        return identificationCompletionDetails;
    }

    public List<PropertyDetails> getAddBankCompletionDetails() {
        return sourceOfFundDetails;
    }

    public List<PropertyDetails> getOtherCompletionDetails() {
        return otherCompletionDetails;
    }

    public List<CompletionStatus> getCompletionStatusList() {
        return completionStatusList;
    }

    public int getCompletionPercentage() {
//        double totalCompletionSum = basicInfoCompletionSum + addressCompletionSum + identificationCompletionSum + sourceOfFundSum;
//        double totalItemCount = basicInfoItemCount + addressItemCount + identificationItemCount + sourceOfFundItemCount;
//        return (int) Math.round(totalCompletionSum / totalItemCount);
        return completionPercentage;
    }

    public int getBasicInfoCompletionPercentage() {
        return (int) Math.round(basicInfoCompletionSum / basicInfoItemCount);
    }

    public int getAddressCompletionPercentage() {
        return (int) Math.round(addressCompletionSum / addressItemCount);
    }

    public int getIdentificationCompletionPercentage() {
        return (int) Math.round(identificationCompletionSum / identificationItemCount);
    }

    public int getAddBankCompletionPercentage() {
        return (int) Math.round(sourceOfFundSum / sourceOfFundItemCount);
    }

    public List<String> getTagList() {
        return tagList;
    }

    private double getPropertyCompletionPercentage(int threshold, int value) {
        if (value >= threshold)
            return 100;
        else
            return (double) value / threshold * 100;
    }

    public boolean isCompletedMandatoryFields() {
        return completedMandetoryFields;
    }

    public String getAnalyzedProfileVerificationMessage() {
        String message = "";

        for (int i = 0; i < tagwiseScorePercentage.size(); i++) {
            if (tagwiseScorePercentage.get(i) != 100) {
                if (!message.isEmpty())
                    message += ", ";
                message += tagList.get(i);
            }
        }

        return message;
    }

    public List<DashboardProfileCompletionPOJO> dashboardProfileCompletionData(Context context) {

        List<DashboardProfileCompletionPOJO> tempData = new ArrayList<>();

        // Iterate the completionStatusList
        for (CompletionStatus mCompletionStatus : completionStatusList) {

            PropertyDetails propertyDetails = new PropertyDetails(mCompletionStatus.getValue(),
                    mCompletionStatus.getThreshold(), mCompletionStatus.getTag(), mCompletionStatus.getProperty());
            double propertyCompletionPercentage = getPropertyCompletionPercentage(mCompletionStatus.getThreshold(), mCompletionStatus.getValue());

            if (mCompletionStatus.getProperty().equals(PROFILE_PICTURE)) {
                if (propertyDetails.getPropertyTitle() != null && propertyCompletionPercentage<100) {
                    tempData.add(new DashboardProfileCompletionPOJO(propertyDetails.getPropertyName(), context.getString(R.string.dashboard_profile_picture), context.getString(R.string.dashboard_profile_picture_text),
                            R.drawable.ic_onboard_profile_pic_upload_small,propertyDetails.getTag()));
                }

            } else if (mCompletionStatus.getProperty().equals(BASIC_PROFILE)) {

                if (propertyDetails.getPropertyTitle() != null && propertyCompletionPercentage<100) {
                    tempData.add(new DashboardProfileCompletionPOJO(propertyDetails.getPropertyName(), context.getString(R.string.dashboard_basic_info), context.getString(R.string.dashboard_basic_info_text),
                            R.drawable.ic_onboard_basic_info_small,propertyDetails.getTag()));
                }

            } else if (mCompletionStatus.getProperty().equals(PERSONAL_ADDRESS)) {

                if (propertyDetails.getPropertyTitle() != null && propertyCompletionPercentage<100) {
                    tempData.add(new DashboardProfileCompletionPOJO(propertyDetails.getPropertyName(), context.getString(R.string.dashboard_present_address), context.getString(R.string.dashboard_personal_address_text),
                            R.drawable.ic_onboard_basic_info_small,propertyDetails.getTag()));
                }

            } else if (mCompletionStatus.getProperty().equals(PHOTOID)) {

                if (propertyDetails.getPropertyTitle() != null && propertyCompletionPercentage<100) {
                    tempData.add(new DashboardProfileCompletionPOJO(propertyDetails.getPropertyName(), context.getString(R.string.dashboard_national_id), context.getString(R.string.dashboard_photo_id_text),
                            R.drawable.ic_onboard_photo_id_small,propertyDetails.getTag()));
                }

            } else if (mCompletionStatus.getProperty().equals(VERIFY_BANK_OR_CARD)) {

                if (propertyDetails.getPropertyTitle() != null && propertyCompletionPercentage<100) {
                    tempData.add(new DashboardProfileCompletionPOJO(propertyDetails.getPropertyName(), context.getString(R.string.dashboard_verify_bank), context.getString(R.string.dashboard_verify_bank_text),
                            R.drawable.ic_onboard_add_bank_helper_small,propertyDetails.getTag()));
                }
            }
        }
        return tempData;
    }

    public void analyzeProfileCompletionData() {

        // Iterate the completionStatusList
        for (CompletionStatus mCompletionStatus : completionStatusList) {

            PropertyDetails propertyDetails = new PropertyDetails(mCompletionStatus.getValue(),
                    mCompletionStatus.getThreshold(), mCompletionStatus.getTag(), mCompletionStatus.getProperty());
            double propertyCompletionPercentage = getPropertyCompletionPercentage(mCompletionStatus.getThreshold(), mCompletionStatus.getValue());

            if (mCompletionStatus.getTag() == TAG_POSITION_PROFILE_PICTURE) {

                basicInfoItemCount++;
                basicInfoCompletionSum = basicInfoCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    basicInfoCompletionDetails.add(propertyDetails);

            } else if (mCompletionStatus.getTag() == TAG_POSITION_BUSINESS_ADDRESS) {

                addressItemCount++;
                addressCompletionSum = addressCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    addressCompletionDetails.add(propertyDetails);

            } else if (mCompletionStatus.getTag() == TAG_POSITION_BUSINESS_DOCUMENTS) {
                identificationItemCount++;
                identificationCompletionSum = identificationCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    identificationCompletionDetails.add(propertyDetails);


            } else if (mCompletionStatus.getTag() == TAG_POSITION_BUSINESS_INFO) {

                basicInfoItemCount++;
                basicInfoCompletionSum = basicInfoCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    basicInfoCompletionDetails.add(propertyDetails);


            } else if (mCompletionStatus.getTag() == TAG_POSITION_BASIC_INFO) {

                addressItemCount++;
                addressCompletionSum = addressCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    addressCompletionDetails.add(propertyDetails);

            } else if (mCompletionStatus.getTag() == TAG_POSITION_IDENTIFICATION) {

                identificationItemCount++;
                identificationCompletionSum = identificationCompletionSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    identificationCompletionDetails.add(propertyDetails);

            } else if (mCompletionStatus.getTag() == TAG_POSITION_SOURCE_OF_FUND) {

                sourceOfFundItemCount++;
                sourceOfFundSum = sourceOfFundSum + propertyCompletionPercentage;

                if (propertyDetails.getPropertyTitle() != null)
                    sourceOfFundDetails.add(propertyDetails);
            } else {
                if (propertyDetails.getPropertyTitle() != null)
                    otherCompletionDetails.add(propertyDetails);
            }
        }

        initScoreFromPropertyName();
    }

    public void initScoreFromPropertyName() {

        // Iterate the completionStatusList: "Profile Picture","Identification","Basic Info","Source of Fund"
        for (int i = 0; i < tagList.size(); i++) {
            int score = tagwiseScorePercentage.get(i);
            String tag = tagList.get(i);
            PROPERTY_NAME_TO_SCORE_MAP.put(tag, score);
        }
    }

    public boolean isPhotoIdUpdated() {
        try {
            if (getPropertyScore("Identification") == 100)
                return true;
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isPhotoUpdated() {
        try {
            if (getPropertyScore("Profile Picture") == 100)
                return true;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isBankAdded() {
        for (CompletionStatus mCompletionStatus : completionStatusList) {
            if (mCompletionStatus.getProperty().equals("ADD_BANK")) {
                if (mCompletionStatus.getValue() > 0)
                    return true;
            }
        }
        return false;
    }

    public boolean isOnboardBasicInfoUpdated() {
        try {
            if (getPropertyScore("Basic Info") == 100)
                return true;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

    }

    public Integer getPropertyScore(String tag) {
        return PROPERTY_NAME_TO_SCORE_MAP.get(tag);
    }


    public class PropertyDetails implements Comparable<PropertyDetails> {
        private final String propertyName;
        private final int value;
        private final int threshold;
        private final int tag;

        public PropertyDetails(int value, int threshold, int tag, String propertyName) {
            this.value = value;
            this.threshold = threshold;
            this.tag = tag;
            this.propertyName = propertyName;
        }

        public boolean isCompleted() {
            return value >= threshold;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getPropertyTitle() {
            if (propertyName.equals(PERSONAL_ADDRESS) && ProfileInfoCacheManager.isBusinessAccount())
                return "Add Business DBContactNode's Address";
            else if (propertyName.equals(BASIC_PROFILE) && ProfileInfoCacheManager.isBusinessAccount())
                return "Complete Business DBContactNode Information";
            return PROPERTY_NAME_TO_TITLE_MAP.get(propertyName);
        }

        public int getValue() {
            return value;
        }

        public int getThreshold() {
            return threshold;
        }

        public int getTag() {
            return tag;
        }

        public Integer getPropertyIcon() {
            return PROPERTY_NAME_TO_ICON_MAP.get(propertyName);
        }

        public String getActionName() {
            return PROPERTY_NAME_TO_ACTION_NAME_MAP.get(propertyName);
        }

        /**
         * Keep the incomplete property first
         */
        @Override
        public int compareTo(PropertyDetails another) {
            if (!this.isCompleted())
                return -1;
            if (!another.isCompleted())
                return 1;
            return 0;
        }
    }
}
