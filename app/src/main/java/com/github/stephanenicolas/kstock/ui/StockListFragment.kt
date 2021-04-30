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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnSuggestionListener
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.databinding.FragmentStockListBinding
import com.github.stephanenicolas.kstock.model.Stock
import com.github.stephanenicolas.kstock.ui.StockItemRecyclerViewAdapter.*
import com.github.stephanenicolas.kstock.viewmodel.StockViewModel


/**
 * A Fragment representing a list of Pings. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link ItemDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class StockListFragment : Fragment() {

    private lateinit var navigator: Navigator
    private var _binding: FragmentStockListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel by viewModels<StockViewModel>({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        setupSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_search -> true
            R.id.action_edit -> true
            else -> super.onOptionsItemSelected(item)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStockListBinding.inflate(inflater, container, false)

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

        val onClickListener = View.OnClickListener { itemView ->
            val stock = itemView.tag as Stock
            navigator.openDetails(stock)
        }

        setupRecyclerView(recyclerView, onClickListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSearchView(menu: Menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        activity?.menuInflater?.inflate(R.menu.menu_list, menu)
        val searchItem = menu.findItem(R.id.action_search)
        //Get SearchView through MenuItem
        val searchView = searchItem.actionView as SearchView
        with(searchView) {
            val searchSuggestionsAdapter = searchSuggestionAdapter()
            val autoCompleteTextView = setupAutoCompleteTextView()
            setOnSuggestionListener(SuggestionListener(searchView))
            suggestionsAdapter = searchSuggestionsAdapter
            setOnQueryTextListener(SearchSuggestionQueryListener(searchSuggestionsAdapter))
            viewModel.error.observe(this@StockListFragment) {
                searchView.collapse(searchItem)
            }
        }
    }

    private fun SearchView.collapse(searchItem: MenuItem) {
        val autoCompleteTextView = findViewById<AppCompatAutoCompleteTextView>(R.id.search_src_text)
        autoCompleteTextView.dismissDropDown()
        setQuery("", false)
        if (!isIconified) {
            isIconified = true
        }
        searchItem.collapseActionView()
    }

    private fun SearchView.setupAutoCompleteTextView(): AppCompatAutoCompleteTextView {
        val autoCompleteTextView = findViewById<AppCompatAutoCompleteTextView>(R.id.search_src_text)
        with(autoCompleteTextView) {
            threshold = 1
            setDropDownBackgroundResource(android.R.color.white)
        }
        return autoCompleteTextView
    }

    private fun searchSuggestionAdapter(): SimpleCursorAdapter {
        val from = arrayOf(SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_TEXT_2)
        val to = intArrayOf(R.id.search_item_symbol, R.id.search_item_description)
        return SimpleCursorAdapter(
            context,
            R.layout.item_suggestion,
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

        val adapter = StockItemRecyclerViewAdapter(
            viewModel.data.value!!,
            onClickListener
        )
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner, { adapter.items = it })

        viewModel.loadQuotes()
        viewModel.loadLastPrices()
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

    inner class Navigator(
        private val itemView: View,
        private val itemDetailFragmentContainer: View?
    ) {

        fun openDetails(stock: Stock) {
            viewModel.selectedStock.value = stock
            if (itemDetailFragmentContainer != null) {
                itemDetailFragmentContainer.findNavController()
                    .navigate(R.id.fragment_item_detail)
            } else {
                itemView.findNavController().navigate(R.id.show_item_detail)
            }
        }
    }

    inner class SearchSuggestionQueryListener(
        private val searchSuggestionsAdapter: SimpleCursorAdapter
    ) : SearchView.OnQueryTextListener {

        init {
            viewModel.searchResults.observe(this@StockListFragment) {
                updateSuggestions(it)
            }
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            hideKeyboard()
            query?.let {
                val stock = Stock(symbol = query)
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

        private fun updateSuggestions(stocks: List<Stock>) {
            val cursor = suggestionCursor()
            stocks.forEachIndexed { index, suggestion ->
                cursor.addRow(arrayOf(index, suggestion.symbol, suggestion.description))
                searchSuggestionsAdapter.changeCursor(cursor)
            }
        }

        private fun suggestionCursor() = MatrixCursor(
            arrayOf(BaseColumns._ID, SUGGEST_COLUMN_TEXT_1, SUGGEST_COLUMN_TEXT_2)
        )
    }

    inner class SuggestionListener(private val searchView: SearchView) : OnSuggestionListener {
        override fun onSuggestionSelect(position: Int): Boolean = false

        override fun onSuggestionClick(position: Int): Boolean {
            hideKeyboard()
            val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
            val selection = cursor.getString(cursor.getColumnIndex(SUGGEST_COLUMN_TEXT_1))
            searchView.setQuery(selection, true)
            return true
        }
    }
}