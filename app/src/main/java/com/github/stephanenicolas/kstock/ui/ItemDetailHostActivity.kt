package com.github.stephanenicolas.kstock.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.R.id
import com.github.stephanenicolas.kstock.databinding.ActivityItemDetailBinding
import com.github.stephanenicolas.kstock.viewmodel.StockViewModel
import com.google.android.material.snackbar.Snackbar

class ItemDetailHostActivity : AppCompatActivity() {

  private lateinit var appBarConfiguration: AppBarConfiguration

  val viewModel by viewModels<StockViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = ActivityItemDetailBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val navHostFragment =
      supportFragmentManager.findFragmentById(id.nav_host_fragment_item_detail) as NavHostFragment
    val navController = navHostFragment.navController
    appBarConfiguration = AppBarConfiguration(navController.graph)
    setupActionBarWithNavController(navController, appBarConfiguration)

    viewModel.error.observe(this, {
      hideKeyboard(binding.root)
      Snackbar.make(
        binding.root,
        resources.getString(R.string.network_error, it),
        Snackbar.LENGTH_SHORT
      ).show()
    })
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(id.nav_host_fragment_item_detail)
    return navController.navigateUp(appBarConfiguration)
      || super.onSupportNavigateUp()
  }
}