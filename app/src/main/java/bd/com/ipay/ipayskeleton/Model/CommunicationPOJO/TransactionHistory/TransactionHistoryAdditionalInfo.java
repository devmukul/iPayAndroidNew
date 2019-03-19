package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.UserProfilePictureClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class TransactionHistoryAdditionalInfo implements Parcelable {
    private String name;
    private String number;
    private String type;
    private List<UserProfilePictureClass> profilePictures;

    protected TransactionHistoryAdditionalInfo(Parcel in) {
        name = in.readString();
        number = in.readString();
        type = in.readString();
        profilePictures = in.createTypedArrayList(UserProfilePictureClass.CREATOR);
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public int getImageWithType(Context context) {
        String imageUrl;
        if (type.equalsIgnoreCase(Constants.TRANSACTION_TYPE_INTERNAL)) {
            imageUrl = profilePictures.get(0).getUrl().toLowerCase();
        } else if (type.equalsIgnoreCase(Constants.TRANSACTION_TYPE_BANK)) {
            imageUrl = "ic_bank" + profilePictures.get(0).getUrl();
        } else {
            imageUrl = profilePictures.get(0).getUrl().toLowerCase();
        }

        Resources resources = context.getResources();
        final int iconId = resources.getIdentifier(imageUrl, "drawable",
                context.getPackageName());
        return iconId != 0 ? iconId : R.drawable.ic_transaction_ipaylogo;
    }

    public String getUserProfilePic() {
        if (!profilePictures.isEmpty())
            return profilePictures.get(0).getUrl();
        else return "";
    }

    public List<UserProfilePictureClass> getProfilePictures() {
        return profilePictures;
    }

    public String getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionHistoryAdditionalInfo> CREATOR = new Creator<TransactionHistoryAdditionalInfo>() {
        @Override
        public TransactionHistoryAdditionalInfo createFromParcel(Parcel in) {
            return new TransactionHistoryAdditionalInfo(in);
        }

        @Override
        public TransactionHistoryAdditionalInfo[] newArray(int size) {
            return new TransactionHistoryAdditionalInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(type);
        dest.writeTypedList(this.profilePictures);
    }
}
