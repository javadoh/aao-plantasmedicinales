package com.javadoh.plantasmedicinales.ui.adapters

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.activities.DetailResActivity
import com.javadoh.plantasmedicinales.utils.bean.MemoryBeanAux
import com.squareup.picasso.Picasso

class BusquedaRespuestaAdapter(
    private val mContext: Context,
    private val hierbasBeanList: ArrayList<HierbasBean>?,
    private val dataUser: Array<String>?
) : RecyclerView.Adapter<BusquedaRespuestaAdapter.CustomViewHolder>() {

    companion object {
        val TAG: String = BusquedaRespuestaAdapter::class.java.name
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CustomViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.respuesta_card_main, viewGroup, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, i: Int) {
        val itemHierba = hierbasBeanList?.get(i) ?: return

        holder.textView.text = itemHierba.nombre
        holder.textViewAlias.text = itemHierba.alias?.toString()?.replace(Regex("[^A-Za-zÑñáéíóúÁÉÍÓÚ, ]"), "")

        var imagePath = ""
        itemHierba.imgurl?.let {
            imagePath = it.replace(Regex("\\.(png|jpg|jpeg)$"), "")
        }

        MemoryBeanAux.hierbaImagePath = imagePath;
        var uri = "@drawable/$imagePath"
        var imageResource = mContext.resources.getIdentifier(uri, "drawable", mContext.packageName)

        if (imageResource != 0) {
            Picasso.get().load(imageResource).resize(180, 100).into(holder.imageView)
        } else {
            uri = "@drawable/hierba_card_background"
            imageResource = mContext.resources.getIdentifier(uri, null, mContext.packageName)
            Picasso.get().load(imageResource).resize(180, 100).into(holder.imageView)
        }
    }

    override fun getItemCount(): Int = hierbasBeanList?.size ?: 0

    inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val imageView: ImageView = view.findViewById(R.id.thumbnail)
        val textView: TextView = view.findViewById(R.id.tituloHierba)
        val textViewAlias: TextView = view.findViewById(R.id.aliasHierba)

        init {
            textView.setOnClickListener(this)
            textViewAlias.setOnClickListener(this)
            imageView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            try {
                if (v.id == textView.id || v.id == textViewAlias.id) {
                    val intent = Intent(mContext, DetailResActivity::class.java).apply {
                        putExtra("HIERBAS_BEAN", hierbasBeanList!![position])
                        putExtra("DATA_USER", dataUser)
                    }
                    mContext.startActivity(intent)
                } else if (v.id == imageView.id) {
                    val vistaDialogo = LayoutInflater.from(mContext).inflate(R.layout.dialog_image_big, null)

                    val textTitulo = vistaDialogo.findViewById<TextView>(R.id.textDialogTitulo)
                    val imageDialog = vistaDialogo.findViewById<ImageView>(R.id.imageBigDialog)

                    val nombreArchivoImg = hierbasBeanList!![position].imgurl?.replace(Regex("\\.\\w+$"), "") ?: ""
                    textTitulo.text = "${mContext.getString(R.string.detailPlant)} ${hierbasBeanList[position].nombre?.uppercase()}"

                    val uri = "@drawable/$nombreArchivoImg"
                    val imageResource = mContext.resources.getIdentifier(uri, "drawable", mContext.packageName)

                    if (imageResource != 0) {
                        if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Picasso.get().load(imageResource).resize(900, 600).centerCrop().into(imageDialog)
                        } else {
                            Picasso.get().load(imageResource).resize(700, 700).centerCrop().into(imageDialog)
                        }
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.errorImagesAdapter), Toast.LENGTH_LONG).show()
                    }

                    AlertDialog.Builder(mContext)
                        .setView(vistaDialogo)
                        .setCancelable(false)
                        .setNegativeButton(mContext.getString(R.string.btn_cancelar)) { dialog, _ -> dialog.cancel() }
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling DetailResActivity: ", e)
            }
        }
    }
}