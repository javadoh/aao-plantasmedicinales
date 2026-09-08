package com.javadoh.plantasmedicinales.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.beans.FacebookUserBean
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.utils.bean.MemoryBeanAux
import java.util.ArrayList

class ParseResults(
    private var hierbasList: ArrayList<HierbasBean>?,
    private val hierbaBean: HierbasBean?,
    private val context: Context
) {
    companion object {
        val TAG: String = ParseResults::class.java.name
    }

    fun parseResult(result: String): ArrayList<HierbasBean> {
        try {
            val listType = object : TypeToken<ArrayList<HierbasBean>>() {}.type
            hierbasList = Gson().fromJson(result, listType) ?: ArrayList()
        } catch (e: Exception) {
            Log.d(TAG, context.getString(R.string.errorGral), e)
            hierbasList = ArrayList()
        }
        return hierbasList!!
    }

    fun parseFaceUserBeanData(result: String) {
        val userFbData = arrayOfNulls<String>(6)
        try {
            val type = object : TypeToken<FacebookUserBean>() {}.type
            val faceUserBean: FacebookUserBean = Gson().fromJson(result, type)

            userFbData[0] = faceUserBean.name ?: context.getString(R.string.noUserName)
            userFbData[1] = faceUserBean.gender ?: context.getString(R.string.noGender)
            userFbData[2] = faceUserBean.birthday ?: context.getString(R.string.noBirthday)
            userFbData[3] = faceUserBean.email ?: context.getString(R.string.noEmail)

            val locationName = faceUserBean.location?.name?.trim()
            if (!locationName.isNullOrEmpty()) {
                val commaIndex = locationName.indexOf(",")
                val lastCommaIndex = locationName.lastIndexOf(",")
                userFbData[4] = if (commaIndex != -1) locationName.substring(0, commaIndex) else locationName
                userFbData[5] = if (lastCommaIndex != -1) locationName.substring(lastCommaIndex + 1) else context.getString(R.string.noCountry)
            } else {
                userFbData[4] = context.getString(R.string.noCity)
                userFbData[5] = context.getString(R.string.noCountry)
            }
            MemoryBeanAux.userFbData = userFbData as Array<String>?;
        } catch (e: Exception) {
            Log.d(TAG, context.getString(R.string.errorGral), e)
        }
    }
}