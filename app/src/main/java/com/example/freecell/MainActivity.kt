package com.example.freecell

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class MainActivity : AppCompatActivity() {

    private lateinit var game: FreecellGame
    private var selectedLocation: FreecellGame.Location? = null
    private var selectedView: View? = null

    private lateinit var freecellsLayout: LinearLayout
    private lateinit var foundationsLayout: LinearLayout
    private lateinit var tableauLayout: LinearLayout

    private val freecellViews = mutableMapOf<Int, TextView>()
    private val foundationViews = mutableMapOf<Int, TextView>()
    private val tableauViews = mutableMapOf<Int, MutableList<TextView>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        freecellsLayout = findViewById(R.id.freecells_layout)
        foundationsLayout = findViewById(R.id.foundations_layout)
        tableauLayout = findViewById(R.id.tableau_layout)

        findViewById<Button>(R.id.undo_button).setOnClickListener {
            game.undoMove()
            updateUI()
        }

        findViewById<Button>(R.id.new_game_button).setOnClickListener {
            game.resetGame()
            updateUI()
        }

        findViewById<Button>(R.id.hint_button).setOnClickListener {
            val hint = game.getHint()
            if (hint != null) {
                highlightHint(hint)
            }
        }

        game = FreecellGame()
        setupUI()
        updateUI()
    }

    private fun setupUI() {
        for (i in 0..3) {
            val freecellView = createCardView(null, FreecellGame.Location.Freecell(i, game))
            freecellsLayout.addView(freecellView)
            freecellViews[i] = freecellView

            val foundationView = createCardView(null, FreecellGame.Location.Foundation(i, game))
            foundationsLayout.addView(foundationView)
            foundationViews[i] = foundationView
        }

        for (i in 0..7) {
            val columnLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            tableauLayout.addView(columnLayout)
            tableauViews[i] = mutableListOf()
        }
    }

    private fun updateUI() {
        renderFreecells()
        renderFoundations()
        renderTableau()
    }

    private fun renderFreecells() {
        for (i in game.freecells.indices) {
            val card = game.freecells[i]
            updateCardView(freecellViews[i], card, FreecellGame.Location.Freecell(i, game))
        }
    }

    private fun renderFoundations() {
        for (i in game.foundations.indices) {
            val card = game.foundations[i].lastOrNull()
            updateCardView(foundationViews[i], card, FreecellGame.Location.Foundation(i, game))
        }
    }

    private fun renderTableau() {
        for (i in game.tableau.indices) {
            val columnLayout = tableauLayout.getChildAt(i) as LinearLayout
            columnLayout.removeAllViews()
            tableauViews[i]?.clear()

            val column = game.tableau[i]
            if (column.isEmpty()) {
                val placeholder = createCardView(null, FreecellGame.Location.Tableau(i, 0, game))
                columnLayout.addView(placeholder)
            } else {
                for (j in column.indices) {
                    val card = column[j]
                    val location = FreecellGame.Location.Tableau(i, j, game)
                    val cardView = createCardView(card, location)
                    columnLayout.addView(cardView)
                    tableauViews[i]?.add(cardView)
                }
            }
        }
    }

    private fun updateCardView(cardView: TextView?, card: Card?, location: FreecellGame.Location) {
        cardView?.apply {
            if (card != null) {
                text = "${card.rank.name.first()}${card.suit.symbol}"
                setTextColor(if (card.suit.color == Card.Color.RED) Color.RED else Color.BLACK)
            } else {
                text = " "
            }
            setOnClickListener { onLocationTapped(location, this) }
            background = getCardBackground(false)
        }
    }

    private fun createCardView(card: Card?, location: FreecellGame.Location): TextView {
        val cardView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_content
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            minHeight = 150 // Increased height for better touch target
            gravity = android.view.Gravity.CENTER
            textSize = 32f // Larger font size
            background = getCardBackground(false)

            if (card != null) {
                text = "${card.rank.name.first()}${card.suit.symbol}"
                setTextColor(if (card.suit.color == Card.Color.RED) Color.RED else Color.BLACK)
            } else {
                text = " " // Placeholder for empty piles
            }
        }

        cardView.setOnClickListener { onLocationTapped(location, cardView) }
        return cardView
    }

    private fun onLocationTapped(location: FreecellGame.Location, view: TextView) {
        val currentSelection = selectedLocation
        if (currentSelection == null) {
            // Nothing is selected, so select this card if it's not empty
            if (location.getCard() != null) {
                if (location is FreecellGame.Location.Tableau) {
                    val column = game.tableau[location.index]
                    if (location.cardIndex == column.size - 1) {
                        selectedLocation = location
                        selectedView = view
                        view.background = getCardBackground(true) // Highlight
                    }
                } else {
                    selectedLocation = location
                    selectedView = view
                    view.background = getCardBackground(true) // Highlight
                }
            }
        } else {
            // A card is already selected, try to move it
            if (game.moveCard(currentSelection, location)) {
                // Move successful
                selectedLocation = null
                selectedView = null
                updateUI()
            } else {
                // Move failed, deselect and remove highlight from the previously selected card
                selectedView?.background = getCardBackground(false)
                selectedLocation = null
                selectedView = null
            }
        }
    }

    private fun getCardBackground(isSelected: Boolean, isHint: Boolean = false): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f
            setColor(Color.WHITE)
            when {
                isSelected -> setStroke(8, Color.BLUE)
                isHint -> setStroke(8, Color.GREEN)
                else -> setStroke(4, Color.GRAY)
            }
        }
    }

    private fun highlightHint(move: FreecellGame.Move) {
        updateUI() // Clear previous hints

        val fromView = findViewForLocation(move.from)
        val toView = findViewForLocation(move.to)

        fromView?.background = getCardBackground(false, true)
        toView?.background = getCardBackground(false, true)
    }

    private fun findViewForLocation(location: FreecellGame.Location): View? {
        return when (location) {
            is FreecellGame.Location.Freecell -> freecellViews[location.index]
            is FreecellGame.Location.Foundation -> foundationViews[location.index]
            is FreecellGame.Location.Tableau -> tableauViews[location.index]?.getOrNull(location.cardIndex)
        }
    }
}
