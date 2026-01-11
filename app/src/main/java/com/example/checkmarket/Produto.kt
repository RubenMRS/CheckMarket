package com.example.checkmarket

import java.io.Serializable

data class Produto(
    var id: String = "",
    var nome: String = "",
    var quantidade: Int = 1, // Valor padrão mudado para 1
    var preco: Double = 0.0,   // Novo campo para o preço
    var categoria: String = "",
    var comprado: Boolean = false
) : Serializable
