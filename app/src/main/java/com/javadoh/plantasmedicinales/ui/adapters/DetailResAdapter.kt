package com.javadoh.plantasmedicinales.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.activities.DetailResActivity
import java.util.ArrayList

class DetailResAdapter(
    private val mContext: Context,
    private val hierbaBeanList: ArrayList<HierbasBean>?,
    private var positionHierba: Int
) : RecyclerView.Adapter<DetailResAdapter.DetailViewHolder>() {

    companion object {
        val TAG: String = DetailResAdapter::class.java.name
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): DetailViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.detail_res_herb_fragment, viewGroup, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, i: Int) {
        val itemHierba = hierbaBeanList?.get(positionHierba) ?: return

        holder.textViewNombre.text = itemHierba.nombre
        holder.textViewAlias.text = itemHierba.alias?.toString()
        holder.textViewDescripcion.text = itemHierba.descripcion
        holder.textViewPropiedades.text = itemHierba.propiedades?.toString()
        holder.textViewIndicaciones.text = itemHierba.indicaciones
        holder.textViewEmpleo.text = itemHierba.empleo
        holder.textViewSintomas.text = itemHierba.sintomas?.toString()
        holder.textViewContraindicaciones.text = itemHierba.contraIndicaciones
        holder.textViewUbicacion.text = itemHierba.ubicacion
        holder.textViewGastronomia.text = itemHierba.gastronomia

        val uri = "@drawable/hierba_card_background"
        val imageResource = mContext.resources.getIdentifier(uri, null, mContext.packageName)
        val res = ContextCompat.getDrawable(mContext, imageResource)
        holder.imageView.setImageDrawable(res)

        this.positionHierba = i

        holder.botonRegreso.setOnClickListener {
            if (mContext is DetailResActivity) {
                mContext.finish()
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: $positionHierba")
        return positionHierba
    }

    inner class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.img_hierba_card_res_ppal)
        val textViewNombre: TextView = view.findViewById(R.id.txt_hierba_name_detail)
        val textViewAlias: TextView = view.findViewById(R.id.txt_hierba_alias_detail)
        val textViewDescripcion: TextView = view.findViewById(R.id.txt_hierba_description_detail)
        val textViewPropiedades: TextView = view.findViewById(R.id.txt_hierba_properties_detail)
        val textViewIndicaciones: TextView = view.findViewById(R.id.txt_hierba_indications_detail)
        val textViewEmpleo: TextView = view.findViewById(R.id.txt_hierba_empleo_detail)
        val textViewSintomas: TextView = view.findViewById(R.id.txt_hierba_symptoms_detail)
        val textViewContraindicaciones: TextView = view.findViewById(R.id.txt_hierba_contraindications_detail)
        val textViewUbicacion: TextView = view.findViewById(R.id.txt_hierba_ubication_detail)
        val textViewGastronomia: TextView = view.findViewById(R.id.txt_hierba_gastronomy_detail)
        val botonAbrirComentario: Button = view.findViewById(R.id.btn_open_comment_detail)
        val botonRegreso: Button = view.findViewById(R.id.btn_regresar_detail)
    }
}