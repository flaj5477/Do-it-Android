package org.androidtown.quicknavi;

import android.content.Context;
import android.util.Log;

public class StringParser {

	Context mContext;

	public StringParser(Context context) {

		mContext = context;
	}

	int currentOffset;

	public void setCurrentOffset(int offset) {
		currentOffset = offset;
	}
	
	public int getCurrentOffset() {
		return currentOffset;
	}

	public String findData(String allStr, char[] allBytes,
			String startTargetStr, String endTargetStr) throws Exception {

		Log.i("YD", startTargetStr + ", " + endTargetStr);
		Log.i("YD", "" + currentOffset);

		int startIndex = findString(allBytes, startTargetStr.toCharArray(),
				currentOffset);
		int foundLength = startTargetStr.length();
		currentOffset = startIndex + foundLength;
		if (startIndex < 1) {
			return null;
		}

		int endIndex = findString(allBytes, endTargetStr.toCharArray(),
				currentOffset);

		if (endIndex < 1) {
			return null;
		}

		String foundStr = allStr.substring(startIndex + foundLength, endIndex);
		foundLength = endTargetStr.length();
		currentOffset = endIndex + foundLength;

		Log.i("YD", "foundStr :" + foundStr);
		
		return foundStr;
		
	}

	public int findString(char[] allBytes, char[] findBytes, int offset) {
		int outIndex = -1;
		int curIndex = 0;

		for (int i = offset; i < allBytes.length; i++) {
			if (allBytes[i] == findBytes[curIndex]) {
//				 Log.d("YD", "" + allBytes[i]);
				curIndex++;
				if (curIndex >= findBytes.length) {
					// Log.d(TAG, "found ++");
					outIndex = i - findBytes.length + 1;
					break;
				}
			} else {
				curIndex = 0;
			}
		}

		return outIndex;
	}

}
