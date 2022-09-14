package com.bignerdranch.android.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.*

const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 0
private const val DATE_FORMAT = "EEE, MMM, dd"
const val ARG_PHOTO_PATH = "photo_path"


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private var PHONE = ""
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var chooseSuspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var observer: ViewTreeObserver
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID:$crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        chooseSuspectButton = view.findViewById(R.id.choose_suspect_button) as Button
        callSuspectButton = view.findViewById(R.id.call_suspect_button) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        observer = photoView.viewTreeObserver

        return view

    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                crime.title = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }


//        photoView.viewTreeObserver.addOnGlobalLayoutListener {
//            Log.d("TEST", "width = ${photoView.width}, height = ${photoView.height}")
//            if (photoFile != null) {
//                val bitmap = BitmapFactory.decodeFile(photoFile!!.path)
//                photoView.setImageBitmap(bitmap)
//            }
//        }


        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        chooseSuspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
//            val packageManager: PackageManager = requireActivity().packageManager
//            val resolvedActivity: ResolveInfo? =
//                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
//            if (resolvedActivity == null) {
//                 isEnabled = false TODO find out wtf
//            }
        }

        callSuspectButton.apply {
            Log.d("TEST PHONE", PHONE)
            checkPermission(android.Manifest.permission.READ_CONTACTS, 0)
            setOnClickListener {
                val phone: Uri = Uri.parse("tel:$PHONE")
                val callSuspectIntent = Intent(Intent.ACTION_DIAL, phone)
                startActivity(callSuspectIntent)
            }
        }

        photoButton.apply {
            val packageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            val resolvedActivity: ResolveInfo? =
//                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
//            if (resolvedActivity == null) {
//                // isEnabled = false
//            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                for (cameraActivity in cameraActivities) {
                    requireActivity()
                        .grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }

            photoView.setOnClickListener {
                // activity?.let { it1 -> BlankFragment().show(it1.supportFragmentManager, "") }
                val args = Bundle().apply {
                    putSerializable(ARG_PHOTO_PATH, photoFile.path)
                }
                val photoDialog = BlankFragment().apply {
                    arguments = args
                }
                photoDialog.show(requireActivity().supportFragmentManager, "")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile
                )
                updateUI()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            chooseSuspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        // updateUI()
        TimePickerFragment.newInstance(crime.date.time).apply {
            setTargetFragment(this@CrimeFragment, REQUEST_TIME)
            show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
        }
    }

    override fun onTimeSelected(time: Long) {
        crime.date.time = time
        updateUI()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields =
                    arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
                var suspectId = ""
                val cursor = contactUri?.let {
                    requireActivity().contentResolver.query(
                        it,
                        queryFields,
                        null,
                        null,
                        null
                    )
                }
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    suspectId = it.getString(1)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    chooseSuspectButton.text = suspect
                }


                val cursor2 = requireActivity().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + suspectId,
                    null,
                    null
                )
                cursor2?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    PHONE =
                        it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    Log.d("TEST", "PHONE$PHONE")
                }

            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    permission
                )
            } == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), requestCode)
        }
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = R.string.crime_photo_image_description.toString()
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = R.string.crime_photo_no_image_description.toString()
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }
}