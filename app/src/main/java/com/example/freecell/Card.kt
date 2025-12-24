package com.example.freecell

data class Card(val rank: Rank, val suit: Suit) {
    enum class Suit(val symbol: Char, val color: Color) {
        HEARTS('♥', Color.RED),
        DIAMONDS('♦', Color.RED),
        CLUBS('♣', Color.BLACK),
        SPADES('♠', Color.BLACK)
    }

    enum class Rank(val value: Int) {
        ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
        EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13)
    }

    enum class Color {
        RED, BLACK
    }

    override fun toString(): String {
        return "${rank.name.first()}${suit.symbol}"
    }
}
