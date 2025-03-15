package com.heroesofpenta

import android.app.Application
import com.heroesofpenta.data.network.RetrofitClient
import com.heroesofpenta.data.web3.WalletService
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets
import com.reown.appkit.presets.AppKitChainsPresets.ethToken
import com.reown.appkit.utils.EthUtils

class MyApp : Application() {
  private var ready: Boolean = false

  override fun onCreate() {
    super.onCreate()

    WalletService.init()
    RetrofitClient.init(applicationContext)
    val projectId = BuildConfig.reownProjectId // Get Project ID at https://cloud.reown.com/
    val connectionType = ConnectionType.AUTOMATIC //or ConnectionType.MANUAL
    val telemetryEnabled: Boolean = true // not sure yet
    val appMetaData = Core.Model.AppMetaData(
      name = "Heroes of Penta",
      description = "Heroes of Penta",
      url = "https://heroesofpenta.com",
      icons = listOf()/*list of icon url strings*/,
      redirect = "kotlin-wallet-wc:/request" // Custom Redirect URI
    )

    CoreClient.initialize(
      application = this,
      projectId = projectId,
      metaData = appMetaData,
      connectionType = connectionType,
      telemetryEnabled = telemetryEnabled,
      onError = { error -> throw InstantiationException(error.toString()) }
    )
    //projectId = projectId, connectionType = connectionType, application = this, metaData = appMetaData, telemetryEnabled = telemetryEnabled)

    AppKit.initialize(
      init = Modal.Params.Init(CoreClient),
      onSuccess = {
        // Callback will be called if initialization is successful
        ready = true
      },
      onError = { error ->
        // Error will be thrown if there's an issue during initialization
        throw InstantiationException(error.toString())
      }
    )
    AppKit.setChains(listOf(Modal.Model.Chain(
      chainName = "Scroll Sepolia",
      chainNamespace = "eip155",
      chainReference = "534351",
      requiredMethods = EthUtils.ethRequiredMethods,
      optionalMethods = EthUtils.ethOptionalMethods,
      events = EthUtils.ethEvents,
      token = ethToken,
      rpcUrl = "https://sepolia-rpc.scroll.io",
      blockExplorerUrl = "https://sepolia.scrollscan.com"
    )))
    AppKit.disconnect({},{})
  }
}