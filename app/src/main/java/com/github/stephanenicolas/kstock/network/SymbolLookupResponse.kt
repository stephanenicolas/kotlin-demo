package com.github.stephanenicolas.kstock.network

data class SymbolLookupResponse(val count:Int, val result: List<SymbolLookupResultResponse>)
