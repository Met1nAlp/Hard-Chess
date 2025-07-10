package com.example.hardchess

enum class tasTuru
{
    KALE , AT , FIL , VEZIR , SAH , PIYON
}

enum class tasRengi
{
    BEYAZ , SIYAH
}

data class ChessPiece
(
    val type: tasTuru,
    val color: tasRengi,
    val drawableResId: Int
)