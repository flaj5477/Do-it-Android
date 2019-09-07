package org.androidtown.quicknavi.stage05;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

class RouteInfoListAdapter extends ArrayAdapter<RouteInfo> {
	private Context mContext;
	private ArrayList<RouteInfo> mRouteList = null;

	public RouteInfoListAdapter(Context context, ArrayList<RouteInfo> objects) {
		super(context, 0, objects);

		mContext = context;
		setRouteList(objects);
	}

	public int getCount() {
		int count = 0;
		if (mRouteList != null) {
			count = mRouteList.size();
		}

		return count;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		RouteInfoView itemView;
		if (convertView == null) {
			itemView = new RouteInfoView(mContext);
		} else {
			itemView = (RouteInfoView) convertView;
		}

		RouteInfo routeInfo = mRouteList.get(position);

		itemView.mRouteName.setText(routeInfo.getName());
		itemView.mRouteGuide.setText(routeInfo.getRoadDistance() + "m ¾Õ " + routeInfo.getGuideName());

		return itemView;
	}


	public void setRouteList(ArrayList<RouteInfo> routeList) {
		mRouteList = routeList;
	}


	public ArrayList<RouteInfo> getRouteList() {
		return mRouteList;
	}


}
