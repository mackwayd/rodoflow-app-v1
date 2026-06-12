package com.example.rodoflow.data.sync

enum class PendingOperationType(val wire: String) {
    CREATE_VIAGEM("CREATE_VIAGEM"),
    CREATE_DESPESA("CREATE_DESPESA"),
    CREATE_ABASTECIMENTO("CREATE_ABASTECIMENTO"),
    FINALIZAR_VIAGEM("FINALIZAR_VIAGEM"),
    ;

    companion object {
        fun fromWire(value: String): PendingOperationType =
            entries.first { it.wire == value }
    }
}
