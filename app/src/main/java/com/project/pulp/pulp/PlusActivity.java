package com.project.pulp.pulp;

import android.Manifest;
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
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlusActivity extends AppCompatActivity {

    private Uri photoUri;
    private Uri cameraUri;
    private Uri galleryUri;
    private String currentPhotoPath;//실제 사진 파일 경로
    String imagePath=null;//갤러리에서 들고오는 사진경로
    String mImageCaptureName;//이미지 이름
    private final int GALLERY_CODE = 1112;
    private final int CAMERA_CODE = 1111;//'CAMERA_CODE'는 requestCode 선택한 사진에 대한 요청 값을 구분하는 용도
    ImageView ivImage;

    EditText tvnote;
    Button btnscrap;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    int folderNum;

    // MaxNum 구하는 변수
    int maxNum;
    String stMaxNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus);

        Intent intent = getIntent();
        folderNum = intent.getIntExtra("folderNum", 1);
        Log.v("folderNum", folderNum+"");

        myHelper=new myDBHelper(this);
        sqlDB=myHelper.getWritableDatabase();

        ivImage=(ImageView)findViewById(R.id.user_image);
        tvnote=(EditText)findViewById(R.id.tvex1);
        btnscrap=(Button)findViewById(R.id.btnscrap);

        btnscrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //원래 폴더숫자 디비에서 챙겨와야함.
                maxNum = myHelper.getMaxNum();

                String sql="";
                if (currentPhotoPath==null){
                    sql="insert into scrap(subject, photo, memo, num) values ("+folderNum+",'"+imagePath+"','"+tvnote.getText()+"', "+maxNum+")";

                }else {
                    sql="insert into scrap(subject, photo, memo, num) values ("+folderNum+", '"+currentPhotoPath+"','"+tvnote.getText()+"', "+maxNum+")";
                }
               // sqlDB.execSQL("delete from scrap");
                sqlDB.execSQL(sql);

                sqlDB.close();

                Intent i = new Intent(PlusActivity.this, AlbumActivity.class);
                i.putExtra("folderNum", folderNum);
                startActivityForResult(i, 1);
                finish();

            }
        });
    }

    public void addPic(View view) {

        //btnimage에 설정된 클릭시 발생하는 함수 이름.(재정의된 함수 이름이 아닌, xml파일에서 button에 적용한 함수이름이다.)
        DialogInterface.OnClickListener cameraListener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectPhoto();
            }
        };
        DialogInterface.OnClickListener albumListener= new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectGallery();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
        //https://developer.android.com/guide/topics/ui/dialogs?hl=ko
    }

    /*
    private void selectPhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CODE);
    }

    7.0 이전 버전은 상위의 코드처럼 간단하게 수행이 가능했지만 보안상의 이슈로
    'FileProvider.getUriForFile()'을 통해 사진의 uri를 가져오는데
    이를 수행하기 위해선 'AndroidMenifest.xml'에 명시해야합니다.
    추가 정보- http://gogorchg.tistory.com/entry/Android-FileProvider-androidosFileUriExposedException
     */

    /*  카메라로 찍은 사진을 가져오는 경우
        인텐트를 통해 카메라를 호출합니다.
        'CAMERA_CODE'는 requestCode 선택한 사진에 대한 요청 값을 구분하는 용도입니다.*/
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
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
            ivImage.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기

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

        ivImage.setImageBitmap(rotate(bitmap, exifDegree));//이미지 뷰에 비트맵 넣기*/

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

    //계속해서 권한 확인이 가능하도록
    @Override
    public void onResume(){
        checkPermissionF();
        super.onResume();
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
                //요청한 권한( WRITE_EXTERNAL_STORAGE )이 없을 때..거부일때...
                /* 사용자가 WRITE_EXTERNAL_STORAGE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                 * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                 */
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)||shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)||shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(/*getApplicationContext()*/ PlusActivity.this);
                    //AlertDialog import 버전변경
                    //http://dreamaz.tistory.com/63
                    //https://stackoverflow.com/questions/27087983/unable-to-add-window-token-null-is-not-valid-is-your-activity-running

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


    /**
     * 사용자가 권한을 허용했는지 거부했는지 체크
     * @param requestCode   1번
     * @param permissions   개발자가 요청한 권한들
     * @param grantResults  권한에 대한 응답들
     *                    permissions와 grantResults는 인덱스 별로 매칭된다.
     */
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



    public class myDBHelper extends SQLiteOpenHelper{
        private Context context;

        public myDBHelper(Context context){
            super(context, "pulp", null,1);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS scrap(subject int, picturepath char(500), note char(1000));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
/*            db.execSQL("DROP TABLE IF exists scrap;");
            onCreate(db);

            Toast.makeText(context, "버전이 올라갔습니다.", Toast.LENGTH_SHORT).show();*/

        }

        public int getMaxNum(){
            sqlDB = getReadableDatabase();
            Cursor cursor;
            cursor = sqlDB.rawQuery("select max(num) from scrap where subject="+folderNum+"", null);

            int MaxNum=1;
            while (cursor.moveToNext()){
                MaxNum=cursor.getInt(0);
                MaxNum=MaxNum+1;

            }
            return MaxNum;

            //stMaxNum가 없으면 null이 떠서 에러..
/*
            int MaxNum=Integer.parseInt(stMaxNum);

            if (MaxNum==0){
                MaxNum=1;
                return MaxNum;
            } else {
                MaxNum=+1;
                return MaxNum;
            }*/
        }


    } // end of DB 메소드




} // end of CLASS


