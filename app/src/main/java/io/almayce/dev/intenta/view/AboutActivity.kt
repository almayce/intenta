package io.almayce.dev.intenta.view

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.databinding.ActivityAboutBinding

/**
 * Created by almayce on 27.07.17.
 */
class AboutActivity : MvpAppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityAboutBinding>(this, R.layout.activity_about)
    }

    fun onViewClick(v: View) {
        when (v.contentDescription.toString()) {
            "back" -> startActivity(Intent(this, WelcomeActivity::class.java))
            "send" -> {send()}
        }
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }

    private fun send() {
        val to = "intentamobile@gmail.com"

        val email = Intent(Intent.ACTION_SEND)
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        email.putExtra(Intent.EXTRA_SUBJECT, "intenta")
        email.putExtra(Intent.EXTRA_TEXT, "")

        //для того чтобы запросить email клиент устанавливаем тип
        email.type = "message/rfc822"

        startActivity(Intent.createChooser(email, "Send email to $to via:"))
    }
}