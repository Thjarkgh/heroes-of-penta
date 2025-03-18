package com.heroesofpenta.ui.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.heroesofpenta.R
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.repository.MainRepository
@Composable
fun RosterScreen(navController: NavController) {
  val context = LocalContext.current

  var nftList by remember { mutableStateOf<List<NftHero>>(emptyList()) }
  val selectedHeroes = remember { mutableStateListOf<NftHero>() }

  // Fetch NFT heroes
  LaunchedEffect(Unit) {
    MainRepository.getNftHeroes { heroes ->
      nftList = heroes
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    // 1) Background Image
    Image(
      painter = painterResource(R.drawable.bg_hero_detail), // your background drawable
      contentDescription = null,
      modifier = Modifier
        .matchParentSize(), // fill the entire box
      contentScale = ContentScale.Crop // crop to fill the screen
      // optional .alpha(0.5f) if you want a translucent background
    )
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

      // 1) Top bar (Train + Account)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Button(
          onClick = {
            if (selectedHeroes.isEmpty()) {
              Toast.makeText(context, "No NFT selected", Toast.LENGTH_SHORT).show()
              return@Button
            }
            MainRepository.checkTrainingCooldown { canTrain ->
              if (canTrain) {
                val selectedIds = selectedHeroes.map { it.id }.toTypedArray()
                navController.navigate("camera/${selectedIds.joinToString(",")}")
              } else {
                Toast.makeText(context, "Training on cooldown!", Toast.LENGTH_SHORT).show()
              }
            }
          }
        ) {
          Text("Train")
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
          modifier = Modifier
            .size(48.dp) // Make it a 48x48 "box"
            .clip(RoundedCornerShape(8.dp)) // Rounded corners
            .background(Color.White),         // White background
          onClick = {
            navController.navigate("account")
          }
        ) {
          Icon(
            imageVector = Icons.Default.AccountBox,
            contentDescription = "Account",
            tint = Color.Black
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2) Horizontally scrollable TCG cards
      LazyRow(
        modifier = Modifier
          .fillMaxSize(), // fill all remaining space
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(nftList) { hero ->
          HeroCard(
            hero = hero,
            isSelected = selectedHeroes.contains(hero),
            onHeroClicked = {
              // Navigate to detail
              navController.navigate("detail/${hero.id}")
            },
            onSelectionChanged = { isChecked ->
              if (isChecked) {
                selectedHeroes.add(hero)
              } else {
                selectedHeroes.remove(hero)
              }
            }
          )
        }
      }
    }
  }
}


@Composable
fun HeroCard(
  hero: NftHero,
  isSelected: Boolean,
  onHeroClicked: () -> Unit,
  onSelectionChanged: (Boolean) -> Unit
) {
  // A single card with a fixed size so we can scroll horizontally through them
  Card(
    shape = RoundedCornerShape(8.dp),
    elevation = 4.dp,
    modifier = Modifier
      .width(250.dp)  // approximate "trading card" width
      .height(400.dp) // approximate "trading card" height
      .clickable { onHeroClicked() }
  ) {
    Column(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxSize()
    ) {
      // Top row: name & XP
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = hero.name,
          style = MaterialTheme.typography.h6
        )
        Text(
          text = "XP: ${hero.xp}",
          style = MaterialTheme.typography.body2
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Thumbnail in the middle
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f), // let the image consume most vertical space
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(hero.imageUrl)
            .crossfade(true)
            .build(),
          contentDescription = "Hero Thumbnail",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Bottom area: checkbox for selecting hero
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Select", modifier = Modifier.weight(1f))
        Checkbox(
          checked = isSelected,
          onCheckedChange = { onSelectionChanged(it) }
        )
      }
    }
  }
}


//import android.widget.Toast
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.Button
//import androidx.compose.material.Checkbox
//import androidx.compose.material.Icon
//import androidx.compose.material.IconButton
//import androidx.compose.material.Text
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AccountBox
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.heroesofpenta.data.models.NftHero
//import com.heroesofpenta.data.repository.MainRepository
//
//@Composable
//fun RosterScreen(navController: NavController) {
//  val context = LocalContext.current
//
//  // List of heroes fetched from the server
//  var nftList by remember { mutableStateOf<List<NftHero>>(emptyList()) }
//
//  // Keep track of which heroes are selected for training
//  val selectedHeroes = remember { mutableStateListOf<NftHero>() }
//
//  // Fetch NFT heroes once (or whenever you decide it should refresh)
//  LaunchedEffect(Unit) {
//    MainRepository.getNftHeroes { heroes ->
//      nftList = heroes
//    }
//  }
//
//  Column(modifier = Modifier.padding(16.dp)) {
//
//    // Top bar with train and account buttons
//    Row(modifier = Modifier.fillMaxWidth()) {
//      Button(
//        onClick = {
//          // Check if any hero is selected
//          if (selectedHeroes.isEmpty()) {
//            Toast.makeText(context, "No NFT selected", Toast.LENGTH_SHORT).show()
//            return@Button
//          }
//          // Check training cooldown from server
//          MainRepository.checkTrainingCooldown { canTrain ->
//            if (canTrain) {
//              // Navigate to camera, passing selected hero IDs
//              val selectedIds = selectedHeroes.map { it.id }.toTypedArray()
//              // Join them as a string if you want to pass them in the nav route
//              navController.navigate(
//                "camera/${selectedIds.joinToString(separator = ",")}"
//              )
//            } else {
//              Toast.makeText(context, "Training on cooldown!", Toast.LENGTH_SHORT).show()
//            }
//          }
//        }
//      ) {
//        Text("Train")
//      }
//
//      Spacer(modifier = Modifier.width(16.dp))
//
//      IconButton(
//        onClick = {
//          // Navigate to account screen
//          navController.navigate("account")
//        }
//      ) {
//        // Replace with your account icon if you have one
//        Icon(
//          imageVector = Icons.Default.AccountBox,
//          contentDescription = "Account"
//        )
//      }
//    }
//
//    Spacer(modifier = Modifier.height(16.dp))
//
//    // LazyColumn for NFT Heroes
//    LazyColumn {
//      items(nftList) { hero ->
//        HeroRow(
//          hero = hero,
//          isSelected = selectedHeroes.contains(hero),
//          onHeroClicked = {
//            // Navigate to detail
//            navController.navigate("detail/${hero.id}")
//          },
//          onSelectionChanged = { isChecked ->
//            if (isChecked) {
//              selectedHeroes.add(hero)
//            } else {
//              selectedHeroes.remove(hero)
//            }
//          }
//        )
//      }
//    }
//  }
//}
//
//@Composable
//fun HeroRow(
//  hero: NftHero,
//  isSelected: Boolean,
//  onHeroClicked: () -> Unit,
//  onSelectionChanged: (Boolean) -> Unit
//) {
//  Row(
//    modifier = Modifier
//      .fillMaxWidth()
//      .clickable { onHeroClicked() }
//      .padding(vertical = 8.dp)
//  ) {
//    // Let the user select/deselect the hero
//    Checkbox(
//      checked = isSelected,
//      onCheckedChange = { onSelectionChanged(it) }
//    )
//    Spacer(modifier = Modifier.width(8.dp))
//
//    // Example text (You might show hero name, image, etc.)
//    Text(text = hero.id)
//  }
//}
//
////
////
////import android.content.Intent
////import android.os.Bundle
////import android.widget.Button
////import android.widget.ImageButton
////import android.widget.Toast
////import androidx.appcompat.app.AppCompatActivity
////import androidx.recyclerview.widget.LinearLayoutManager
////import androidx.recyclerview.widget.RecyclerView
////import com.heroesofpenta.R
////import com.heroesofpenta.data.models.NftHero
////import com.heroesofpenta.data.repository.MainRepository
////import com.heroesofpenta.ui.account.AccountActivity
////import com.heroesofpenta.ui.camera.CameraActivity
////
////class RosterActivity : AppCompatActivity() {
////
////    private lateinit var recyclerView: RecyclerView
////    private lateinit var nftAdapter: NftAdapter
////    private var nftList: List<NftHero> = listOf()
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
////
////        recyclerView = findViewById(R.id.recyclerView)
////        recyclerView.layoutManager = LinearLayoutManager(this)
////
////        nftAdapter = NftAdapter(onNftClicked = { hero ->
////            // Navigate to detail
////            goToNftDetail(hero)
////        })
////        recyclerView.adapter = nftAdapter
////
////        fetchNftHeroes()
////        setupTrainButton()
////        setupAccountMenu()
////    }
////
////    private fun fetchNftHeroes() {
////        // Make a network call to get the user’s NFT list
////        MainRepository.getNftHeroes { heroes ->
////            this.nftList = heroes
////            nftAdapter.submitList(heroes)
////        }
////    }
////
////    private fun setupTrainButton() {
////        val trainButton: Button = findViewById(R.id.btnTrain)
////        trainButton.setOnClickListener {
////            // Check if training is available from server or if cooldown
////            // For multi-select, you'd keep track of selected items in the adapter
////            val selectedHeroes = nftAdapter.getSelectedHeroes()
////            if (selectedHeroes.isEmpty()) {
////                Toast.makeText(this, "No NFT selected", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
////
////            MainRepository.checkTrainingCooldown { canTrain ->
////                if (canTrain) {
////                    // Launch camera to capture a selfie
////                    val intent = Intent(this, CameraActivity::class.java)
////                    intent.putExtra("selectedHeroIds", selectedHeroes.map { it.id }.toTypedArray())
////                    startActivity(intent)
////                } else {
////                    Toast.makeText(this, "Training on cooldown!", Toast.LENGTH_SHORT).show()
////                }
////            }
////        }
////    }
////
////    private fun setupAccountMenu() {
////        val accountButton: ImageButton = findViewById(R.id.btnAccount)
////        accountButton.setOnClickListener {
////            val intent = Intent(this, AccountActivity::class.java)
////            startActivity(intent)
////        }
////    }
////
////    private fun goToNftDetail(hero: NftHero) {
////        val intent = Intent(this, NftDetailActivity::class.java)
////        intent.putExtra("nft_hero_id", hero.id)
////        startActivity(intent)
////    }
////}
