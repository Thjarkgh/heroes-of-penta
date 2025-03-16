package com.heroesofpenta.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import com.heroesofpenta.data.network.createIpfsImageLoader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.heroesofpenta.R
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
  val context = LocalContext.current
  val customLoader = remember { createIpfsImageLoader(context) }

  val enchantedLandFont = FontFamily(Font(R.font.enchanted_land))

  val displayName = hero.name

  // The original image is 768x1024 => aspect ratio = 3:4 = 0.75
  val aspectRatio = 3f / 4f
  // Wrap everything in a Box so we can have a background layer
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      //    horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Row at the top: name (center) + XP (right)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // 2a) Name: centered in the row, but we do a Spacer on the left
        Spacer(modifier = Modifier.weight(1f)) // empty left space

        Text(
          text = displayName,
          fontFamily = enchantedLandFont,
          fontSize = 32.sp, // or whatever size suits you
          modifier = Modifier.weight(2f),
          textAlign = TextAlign.Center,
          color = Color.White
        )

        // 2b) XP: smaller, on the right
        Text(
          text = "XP: ${hero.xp}",
          fontSize = 14.sp,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.End,
          color = Color.White
        )
      }

      // 3) The image area in the middle, with golden border
      //    The aspect ratio ~ 3:4 for your 768x1024 images
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),  // take up most vertical space
        contentAlignment = Alignment.Center
      ) {
        //      AspectRatio(ratio = aspectRatio) {
        // A bit of padding if you want a "border-like" space around the image
        Box(
          modifier = Modifier
            .aspectRatio(ratio = aspectRatio)
            .fillMaxSize()
            .border(
              width = 2.dp,
              color = androidx.compose.ui.graphics.Color(0xFFFFD700)
            )
            .padding(4.dp), // small inner padding so the border is visible
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(hero.imageUrl)
              .crossfade(true)
              .listener(
                onError = { _, result ->
                  // Print the error
                  println("Coil error: ${result.throwable}")
                }
              )
              .build(),
            contentDescription = "Hero Image",
            imageLoader = customLoader,
            modifier = Modifier.fillMaxSize(),
            // For a letterbox approach that keeps ratio, you can use `ContentScale.Fit`
            // or `ContentScale.Contain`
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.ic_broken_image)
          )
        }
        // }
      }

      // 4) Description below the image, centered horizontally
      Text(
        text = hero.description.orEmpty(),
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        textAlign = TextAlign.Center,
        color = Color.White,
        fontSize = 14.sp
      )
    }
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
