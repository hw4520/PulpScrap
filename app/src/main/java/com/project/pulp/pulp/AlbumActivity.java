package com.project.pulp.pulp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlbumActivity extends AppCompatActivity {

    private Uri photoUri;
    private Uri cameraUri;
    private Uri galleryUri;
    private String currentPhotoPath;//실제 사진 파일 경로
    String imagePath=null;//갤러리에서 들고오는 사진경로
    String mImageCaptureName;//이미지 이름
    private final int GALLERY_CODE = 1112;
    private final int CAMERA_CODE = 1111;//'CAMERA_CODE'는 requestCode 선택한 사진에 대한 요청 값을 구분하는 용도


    // activity_swipe.xml 뷰 페이지 변수 선언
    ViewPager pager;
    CustomAdapter adapter;

    // 변수값 받아오기
    TextView albumMemo;

    // SQLite 변수 생성
    SQLiteDatabase sqliteDatabase;
    DBHelper dbHelper;
    Cursor cursor;

    // DB 컬럼 개수
    String count;
    int countNum, swipeNum;

    // Intent 받아온 num 값
    static int folderNum;

    // DB maxNum 구하는 메소드
    String stMaxNum;
    int MaxNum;

    //버튼 3가지
    ImageView btnplus, btnminus, btnlist;

    // 정렬 boolean 변수 선언
    boolean orderCheck = true;


    ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);


        // activity_swipe.xml 뷰 페이지 변수 값 받기
        pager = (ViewPager) findViewById(R.id.pager);

        Intent intent = getIntent();
        folderNum = intent.getIntExtra("folderNum", 1);
       Log.v("folderNum", folderNum+"");

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffff33")); //제목의 칼라
        toolbar.setSubtitle("PULP"); //부제목 넣기
        toolbar.setNavigationIcon(R.mipmap.ic_launcher); //제목앞에 아이콘 넣기
        setSupportActionBar(toolbar); //툴바를 액션바와 같게 만들어 준다.

        // DB객체 생성
        dbHelper = new DBHelper(this);

        // DB 개수 구하기
        sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqliteDatabase.rawQuery("select count(*) from scrap where subject="+folderNum+"",null);

        while (cursor.moveToNext()){
            countNum = cursor.getInt(0);
            Log.v("countNum", countNum+"");
        }
        if(countNum==0){
            Intent insertMemo = new Intent(getApplicationContext(), PlusActivity.class);
            insertMemo.putExtra("folderNum", folderNum);
            startActivity(insertMemo);
            finish();
        }

        //ViewPager에 설정할 Adapter 객체 생성
        //ListView에서 사용하는 Adapter와 같은 역할.
        //다만. ViewPager로 스크롤 될 수 있도록 되어 있다는 것이 다름
        //PagerAdapter를 상속받은 CustomAdapter 객체 생성
        //CustomAdapter에게 LayoutInflater 객체 전달
        adapter = new CustomAdapter(getLayoutInflater());
        //ViewPager에 Adapter 설정
        pager.setAdapter(adapter);
        //albumMemo = (TextView)pager.findViewById(R.id.albumMemo);

        btnplus=(ImageView) findViewById(R.id.btnplus);
        btnminus=(ImageView) findViewById(R.id.btnminus);
        btnlist=(ImageView) findViewById(R.id.btnlist);

        btnplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGo= new Intent(getApplicationContext(), PlusActivity.class);
                intentGo.putExtra("folderNum", folderNum);
                startActivity(intentGo);
                finish();
            }
        });

        btnminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqliteDatabase = dbHelper.getWritableDatabase();
                int position =  pager.getCurrentItem();
                String sql;
                String sql2;
                Log.v("tagg",(Integer)pager.getCurrentItem()+"");

                if (orderCheck){
                    int ckmaxNum = dbHelper.getMaxNum()-position;
                    sql = "delete from scrap where num=(select max(num) from scrap where subject="+folderNum+")-"+position+" and subject="+folderNum;
                    sql2 = "update scrap set num=num-1 where num>(select max(num) from scrap where subject="+folderNum+")-"+position+" and subject="+folderNum;
                } else {
                    int ckmaxNum = position+1;
                    sql = "delete from scrap where num="+ckmaxNum+" and subject="+folderNum;
                    sql2 = "update scrap set num=num-1 where num>"+ckmaxNum+" and subject="+folderNum;
                }
                sqliteDatabase.execSQL(sql);
                sqliteDatabase.execSQL(sql2);


                Toast.makeText(AlbumActivity.this.getApplicationContext(),"삭제 완료되었습니다!",Toast.LENGTH_SHORT).show();

                onResume();


            }
        });




    } // end of onCreate 메소드


    @Override
    protected void onResume() {
        super.onResume();

        sqliteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = sqliteDatabase.rawQuery("select count(*) from scrap where subject="+folderNum+"",null);

        while (cursor.moveToNext()){
            countNum = cursor.getInt(0);
            Log.v("countNum", countNum+"");
        }
        if(countNum==0){
            Intent insertMemo = new Intent(getApplicationContext(), PlusActivity.class);
            insertMemo.putExtra("folderNum", folderNum);
            startActivity(insertMemo);
            finish();
        }

        CustomAdapter adapter = new CustomAdapter(getLayoutInflater());
        //ViewPager에 Adapter 설정
        pager.setAdapter(adapter);

        checkPermissionF();

    }


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
        CustomAdapter adapter = new CustomAdapter(getLayoutInflater());

        switch (id){
            case R.id.action_settings:
                orderCheck = true;
                pager.setAdapter(adapter);
                break;
            case R.id.action_quit:
                orderCheck = false;
                pager.setAdapter(adapter);
                break;
        }

        /* if 문으로 구현
        if (id == R.id.action_settings) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    } // end of Menu Option 설정


    public class CustomAdapter extends PagerAdapter {
        LayoutInflater inflater;

        public CustomAdapter(LayoutInflater inflater) {
            //전달 받은 LayoutInflater를 멤버변수로 전달
            this.inflater=inflater;
        }

        //PagerAdapter가 가지고 잇는 View의 개수를 리턴
        //보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
        @Override
        public int getCount() {
            return countNum; //이미지 개수 리턴(그림이 10개라서 10을 리턴)
        }
        //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
        //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
        //첫번째 파라미터 : ViewPager
        //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view=null;
            String albuMemo=null;
            String sql;

            imagePath = null;

            //새로운 View 객체를 Layoutinflater를 이용해서 생성
            //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
            view= inflater.inflate(R.layout.fragment_album, null);

            TextView albumMemo = (TextView)view.findViewById(R.id.albumMemo);
            img= (ImageView)view.findViewById(R.id.img_viewpager_childimage);

            if (orderCheck){
               sql = "select memo, photo from scrap where num=((select max(num) from scrap where subject="+folderNum+")-"+position+") and subject="+folderNum;
            } else {
                sql = "select memo, photo from scrap where num=("+position+"+1) and subject="+folderNum;
            }
            cursor = sqliteDatabase.rawQuery(sql, null);
            while (cursor.moveToNext()){
                albuMemo = cursor.getString(0);
                imagePath=cursor.getString(1);
                Log.v("albuMemo", albuMemo+"");

            }

            albumMemo.setText(albuMemo);

/*
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
*/

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
            //img.setImageBitmap(rotate(bitmap, exifDegree));
            img.setImageBitmap(bitmap);


            img.setOnLongClickListener(new View.OnLongClickListener(){
                 public boolean onLongClick(View v) {

                     AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
                     builder.setTitle("업로드할 이미지 선택");


                     builder.setPositiveButton("사진촬영", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             selectPhoto();

                         }
                     });

                     builder.setNeutralButton("앨범선택", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             selectGallery();
                         }
                     });

                     builder.setNegativeButton("취소",
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss(); //닫기
                                 }
                             });


                     AlertDialog alert = builder.create();
                     alert.show();

                     return true;
                 }
            });


            albumMemo.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
                    builder.setTitle("스크랩 메모");

                    final EditText input = new EditText(AlbumActivity.this);

                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setSingleLine(false);
                    input.setLines(5);
                    input.setMaxLines(6);
                    input.setGravity(Gravity.LEFT | Gravity.TOP);
                    input.setHorizontalFadingEdgeEnabled(false);
                    builder.setView(input);

                    builder.setPositiveButton("수정하기", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //String sql="";
                            int position =  pager.getCurrentItem();
                            // Text 값 받아서 로그 남기기
                            String updateMemo = input.getText().toString();  // Text 값 받기
                            String SupdateMemo = updateMemo.replace("'","\"");

                            if (orderCheck){
                                sqliteDatabase.execSQL("UPDATE scrap SET memo='"+SupdateMemo+"' WHERE num=(select max(num) from scrap where subject=" +
                                        folderNum+")-"+position+" and subject="+folderNum+";");
                                //sql = "update scrap set memo="+updateMemo+" where num=(select max(num) from scrap where subject="+folderNum+")-"+position+" and subject="+folderNum;
                            } else {
                                sqliteDatabase.execSQL("UPDATE scrap SET memo='"+SupdateMemo+"' where num=("
                                +position+"+1) and subject="+folderNum+";");

                                //sql = "update scrap set memo="+updateMemo+" where num=("+position+"+1) and subject="+folderNum;
                            }

                            Toast.makeText(AlbumActivity.this.getApplicationContext(),"스크랩 내용을 수정하였습니다!",Toast.LENGTH_SHORT).show();

                            dialog.dismiss();     //닫기

                            pager.setAdapter(adapter);
                            pager.setCurrentItem(position); // this is suppose to be your pagePosition



                        }
                    });

                    builder.setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss(); //닫기
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();

                    return true;
                }


            });

            container.addView(view);

            return view;
        }


        //화면에 보이지 않은 View는파쾨를 해서 메모리를 관리함.
            //첫번째 파라미터 : ViewPager
            //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
            //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //ViewPager에서 보이지 않는 View는 제거
                //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
                container.removeView((View)object);
            }

            //instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
            @Override
            public boolean isViewFromObject(View v, Object obj) {
                return v==obj;
        }

    }



    // SQLite 메소드 설정
    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context){
            super(context,"pulp",null,1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            /* sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER, num INTEGER PRIMARY KEY AUTOINCREMENT, photo BLOB, memo char(100));"); */
            sqLiteDatabase.execSQL("create table IF NOT EXISTS scrap " +
                    "(subject INTEGER, num INTEGER, photo char(500), memo char(100));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("drop table if exists scrap");
            onCreate(sqLiteDatabase);
        }


        public int getMaxNum(){
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqliteDatabase.rawQuery("select max(num) from scrap where subject="+folderNum+"", null);

            int MaxNum=1;
            while (cursor.moveToNext()){
                MaxNum=cursor.getInt(0);
                MaxNum=MaxNum+1;

            }
            return MaxNum;
        }

        // DB insert 메소드
        public void insertAlbum(String albuMemo, int maxNum){
            sqliteDatabase = getWritableDatabase();
            sqliteDatabase.execSQL("insert into scrap (subject, num, memo) values ("+folderNum+", "+maxNum+", '"+albuMemo+"');");
        }


        // 처음에 DB 뿌려주는 메소드
        public Cursor getAlbum(int sectionNumber) {

            // 읽기가 가능하게 DB 열기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            cursor = sqliteDatabase.rawQuery("select memo from scrap where num=((select max(num) from scrap)-"+sectionNumber+") and subject="+folderNum+"", null);

            return cursor;

        }

        // DB 개수 구하는 메소드
        public int getCount(){

            // DB 개수 구하기
            sqliteDatabase = getReadableDatabase();
            Cursor cursor;
            cursor = sqliteDatabase.rawQuery("select count(*) from scrap where subject="+folderNum+"",null);

            while (cursor.moveToNext()){
                int count = cursor.getInt(0);
            }
            int viewNum = Integer.parseInt(count);
            return viewNum;

        }

    } // end of 디비 설정




    // 앨범 업데이트 구문
    public void albumUpdate(){
        int position = pager.getCurrentItem();

        if (currentPhotoPath == null) {
            if (orderCheck) {
                sqliteDatabase.execSQL("UPDATE scrap SET photo='" + imagePath + "' WHERE num=(select max(num) from scrap where subject=" +
                        folderNum + ")-" + position + " and subject=" + folderNum + ";");
                //sql = "update scrap set memo="+updateMemo+" where num=(select max(num) from scrap where subject="+folderNum+")-"+position+" and subject="+folderNum;
            } else {
                sqliteDatabase.execSQL("UPDATE scrap SET photo='" + imagePath + "' where num=("
                        + position + "+1) and subject=" + folderNum + ";");

                //sql = "update scrap set memo="+updateMemo+" where num=("+position+"+1) and subject="+folderNum;
            }
        } else {
            if (orderCheck) {
                sqliteDatabase.execSQL("UPDATE scrap SET photo='" + currentPhotoPath + "' WHERE num=(select max(num) from scrap where subject=" +
                        folderNum + ")-" + position + " and subject=" + folderNum + ";");
                //sql = "update scrap set memo="+updateMemo+" where num=(select max(num) from scrap where subject="+folderNum+")-"+position+" and subject="+folderNum;
            } else {
                sqliteDatabase.execSQL("UPDATE scrap SET photo='" + currentPhotoPath + "' where num=("
                        + position + "+1) and subject=" + folderNum + ";");

                //sql = "update scrap set memo="+updateMemo+" where num=("+position+"+1) and subject="+folderNum;
            }
        }

    }



    // PlusActivity 객체 생성 여부 미정이라서 거기 있는 갤러리 관련 메소드를 전부 가져왔음
    private void selectPhoto() {
        String state = Environment.getExternalStorageState();
        //외부저장소 sd카드가 이용가능한 상태인지 확인한다
        //추가 설명 - http://apphappy.tistory.com/71, http://mainia.tistory.com/662

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //read,write가능상태일때 MEDIA_MOUNTED 상수
            //read 가능 상태일때 MEDIA_MOUNTED_READ_ONLY 상수

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intent.resolveActivity(getPackageManager()) != null) {
                //인텐트를 받을 수 있는 컴포넌트가 존재하는지 확인. 암시적인텐트에서 사용
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    //사진폴더를 만들고 현재시각으로 이름지어진 파일을 생성하는 사용자 작성함수 createImageFile().
                } catch (IOException ex) {
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, "com.project.pulp.pulp.fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CAMERA_CODE);
                }
            }
        }else{
            Toast.makeText(this, "SD카드가 사용불가능합니다.", Toast.LENGTH_SHORT);
        }
    }

    //사진 촬영 선택시, 사진이 저장될 폴더와 사진파일 생성.
    private File createImageFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Pictures/pulpscrap/");
        //디렉토리 생성 /storage/emulated/0 (/sdcard)+ /pulpscrap/
        //파일생성을 위해서는 new File로 객체 생성후 createNewFile()함수를 실행시켜야 파일로 생성이 된다.
        //설명 - https://qkrrudtjr954.github.io/java/2017/11/13/create-file-and-file-method.html

        if (!dir.exists()) {//File 클래스 함수 exists() - 파일이 존재하는가?
            dir.mkdirs();//폴더가 존재하지 않으면 폴더 생성
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //java.text.SimpleDateFormat은 date타입 객체를 원하는 형태로 출력하게 해주는 클래스
        //format()함수를 통해서 date객체를 StringBuffer로, parse함수를 통해서 String객체를 Date로 변환
        //설명 - http://bvc12.tistory.com/168, https://developer.android.com/reference/java/text/SimpleDateFormat
        mImageCaptureName = timeStamp + ".png";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Pictures/pulpscrap/"
                + mImageCaptureName);
        currentPhotoPath = storageDir.getAbsolutePath();
        return storageDir;
    }

    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_CODE:
                    galleryUri=data.getData();
                    sendPicture(data.getData()); //갤러리에서 가져오기
                    break;
                case CAMERA_CODE:
                    cameraUri=data.getData();
                    getPictureForPhoto(data.getData()); //카메라에서 가져오기
                    break;
                default:
                    break;
            }
        }
    }

    private void sendPicture(Uri imgUri) {

        if (imgUri==null){
            //imagePath=하나 샘플??;
            Toast.makeText(this, "사진을 들고올 수 없습니다.",Toast.LENGTH_SHORT).show();

        }else{
            imagePath = getRealPathFromURI(imgUri); // path 경로

            albumUpdate();


            Toast.makeText(AlbumActivity.this.getApplicationContext(),
                    "스크랩 내용을 수정하였습니다!",Toast.LENGTH_SHORT).show();

            int position = pager.getCurrentItem();
            pager.setAdapter(adapter);
            pager.setCurrentItem(position); // this is suppose to be your pagePosition


        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }


    private void getPictureForPhoto(Uri imgUri) {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }

        img.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기*/

    }


    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    final int M =23;

    private void checkPermissionF() {

        if (Build.VERSION.SDK_INT >= M) {
            // only for LOLLIPOP and newer versions
            System.out.println("Hello Marshmallow (마시멜로우)");
            int permissionResult = getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionResult2 = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionResult3 = getApplicationContext().checkSelfPermission(Manifest.permission.CAMERA);

            if (permissionResult == PackageManager.PERMISSION_DENIED||permissionResult2 == PackageManager.PERMISSION_DENIED||permissionResult3 == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)||shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)||shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(/*getApplicationContext()*/ AlbumActivity.this);

                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("단말기의 파일쓰기 권한이 필요합니다.\\n계속하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= M) {
                                        System.out.println("감사합니다. 권한을 허락했네요 (마시멜로우)");
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,/*Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,*/Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 1);
                                    }
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(),"권한 요청 취소", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();

                    //최초로 권한을 요청할 때.
                } else {
                    System.out.println("최초로 권한을 요청할 때. (마시멜로우)");
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 1);
                    //        getThumbInfo();
                }
            }else{
                //권한이 있을 때.
                //       getThumbInfo();
            }


        } else {
            System.out.println("(마시멜로우 이하 버전입니다.)");
            //   getThumbInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, /*@NonNull*/ String[] permissions, /*@NonNull*/ int[] grantResults) {

        if (requestCode == 1) {
            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
                내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
/*            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);*/

            for(int i = 0 ; i < permissions.length ; i++) {
                if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult WRITE_EXTERNAL_STORAGE ( 권한 성공 ) ");
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult ACCESS_FINE_LOCATION ( 권한 성공 ) ");
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult ACCESS_COARSE_LOCATION ( 권한 성공 ) ");
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult READ_PHONE_STATE ( 권한 성공 ) ");
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult READ_EXTERNAL_STORAGE ( 권한 성공 ) ");
                    }
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("onRequestPermissionsResult CAMERA ( 권한 성공 ) ");
                    }
                }
            }

        } else {
            System.out.println("onRequestPermissionsResult ( 권한 거부) ");
            Toast.makeText(getApplicationContext(), "요청 권한 거부", Toast.LENGTH_SHORT).show();
        }

    }



} // ★ CLASS ★
