package com.heroesofpenta.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heroesofpenta.R
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.repository.MainRepository
import com.heroesofpenta.ui.account.AccountActivity
import com.heroesofpenta.ui.camera.CameraActivity

class RosterActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var nftAdapter: NftAdapter
    private var nftList: List<NftHero> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        nftAdapter = NftAdapter(onNftClicked = { hero ->
            // Navigate to detail
            goToNftDetail(hero)
        })
        recyclerView.adapter = nftAdapter

        fetchNftHeroes()
        setupTrainButton()
        setupAccountMenu()
    }

    private fun fetchNftHeroes() {
        // Make a network call to get the user’s NFT list
        MainRepository.getNftHeroes { heroes ->
            this.nftList = heroes
            nftAdapter.submitList(heroes)
        }
    }

    private fun setupTrainButton() {
        val trainButton: Button = findViewById(R.id.btnTrain)
        trainButton.setOnClickListener {
            // Check if training is available from server or if cooldown
            // For multi-select, you'd keep track of selected items in the adapter
            val selectedHeroes = nftAdapter.getSelectedHeroes()
            if (selectedHeroes.isEmpty()) {
                Toast.makeText(this, "No NFT selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            MainRepository.checkTrainingCooldown { canTrain ->
                if (canTrain) {
                    // Launch camera to capture a selfie
                    val intent = Intent(this, CameraActivity::class.java)
                    intent.putExtra("selectedHeroIds", selectedHeroes.map { it.id }.toTypedArray())
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Training on cooldown!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupAccountMenu() {
        val accountButton: ImageButton = findViewById(R.id.btnAccount)
        accountButton.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goToNftDetail(hero: NftHero) {
        val intent = Intent(this, NftDetailActivity::class.java)
        intent.putExtra("nft_hero_id", hero.id)
        startActivity(intent)
    }
}
