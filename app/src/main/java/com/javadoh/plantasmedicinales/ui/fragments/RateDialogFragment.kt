package com.javadoh.plantasmedicinales.ui.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.javadoh.plantasmedicinales.R

class RateDialogFragment : DialogFragment() {

    companion object {
        private const val LAUNCHES_UNTIL_PROMPT = 10
        private const val DAYS_UNTIL_PROMPT = 2
        private const val MILLIS_UNTIL_PROMPT = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000L
        private const val PREF_NAME = "APP_RATER"
        private const val LAST_PROMPT = "LAST_PROMPT"
        private const val LAUNCHES = "LAUNCHES"
        private const val DISABLED = "DISABLED"

        fun show(context: Context, fragmentManager: FragmentManager) {
            val sharedPreferences = getSharedPreferences(context)
            val editor = sharedPreferences.edit()
            val currentTime = System.currentTimeMillis()

            var lastPromptTime = sharedPreferences.getLong(LAST_PROMPT, 0)
            if (lastPromptTime == 0L) {
                lastPromptTime = currentTime
                editor.putLong(LAST_PROMPT, lastPromptTime)
            }

            var shouldShow = false
            if (!sharedPreferences.getBoolean(DISABLED, false)) {
                val launches = sharedPreferences.getInt(LAUNCHES, 0) + 1
                if (launches > LAUNCHES_UNTIL_PROMPT && currentTime > lastPromptTime + MILLIS_UNTIL_PROMPT) {
                    shouldShow = true
                }
                editor.putInt(LAUNCHES, launches)
            }

            if (shouldShow) {
                editor.putInt(LAUNCHES, 0).putLong(LAST_PROMPT, System.currentTimeMillis()).apply()
                RateDialogFragment().show(fragmentManager, null)
            } else {
                editor.apply()
            }
        }

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_rate_app, null)

        return AlertDialog.Builder(activity)
            .setView(view)
            .setPositiveButton(R.string.rate_positive) { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}")))
                    getSharedPreferences(activity).edit().putBoolean(DISABLED, true).apply()
                    dismiss()
                } catch (e: Exception) {
                    Log.d("RateDialogFragment", getString(R.string.errorCannotRate))
                    Toast.makeText(activity, getString(R.string.errorCannotRate), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
            .setNeutralButton(R.string.rate_remind_later) { _, _ -> dismiss() }
            .setNegativeButton(R.string.rate_never) { _, _ ->
                getSharedPreferences(activity).edit().putBoolean(DISABLED, true).apply()
                dismiss()
            }
            .create()
    }
}