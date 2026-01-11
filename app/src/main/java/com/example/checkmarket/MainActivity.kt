package com.example.checkmarket

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checkmarket.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProdutoAdapter
    private var lastSortId: Int = R.id.chipSortNome // Guarda a última ordenação usada

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        setupSortChips()
        setupObserver()
        setupSwipeGestures()
        setupScrollListener()
    }

    // ESTA É A VERSÃO CORRETA E SIMPLIFICADA
    private fun setupRecyclerView() {
        // A única interação que o adapter precisa de saber é o clique para editar.
        // As outras (check, delete) são tratadas pelos gestos de swipe.
        adapter = ProdutoAdapter { produto ->
            val intent = Intent(this, FormularioActivity::class.java)
            intent.putExtra("produto", produto)
            startActivity(intent)
        }
        binding.recyclerViewProdutos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProdutos.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdicionar.setOnClickListener {
            startActivity(Intent(this, FormularioActivity::class.java))
        }
    }

    private fun setupSortChips() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                lastSortId = checkedIds.first()
                lerDadosDoFirebase() // Re-lê os dados com a nova ordenação
            }
        }
    }

    private fun setupObserver() {
        lerDadosDoFirebase()
    }

    private fun lerDadosDoFirebase() {
        val query = when (lastSortId) {
            R.id.chipSortCategoria -> db.collection("produtos").orderBy("categoria")
            R.id.chipSortComprado -> db.collection("produtos").orderBy("comprado")
            else -> db.collection("produtos").orderBy("nome", Query.Direction.ASCENDING)
        }

        query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.w("MainActivity", "Listen failed.", error)
                Toast.makeText(this, "Erro ao ler dados!", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val listaProdutos = snapshots?.map { documento ->
                documento.toObject(Produto::class.java).copy(id = documento.id)
            } ?: emptyList()

            adapter.submitList(listaProdutos)

            // Controla a visibilidade do ecrã de lista vazia
            if (listaProdutos.isEmpty()) {
                binding.recyclerViewProdutos.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerViewProdutos.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }
    }

    private fun deletarProdutoComUndo(produto: Produto) {
        // Primeiro, remove da base de dados
        db.collection("produtos").document(produto.id).delete()

        // Depois, mostra a opção de desfazer
        Snackbar.make(binding.root, "''${produto.nome}'' removido", Snackbar.LENGTH_LONG)
            .setAction("DESFAZER") {
                // Se clicar em desfazer, adiciona o produto de volta
                db.collection("produtos").document(produto.id).set(produto)
            }
            .show()
    }

    private fun setupSwipeGestures() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val produto = adapter.getItem(position)

                if (direction == ItemTouchHelper.LEFT) {
                    deletarProdutoComUndo(produto)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val produtoAtualizado = produto.copy(comprado = !produto.comprado)
                    db.collection("produtos").document(produto.id).set(produtoAtualizado)
                }
            }

            // O resto do código para desenhar o fundo e os ícones
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!.intrinsicHeight) / 2
                val iconBottom = iconTop + ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!.intrinsicHeight

                if (dX > 0) { // Swiping to the right (Marcar como comprado)
                    val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_check)!!
                    val background = ContextCompat.getDrawable(this@MainActivity, R.color.swipe_background_check)!!
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)

                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)

                } else if (dX < 0) { // Swiping to the left (Apagar)
                    val icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!
                    val background = ContextCompat.getDrawable(this@MainActivity, R.color.swipe_background_delete)!!
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    icon.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recyclerViewProdutos)
    }

    private fun setupScrollListener() {
        binding.recyclerViewProdutos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fabAdicionar.isShown) {
                    binding.fabAdicionar.hide()
                } else if (dy < 0 && !binding.fabAdicionar.isShown) {
                    binding.fabAdicionar.show()
                }
            }
        })
    }
}
