package com.project.pulp.pulp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btn;
    LinearLayout layout;
    myDBHelper myDBHelper;
    SQLiteDatabase sqLiteDatabase;
    String count; // 총 폴더의 갯수
    int countNum; // 총 폴더의 갯수
    int layoutSize = 2; //한 줄당 폴더 갯수
    int layoutCount; //레이아웃 갯수

    int floderNum;
    String folderName;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,1,0,"갤러리로 정렬");
        menu.add(0,2,0,"텍스트로 정렬");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1 :
                //갤러리모드로 정렬
                layout.removeAllViewsInLayout();
                GallaryMode gallaryMode = new GallaryMode();
                gallaryMode.run();
                return true;
            case 2 :
                //텍스트모드로 정렬
                layout.removeAllViewsInLayout();
                TextView title = new TextView(MainActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10,10,10,30);
                title.setText("폴더 리스트");
                title.setTextSize(50);
                title.setLayoutParams(params);
                layout.addView(title);
                TextMode textMode = new TextMode();
                textMode.run();
                return true;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDBHelper = new myDBHelper(this);
        //레이아웃
        layout = (LinearLayout)findViewById(R.id.layout);
        //입력창
        final AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog add = ad.create();
        add.setTitle("새로 만들기"); //제목 설정
        add.setMessage("폴더명을 적어주세요");//내용 설정

        final EditText et = new EditText(MainActivity.this);//edittext삽입하기
        add.setView(et);

        add.setButton(DialogInterface.BUTTON_NEGATIVE, "닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();//닫기
            }
        });

        add.setButton(DialogInterface.BUTTON_POSITIVE, "설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Text 값 받아서 텍스트뷰에 넣기
                String value = et.getText().toString();
                dialogInterface.dismiss();//닫기

                //디비에 입력
                sqLiteDatabase = myDBHelper.getWritableDatabase();
                sqLiteDatabase.execSQL("insert into folder (subject) values"+"('"+value+"')");
                sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                        "(subject INTEGER, num INTEGER, photo char(500), memo char(100));");

            }
        });
        //입력창

        //New버튼
        btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.show();
            }
        });

        //초기화면 갤러리모드로 정렬
        GallaryMode gallaryMode = new GallaryMode();
        gallaryMode.run();

    }//oncreate

    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(Context context){
            super(context,"pulp",null,1);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table IF NOT EXISTS folder(num INTEGER PRIMARY KEY AUTOINCREMENT,subject char(10));");
            sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER, num INTEGER, photo char(500), memo char(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists folder");
            onCreate(sqLiteDatabase);
        }

        public int folderNum(String subject){
            sqLiteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select num from folder where subject='\"+subject.getText().toString()+\"'", null);

            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }
            int viewNum = Integer.parseInt(count);
            return viewNum;
        }

    }//end class

    public class GallaryMode {
        public void run(){
            //폴더 뿌려주기
            sqLiteDatabase = myDBHelper.getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select count(*) as count from folder",null);
            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }

            countNum = Integer.parseInt(count);
            //Log.v("coun", ""+countNum);
            if(countNum!=0) {
                layoutCount = countNum/layoutSize;
                layoutCount = (countNum % layoutSize) != 0 ? layoutCount + 1 : layoutCount;
                Log.v("lay", ""+layoutCount);
                for (int i = 0; i < layoutCount; i++) {
                    LinearLayout linearLayout = new LinearLayout(MainActivity.this); //폴더를 담는 리니어레이아웃
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                            ,LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(10,10,10,10);
                    linearLayout.setLayoutParams(layoutParams);
                    int currentLayout = i + 1;
                    int startRow = (currentLayout-1)*layoutSize+1-1;//startRow-1해서 대입
                    cursor = sqLiteDatabase.rawQuery("select subject from folder order by num desc LIMIT "+startRow+","+layoutSize,null);
                    while (cursor.moveToNext()){
                        //폴더명 띄우기
                        folderName = cursor.getString(0);
                        TextView txt = new TextView(MainActivity.this);
                        txt.setText(folderName);
                        txt.setTextSize(30);
                        txt.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);
                        Log.v("val2", folderName);

                        //폴더 이미지 넣기
                        ImageView imageView = new ImageView(MainActivity.this);
                        Drawable image = getResources().getDrawable(R.drawable.pic2);
                        image.setAlpha(100);
                        imageView.setImageDrawable(image);
                        imageView.setBackgroundResource(R.drawable.image_border);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                        //폴더 띄우기
                        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
                        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(510,510);
                        RelativeLayout.LayoutParams param2 = new RelativeLayout.LayoutParams(510,510);
                        RelativeLayout.LayoutParams param3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        param.setMargins(10,10,10,10);
                        param3.addRule(RelativeLayout.CENTER_IN_PARENT);
                        param2.setMargins(0,0,0,0);
                        relativeLayout.setLayoutParams(param);
                        imageView.setLayoutParams(param2);
                        txt.setLayoutParams(param3);
                        relativeLayout.setGravity(Gravity.CENTER);

                        relativeLayout.addView(imageView);
                        relativeLayout.addView(txt);
                        linearLayout.addView(relativeLayout);

                        floderNum = myDBHelper.folderNum(folderName);

                        relativeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                                intent.putExtra("folderNum", floderNum);
                                startActivity(intent);

                            }
                        });

                    }//while

                    layout.addView(linearLayout);

                }//for
            }//end if
            cursor.close();
            sqLiteDatabase.close();
        }
    }//end class

    public class TextMode{
        public void run(){
            sqLiteDatabase = myDBHelper.getReadableDatabase();
            Cursor cursor;
            cursor = sqLiteDatabase.rawQuery("select subject from folder order by num desc",null);
            while (cursor.moveToNext()){
                String folderName = cursor.getString(0);
                TextView txt = new TextView(MainActivity.this);
                txt.setText("  " + folderName);
                txt.setTextSize(30);
                txt.setTypeface(Typeface.SANS_SERIF,Typeface.NORMAL);
                txt.setPadding(30,30,30,30);
                txt.setBackgroundResource(R.drawable.textview_border);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(10,10,10,50);
                txt.setLayoutParams(params);
                layout.addView(txt);
            }
            cursor.close();
            sqLiteDatabase.close();
        }
    }//end class
}
