package io.kps.appcam

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.kps.appcam.models.Img

class CustomAdapter(val photos: ArrayList<Img>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(photos[position])
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(img: Img) {
            val image = itemView.findViewById(R.id.image) as ImageView
            var bitmap : Bitmap = BitmapFactory.decodeFile(img.image)
            image.setImageBitmap(bitmap)
            image.setOnClickListener{
                val intent = Intent( itemView.context , ImageDetails::class.java)
                intent.putExtra("image",  img)
                itemView.context.startActivity(intent)
            }

        }
    }
}