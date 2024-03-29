package com.example.hjmemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;

/**
 * 색상 선택 대화상자
 *
 *
 */
public class ColorPaletteDialog extends Activity {

	GridView mGridView;
	Button mCloseBtn;
	ColorDataAdapter mAdapter;

	public static OnColorSelectedListener mSelectedListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handwriting_dialog);

        this.setTitle(R.string.color_selection_title);

        mGridView = (GridView) findViewById(R.id.colorGrid);
        mCloseBtn = (Button) findViewById(R.id.closeBtn);

        mGridView.setColumnWidth(12);
        mGridView.setBackgroundColor(Color.GRAY);
        mGridView.setVerticalSpacing(4);
        mGridView.setHorizontalSpacing(4);

        mAdapter = new ColorDataAdapter(this);
        mGridView.setAdapter(mAdapter);
        mGridView.setNumColumns(mAdapter.getNumColumns());

        mCloseBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });

	}

}

/**
 * Adapter for Color Data
 *
 * @author Mike
 */
class ColorDataAdapter extends BaseAdapter {

	/**
	 * Application Context
	 */
	Context mContext;

	/**
	 * Colors defined
	 */
    public static final int [] colors = new int[] {
        0xff000000,0xff00007f,0xff0000ff,0xff007f00,0xff007f7f,0xff00ff00,0xff00ff7f,
        0xff00ffff,0xff7f007f,0xff7f00ff,0xff7f7f00,0xff7f7f7f,0xffff0000,0xffff007f,
        0xffff00ff,0xffff7f00,0xffff7f7f,0xffff7fff,0xffffff00,0xffffff7f,0xffffffff
    };

	int rowCount;
	int columnCount;



	public ColorDataAdapter(Context context) {
		super();

		mContext = context;

		// create test data
		rowCount = 3;
		columnCount = 7;

	}

	public int getNumColumns() {
		return columnCount;
	}

	public int getCount() {
		return rowCount * columnCount;
	}

	public Object getItem(int position) {
		return colors[position];
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View view, ViewGroup group) {
		Log.d("ColorDataAdapter", "getView(" + position + ") called.");

		// calculate position
		int rowIndex = position / rowCount;
		int columnIndex = position % rowCount;
		Log.d("ColorDataAdapter", "Index : " + rowIndex + ", " + columnIndex);

		GridView.LayoutParams params = new GridView.LayoutParams(
				GridView.LayoutParams.FILL_PARENT,
				GridView.LayoutParams.FILL_PARENT);



		// create a Button with the color
		ImageButton aItem = new ImageButton(mContext);
		aItem.setMinimumHeight(80);
		aItem.setLayoutParams(params);
		aItem.setPadding(4, 4, 4, 4);
		aItem.setBackgroundColor(colors[position]);
		aItem.setTag(colors[position]);

		// set listener
		aItem.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (ColorPaletteDialog.mSelectedListener != null) {
					ColorPaletteDialog.mSelectedListener.onColorSelected(((Integer)v.getTag()).intValue());
				}

				((ColorPaletteDialog)mContext).finish();
			}
		});

		return aItem;
	}
}


