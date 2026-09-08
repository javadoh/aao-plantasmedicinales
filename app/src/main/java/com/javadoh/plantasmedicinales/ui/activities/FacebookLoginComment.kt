package com.javadoh.plantasmedicinales.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.utils.bean.MemoryBeanAux

class FacebookLoginComment : AppCompatActivity() {

    private lateinit var loginButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    private lateinit var accessTokenTracker: AccessTokenTracker
    private lateinit var profileTracker: ProfileTracker
    private var accessToken: AccessToken? = null
    private var profileUserFb: Profile? = null
    private val userFbData = arrayOfNulls<String>(6)

    companion object {
        val TAG: String = FacebookLoginComment::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            callbackManager = CallbackManager.Factory.create()
            accessToken = AccessToken.getCurrentAccessToken()

            accessTokenTracker = object : AccessTokenTracker() {
                override fun onCurrentAccessTokenChanged(oldToken: AccessToken?, newToken: AccessToken?) {
                    accessToken = newToken
                }
            }

            profileTracker = object : ProfileTracker() {
                override fun onCurrentProfileChanged(oldProfile: Profile?, newProfile: Profile?) {
                    profileUserFb = newProfile
                }
            }

            accessTokenTracker.startTracking()
            profileTracker.startTracking()

            setContentView(R.layout.dialog_facebook_activity_login)
            loginButton = findViewById(R.id.login_button)

            loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) { getFacebookData() }
                override fun onCancel() {
                    Toast.makeText(applicationContext, getString(R.string.facebookCancelSession), Toast.LENGTH_SHORT).show()
                    finish()
                }
                override fun onError(error: FacebookException) {
                    Toast.makeText(applicationContext, getString(R.string.facebookErrorSession), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Error: ", error)
                    finish()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "Error: ", e)
            Toast.makeText(applicationContext, getString(R.string.facebookErrorSession), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
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

    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(applicationContext, getString(R.string.facebookCancelSession), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun nextActivity(profile: Profile?, userFbData: Array<String?>) {
        val currentProfile = profile ?: Profile.getCurrentProfile()
        currentProfile?.let {
            Log.d(TAG, "User Data: ${userFbData.contentToString()}")
            Log.d(TAG, "Name and Image: ${it.firstName}, ${it.getProfilePictureUri(100, 100)}")
            MemoryBeanAux.userFbData = userFbData as Array<String>?;
            MemoryBeanAux.userFbUlrImage = it.getProfilePictureUri(100, 100).toString();
            finish()
        }
    }

    private fun getFacebookData() {
        try {
            Toast.makeText(applicationContext, getString(R.string.facebookRecoverDataSingle), Toast.LENGTH_SHORT).show()

            val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { objectData, _ ->
                if (objectData != null) {
                    userFbData[0] = objectData.optString("name", getString(R.string.noUserName))
                    userFbData[1] = objectData.optString("gender", getString(R.string.noGender))
                    userFbData[2] = objectData.optString("birthday", getString(R.string.noBirthday))
                    userFbData[3] = objectData.optString("email", getString(R.string.noEmail))

                    val locationObj = objectData.optJSONObject("location")
                    if (locationObj != null) {
                        userFbData[4] = locationObj.optString("name", getString(R.string.noCountry))
                        userFbData[5] = "Internacional"
                    } else {
                        userFbData[4] = getString(R.string.noCity)
                        userFbData[5] = getString(R.string.noCountry)
                    }
                    nextActivity(profileUserFb, userFbData)
                }
            }

            val parameters = Bundle().apply {
                putString("fields", "id, name, birthday, gender, email, location")
            }
            request.parameters = parameters
            request.executeAsync()

        } catch (e: Exception) {
            Log.d(TAG, "Error: ", e)
            Toast.makeText(applicationContext, getString(R.string.facebookCancelSession), Toast.LENGTH_SHORT).show()
        }
    }
}