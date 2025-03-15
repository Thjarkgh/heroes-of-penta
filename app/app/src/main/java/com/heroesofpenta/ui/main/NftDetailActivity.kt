package com.heroesofpenta.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.repository.MainRepository

@Composable
fun NftDetailScreen(
  heroId: String?,
  navController: NavController
) {
  if (heroId == null) {
    // No heroId passed, just go back or show an error
    LaunchedEffect(Unit) {
      navController.popBackStack()  // or handle however you like
    }
    return
  }

  // We’ll track the hero’s details. If `null`, we’re loading or not found.
  var hero by remember { mutableStateOf<NftHero?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  // Fetch the hero details once
  LaunchedEffect(heroId) {
    MainRepository.getNftHeroById(heroId) { fetchedHero ->
      hero = fetchedHero
      isLoading = false
      if (fetchedHero == null) {
        // Possibly pop back if hero not found
        navController.popBackStack()
      }
    }
  }

  // Display a loading screen or the hero detail
  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(text = "Loading hero details...")
    }
  } else {
    hero?.let { theHero ->
      HeroDetailContent(theHero)
    }
  }
}

@Composable
fun HeroDetailContent(hero: NftHero) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Load hero image using Coil
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(hero.imageUrl)
        .crossfade(true)
        .build(),
      contentDescription = "Hero Image",
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = hero.name,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "XP: ${hero.xp}",
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = hero.description ?: "",
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center
    )
  }
}

//
//import android.os.Bundle
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import com.heroesofpenta.R
//import com.heroesofpenta.data.repository.MainRepository
//
//class NftDetailActivity : AppCompatActivity() {
//
//    private lateinit var heroImage: ImageView
//    private lateinit var heroName: TextView
//    private lateinit var heroXp: TextView
//    private lateinit var heroDesc: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_nft_detail)
//
//        heroImage = findViewById(R.id.imgHeroDetail)
//        heroName = findViewById(R.id.txtHeroDetailName)
//        heroXp = findViewById(R.id.txtHeroDetailXp)
//        heroDesc = findViewById(R.id.txtHeroDetailDesc)
//
//        val heroId = intent.getStringExtra("nft_hero_id")
//        if (heroId != null) {
//            loadHeroDetails(heroId)
//        } else {
//            finish()
//        }
//    }
//
//    private fun loadHeroDetails(id: String) {
//        // call server or get from a cache
//        MainRepository.getNftHeroById(id) { hero ->
//            hero?.let {
//                heroName.text = it.name
//                heroXp.text = "XP: ${it.xp}"
//                heroDesc.text = it.description
//                Glide.with(this).load(it.imageUrl).into(heroImage)
//            } ?: run {
//                finish()
//            }
//        }
//    }
//}
