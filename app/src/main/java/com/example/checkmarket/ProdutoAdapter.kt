package com.example.checkmarket // Confirme se o pacote é o seu mesmo

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProdutoAdapter(
    private val listaProdutos: MutableList<Produto>,
    private val onProdutoCheck: (Produto) -> Unit,
    private val onDeletarClick: (Produto) -> Unit,
    private val onEditarClick: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun getItemCount(): Int = listaProdutos.size

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = listaProdutos[position]

        holder.txtNome.text = produto.nome
        holder.txtDetalhes.text = "${produto.quantidade} - ${produto.categoria}"

        holder.cbComprado.setOnCheckedChangeListener(null)
        holder.cbComprado.isChecked = produto.comprado

        if (produto.comprado) {
            holder.txtNome.paintFlags = holder.txtNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.txtNome.paintFlags = holder.txtNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }


        holder.cbComprado.setOnClickListener {
            produto.comprado = holder.cbComprado.isChecked
            onProdutoCheck(produto)
        }

        holder.btnDeletar.setOnClickListener {
            onDeletarClick(produto)
        }

        holder.itemView.setOnClickListener {
            onEditarClick(produto)
        }
    }

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome: TextView = itemView.findViewById(R.id.txtNomeProduto)
        val txtDetalhes: TextView = itemView.findViewById(R.id.txtDetalhesProduto)
        val cbComprado: CheckBox = itemView.findViewById(R.id.cbComprado)
        val btnDeletar: ImageButton = itemView.findViewById(R.id.btnDeletar)
    }
}