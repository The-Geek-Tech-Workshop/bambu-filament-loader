package com.gtw.filamentmanager.model.domain

enum class PrinterModel(val modelName: String, val code: String) {
    X1("X1", "BL-P002"),
    X1C("X1C", "BL-P001"),
    P1S("P1S", "C12"),
    P1P("P1P", "C11"),
    A1("A1", "N2S"),
    A1_MINI("A1 Mini", "N1");

    companion object {
        fun fromCode(code: String): PrinterModel {
            return PrinterModel.entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown model code: $code")
        }
    }
}
