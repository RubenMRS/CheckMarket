package com.example.checkmarket

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class FormularioActivity : AppCompatActivity() {

    private var produtoEditar: Produto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario)

        val editNome = findViewById<EditText>(R.id.editNome)
        val editQuantidade = findViewById<EditText>(R.id.editQuantidade)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)

        val categorias = listOf("Mercado", "Hortifruti", "Açougue", "Padaria", "Limpeza", "Outros")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterSpinner

        produtoEditar = intent.getSerializableExtra("produto") as? Produto

        if (produtoEditar != null) {
            editNome.setText(produtoEditar?.nome)
            editQuantidade.setText(produtoEditar?.quantidade.toString())

            val posicaoCategoria = categorias.indexOf(produtoEditar?.categoria)
            spinnerCategoria.setSelection(posicaoCategoria)

            btnSalvar.text = "Atualizar"
        }

        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString()
            val quantidade = editQuantidade.text.toString().toIntOrNull() ?: 0
            val categoria = spinnerCategoria.selectedItem.toString()

            if (nome.isEmpty()) {
                editNome.error = "Preencha o nome!"
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            if (produtoEditar != null) {
                val dadosAtualizados = mapOf(
                    "nome" to nome,
                    "quantidade" to quantidade,
                    "categoria" to categoria
                )

                db.collection("produtos").document(produtoEditar!!.id)
                    .update(dadosAtualizados)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }

            } else {
                val novo = Produto("", nome, quantidade, categoria, false)

                db.collection("produtos").add(novo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Criado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }
}