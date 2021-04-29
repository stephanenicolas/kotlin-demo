package com.github.stephanenicolas.kstock.network

data class SymbolLookupResultResponse(
    val description: String,
    val displaySymbol: String,
    val symbol: String,
    val type: String
)
