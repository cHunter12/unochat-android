package com.chunter.unochat.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chunter.unochat.R

typealias UserClickListener = (User) -> Unit

class UserAdapter(
    private val userClickListener: UserClickListener
) : ListAdapter<User, UserAdapter.UserViewHolder>(userDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_user,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(user: User) {
            itemView.findViewById<TextView>(R.id.email).text = user.name

            itemView.setOnClickListener { userClickListener(user) }
        }
    }

    companion object {

        private val userDiffUtil = object : DiffUtil.ItemCallback<User>() {

            override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem == newItem

            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        }
    }
}