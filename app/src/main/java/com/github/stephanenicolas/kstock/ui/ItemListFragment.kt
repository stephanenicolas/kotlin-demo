package com.github.stephanenicolas.kstock.ui

import android.app.SearchManager.SUGGEST_COLUMN_TEXT_1
import android.app.SearchManager.SUGGEST_COLUMN_TEXT_2
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.databinding.FragmentItemListBinding
import com.github.stephanenicolas.kstock.databinding.ItemListContentBinding
import com.github.stephanenicolas.kstock.model.Stock
import com.github.stephanenicolas.kstock.ui.ItemListFragment.SimpleItemRecyclerViewAdapter.*
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

    private lateinit var navigator: Navigator
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
        setupSearchView(menu)
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
        navigator = Navigator(view, itemDetailFragmentContainer)

        /** Click Listener to trigger navigation based on if you have
         * a single pane layout or two pane layout
         */
        val onClickListener = View.OnClickListener { itemView ->
            val stock = itemView.tag as Stock
            navigator.openDetails(stock)
        }

        setupRecyclerView(recyclerView, onClickListener)
    }


    class Navigator(
        private val itemView: View,
        private val itemDetailFragmentContainer: View?
    ) {

        fun openDetails(stock: Stock) {


            val bundle = Bundle()
            bundle.putString(
                ItemDetailFragment.ARG_ITEM_ID,
                stock.symbol
            )
            if (itemDetailFragmentContainer != null) {
                itemDetailFragmentContainer.findNavController()
                    .navigate(R.id.fragment_item_detail, bundle)
            } else {
                itemView.findNavController().navigate(R.id.show_item_detail, bundle)
            }
        }
    }

    private fun setupSearchView(menu: Menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        activity?.menuInflater?.inflate(R.menu.menu_list, menu)
        val searchItem = menu.findItem(R.id.action_search)
        //Get SearchView through MenuItem
        val searchView = searchItem.actionView as SearchView

        val searchSuggestionsAdapter = searchSuggestionAdapter()

        searchView.suggestionsAdapter = searchSuggestionsAdapter
        val autoCompleteTextView =
            searchView.findViewById<AppCompatAutoCompleteTextView>(R.id.search_src_text)
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.setDropDownBackgroundResource(android.R.color.white)

        searchView.setOnQueryTextListener(
            SearchSuggestionQueryListener(
                viewModel,
                searchSuggestionsAdapter
            )
        )

        searchView.setOnSuggestionListener(SuggestionListener(searchView))
    }

    private fun searchSuggestionAdapter(): SimpleCursorAdapter {
        val from = arrayOf(SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_TEXT_2)
        val to = intArrayOf(R.id.search_item_symbol, R.id.search_item_description)
        return SimpleCursorAdapter(
            context,
            R.layout.search_item,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
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

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Fragment.hideKeyboard() {
        view?.let {
            activity?.hideKeyboard(it)
        }
    }

    inner class SearchSuggestionQueryListener(
        private val viewModel: StockViewModel,
        private val searchSuggestionsAdapter: SimpleCursorAdapter
    ) : SearchView.OnQueryTextListener {

        init {
            viewModel.searchResults.observe(this@ItemListFragment) {
                val cursor =
                    MatrixCursor(
                        arrayOf(
                            BaseColumns._ID,
                            SUGGEST_COLUMN_TEXT_1,
                            SUGGEST_COLUMN_TEXT_2
                        )
                    )
                it.forEachIndexed { index, suggestion ->
                    cursor.addRow(arrayOf(index, suggestion.symbol, suggestion.description))
                    searchSuggestionsAdapter.changeCursor(cursor)
                }
            }
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            hideKeyboard()
            query?.let {
                val stock = Stock(symbol = query!!)
                navigator.openDetails(stock)
            }
            return true
        }

        override fun onQueryTextChange(query: String?): Boolean {
            query?.let {
                if (query.isNotBlank()) {
                    viewModel.search(it)
                }
                return@let
            }
            return true
        }
    }

    inner class SuggestionListener(private val searchView: SearchView) :
        SearchView.OnSuggestionListener {
        override fun onSuggestionSelect(position: Int): Boolean {
            return false
        }

        override fun onSuggestionClick(position: Int): Boolean {
            this@ItemListFragment.hideKeyboard()
            val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
            val selection = cursor.getString(cursor.getColumnIndex(SUGGEST_COLUMN_TEXT_1))
            searchView.setQuery(selection, true)
            return true
        }
    }
}