package com.heroesofpenta.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heroesofpenta.R
import com.heroesofpenta.data.models.NftHero
import com.google.android.material.card.MaterialCardView

class NftAdapter(
    private val onNftClicked: (NftHero) -> Unit
) : RecyclerView.Adapter<NftAdapter.NftViewHolder>() {

    private val heroes = mutableListOf<NftHero>()
    private val selectedHeroes = mutableSetOf<String>() // store hero IDs

    fun submitList(list: List<NftHero>) {
        heroes.clear()
        heroes.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelectedHeroes(): List<NftHero> {
        return heroes.filter { selectedHeroes.contains(it.id) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nft_hero, parent, false)
        return NftViewHolder(view)
    }

    override fun onBindViewHolder(holder: NftViewHolder, position: Int) {
        holder.bind(heroes[position])
    }

    override fun getItemCount() = heroes.size

    inner class NftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Now be sure to reference MaterialCardView, not just CardView
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        private val imageView: ImageView = itemView.findViewById(R.id.imgHero)
        private val nameText: TextView = itemView.findViewById(R.id.txtHeroName)
        private val xpText: TextView = itemView.findViewById(R.id.txtHeroXp)

        fun bind(hero: NftHero) {
            nameText.text = hero.name
            xpText.text = "XP: ${hero.xp}"

            // Load image using Glide/Coil/Picasso
            Glide.with(itemView).load(hero.imageUrl).into(imageView)

            // Visual indication if selected
            cardView.isChecked = selectedHeroes.contains(hero.id)
            cardView.setOnClickListener {
                // Toggle selection
                if (selectedHeroes.contains(hero.id)) {
                    selectedHeroes.remove(hero.id)
                    cardView.isChecked = false //NEW
                } else {
                    selectedHeroes.add(hero.id)
                    cardView.isChecked = true //NEW
                }
                notifyItemChanged(adapterPosition)

                // Also call the detail callback
                onNftClicked(hero)
            }
            // Animate or tilt the card if you want fancy scrolling effects
            // e.g. using itemView.setOnTouchListener(...) or a custom LayoutManager
        }
    }
}
