package com.gtw.filamentmanager.model.domain

sealed interface PrinterMake {
    val name: String
}

data object Bambu : PrinterMake {
    override val name: String = "Bambu Lab"
}