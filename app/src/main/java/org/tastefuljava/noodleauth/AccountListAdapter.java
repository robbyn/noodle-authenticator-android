package org.tastefuljava.noodleauth;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class AccountListAdapter extends ArrayAdapter<Account> {
    public AccountListAdapter(Activity activity) {
        super(activity, 0, new ArrayList<Account>());
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Activity activity = (Activity)getContext();
        Account device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.account_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.name_view);
        TextView tvToken = convertView.findViewById(R.id.token_view);
        ClockView cvClock = convertView.findViewById(R.id.clock_view);
        // Populate the data into the template view using the data object
        if (device != null) {
            tvName.setText(device.getName());
            long now = System.currentTimeMillis();
            tvToken.setText(device.generateOTP(now));
            cvClock.setAngle((int)(360*device.getRemainingRatio(now)));
        } else {
            tvName.setText("");
            tvToken.setText("");
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
