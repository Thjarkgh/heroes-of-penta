package com.heroesofpenta.ui.account

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.heroesofpenta.R
import com.heroesofpenta.data.repository.MainRepository
import com.heroesofpenta.data.web3.WalletService
import com.heroesofpenta.ui.auth.AuthActivity
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.ui.components.button.ConnectButton
import com.reown.appkit.ui.components.button.ConnectButtonSize
import com.reown.appkit.ui.components.button.rememberAppKitState

class AccountActivity/*(navController: NavController)*/ : AppCompatActivity() {
    private lateinit var userNameEditText: EditText
    private lateinit var saveNameButton: Button
    private lateinit var connectMetamaskButton: Button
    private lateinit var disconnectMetamaskButton: Button
    private lateinit var deleteAccountButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        userNameEditText = findViewById(R.id.editUserName)
        saveNameButton = findViewById(R.id.btnSaveUserName)
        connectMetamaskButton = findViewById(R.id.btnConnectMetamask)
        disconnectMetamaskButton = findViewById(R.id.btnDisconnectMetamask)
        deleteAccountButton = findViewById(R.id.btnDeleteAccount)

        //loadUserInfo()
        MainRepository.getUser { user ->
            runOnUiThread {
                if (user != null) {
                    userNameEditText.setText(user.name)
                } else {
                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
            }
        }
        saveNameButton.setOnClickListener {
            val newName = userNameEditText.text.toString()
            MainRepository.updateUserName(newName) { success ->
                if (success) {
                    Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Disconnect wallet:
        disconnectMetamaskButton.setOnClickListener {
            WalletService.disconnect(
              { url -> runOnUiThread { Toast.makeText(this, "Disconnected: $url", Toast.LENGTH_LONG).show() } },
              { err -> runOnUiThread { Toast.makeText(this, "Failed to disconnect wallet: $err", Toast.LENGTH_LONG).show() } }
            )
        }

        // Delete account:
        deleteAccountButton.setOnClickListener {
            MainRepository.deleteAccount { success ->
                runOnUiThread {
                    if (success) {
                        // Clears local token in the repository
                        clearLocalSession()
                        val intent = Intent(this, AuthActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Connect wallet:
//        val appKitState = rememberAppKitState(navController = navController)
//        ConnectButton(
//            state = appKitState,
//            buttonSize = ConnectButtonSize.NORMAL || ConnectButtonSize.SMALL
//        )
//        connectMetamaskButton.setOnClickListener {
//            connectMetamask()
//        }
//        AppKit.authenticate()
//        AppKit.connect(Modal.Params.Connect { })
    }

//    private fun loadUserInfo() {
//        // Get user from local or server
//        MainRepository.getUser { user ->
//            userNameEditText.setText(user?.name ?: "")
//        }
//    }

//    private fun connectMetamask() {
//        MainRepository.getNonceForWallet { nonce ->
//            if (nonce == null) {
//                // Show error
//                return@getNonceForWallet
//            }
//            // Show wallet app or do a deep link for metamask, sign nonce...
//            val signature = signNonceInWalletApp(nonce) // pseudo-code
//            MainRepository.registerWallet(signature, "0x123YourWalletHere") { success ->
//                runOnUiThread {
//                    if (success) {
//                        Toast.makeText(this, "Wallet connected!", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(this, "Failed to connect wallet", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//        MainRepository.getNonceForWallet { nonce ->
//            // Open Metamask or WalletConnect flow
//            // On success, call:
//            MainRepository.registerWallet(signature, walletAddress) { success ->
//                if (success) {
//                    Toast.makeText(this, "Wallet connected", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    private fun clearLocalSession() {
        getSharedPreferences("my_prefs", MODE_PRIVATE).edit().clear().apply()
    }
}
