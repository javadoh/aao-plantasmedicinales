package com.javadoh.plantasmedicinales.utils

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class PostAsyncHttpTask(
    private val context: Context,
    private val progressBar: ProgressBar,
    private val flagCall: String,
    private val jsonReqObj: JSONObject,
    private val hierba: HierbasBean?,
    private val hierbaId: Int,
    private val dialog: DialogInterface?
) {
    companion object {
        val TAG: String = PostAsyncHttpTask::class.java.name
    }

    fun execute(urlParam: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                doInBackground(urlParam)
            }
            onPostExecute(result)
        }
    }

    private fun doInBackground(urlParam: String): Int {
        var result: Int
        try {
            val url = URL(urlParam)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("charset", "utf-8")
            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000

            if ("ADD_COMMENT".equals(flagCall, ignoreCase = true)) {
                urlConnection.requestMethod = "PUT"
                urlConnection.doInput = true
                urlConnection.doOutput = true
                urlConnection.useCaches = false
                urlConnection.setRequestProperty("Accept", "application/json")

                val outputBytes = jsonReqObj.toString().toByteArray(Charsets.UTF_8)
                urlConnection.outputStream.use { os ->
                    os.write(outputBytes)
                    os.flush()
                }
            }

            val statusCode = urlConnection.responseCode
            if (statusCode == 200 || statusCode == 202) {
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                if ("GET_DATA_FACE_WITH_TOKEN".equals(flagCall, ignoreCase = true)) {
                    val parseResults = ParseResults(null, hierba, context)
                    parseResults.parseFaceUserBeanData(response.toString())
                }
                result = 1
            } else {
                result = 3
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException, is ConnectException -> {
                    e.printStackTrace()
                    result = 3
                }
                else -> {
                    Log.d(TAG, e.localizedMessage ?: "Unknown error")
                    throw RuntimeException(context.getString(R.string.errorGral), e.cause)
                }
            }
        }
        return result
    }

    private fun onPostExecute(result: Int) {
        progressBar.visibility = View.GONE

        if (result == 1) {
            dialog?.dismiss()
            if ("GET_DATA_FACE_WITH_TOKEN".equals(flagCall, ignoreCase = true)) {
                Toast.makeText(context, context.getString(R.string.facebookDataRecovered), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.facebookCommentSucceed), Toast.LENGTH_SHORT).show()
            }
        } else {
            dialog?.dismiss()
            if ("GET_DATA_FACE_WITH_TOKEN".equals(flagCall, ignoreCase = true)) {
                Toast.makeText(context, context.getString(R.string.facebookGralError), Toast.LENGTH_SHORT).show()
            } else {
                val errorMsg = if (result == 3) R.string.facebookCommentError else R.string.errorGral2
                Toast.makeText(context, context.getString(errorMsg), Toast.LENGTH_SHORT).show()
            }
        }
    }
}