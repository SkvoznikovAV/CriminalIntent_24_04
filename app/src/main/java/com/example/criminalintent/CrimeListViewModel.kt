package com.example.criminalintent

import android.util.Log
import androidx.lifecycle.ViewModel

private val LOG_TAG = "CrimeListViewModel"
class CrimeListViewModel:ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()
}