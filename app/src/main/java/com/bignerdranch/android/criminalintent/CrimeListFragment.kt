package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

private var callbacks: CrimeListFragment.Callbacks? = null

class CrimeListFragment : Fragment() {
    private val crimeListViewModel by lazy { ViewModelProvider(this).get(CrimeListViewModel::class.java) }

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }


    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter()

    private class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val isSolvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            val data = DateFormat.format("EEEE, MMM dd, yyyy", this.crime.date)
            dateTextView.text = data
            isSolvedImageView.visibility = if (this.crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(p0: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private class CrimeAdapter :
        ListAdapter<Crime, CrimeHolder>(CrimesDiffCallback()) {

        private class CrimesDiffCallback : DiffUtil.ItemCallback<Crime>() {
            override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem == newItem
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = currentList[position]
            holder.bind(crime)
        }

        override fun getItemCount(): Int {
            return currentList.size
        }

    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner
        ) { crimes ->
            crimes?.let {
                Log.i(TAG, "Got crimes ${crimes.size}")
                updateUI(crimes)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter?.submitList(crimes)
    }
}