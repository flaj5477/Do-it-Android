package org.androidtown.quicknavi;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.androidtown.quicknavi.common.TitleBitmapButton;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class QuickNaviActivity extends Activity {

	public static final String TAG = "QuickNaviActivity";

	//===== View references =====//

	CheckBox myLocationCheck;
	EditText startSearchEdit;
	TitleBitmapButton startSearchBtn;

	LinearLayout startSearchLayout;

	TitleBitmapButton recentEndBtn;
	EditText destSearchEdit;
	TitleBitmapButton destSearchBtn;

	TitleBitmapButton routeSearchBtn;
	TitleBitmapButton recentRouteBtn;

	TextView startText;
	TextView endText;


	boolean myLocationFound = false;
	LocationManager locationManager;

	private GoogleMap map;


	LatLng myGeoPoint;
	LatLng startPoint;
	LatLng destPoint;

	LocationInfo selectedStartLocation;
	LocationInfo selectedDestLocation;

	public static final int SEARCH_STARTPOINT = 2001;
	public static final int SEARCH_DESTPOINT = 2002;
	public static int locationSearchMode;

	ArrayList<LocationInfo> locationInfoList;
	ListView locationInfoListView;
	LocationInfoListAdapter locationInfoListAdapter;

	ArrayList<RouteInfo> routeInfoList;
	ListView routeInfoListView;
	RouteInfoListAdapter routeInfoListAdapter;


	StringParser strParser;
	String encodedStr;
	String searchStr;

	Handler handler = new Handler();

	public static int timeoutMs = 10000;

	TextView contentsText;

	StringBuffer StrBuf;

	int pageNo;
	int maxPageNo;
	int totalCount;

	ArrayList<GPoint> boundsList;
	ArrayList<GPoint> pathsList;


	//===== DIALOG CONSTANTS =====//

	public static final int LOCATION_INFO_LIST = 1001;
	public static final int ROUTE_INFO_LIST = 1002;
	public static final int SEARCH_PROGRESS_DIALOG = 1003;


	public ProgressDialog progressDialog;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quicknavi);


        initView();

        initMyLocation();

    }

    private void initView() {
    	myLocationCheck = (CheckBox) findViewById(R.id.myLocationCheck);
    	startSearchEdit = (EditText) findViewById(R.id.startSearchEdit);
    	startSearchBtn = (TitleBitmapButton) findViewById(R.id.startSearchBtn);

    	startSearchLayout = (LinearLayout) findViewById(R.id.startSearchLayout);

    	recentEndBtn = (TitleBitmapButton) findViewById(R.id.recentEndBtn);
    	destSearchEdit = (EditText) findViewById(R.id.destSearchEdit);
    	destSearchBtn = (TitleBitmapButton) findViewById(R.id.destSearchBtn);

    	TitleBitmapButton routeInfoBtn = (TitleBitmapButton)findViewById(R.id.routeInfoBtn);
    	routeInfoBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "routeInfoBtn clicked.");

			}
		});

        TitleBitmapButton showAllBtn = (TitleBitmapButton)findViewById(R.id.showAllBtn);
        showAllBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "showAllBtn clicked.");

				showNavigationDisplay();
			}
		});

		// 지도 객체 참조
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
     	map.setMapType(GoogleMap.MAP_TYPE_NORMAL); 
        
		// initialze location list
		locationInfoList = new ArrayList<LocationInfo>();
		locationInfoListView = new ListView(getApplicationContext());
		locationInfoListView.setCacheColorHint(Color.argb(0, 0, 0, 0));
		locationInfoListAdapter = new LocationInfoListAdapter(this, locationInfoList);
		locationInfoListView.setAdapter(locationInfoListAdapter);

		// initialze route list
		routeInfoList = new ArrayList<RouteInfo>();
		routeInfoListView = new ListView(getApplicationContext());
		routeInfoListView.setCacheColorHint(Color.argb(0, 0, 0, 0));
		routeInfoListAdapter = new RouteInfoListAdapter(this, routeInfoList);
		routeInfoListView.setAdapter(routeInfoListAdapter);
		routeInfoListView.setClickable(false);


        myLocationCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					if (myLocationFound) {
						startSearchLayout.setVisibility(View.GONE);
						LocationInfo locationInfo = new LocationInfo();

						String xCoord = String.valueOf(myGeoPoint.longitude);
						String yCoord = String.valueOf(myGeoPoint.latitude);

						locationInfo.setName("내 위치");
						locationInfo.setX(xCoord);
						locationInfo.setY(yCoord);

						selectedStartLocation = locationInfo;
						startText.setTextColor(Color.BLUE);

					} else {
						Toast.makeText(getApplicationContext(), "내 위치를 찾을 수 없습니다.", 10000).show();
						myLocationCheck.setChecked(false);
						startText.setTextColor(Color.BLACK);
					}
				} else {
					startSearchLayout.setVisibility(View.VISIBLE);
					selectedStartLocation = null;
					startText.setTextColor(Color.BLACK);
				}

			}
		});


        startSearchBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				searchStr = startSearchEdit.getText().toString();

				if(TextUtils.isEmpty(searchStr)) {
					Toast.makeText(getApplicationContext(), "출발지 검색어를 입력하세요.", 1000).show();
				} else {
					locationSearchMode = SEARCH_STARTPOINT;

					locationInfoList.clear();
					locationInfoListAdapter.notifyDataSetChanged();

					pageNo = 1;
					encodedStr = URLEncoder.encode(searchStr.trim());

					searchLocation();
				}
			}
		});

        destSearchBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				searchStr = destSearchEdit.getText().toString();

				if(TextUtils.isEmpty(searchStr)) {
					Toast.makeText(getApplicationContext(), "목적지 검색어를 입력하세요.", 1000).show();
				} else {
					locationSearchMode = SEARCH_DESTPOINT;

					locationInfoList.clear();
					locationInfoListAdapter.notifyDataSetChanged();

					pageNo = 1;
					encodedStr = URLEncoder.encode(searchStr.trim());

					searchLocation();
				}
			}
		});

        locationInfoListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				LocationInfo locationInfo = locationInfoList.get(position);

				String locationName = locationInfo.getName();
				String xCoord = locationInfo.getX();
				String yCoord = locationInfo.getY();

				Toast.makeText(getApplicationContext(), locationName + "\n" + xCoord + ", " + yCoord, 1000).show();

				switch (locationSearchMode) {
					case SEARCH_STARTPOINT:
						selectedStartLocation = locationInfo;
						startSearchEdit.setText(locationName);
						startText.setTextColor(Color.BLUE);

						break;

					case SEARCH_DESTPOINT:
						selectedDestLocation = locationInfo;
						destSearchEdit.setText(locationName);
						endText.setTextColor(Color.BLUE);

						break;
					default:
						break;
				}

				dismissDialog(LOCATION_INFO_LIST);
			}
		});


        TitleBitmapButton routeSearchBtn = (TitleBitmapButton)findViewById(R.id.routeSearchBtn);
        routeSearchBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "routeSearchBtn clicked.");

				if (selectedStartLocation != null && selectedDestLocation != null) {
					requestRoute(selectedStartLocation, selectedDestLocation);
				} else {
					Toast.makeText(getApplicationContext(), "출발지 또는 목적지를 먼저 검색하세요.", 1000).show();
				}
			}
		});

        TitleBitmapButton recentRouteBtn = (TitleBitmapButton)findViewById(R.id.recentRouteBtn);
        recentRouteBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "recentRouteBtn clicked.");

				showNavigationDisplay();
			}
		});


        startText = (TextView)findViewById(R.id.startText);
        startText.setTextColor(Color.BLACK);

        endText = (TextView)findViewById(R.id.endText);
        endText.setTextColor(Color.BLACK);

    }


	public void initMyLocation() {
		myLocationFound = false;

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		try {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			String provider = locationManager.getBestProvider(criteria, true);

			Location location = locationManager.getLastKnownLocation(provider);

			myGeoPoint = new LatLng(location.getLatitude(), location.getLongitude());

			map.animateCamera(CameraUpdateFactory.newLatLngZoom(myGeoPoint, 15));

	    	GPSListener gpsListener = new GPSListener();
			long minTime = 10000;
			float minDistance = 0;

			locationManager.requestLocationUpdates(provider, minTime, minDistance, gpsListener);
			Toast.makeText(getApplicationContext(), "Location Service started.", 2000).show();

			myLocationFound = true;

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "현 위치를 검색 할 수 없습니다.", 10000).show();
		}
	}

	private class GPSListener implements LocationListener {

	    public void onLocationChanged(Location location) {
	    	myGeoPoint = new LatLng(location.getLatitude(), location.getLongitude());
		}

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }

	}

	public void onResume() {
		super.onResume();

		map.setMyLocationEnabled(true);
	}

	public void onPause() {
		super.onPause();

		map.setMyLocationEnabled(false);
	}


	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;

		switch (id) {
			case LOCATION_INFO_LIST:
				builder = new AlertDialog.Builder(this);

				if (locationSearchMode == SEARCH_STARTPOINT) {
					builder.setTitle("출발지 선택");
				} else if (locationSearchMode == SEARCH_DESTPOINT) {
					builder.setTitle("목적지 선택");
				}

				builder.setView(locationInfoListView);

				return builder.create();
			case ROUTE_INFO_LIST:
				builder = new AlertDialog.Builder(this);

				builder.setTitle("경로 안내");
				builder.setView(routeInfoListView);

				return builder.create();

			case (SEARCH_PROGRESS_DIALOG):
	    		progressDialog = new ProgressDialog(this);
	    		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    		progressDialog.setMessage("검색중입니다.");
	    		progressDialog.setOnKeyListener(new OnKeyListener() {
	    			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	    				if (keyCode == KeyEvent.KEYCODE_BACK) {
	    					progressDialog.dismiss();
	    				}

	    				return true;
	    			}
	    		});

	    		return progressDialog;
		}

		return null;
	}

 
	private void showNavigationDisplay() {
		Intent intent = new Intent(getApplicationContext(), NavigationDisplayActivity.class);
		startActivityForResult(intent, BasicInfo.REQ_NAVI_DISPLAY_ACTIVITY);
	}


	private void searchLocation() {
		try {
			showDialog(SEARCH_PROGRESS_DIALOG);

			RequestLocationThread thread = new RequestLocationThread();
			thread.start();
		} catch (Exception e) {
			Log.e(TAG, "Error", e);
		}
	}


	class RequestLocationThread extends Thread {
		public RequestLocationThread() {
		}

		public void run() {
			try {
				Socket sock = createSocket(new URL(BasicInfo.base_url));
				DataOutputStream outstream = new DataOutputStream(sock.getOutputStream());
				DataInputStream instream = new DataInputStream(sock.getInputStream());

				String outStr = "";
				if (maxPageNo < 2) {
					outStr = "GET /search2/local.nhn?query=" + encodedStr
							+ "&menu=location HTTP/1.1\r\n";

					Log.e(TAG, "mp < 2");
				} else {
					outStr = "GET /search2/local.nhn?queryRank=1&type=SITE_1&siteOrder=41241211&siteSort=0&page="
							+ pageNo
							+ "&busLinkYN=0&re=1&query="
							+ encodedStr
							+ "&menu=location HTTP/1.1\r\n";

					Log.e(TAG, "mp =< 2");
				}

				outStr = outStr
						+ "Host: map.naver.com\r\n"
						+ "Connection: keep-alive\r\n"
						+ "User-Agent: Mozilla/5.0\r\n"
						+ "Referer: http://map.naver.com/\r\n"
						+ "Content-Type: application/x-www-form-urlencoded; charset=utf-8\r\n"
						+ "charset: utf-8\r\n" + "Accept: */*\r\n"
						+ "Accept-Encoding: gzip,deflate,sdch\r\n"
						+ "Accept-Language: ko-KR,ko\r\n"
						+ "Accept-Charset: windows-949,utf-8\r\n" + "\r\n";

				outstream.write(outStr.getBytes());
				outstream.flush();

				byte[] inBytes = new byte[1024];
				ArrayList<byte[]> ByteBuffer = new ArrayList<byte[]>();
				StrBuf = new StringBuffer();
				int totalBytes = 0;

				while (true) {
					int readCount = instream.read(inBytes, 0, inBytes.length);
					if (readCount <= 0) {
						break;
					}

					totalBytes = totalBytes + readCount;
					byte[] aBytes = new byte[readCount];
					System.arraycopy(inBytes, 0, aBytes, 0, readCount);
					ByteBuffer.add(aBytes);

					Log.d(TAG, "readCount : " + readCount);
				}

				Log.d(TAG, "totalBytes : " + totalBytes);
				byte[] outBytes = new byte[totalBytes];
				int curIndex = 0;
				for (int i = 0; i < ByteBuffer.size(); i++) {
					byte[] aBytes = ByteBuffer.get(i);
					System.arraycopy(aBytes, 0, outBytes, curIndex, aBytes.length);
					curIndex = curIndex + aBytes.length;
				}

				String inStr = new String(outBytes, 0, curIndex, "UTF8");
				StrBuf.append(inStr);


				Log.e(TAG, "full : " + StrBuf);


				// trim contents
				int curSepIndex = 0;
				int addCount = 0;
				for (int i = 0; i < outBytes.length; i++) {
					if (outBytes[i] == '\r' || outBytes[i] == '\n') {
						addCount++;
					} else {
						addCount = 0;
					}
					if (addCount > 3) {
						curSepIndex = i+1;
						break;
					}
				}

				Log.d("DEBUG", "curSepIndex : " + curSepIndex);
				String curContentsStr = "";
				if (curSepIndex > 0) {
					curContentsStr = new String(outBytes, curSepIndex, curIndex-curSepIndex, "UTF8");
				}
				Log.d("DEBUG", "curContents : " + curContentsStr);


				// content encoding
				boolean isPlain = true;
				String curStr = new String(outBytes);
				int startIndex = curStr.indexOf("Content-Encoding:");
				int endIndex = curStr.indexOf("\n", startIndex + 17);
				if (startIndex > 0 && endIndex > 0) {
					String contentTypeStr = curStr.substring(startIndex + 18, endIndex);
					Log.e(TAG, "Content-Encoding String : " + contentTypeStr);
					
					if (contentTypeStr != null && contentTypeStr.indexOf("gzip") > -1) {
						isPlain = false;
					}
				}
				 
				
				// conversion of gzip
				if (!isPlain) {
					ByteArrayInputStream curInstream = new ByteArrayInputStream(outBytes, curSepIndex, curIndex-curSepIndex);
					GZIPInputStream curStream = new GZIPInputStream(curInstream);
	
		            int totalCount = 0;
					int count = 1;
		            byte data[] = new byte[1024];
		            ArrayList<byte[]> parsedStrList = new ArrayList<byte[]>();
		            while(count > 0) {
		            	count = curStream.read(data, 0, 1024);
		            	if (count > 0) {
		            		byte[] curData = new byte[count];
		            		System.arraycopy(data, 0, curData, 0, count);
		            		parsedStrList.add(curData);
		            		totalCount = totalCount + count;
		            	}
		            }
	
		            byte[] curContentsBytes = new byte[totalCount];
		            int curOffset = 0;
		            for (int i = 0; i < parsedStrList.size(); i++) {
		            	byte[] aBytes = parsedStrList.get(i);
		            	System.arraycopy(aBytes, 0, curContentsBytes, curOffset, aBytes.length);
		            	curOffset = curOffset + aBytes.length;
		            }
	
		            String curContentsData = new String(curContentsBytes, 0, totalCount, "UTF8");
		            Log.d("DEBUG", "curContentsData : " + curContentsData);
	
					curStream.close();

					// replace the string buffer data
					StrBuf = new StringBuffer();
					StrBuf.append(curContentsData);

				}
				
				closeSocket(sock, outstream, instream);

				// post for the display of search result
				handler.post(locationSearchResultRunnable);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	private void requestRoute(LocationInfo sLoc, LocationInfo eLoc) {
		map.clear();

		routeInfoList.clear();
		routeInfoListAdapter.clear();

		double startLatitude = Double.parseDouble(sLoc.getY());
		double startLongitude = Double.parseDouble(sLoc.getX());
		double destLatitude = Double.parseDouble(eLoc.getY());
		double destLongitude = Double.parseDouble(eLoc.getX());

		startPoint = new LatLng(startLatitude, startLongitude);

		MarkerOptions startMarker = new MarkerOptions();
		startMarker.position(startPoint);
		startMarker.title("출발지점");
		startMarker.snippet("출발지점");
		startMarker.draggable(true);
		startMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_icon_01));
		
		map.addMarker(startMarker);

		destPoint = new LatLng(destLatitude, destLongitude);
		
		MarkerOptions destMarker = new MarkerOptions();
		destMarker.position(destPoint);
		destMarker.title("도착지점");
		destMarker.snippet("도착지점");
		destMarker.draggable(true);
		destMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.point_icon_02));
		
		map.addMarker(destMarker);

		try {
			showDialog(SEARCH_PROGRESS_DIALOG);

			RequestRouteThread thread = new RequestRouteThread(sLoc, eLoc);
			thread.start();
		} catch (Exception e) {
			Log.e(TAG, "Error", e);
		}

		map.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));
		
	}


	class RequestRouteThread extends Thread {

		LocationInfo startLocInfo;
		LocationInfo destLocInfo;

		public RequestRouteThread(LocationInfo sLoc, LocationInfo eLoc) {
			startLocInfo = sLoc;
			destLocInfo = eLoc;
		}

		public void run() {
			try {
				Socket sock = createSocket(new URL(BasicInfo.base_url));
				DataOutputStream outstream = new DataOutputStream(sock
						.getOutputStream());
				DataInputStream instream = new DataInputStream(sock
						.getInputStream());

				String outStr = "";
				String encodedStartName = URLEncoder.encode(startLocInfo
						.getName());
				String encodedDestName = URLEncoder.encode(destLocInfo
						.getName());
				outStr = "GET /findroute2/findCarRoute.nhn?via=&call=route2&output=json&car=0&mileage=12.4&start="
						+ startLocInfo.getX()
						+ "%2C"
						+ startLocInfo.getY()
						+ "%2C"
						+ encodedStartName // 출발지
						+ "&destination="
						+ destLocInfo.getX()
						+ "%2C"
						+ destLocInfo.getY() + "%2C" + encodedDestName // 도착지
						+ "&search=2 HTTP/1.1\r\n";

				outStr = outStr
						+ "Host: map.naver.com\r\n"
						+ "Connection: keep-alive\r\n"
						+ "User-Agent: Mozilla/5.0\r\n"
						+ "Referer: http://map.naver.com/\r\n"
						+ "Content-Type: application/x-www-form-urlencoded; charset=utf-8\r\n"
						+ "charset: utf-8\r\n" + "Accept: */*\r\n"
						+ "Accept-Encoding: gzip,deflate,sdch\r\n"
						+ "Accept-Language: ko-KR,ko\r\n"
						+ "Accept-Charset: windows-949,utf-8\r\n" + "\r\n";

				// debugging
				// Log.d(TAG, "REQUEST : " + outStr);

				Log.d(TAG, "request sending...");
				outstream.write(outStr.getBytes());
				outstream.flush();

				byte[] inBytes = new byte[1024];
				ArrayList<byte[]> ByteBuffer = new ArrayList<byte[]>();
				StrBuf = new StringBuffer();
				int totalBytes = 0;

				while (true) {
					int readCount = instream.read(inBytes, 0, inBytes.length);
					if (readCount <= 0) {
						break;
					}

					totalBytes = totalBytes + readCount;
					byte[] aBytes = new byte[readCount];
					System.arraycopy(inBytes, 0, aBytes, 0, readCount);
					ByteBuffer.add(aBytes);

					Log.d(TAG, "readCount : " + readCount);
				}

				Log.d(TAG, "totalBytes : " + totalBytes);
				byte[] outBytes = new byte[totalBytes];
				int curIndex = 0;
				for (int i = 0; i < ByteBuffer.size(); i++) {
					byte[] aBytes = ByteBuffer.get(i);
					System.arraycopy(aBytes, 0, outBytes, curIndex, aBytes.length);
					curIndex = curIndex + aBytes.length;
				}

				String inStr = new String(outBytes, 0, curIndex, "UTF8");
				StrBuf.append(inStr);


				Log.e(TAG, "full : " + StrBuf);
				String curStr = StrBuf.toString();

				outBytes = curStr.getBytes();
				
				// trim contents
				int curSepIndex = 0;
				int addCount = 0;
				for (int i = 0; i < outBytes.length; i++) {
					if (outBytes[i] == '\r' || outBytes[i] == '\n') {
						addCount++;
					} else {
						addCount = 0;
					}
					if (addCount > 3) {
						curSepIndex = i+1;
						break;
					}
				}

				Log.d("DEBUG", "curSepIndex : " + curSepIndex);
				String curContentsStr = "";
				if (curSepIndex > 0) {
					curContentsStr = new String(outBytes, curSepIndex, curIndex-curSepIndex, "UTF8");
				}
				Log.d("DEBUG", "curContents : " + curContentsStr);

				
				
				// content encoding
				boolean isPlain = true;
				int startIndex = curStr.indexOf("Content-Encoding:");
				int endIndex = curStr.indexOf("\n", startIndex + 17);
				if (startIndex > 0 && endIndex > 0) {
					String contentTypeStr = curStr.substring(startIndex + 18, endIndex);
					Log.e(TAG, "Content-Encoding String : " + contentTypeStr);
					
					if (contentTypeStr != null && contentTypeStr.indexOf("gzip") > -1) {
						isPlain = false;
					}
				}
				 
				
				// conversion of gzip
				if (!isPlain) {
					ByteArrayInputStream curInstream = new ByteArrayInputStream(outBytes, curSepIndex, curIndex-curSepIndex);
					GZIPInputStream curStream = new GZIPInputStream(curInstream);
	
					int curSize = curStream.available();
					Log.d("DEBUG", "curSize : " + curSize);
	
		            int count;
		            byte data[] = new byte[curSize];
		            count = curStream.read(data, 0, curSize);
		            Log.d("DEBUG", "curData : " + new String(data, 0, count));
	
					curStream.close();

					StrBuf = new StringBuffer();
					StrBuf.append(new String(data, 0, count));
				} else {
					
					StrBuf = new StringBuffer();
					StrBuf.append(new String(outBytes, curSepIndex, curIndex-curSepIndex));
					
				}
				
				closeSocket(sock, outstream, instream);

				// post for the display of search result
				handler.post(routeSearchResultRunnable);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	public Socket createSocket(URL url) {

		String hostname = url.getHost();
		int port = url.getPort();
		if (port < 1) {
			port = 80;
		}

		Log.d(TAG, "URL in createSocket() : " + hostname + ", " + port);

		long now = SystemClock.uptimeMillis();

		// Create a socket with a timeout
		try {
			InetAddress addr = InetAddress.getByName(hostname);

			SocketAddress sockaddr = new InetSocketAddress(addr, port);
			Socket sock = new Socket();
			sock.connect(sockaddr, timeoutMs);

			Log.d(TAG, "Connected to " + sockaddr);
			Log.d(TAG, "Spent " + (SystemClock.uptimeMillis() - now)
					+ " millis connecting to the server.");

			return sock;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void closeSocket(Socket sock, DataOutputStream outstream,
			DataInputStream instream) throws IOException {
		if (outstream != null) {
			outstream.close();
		}

		if (instream != null) {
			instream.close();
		}

		if (sock != null) {
			sock.close();
		}
	}



	Runnable locationSearchResultRunnable = new Runnable() {
		public void run() {
			try {

				String allStr = StrBuf.toString();
				//Log.d("DEBUG", "RESULT : \n\n" + allStr);
				
				// JSON 객체로 변환
				JSONObject json = new JSONObject(allStr);
				
				Iterator iter = json.keys();
				int curIndex = 0;
				while(iter.hasNext()) {
					String attName = (String) iter.next();
					//Log.d(TAG, "attribute name for root #" + curIndex + " : " + attName);
					
					if (attName != null && attName.equals("result")) {
						JSONObject resultObj = json.getJSONObject(attName);
						 
						if (resultObj != null) {
							//Log.d(TAG, "result object found.");
							Iterator resultIter = resultObj.keys();
							int curIndex2 = 0;
							while(resultIter.hasNext()) {
								String resultAttName = (String) resultIter.next();
								//Log.d(TAG, "attribute name result #" + curIndex2 + " : " + resultAttName);
								
								if (resultAttName != null && resultAttName.equals("totalCount")) {
									int totalCount = resultObj.getInt("totalCount");
									Log.d(TAG, "totalCount : " + totalCount);
								}
								
								if (resultAttName != null && resultAttName.equals("site")) {
									JSONObject siteObj = resultObj.getJSONObject(resultAttName);
									if (siteObj != null) {
										Log.d(TAG, "site object found.");
										
										Iterator siteIter = siteObj.keys();
										int curIndex3 = 0;
										while(siteIter.hasNext()) {
											String siteAttName = (String) siteIter.next();
											//Log.d(TAG, "attribute name site #" + curIndex3 + " : " + siteAttName);
											if (siteAttName != null && siteAttName.equals("list")) {
												JSONArray listArr = siteObj.getJSONArray(siteAttName);
												if (listArr != null) {
													Log.d(TAG, "list array object found.");
												
													for (int i = 0; i < listArr.length(); i++) {
														JSONObject itemObj = (JSONObject) listArr.get(i);
														Iterator itemIter = itemObj.keys();

														Log.d(TAG, "processing record #" + i + " ...");
														LocationInfo aInfo = new LocationInfo();

														String nameStr = "";
														String xStr = "";
														String yStr = "";
														
														int curIndex4 = 0;
														while(itemIter.hasNext()) {
															String itemAttrName = (String) itemIter.next();
															//Log.d(TAG, "attribute name item #" + curIndex4 + " : " + itemAttrName);
														
															try {
																
																// name
																if (itemAttrName != null && itemAttrName.equals("name")) {
																	nameStr = itemObj.getString(itemAttrName);
																	aInfo.setName(nameStr);
																}

																// tel
																if (itemAttrName != null && itemAttrName.equals("tel")) {
																	String telStr = itemObj.getString(itemAttrName);
																	aInfo.setTel(telStr);
																}

																// address
																if (itemAttrName != null && itemAttrName.equals("address")) {
																	String addressStr = itemObj.getString(itemAttrName);
																	aInfo.setAddress(addressStr);
																}
																 
																// x
																if (itemAttrName != null && itemAttrName.equals("x")) {
																	xStr = itemObj.getString(itemAttrName);
																	aInfo.setX(xStr);
																}
																 
																// y
																if (itemAttrName != null && itemAttrName.equals("y")) {
																	yStr = itemObj.getString(itemAttrName);
																	aInfo.setY(yStr);
																}
 
															} catch (Exception ext) {
																ext.printStackTrace();
																break;
															}
 
															curIndex4++;
														}
														

														Log.d(TAG, "name : " + nameStr + ", x : " + xStr + ", y : " + yStr);

														locationInfoList.add(aInfo);

													}
												}
											}
											
											curIndex3++;
										}
									}
									
								}
								 
								curIndex2++;
							}
						}
					}
					
					curIndex++;
				}
				
				
				locationInfoListAdapter.notifyDataSetChanged();


				dismissDialog(SEARCH_PROGRESS_DIALOG);

				showDialog(LOCATION_INFO_LIST);


				handler.removeCallbacks(locationSearchResultRunnable);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				Thread.sleep(500);
			} catch (Exception ext) {
				ext.printStackTrace();
			}

			// get more
			if (pageNo < maxPageNo) {
				// getNextPage();
			} else {

			}

		}
	};

	Runnable routeSearchResultRunnable = new Runnable() {
		public void run() {

			String allStr = StrBuf.toString();
			
			try {

				strParser = new StringParser(getApplicationContext());

				// initialize lists
				boundsList = new ArrayList<GPoint>();
				pathsList = new ArrayList<GPoint>();

				char[] allBytes = allStr.toCharArray();

				String targetStr = null;
				int foundLength = 0;
				int startIndex = 0;
				int endIndex = 0;

				strParser.setCurrentOffset(0);

				// parse totalDistance
				String foundStr = strParser.findData(allStr, allBytes,
						"totalDistance\":", ",");
				if (foundStr == null) {
					Log.d(TAG, "foundStr is null.");
				}
				// Log.d(TAG, "found totalDistance : " + foundStr);
				String totalDistance = foundStr;

				// route section
				foundStr = strParser.findData(allStr, allBytes, "point\":", "]");
				if (foundStr == null) {
					Log.d(TAG, "foundStr is null.");
				}
				// Log.d(TAG, "found route section : " + foundStr);

				String routeSectionStr = foundStr;
				char[] routeSectionBytes = routeSectionStr.toCharArray();

				// path
				foundStr = strParser.findData(allStr, allBytes, "path\":\"",
						"\"");
				if (foundStr == null) {
					Log.d(TAG, "foundStr is null.");
				}
				// Log.d(TAG, "found path : " + foundStr);

				String[] paths = foundStr.split("\\ ");
				if (paths == null) {
					Log.d(TAG, "paths is invalid.");
				}
				Log.d(TAG, "length of path points : " + paths.length);

				for (int i = 0; i < paths.length; i++) {
					String[] outCoord = paths[i].split("\\,");
					if (outCoord == null || outCoord.length < 2) {
						Log.d(TAG, "splitted coord is null or less than 2 : "
								+ paths[i]);
						continue;
					}
					GPoint outPnt = transformPoint(outCoord[0], outCoord[1]);
					if (outPnt == null) {
						Log.d(TAG, "out point is null.");
						continue;
					}
					pathsList.add(outPnt);

					// Log.d(TAG, "out path #" + i + " : " + outPnt.y + ", " +
					// outPnt.x);
				}

				// bound
				foundStr = strParser.findData(allStr, allBytes, "bound\":\"",
						"\"");
				if (foundStr == null) {
					Log.d(TAG, "foundStr is null.");
				}
				// Log.d(TAG, "found bound : " + foundStr);

				String[] bounds = foundStr.split("\\,");
				if (bounds == null || bounds.length != 4) {
					Log.d(TAG, "bounds is invalid.");
				}
				Log.d(TAG, "bounds : " + bounds[0] + ", " + bounds[1] + ", "
						+ bounds[2] + ", " + bounds[3]);

				GPoint outPnt = transformPoint(bounds[0], bounds[1]);
				boundsList.add(outPnt);

				outPnt = transformPoint(bounds[2], bounds[3]);
				boundsList.add(outPnt);

				int routeItemCount = 0;
				int routeSectionOffset = 0;

				strParser.setCurrentOffset(0);

				ArrayList<String> routeItemList = new ArrayList<String>();
				while (true) {
					Log.d(TAG, "RouteItem #" + routeItemCount);

					targetStr = "{\"name\":\"";
					startIndex = strParser.findString(routeSectionBytes,
							targetStr.toCharArray(), strParser
									.getCurrentOffset());
					foundLength = targetStr.length();

					targetStr = "{\"name\":\"";
					endIndex = strParser.findString(routeSectionBytes,
							targetStr.toCharArray(), startIndex + foundLength);
					strParser.setCurrentOffset(endIndex);

					if (endIndex < 0) {
						Log.d(TAG, "foundStr is null.");

						String outStr = routeSectionStr
								.substring(routeSectionOffset);
						// Log.d(TAG, "found route item : " + outStr);
						routeItemList.add(outStr);
						break;
					} else {
						foundStr = routeSectionStr.substring(startIndex,
								endIndex);

						// Log.d(TAG, "found route item : " + foundStr);
						routeItemList.add(foundStr);
					}

					routeItemCount++;
					routeSectionOffset = strParser.getCurrentOffset() - 1;
				}

				for (int i = 0; i < routeItemList.size(); i++) {
					Log.d(TAG, "processing RouteItem #" + i);

					String routeItemStr = routeItemList.get(i);
					char[] routeItemBytes = routeItemStr.toCharArray();

					// name
					targetStr = "name\":\"";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), 0);
					foundLength = targetStr.length();

					targetStr = "\"";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found route name : " + foundStr);
					String routeName = foundStr;

					foundLength = targetStr.length();

					// key
					targetStr = "key\":\"";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = "\"";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found route key : " + foundStr);
					String routeKey = foundStr;

					foundLength = targetStr.length();

					// x
					targetStr = "x\":";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = ",";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found route x : " + foundStr);
					String routeX = foundStr;

					foundLength = targetStr.length();

					// y
					targetStr = "y\":";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = ",";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found route y : " + foundStr);
					String routeY = foundStr;

					foundLength = targetStr.length();

					// guide no
					targetStr = "no\":";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = ",";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found guide no : " + foundStr);
					String guideNo = foundStr;

					foundLength = targetStr.length();

					// guide name
					targetStr = "name\":\"";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = "\"";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);
					// Log.d(TAG, "found guide name : " + foundStr);
					String guideName = foundStr;

					foundLength = targetStr.length();

					// road distance
					targetStr = "distance\":";
					startIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), endIndex + foundLength);
					foundLength = targetStr.length();

					targetStr = ",";
					endIndex = strParser.findString(routeItemBytes, targetStr
							.toCharArray(), startIndex + foundLength);

					foundStr = routeItemStr.substring(startIndex + foundLength,
							endIndex);

					// Log.d(TAG, "found road distance : " + foundStr);
					String roadDistance = foundStr;

					foundLength = targetStr.length();

					RouteInfo aInfo = new RouteInfo();
					aInfo.setName(routeName);
					aInfo.setKey(routeKey);
					aInfo.setX(routeX);
					aInfo.setY(routeY);
					aInfo.setGuideNo(guideNo);
					aInfo.setGuideName(guideName);
					aInfo.setRoadDistance(roadDistance);

					routeInfoList.add(aInfo);

					if(i == routeItemList.size()-1){
						RouteInfo destInfo = new RouteInfo();
						destInfo.setName(selectedDestLocation.getName());
						destInfo.setX(selectedDestLocation.getX());
						destInfo.setY(selectedDestLocation.getY());
						destInfo.setRoadDistance("0");
						destInfo.setGuideName("도착");
						routeInfoList.add(destInfo);

					}

				}

				routeInfoListAdapter.notifyDataSetChanged();

				dismissDialog(SEARCH_PROGRESS_DIALOG);

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try {
				Thread.sleep(200);
				drawRoute(Color.BLUE);


				handler.removeCallbacks(routeSearchResultRunnable);

			} catch (Exception ext) {
				ext.printStackTrace();
			}
		}
	};


	private GPoint transformPoint(String xStr, String yStr) {
		double x = 0.0D;
		double y = 0.0D;
		try {
			x = new Double(xStr).doubleValue();
			y = new Double(yStr).doubleValue();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return transformPoint(x, y);
	}

	private GPoint transformPoint(double x, double y) {

		double utmX = (x - 340000000.0D) / 10.0D;
		double utmY = (y - 130000000.0D) / 10.0D;

		GPoint pnt = new GPoint(utmX, utmY);

		GPoint newPnt = CoordinateTransformation.fromRectangularToGeodetic(pnt);

		newPnt.x *= UTMK.u;
		newPnt.y *= UTMK.u;

		return newPnt;
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
		
		handler.removeCallbacks(routeSearchResultRunnable);
	}

}