package com.example.checkmarket

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.checkmarket.databinding.ActivityItemProdutoBinding
import java.util.Locale

class ProdutoAdapter(
    private val onEditarClick: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(ProdutoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ActivityItemProdutoBinding.inflate(inflater, parent, false)
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = getItem(position)
        holder.bind(produto, onEditarClick)
    }

    public override fun getItem(position: Int): Produto {
        return super.getItem(position)
    }

    class ProdutoViewHolder(private val binding: ActivityItemProdutoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            produto: Produto,
            onEditarClick: (Produto) -> Unit
        ) {
            binding.txtNomeProduto.text = produto.nome
            binding.txtCategoriaProduto.text = produto.categoria
            binding.txtQuantidadeProduto.text = "x${produto.quantidade}"

            // Formata o preço para o formato de moeda (€1,23)
            if (produto.preco > 0) {
                binding.txtPrecoProduto.visibility = View.VISIBLE
                binding.txtPrecoProduto.text = String.format(Locale.getDefault(), "€%.2f", produto.preco)
            } else {
                binding.txtPrecoProduto.visibility = View.GONE
            }

            // Aplica os estilos visuais com base no estado "comprado"
            if (produto.comprado) {
                binding.txtNomeProduto.paintFlags = binding.txtNomeProduto.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.txtCategoriaProduto.paintFlags = binding.txtCategoriaProduto.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                itemView.alpha = 0.6f
            } else {
                binding.txtNomeProduto.paintFlags = binding.txtNomeProduto.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.txtCategoriaProduto.paintFlags = binding.txtCategoriaProduto.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemView.alpha = 1.0f
            }

            itemView.setOnClickListener {
                onEditarClick(produto)
            }
        }
    }
}

class ProdutoDiffCallback : DiffUtil.ItemCallback<Produto>() {
    override fun areItemsTheSame(oldItem: Produto, newItem: Produto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Produto, newItem: Produto): Boolean {
        return oldItem == newItem
    }
}
