package io.almayce.dev.intenta.view

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.adapter.CustomCheckAdapter
import io.almayce.dev.intenta.databinding.*
import io.almayce.dev.intenta.global.SelectedPoint
import io.almayce.dev.intenta.presenter.CheckPresenter


class CheckActivity : MvpAppCompatActivity(), CheckView, CustomCheckAdapter.ItemClickListener, CustomCheckAdapter.ItemLongClickListener {

    @InjectPresenter
    lateinit var presenter: CheckPresenter
    private lateinit var binding: ActivityCheckBinding

    private lateinit var dialogAddBinding: DialogAddBinding
    private lateinit var dialogEditBinding: DialogEditBinding
    private lateinit var dialogDeleteBinding: DialogDeleteBinding
    private lateinit var dialogModeBinding: DialogModeBinding

    private lateinit var dialogAdd: AlertDialog
    private lateinit var dialogEdit: AlertDialog
    private lateinit var dialogDelete: AlertDialog
    private lateinit var dialogMode: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter.onCreate(this)
        binding = DataBindingUtil.setContentView<ActivityCheckBinding>(this, R.layout.activity_check)
        binding.recyclerView.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?

        presenter.adapter.setClickListener(this)
        presenter.adapter.setLongClickListener(this)
        binding.recyclerView.adapter = presenter.adapter

        presenter.setTitle()
        presenter.read()

        binding.ivAdd.setOnClickListener { addPointDialog() }
        binding.tvPointTitle.setOnClickListener { onInfoClick() }
        binding.tvPointDescription.setOnClickListener { onInfoClick() }
        binding.ivPhoto.setOnClickListener { onPhotoClick() }
        binding.ivShare.setOnClickListener { onShareClick() }
        binding.ivStart.setOnClickListener { onStartClick() }

        verifyStoragePermissions()
        checkCameraHardware(this)

        if (!isOnline()) showToast("Check the connection.")
    }

    private fun shareSelectedPoint() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, SelectedPoint.title)
        sharingIntent.putExtra(Intent.EXTRA_TEXT,
                "${SelectedPoint.title} " +
                        "\n\n${SelectedPoint.description} " +
                        "\n\n${SelectedPoint.photo}")
        startActivity(Intent.createChooser(sharingIntent, "Send via:"))
    }

    override fun setTitle(title: String?) {
        binding.tvTitle.text = title
    }

    override fun setPhoto(bitmap: Bitmap?) {
        dialogEditBinding.ivPreview.scaleType = ImageView.ScaleType.CENTER_CROP
        dialogEditBinding.ivPreview.setImageBitmap(bitmap)
    }


    private fun onPhotoClick() {
        if (presenter.listIsEmpty() || binding.tvPointTitle.text.isNullOrEmpty())
            addPointDialog()
        else
            getPointPhoto()
    }

    private fun onShareClick() {
        if (presenter.listIsEmpty() || binding.tvPointTitle.text.isNullOrEmpty())
            addPointDialog()
        else
            shareSelectedPoint()
    }

    private fun onInfoClick() {
        if (presenter.listIsEmpty() || binding.tvPointTitle.text.isNullOrEmpty())
            addPointDialog()
        else
            editPointDialog()
    }

    private fun onStartClick() {
        if (presenter.listIsEmpty() || binding.tvPointTitle.text.isNullOrEmpty())
            addPointDialog()
        else
            modeDialog()
    }

    override fun addPointDialog() {
        dialogAddBinding = DataBindingUtil
                .inflate<DialogAddBinding>(LayoutInflater.from(this), R.layout.dialog_add, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogAddBinding.getRoot())

        dialogAddBinding.ivDone.setOnClickListener({
            var title = dialogAddBinding.etName.text.toString()

            if (!title.isEmpty()) {
                presenter.addPoint(title)
                dialogAdd.cancel()
            } else
                showToast("Fill in the fields.")
        })

        dialogAddBinding.ivClose.setOnClickListener { v ->
            dialogAdd.cancel()
        }

        builder.setCancelable(true)
        dialogAdd = builder.create()
        dialogAdd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialogAdd.show()
        dialogAdd.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun editPointDialog() {
        val builder = AlertDialog.Builder(this)
        dialogEditBinding = DataBindingUtil
                .inflate<DialogEditBinding>(LayoutInflater.from(this), R.layout.dialog_edit, null, false)
        dialogEditBinding.etName.setText(binding.tvPointTitle.text)
        dialogEditBinding.etDescription.setText(binding.tvPointDescription.text)
        presenter.getImageFromURL(SelectedPoint.photo!!)
        dialogEditBinding.ivDelete.setOnClickListener { v ->
            deletePointDialog()
            dialogEdit.cancel()
        }

        dialogEditBinding.ivDone.setOnClickListener({ v ->

            var title = dialogEditBinding.etName.text.toString()
            var description = dialogEditBinding.etDescription.text.toString()

            if (!title.isEmpty() && !description.isEmpty()) {
                SelectedPoint.title = title
                SelectedPoint.description = description
                presenter.updatePoint()
                dialogEdit.cancel()
            } else
                showToast("Fill in the fields.")
        })

        dialogEditBinding.ivClose.setOnClickListener { v ->
            dialogEdit.cancel()
        }

        dialogEditBinding.ivPreview.setOnClickListener { v ->
            onPhotoClick()
        }

        builder.setView(dialogEditBinding.getRoot())
        builder.setCancelable(false)
        dialogEdit = builder.create()
        dialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialogEdit.show()
        dialogEdit.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun deletePointDialog() {
        dialogDeleteBinding = DataBindingUtil
                .inflate<DialogDeleteBinding>(LayoutInflater.from(this), R.layout.dialog_delete, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogDeleteBinding.getRoot())

        dialogDeleteBinding.tvPointTitle.setText(binding.tvPointTitle.text)

        dialogDeleteBinding.ivDone.setOnClickListener({ v ->
            presenter.removePoint()
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

    private fun deletePointDialog(view: View, position: Int) {
        dialogDeleteBinding = DataBindingUtil
                .inflate<DialogDeleteBinding>(LayoutInflater.from(this), R.layout.dialog_delete, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogDeleteBinding.getRoot())

        dialogDeleteBinding.tvPointTitle.setText(view.contentDescription)

        dialogDeleteBinding.ivDone.setOnClickListener({ v ->
            presenter.removePoint(position)
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

    private fun modeDialog() {
        dialogModeBinding = DataBindingUtil
                .inflate<DialogModeBinding>(LayoutInflater.from(this), R.layout.dialog_mode, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogModeBinding.getRoot())

        dialogModeBinding.rlMode1.setOnClickListener { v ->
            dialogMode.cancel()
            startActivity(1)
        }

        dialogModeBinding.rlMode2.setOnClickListener { v ->
            dialogMode.cancel()
            startActivity(2)
        }

        dialogModeBinding.rlMode3.setOnClickListener { v ->
            dialogMode.cancel()
            startActivity(3)
        }

        builder.setCancelable(true)
        dialogMode = builder.create()
        dialogMode.show()
        dialogMode.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun startActivity(num: Int) {
        var intent = Intent(this, ProcessActivity::class.java)
        intent.putExtra("mode", num)
        startActivity(intent)
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
    }


    private val CAMERA_PHOTO = 111
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA)

    fun verifyStoragePermissions() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(this@CheckActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this@CheckActivity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    private lateinit var path: Uri

    fun getPointPhoto() {
        var intentCapture = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        var contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "temp")
        path = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
        intentCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                path)

        startActivityForResult(intentCapture, CAMERA_PHOTO);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            var projection = arrayOf<String>(MediaStore.Images.Media.DATA);
            var cursor = getContentResolver().query(path, projection, null, null, null);
            var index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            var capturedImageFilePath = cursor.getString(index);
            cursor.close()

            presenter.transformBitmap(capturedImageFilePath)
        }
    }

    override fun setInfo(title: String?, description: String?, photo: String?) {
        binding.tvPointTitle.text = title
        binding.tvPointDescription.text = description
    }

    private var item_pressed: Long = 0
    private var item_position = 0

    override fun onItemClick(view: View, position: Int) {
        if (position == item_position && item_pressed + 2000 > System.currentTimeMillis())
            editPointDialog()
        else {
            presenter.setInfo(position)
            item_position = position
        }
        item_pressed = System.currentTimeMillis()
    }

    override fun onItemLongClick(view: View, position: Int) {
        deletePointDialog(view, position)
    }

    override fun onBackPressed() {
        var backIntent = Intent(this, SessionActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(backIntent)
        overridePendingTransition(R.anim.alpha_anim_in, R.anim.alpha_anim_out)
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

    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            binding.ivPhoto.visibility = View.INVISIBLE
            dialogEditBinding.ivPreview.visibility = View.INVISIBLE
            return false;
        }
    }
}