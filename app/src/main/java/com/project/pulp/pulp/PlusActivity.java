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

    /*
    전체 코드 참고 - http://superwony.tistory.com/5
    사진 클릭시 onClick(){ 대화상자 => 사진촬영, 앨범선택, 취소 }
    사진촬영 클릭=>selectPhoto(){
        createImageFile() : 사진 촬영용 폴더생성, 파일 생성.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_CODE) :인텐트를 통해 사진촬영앱에 연결, 들고온 경로와 파일에 사진저장.
    }
    =>onActivityResult(){
        리퀘스트 코드가 CAMERA_CODE일때,
        getPictureForPhoto() : 사진촬영앱을 통해 저장된 사진. 경로를 통해 불러오고 이미지뷰에 저장
    }
     */


    //현재 오류
    //crop 미적용
    //현재 흐름은 사진 직접 저장. 수정버젼은 사진은 temp로 저장하고 crop된 사진을 저장. 그 경로를 db에 저장할것.


    //device File Explorer로 파일 올려두면 에뮬레이터에서 적용이 안됨. 파일에서 보이는데도 적용이 안됨.
    //exifOrientationToDegrees 사진촬영 하면 시계 반대방향으로 90도 돌아감.(에뮬레이터 설정자체가 문제인듯.)

    //sqlite에서 불러온 글자는 제대로 뜬다. blob 그림이 제대로 안뜬다.
    //sqlite에 글자저장이 깨져서 저장됨. 불러올때는 바로 불러와짐.



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
//    ImageView ivtestt;
//    Button btnimage;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus);


        myHelper=new myDBHelper(this);
        sqlDB=myHelper.getWritableDatabase();


        ivImage=(ImageView)findViewById(R.id.user_image);
        tvnote=(EditText)findViewById(R.id.tvex1);
        btnscrap=(Button)findViewById(R.id.btnscrap);

        btnscrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //원래 폴더숫자 디비에서 챙겨와야함.

                String sql="";
                if (currentPhotoPath==null){
                    sql="insert into scrap(subject, photo, memo) values ('1','"+imagePath+"','"+tvnote.getText()+"')";

                }else {
                    sql="insert into scrap(subject, photo, memo) values ('1','"+currentPhotoPath+"','"+tvnote.getText()+"')";
                }
                sqlDB.execSQL(sql);

                sqlDB.close();

                finish();

            }
        });


        //db에서 select되는지 확인한 코드.
/*        ivtestt=(ImageView)findViewById(R.id.ivtext);
        ivtestt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor;
                cursor=sqlDB.rawQuery("select * from scrap ;",null);


                if(cursor.moveToNext()){*/
                    /*
                    byte[] photo=cursor.getBlob(1);
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(photo);
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = false;
//                    Bitmap theImage= BitmapFactory.decodeStream(imageStream, null, options);
//                    ivtestt.setImageBitmap(theImage);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(imageStream, null, options);
                        try {
                        imageStream.close();
                    } catch (Exception e) {
                    }
                    options.inJustDecodeBounds = false;
                    Bitmap theImage= BitmapFactory.decodeStream(imageStream, null, options);
                    ivtestt.setImageBitmap(theImage);


                    //글자는 제대로 뜬다. 그림이 제대로 안뜬다.
                    Log.i("ahn",cursor.getString(3));
            tvnote.setText(cursor.getString(3));
*/
 /*
                File imgFile = new  File(cursor.getString(2));

                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ivtestt.setImageBitmap(myBitmap);

                }*/

/*
                    Bitmap myBitmap = BitmapFactory.decodeFile(cursor.getString(2));

                    ivtestt.setImageBitmap(myBitmap);
                }
                cursor.close();
                //이미지 저장
                //http://www.masterqna.com/android/82777/sqlite-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%A0%80%EC%9E%A5-%EC%A7%88%EB%AC%B8%EB%93%9C%EB%A6%BD%EB%8B%88%EB%8B%A4

            }
        });
                sqlDB.close();
*/




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
        //Dialog 하위 클래스 AlertDialog 사용
        //AlertDialog는 최대 3개의 버튼을 가질수 있다.
        //이름을 지정하여 객체를 생성했을때- AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //https://developer.android.com/guide/topics/ui/dialogs?hl=ko

    }

    /*
    카메라로 찍은 사진을 가져오는 경우 인텐트를 통해 카메라를 호출합니다.
    'CAMERA_CODE'는 requestCode 선택한 사진에 대한 요청 값을 구분하는 용도입니다.

    private void selectPhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CODE);
    }

    7.0 이전 버전은 상위의 코드처럼 간단하게 수행이 가능했지만 보안상의 이슈로
    'FileProvider.getUriForFile()'을 통해 사진의 uri를 가져오는데
    이를 수행하기 위해선 'AndroidMenifest.xml'에 명시해야합니다.
    추가 정보- http://gogorchg.tistory.com/entry/Android-FileProvider-androidosFileUriExposedException
     */

    //인텐트와 프로바이더 설명
    //intent인텐트는 주로 새로운 액티비티를 생성, 새로운 서비스 생성, 브로드캐스트 전달에 사용된다.
    //암시적 액티비티
    //명시적 액티비티의 개념이 두 액티비티를 사용자가 직접 생성하고 코딩하는 것이라면, 암시적 인텐트는
    //약속된 액션을 지정하여 안드로이드에서 제공하는 기존 응용프로그램을 실행하는 것이다.
    //흐름 -  메인액티비티의 Intent intent= new Intent(실행하고자 하는 액션, Uri.parse());
    //or Intent intent= new Intent(실행하고자 하는 액션);
    //intent.putExtra("Extra이름", "내용")
    //intent.setData(Uri.parse())
    //startActivity(intent)

    //명시적 액티비티
    //흐름 - 메인액티비티의 인텐트 putExtra() 소통할 데이터를 담는다
    // =>startActivityForResult(){ 데이터를 돌려받기 위해 사용되는 액티비티호출메소드 }
    // =>세컨트액티비티의 인텐트 getExtra() 데이터를 받는 메소드
    // =>세컨드 액티비티의 인텐트 putExtra() 메인 액티비티로 반환할 데이터를 담는다.
    // => setResult(코드, 인텐트명) 메인액티비티로 데이터를 돌려준다.
    // => onActivityResult(int requestCode, int resultCode, Intent data){
    //    getExtra() 데이터를 돌려받는다.
    // }
    //출처 - 안드로이드 프로그래밍(책) 412p
    //https://developer.android.com/guide/components/intents-filters?hl=ko

    //android.provider.MediaStore 프로바이더의 MediaStore 클래스의 ACTION_IMAGE_CAPTURE 상수
    //안드로이드의 4대 컴포넌트 액티비티, 서비스, 브로드캐스트 리시버, 콘텐트 프로바이더
    //안드로이드 응용 프로그램은 데이터를 자기 내부에서만 공유할수 있기때문에 프로그램의 콘텐트 프로바이더끼리
    //소통하며 소통의 창구는 Uri이다. 콘텐트 프로바이더에서 처리된 데이터는 일반적으로 데이터베이스 또는 파일로 저장된다.
    //출처 - 안드로이드 프로그래밍(책) 391p



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
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
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
        File dir = new File(Environment.getExternalStorageDirectory() + "/pulpscrap/");
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

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/pulpscrap/"
                + mImageCaptureName);
        currentPhotoPath = storageDir.getAbsolutePath();
        //getAbsolutePath() 절대경로 String형 반환
        //getAbsoluteFile() 절대경로 File형 반환
        //Environment.getExternalStorageDirectory()[file 반환]
        //Environment.getExternalStorageDirectory().getAbsolutePath()[String 반환]
        //Environment.getExternalStorageDirectory().getAbsoluteFile()[file 반환]
        //세 가지 모두 같은경로를 반환한다.

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
        /*
        blob타입으로 저장. but,실패함.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photo = baos.toByteArray();
*/

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
        /** * Database가 존재하지 않을 때, 딱 한번 실행된다.
         * DB를 만드는 역할을 한다.
         출처: http://cocomo.tistory.com/409 [Cocomo Coding]
         */

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
    }




}


