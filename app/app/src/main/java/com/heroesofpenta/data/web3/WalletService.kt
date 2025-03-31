package com.heroesofpenta.data.web3


//import com.heroesofpenta.MainActivity
import android.net.Uri
import androidx.core.net.toUri
import com.heroesofpenta.data.repository.MainRepository
import com.reown.android.CoreClient
//import com.reown.android.Core
//import com.reown.android.CoreClient
//import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.Session
import com.reown.appkit.client.models.request.Request
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import timber.log.Timber
import java.math.BigInteger
import java.util.UUID

object WalletService {
  private const val CONTRACT_ADDRESS = "17c859A939591c293375AC23307dbe868b387c84"
  private const val REGISTER_ACCOUNT_CODE = "64724f5e"
  fun init() {
    val authPayloadParams = Modal.Model.AuthPayloadParams(
      chains = listOf("eip155:534351", "eip155:534352"),
      domain = "heroesofpenta.com",
      nonce =  UUID.randomUUID().toString(),//uniqueNonce,
      uri = "https://heroesofpenta.com/login",
      statement = "I confirm that I want to link my wallet address with my Heroes of Penta and TikTok accounts. I accept the General Terms of Service as stated at https://heroesofpenta.com/terms",
      methods = listOf("personal_sign", "eth_sendTransaction"),
      resources = null //// Here your dapp may request authorization with ReCaps
    )
    AppKit.setAuthRequestParams(authPayloadParams)
    AppKit.setDelegate(WalletDelegate)
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun assign(userId: UInt, callback: (error: Throwable?) -> Unit) {
    val userWalletAddress = AppKit.getAccount()?.address
      ?: return callback(IllegalStateException("no connected wallet"))

    val argumentsLength = "1".padStart(length = 64, padChar = '0')
    val accountId = userId.toHexString().padStart(length = 64, padChar = '0')
    val data = "0x$REGISTER_ACCOUNT_CODE$argumentsLength$accountId"
    val function = org.web3j.abi.datatypes.Function(
      "registerAccount",
      listOf(
        Address(userWalletAddress),
        Address("0x$CONTRACT_ADDRESS"),
//        Address("0x$REGISTER_ACCOUNT_CODE"),
//        Uint256(1),
        Uint256(userId.toLong())

//        Address(userWalletAddress),
//        Address("0x$CONTRACT_ADDRESS"),
//        Uint256(BigInteger(tokenId)),
      ),  // input parameters. Change this based on the method you're using
      listOf(object : TypeReference<Utf8String>() {}) // output parameters. Change this based on the method you're using,
    )

    val encodedFunction = FunctionEncoder.encode(function)
    val requestParams = Request(
      method = "eth_sendTransaction", // 0x17c859A939591c293375AC23307dbe868b387c84
      params = "{\"to\":\"0x$CONTRACT_ADDRESS\",\"from\":\"$userWalletAddress\",\"data\":\"$encodedFunction\"}"
    )
//    AppKit.getSession()?.performMethodCall
//    MainApplication.session?.performMethodCall(
//      Session.MethodCall.SendTransaction(
//        txRequest,
//        from = viewModel.address.value,
//        to = contractAddress,
//        nonce = nonce.transactionCount.toString(16),
//        gasPrice = DefaultGasProvider.GAS_PRICE.toString(16),
//        gasLimit = DefaultGasProvider.GAS_LIMIT.toString(16),
//        value = BigInteger.ZERO.toString(16),
//        data = endcodedFunction
//      )
//    ) { resp ->
//      // do something with callback
//    }
//    val i = Intent(Intent.ACTION_VIEW)
//    i.data = Uri.parse("wc:")
//    context.startActivity(i)

    AppKit.request(
      request = requestParams,
      onError = { err -> throw err },
      onSuccess = { ->
        MainRepository.registerWallet(
          walletAddress = userWalletAddress,
          callback = { success ->
            if (!success) {
              callback(RuntimeException("Failed to update local wallet address"))
            } else {
              callback(null)
            }
          }
        )
      }
    )
  }

  fun connect(onSuccess: (url: String) -> Unit, onError: (err: Modal.Model.Error) -> Unit) {
    val authOpts = Modal.Params.Authenticate(
      chains = listOf("eip155:534351", "eip155:534352"),
      domain = "heroesofpenta.com",
      nonce =  UUID.randomUUID().toString(),
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

  fun disconnect(onSuccess: (url: String) -> Unit, onError: (err: Modal.Model.Error) -> Unit) {
    val authOpts = Modal.Params.Authenticate(
      chains = listOf("eip155:534351", "eip155:534352"),
      domain = "heroesofpenta.com",
      nonce = UUID.randomUUID().toString(),
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