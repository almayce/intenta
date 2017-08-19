package io.almayce.dev.intenta.view

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.firebase.auth.FirebaseAuth
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.adapter.CustomSessionAdapter
import io.almayce.dev.intenta.databinding.ActivitySessionBinding
import io.almayce.dev.intenta.databinding.DialogDeleteBinding
import io.almayce.dev.intenta.databinding.DialogSessionBinding
import io.almayce.dev.intenta.global.SelectedSession
import io.almayce.dev.intenta.model.FirebaseManager
import io.almayce.dev.intenta.presenter.SessionPresenter

class SessionActivity : MvpAppCompatActivity(),
        SessionView,
        CustomSessionAdapter.ItemClickListener,
        CustomSessionAdapter.ItemLongClickListener,
        BillingProcessor.IBillingHandler {

    @InjectPresenter
    lateinit var presenter: SessionPresenter
    private lateinit var binding: ActivitySessionBinding
    private lateinit var dialogDeleteBinding: DialogDeleteBinding

    private lateinit var dialogAdd: AlertDialog
    private lateinit var dialogDelete: AlertDialog

    private lateinit var bp: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bp = BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkrz/jdDKgzqVY3nQhJNtOYQyZgYG9ZWF3vCp0ciG1vN/JfFNO0HhEDLiaMBvDyGPbrysfMbubRVaUazZqsKD27oiKheHExt+S46loxDDast8UjEO2LCfOPFhBYdU/G/XiuzDKqTTq20z4pyQq6RPAJWdnoY1bgXqSf7C+29EtcHHQWI59LfpMTkQV1XyoorVwRxNdPYvTwKhQ+NMFfdstIXmo50DqW1jhD8R7LZr1meVEry/6P6Pj5+riFrAse2drBh4YDejqtm3pXJK5R7Ntna3FTKMM4wcOIj8oHCLl1ZLeEw243WvzwARx0OLP8Vz59YZcQa+P9YJ5A26Cr56KQIDAQAB", this)
        presenter.onCreate(this)
        binding = DataBindingUtil.setContentView<ActivitySessionBinding>(this, R.layout.activity_session)
        binding.recyclerView.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        binding.ivAdd.setOnClickListener { v -> presenter.addRequest() }
        binding.rlSignout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var backIntent = Intent(this, WelcomeActivity::class.java)
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backIntent)
            showToast("Signed out.")
            overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
        }
        binding.recyclerView.adapter = presenter.adapter
        presenter.adapter.setClickListener(this)
        presenter.adapter.setLongClickListener(this)
        presenter.read()
        if (!isOnline()) showToast("Check the connection.")
    }

    override fun addSessionDialog() {
        val dialogBinding = DataBindingUtil
                .inflate<DialogSessionBinding>(LayoutInflater.from(this), R.layout.dialog_session, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.getRoot())


        var array = resources.getStringArray(R.array.Hints)
        val adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, array)
        dialogBinding.etName.setAdapter(adapter)
        dialogBinding.ivDone.setOnClickListener({ v ->
            var title = dialogBinding.etName.text.toString()
            if (!title.isEmpty()) {
                presenter.addSession(title)
                dialogAdd.cancel()
            } else
                showToast("Fill in the fields.")
        })

        dialogBinding.ivClose.setOnClickListener { v ->
            dialogAdd.cancel()
        }

        builder.setCancelable(true)
        dialogAdd = builder.create()
        dialogAdd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialogAdd.show()
        dialogAdd.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onItemClick(view: View, position: Int) {
        var intent = Intent(this, CheckActivity::class.java)
        SelectedSession.idSession = presenter.getId(position)
        SelectedSession.title = presenter.getTitle(position)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }

    override fun onItemLongClick(view: View, position: Int) {
        deleteSessionDialog(view, position)
    }

    private fun deleteSessionDialog(view: View, position: Int) {
        dialogDeleteBinding = DataBindingUtil
                .inflate<DialogDeleteBinding>(LayoutInflater.from(this), R.layout.dialog_delete, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogDeleteBinding.getRoot())

        dialogDeleteBinding.tvPointTitle.setText(view.contentDescription)

        dialogDeleteBinding.ivDone.setOnClickListener({ v ->
            presenter.removeSession(position)
            dialogDelete.cancel()
        })

        dialogDeleteBinding.ivClose.setOnClickListener { v ->
            dialogDelete.cancel()
        }

        builder.setCancelable(true)
        dialogDelete = builder.create()
        dialogDelete.show()
        dialogDelete.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private var back_pressed: Long = 0
    override fun onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_exit)
        } else
            showToast("Tap again to exit.")
        back_pressed = System.currentTimeMillis()
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


    override fun purchase() {
        if (BillingProcessor.isIabServiceAvailable(applicationContext))
            bp.purchase(this, "premium")
    }

    override fun onBillingInitialized() {
        if (!intent.getStringExtra("premiumRequest").isNullOrEmpty()) {
            presenter.getPremiumStatus()
        }
    }

    override fun onPurchaseHistoryRestored() {
        Log.d("IABv3", "onPurchaseHistoryRestored")
    }

    override fun onProductPurchased(productId: String?, details: TransactionDetails?) {
        FirebaseManager.setPremiumStatus()
        showToast("Premium account activated.")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        showToast("Error, try again.")
    }

    public override fun onDestroy() {
        if (bp != null) {
            bp.release()
        }
        super.onDestroy()
    }
}