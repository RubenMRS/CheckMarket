package com.example.checkmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdicionar: FloatingActionButton
    private lateinit var adapter: ProdutoAdapter

    private val listaProdutos = mutableListOf<Produto>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewProdutos)
        fabAdicionar = findViewById(R.id.fabAdicionar)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ProdutoAdapter(listaProdutos,
            onProdutoCheck = { produto -> atualizarProduto(produto) },
            onDeletarClick = { produto -> deletarProduto(produto) },
            onEditarClick = { produto ->
                val intent = Intent(this, FormularioActivity::class.java)
                intent.putExtra("produto", produto)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        fabAdicionar.setOnClickListener {
            val intent = Intent(this, FormularioActivity::class.java)
            startActivity(intent)
        }

        findViewById<android.widget.Button>(R.id.btnSortNome).setOnClickListener {
            listaProdutos.sortBy { it.nome.lowercase() }
            adapter.notifyDataSetChanged()
        }

        findViewById<android.widget.Button>(R.id.btnSortCategoria).setOnClickListener {
            listaProdutos.sortBy { it.categoria }
            adapter.notifyDataSetChanged()
        }

        findViewById<android.widget.Button>(R.id.btnSortPreco).setOnClickListener {
            listaProdutos.sortBy { it.comprado }
            adapter.notifyDataSetChanged()
        }

        lerDadosDoFirebase()
    }

    private fun lerDadosDoFirebase() {
        db.collection("produtos")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Toast.makeText(this, "Erro ao ler dados!", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaProdutos.clear()

                    for (documento in snapshots) {
                        val produto = documento.toObject(Produto::class.java)
                        produto.id = documento.id
                        listaProdutos.add(produto)
                    }

                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun atualizarProduto(produto: Produto) {
        db.collection("produtos").document(produto.id)
            .update("comprado", produto.comprado)
    }

    private fun deletarProduto(produto: Produto) {
        db.collection("produtos").document(produto.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Produto removido!", Toast.LENGTH_SHORT).show()
            }
    }
}