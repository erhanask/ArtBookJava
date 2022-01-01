package com.erhanask.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.erhanask.artbook.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class artActivity extends AppCompatActivity {
    //bağlayıcı oluşturduk
    ActivityArtBinding binding;
    //Bu sınıf actviteden dönen sonucu başlatarak işlem yapmamıza yarıyor. Ama önce oncreate içinde "register" etmemiz gerekiyor.
    //burada intentle çalışıyoruz intent dönecek.
    ActivityResultLauncher<Intent> activityResultLauncher;
    //burada permission string döndürüyor String dönecek.
    ActivityResultLauncher<String> permissionLauncher;

    Bitmap selectedImageBitmapped;

    SQLiteDatabase db;

    ArrayList<Art> artArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //bağlayıcıyı bu aktiviteni oluşturula bağlayıcı classına eşitledik
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        //bağladığımız classın en üst seviyesinde görünüm oluşturduk
        View view = binding.getRoot();
        //aktivitenin en üstüne oluşturduğumuz görünümü attık ki binding. dediğimizde bütün verillere erişebilelim.
        setContentView(view);
        registerLauncher();

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")){
            //Wants to create new art
            binding.yearText.setText("");
            binding.artistText.setText("");
            binding.artText.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.selectimage);
        } else{
            //Wants to view art
            int artId = intent.getIntExtra("artId",0);
            binding.saveButton.setVisibility(View.INVISIBLE);

            try {
                System.out.println("Comes");
                System.out.println("Comes"+artId);

                db = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);


                Cursor cursor = db.rawQuery("SELECT * FROM Arts WHERE id = ?",new String[] {String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("artName");
                int artistNameIx = cursor.getColumnIndex("artistName");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    System.out.println("Comes"+artId);

                    System.out.println(cursor.getString(artNameIx));
                    binding.artText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(artistNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

                cursor.close();


            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }



    public void saveArt(View view){

        String artName = binding.artText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();
        //resmi küçülterek aldık
        Bitmap smallImage = makeSmallerImage(selectedImageBitmapped,300);

        //küçülttüğümüz resmi 1001011101 gibi byte arrayine böyle çeviriyoruz.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            //Veritabanı oluşturduk
            db = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

            db.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, year VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO arts (artName,artistName,year,image) VALUES(?, ?, ?, ?)";
            //Tıpkı PHP PDO gibi "?"ne karşılık gelen indexe bind ile bağlayıp execute ettik.
            SQLiteStatement sqLiteStatement = db.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //ART EKLEDİKTEN SONRA MAİN ACTİVİTYE GİDİYORUZ AMA ONDAN ÖNCE BU AKTİVİTEYİ KAPATARAK TEMİZLİYORUZ.
        Intent intent = new Intent(artActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }

    //resim boyutunu veritabanına kayıt yapacağımız için küçültmemiz lazım.
    public Bitmap makeSmallerImage (Bitmap image,int maxSize) {

        int width = image.getWidth();
        int height = image.getHeight();
        //küçültme oranı
        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {

            // Fotoğraf yatay ise , yani width heighten fazlaysa

            width = maxSize;
            height = (int) (width / bitmapRatio);

        } else {

            // Fotoğraf dikey ise , yani height widthten fazlaysa

            height = maxSize;
            width = (int) (height / bitmapRatio);

        }
        //resmin sclae edilmiş halini döndürüyoruz.
        return image.createScaledBitmap(image,width,height,true);

    }

    public void selectImage(View view){

        //BURADA GALERİYE GİTMEME İZİN VERDİ Mİ KONTROL EDİYORUM, EĞER VERMEDİYSE İLK BLOK VERDİYSE İKİNCİ BLOK.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            //EĞER İZİN İSTEMEK ZORUNDAYSAK O İZNİ ALMAK ZORUNDAYSAK.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //İZNİ TEKRAR SORMAK İÇİN SNACKBAR OLUŞTURUYORUZ VE setAction()'a bastığında tekrar soruyoruz...
                Snackbar.make(view,"Permission needed to access galery.",Snackbar.LENGTH_INDEFINITE).setAction("Give permission.", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //IZIN ISTEME İŞLEMİ.
                        //yazdığımız permission launchera string olarak permissionumuzu gönderdik
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            } else {
                //IZIN ISTEME İŞLEMİ.
                //yazdığımız permission launchera string olarak permissionumuzu gönderdik

                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

        } else {

            //Galeriye gidişimizi intente atıyoruz ardından değişkeni activityLauncher içerisinde göndererek kullanıyoruz.
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }



    }

    //Launcherları kaydetmek için bu fonksiyonu yazdık
    public void registerLauncher() {

        //galeriden resim seçince çalışacak olan activity launcher kaydını yapıyoruz. Burada gelen resim uri 'yı için işlemlerimiz olduğundan start activity methodunu kullanıyoruz.
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                //kullanıcı resim seçtiyse işlem yapıyoruz.
                //gelen sonucun kodu ok ise ve değilse işlemlerimiz.
                if (result.getResultCode() == RESULT_OK) {
                    //sonuç bize getData() dediğimizde bir intent olarak dönüyor.
                    Intent intentFromResult = result.getData();

                    if (intentFromResult != null) {
                        //gelen intent boş değilse içerisinden getdata() diyerek resimin URI'ını alıyoruz.
                        Uri selectedImage = intentFromResult.getData();

                        //Sadece ekrada göstermek istesek işimizi görürdü.
                        //binding.imageView.setImageResource(selectedImage);
                        //Ama biz 'bitmap'e çevirerek veritabanına da kaydedeceğiz.
                        //Bu yüzden try catch bloğu içinde işlemlerimizi yapacağız.
                        try {
                            //Android versiyonu 28 üzerinde ImageDecoder Kullanılıyor.
                            if (Build.VERSION.SDK_INT >= 28) {

                                //Gelen veriyi bitmape çevirmek için classı bu şekilde kullanıyoruz ve ekrana resmi koyuyoruz.
                                ImageDecoder.Source source = ImageDecoder.createSource(artActivity.this.getContentResolver(),selectedImage);
                                selectedImageBitmapped = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImageBitmapped);


                            } else {
                                selectedImageBitmapped = MediaStore.Images.Media.getBitmap(artActivity.this.getContentResolver(),selectedImage);
                                binding.imageView.setImageBitmap(selectedImageBitmapped);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        });



        //değişkeni kayıt işleminde request permission methodunu seçiyoruz çünkü izin istediğimizi belirterek kaydediyoruz.
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            //izin verildi mi kontrol etmek için callback fonksiyonunu yazıyoruz.
            @Override
            public void onActivityResult(Boolean result) {

                if (result){
                    //izin verilmişse galeriye gidiyoruz. ve gelen sonuçla activityresultlaunchera intenti gödnerip çalıştırıyor.
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else{
                    //izin verilmemiş
                    Toast.makeText(artActivity.this,"We need your permission.",Toast.LENGTH_LONG).show();
                }


            }
        });

    }

}