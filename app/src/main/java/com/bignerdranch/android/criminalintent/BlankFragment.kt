package com.bignerdranch.android.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

class BlankFragment : DialogFragment() {

    private lateinit var image: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_blank)
        image = dialog.findViewById(R.id.full_size_photo)
        val crimePhotoFileName = arguments?.getString(ARG_PHOTO_PATH)
        Log.d("TEST", "Crime id = $crimePhotoFileName")
        if (crimePhotoFileName != null) {
            val bitmap = BitmapFactory.decodeFile(crimePhotoFileName)
            image.setImageBitmap(bitmap)
        }
        return dialog
        // return AlertDialog.Builder(requireContext()).setMessage(R.string.app_name).create()
    }
}