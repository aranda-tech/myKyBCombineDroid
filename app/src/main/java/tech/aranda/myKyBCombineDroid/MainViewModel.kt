package tech.aranda.myKyBCombineDroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import tech.aranda.myKyBCombineDroid.ble.BLEManager

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val ble = BLEManager(application)

    override fun onCleared() {
        super.onCleared()
        ble.disconnect()
    }
}
