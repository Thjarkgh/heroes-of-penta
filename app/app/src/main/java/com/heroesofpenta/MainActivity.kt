package com.heroesofpenta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.repository.MainRepository
import com.heroesofpenta.ui.account.AccountActivity
import com.heroesofpenta.ui.auth.AuthActivity
import com.heroesofpenta.ui.main.NftAdapter
import com.heroesofpenta.ui.main.NftDetailActivity
import com.heroesofpenta.ui.main.RosterActivity

class MainActivity : AppCompatActivity() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var nftAdapter: NftAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    MainRepository.getUser { user ->
      if (user == null) {
        // no active account => register
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent, savedInstanceState)
      } else {
        val intent = Intent(this, RosterActivity::class.java)
        startActivity(intent, savedInstanceState)
      }
    }

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

  private fun fetchNftHeroes() {
    MainRepository.getNftHeroes { heroes ->
      runOnUiThread {
        nftAdapter.submitList(heroes)
      }
    }
  }

  private fun goToNftDetail(hero: NftHero) {
    val intent = Intent(this, NftDetailActivity::class.java)
    intent.putExtra("nft_hero_id", hero.id)
    startActivity(intent)
  }
}
