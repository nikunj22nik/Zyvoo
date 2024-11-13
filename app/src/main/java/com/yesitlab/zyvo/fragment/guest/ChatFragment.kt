package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterChatList
import com.yesitlab.zyvo.databinding.FragmentChatBinding
import com.yesitlab.zyvo.model.ChatListModel
import com.yesitlab.zyvo.viewmodel.ChatListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterChatList: AdapterChatList
    private val viewModel: ChatListViewModel by viewModels()
    var objects: Int = 0
    private var chatList: MutableList<ChatListModel> = mutableListOf()
    private var filteredList: MutableList<ChatListModel> = chatList.toMutableList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentChatBinding.inflate(LayoutInflater.from(requireContext()), container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapterChatList = AdapterChatList(requireContext(), chatList)
        binding.recyclerViewChat.adapter = adapterChatList
        viewModel.list.observe(viewLifecycleOwner, Observer { list ->
            chatList = list
            adapterChatList.updateItem(list)
        })

        binding.etSearchButton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

               // val searchText = p0.toString().trim()

            }

            override fun afterTextChanged(p0: Editable?) {
                val query = p0.toString()
                filter(query)

            }
        })
        // Add text change listener for search bar
//        binding.etSearchButton.addTextChangedListener { text ->
//           // Call filter function in adapter
//        }

    }


    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            chatList.toMutableList()
        } else {
            chatList.filter {
                it.textUserName.contains(query, ignoreCase = true) ||
                        it.textDescription.contains(query, ignoreCase = true)
            }.toMutableList()
        }

        adapterChatList.updateItem(filteredList)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}