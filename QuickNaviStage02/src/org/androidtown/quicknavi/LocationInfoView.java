package org.androidtown.quicknavi;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LocationInfoView extends LinearLayout {

	public TextView mLocName;
	public TextView mLocAddress;

	public LocationInfoView(Context context) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.locationinfoitem, this, true);

		mLocName = (TextView)findViewById(R.id.locName);
		mLocAddress = (TextView)findViewById(R.id.locAddress);
	}

	public LocationInfoView(Context context, LocationInfo locInfo) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.locationinfoitem, this, true);

		mLocName = (TextView)findViewById(R.id.locName);
		mLocAddress = (TextView)findViewById(R.id.locAddress);

		mLocName.setText(locInfo.getName());
		mLocAddress.setText(locInfo.getAddress());
	}
}
