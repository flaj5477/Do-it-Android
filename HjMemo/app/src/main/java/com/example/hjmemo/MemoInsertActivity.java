package com.example.hjmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MemoInsertActivity extends Activity {

    public static final String TAG = "MemoInsertActivity";

    ImageButton DeleteBtn;      //삭제버튼
    ImageButton VoiceBtn;       //음성버튼
    ImageButton HandwritingBtn; //손그림버튼
    ImageButton PhotoBtn;       //사진버튼
    ImageButton SaveBtn;        //저장버튼

    EditText mTitleEdit;
    TextView date_space;
    EditText mMemoEdit;         //텍스트 표시 객체
    ImageView mPhoto;           //사진 표시 객체
    ImageView insert_handwriting;    //손그림 표시 객체

    String mMemoMode;           //메모 모드(손그림입력모드인지, 그런거...?)
    String mMemoId;             //메모 아이디
    String mMemoDate;           //메모 날짜

    String mMediaPhotoId;       //사진 아이디
    String mMediaPhotoUri;      //사진 uri주소
    String mMediaVoiceId;       //음성 아이디
    String mMediaVoiceUri;      //음성 uri주소
    String mMediaHandwritingId; //손그림 아이디
    String mMediaHandwritingUri;//손그림 uri주소

    String tempPhotoUri;        //임시 사진 uri
    String tempVoiceUri;        //임시 음성 uri
    String tempHandwritingUri;  //임시 손그림 uri

    String mDateStr;            //날짜 스트링
    String mTitleStr;
    String mMemoStr;            //메모 스트링

    Bitmap resultPhotoBitmap;   //결과 사진 비트맵
    Bitmap resultHandwritingBitmap; //결과 손그림 비트맵

    boolean isPhotoCaptured;
    boolean isVoiceRecorded;
    boolean isHandwritingMade;

    boolean isPhotoFileSaved;
    boolean isVoiceFileSaved;
    boolean isHandwritingFileSaved;

    boolean isPhotoCanceled;
    boolean isVoiceCanceled;
    boolean isHandwritingCanceled;

    Calendar mCalendar = Calendar.getInstance();
    TextView insert_date;

    int mSelectdContentArray;
    int mChoicedArrayItem;

    int textViewMode = 0;
    EditText insert_memoEdit;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_insert_activity);

        mTitleEdit = (EditText) findViewById(R.id.title);
        mMemoEdit = (EditText) findViewById(R.id.memoEdit);
        mPhoto = (ImageView) findViewById(R.id.photo_space);
        
        insert_handwriting = (ImageView)findViewById(R.id.handwt_space);

        mPhoto.setOnClickListener(new View.OnClickListener() {      //사진을 클릭하면 다이얼로그 띄우기!
            public void onClick(View v) {
                if (isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(BasicInfo.CONTENT_PHOTO_EX);     //2005
                } else {
                    showDialog(BasicInfo.CONTENT_PHOTO);        //2001
                }
            }
        });


        setBottomButtons(); //제일 마지막 버튼 셋팅하기! (저장, 닫기-> 삭제, 음성, 손그림, 사진, 저장)

        setCalendar();      //날짜 지정하기

        Intent intent = getIntent();
        mMemoMode = intent.getStringExtra(BasicInfo.KEY_MEMO_MODE);     //메모 모드에 (메모모드) 설정!
        if (mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {  //메모모드가 수정이거나 보기 모드 일때
            processIntent(intent);

            //제목이랑 내용에 DB에 있는 내용 가져오기?
            //원래는 타이틀에 "새매모" or "메모보기", 바닥 버튼에 "저장" or "수정" 이런 코드 있었음

        } else {     //새로 입력하는 모드일때

        }
    }

    public void processIntent(Intent intent) {          //DB에서 내용 가져오는 부분인듯?
        mMemoId = intent.getStringExtra(BasicInfo.KEY_MEMO_ID);
        mTitleEdit.setText(intent.getStringExtra(BasicInfo.KEY_MEMO_TITLE));
        mMemoEdit.setText(intent.getStringExtra(BasicInfo.KEY_MEMO_TEXT));
        mMediaPhotoId = intent.getStringExtra(BasicInfo.KEY_ID_PHOTO);
        mMediaPhotoUri = intent.getStringExtra(BasicInfo.KEY_URI_PHOTO);
        mMediaVoiceId = intent.getStringExtra(BasicInfo.KEY_ID_VOICE);
        mMediaVoiceUri = intent.getStringExtra(BasicInfo.KEY_URI_VOICE);
        mMediaHandwritingId = intent.getStringExtra(BasicInfo.KEY_ID_HANDWRITING);
        mMediaHandwritingUri = intent.getStringExtra(BasicInfo.KEY_URI_HANDWRITING);

        setMediaImage(mMediaPhotoId, mMediaPhotoUri, mMediaVoiceId, mMediaHandwritingId);
    }

    public void showVoiceRecordingActivity() {
        Log.d(TAG,"화정 -> showVoiceRecording Activity 까지 실행함");
        Intent intent = new Intent(getApplicationContext(), VoiceRecordingActivity.class);
        startActivityForResult(intent, BasicInfo.REQ_VOICE_RECORDING_ACTIVITY);
    }

    public void showVoicePlayingActivity() {
        Intent intent = new Intent(getApplicationContext(), VoicePlayActivity.class);
        intent.putExtra(BasicInfo.KEY_URI_VOICE, BasicInfo.FOLDER_VOICE + tempVoiceUri);
        startActivity(intent);
    }


    //미디어 이미지 지정
    public void setMediaImage(String photoId, String photoUri, String voiceId, String handwritingId) {
        Log.d(TAG, "[photoId : " + photoId + ", photoUri : " + photoUri + ", handwritingId : " + handwritingId + " ]*******************************************************************");

        if (photoId.equals("") || photoId.equals("-1")) {
            mPhoto.setImageResource(R.drawable.add_photo);
        } else {
            isPhotoFileSaved = true;
            mPhoto.setImageURI(Uri.parse(BasicInfo.FOLDER_PHOTO + photoUri));
        }

        if(handwritingId.equals("") || handwritingId.equals("-1")) {

        } else {
            isHandwritingFileSaved = true;
            tempHandwritingUri = mMediaHandwritingUri;

            Bitmap resultBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_HANDWRITING + tempHandwritingUri);
            Log.d(TAG,"손그림 비트맵 경로? -> " + BasicInfo.FOLDER_HANDWRITING + tempHandwritingUri);
            insert_handwriting.setImageBitmap(resultBitmap);
        }
        
        
    }

    /**
     * 삭제, 음성, 손그림, 사진, 저장 버튼 설정!
     */
    public void setBottomButtons() {
        //미디어 레이아웃 설정!
        isPhotoCaptured = false;
        isVoiceRecorded = false;
        isHandwritingMade = false;

        //버튼을 레이아웃과 연결!
        DeleteBtn = (ImageButton) findViewById(R.id.deleteBtn);      //삭제버튼
        VoiceBtn = (ImageButton) findViewById(R.id.voiceBtn);       //음성버튼
        HandwritingBtn = (ImageButton) findViewById(R.id.handwritingBtn); //손그림버튼
        PhotoBtn = (ImageButton) findViewById(R.id.photoBtn);       //사진버튼
        SaveBtn = (ImageButton) findViewById(R.id.saveBtn);        //저장버튼

        //삭제버튼 (새 매모 일때는 닫기와 같고, 수정메모 일때는 삭제버튼과 같음)
        DeleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {  //메모모드가 수정이거나 보기 모드 일때
                    showDialog(BasicInfo.CONFIRM_DELETE);                    //삭제 후 닫기
                } else {        //메모모드가 입력일때
                    finish();   //그냥 닫기
                }
            }
        });

        //음성버튼
        VoiceBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(isVoiceRecorded || isVoiceFileSaved)
                {
                    showDialog(BasicInfo.CONTENT_VOICE_EX);     //실행하는 다이얼로그
                }
                else
                {
                    showDialog(BasicInfo.CONTENT_VOICE);        //녹음하는 다이얼로그
                }
            }
        });


        //손그림버튼
        HandwritingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HandwritingMakingActivity.class);
                startActivityForResult(intent, BasicInfo.REQ_HANDWRITING_MAKING_ACTIVITY);
            }
        });

        //사진버튼
        PhotoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(BasicInfo.CONTENT_PHOTO_EX);     //2005
                } else {
                    showDialog(BasicInfo.CONTENT_PHOTO);        //2001
                }
            }
        });


        // 저장 버튼
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean isParsed = parseValues();
                if (isParsed) {
                    if (mMemoMode.equals(BasicInfo.MODE_INSERT)) {   //새메모 모드면 saveInput()하고
                        saveInput();
                    } else if (mMemoMode.equals(BasicInfo.MODE_MODIFY) || mMemoMode.equals(BasicInfo.MODE_VIEW)) {   //수정메모 모드면 modifyInput()실행
                        modifyInput();
                    }
                }
            }
        });
    }


    /**
     * 저장버튼 눌렀을때 DB에 저장!
     */
    private void saveInput() {

        String photoFilename = insertPhoto();       //Photo테이블에 사진 입력 하고!
        int photoId = -1;

        String SQL = null;

        if (photoFilename != null) {
            // query picture id
            SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";   //Photo테이블에서 사진id 검색해서
            Log.d(TAG, "SQL : " + SQL);
            if (MultiMemoActivity.mDatabase != null) {
                Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
                if (cursor.moveToNext()) {
                    photoId = cursor.getInt(0); //검색한 사진 id 넣음
                }
                cursor.close();
            }
        }

        //손그림 저장하고
        String handwritingFileName = insertHandwriting();
        int handwritingId = -1;

        if (handwritingFileName != null) {
            // query picture id
            SQL = "select _ID from " + MemoDatabase.TABLE_HANDWRITING + " where URI = '" + handwritingFileName + "'";
            Log.d(TAG, "SQL : " + SQL);
            if (MultiMemoActivity.mDatabase != null) {
                Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
                if (cursor.moveToNext()) {
                    handwritingId = cursor.getInt(0);
                }
                cursor.close();
            }
        }

        //memo테이블에 입력하는 sql문
        SQL = "insert into " + MemoDatabase.TABLE_MEMO +
                "(INPUT_DATE, TITLE_TEXT, CONTENT_TEXT, ID_PHOTO, ID_VOICE, ID_HANDWRITING) values(" +
                "DATETIME('" + mDateStr + "'), " +
                "'" + mTitleStr + "', " +
                "'" + mMemoStr + "', " +
                "'" + photoId + "', " +
                "'" + "" + "', " +
                "'" + handwritingId + "')";

        Log.d(TAG, "SQL : " + SQL);
        if (MultiMemoActivity.mDatabase != null) {
            MultiMemoActivity.mDatabase.execSQL(SQL);   //sql문 실행
        }

        Intent intent = getIntent();    //새로운 인텐트
        setResult(RESULT_OK, intent);   //결과 출력
        finish();
    }

    /**
     * 수정버튼 눌렀을 때 DB에 저장
     */
    private void modifyInput() {

        Intent intent = getIntent();

        String photoFilename = insertPhoto();
        int photoId = -1;

        String SQL = null;

        if (photoFilename != null) {    //들어간 사진이 있을때
            // query picture id
            SQL = "select _ID from " + MemoDatabase.TABLE_PHOTO + " where URI = '" + photoFilename + "'";   // DB의 사진테이블에서 사진아이디 검색
            Log.d(TAG, "SQL : " + SQL);
            if (MultiMemoActivity.mDatabase != null) {
                Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
                if (cursor.moveToNext()) {
                    photoId = cursor.getInt(0); //사진 아이디 가져옴
                }
                cursor.close();

                mMediaPhotoUri = photoFilename; //사진 uri도 바꾼사진 uri로 바꿈

                //메모테이블의 사진id를 바꿈 (사진테이블에 참조하는 사진uri가 달라짐)
                SQL = "update " + MemoDatabase.TABLE_MEMO +
                        " set " +
                        " ID_PHOTO = '" + photoId + "'" +
                        " where _id = '" + mMemoId + "'";

                if (MultiMemoActivity.mDatabase != null) {
                    MultiMemoActivity.mDatabase.rawQuery(SQL);
                }

                mMediaPhotoId = String.valueOf(photoId);    //사진 아이디를 바꿈
            }
        } else if (isPhotoCanceled && isPhotoFileSaved) {    //사진이 삭제되거나 사진이 저장되어있으면,
            SQL = "delete from " + MemoDatabase.TABLE_PHOTO +   //사진테이블에서 해당 사진id를 삭제함
                    " where _ID = '" + mMediaPhotoId + "'";
            Log.d(TAG, "SQL : " + SQL);
            if (MultiMemoActivity.mDatabase != null) {
                MultiMemoActivity.mDatabase.execSQL(SQL);
            }

            File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri); //실제 파일도 지움
            if (photoFile.exists()) {
                photoFile.delete();
            }

            //memo테이블 수정!
            SQL = "update " + MemoDatabase.TABLE_MEMO +
                    " set " +
                    " ID_PHOTO = '" + photoId + "'" +
                    " where _id = '" + mMemoId + "'";

            if (MultiMemoActivity.mDatabase != null) {
                MultiMemoActivity.mDatabase.rawQuery(SQL);
            }

            mMediaPhotoId = String.valueOf(photoId);
        }

        //손그림 저장
        String handwritingFileName = insertHandwriting();
        int handwritingId = -1;

        if (handwritingFileName != null) {
            // query picture id
            SQL = "select _ID from " + MemoDatabase.TABLE_HANDWRITING + " where URI = '" + handwritingFileName + "'";
            Log.d(TAG, "SQL : " + SQL);
            if (MultiMemoActivity.mDatabase != null) {
                Cursor cursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
                if (cursor.moveToNext()) {
                    handwritingId = cursor.getInt(0);
                    Log.d (TAG, "Handwrite_SQL_cursor: " + handwritingId);        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~지우기~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                }
                cursor.close();

                mMediaHandwritingUri = handwritingFileName;

                SQL = "update " + MemoDatabase.TABLE_MEMO +
                        " set " +
                        " ID_HANDWRITING = '" + handwritingId + "' " +
                        " where _id = '" + mMemoId + "'";
                Log.d(TAG, "Handwrite_SQL: " + SQL);                     //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~지우기~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (MultiMemoActivity.mDatabase != null) {
                    MultiMemoActivity.mDatabase.rawQuery(SQL);
                }

                mMediaHandwritingId = String.valueOf(handwritingId);
            }
        } else if(isHandwritingCanceled && isHandwritingFileSaved) {
            SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
                    " where _ID = '" + mMediaHandwritingId + "'";
            Log.d(TAG, "Handwrite_SQL: " + SQL);                     //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~지우기~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            if (MultiMemoActivity.mDatabase != null) {
                MultiMemoActivity.mDatabase.execSQL(SQL);
            }

            File handwritingFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
            if (handwritingFile.exists()) {
                handwritingFile.delete();
            }

            SQL = "update " + MemoDatabase.TABLE_MEMO +
                    " set " +
                    " ID_HANDWRITING = '" + handwritingId + "' " +
                    " where _id = '" + mMemoId + "'";
            Log.d(TAG, "Handwrite_SQL: " + SQL);                     //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~지우기~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            if (MultiMemoActivity.mDatabase != null) {
                MultiMemoActivity.mDatabase.rawQuery(SQL);
            }

            mMediaHandwritingId = String.valueOf(handwritingId);
        }



        //수정 sql문 적기 전에 시간 변경하는 코드 넣어야함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*************************************미완****************
        //mDateStr = ~~~~~~~~~~~
        // update memo info (사진 외 다른 정보 수정)
        SQL = "update " + MemoDatabase.TABLE_MEMO +
                " set " +
                " INPUT_DATE = DATETIME('" + mDateStr + "'), " +
                " TITLE_TEXT = '" + mTitleStr + "'," +
                " CONTENT_TEXT = '" + mMemoStr + "'" +
                " where _id = '" + mMemoId + "'";

        Log.d(TAG, "SQL : " + SQL);
        if (MultiMemoActivity.mDatabase != null) {
            MultiMemoActivity.mDatabase.execSQL(SQL);
        }

        intent.putExtra(BasicInfo.KEY_MEMO_TITLE, mTitleStr);
        intent.putExtra(BasicInfo.KEY_MEMO_TEXT, mMemoStr);
        intent.putExtra(BasicInfo.KEY_ID_PHOTO, mMediaPhotoId);
        intent.putExtra(BasicInfo.KEY_ID_VOICE, mMediaVoiceId);
        intent.putExtra(BasicInfo.KEY_ID_HANDWRITING, mMediaHandwritingId);
        intent.putExtra(BasicInfo.KEY_URI_PHOTO, mMediaPhotoUri);
        intent.putExtra(BasicInfo.KEY_URI_VOICE, mMediaVoiceUri);
        intent.putExtra(BasicInfo.KEY_URI_HANDWRITING, mMediaHandwritingUri);

        setResult(RESULT_OK, intent);
        finish();
    }


    /**
     * Photo테이블에 사진 정보 넣는 함수!
     *
     * @return 사진 이름값 (uri)
     */

    private String insertPhoto() {
        String photoName = null;

        if (isPhotoCaptured) { //사진이 캡쳐 되는 경우
            try {
                if (mMemoMode != null && mMemoMode.equals(BasicInfo.MODE_MODIFY)) { //메모모드가 수정
                    Log.d(TAG, "previous photo is newly created for modify mode.");

                    String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +    //DB에서 이전 사진정보 삭제
                            " where _ID = '" + mMediaPhotoId + "'";
                    Log.d(TAG, "SQL : " + SQL);
                    if (MultiMemoActivity.mDatabase != null) {
                        MultiMemoActivity.mDatabase.execSQL(SQL);
                    }

                    File previousFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
                    if (previousFile.exists()) {
                        previousFile.delete();  //이전파일 삭제
                    }
                }


                File photoFolder = new File(BasicInfo.FOLDER_PHOTO);

                //사진폴더가 없을경우
                if (!photoFolder.isDirectory()) {
                    Log.d(TAG, "creating photo folder : " + photoFolder);
                    photoFolder.mkdirs(); //폴더 생성
                }

                // Temporary Hash for photo file name
                photoName = createFilename();     //사진파일 이름을 생성(사진 uri)

                FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_PHOTO + photoName);
                resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();


                if (photoName != null) {
                    Log.d(TAG, "isCaptured            : " + isPhotoCaptured);

                    // Photo테이블에 uri값 입력!
                    String SQL = "insert into " + MemoDatabase.TABLE_PHOTO + "(URI) values(" + "'" + photoName + "')";
                    if (MultiMemoActivity.mDatabase != null) {
                        MultiMemoActivity.mDatabase.execSQL(SQL);
                    }
                }

            } catch (IOException ex) {
                Log.d(TAG, "Exception in copying photo : " + ex.toString());
            }


        }
        return photoName;
    }

    private String insertHandwriting() {
        String handwritingName = null;
        Log.d(TAG, "isHandwritingMade            : " +isHandwritingMade);
        if (isHandwritingMade) { // captured Bitmap
            try {

                if (mMemoMode != null && (mMemoMode.equals(BasicInfo.MODE_MODIFY)  || mMemoMode.equals(BasicInfo.MODE_VIEW))) {
                    Log.d(TAG, "previous handwriting is newly created for modify mode.");

                    String SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
                            " where _ID = '" + mMediaHandwritingId + "'";
                    Log.d(TAG, "SQL : " + SQL);
                    if (MultiMemoActivity.mDatabase != null) {
                        MultiMemoActivity.mDatabase.execSQL(SQL);
                    }

                    File previousFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
                    if (previousFile.exists()) {
                        previousFile.delete();
                    }
                }


                File handwritingFolder = new File(BasicInfo.FOLDER_HANDWRITING);

                //폴더가 없다면 폴더를 생성한다.
                if(!handwritingFolder.isDirectory()){
                    Log.d(TAG, "creating handwriting folder : " + handwritingFolder);
                    handwritingFolder.mkdirs();
                }

                // Temporal Hash for handwriting file name

                handwritingName = createFilename();

                FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_HANDWRITING + handwritingName);
                // MIKE 20101215
                resultHandwritingBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                // MIKE END
                outstream.close();


                if (handwritingName != null) {
                    Log.d(TAG, "isCaptured            : " +isHandwritingMade);

                    // INSERT HANDWRITING INFO
                    String SQL = "insert into " + MemoDatabase.TABLE_HANDWRITING + "(URI) values(" + "'" + handwritingName + "')";
                    if (MultiMemoActivity.mDatabase != null) {
                        MultiMemoActivity.mDatabase.execSQL(SQL);
                    }
                }

            } catch (IOException ex) {
                Log.d(TAG, "Exception in copying handwriting : " + ex.toString());
            }


        }
        return handwritingName;
    }

    //파일이름 생성하는 함수! (날짜로 생성함)
    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }


    //날짜 셋팅하는 함수
    private void setCalendar() {
        date_space = (TextView) findViewById(R.id.input_date);

        Date curDate = new Date();
        mCalendar.setTime(curDate);

        int year = mCalendar.get(Calendar.YEAR);
        int monthOfYear = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

        String mDateStr = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일";

        date_space.setText(mDateStr);
    }


    /**
     * 값 파싱해서 변수값 지정
     */
    private boolean parseValues() {
        String insertDateStr = date_space.getText().toString();
        try {
            Date insertDate = BasicInfo.dateDayNameFormat.parse(insertDateStr); //년 월 일 -> date타입
            mDateStr = BasicInfo.dateDayFormat.format(insertDate);  //date타입 -> 0000-00-00
        } catch (ParseException ex) {
            Log.e(TAG, "Exception in parsing date : " + insertDateStr);
        }
        String titletxt = mTitleEdit.getText().toString();    //제목 edit에서 읽어서
        mTitleStr = titletxt; //mTitleStr에 저장

        String memotxt = mMemoEdit.getText().toString();    //메모 edit에서 읽어서
        mMemoStr = memotxt; //mMemoStr에 저장

        if (mMemoStr.trim().length() < 1) {
            showDialog(BasicInfo.CONFIRM_TEXT_INPUT);
            return false;
        }
        return true;
    }

    //다이얼로그 생성할때
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = null;

        switch (id) {
            case BasicInfo.CONFIRM_TEXT_INPUT:      //3002
                builder = new AlertDialog.Builder(this);
                builder.setTitle("선택하세요");
                builder.setMessage("텍스트를 입력하시겠습니까?");
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case BasicInfo.CONTENT_PHOTO:           //2001
                builder = new AlertDialog.Builder(this);

                mSelectdContentArray = R.array.array_photo;
                builder.setTitle("선택하세요");
                builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mChoicedArrayItem = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mChoicedArrayItem == 0) {
                            showPhotoCaptureActivity();
                        } else if (mChoicedArrayItem == 1) {
                            showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Log.d(TAG, "whichButton3        ======        " + whichButton);
                    }
                });

                break;

            case BasicInfo.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(this);

                mSelectdContentArray = R.array.array_photo_ex;
                builder.setTitle("선택하세요.");
                builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mChoicedArrayItem = whichButton;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mChoicedArrayItem == 0) {
                            showPhotoCaptureActivity();
                        } else if (mChoicedArrayItem == 1) {
                            showPhotoSelectionActivity();
                        } else if (mChoicedArrayItem == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            mPhoto.setImageResource(R.drawable.add_photo);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case BasicInfo.CONFIRM_DELETE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.memo_title);
                builder.setMessage(R.string.memo_delete_question);
                builder.setPositiveButton(R.string.yes_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteMemo();
                    }
                });
                builder.setNegativeButton(R.string.no_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dismissDialog(BasicInfo.CONFIRM_DELETE);
                    }
                });

                break;

            case BasicInfo.CONTENT_VOICE:       //음성파일 녹음하는 다이얼로그
                builder = new AlertDialog.Builder(this);

                mSelectdContentArray = R.array.array_voice;
                builder.setTitle(R.string.selection_title);
                builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "whichButton1 : " + whichButton);
                        mChoicedArrayItem = whichButton;
                    }
                });
                builder.setPositiveButton(R.string.selection_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "whichButton2        ======        " + whichButton);
                        if(mChoicedArrayItem == 0 ){
                            showVoiceRecordingActivity();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            case BasicInfo.CONTENT_VOICE_EX:        //음성파일 실행하는 다이얼로그
                builder = new AlertDialog.Builder(this);

                mSelectdContentArray = R.array.array_voice_ex;
                builder.setTitle(R.string.selection_title);
                builder.setSingleChoiceItems(mSelectdContentArray, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "whichButton1 : " + whichButton);
                        mChoicedArrayItem = whichButton;
                    }
                });
                builder.setPositiveButton(R.string.selection_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(TAG, "Selected Index : " + mChoicedArrayItem);

                        if(mChoicedArrayItem == 0 ) {
                            showVoicePlayingActivity();
                        } else if(mChoicedArrayItem == 1) {
                            showVoiceRecordingActivity();
                        } else if(mChoicedArrayItem == 2) {
                            isVoiceCanceled = true;
                            isVoiceRecorded = false;

                            VoiceBtn.setImageResource(R.drawable.icon_voice_empty);
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                break;

            default:
                break;
        }
        return builder.create();
    }

    /**
     * 메모 삭제
     */
    private void deleteMemo() {

        // delete photo record
        Log.d(TAG, "deleting previous photo record and file : " + mMediaPhotoId);
        String SQL = "delete from " + MemoDatabase.TABLE_PHOTO +
                " where _ID = '" + mMediaPhotoId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if (MultiMemoActivity.mDatabase != null) {
            MultiMemoActivity.mDatabase.execSQL(SQL);
        }

        File photoFile = new File(BasicInfo.FOLDER_PHOTO + mMediaPhotoUri);
        if (photoFile.exists()) {
            photoFile.delete();
        }


        // delete handwriting record
        Log.d(TAG, "deleting previous handwriting record and file : " + mMediaHandwritingId);
        SQL = "delete from " + MemoDatabase.TABLE_HANDWRITING +
                " where _ID = '" + mMediaHandwritingId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if (MultiMemoActivity.mDatabase != null) {
            MultiMemoActivity.mDatabase.execSQL(SQL);
        }

        File handwritingFile = new File(BasicInfo.FOLDER_HANDWRITING + mMediaHandwritingUri);
        if (handwritingFile.exists()) {
            handwritingFile.delete();
        }


        // delete memo record
        Log.d(TAG, "deleting previous memo record : " + mMemoId);
        SQL = "delete from " + MemoDatabase.TABLE_MEMO +
                " where _id = '" + mMemoId + "'";
        Log.d(TAG, "SQL : " + SQL);
        if (MultiMemoActivity.mDatabase != null) {
            MultiMemoActivity.mDatabase.execSQL(SQL);
        }
        setResult(RESULT_OK);

        finish();
    }

    //사진촬영 액티비티
    public void showPhotoCaptureActivity() {
        Intent intent = new Intent(getApplicationContext(), PhotoCaptureActivity.class);
        startActivityForResult(intent, BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY);
    }

    //사진 불러오기 액티비티
    public void showPhotoSelectionActivity() {
        /*Intent intent = new Intent(getApplicationContext(), PhotoSelectionActivity.class);
        startActivityForResult(intent, BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY);*/
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY);
    }

    /**
     * 액티비티 결과 처리
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case BasicInfo.REQ_PHOTO_CAPTURE_ACTIVITY:  // 사진촬용 액티비티로 부터 응답처리
                Log.d(TAG, "onActivityResult() for REQ_PHOTO_CAPTURE_ACTIVITY.");

                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "resultCode : " + resultCode);

                    boolean isPhotoExists = checkCapturedPhotoFile();
                    if (isPhotoExists) {
                        Log.d(TAG, "image file exists : " + BasicInfo.FOLDER_PHOTO + "captured");

                        //사진촬용으로 저장된 파일을 디코딩하여 비트맵 이미지로 생성
                        resultPhotoBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_PHOTO + "captured");

                        tempPhotoUri = "captured";

                        mPhoto.setImageBitmap(resultPhotoBitmap);
                        isPhotoCaptured = true;

                        mPhoto.invalidate();
                    } else {
                        Log.d(TAG, "image file doesn't exists : " + BasicInfo.FOLDER_PHOTO + "captured");
                    }
                }

                break;

            case BasicInfo.REQ_PHOTO_SELECTION_ACTIVITY:  // 앨범선택 액티비티로 부터 응답처리
                Log.d(TAG, "onActivityResult() for REQ_PHOTO_LOADING_ACTIVITY.");

                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "resultCode : " + resultCode);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;   //사진 크기 조절

                        InputStream in = getContentResolver().openInputStream(intent.getData());

                        resultPhotoBitmap = BitmapFactory.decodeStream(in, null, options);
                        in.close();

                        //이미지 표시
                        mPhoto.setImageBitmap(resultPhotoBitmap);
                        isPhotoCaptured = true;

                        mPhoto.invalidate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                break;
            case BasicInfo.REQ_HANDWRITING_MAKING_ACTIVITY:  // 손글씨를 저장하는 경우
                Log.d(TAG, "onActivityResult() for REQ_HANDWRITING_MAKING_ACTIVITY.");

                if (resultCode == RESULT_OK) {
                    boolean isHandwritingExists = checkMadeHandwritingFile();
                    if(isHandwritingExists) {
                        resultHandwritingBitmap = BitmapFactory.decodeFile(BasicInfo.FOLDER_HANDWRITING + "made");
                        tempHandwritingUri = "made";

                        isHandwritingMade = true;

                        insert_handwriting.setImageBitmap(resultHandwritingBitmap);
                    }
                }

                break;

            case BasicInfo.REQ_VOICE_RECORDING_ACTIVITY:  // 녹음하는 경우
                Log.d(TAG, "onActivityResult() for REQ_VOICE_RECORDING_ACTIVITY.");

                if (resultCode == RESULT_OK) {
                    boolean isVoiceExists = checkRecordedVoiceFile();
                    if(isVoiceExists) {
                        tempVoiceUri = "recorded";

                        isVoiceRecorded = true;

                        VoiceBtn.setImageResource(R.drawable.icon_voice);
                    }
                }

                break;

        }
    }


    /**
     *  저장된 사진 파일 확인
     */
    private boolean checkCapturedPhotoFile() {
        File file = new File(BasicInfo.FOLDER_PHOTO + "captured");
        if (file.exists()) {
            return true;
        }

        return false;
    }

    /**
     * 저장된 손글씨 파일 확인
     */
    private boolean checkMadeHandwritingFile() {
        File file = new File(BasicInfo.FOLDER_HANDWRITING + "made");
        if(file.exists()) {
            return true;
        }

        return false;
    }

    /**
     * 저장된 녹음 파일 확인
     */
    private boolean checkRecordedVoiceFile() {
        File file = new File(BasicInfo.FOLDER_VOICE + "recorded");
        if(file.exists()) {
            return true;
        }

        return false;
    }


}
