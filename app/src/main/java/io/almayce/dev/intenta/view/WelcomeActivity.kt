package io.almayce.dev.intenta.view

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.databinding.ActivityWelcomeBinding

/**
 * Created by almayce on 27.07.17.
 */

class WelcomeActivity : MvpAppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gac: GoogleApiClient
    private val RC_SIGN_IN: Int = 1
    private lateinit var signinIntent: Intent

    override fun onResume() {
        super.onResume()
        overridePendingTransition(R.anim.alpha_anim_start, R.anim.alpha_anim_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.alpha_anim_start, R.anim.alpha_anim_out)
        binding = DataBindingUtil.setContentView<ActivityWelcomeBinding>(this, R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        gac = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        signinIntent = Intent(this, SessionActivity::class.java)

        if (isOnline()) {
            if (auth.currentUser != null)
                startSessionActvity()
        } else showToast("Check the connection.")
    }

    fun onViewClick(v: View) {
        when (v.contentDescription.toString()) {
            "signin" -> if (isOnline()) signin() else showToast("Check the connection.")
            "premium" -> {
                signinIntent.putExtra("premiumRequest", "premiumRequest")
                if (isOnline()) signin() else showToast("Check the connection.")
            }
            "about" -> {
                var aboutIntent = Intent(this, AboutActivity::class.java)
                aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                aboutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(aboutIntent)
                overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
            }
        }
    }

    fun signin() {
        if (auth.currentUser == null) {
            var signInIntent = Auth.GoogleSignInApi.getSignInIntent(gac)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else startSessionActvity()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess)
            firebaseAuthWithGoogle(result.signInAccount)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        startSessionActvity()
                    } else
                        showToast("Error, try again.")
                })
    }

    private fun startSessionActvity() {
        showToast("Signed in.")
        signinIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        signinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signinIntent)
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }

    private var back_pressed: Long = 0
    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_exit)
        } else
            showToast("Tap again to exit.");
        back_pressed = System.currentTimeMillis();
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        showToast("Check the connection.");
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text,
                Toast.LENGTH_SHORT).show()
    }

    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

}
