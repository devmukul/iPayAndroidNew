package bd.com.ipay.ipayskeleton.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import bd.com.ipay.ipayskeleton.R;

public class IPaySupportPlaceAutocompleteFragment extends SupportPlaceAutocompleteFragment {

    private OnSearchClearListener onSearchClearListener;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final EditText query = view.findViewById(com.google.android.gms.location.places.R.id.place_autocomplete_search_input);
        query.setHint(getString(R.string.search));

        final View clear = view.findViewById(com.google.android.gms.location.places.R.id.place_autocomplete_clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSearchClearListener != null) {
                    clear.setVisibility(View.GONE);
                    query.setText("");
                    onSearchClearListener.onClear();
                }
            }
        });
    }

    public OnSearchClearListener getOnSearchClearListener() {
        return onSearchClearListener;
    }

    public void setOnSearchClearListener(OnSearchClearListener onSearchClearListener) {
        this.onSearchClearListener = onSearchClearListener;
    }

   public interface OnSearchClearListener {
        void onClear();
    }
}
