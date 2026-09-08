package com.javadoh.plantasmedicinales.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.adapters.BusquedaRespuestaAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.ArrayList

class AsyncHttpTask(
    private val context: Context,
    private val mRecyclerView: RecyclerView,
    private var adapter: BusquedaRespuestaAdapter?,
    private val progressBar: ProgressBar,
    private var hierbasList: ArrayList<HierbasBean>?,
    private val imgNoData: ImageView,
    private val imgNoConex: ImageView,
    private val dataUser: Array<String>?
) {
    companion object {
        val TAG: String = AsyncHttpTask::class.java.name
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

            urlConnection.connectTimeout = 10000
            urlConnection.readTimeout = 10000
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("charset", "utf-8")

            val statusCode = urlConnection.responseCode
            Log.d(TAG, "Request URL: $urlParam")
            Log.d(TAG, "Response Code: $statusCode")

            if (statusCode == 200) {
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                Log.d(TAG, "Response Body: ${response.toString()}")

                val parseResults = ParseResults(hierbasList, null, context)
                hierbasList = parseResults.parseResult(response.toString())

                result = 1

                if (hierbasList?.isEmpty() == true) {
                    result = 2
                }
            } else {
                result = 3
            }
        } catch (e: Exception) {
            when (e) {
                is SocketTimeoutException, is SocketException -> {
                    e.printStackTrace()
                    result = 3
                }
                else -> {
                    Log.d(TAG, e.localizedMessage ?: "Unknown Error")
                    throw RuntimeException(context.getString(R.string.errorGral), e.cause)
                }
            }
        }
        return result
    }

    private fun onPostExecute(result: Int) {
        progressBar.visibility = View.GONE

        when (result) {
            1 -> {
                adapter = BusquedaRespuestaAdapter(context, hierbasList, dataUser)
                mRecyclerView.adapter = adapter
            }
            2 -> {
                imgNoData.visibility = View.VISIBLE
                Toast.makeText(context, context.getString(R.string.errorSinResultados), Toast.LENGTH_SHORT).show()
            }
            3 -> {
                imgNoConex.visibility = View.VISIBLE
                Toast.makeText(context, context.getString(R.string.errorSinConexion), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, context.getString(R.string.errorGral2), Toast.LENGTH_SHORT).show()
            }
        }
    }
}