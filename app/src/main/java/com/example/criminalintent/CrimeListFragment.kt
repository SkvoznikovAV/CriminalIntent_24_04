package com.example.criminalintent

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

private val LOG_TAG="CrimeListFragment"
class CrimeListFragment: Fragment() {
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(LOG_TAG,"instance CrimeListFragment $this")
    }

    private inner class CrimeHolder(view: View):RecyclerView.ViewHolder(view),View.OnClickListener{
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView? = itemView.findViewById(R.id.crime_solved_image_view)

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title

            val dateFormated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("EEEE dd MMMM yyyy hh:mm:ss").format(this.crime.date)
            } else {
                this.crime.date.toString()
            }

            dateTextView.text = dateFormated
            solvedImageView?.visibility = if (this.crime.isSolved) View.VISIBLE else View.GONE

        }

        override fun onClick(p0: View?) {
            Toast.makeText(context,"${crime.title} pressed",Toast.LENGTH_SHORT).show()
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    private inner class CrimeAdapter(val crimes: List<Crime>):RecyclerView.Adapter<CrimeHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val itemLayoutId= when (viewType){
                1 -> R.layout.list_item_crime_require_police
                else -> R.layout.list_item_crime
            }

            val view = layoutInflater.inflate(itemLayoutId,parent,false)
            return CrimeHolder(view)
        }

        override fun getItemCount(): Int {
            return crimes.size
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

        override fun getItemViewType(position: Int): Int {
            var viewType = 0
            if (crimes[position].requiresPolice) viewType = 1

            return viewType
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list,container,false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        val crimes = crimeListViewModel.crimes
        Log.d(LOG_TAG,"get crimeListViewModel ${crimeListViewModel}")
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter

        return view
    }

    companion object {
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }
}