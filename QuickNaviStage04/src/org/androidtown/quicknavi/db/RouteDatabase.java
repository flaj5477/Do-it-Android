package org.androidtown.quicknavi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RouteDatabase {

	/**
	 * TAG for debugging
	 */
	public static final String TAG = "RouteDatabase";

	/**
	 * Singleton instance
	 */
	private static RouteDatabase database;


	/**
	 * database name
	 */
	public static String DATABASE_NAME = "/sdcard/QuickNavi/route.db";

	/**
	 * table name for ROUTE
	 */
	public static String TABLE_ROUTE = "ROUTE";

	public static String COL_ID = "_id";
	public static String COL_SNAME = "START_NAME";
	public static String COL_SLAT = "START_LAT";
	public static String COL_SLONG = "START_LONG";
	public static String COL_SADDRESS = "START_ADDRESS";
	public static String COL_STEL = "START_TEL";

	public static String COL_DNAME = "DEST_NAME";
	public static String COL_DLAT = "DEST_LAT";
	public static String COL_DLONG = "DEST_LONG";
	public static String COL_DADDRESS = "DEST_ADDRESS";
	public static String COL_DTEL = "DEST_TEL";

	public static String COL_TIME = "CREATE_DATE";

    /**
     * version
     */
	public static int DATABASE_VERSION = 1;


    /**
     * Helper class defined
     */
    private DatabaseHelper dbHelper;

    /**
     * Database object
     */
    private SQLiteDatabase db;


    private Context context;

    /**
     * Constructor
     */
	private RouteDatabase(Context context) {
		this.context = context;
	}


	public static RouteDatabase getInstance(Context context) {
		if (database == null) {
			database = new RouteDatabase(context);
		}

		return database;
	}

	/**
	 * open database
	 *
	 * @return
	 */
    public boolean open() {
    	println("opening database [" + DATABASE_NAME + "].");

    	dbHelper = new DatabaseHelper(context);
    	db = dbHelper.getWritableDatabase();

    	return true;
    }

    /**
     * close database
     */
    public void close() {
    	println("closing database [" + DATABASE_NAME + "].");
    	db.close();

    	database = null;
    }

    /**
     * execute raw query using the input SQL
     * close the cursor after fetching any result
     *
     * @param SQL
     * @return
     */
    public Cursor executeQuery(String SQL) {
		println("\nexecuteQuery called.\n");

		Cursor c1 = null;
		try {
			c1 = db.rawQuery(SQL, null);
			println("cursor count : " + c1.getCount());
		} catch(Exception ex) {
    		Log.e(TAG, "Exception in executeQuery", ex);
    	}

		return c1;
	}

    public boolean execute(String SQL) {
		println("\nexecute called.\n");

		try {
			Log.d(TAG, "SQL : " + SQL);
			db.execSQL(SQL);
	    } catch(Exception ex) {
			Log.e(TAG, "Exception in executeQuery", ex);
			return false;
		}

		return true;
	}




    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
        	Log.d(TAG, "onCreate() called.");

        	// TABLE_ROUTE
        	println("creating table [" + TABLE_ROUTE + "].");

        	// drop existing table
        	String DROP_SQL = "drop table if exists " + TABLE_ROUTE;
        	try {
        		db.execSQL(DROP_SQL);
        	} catch(Exception ex) {
        		Log.e(TAG, "Exception in DROP_SQL", ex);
        	}

        	// create table
        	String CREATE_SQL = "create table " + TABLE_ROUTE + "("
		        			+ COL_ID + " INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT, "
		        			+ COL_SNAME + " TEXT DEFAULT '', "
		        			+ COL_SLONG + " TEXT DEFAULT '', "
		        			+ COL_SLAT + " TEXT DEFAULT '', "
		        			+ COL_SADDRESS + " TEXT DEFAULT '', "
		        			+ COL_STEL + " TEXT DEFAULT '', "
		        			+ COL_DNAME + " TEXT DEFAULT '', "
		        			+ COL_DLONG + " TEXT DEFAULT '', "
		        			+ COL_DLAT + " TEXT DEFAULT '', "
		        			+ COL_DADDRESS + " TEXT DEFAULT '', "
		        			+ COL_DTEL + " TEXT DEFAULT '', "
		        			+ COL_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
		        			+ ")";
            try {
            	db.execSQL(CREATE_SQL);
            } catch(Exception ex) {
        		Log.e(TAG, "Exception in CREATE_SQL", ex);
        	}
        }

        public void onOpen(SQLiteDatabase db)
        {
        	println("opened database [" + DATABASE_NAME + "].");

        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
        	println("Upgrading database from version " + oldVersion + " to " + newVersion + ".");



        }
    }

    private void println(String msg) {
    	Log.d(TAG, msg);
    }


}