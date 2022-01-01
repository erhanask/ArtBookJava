package com.erhanask.artbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erhanask.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

//Oluşturduğumuz artholderı burada çağırıyoruz.
public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList) {
        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Oluşturulduğunda bu method çağırılıyor ve inflate yani bağlama işlemi yapıyoruz.
        //binding kullanarak rowumuzu alıyoruz ve layoutInflater ile bağlıyoruz.
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        //ArtHolder constructoruına aldığımız rowu gönderiyoruz
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        int artIx = position;
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),artActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",artArrayList.get(artIx).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }


    //Art Holder sınıfını yazarken RecyclerView içerisinde tutacağımız her bir verinin
    //listede oluşturduğumuz küçük viewınnı alıyoruz ve aşağıdaki gibi ArtHolder Constructoruna yerleştiriyoruz.
    public class ArtHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
