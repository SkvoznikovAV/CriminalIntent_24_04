package com.example.criminalintent

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import java.util.UUID

private const val LOG_TAG="CrimeListFragment"
class CrimeListFragment: Fragment() {
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyListTextView: TextView
    private var adapter: CrimeAdapter = CrimeAdapter()
    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d(LOG_TAG,"instance CrimeListFragment $this")
    }

    private inner class CrimeHolder(view: View):RecyclerView.ViewHolder(view),View.OnClickListener{
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView? = itemView.findViewById(R.id.crime_solved_image_view)

        fun bind(crime: Crime){
            this.crime = crime

            var showTitle = this.crime.title
            if (showTitle=="") showTitle = "Без описания"
            titleTextView.text = showTitle

            val dateToString = SimpleDateFormat("EEEE dd MMMM yyyy HH:mm:ss", Locale.ENGLISH).format(this.crime.date)

            dateTextView.text = dateToString
            solvedImageView?.visibility = if (this.crime.isSolved) View.VISIBLE else View.GONE
        }

        override fun onClick(p0: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    //private inner class CrimeAdapter(val crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>(){
    private inner class CrimeAdapter : ListAdapter<Crime, CrimeHolder>(CrimeDiffCallback){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val itemLayoutId = when (viewType){
                1 -> R.layout.list_item_crime_require_police
                else -> R.layout.list_item_crime
            }

            val view = layoutInflater.inflate(itemLayoutId,parent,false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = getItem(position)
            holder.bind(crime)
        }

        override fun getItemViewType(position: Int): Int {
            var viewType = 0
            if (getItem(position).requiresPolice) viewType = 1

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
        crimeRecyclerView.adapter = adapter
        emptyListTextView = view.findViewById(R.id.empty_list_text_view) as TextView


        //Log.d(LOG_TAG,"get crimeListViewModel $crimeListViewModel")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val obs = Observer<List<Crime>> { crimes ->
            crimes?.let{
                updateUI(it)
            }}

        val obs2 = Observer { crimes:List<Crime> ->
            crimes?.let{
                updateUI(crimes)
            }
        }

        val obs3: Observer<List<Crime>> = object : Observer<List<Crime>> {
            override fun onChanged(crimes: List<Crime>?) {
                crimes?.let{
                    updateUI(crimes)
                }
            }

        }*/
        Log.d(LOG_TAG,"CrimeListFragment onViewCreated")
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner){ crimes ->
            crimes?.let {
                updateUI(crimes)
            }
        }
        //crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner,obs3)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG,"CrimeListFragment onDestroy")
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter.submitList(crimes as MutableList<Crime>)

        if (crimes.isEmpty()) {
            emptyListTextView.visibility = View.VISIBLE
        } else {
            emptyListTextView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.new_crime ->{
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }
}
object CrimeDiffCallback : DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem.id == newItem.id
    }
}