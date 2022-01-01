package com.erhanask.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.erhanask.artbook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

        artArrayList = new ArrayList<>();

        //Layout yöneticiye linear olarak dur diytoruz.
        binding.artRecycler.setLayoutManager(new LinearLayoutManager(this));
        //adaptörümüze arraylistimizi gönderiyoruz
        artAdapter = new ArtAdapter(artArrayList);
        //artRcyclerımızın adaptörünü set ediyopru.
        binding.artRecycler.setAdapter(artAdapter);

        getData();


    }

    //SQL İLE VERİLERİ ÇEKTİĞİMİZ FONKSİYON
    private void getData() {

        try {
            //BAĞLANDIK
            SQLiteDatabase db = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

            //CURSOR İLE VERİTABANINI GEZİP NAME VE ID İNDEXE KOLONLARI EŞİTEDİK
            Cursor cursor = db.rawQuery("SELECT * FROM Arts",null);
            int nameIx = cursor.getColumnIndex("artName");
            int idIx = cursor.getColumnIndex("id");

            //CURSOR TABLOYU GEZERKEN ART SINIFINDAN OLUŞTURDUĞUMUZ OBJELERI ARRAYLİSTE ATTIK
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art = new Art(name,id);
                artArrayList.add(art);
            }

            artAdapter.notifyDataSetChanged();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Menüyü çalıştırıldığında bağladık
        MenuInflater menuInflater = getMenuInflater();
        //Burada R.menu diyerek menüyü aldık direkt.
        menuInflater.inflate(R.menu.art_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //R.id diyerek addArt menu itemini çektik seçilen o mu baktık vve işlemimizi yaptık.
        if (item.getItemId() == R.id.addArt) {
            Intent intent= new Intent(this,artActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}