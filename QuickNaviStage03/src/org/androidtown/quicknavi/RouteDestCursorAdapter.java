package org.androidtown.quicknavi;

import org.androidtown.quicknavi.db.RouteDatabase;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class RouteDestCursorAdapter extends CursorAdapter {

	public static final String TAG = "RouteDataCursorAdapter";

	TextView dName;
	TextView dAddress;
	TextView dLongi;
	TextView dLati;

	Cursor curCursor;

	public RouteDestCursorAdapter(Context context, Cursor c) {
		super(context, c);

		curCursor = c;
	}

	public int getCount() {
		int count = 0;

		if (curCursor != null) {
			count = curCursor.getCount();
		}

		return count;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		dName.setText(cursor.getString(cursor.getColumnIndexOrThrow(RouteDatabase.COL_DNAME)));
		dAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(RouteDatabase.COL_DADDRESS)));
		//dLongi.setText(cursor.getString(cursor.getColumnIndexOrThrow(RouteDatabase.COL_DLONG)));
		//dLati.setText(cursor.getString(cursor.getColumnIndexOrThrow(RouteDatabase.COL_DLAT)));

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "newView() called.");

		LayoutInflater inflater = LayoutInflater.from(context);
		View item = inflater.inflate(R.layout.recentlydestitem, parent, false);

		dName = (TextView)item.findViewById(R.id.destName);
		dAddress = (TextView)item.findViewById(R.id.destAddress);
		//dLongi = (TextView)item.findViewById(R.id.destLongi);
		//dLati = (TextView)item.findViewById(R.id.destLati);



		return item;
	}
}
