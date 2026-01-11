package com.example.checkmarket

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checkmarket.databinding.ActivityFormularioBinding
import com.google.firebase.firestore.FirebaseFirestore

class FormularioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioBinding
    private var produtoEditar: Produto? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategoryInput()
        getProdutoParaEditar()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCategoryInput() {
        val categorias = listOf("Mercearia", "Hortifruti", "Açougue", "Padaria", "Higiene", "Limpeza", "Outros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        binding.autoCompleteCategoria.setAdapter(adapter)
    }

    private fun getProdutoParaEditar() {
        produtoEditar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("produto", Produto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("produto") as? Produto
        }

        if (produtoEditar != null) {
            binding.toolbar.title = "Editar Produto"
            binding.editNome.setText(produtoEditar?.nome)
            binding.editQuantidade.setText(produtoEditar?.quantidade.toString())
            binding.autoCompleteCategoria.setText(produtoEditar?.categoria, false) // false para não filtrar a lista
            binding.btnSalvar.text = "Atualizar Produto"
        }
    }

    private fun setupSaveButton() {
        binding.btnSalvar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val quantidadeStr = binding.editQuantidade.text.toString().trim()
            val categoria = binding.autoCompleteCategoria.text.toString().trim()

            if (validateInput(nome, quantidadeStr, categoria)) {
                val quantidade = quantidadeStr.toInt() // Conversão para Int
                salvarOuAtualizarProduto(nome, quantidade, categoria)
            }
        }
    }

    private fun validateInput(nome: String, quantidade: String, categoria: String): Boolean {
        var isValid = true
        if (nome.isEmpty()) {
            binding.tilNome.error = "O nome do produto é obrigatório."
            isValid = false
        } else {
            binding.tilNome.error = null
        }

        if (quantidade.isEmpty()) {
            binding.tilQuantidade.error = "A quantidade é obrigatória."
            isValid = false
        } else {
            try {
                quantidade.toInt()
                binding.tilQuantidade.error = null
            } catch (e: NumberFormatException) {
                binding.tilQuantidade.error = "Quantidade inválida."
                isValid = false
            }
        }

        if (categoria.isEmpty()) {
            binding.tilCategoria.error = "A categoria é obrigatória."
            isValid = false
        } else {
            binding.tilCategoria.error = null
        }

        return isValid
    }

    private fun salvarOuAtualizarProduto(nome: String, quantidade: Int, categoria: String) {
        showLoading(true)

        if (produtoEditar != null) {
            // Atualizar produto existente
            val produtoAtualizado = mapOf(
                "nome" to nome,
                "quantidade" to quantidade,
                "categoria" to categoria,
            )
            db.collection("produtos").document(produtoEditar!!.id)
                .update(produtoAtualizado)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Erro ao atualizar o produto.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Salvar novo produto
            val novoProduto = Produto(
                id = "", // O ID será gerado pelo Firestore
                nome = nome,
                quantidade = quantidade,
                categoria = categoria,
                comprado = false
            )
            db.collection("produtos").add(novoProduto)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Produto adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Erro ao adicionar o produto.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSalvar.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSalvar.isEnabled = true
        }
    }
}
