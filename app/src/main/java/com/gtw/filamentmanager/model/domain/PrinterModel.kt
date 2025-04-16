package com.gtw.filamentmanager.model.domain

enum class PrinterModel(val modelName: String, val code: String, val make: PrinterMake) {

    X1("X1", "BL-P002", Bambu),
    X1C("X1C", "BL-P001", Bambu),
    P1S("P1S", "C12", Bambu),
    P1P("P1P", "C11", Bambu),
    A1("A1", "N2S", Bambu),
    A1_MINI("A1 Mini", "N1", Bambu);

    companion object {
        fun fromBambuCode(code: String): PrinterModel {
            return PrinterModel.entries.filter { it.make == Bambu }.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown model code: $code")
        }
    }
}
