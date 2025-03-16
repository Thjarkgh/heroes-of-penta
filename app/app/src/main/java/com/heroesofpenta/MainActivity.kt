@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.heroesofpenta

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.repository.MainRepository
import com.heroesofpenta.ui.auth.AuthActivity
import com.heroesofpenta.ui.main.NftAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.heroesofpenta.ui.account.AccountScreen
import com.heroesofpenta.ui.camera.CameraScreen
import com.heroesofpenta.ui.main.NftDetailScreen
import com.heroesofpenta.ui.main.RosterScreen
import com.reown.appkit.ui.appKitGraph

class MainActivity : AppCompatActivity() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var nftAdapter: NftAdapter

  @OptIn(ExperimentalMaterialNavigationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val context: Context = this
    val manager = context.getSystemService(DomainVerificationManager::class.java)
    val userState = manager.getDomainVerificationUserState(context.packageName)

//// Domains that have passed Android App Links verification.
//    val verifiedDomains = userState?.hostToStateMap
//      ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }
//
//// Domains that haven't passed Android App Links verification but that the user
//// has associated with an app.
//    val selectedDomains = userState?.hostToStateMap
//      ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }

// All other domains.
    val unapprovedDomains = userState?.hostToStateMap
      ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_NONE }

    if (unapprovedDomains != null) {
      if (unapprovedDomains.isNotEmpty()) {
        val intent = Intent(
          Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
          Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
      }
    }

    MainRepository.getUser(
      { user ->
        if (user == null) {
          // no active account => register
          val intent = Intent(this, AuthActivity::class.java)
          startActivity(intent, savedInstanceState)
        } else {
//          val intent = Intent(this, RosterActivity::class.java)
//          startActivity(intent, savedInstanceState)
          this.setContent {
            var isOfflineState: Boolean? by remember { mutableStateOf(null) }
            val sheetState = rememberModalBottomSheetState(
              initialValue = ModalBottomSheetValue.Hidden,
              skipHalfExpanded = true
            )

            val bottomSheetNavigator = BottomSheetNavigator(sheetState)// rememberBottomSheetNavigator() // BottomSheetNavigator(sheetState)
            val navController = rememberNavController(bottomSheetNavigator)

//            val viewModel: DappSampleViewModel = viewModel()
            ModalBottomSheetLayout(
              bottomSheetNavigator = bottomSheetNavigator,
              sheetBackgroundColor = Color.Transparent,
              sheetElevation = 0.dp,
              scrimColor = Color.Unspecified,
              sheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
              NavHost(
                navController = navController,
                startDestination = "roster"
              ) {
                composable("roster") {
                  RosterScreen(navController)
                }
                composable(
                  route = "detail/{heroId}",
                  arguments = listOf(navArgument("heroId") { type = NavType.StringType })
                ) { backStackEntry ->
                  val heroId = backStackEntry.arguments?.getString("heroId")
                  NftDetailScreen(heroId, navController)
                }
                composable(
                  route = "camera/{selectedHeroIds}",
                  arguments = listOf(navArgument("selectedHeroIds") { type = NavType.StringType })
                ) { backStackEntry ->
                  val selectedHeroIds = backStackEntry.arguments?.getString("selectedHeroIds")
                  CameraScreen(selectedHeroIds, navController)
                }
                composable("account") {
                  AccountScreen(navController)
                }
                appKitGraph(navController)
              }
            }
          }
        }
      },
      false
    )

//    setContentView(R.layout.activity_main)
//
//    // Init Repo
//    // Get references to UI
//    val trainButton = findViewById<Button>(R.id.btnTrain)
//    val accountButton = findViewById<ImageButton>(R.id.btnAccount)
//    recyclerView = findViewById(R.id.recyclerView)
//
//    // Initialize the adapter (pass the click callback)
//    nftAdapter = NftAdapter { hero ->
//      goToNftDetail(hero)
//    }
//
//    recyclerView.layoutManager = LinearLayoutManager(this)
//    recyclerView.adapter = nftAdapter
//
//    trainButton.setOnClickListener {
//      // Example: gather selected heroes
//      val selected = nftAdapter.getSelectedHeroes()
//      if (selected.isEmpty()) {
//        Toast.makeText(this, "No heroes selected", Toast.LENGTH_SHORT).show()
//      } else {
//        // Do something to start the training flow
//        Toast.makeText(this, "Train these heroes: ${selected.size}", Toast.LENGTH_SHORT).show()
//      }
//    }
//
//    accountButton.setOnClickListener {
//      // Go to account screen
//      val intent = Intent(this, AccountActivity::class.java)
//      startActivity(intent)
//    }
//
//    // Finally, fetch the NFT heroes from the repository or server
//    fetchNftHeroes()
  }

//  private fun fetchNftHeroes() {
//    MainRepository.getNftHeroes { heroes ->
//      runOnUiThread {
//        nftAdapter.submitList(heroes)
//      }
//    }
//  }
//
//  private fun goToNftDetail(hero: NftHero) {
//    val intent = Intent(this, NftDetailActivity::class.java)
//    intent.putExtra("nft_hero_id", hero.id)
//    startActivity(intent)
//  }
}
