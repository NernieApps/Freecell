package com.example.freecell

class FreecellGame {
    // Game state
    val freecells = MutableList<Card?>(4) { null }
    val foundations = MutableList<MutableList<Card>>(4) { mutableListOf() }
    val tableau = MutableList<MutableList<Card>>(8) { mutableListOf() }

    // History for the undo feature
    private val history = ArrayDeque<Move>()

    init {
        resetGame()
    }

    fun resetGame() {
        history.clear()
        freecells.fill(null)
        foundations.forEach { it.clear() }
        tableau.forEach { it.clear() }

        val deck = createDeck().shuffled()
        dealDeck(deck)
    }

    private fun createDeck(): List<Card> {
        return Card.Suit.values().flatMap { suit ->
            Card.Rank.values().map { rank ->
                Card(rank, suit)
            }
        }
    }

    private fun dealDeck(deck: List<Card>) {
        var cardIndex = 0
        for (i in 0 until 8) {
            val numCards = if (i < 4) 7 else 6
            for (j in 0 until numCards) {
                tableau[i].add(deck[cardIndex++])
            }
        }
    }

    fun moveCard(from: Location, to: Location): Boolean {
        val card = from.getCard() ?: return false

        if (isValidMove(card, to)) {
            val move = Move(from, to)
            history.addLast(move)

            from.removeCard()
            to.addCard(card)
            return true
        }
        return false
    }

    fun undoMove(): Boolean {
        val lastMove = history.removeLastOrNull() ?: return false
        val card = lastMove.to.getCard() ?: return false

        lastMove.to.removeCard()
        lastMove.from.addCard(card)
        return true
    }

    fun autoMoveToFoundation(from: Location): Boolean {
        val card = from.getCard() ?: return false
        for (i in foundations.indices) {
            val foundation = foundations[i]
            val foundationLocation = Location.Foundation(i)
            if (isValidMove(card, foundationLocation)) {
                return moveCard(from, foundationLocation)
            }
        }
        return false
    }

    fun isGameWon(): Boolean {
        return foundations.all { it.size == 13 }
    }

    private fun isValidMove(card: Card, to: Location): Boolean {
        return when (to) {
            is Location.Freecell -> to.getCard() == null
            is Location.Foundation -> {
                val foundation = foundations[to.index]
                if (foundation.isEmpty()) {
                    card.rank == Card.Rank.ACE
                } else {
                    val topCard = foundation.last()
                    card.suit == topCard.suit && card.rank.value == topCard.rank.value + 1
                }
            }
            is Location.Tableau -> {
                val tableauColumn = tableau[to.index]
                if (tableauColumn.isEmpty()) {
                    true
                } else {
                    val topCard = tableauColumn.last()
                    card.suit.color != topCard.suit.color && card.rank.value == topCard.rank.value - 1
                }
            }
        }
    }

    // Sealed class to represent card locations
    sealed class Location {
        abstract fun getCard(): Card?
        abstract fun removeCard()
        abstract fun addCard(card: Card)

        data class Freecell(val index: Int, val game: FreecellGame) : Location() {
            override fun getCard(): Card? = game.freecells[index]
            override fun removeCard() { game.freecells[index] = null }
            override fun addCard(card: Card) { game.freecells[index] = card }
        }

        data class Foundation(val index: Int, val game: FreecellGame) : Location() {
            override fun getCard(): Card? = game.foundations[index].lastOrNull()
            override fun removeCard() { game.foundations[index].removeLast() }
            override fun addCard(card: Card) { game.foundations[index].add(card) }
        }

        data class Tableau(val index: Int, val cardIndex: Int, val game: FreecellGame) : Location() {
            override fun getCard(): Card? = game.tableau[index].getOrNull(cardIndex)
            override fun removeCard() { game.tableau[index].removeLast() }
            override fun addCard(card: Card) { game.tableau[index].add(card) }
        }
    }

    data class Move(val from: Location, val to: Location)

    fun getHint(): Move? {
        // Check for moves from tableau to foundation
        for (i in tableau.indices) {
            val column = tableau[i]
            if (column.isNotEmpty()) {
                val card = column.last()
                val from = Location.Tableau(i, column.size - 1, this)
                for (j in foundations.indices) {
                    val to = Location.Foundation(j, this)
                    if (isValidMove(card, to)) {
                        return Move(from, to)
                    }
                }
            }
        }

        // Check for moves from freecell to foundation
        for (i in freecells.indices) {
            val card = freecells[i]
            if (card != null) {
                val from = Location.Freecell(i, this)
                for (j in foundations.indices) {
                    val to = Location.Foundation(j, this)
                    if (isValidMove(card, to)) {
                        return Move(from, to)
                    }
                }
            }
        }

        // Check for moves from tableau to freecell
        for (i in tableau.indices) {
            val column = tableau[i]
            if (column.isNotEmpty()) {
                val card = column.last()
                val from = Location.Tableau(i, column.size - 1, this)
                for (j in freecells.indices) {
                    val to = Location.Freecell(j, this)
                    if (isValidMove(card, to)) {
                        return Move(from, to)
                    }
                }
            }
        }

        // Check for moves from tableau to other tableau columns
        for (i in tableau.indices) {
            val fromColumn = tableau[i]
            if (fromColumn.isNotEmpty()) {
                val card = fromColumn.last()
                val from = Location.Tableau(i, fromColumn.size - 1, this)
                for (j in tableau.indices) {
                    if (i != j) {
                        val to = Location.Tableau(j, tableau[j].size, this)
                        if (isValidMove(card, to)) {
                            return Move(from, to)
                        }
                    }
                }
            }
        }

        // Check for moves from freecell to tableau
        for (i in freecells.indices) {
            val card = freecells[i]
            if (card != null) {
                val from = Location.Freecell(i, this)
                for (j in tableau.indices) {
                    val to = Location.Tableau(j, tableau[j].size, this)
                    if (isValidMove(card, to)) {
                        return Move(from, to)
                    }
                }
            }
        }

        return null
    }
}
