package com.javadoh.plantasmedicinales.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.Profile
import com.facebook.ProfileTracker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.Constants
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.activities.DetailResActivity
import com.javadoh.plantasmedicinales.ui.activities.FacebookLoginComment
import com.javadoh.plantasmedicinales.utils.PostAsyncHttpTask
import com.javadoh.plantasmedicinales.utils.TextAnimationColor
import com.javadoh.plantasmedicinales.utils.WindowInsetsHelper
import com.javadoh.plantasmedicinales.utils.bean.MemoryBeanAux
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DetailResFragment : Fragment() {

    private lateinit var textViewNombre: TextView
    private lateinit var textViewNombreCientifico: TextView
    private lateinit var textViewAlias: TextView
    private lateinit var textViewDescripcion: TextView
    private lateinit var textViewSeccionUso: TextView
    private lateinit var textViewPropiedades: TextView
    private lateinit var textViewIndicaciones: TextView
    private lateinit var textViewContraIndicaciones: TextView
    private lateinit var textViewSintomas: TextView
    private lateinit var textViewEmpleo: TextView
    private lateinit var textViewUbicacion: TextView
    private lateinit var textViewGastronomia: TextView
    private lateinit var botonRegreso: FloatingActionButton
    private lateinit var botonAbrirComentario: FloatingActionButton

    private lateinit var layoutUserName: TextInputLayout
    private lateinit var layoutEmailName: TextInputLayout
    private lateinit var layoutComentario: TextInputLayout
    private lateinit var editTextUserName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextComentario: EditText

    private lateinit var imageViewUrl: ImageView
    private lateinit var btnTomarDatosFacecbook: Button
    private lateinit var scrollFragment: ScrollView
    private lateinit var toolbar: Toolbar

    private var accessToken: AccessToken? = null
    private var profileUser: Profile? = null
    private var profileUserFb: Profile? = null
    private lateinit var accessTokenTracker: AccessTokenTracker
    private lateinit var profileTracker: ProfileTracker

    private var hierba: HierbasBean? = null
    private var url: String = ""

    companion object {
        val TAG: String = DetailResFragment::class.java.name

        fun newInstance(arguments: Bundle?): DetailResFragment {
            val fragment = DetailResFragment()
            fragment.arguments = arguments
            return fragment
        }

        fun getPixelValue(context: Context, dimenId: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        accessToken = AccessToken.getCurrentAccessToken()

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldToken: AccessToken?, newToken: AccessToken?) {
                accessToken = newToken
            }
        }

        accessToken?.let { Log.d(TAG, "AccessToken: ${it.token}") }

        profileTracker = object : ProfileTracker() {
            override fun onCurrentProfileChanged(oldProfile: Profile?, newProfile: Profile?) {
                profileUserFb = newProfile
            }
        }

        Profile.fetchProfileForCurrentAccessToken()
        profileUser = Profile.getCurrentProfile()

        accessTokenTracker.startTracking()
        profileTracker.startTracking()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.detail_res_herb_fragment, container, false)
        val mContext = requireActivity()

        toolbar = rootView.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        WindowInsetsHelper.applyStatusBarPadding(toolbar)

        scrollFragment = rootView.findViewById(R.id.scrollFragmentLayout)
        imageViewUrl = rootView.findViewById(R.id.img_hierba_card_res_ppal)
        textViewNombre = rootView.findViewById(R.id.txt_hierba_name_detail)
        textViewNombreCientifico = rootView.findViewById(R.id.txt_hierba_name_cientif_detail)
        textViewAlias = rootView.findViewById(R.id.txt_hierba_alias_detail)
        textViewDescripcion = rootView.findViewById(R.id.txt_hierba_description_detail)
        textViewSeccionUso = rootView.findViewById(R.id.txt_hierba_section_use_detail)
        textViewPropiedades = rootView.findViewById(R.id.txt_hierba_properties_detail)
        textViewIndicaciones = rootView.findViewById(R.id.txt_hierba_indications_detail)
        textViewContraIndicaciones = rootView.findViewById(R.id.txt_hierba_contraindications_detail)
        textViewEmpleo = rootView.findViewById(R.id.txt_hierba_empleo_detail)
        textViewSintomas = rootView.findViewById(R.id.txt_hierba_symptoms_detail)
        textViewUbicacion = rootView.findViewById(R.id.txt_hierba_ubication_detail)
        textViewGastronomia = rootView.findViewById(R.id.txt_hierba_gastronomy_detail)
        botonAbrirComentario = rootView.findViewById(R.id.btn_open_comment_detail)
        botonRegreso = rootView.findViewById(R.id.btn_regresar_detail)

        arguments?.let { args ->
            @Suppress("DEPRECATION")
            hierba = args.getSerializable("HIERBAS_BEAN") as? HierbasBean

            hierba?.let { h ->
                var imagePath = ""
                h.imgurl?.let {
                    imagePath = it.replace(Regex("\\.(png|jpg|jpeg)$"), "")
                }
                val uri = "@drawable/$imagePath"
                val imageResource = mContext.resources.getIdentifier(uri, "drawable", mContext.packageName)
                if (imageResource != 0) {
                    Picasso.get().load(imageResource).into(imageViewUrl)
                } else {
                    Picasso.get().load(R.drawable.hierba_card_background).into(imageViewUrl)
                }

                textViewNombre.text = h.nombre
                textViewNombreCientifico.text = "(${h.nombreCientifico})"
                textViewAlias.text = Html.fromHtml("${getString(R.string.aliasFragTitle)}${h.alias?.toString()?.replace(Regex("[^A-Za-zÑñáéíóúÁÉÍÓÚ, ]"), "")}", Html.FROM_HTML_MODE_LEGACY)
                textViewDescripcion.text = Html.fromHtml("${getString(R.string.descFragTitle)}${h.descripcion}", Html.FROM_HTML_MODE_LEGACY)
                textViewDescripcion.setOnTouchListener(TextAnimationColor(mContext))
                textViewSeccionUso.text = Html.fromHtml("${getString(R.string.sectionFragTitle)}${h.seccion}", Html.FROM_HTML_MODE_LEGACY)
                textViewPropiedades.text = Html.fromHtml("${getString(R.string.propertiesFragTitle)}${h.propiedades?.toString()?.replace(Regex("[^A-Za-zÑñáéíóúÁÉÍÓÚ, ]"), "")}", Html.FROM_HTML_MODE_LEGACY)

                textViewIndicaciones.text = Html.fromHtml("${getString(R.string.indicationsFragTitle)}${h.indicaciones}", Html.FROM_HTML_MODE_LEGACY)
                textViewIndicaciones.setOnTouchListener(TextAnimationColor(mContext))

                textViewContraIndicaciones.text = Html.fromHtml("${getString(R.string.contraIndicationsFragTitle)}${h.contraIndicaciones}", Html.FROM_HTML_MODE_LEGACY)
                textViewContraIndicaciones.setOnTouchListener(TextAnimationColor(mContext))

                textViewEmpleo.text = Html.fromHtml("${getString(R.string.useFragTitle)}${h.empleo}", Html.FROM_HTML_MODE_LEGACY)
                textViewEmpleo.setOnTouchListener(TextAnimationColor(mContext))

                textViewSintomas.text = Html.fromHtml("${getString(R.string.symptomsFragTitle)}${h.sintomas?.toString()?.replace(Regex("[^A-Za-zÑñáéíóúÁÉÍÓÚ, ]"), "")}", Html.FROM_HTML_MODE_LEGACY)
                textViewSintomas.setOnTouchListener(TextAnimationColor(mContext))

                textViewUbicacion.text = Html.fromHtml("${getString(R.string.locationFragTitle)}${h.ubicacion}", Html.FROM_HTML_MODE_LEGACY)
                textViewGastronomia.text = Html.fromHtml("${getString(R.string.gastronomyFragTitle)}${h.gastronomia}", Html.FROM_HTML_MODE_LEGACY)
                textViewGastronomia.setOnTouchListener(TextAnimationColor(mContext))

                var x = 1
                var contadorAuxiliarId = 100
                val linearLayout = rootView.findViewById<LinearLayout>(R.id.linear_fragment_id)

                if (!h.comentarios.isNullOrEmpty()) {
                    val textTituloComentarios = TextView(mContext).apply {
                        text = getString(R.string.titleComments)
                        setTextColor(Color.parseColor("#000000"))
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                        visibility = View.INVISIBLE
                    }
                    linearLayout.addView(textTituloComentarios)

                    h.comentarios?.forEach { comentarioBean ->
                        if ("habilitado".equals(comentarioBean.estado, ignoreCase = true)) {
                            if (textTituloComentarios.visibility == View.INVISIBLE) {
                                textTituloComentarios.visibility = View.VISIBLE
                            }

                            x++
                            contadorAuxiliarId++

                            val linearLayoutCommentPpal = LinearLayout(mContext).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply { setMargins(30, 20, 30, 20) }
                                setBackgroundColor(Color.parseColor("#f9f4f9"))

                                background = GradientDrawable().apply {
                                    setColor(0xFFFFFFFF.toInt())
                                    setStroke(2, 0xFF000000.toInt())
                                }
                            }

                            val linearHorizontalLayout = LinearLayout(mContext).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply { setMargins(30, 0, 30, 10) }
                            }

                            try {
                                if (!comentarioBean.imgFbUrlUsuario.isNullOrEmpty()) {
                                    val imgProfileFacebook = ImageView(mContext).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply { gravity = Gravity.START }
                                    }
                                    Picasso.get().load(comentarioBean.imgFbUrlUsuario).into(imgProfileFacebook)
                                    linearHorizontalLayout.addView(imgProfileFacebook)
                                }

                                comentarioBean.pais?.let { pais ->
                                    val imgBandera = ImageView(mContext).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply { gravity = Gravity.END }
                                    }

                                    val flagRes = when (pais.trim().lowercase()) {
                                        "chile" -> R.drawable.chileflag
                                        "venezuela" -> R.drawable.vzlaflag
                                        "argentina" -> R.drawable.argflag
                                        "united states", "estados unidos" -> R.drawable.usaflag
                                        in Constants.europeCountries.map { it.lowercase() } -> R.drawable.euflag
                                        else -> R.drawable.worldflag
                                    }
                                    Picasso.get().load(flagRes).resize(60, 40).into(imgBandera)
                                    linearHorizontalLayout.addView(imgBandera)
                                }

                                linearLayoutCommentPpal.addView(linearHorizontalLayout)

                                if (!comentarioBean.pais.isNullOrEmpty() && !comentarioBean.ciudad.isNullOrEmpty()) {
                                    val txtCiudadPais = TextView(mContext).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply { setMargins(20, 20, 20, 10) }
                                        text = "${comentarioBean.ciudad}, ${comentarioBean.pais}"
                                        textSize = 10f
                                        gravity = Gravity.START
                                        setTextColor(Color.parseColor("#3d0d0d"))
                                    }
                                    linearLayoutCommentPpal.addView(txtCiudadPais)
                                }

                                val txtComment = TextView(mContext).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply { setMargins(20, 0, 20, 10) }
                                    id = contadorAuxiliarId + x + x
                                    textSize = 12f
                                    text = Html.fromHtml(
                                        "${getString(R.string.commentUsuario)}${comentarioBean.nombreUsuario}<br/>" +
                                                "${getString(R.string.commentEmail)}${comentarioBean.emailUsuario}<br/>" +
                                                "${getString(R.string.commentText)}${comentarioBean.comentario}",
                                        Html.FROM_HTML_MODE_LEGACY
                                    )
                                    setTextColor(Color.parseColor("#000000"))
                                    gravity = Gravity.START
                                }
                                linearLayoutCommentPpal.addView(txtComment)
                                linearLayout.addView(linearLayoutCommentPpal)

                            } catch (e: Exception) {
                                Log.e(TAG, "Error: ", e)
                            }
                        }
                    }
                }
            }
        }

        botonAbrirComentario.setOnClickListener {
            val context = activity
            if (context is DetailResActivity) {
                val vistaDialogo = LayoutInflater.from(context).inflate(R.layout.detail_herb_dialogo_comentario, null)
                val alertDialogBuilder = AlertDialog.Builder(context).setView(vistaDialogo)

                layoutUserName = vistaDialogo.findViewById(R.id.layoutNombreUsuario)
                editTextUserName = vistaDialogo.findViewById(R.id.nombreUsuario)
                layoutEmailName = vistaDialogo.findViewById(R.id.layoutEmailUsuario)
                editTextEmail = vistaDialogo.findViewById(R.id.emailUsuario)
                layoutComentario = vistaDialogo.findViewById(R.id.layoutComentario)
                editTextComentario = vistaDialogo.findViewById(R.id.comentario)
                btnTomarDatosFacecbook = vistaDialogo.findViewById(R.id.btnTomarDatosFacecbook)

                editTextUserName.addTextChangedListener(MyTextWatcher(editTextUserName))
                editTextEmail.addTextChangedListener(MyTextWatcher(editTextEmail))

                if (accessToken != null) {
                    btnTomarDatosFacecbook.isEnabled = false
                    btnTomarDatosFacecbook.background = ContextCompat.getDrawable(context, R.drawable.btn_grey_face)
                }

                if (MemoryBeanAux.userFbData == null && accessToken != null) {
                    val graphUrl = "${Constants.URL_GRAPH_FACEBOOK_ME_DATA}${accessToken?.token}"
                    val progressBar = ProgressBar(context)
                    val jsonObject = JSONObject()
                    PostAsyncHttpTask(context, progressBar, "GET_DATA_FACE_WITH_TOKEN", jsonObject, hierba, hierba?.id ?: 0, null).execute(graphUrl)
                }

                MemoryBeanAux.userFbData?.let { fbData ->
                    if (!fbData[0].equals("sinnombre", ignoreCase = true)) {
                        layoutUserName.isErrorEnabled = false
                        editTextUserName.setText(fbData[0])
                        editTextUserName.isEnabled = false
                        editTextUserName.setTextColor(Color.parseColor("#293082"))
                    }

                    if (!fbData[3].equals("sincorreo", ignoreCase = true)) {
                        layoutEmailName.isErrorEnabled = false
                        editTextEmail.setText(fbData[3])
                        editTextEmail.isEnabled = false
                        editTextEmail.setTextColor(Color.parseColor("#293082"))
                    }
                }

                editTextComentario.addTextChangedListener(MyTextWatcher(editTextComentario))

                alertDialogBuilder.setPositiveButton(getString(R.string.btn_enviar)) { dialog, _ ->
                    try {
                        val usuario = editTextUserName.text.toString()
                        val email = editTextEmail.text.toString()
                        val comentario = editTextComentario.text.toString()

                        if (MemoryBeanAux.userFbData != null ||
                            (usuario.isNotEmpty() && email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && comentario.length > 4)) {

                            val progressBar = ProgressBar(context)
                            val jsonObject = JSONObject().apply {
                                put("id", (hierba?.comentarios?.size ?: 0) + 1)

                                var nombreToSend = MemoryBeanAux.userFbData?.get(0) ?: "sinnombre"
                                if (nombreToSend.equals("sinnombre", ignoreCase = true)) nombreToSend = usuario
                                put("nombreUsuario", nombreToSend)
                                put("fechaNacUsuario", MemoryBeanAux.userFbData?.get(2) ?: "")

                                var emailToSend = MemoryBeanAux.userFbData?.get(3) ?: "sincorreo"
                                if (emailToSend.equals("sincorreo", ignoreCase = true)) emailToSend = email
                                put("emailUsuario", emailToSend)
                                put("comentario", comentario)
                                put("ciudad", MemoryBeanAux.userFbData?.get(4) ?: "")
                                put("pais", MemoryBeanAux.userFbData?.get(5) ?: "")
                                put("imgFbUrlUsuario", MemoryBeanAux.userFbUlrImage ?: profileUser?.getProfilePictureUri(100, 100)?.toString() ?: "")
                                put("estado", "inhabilitado")
                            }

                            url = "${Constants.URL_SERVIDOR_RMT_APP_HIERBAS}${Constants.URL_POST_COMENTARIO_HIERBA}${hierba?.id}"
                            PostAsyncHttpTask(context, progressBar, "ADD_COMMENT", jsonObject, hierba, hierba?.id ?: 0, dialog).execute(url)
                        } else {
                            Toast.makeText(context, getString(R.string.errorCommentForm), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "${getString(R.string.errorGral2)}$e", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }.setNegativeButton(getString(R.string.btn_cancelar)) { dialog, _ -> dialog.cancel() }

                val alertDialog = alertDialogBuilder.create()

                btnTomarDatosFacecbook.setOnClickListener {
                    startActivity(Intent(activity, FacebookLoginComment::class.java))
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }

        botonRegreso.setOnClickListener {
            if (activity is DetailResActivity) {
                activity?.finish()
            }
        }

        return rootView
    }

    private fun validateName(): Boolean {
        return if (editTextUserName.text.toString().trim().isEmpty()) {
            layoutUserName.error = getString(R.string.error_nombre_dialogo)
            requestFocus(editTextUserName)
            false
        } else {
            layoutUserName.isErrorEnabled = false
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = editTextEmail.text.toString().trim()
        return if (email.isEmpty() || !isValidEmail(email)) {
            layoutEmailName.error = getString(R.string.error_email_dialogo)
            requestFocus(editTextEmail)
            false
        } else {
            layoutEmailName.isErrorEnabled = false
            true
        }
    }

    private fun validateComment(): Boolean {
        return if (editTextComentario.text.toString().trim().isEmpty()) {
            layoutComentario.error = getString(R.string.error_comentarios_dialogo)
            requestFocus(editTextComentario)
            false
        } else {
            layoutComentario.isErrorEnabled = false
            true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private inner class MyTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            when (view.id) {
                R.id.nombreUsuario -> validateName()
                R.id.emailUsuario -> validateEmail()
                R.id.comentario -> validateComment()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Profile.getCurrentProfile()?.let { profileUserFb = it }
        AccessToken.getCurrentAccessToken()?.let { accessToken = it }
    }

    override fun onStop() {
        super.onStop()
        accessTokenTracker.stopTracking()
        profileTracker.stopTracking()
    }
}