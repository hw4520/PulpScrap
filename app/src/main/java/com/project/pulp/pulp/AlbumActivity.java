package com.project.pulp.pulp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AlbumActivity extends AppCompatActivity {

    // swipeAdapter 메소드 변수 선언
    private SwipeAdapter swipeAdapter;
    // swipe 페이지 변수 선언
    private ViewPager viewPager;

    // 변수값 받아오기
    ImageView albumView;
    EditText albumMemo;

    // SQLite 변수 생성
    SQLiteDatabase sqliteDatabase;
    myDBHelper dbHelper;

    // DB 컬럼 개수
    String count;
    int countNum, swipeNum;


    //버튼 3가지
    ImageView btnplus, btnminus, btnlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // 아이디 값 변수 안에 넣어주기
        albumView = (ImageView) findViewById(R.id.albumView);
        albumMemo = (EditText)findViewById(R.id.albumMemo);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffff33")); //제목의 칼라
        toolbar.setSubtitle("PULP"); //부제목 넣기
        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        // DB객체 생성
        dbHelper = new myDBHelper(this);

        // DB 개수 구하기
        sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqliteDatabase.rawQuery("select count(*) from scrap",null);

        while (cursor.moveToNext()){
            count = cursor.getString(0);
        }
        countNum = Integer.parseInt(count);
        swipeNum = Integer.parseInt(count);
        if (swipeNum==0) {swipeNum=1;}
        else {swipeNum=countNum;}

        // swipeAdapter 객체 생성
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager());

        // viewPager 변수 안에 activity_memo.xml에 있는 viewPager타입의 container를 넣어준다.
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(swipeAdapter);


        /*
        // 저장하기 버튼 효과
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.insertAlbum(albumMemo);
                Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
*/



        //버튼에 설정

        btnplus=(ImageView) findViewById(R.id.btnplus);
        btnminus=(ImageView) findViewById(R.id.btnminus);
        btnlist=(ImageView) findViewById(R.id.btnlist);

        btnplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), PlusActivity.class);
                startActivity(intent);
            }
        });


    } // end of onCreate 메소드



    // 생성한 툴바에 메뉴 넣어주기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return true;
    }



    // 메뉴 기능 넣어주기
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Toast.makeText(this, "환경설정을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_quit:
                Toast.makeText(this, "나가기를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                break;
        }

        /* if 문으로 구현
        if (id == R.id.action_settings) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    } // end of Menu Option 설정



    // Fragment에 보이는 view
    public static class FragmentPage extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        AlbumActivity albumActivity = new AlbumActivity();
        Cursor cursor;
        String albuMemo; // editText String 값 넣을 변수

        public FragmentPage() {
        }

        public static FragmentPage newInstance(int sectionNumber) {
            FragmentPage fragment = new FragmentPage();
            Bundle bundle = new Bundle();
            bundle.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(bundle);
            return fragment;
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view;
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER); // position 값 // 1번째 페이지는 0
            int countNum = albumActivity.countNum; // countNum의 값 변수 선언

            if (countNum != 0){
                cursor = albumActivity.dbHelper.getAlbum(sectionNumber);

                while (cursor.moveToNext()){
                    albuMemo = cursor.getString(0);
                }

                view = inflater.inflate(R.layout.fragment_album, container, false);
                EditText albumMemo = (EditText)view.findViewById(R.id.albumMemo);
                albumMemo.setText(albuMemo);
                return view;
            } else {
                view = inflater.inflate(R.layout.fragment_album, container, false);
                return view;

            }

            //TextView textView = (TextView) view.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        }
    } // end of Fragment 설정



    // 스와이프 페이지 개수 / 스와이프 section_number 지정
    public class SwipeAdapter extends FragmentPagerAdapter {

        public SwipeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentPage.newInstance(position);
        }

        @Override
        public int getCount() { // countNum의 값이 0일 때는 기본 페이지를 불러오기 위해 swipeNum에 1 값을 부여함
            return swipeNum;
        }

    } // end of Swipe Page 설정



    // SQLite 메소드 설정
    public class myDBHelper extends SQLiteOpenHelper {

        public myDBHelper(Context context){
            super(context,"pulp",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap (num INTEGER PRIMARY KEY AUTOINCREMENT, subject int, photo char(500), memo char(1000));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists scrap");
            onCreate(sqLiteDatabase);
        }


        // DB insert 메소드
        public void insertAlbum(EditText albumMemo){
            sqliteDatabase = getWritableDatabase();
            sqliteDatabase.execSQL("insert into scrap values ('','1','','"+albumMemo.getText().toString()+"');");
        }


        // 처음에 DB 뿌려주는 메소드
        public Cursor getAlbum(int sectionNumber) {

            // 읽기가 가능하게 DB 열기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            cursor = sqliteDatabase.rawQuery("select * from scrap where num=((select max(num) from scrap)-'+sectionNumber+')", null);

            return cursor;

        }

        // DB 개수 구하는 메소드
        public int getCount(){

            // DB 개수 구하기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqliteDatabase.rawQuery("select count(*) from scrap",null);

            while (cursor.moveToNext()){
                count = cursor.getString(0);
            }
            int viewNum = Integer.parseInt(count);
            return viewNum;

        }


    } // end of 디비 설정



} // ★ CLASS ★
