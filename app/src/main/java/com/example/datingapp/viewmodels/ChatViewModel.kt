package com.example.datingapp.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.datingapp.models.Message
import com.example.datingapp.models.MessageType
import com.example.datingapp.models.LocationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    private val _messageStatus = MutableStateFlow<MessageStatus>(MessageStatus.SENT)
    val messageStatus: StateFlow<MessageStatus> = _messageStatus

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val currentUserId: StateFlow<String> = _currentUserId

    private val fusedLocationClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(auth.app.applicationContext)

    init {
        checkPremiumStatus()
    }

    private fun checkPremiumStatus() {
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { document ->
                _isPremium.value = document.getBoolean("isPremium") ?: false
            }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            try {
                _messageStatus.value = MessageStatus.SENT
                
                firestore.collection("messages")
                    .add(message)
                    .addOnSuccessListener { document ->
                        _messageStatus.value = MessageStatus.DELIVERED
                    }
                    .addOnFailureListener { exception ->
                        _messageStatus.value = MessageStatus.FAILED
                    }
            } catch (e: Exception) {
                _messageStatus.value = MessageStatus.FAILED
            }
        }
    }

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (!isLocationPermissionGranted()) {
            callback(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                callback(location)
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val context = auth.app.applicationContext
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getAddressFromLocation(location: Location): String {
        val geocoder = Geocoder(auth.app.applicationContext)
        try {
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            return addresses?.firstOrNull()?.getAddressLine(0) ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
