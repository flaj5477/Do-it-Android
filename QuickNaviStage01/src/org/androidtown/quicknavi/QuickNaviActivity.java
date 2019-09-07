package org.androidtown.quicknavi;

import org.androidtown.quicknavi.common.TitleBitmapButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class QuickNaviActivity extends Activity {

	public static final String TAG = "QuickNaviActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quicknavi);


        TitleBitmapButton showAllBtn = (TitleBitmapButton)findViewById(R.id.showAllBtn);
        showAllBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "showAllBtn clicked.");

				showNavigationDisplay();
			}
		});

    }

	private void showNavigationDisplay() {
		Intent intent = new Intent(getApplicationContext(), NavigationDisplayActivity.class);
		startActivityForResult(intent, BasicInfo.REQ_NAVI_DISPLAY_ACTIVITY);
	}

}