package com.heroesofpenta.ui.account
import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
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

@Composable
fun AccountScreen(navController: NavHostController) {
  val context = LocalContext.current

  // We'll load the user asynchronously. If null, show a loading indicator or text
  var user by remember { mutableStateOf<User?>(null) }

  // Fetch the user in a coroutine
  LaunchedEffect(Unit) {
    MainRepository.getUser(
      callback = { fetchedUser -> user = fetchedUser },
      force = false
    )
  }

  if (user == null) {
    // Simple placeholder until the user data is loaded
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "Loading user info...")
    }
  } else {
    // Show the actual account UI once we have the user
    AccountScreenUI(user = user!!, context = context, navController = navController)
  }
}

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
          WalletService.assign(user.id) { err ->
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
                snackbarHostState.showSnackbar("Wallet assigned!")
                delay(3000)
                showSnackbar = false
              }
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
