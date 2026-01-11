package com.example.checkmarket

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checkmarket.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProdutoAdapter
    private var lastSortId: Int = R.id.chipSortNome

    private var listaCompletaDeProdutos: List<Produto> = emptyList()
    private var searchQuery: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupFab()
        setupSortChips()
        setupObserver()
        setupSwipeGestures()
        setupScrollListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText
                filtrarEAtualizarAdapter()
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchQuery = null
                filtrarEAtualizarAdapter()
                return true
            }
        })

        return true
    }

    private fun setupRecyclerView() {
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
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                lastSortId = checkedIds.first()
                lerDadosDoFirebase()
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

            listaCompletaDeProdutos = snapshots?.map { doc ->
                doc.toObject(Produto::class.java).copy(id = doc.id)
            } ?: emptyList()

            filtrarEAtualizarAdapter()
        }
    }

    private fun filtrarEAtualizarAdapter() {
        val listaFiltrada = if (searchQuery.isNullOrBlank()) {
            listaCompletaDeProdutos
        } else {
            listaCompletaDeProdutos.filter {
                it.nome.contains(searchQuery!!, ignoreCase = true) ||
                it.categoria.contains(searchQuery!!, ignoreCase = true)
            }
        }

        adapter.submitList(listaFiltrada)
        atualizarTotais(listaFiltrada) // Calcula e exibe os totais

        binding.emptyState.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewProdutos.visibility = if (listaFiltrada.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun atualizarTotais(lista: List<Produto>) {
        val totalPrevisto = lista.sumOf { it.preco * it.quantidade }
        val totalGasto = lista.filter { it.comprado }.sumOf { it.preco * it.quantidade }

        // Mostra o card de totais apenas se algum produto tiver preço
        val temPreco = lista.any { it.preco > 0 }
        binding.cardTotais.visibility = if (temPreco) View.VISIBLE else View.GONE

        binding.txtTotalPrevisto.text = String.format(Locale.getDefault(), "€%.2f", totalPrevisto)
        binding.txtTotalGasto.text = String.format(Locale.getDefault(), "€%.2f", totalGasto)
    }

    private fun deletarProdutoComUndo(produto: Produto) {
        db.collection("produtos").document(produto.id).delete()
        Snackbar.make(binding.root, "''${produto.nome}'' removido", Snackbar.LENGTH_LONG)
            .setAction("DESFAZER") { db.collection("produtos").document(produto.id).set(produto) }
            .show()
    }

    private fun setupSwipeGestures() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private val deleteColor = ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.swipe_background_delete))
            private val checkColor = ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.swipe_background_check))
            private val deleteIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_delete)!!
            private val checkIcon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_check)!!

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val produto = adapter.currentList[position]

                if (direction == ItemTouchHelper.LEFT) {
                    deletarProdutoComUndo(produto)
                } else if (direction == ItemTouchHelper.RIGHT) {
                    val produtoAtualizado = produto.copy(comprado = !produto.comprado)
                    db.collection("produtos").document(produto.id).set(produtoAtualizado)
                }
            }

            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, action: Int, active: Boolean) {
                val itemView = vh.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + deleteIcon.intrinsicHeight

                when {
                    // Swiping right (check)
                    dX > 0 -> {
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + checkIcon.intrinsicWidth
                        checkIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        checkColor.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                        checkColor.draw(c)
                        checkIcon.draw(c)
                    }
                    // Swiping left (delete)
                    dX < 0 -> {
                        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteColor.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                        deleteColor.draw(c)
                        deleteIcon.draw(c)
                    }
                    else -> { // No swipe
                        checkColor.setBounds(0, 0, 0, 0)
                        deleteColor.setBounds(0, 0, 0, 0)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, action, active)
            }
        }
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(binding.recyclerViewProdutos)
    }

    private fun setupScrollListener() {
        binding.recyclerViewProdutos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && binding.fabAdicionar.isShown) binding.fabAdicionar.hide()
                else if (dy < 0 && !binding.fabAdicionar.isShown) binding.fabAdicionar.show()
            }
        })
    }
}
