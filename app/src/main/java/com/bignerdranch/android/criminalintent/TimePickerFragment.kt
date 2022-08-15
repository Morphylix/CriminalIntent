package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {
    interface Callbacks {
        fun onTimeSelected(time: Long)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = arguments?.getLong(ARG_TIME) ?: 0
        val hours = (time / 3600).toInt() / 1000
        val minutes = (time % 3600 / 60).toInt() / 1000
        val timeListener = TimePickerDialog.OnTimeSetListener { _, currentHours, currentMins ->
            val currentTime = (currentHours * 3600 + currentMins * 60).toLong() * 1000 + time

            targetFragment.let { fragment ->
                (fragment as Callbacks).onTimeSelected(currentTime)
            }
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            hours,
            minutes,
            true
        )
    }

    companion object {
        fun newInstance(time: Long): TimePickerFragment {
            val args = Bundle().apply {
                putLong(ARG_TIME, time)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}