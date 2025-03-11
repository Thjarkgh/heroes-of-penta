package com.heroesofpenta.data.web3


import com.heroesofpenta.MainActivity
import com.heroesofpenta.data.repository.MainRepository
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object WalletService {
  @OptIn(ExperimentalUuidApi::class)
  fun init() {
    val authPayloadParams = Modal.Model.AuthPayloadParams(
      chains = listOf("eip155:1", "eip155:137"),
      domain = "heroesofpenta.com",
      nonce = Uuid.random().toString(),//uniqueNonce,
      uri = "https://heroesofpenta.com/login",
      statement = "I confirm that I want to link my wallet address with my Heroes of Penta and TikTok accounts. I accept the General Terms of Service as stated at https://heroesofpenta.com/terms",
      methods = listOf("personal_sign", "eth_sendTransaction"),
      resources = null //// Here your dapp may request authorization with ReCaps
    )
    AppKit.setAuthRequestParams(authPayloadParams)
    AppKit.setDelegate(WalletDelegate)
  }

  @OptIn(ExperimentalUuidApi::class)
  fun connect(onSuccess: (url: String) -> Unit, onError: (err: Modal.Model.Error) -> Unit) {
    val authOpts = Modal.Params.Authenticate(
      chains = listOf("eip155:534351", "eip155:534352"),
      domain = "heroesofpenta.com",
      nonce = Uuid.random().toString(),
      uri = "https://heroesofpenta.com/login",
      statement = "I confirm that I want to link my wallet address with my Heroes of Penta and TikTok accounts. I accept the General Terms of Service as stated at https://heroesofpenta.com/terms",
      methods = listOf("personal_sign", "eth_sendTransaction")
    )
    AppKit.authenticate(
      authenticate = authOpts,
      onSuccess = { success ->
        val wallet = AppKit.getAccount()?.address
        if (wallet == null) {
          onError(Modal.Model.Error(Exception("authenticated, but wallet is null")))
        } else {
          MainRepository.registerWallet(
            walletAddress = wallet,
            callback = { registered ->
              if (registered) {
                onSuccess(success)
              } else {
                onError(Modal.Model.Error(Exception("failed to register authenticated wallet")))
              }
            }
          )
        }
      },
      onError = { err ->
        onError(err)
      }
    )
  }

  @OptIn(ExperimentalUuidApi::class)
  fun disconnect(onSuccess: (url: String) -> Unit, onError: (err: Modal.Model.Error) -> Unit) {
    val authOpts = Modal.Params.Authenticate(
      chains = listOf("eip155:534351", "eip155:534352"),
      domain = "heroesofpenta.com",
      nonce = Uuid.random().toString(),
      uri = "https://heroesofpenta.com/login",
      statement = "I confirm that I want to disconnect my wallet address from my Heroes of Penta and TikTok accounts.",
      methods = listOf("personal_sign", "eth_sendTransaction")
    )
    AppKit.authenticate(
      authenticate = authOpts,
      onSuccess = { url ->
        MainRepository.disconnectWallet { success ->
          if (success) {
            onSuccess(url)
          } else {
            onError(Modal.Model.Error(Exception("failed to disconnect authenticated wallet")))
          }
        }
      },
      onError = { err ->
        onError(err)
      }
    )
  }
}