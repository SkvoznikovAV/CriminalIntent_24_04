package com.example.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.UUID

private const val LOG_TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_PHOTO = 2

class CrimeFragment: Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var idField: TextView
    private lateinit var requiresPoliceCheckBox: CheckBox
    private lateinit var removeCrimeButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        val crimeId : UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime,container,false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        idField = view.findViewById(R.id.crime_id) as TextView
        requiresPoliceCheckBox = view.findViewById(R.id.requires_police)
        removeCrimeButton = view.findViewById(R.id.delete_crime)
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        dateButton.apply {
            text = crime.date.toString()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner) { crime ->
                crime?.let {
                    this.crime = crime
                    //Log.d(LOG_TAG,"get crime onViewCreated = ${crime.title}")
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.example.criminalintent.fileprovider",
                        photoFile)
                    updateUI()
                }
            }
    }

    private fun updateUI(){
        idField.text = crime.id.toString()
        titleField.setText(crime.title)

        val dateToString: String = SimpleDateFormat("EEEE dd MMMM yyyy HH:mm:ss", Locale.ENGLISH).format(crime.date)
        dateButton.text = dateToString
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        requiresPoliceCheckBox.apply {
            isChecked = crime.requiresPolice
            jumpDrawablesToCurrentState()
        }
        updatePhotoView()

    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object:TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                crime.title = p0.toString()
            }
            override fun afterTextChanged(p0: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.setOnCheckedChangeListener { _, isChecked -> crime.isSolved = isChecked  }
        requiresPoliceCheckBox.setOnCheckedChangeListener { _, isChecked -> crime.requiresPolice = isChecked  }

        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        removeCrimeButton.setOnClickListener {
            crimeDetailViewModel.removeCrime(crime)
            requireFragmentManager().popBackStack()
        }

        photoButton.apply {
            val packageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage,PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null){
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()

        TimePickerFragment().apply {
            setTargetFragment(this@CrimeFragment, REQUEST_TIME)
            show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {

            requestCode == REQUEST_PHOTO ->{
                requireActivity().revokeUriPermission(photoUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = crime.date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        crime.date = GregorianCalendar(year, month, day,hour,minute).time
        updateUI()
    }
}