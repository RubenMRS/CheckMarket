package com.example.checkmarket

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checkmarket.databinding.ActivityFormularioBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

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
            // Formata o preço para o locale do telemóvel (ex: 1,50 ou 1.50)
            binding.editPreco.setText(String.format(Locale.getDefault(), "%.2f", produtoEditar?.preco))
            binding.autoCompleteCategoria.setText(produtoEditar?.categoria, false)
            binding.btnSalvar.text = "Atualizar Produto"
        }
    }

    private fun setupSaveButton() {
        binding.btnSalvar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val quantidadeStr = binding.editQuantidade.text.toString().trim()
            val precoStr = binding.editPreco.text.toString().trim().replace(',', '.') // Aceita vírgula e ponto
            val categoria = binding.autoCompleteCategoria.text.toString().trim()

            if (validateInput(nome, quantidadeStr, precoStr, categoria)) {
                val quantidade = quantidadeStr.toInt()
                val preco = precoStr.toDouble()
                salvarOuAtualizarProduto(nome, quantidade, preco, categoria)
            }
        }
    }

    private fun validateInput(nome: String, quantidadeStr: String, precoStr: String, categoria: String): Boolean {
        var isValid = true
        // Validação do Nome
        if (nome.isEmpty()) {
            binding.tilNome.error = "O nome é obrigatório."
            isValid = false
        } else {
            binding.tilNome.error = null
        }

        // Validação da Quantidade
        try {
            val qtd = quantidadeStr.toInt()
            if (qtd <= 0) {
                binding.tilQuantidade.error = "Deve ser > 0"
                isValid = false
            } else {
                binding.tilQuantidade.error = null
            }
        } catch (e: NumberFormatException) {
            binding.tilQuantidade.error = "Inválida"
            isValid = false
        }

        // Validação do Preço
        try {
            if (precoStr.isNotEmpty()) {
                val preco = precoStr.toDouble()
                if (preco < 0) {
                    binding.tilPreco.error = "Inválido"
                    isValid = false
                } else {
                    binding.tilPreco.error = null
                }
            }
        } catch (e: NumberFormatException) {
            binding.tilPreco.error = "Inválido"
            isValid = false
        }

        // Validação da Categoria
        if (categoria.isEmpty()) {
            binding.tilCategoria.error = "A categoria é obrigatória."
            isValid = false
        } else {
            binding.tilCategoria.error = null
        }

        return isValid
    }

    private fun salvarOuAtualizarProduto(nome: String, quantidade: Int, preco: Double, categoria: String) {
        showLoading(true)

        if (produtoEditar != null) {
            // Atualiza o produto que já existe
            val produtoAtualizado = produtoEditar!!.copy(
                nome = nome,
                quantidade = quantidade,
                preco = preco,
                categoria = categoria
            )
            db.collection("produtos").document(produtoAtualizado.id).set(produtoAtualizado)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Cria um novo produto
            val novoProduto = Produto(
                nome = nome,
                quantidade = quantidade,
                preco = preco,
                categoria = categoria,
                comprado = false // Novos produtos nunca estão comprados
            )
            db.collection("produtos").add(novoProduto)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "Produto adicionado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "Erro ao adicionar.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSalvar.isEnabled = !isLoading
    }
}
