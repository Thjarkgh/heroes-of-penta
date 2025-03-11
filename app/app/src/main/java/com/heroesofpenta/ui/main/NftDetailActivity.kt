package com.heroesofpenta.ui.main

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.heroesofpenta.R
import com.heroesofpenta.data.repository.MainRepository

class NftDetailActivity : AppCompatActivity() {

    private lateinit var heroImage: ImageView
    private lateinit var heroName: TextView
    private lateinit var heroXp: TextView
    private lateinit var heroDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nft_detail)

        heroImage = findViewById(R.id.imgHeroDetail)
        heroName = findViewById(R.id.txtHeroDetailName)
        heroXp = findViewById(R.id.txtHeroDetailXp)
        heroDesc = findViewById(R.id.txtHeroDetailDesc)

        val heroId = intent.getStringExtra("nft_hero_id")
        if (heroId != null) {
            loadHeroDetails(heroId)
        } else {
            finish()
        }
    }

    private fun loadHeroDetails(id: String) {
        // call server or get from a cache
        MainRepository.getNftHeroById(id) { hero ->
            hero?.let {
                heroName.text = it.name
                heroXp.text = "XP: ${it.xp}"
                heroDesc.text = it.description
                Glide.with(this).load(it.imageUrl).into(heroImage)
            } ?: run {
                finish()
            }
        }
    }
}
