package io.almayce.dev.intenta.view

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.databinding.ActivityProcessBinding
import io.almayce.dev.intenta.model.Point
import io.almayce.dev.intenta.presenter.ProcessPresenter

/**
 * Created by almayce on 08.08.17.
 */
class ProcessActivity : MvpAppCompatActivity(), ProcessView {

    @InjectPresenter
    lateinit var presenter: ProcessPresenter

    private lateinit var binding: ActivityProcessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityProcessBinding>(
                this, R.layout.activity_process)
        presenter.initMode(intent.getIntExtra("mode", 3))
        binding.ivDone.setOnClickListener { presenter.onNext(true) }
        binding.ivSkip.setOnClickListener { presenter.onNext(false) }
        if (!isOnline()) showToast("Check the connection.")
    }

    override fun showPointInfo(p: Point, index: Int, count: Int) {
        binding.tvTitle.text = "$index / $count"
        binding.tvPointTitle.text = p.title
        binding.tvPointDescription.text = p.description
        presenter.getImage()
    }

    override fun setPhoto(b: Bitmap?) {
        binding.ivPhoto.setImageBitmap(b)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, CheckActivity::class.java))
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }

    override fun showToast(text: String) {
        Toast.makeText(this, text,
                Toast.LENGTH_SHORT).show()
    }

    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

}