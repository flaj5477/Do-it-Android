package org.androidtown.quicknavi.stage05;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteInfoView extends LinearLayout {

	public TextView mRouteName;
	public TextView mRouteGuide;

	public RouteInfoView(Context context) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.routeinfoitem, this, true);

		mRouteName = (TextView)findViewById(R.id.routeName);
		mRouteGuide = (TextView)findViewById(R.id.routeGuide);
	}

	public RouteInfoView(Context context, RouteInfo routeInfo) {
		super(context);

		// Layout Inflation
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.routeinfoitem, this, true);

		mRouteName = (TextView)findViewById(R.id.routeName);
		mRouteGuide = (TextView)findViewById(R.id.routeGuide);

		mRouteName.setText(routeInfo.getName());
		mRouteGuide.setText(routeInfo.getRoadDistance() + "m ¾Õ " + routeInfo.getGuideName());
	}
}
