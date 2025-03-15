package com.heroesofpenta.data.web3

import android.widget.Toast
import com.heroesofpenta.data.repository.MainRepository
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import kotlinx.coroutines.currentCoroutineContext

object WalletDelegate : AppKit.ModalDelegate {
  private const val CONTRACT_ADDRESS = "17c859A939591c293375AC23307dbe868b387c84"
  private const val REGISTER_ACCOUNT_CODE = "64724f5e"

  @OptIn(ExperimentalStdlibApi::class)
  override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Modal.Model.SessionAuthenticateResponse) {
    // Triggered when Dapp receives the session authenticate response from wallet

    if (sessionAuthenticateResponse is Modal.Model.SessionAuthenticateResponse.Result) {
      if (sessionAuthenticateResponse.session != null) {
        // Authentication successful, session established
        val userWalletAddress = AppKit.getAccount()?.address

        if (userWalletAddress != null) {
          MainRepository.getUser(
            { user ->
              if (user != null) {
                val argumentsLength = "1".padStart(length = 64, padChar = '0')
                val accountId = user.id.toHexString().padStart(length = 64, padChar = '0')
                val data = "0x$REGISTER_ACCOUNT_CODE$argumentsLength$accountId"
                val requestParams = Request(
                  method = "eth_sendTransaction", // 0x17c859A939591c293375AC23307dbe868b387c84
                  params = "{\"to\":\"0x$CONTRACT_ADDRESS\",\"from\":\"$userWalletAddress\",\"data\":\"$data\"}"
                )

                AppKit.request(
                  request = requestParams,
                  onError = { err -> throw err },
                  onSuccess = { ->
                    MainRepository.registerWallet(
                      walletAddress = userWalletAddress,
                      callback = { success ->
                        if (!success) {
                          throw RuntimeException("Failed to update local wallet address")
                        }
                      }
                    )
                  }
                )
              } else {
                AppKit.disconnect({}, {})
              }
            },
            false
          )
        }
      } else {
        // Authentication successful, but no session created (SIWE-only flow)
      }
    } else {
      // Authentication request was rejected or failed
    }
  }

  override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
    // Triggered when receives the session approval from wallet
  }

  override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
    // Triggered when receives the session rejection from wallet
  }

  override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
    // Triggered when receives the session update from wallet
  }

  override fun onSessionExtend(session: Modal.Model.Session) {
    // Triggered when receives the session extend from wallet
  }

  @Deprecated(
    message = "Use onSessionEvent(Modal. Model. Event) instead. Using both will result in duplicate events.",
    replaceWith = ReplaceWith("onEvent(event)", "")
  )
  override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
    // Triggered when the peer emits events that match the list of events agreed upon session settlement
  }

  override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
    // Triggered when receives the session delete from wallet
  }

  override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
    // Triggered when receives the session request response from wallet
  }

  override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
    // Triggered when a proposal becomes expired
  }

  override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
    // Triggered when a request becomes expired
  }

  override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
    //Triggered whenever the connection state is changed
  }

  override fun onError(error: Modal.Model.Error) {
    //throw RuntimeException(error.toString())
  }
}