package com.sliebald.pairshare.ui.addExpense

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.sliebald.pairshare.MainActivityViewModel
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.models.User
import com.sliebald.pairshare.databinding.FragmentAddExpenseBinding
import com.sliebald.pairshare.utils.ImageUtils
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * The [AddExpenseFragment] gives users the option to add new expenses to the currently
 * selected list.
 */
class AddExpenseFragment : Fragment() {
    /**
     * [androidx.lifecycle.ViewModel] of this fragment.
     */
    private val mViewModel: AddExpenseViewModel by viewModels()

    private val mViewModelMain: MainActivityViewModel by activityViewModels()


    /**
     * Databinding of the corresponding fragment layout.
     */
    private lateinit var mBinding: FragmentAddExpenseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_add_expense, container, false)

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupTimePicker()
        mViewModel.calendar.observe(this, Observer { this.setDate(it) })

        // Add onClicklistener for adding an expense.
        mBinding.btAddExpense.setOnClickListener {
            // check if input is valid
            if (mBinding.etAddExpense.text.toString().isEmpty() || mBinding.etAddDate.text.toString().isEmpty()) {
                Snackbar.make(mBinding.clAddExpenseLayout, "Expense and date cannot be empty!",
                        Snackbar.LENGTH_SHORT).show()

            } else {
                // Get the Username and add expense.
                try {
                    val amount = mBinding.etAddExpense.text.toString().toDouble()
                    mViewModelMain.user.observe(this, object : Observer<User> {
                        override fun onChanged(user: User) {
                            mViewModelMain.user.removeObserver(this)
                            mViewModel.addExpense(amount, mBinding.etAddComment.text.toString(),
                                    user.username)
                            Snackbar.make(mBinding.clAddExpenseLayout,
                                    "Added expense of ${mBinding.etAddExpense.text} to list",
                                    Snackbar.LENGTH_SHORT).show()
                            mBinding.etAddComment.text.clear()
                            mBinding.etAddExpense.text.clear()
                            mBinding.ivAddImage.setImageDrawable(null)
                            UIUtil.hideKeyboard(Objects.requireNonNull(activity))

                        }
                    })

                } catch (ex: NumberFormatException) {
                    Snackbar.make(mBinding.clAddExpenseLayout, "Invalid date",
                            Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        mBinding.btAddPic.setOnClickListener { takePicture() }

    }

    /**
     * Sets up the time picker when clicking on the date [android.widget.EditText] in the UI.
     */
    private fun setupTimePicker() {
        mBinding.etAddDate.setOnClickListener {
            UIUtil.hideKeyboard(Objects.requireNonNull(activity))

            val datePickerDialog = DatePickerDialog(context!!, { _, year, month, dayOfMonth ->
                mViewModel.setDate(year, month, dayOfMonth)

            }, mViewModel.calendar.value!!.get(Calendar.YEAR),
                    mViewModel.calendar.value!!.get(Calendar.MONTH),
                    mViewModel.calendar.value!!.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }
    }

    /**
     * Set the date in the corresponding [android.widget.EditText] field in the UI
     * according to the given calendar object.
     *
     * @param calendar The calendar with the date to display.
     */
    private fun setDate(calendar: Calendar) {
        val format = SimpleDateFormat("EEE, dd.MM.yyyy", Locale.getDefault())
        mBinding.etAddDate.setText(format.format(calendar.time))
    }


    /**
     * Starts an [Intent] for retrieving an image.
     */
    private fun takePicture() {

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val picFile: File = createImageFile()
            val picURI = FileProvider.getUriForFile(context!!,
                    "com.sliebald.pairshare.fileprovider", picFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picURI)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
        }

    }


    /**
     * Check for the required permissions.
     */
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context!!, CAMERA) == PackageManager
                .PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!, READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request required permissions.
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
    }


    /**
     * Create a file where a full res image can be stored.
     * Based on https://developer.android.com/training/camera/photobasics#java
     *
     * @return The [File] for the image.
     * @throws IOException thrown in case of an IO error.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN).format(Date())
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile("JPEG_$timeStamp", ".jpg", storageDir)
        mViewModel.latestImagePath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Handle taking a picture
        if (requestCode == REQUEST_TAKE_PICTURE && resultCode == RESULT_OK) {
            mViewModel.image = ImageUtils.getResizedBitmap(Uri.fromFile(File(mViewModel
                    .latestImagePath)), 600, context!!)
            mViewModel.thumbnail = ImageUtils.getResizedBitmap(Uri.fromFile(File(mViewModel
                    .latestImagePath)), 160, context!!)

            mBinding.ivAddImage.setImageBitmap(mViewModel.image)
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 342

        /**
         * Request code for the external image capture activity
         */
        private const val REQUEST_TAKE_PICTURE = 6433
    }
}