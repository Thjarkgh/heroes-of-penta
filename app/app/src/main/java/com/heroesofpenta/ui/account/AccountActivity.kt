package com.heroesofpenta.ui.account
//import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
//import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.heroesofpenta.data.models.User
import com.heroesofpenta.data.repository.MainRepository
import com.heroesofpenta.data.web3.WalletService
import com.reown.appkit.ui.components.button.ConnectButton
import com.reown.appkit.ui.components.button.ConnectButtonSize
import com.reown.appkit.ui.components.button.rememberAppKitState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heroesofpenta.ui.MyEvents
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.heroesofpenta.data.web3.WalletDelegate
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import com.reown.appkit.client.models.request.SentRequestResult
import com.reown.appkit.ui.AppKitTheme
import com.reown.appkit.ui.components.button.AccountButtonType
import com.reown.appkit.ui.components.button.NetworkButton
import com.reown.appkit.ui.components.button.Web3Button
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

@Composable
fun AccountScreen(navController: NavHostController) {
  val context = LocalContext.current
//  val viewModel: AccountViewModel = viewModel()
//  val state by viewModel.uiState.collectAsState()
//  val awaitResponse by viewModel.awaitResponse.collectAsState(false)
  val showDialog = remember { mutableStateOf(false) }
  val dialogMessage = remember { mutableStateOf("") }
  val errorMessage = remember { mutableStateOf("") }

  val web3ModalState = rememberAppKitState(navController = navController)
  val isConnected by web3ModalState.isConnected.collectAsState()

  val coroutineScope = rememberCoroutineScope()
//  val context = LocalContext.current

  LaunchedEffect(Unit) {
    WalletDelegate.wcEventModels.collect { event ->
      when (event) {
        is Modal.Model.SessionRequestResponse -> {
          when (event.result) {
            is Modal.Model.JsonRpcResponse.JsonRpcError -> {
              val error = event.result as Modal.Model.JsonRpcResponse.JsonRpcError
              Toast.makeText(context, "Error Message: ${error.message}\n Error Code: ${error.code}", Toast.LENGTH_SHORT).show()
            }

            is Modal.Model.JsonRpcResponse.JsonRpcResult -> Toast.makeText(context, (event.result as Modal.Model.JsonRpcResponse.JsonRpcResult).result, Toast.LENGTH_SHORT).show()
          }
        }

        is Modal.Model.Error -> Toast.makeText(context, event.throwable.localizedMessage ?: "Something went wrong", Toast.LENGTH_SHORT).show()

        else -> Unit
      }
    }
  }
//
//  AppKitTheme(
//    mode = AppKitTheme.Mode.AUTO
//  ) {
//    LazyColumn(
//      modifier = Modifier.fillMaxSize(),
//      verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
//      horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//      item { Web3Button(state = web3ModalState, accountButtonType = AccountButtonType.MIXED) }
//      item { NetworkButton(state = web3ModalState) }
//      if (isConnected) {
//        AppKit.getAccount()?.let { session ->
//          val account = session.address
//          val onError: (Throwable) -> Unit = {
//            coroutineScope.launch(Dispatchers.Main) {
//              Toast.makeText(context, it.localizedMessage ?: "Error trying to send request", Toast.LENGTH_SHORT).show()
//            }
//          }
//          //item { BlueButton(text = "Personal sign", onClick = { sendPersonalSignRequest(account, {}, onError) }) }
//          item { BlueButton(text = "Eth send transaction", onClick = { sendEthSendTransactionRequest(account, {}, onError) }) }
//          //item { BlueButton(text = "Eth sign typed data", onClick = { sendEthSignTypedDataRequest(account, {}, onError) }) }
//        }
//      }
//    }
//  }
//}
//
////private fun sendPersonalSignRequest(
////  account: String,
////  onSuccess: (SentRequestResult) -> Unit,
////  onError: (Throwable) -> Unit
////) {
////  AppKit.request(
////    request = Request("personal_sign", getPersonalSignBody(account)),
////    onSuccess = onSuccess,
////    onError = onError,
////  )
////}
//
//private fun sendEthSendTransactionRequest(
//  account: String,
//  onSuccess: (SentRequestResult) -> Unit,
//  onError: (Throwable) -> Unit
//) {
//  val params = "[{\"to\":\"0x17c859A939591c293375AC23307dbe868b387c84\",\"from\":\"$account\",\"gas\":\"0x118020\",\"gasPrice\":\"0x0392093790\",\"nonce\":\"0x07\",\"value\":\"0\",\"data\":\"0x64724f5e0000000000000000000000000000000000000000000000000000000000a9ad87\"}]" //"{\"to\":\"0x$CONTRACT_ADDRESS\",\"from\":\"$userWalletAddress\",\"data\":\"$encodedFunction\"}"
//
//  AppKit.request(
//    request = Request("eth_sendTransaction", params),
//    onSuccess = onSuccess,
//    onError = onError,
//  )
//}//////////////////////////////
  // We'll load the user asynchronously. If null, show a loading indicator or text
  var user by remember { mutableStateOf<User?>(null) }

  LaunchedEffect(Unit) {
    MainRepository.getUser(
      callback = { fetchedUser -> user = fetchedUser },
      force = false
    )

//    viewModel.fetchAccountDetails()
//
//    viewModel.events.collect { event ->
//      when (event) {
//        is MyEvents.RequestSuccess -> {
//          dialogMessage.value = "Result: ${event.result}"
//          showDialog.value = true
//        }
//
//        is MyEvents.RequestPeerError -> {
//          errorMessage.value = "Error: ${event.errorMsg}"
//          showDialog.value = true
//        }
//
//        is MyEvents.RequestError -> {
//          errorMessage.value = "Error: ${event.exceptionMsg}"
//          showDialog.value = true
//        }
//
//        is MyEvents.Disconnect -> {
//          errorMessage.value = "Error: lost connection"
//          showDialog.value = true
//        }
////        is MyEvents.Disconnect -> navController.navigate(Route.ChainSelection.path) {
////          popUpTo(navController.graph.startDestinationId) {
////            inclusive = true
////          }
////        }
//
//        else -> Unit
//      }
//    }
  }

  if (showDialog.value) {
    SimpleResultDialog(
      message = dialogMessage.value,
      error = errorMessage.value,
      onClose = { showDialog.value = false }
    )
  }

  if (user == null) {
    // Simple placeholder until the user data is loaded
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "Loading user info...")
    }
  } else {
    // Show the actual account UI once we have the user
    AccountScreenUI(
      user = user!!,
      context = context,
      navController = navController
    )

//    AccountScreen(
//      state = state,
//      onMethodClick = viewModel::requestMethod,
//      awaitResponse = awaitResponse
//    )
  }
}

//@Composable
//private fun AccountScreen(
//  state: AccountUi,
//  onMethodClick: (String, (Uri) -> Unit) -> Unit,
//  awaitResponse: Boolean,
//) {
//  when (state) {
//    AccountUi.Loading -> FullScreenLoader()
//    is AccountUi.AccountData -> AccountContent(state, onMethodClick, awaitResponse)
//  }
//}

//@Composable
//fun AccountContent(
//  state: AccountUi.AccountData,
//  onMethodClick: (String, (Uri) -> Unit) -> Unit,
//  awaitResponse: Boolean,
//) {
//  Box {
//    Column(
//      modifier = Modifier
//        .fillMaxSize()
//        .padding(20.dp)
//    ) {
//      ChainData(chain = state)
//      Spacer(modifier = Modifier.height(6.dp))
//      MethodList(
//        methods = state.listOfMethods,
//        onMethodClick = onMethodClick
//      )
//    }
//
//    if (awaitResponse) {
//      Loader()
//    }
//  }
//}
//@Composable
//private fun ChainData(chain: AccountUi.AccountData) {
//  Column(
//    modifier = Modifier
//      .clickable { }
//      .fillMaxWidth()
//      .padding(horizontal = 24.dp, 16.dp)
//  ) {
//    Row(verticalAlignment = Alignment.CenterVertically) {
//      //AsyncImage(model = chain.icon, contentDescription = null, Modifier.size(48.dp))
//      Spacer(modifier = Modifier.width(8.dp))
//      Text(
//        text = chain.chainName,
//        style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
//      )
//    }
//    Spacer(modifier = Modifier.height(4.dp))
//    Text(
//      text = chain.account ?: "0x00",
//      style = TextStyle(fontSize = 12.sp),
//      maxLines = 1,
//      modifier = Modifier.padding(horizontal = 6.dp),
//      overflow = TextOverflow.Ellipsis,
//    )
//  }
//}

//@Composable
//private fun MethodList(
//  methods: List<String>,
//  onMethodClick: (String, (Uri) -> Unit) -> Unit,
//) {
//  val context = LocalContext.current
//  LazyColumn(modifier = Modifier.fillMaxWidth()) {
//    itemsIndexed(methods) { _, item ->
//      BlueButton(
//        text = item,
//        onClick = {
//          onMethodClick(item) { uri ->
//            try {
//              context.startActivity(Intent(Intent.ACTION_VIEW, uri))
//            } catch (e: Exception) {
//              Timber.tag("AccountRoute").d("Activity not found: $e")
//            }
//          }
//        },
//        modifier = Modifier.fillMaxWidth(),
//      )
//    }
//  }
//}

@Composable
fun BlueButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val contentColor = Color(0xFFE5E7E7)
  Button(
    shape = RoundedCornerShape(12.dp),
    modifier = modifier,
    colors = ButtonDefaults.buttonColors(
      backgroundColor = Color(0xFF3496ff),
      contentColor = contentColor
    ),
    onClick = {
      onClick()
    },
  ) {
    Text(text = text, color = contentColor)
  }
}

//@Composable
//fun FullScreenLoader(
//  modifier: Modifier = Modifier,
//  color: Color = Color(0xFF3496ff),
//  backgroundColor: Color = MaterialTheme.colors.background.copy(alpha = .5f)
//) {
//  Box(
//    modifier = Modifier
//      .fillMaxSize()
//      .background(backgroundColor)
//  ) {
//    CircularProgressIndicator(
//      color = color,
//      modifier = modifier
//    )
//  }
//}

@Composable
fun BoxScope.Loader() {
  Column(
    modifier = Modifier
      .align(Alignment.Center)
      .clip(RoundedCornerShape(34.dp))
      .background(themedColor(Color(0xFF242425).copy(alpha = .95f), Color(0xFFF2F2F7).copy(alpha = .95f)))
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    CircularProgressIndicator(
      strokeWidth = 8.dp,
      modifier = Modifier
        .size(75.dp), color = Color(0xFFB8F53D)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "Awaiting response...",
      maxLines = 1,
      style = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        color = themedColor(Color(0xFFb9b3b5), Color(0xFF484648))
      ),
    )
  }
}

@Composable
fun themedColor(darkColor: Color, lightColor: Color): Color =
  if (isSystemInDarkTheme()) darkColor else lightColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AccountScreenUI(
  user: User,
  context: Context,
  navController: NavHostController
) {

  val scope = rememberCoroutineScope()
  // If the ReOwn library requires a BottomSheet state:
  val modalSheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = true
  )

  // Create or obtain the AppKitState from the library, passing in your navController
  val appKitState = rememberAppKitState(navController = navController)

  // Track username changes in a local state so the user can edit the text field
  var username by remember { mutableStateOf(user.name) }
  val snackbarHostState = remember { SnackbarHostState() }
  var showSnackbar by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    content = { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues = paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Outlined text field for updating username
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save/Update username button
        Button(onClick = {
          MainRepository.updateUserName(username) { success ->
            if (success) {
              scope.launch {
                showSnackbar = true
                snackbarHostState.showSnackbar("Name updated!")
                delay(3000)
                showSnackbar = false
              }
            } else {
              scope.launch {
                showSnackbar = true
                snackbarHostState.showSnackbar("Failed to update name!")
                delay(3000)
                showSnackbar = false
              }
            }
          }
        }) {
          Text("Save Username")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ReOwn Connect Button
        ConnectButton(
          state = appKitState,
          buttonSize = ConnectButtonSize.NORMAL
          // Possibly other parameters if your library supports them
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
          WalletService.assign(user.id) { err, url ->
            if (err != null) {
              scope.launch {
                showSnackbar = true
                snackbarHostState.showSnackbar("Failed to assign wallet $err")
                delay(3000)
                showSnackbar = false
              }
            } else {
              scope.launch {
                showSnackbar = true
                snackbarHostState.showSnackbar("Wallet assigned! $url")
                delay(3000)
                showSnackbar = false
              }
//              try {
//                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
//              } catch (e: Exception) {
//                Timber.tag("AccountRoute").d("Activity not found: $e")
//              }
            }
          }
        }) {
          Text("Assign Wallet to Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disconnect wallet
        Button(onClick = {
          WalletService.disconnect(
            onSuccess = { url ->
              // TODO: replace with ViewModel stuff Toast.makeText(context, "Disconnected: $url", Toast.LENGTH_LONG).show()
            },
            onError = { err ->
              // TODO: Toast.makeText(context, "Failed to disconnect wallet: $err", Toast.LENGTH_LONG).show()
            }
          )
        }) {
          Text("Disconnect Metamask")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete account
        Button(onClick = {
          MainRepository.deleteAccount { success ->
            if (success) {
              // Clears local token in the repository
              context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

              // TODO: Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()

              // If you have a dedicated Auth route in Compose Navigation, you can navigate to it:
              // navController.navigate("auth") {
              //     popUpTo(0) // or back to a suitable start route
              // }
              // Or if you want to close the entire app or activity, handle that at the Activity level if needed.
            } else {
              // TODO: Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
          }
        }) {
          Text("Delete Account")
        }

        Button(onClick = {
          // Clears local token in the repository
          context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        }) {
          Text("Logout")
        }
      }
    }
  )
}




//class AccountActivity/*(navController: NavController)*/ : AppCompatActivity() {
//    private lateinit var userNameEditText: EditText
//    private lateinit var saveNameButton: Button
//    //private lateinit var connectMetamaskButton: Button
//    private lateinit var disconnectMetamaskButton: Button
//    private lateinit var deleteAccountButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_account)
//
//        userNameEditText = findViewById(R.id.editUserName)
//        saveNameButton = findViewById(R.id.btnSaveUserName)
//        //connectMetamaskButton = findViewById(R.id.btnConnectMetamask)
//        disconnectMetamaskButton = findViewById(R.id.btnDisconnectMetamask)
//        deleteAccountButton = findViewById(R.id.btnDeleteAccount)
//
//        //loadUserInfo()
//        MainRepository.getUser(
//          { user ->
//              runOnUiThread {
//                  if (user != null) {
//                      userNameEditText.setText(user.name)
//                  } else {
//                      Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
//                  }
//              }
//          },
//          false
//        )
//        saveNameButton.setOnClickListener {
//            val newName = userNameEditText.text.toString()
//            MainRepository.updateUserName(newName) { success ->
//                if (success) {
//                    Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // Disconnect wallet:
//        disconnectMetamaskButton.setOnClickListener {
//            WalletService.disconnect(
//              { url -> runOnUiThread { Toast.makeText(this, "Disconnected: $url", Toast.LENGTH_LONG).show() } },
//              { err -> runOnUiThread { Toast.makeText(this, "Failed to disconnect wallet: $err", Toast.LENGTH_LONG).show() } }
//            )
//        }
//
//        // Delete account:
//        deleteAccountButton.setOnClickListener {
//            MainRepository.deleteAccount { success ->
//                runOnUiThread {
//                    if (success) {
//                        // Clears local token in the repository
//                        clearLocalSession()
//                        val intent = Intent(this, AuthActivity::class.java)
//                        startActivity(intent)
//                        finishAffinity()
//                    } else {
//                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//
//        // Connect wallet:
////        val appKitState = rememberAppKitState(navController = navController)
////        ConnectButton(
////            state = appKitState,
////            buttonSize = ConnectButtonSize.NORMAL || ConnectButtonSize.SMALL
////        )
////        connectMetamaskButton.setOnClickListener {
////            connectMetamask()
////        }
////        AppKit.authenticate()
////        AppKit.connect(Modal.Params.Connect { })
//    }
//
////    private fun loadUserInfo() {
////        // Get user from local or server
////        MainRepository.getUser { user ->
////            userNameEditText.setText(user?.name ?: "")
////        }
////    }
//
////    private fun connectMetamask() {
////        MainRepository.getNonceForWallet { nonce ->
////            if (nonce == null) {
////                // Show error
////                return@getNonceForWallet
////            }
////            // Show wallet app or do a deep link for metamask, sign nonce...
////            val signature = signNonceInWalletApp(nonce) // pseudo-code
////            MainRepository.registerWallet(signature, "0x123YourWalletHere") { success ->
////                runOnUiThread {
////                    if (success) {
////                        Toast.makeText(this, "Wallet connected!", Toast.LENGTH_SHORT).show()
////                    } else {
////                        Toast.makeText(this, "Failed to connect wallet", Toast.LENGTH_SHORT).show()
////                    }
////                }
////            }
////        }
////        MainRepository.getNonceForWallet { nonce ->
////            // Open Metamask or WalletConnect flow
////            // On success, call:
////            MainRepository.registerWallet(signature, walletAddress) { success ->
////                if (success) {
////                    Toast.makeText(this, "Wallet connected", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
//
//    private fun clearLocalSession() {
//        getSharedPreferences("my_prefs", MODE_PRIVATE).edit().clear().apply()
//    }
//}

@Composable
fun SimpleResultDialog(
  message: String,
  error: String,
  onClose: () -> Unit
) {
  AlertDialog(
    modifier = Modifier.semantics {
      contentDescription = "result_dialog"
    },
    onDismissRequest = onClose,
    text = {
      if (message.isNotEmpty()) {
        Text(
          text = message,
          modifier = Modifier.semantics {
            contentDescription = "result_message"
          }
        )
      }
      if (error.isNotEmpty()) {
        Text(
          text = error,
          modifier = Modifier.semantics {
            contentDescription = "result_error"
          }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onClose,
        modifier = Modifier.semantics {
          contentDescription = "close_button"
        }
      ) {
        Text("Close")
      }
    }
  )
}