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
import timber.log.Timber

class MyApp : Application() {
  private var ready: Boolean = false

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }

    //WalletService.init()
    RetrofitClient.init(applicationContext)
    val projectId = BuildConfig.reownProjectId // Get Project ID at https://cloud.reown.com/
    val connectionType = ConnectionType.AUTOMATIC //or ConnectionType.MANUAL
    val telemetryEnabled = true // not sure yet
    val appMetaData = Core.Model.AppMetaData(
      name = "Heroes of Penta",
      description = "Heroes of Penta",
      url = "https://heroesofpenta.com",
      icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media")/*list of icon url strings*/,
      redirect = "kotlin-dapp-wc://request", //"kotlin-wallet-wc:/request", // Custom Redirect URI
      appLink = "https://heroesofpenta.com/mobileapp",
      linkMode = true
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
      init = Modal.Params.Init(core = CoreClient),
      onSuccess = {
        // Callback will be called if initialization is successful
        ready = true
        // not sure WalletService.init()
      },
      onError = { error ->
        // Error will be thrown if there's an issue during initialization
        Timber.e(tag(this), error.throwable.stackTraceToString())
        // throw InstantiationException(error.toString())
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
    //AppKit.setChains(AppKitChainsPresets.ethChains.values.toList())
    WalletService.init()

//        val authParams = Modal.Model.AuthPayloadParams(
//            chains = AppKitChainsPresets.ethChains.values.toList().map { it.id },
//            domain = "sample.kotlin.modal",
//            uri = "https://web3inbox.com/all-apps",
//            nonce = randomBytes(12).bytesToHex(),
//            statement = "I accept the Terms of Service: https://yourDappDomain.com/",
//            methods = EthUtils.ethMethods
//        )
//        AppKit.setAuthRequestParams(authParams)

  }

  private inline fun <reified T : Any> tag(currentClass: T): String {
    return ("Wallet" + currentClass::class.java.canonicalName!!.substringAfterLast(".")).take(23)
  }
}