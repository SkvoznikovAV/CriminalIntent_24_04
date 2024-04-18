package com.example.criminalintent

import android.util.Log
import androidx.lifecycle.ViewModel

private val LOG_TAG = "CrimeListViewModel"
class CrimeListViewModel:ViewModel() {
//        val crimes = mutableListOf<Crime>()
//
//        init {
//            Log.d(LOG_TAG,"CrimeListViewModel instance created")
//            for (i in 0 until 100){
//                val crime = Crime()
//                crime.title = "Crime #$i"
//                crime.isSolved = i % 2 == 0
//
//                if ((i==11) || (i==15) || (i==57)){
//                    crime.requiresPolice = true
//                }
//
//                crimes += crime
//            }
//        }

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()
}