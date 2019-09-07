package org.androidtown.quicknavi.stage05;

import java.util.ArrayList;

import org.androidtown.quicknavi.common.TitleBitmapButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class NavigationDisplayActivity extends Activity {

	public static final String TAG = "NavigationDisplayActivity";

	//===== DIALOG CONSTANTS =====//
	public static final int ROUTE_INFO_LIST = 1002;

	LocationInfo selectedStartLocation;
	LocationInfo selectedDestLocation;

	ArrayList<GPoint> pathsList;
	ArrayList<RouteInfo> routeInfoList;
	ListView routeInfoListView;
	RouteInfoListAdapter routeInfoListAdapter;

	TextView startName;
	TextView destName;

	TitleBitmapButton showRouteInfoBtn;


	private GoogleMap map;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_display);

        initView();

        processIntent();
    }

    private void initView() {
    	startName = (TextView)findViewById(R.id.startName);
    	destName = (TextView)findViewById(R.id.destName);

    	showRouteInfoBtn = (TitleBitmapButton)findViewById(R.id.showRouteInfoBtn);
    	showRouteInfoBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "showRouteInfoBtn clicked.");

				if (selectedStartLocation != null && selectedDestLocation != null) {
					showDialog(ROUTE_INFO_LIST);
				} else {
					Toast.makeText(getApplicationContext(), "먼저 경로검색을 해주세요.", 1000).show();
				}
			}
		});


		routeInfoListView = new ListView(getApplicationContext());
		routeInfoListView.setCacheColorHint(Color.argb(0, 0, 0, 0));
		routeInfoListView.setClickable(false);

		// 지도 객체 참조
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    }

    private void processIntent() {
    	Intent passedIntent = getIntent();

    	if (passedIntent != null) {
    		selectedStartLocation = (LocationInfo) passedIntent.getSerializableExtra("selectedStartLocation");
    		selectedDestLocation = (LocationInfo) passedIntent.getSerializableExtra("selectedDestLocation");
    		routeInfoList = (ArrayList<RouteInfo>) passedIntent.getSerializableExtra("routeInfoList");
    		pathsList = (ArrayList<GPoint>) passedIntent.getSerializableExtra("pathsList");

    		if (selectedStartLocation != null) {
	    		Log.d(TAG, "selectedStartLocation : " + selectedStartLocation.getName());
	    		Log.d(TAG, "selectedDestLocation : " + selectedDestLocation.getName());
	    		Log.d(TAG, "routeInfoList : " + routeInfoList.size());

	    		// set name
	    		String startNameStr = selectedStartLocation.getName();
	    		String destNameStr = selectedDestLocation.getName();

	    		startName.setText(startNameStr);
	    		destName.setText(destNameStr);
    		}
    	}
    }


	protected void onStart() {
		if (pathsList != null) {
			drawLocations();
			drawRoute(Color.BLUE);
		}

		super.onStart();
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;

		switch (id) {
			case ROUTE_INFO_LIST:
				builder = new AlertDialog.Builder(this);

				routeInfoListAdapter = new RouteInfoListAdapter(this, routeInfoList);
				routeInfoListView.setAdapter(routeInfoListAdapter);
				int routeInfoCount = routeInfoListAdapter.getCount();

				builder.setTitle("경로 정보 (" + routeInfoCount + " 건)");
				builder.setView(routeInfoListView);

				return builder.create();

		}

		return null;
	}


	private void drawLocations() {
		double startLatitude = Double.parseDouble(selectedStartLocation.getY());
		double startLongitude = Double.parseDouble(selectedStartLocation.getX());
		double destLatitude = Double.parseDouble(selectedDestLocation.getY());
		double destLongitude = Double.parseDouble(selectedDestLocation.getX());

		LatLng startPoint = new LatLng(startLatitude, startLongitude);

		MarkerOptions startMarker = new MarkerOptions();
		startMarker.position(startPoint);
		startMarker.title("출발지점");
		startMarker.snippet("출발지점");
		startMarker.draggable(true);
		startMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_icon_01));
		
		map.addMarker(startMarker);

		LatLng destPoint = new LatLng(destLatitude, destLongitude);
		
		MarkerOptions destMarker = new MarkerOptions();
		destMarker.position(destPoint);
		destMarker.title("도착지점");
		destMarker.snippet("도착지점");
		destMarker.draggable(true);
		destMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_icon_02));
		
		map.addMarker(destMarker);

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));
		
	}

	private void drawRoute(int color) {

		LatLng oldPnt = null;

		PolylineOptions polyline = new PolylineOptions();
		polyline.width(4);
		polyline.color(color);
		polyline.geodesic(true);
        
		for (int i = 0; i < pathsList.size(); i++) {
			GPoint pnt = pathsList.get(i);
			LatLng curPnt = new LatLng(pnt.y, pnt.x);

			if (oldPnt == null) {
				oldPnt = curPnt;
				continue;
			} else {
				polyline.add(oldPnt, curPnt);
				oldPnt = curPnt;
			}
		}

		map.addPolyline(polyline);
	}
 
}