package com.github.stephanenicolas.kstock.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.databinding.FragmentItemListBinding
import com.github.stephanenicolas.kstock.databinding.ItemListContentBinding
import com.github.stephanenicolas.kstock.ui.ItemListFragment.SimpleItemRecyclerViewAdapter.ViewHolder
import com.github.stephanenicolas.kstock.model.Stock
import com.github.stephanenicolas.kstock.ui.views.LineChartPricesView
import com.github.stephanenicolas.kstock.viewmodel.StockViewModel
import kotlin.properties.Delegates

/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link ItemDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class ItemListFragment : Fragment() {

    private var _binding: FragmentItemListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<StockViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_edit -> {
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        activity?.menuInflater?.inflate(R.menu.menu_list, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentItemListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.itemList

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.item_detail_nav_container)

        /** Click Listener to trigger navigation based on if you have
         * a single pane layout or two pane layout
         */
        val onClickListener = View.OnClickListener { itemView ->
            val item = itemView.tag as Stock

            val bundle = Bundle()
            bundle.putString(
                ItemDetailFragment.ARG_ITEM_ID,
                item.symbol
            )
            if (itemDetailFragmentContainer != null) {
                itemDetailFragmentContainer.findNavController()
                    .navigate(R.id.fragment_item_detail, bundle)
            } else {
                itemView.findNavController().navigate(R.id.show_item_detail, bundle)
            }
        }

        setupRecyclerView(recyclerView, onClickListener)
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        onClickListener: View.OnClickListener
    ) {

        val adapter = SimpleItemRecyclerViewAdapter(
            viewModel.data.value!!,
            onClickListener
        )

        recyclerView.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner, { adapter.items = it })

        viewModel.loadQuotes()
        viewModel.loadLastPrices()
    }

    interface DiffUtilAdapter {
        fun <T> notifyChanges(
            oldList: List<T>,
            newList: List<T>,
            compare: (oldItem: T, newItem: T) -> Boolean
        ) {
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    compare(oldList[oldItemPosition], newList[newItemPosition])

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    oldList[oldItemPosition] == newList[newItemPosition]

                override fun getOldListSize() = oldList.size
                override fun getNewListSize() = newList.size
            })
            diff.dispatchUpdatesTo(this as RecyclerView.Adapter<ViewHolder>)
        }
    }

    class SimpleItemRecyclerViewAdapter(
        values: List<Stock>,
        private val onClickListener: View.OnClickListener
    ) :
        RecyclerView.Adapter<ViewHolder>(), DiffUtilAdapter {

        var items: List<Stock> by Delegates.observable(emptyList()) { prop, oldList, newList ->
            notifyChanges(oldList, newList) { oldItem, newItem ->
                oldItem.symbol == newItem.symbol
            }
        }

        init {
            items = values
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {

            val binding =
                ItemListContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            val item = items[position]
            holder.idView.text = item.symbol
            holder.contentView.text = item.price?.round()
            item.lastPrices?.let {
                holder.lineChartPricesChartView.setLastPrices(item.lastPrices)
            }

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        private fun Float.round(decimals: Int = 2): String = "%.${decimals}f".format(this)

        override fun getItemCount() = items.size

        inner class ViewHolder(binding: ItemListContentBinding) : RecyclerView.ViewHolder(
            binding.root
        ) {
            val idView: TextView = binding.idText
            val contentView: TextView = binding.content
            val lineChartPricesChartView: LineChartPricesView = binding.lastPricesChart
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}