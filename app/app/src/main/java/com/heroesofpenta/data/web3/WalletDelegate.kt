package com.heroesofpenta.data.web3

import android.util.Log // Import Log for debugging
import android.widget.Toast // Keep if needed for UI feedback
import com.heroesofpenta.data.repository.MainRepository
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.internal.common.wcKoinApp
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch // Import coroutine launch
import com.reown.foundation.common.model.Topic // Assuming Topic is here based on common structure
import java.util.concurrent.TimeUnit // For request expiry example

// Assuming these exist or create them based on the library's structure
import com.reown.sign.client.Sign // If AppKit uses SignClient internally
import com.reown.sign.client.SignClient // If AppKit uses SignClient internally
import com.squareup.moshi.Moshi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

object WalletDelegate : AppKit.ModalDelegate, CoreClient.CoreDelegate {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
  val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()
  private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
  val coreEvents: SharedFlow<Core.Model> = _coreEvents.asSharedFlow()
  private val _connectionState: MutableSharedFlow<Modal.Model.ConnectionState> = MutableSharedFlow(replay = 1)
  val connectionState: SharedFlow<Modal.Model.ConnectionState> = _connectionState.asSharedFlow()

  var selectedSessionTopic: String? = null
    private set

  private const val TAG = "WalletDelegate" // Tag for logging
  private const val CONTRACT_ADDRESS = "0x17c859A939591c293375AC23307dbe868b387c84" // Added 0x prefix
  private const val REGISTER_ACCOUNT_CODE = "64724f5e"

  // Store the address associated with the pending request if needed for context in onSessionRequestResponse
  private var pendingTransactionUserAddress: String? = null
  private var pendingTransactionRequestId: Long? = null // Optional: Store request ID for better matching
  // Get Moshi instance (assuming it's available via Koin as seen in AppKit's DI)
  private val moshi: Moshi by lazy { wcKoinApp.koin.get() }

  init {
    AppKit.setDelegate(this)
    CoreClient.setDelegate(this)
  }

  @OptIn(ExperimentalStdlibApi::class)
  override fun onSessionAuthenticateResponse(sessionAuthenticateResponse: Modal.Model.SessionAuthenticateResponse) {
    // Triggered when Dapp receives the session authenticate response from wallet
    Timber.tag(TAG).d("onSessionAuthenticateResponse: $sessionAuthenticateResponse")

    if (sessionAuthenticateResponse is Modal.Model.SessionAuthenticateResponse.Result) {
      selectedSessionTopic = sessionAuthenticateResponse.session?.topic
      if (sessionAuthenticateResponse.session != null) {
        Timber.tag(TAG).i("Authentication successful, session established.")
        val userWalletAddress = AppKit.getAccount()?.address // Or use sessionAuthenticateResponse.session.namespaces for accounts

        if (userWalletAddress != null) {
          Timber.tag(TAG).d("User address found: $userWalletAddress")
          // Use a coroutine scope if MainRepository calls are suspending or long-running
          CoroutineScope(Dispatchers.IO).launch { // Example: Use IO dispatcher for repo calls
            MainRepository.getUser(
              callback = { user ->
                if (user != null) {
                  Log.d(TAG, "User data fetched for ID: ${user.id}")


                  // --- ABI Encoding ---
                  // It's CRUCIAL that this encoding exactly matches your smart contract function signature.
                  // Be mindful of types (address, uint256, etc.) and padding.
                  // Using simple padStart might not be correct for all types (e.g., numbers).
                  // Consider using a proper ABI encoding library if available (like web3j or similar adapted for Kotlin).
                  // Example: Function register(uint256 userId)
                  // argumentsLength might represent the number of dynamic arguments, which is 0 here if userId is the only arg.
                  // Let's assume for now register(uint256 userId)
                  // The function selector is 0x64724f5e (correct length)
                  // userId needs to be encoded as uint256 (32 bytes = 64 hex chars)
                  val accountIdEncoded = try {
                    // Assuming user.id is something convertible to a number (e.g., Long)
                    // If user.id is already a hex string representing a number, parse it first
                    val numericId = user.id.toHexString().toLong(16) // Example conversion
                    numericId.toString(16).padStart(64, '0')
                  } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error encoding user ID")
                    null // Handle encoding error
                  }

                  if (accountIdEncoded == null) {
                    Timber.tag(TAG).e("Could not encode account ID, aborting transaction.")
                    // Optionally disconnect or show error
                    AppKit.disconnect({}, {})
                    return@getUser
                  }

                  // Data = function selector + encoded arguments
                  val data = "0x$REGISTER_ACCOUNT_CODE$accountIdEncoded"
                  Timber.tag(TAG).d("Encoded transaction data: $data")

//                  // *** Refined Transaction Parameters ***
//                  // Common format for eth_sendTransaction is List<Map<String, String>>
//                  // CHECK AppKit/WalletConnectKotlinV2 documentation for the exact expected format!
//                  val transactionParams = listOf(
//                    mapOf(
//                      "to" to CONTRACT_ADDRESS,
//                      "from" to userWalletAddress,
//                      "data" to data
//                      // Add "gas", "gasPrice", or "value": "0x..." if needed
//                      // "value": "0x0" // If sending 0 ETH
//                    )
//                  )
                  // --- Create the transaction Map ---
                  val transactionParamsMap = mapOf(
                    "to" to CONTRACT_ADDRESS,
                    "from" to userWalletAddress,
                    "data" to data
                    // Add "gas", "gasPrice", or "value": "0x..." if needed
                    // "value": "0x0"
                  )

                  // *** Serialize the parameters to a JSON String ***
                  // eth_sendTransaction expects a JSON array containing the transaction object.
                  val paramsList = listOf(transactionParamsMap)
                  val paramsJsonString = try {
                    // Use Moshi to serialize the list into a JSON array string
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Map::class.java, String::class.java, Any::class.java)
                    val adapter: com.squareup.moshi.JsonAdapter<List<Map<String, Any>>> = moshi.adapter(listType)
                    adapter.toJson(paramsList)
                  } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Error serializing transaction parameters")
                    null // Handle serialization error
                  }

                  if (paramsJsonString == null) {
                    Timber.tag(TAG).e("Could not serialize transaction parameters, aborting.")
                    AppKit.disconnect({}, {})
                    return@getUser
                  }

                  // Generate a unique request ID (or use library provided mechanism if available)
                  // Using timestamp + random for simplicity, a more robust solution might be needed
                  val requestId = System.currentTimeMillis() + (Math.random() * 1000).toLong()

                  // *** Request Expiry (Example using Sign v2 structure) ***
                  // Calculate expiry timestamp (e.g., 5 minutes from now)
                  // val fiveMinutesInMillis = TimeUnit.MINUTES.toMillis(5)
                  // val expiryTimestamp = (System.currentTimeMillis() + fiveMinutesInMillis) / 1000

                  // --- Create the Request object with the JSON String ---
                  val requestParams = Request(
                    // topic = sessionAuthenticateResponse.session.topic, // Usually needed if AppKit doesn't add it
                    // chainId = AppKit.getSelectedChain()?.id ?: "eip155:1", // Ensure correct chain ID
                    method = "eth_sendTransaction",
                    params = paramsJsonString // Pass the serialized JSON string
                    // id = requestId, // Pass ID if required by AppKit.request
                    // expiry = expiryTimestamp // Pass expiry if required/supported by AppKit.request
                  )

                  Timber.tag(TAG)
                    .d("Sending eth_sendTransaction request with params string: $paramsJsonString")
                  // Store context for the response handler
                  pendingTransactionUserAddress = userWalletAddress
                  pendingTransactionRequestId = requestId // Store the ID to match the response

                  AppKit.request(
                    request = requestParams,
                    onError = { err ->
                      Timber.tag(TAG)
                        .e(err, "Failed to *send* transaction request to wallet: $err")
                      pendingTransactionUserAddress = null
                      pendingTransactionRequestId = null
                      // Handle UI feedback for failure to send
                    },
                    onSuccess = { response ->
                      // This likely confirms the request was SENT, NOT approved.
                      // The 'response' object here might contain the request ID or be null/Unit.
                      Timber.tag(TAG)
                        .i("eth_sendTransaction request *sent* successfully to wallet. Response obj: $response. Waiting for user action...")
                    }
                  )
                } else {
                  Timber.tag(TAG).w("User data not found after authentication. Disconnecting.")
                  AppKit.disconnect({}, {})
                }
              },
              force = false // Assuming this parameter exists
            )
          }
        } else {
          Timber.tag(TAG).w("User address is null after authentication.")
        }
      } else {
        // Authentication successful, but no session created (SIWE-only flow?)
        Timber.tag(TAG).d("Authentication successful, SIWE flow or session is null.")
      }
    } else if (sessionAuthenticateResponse is Modal.Model.SessionAuthenticateResponse.Error) {
      Timber.tag(TAG).e(
        "${sessionAuthenticateResponse.code}: Authentication failed or rejected: ${sessionAuthenticateResponse.message}"
      )
      // Handle authentication error (e.g., user rejected connection)
    }
    scope.launch {
      _wcEventModels.emit(sessionAuthenticateResponse)
    }
  }

  override fun onSIWEAuthenticationResponse(response: Modal.Model.SIWEAuthenticateResponse) {
    println("SIWE response: $response")
  }

  override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
    selectedSessionTopic = (approvedSession as Modal.Model.ApprovedSession.WalletConnectSession).topic
    Timber.tag(TAG).d("onSessionApproved: $approvedSession") // Use topic if available directly
    // Triggered when receives the session approval from wallet (for the initial connection)
    // Often followed by onSessionAuthenticateResponse if auth is also involved.

    scope.launch {
      _wcEventModels.emit(approvedSession)
    }
  }

  override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
    Timber.tag(TAG)
      .w("onSessionRejected: ${rejectedSession.topic} Reason: ${rejectedSession.reason}")
    // Triggered when receives the session rejection from wallet
    scope.launch {
      _wcEventModels.emit(rejectedSession)
    }
  }

  override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
    Timber.tag(TAG).d("onSessionUpdate: ${updatedSession.topic}")
    // Triggered when receives the session update from wallet
    scope.launch {
      _wcEventModels.emit(updatedSession)
    }
  }

  override fun onSessionEvent(sessionEvent: Modal.Model.Event) {
    Timber.tag(TAG)
      .d("onSessionEvent: Topic: ${sessionEvent.topic}, Name: ${sessionEvent.name}, Data: ${sessionEvent.data}")
    // Handle session events like accountsChanged, chainChanged
    scope.launch {
      _wcEventModels.emit(sessionEvent)
    }
  }

  // --- Deprecated ---
  @Deprecated(
    message = "Use onSessionEvent(Modal.Model.Event) instead. Using both will result in duplicate events.",
    replaceWith = ReplaceWith("onEvent(event)", "")
  )
  override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
    Timber.tag(TAG)
      .d("onSessionEvent (DEPRECATED): Name: ${sessionEvent.name}, Data: ${sessionEvent.data}")
    scope.launch {
      _wcEventModels.emit(sessionEvent)
    }
  }

  override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
    Timber.tag(TAG).d("onSessionDelete: $deletedSession")
    // Triggered when receives the session delete from wallet
    pendingTransactionUserAddress = null // Clear context if session is deleted during transaction
    pendingTransactionRequestId = null
    deselectAccountDetails()

    scope.launch {
      _wcEventModels.emit(deletedSession)
    }
  }

  override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
    Timber.tag(TAG)
      .i("onSessionRequestResponse: ID: ${response.result.id}, Topic: ${response.topic}")

    // *** IMPORTANT: Match the response ID with the pending request ID ***
    // if (response.result.id != pendingTransactionRequestId) {
    //     Log.d(TAG, "Received response for unrelated request ID: ${response.result.id}. Ignoring.")
    //     return
    // }

    // You might also need to check response.method if the ID matching isn't perfect
    // to ensure this is the response for 'eth_sendTransaction'

    when (response.result) {
      is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
        // Successfully approved by the user in the wallet
        val txHash = (response.result as Modal.Model.JsonRpcResponse.JsonRpcResult).result // Adapt based on actual structure, might be a string
        Timber.tag(TAG).i("Transaction approved by user! TxHash: $txHash")

        // *** Logic moved here ***
        val userWalletAddress = pendingTransactionUserAddress // Use the stored address
        if (userWalletAddress != null) {
          CoroutineScope(Dispatchers.IO).launch { // Use appropriate scope
            MainRepository.registerWallet(
              walletAddress = userWalletAddress,
              callback = { success ->
                if (success) {
                  Timber.tag(TAG)
                    .i("Local wallet address updated successfully for $userWalletAddress")
                  // Optionally show success message to user or navigate
                } else {
                  Timber.tag(TAG).e("Failed to update local wallet address for $userWalletAddress")
                  // Handle local update failure (e.g., show error toast)
                }
                // Clear pending state ONLY AFTER handling the response fully
                pendingTransactionUserAddress = null
                pendingTransactionRequestId = null
              }
            )
          }
        } else {
          Timber.tag(TAG).w("Transaction approved, but pending user address context was lost.")
          // Handle this edge case - maybe fetch address again? Or just log.
          pendingTransactionRequestId = null // Still clear the request ID
        }
      }
      is Modal.Model.JsonRpcResponse.JsonRpcError -> {
        // Rejected by the user in the wallet or other error during signing/sending
        val error = response.result
        Timber.tag(TAG)
          .e("Transaction rejected or failed by wallet. Code: ${error.id}, Message: ${error.jsonrpc}")
        // Show error message to the user (e.g., "Transaction rejected.")
        // Toast.makeText(context, "Transaction rejected: ${error.message}", Toast.LENGTH_LONG).show() // Needs context

        // Clear pending state as the request is finished (with an error)
        pendingTransactionUserAddress = null
        pendingTransactionRequestId = null
      }
    }
    scope.launch {
      _wcEventModels.emit(response)
    }
  }

  override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
    Timber.tag(TAG).w("onProposalExpired: ${proposal.proposerPublicKey}")
    // Triggered when a proposal becomes expired
    scope.launch {
      _wcEventModels.emit(proposal)
    }
  }

  override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
    Timber.tag(TAG).w("onRequestExpired: ID: ${request.id}, Topic: ${request.topic}")
    // Triggered when a request becomes expired
    // Check if it matches our pending request
    if (request.id == pendingTransactionRequestId) {
      Timber.tag(TAG).e("Our pending transaction request expired!")
      // Handle UI update, maybe show a message "Transaction request timed out"
      pendingTransactionUserAddress = null
      pendingTransactionRequestId = null
    }
    scope.launch {
      _wcEventModels.emit(request)
    }
  }

  override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
    Timber.tag(TAG).d("onConnectionStateChange: Available: ${state.isAvailable}")
    // Triggered whenever the connection state is changed
    scope.launch {
      _connectionState.emit(state)
    }
  }

  override fun onError(error: Modal.Model.Error) {
    // General SDK errors
    Timber.tag(TAG).e(error.throwable.stackTraceToString())
    // Maybe show a generic error message to the user
    // Toast.makeText(context, "WalletConnect Error: ${error.message}", Toast.LENGTH_LONG).show() // Needs context

    // If an error occurs, consider clearing pending transaction state if appropriate
    // pendingTransactionUserAddress = null
    // pendingTransactionRequestId = null
    scope.launch {
      _wcEventModels.emit(error)
    }
  }

  // --- Methods likely not directly involved in the eth_sendTransaction flow but good to log ---
  override fun onSessionExtend(session: Modal.Model.Session) {
    Timber.tag(TAG).d("onSessionExtend: ${session.topic}")
    scope.launch {
      _wcEventModels.emit(session)
    }
  }

  @Deprecated("onPairingDelete callback has been deprecated. It will be removed soon. Pairing will disconnect automatically internally.")
  override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
    //Deprecated - pairings are automatically deleted
  }

  @Deprecated("onPairingExpired callback has been deprecated. It will be removed soon. Pairing will disconnect automatically internally.")
  override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
    //Deprecated - pairings are automatically expired
  }

  override fun onPairingState(pairingState: Core.Model.PairingState) {
    Timber.tag(TAG).d("Dapp pairing state: ${pairingState.isPairingState}")
  }

  fun deselectAccountDetails() {
    selectedSessionTopic = null
  }
}