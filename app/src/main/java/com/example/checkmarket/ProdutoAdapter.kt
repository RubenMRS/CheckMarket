package com.example.checkmarket

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProdutoAdapter(
    private val onEditarClick: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(ProdutoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = getItem(position)
        holder.bind(produto, onEditarClick)
    }

    public override fun getItem(position: Int): Produto {
        return super.getItem(position)
    }

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNome: TextView = itemView.findViewById(R.id.txtNomeProduto)
        private val txtDetalhes: TextView = itemView.findViewById(R.id.txtDetalhesProduto)
        private val cbComprado: CheckBox = itemView.findViewById(R.id.cbComprado)

        fun bind(
            produto: Produto,
            onEditarClick: (Produto) -> Unit
        ) {
            txtNome.text = produto.nome
            txtDetalhes.text = "${produto.quantidade} - ${produto.categoria}"

            cbComprado.isChecked = produto.comprado
            cbComprado.isClickable = false // Apenas para visualização

            if (produto.comprado) {
                txtNome.paintFlags = txtNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                txtNome.paintFlags = txtNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
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
