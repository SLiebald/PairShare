package com.sliebald.pairshare.ui.addExpense

import android.Manifest
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
        mBinding.button.setOnClickListener {
            // check if input is valid
            if (mBinding.etAddExpense.text.toString().isEmpty() || mBinding.etAddDate.text.toString().isEmpty()) {
                Snackbar.make(mBinding.clAddExpenseLayout, "Expense and date cannot be empty!",
                        Snackbar.LENGTH_SHORT).show()
            }
            // Get the Username and add expense.
            try {
                val amount = mBinding.etAddExpense.text.toString().toDouble()
                mViewModelMain.getUser().observe(this, object : Observer<User> {
                    override fun onChanged(user: User) {
                        mViewModelMain.getUser().removeObserver(this)
                        mViewModel.addExpense(amount, mBinding.etAddComment.text.toString(),
                                user.username)
                        Snackbar.make(mBinding.clAddExpenseLayout,
                                "Added expense of ${mBinding.etAddExpense.text} to list",
                                Snackbar.LENGTH_SHORT).show()
                        mBinding.etAddComment.text.clear()
                        mBinding.etAddExpense.text.clear()
                        mBinding.ibAddImage.setImageResource(android.R.color.transparent)
                        UIUtil.hideKeyboard(Objects.requireNonNull(activity))

                    }
                })

            } catch (ex: NumberFormatException) {
                Snackbar.make(mBinding.clAddExpenseLayout, "Invalid date",
                        Snackbar.LENGTH_SHORT).show()
            }
        }

        mBinding.ibAddImage.setOnClickListener { takePicture() }

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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (activity != null && context != null
                && takePictureIntent.resolveActivity(context!!.packageManager) != null) {


            if (ContextCompat.checkSelfPermission(context!!,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            } else {


                val photoFile: File?
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    return
                }

                // Continue only if the File was successfully created
                val photoURI = FileProvider.getUriForFile(context!!,
                        "com.sliebald.pairshare.fileprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
            }
        }

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
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        mViewModel.latestImagePath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PICTURE && resultCode == RESULT_OK && data != null
                && context != null) {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver,
                        Uri.fromFile(File(mViewModel.latestImagePath)))
                mViewModel.image = imageBitmap
                mBinding.ibAddImage.setImageBitmap(mViewModel.image)

            } catch (e: IOException) {
                e.printStackTrace()
            }

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
