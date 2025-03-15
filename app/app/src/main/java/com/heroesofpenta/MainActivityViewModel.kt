package com.heroesofpenta
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.heroesofpenta.data.web3.WalletDelegate
//import com.reown.appkit.client.Modal
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.merge
//import kotlinx.coroutines.flow.shareIn
//
//class DappSampleViewModel : ViewModel() {
//  val events = merge(WalletDelegate.wcEventModels, DappDelegate.connectionState)
//    .map { event ->
//      when (event) {
//        is Modal.Model.ConnectionState -> DappSampleEvents.ConnectionEvent(event.isAvailable)
//        is Modal.Model.DeletedSession -> DappSampleEvents.Disconnect
//        is Modal.Model.Session -> DappSampleEvents.SessionExtend
//        is Modal.Model.Error -> DappSampleEvents.RequestError(event.throwable.localizedMessage ?: "Something goes wrong")
//        else -> DappSampleEvents.NoAction
//      }
//    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
//}