package com.example.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.UUID

class CrimeDetailViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get()

    //private lateinit var crimeId : UUID
    //lateinit var crimeLiveData: LiveData<Crime?>

    private val crimeIdLiveData = MutableLiveData<UUID>()

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime: Crime){
        crimeRepository.updateCrime(crime)
    }

    fun removeCrime(crime: Crime){
        crimeRepository.removeCrime(crime)
    }

    /*fun loadCrime(crimeId: UUID){
        this.crimeId = crimeId
        crimeLiveData = crimeRepository.getCrime(crimeId)
    }*/

}