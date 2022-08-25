package org.pathcheck.cqleditorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.pathcheck.cqleditorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  enum class TabEntry (val value: Int) {
    CODE(0),
    DATA(1)
  }

  private var tabs = mutableListOf("","")

  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    tabs.set(TabEntry.CODE.value, assets.open("ImmunityCheck-1.0.0.cql").bufferedReader().readText())
    tabs.set(TabEntry.DATA.value, assets.open("ImmunizationHistory.json").bufferedReader().readText())

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    binding.etTextEditor.setText(tabs.get(TabEntry.CODE.value))
    binding.edExpressionName.setText("CompletedImmunization")

    binding.btCompile.setOnClickListener {
      save(binding.tabLayout.selectedTabPosition)
      binding.tvResults.text = "Compiling..."
      binding.btCompile.isEnabled = false
      GlobalScope.launch(Dispatchers.IO) {
        val report = CqlBuildReporter().run(
          tabs.get(TabEntry.CODE.value),
          tabs.get(TabEntry.DATA.value),
          binding.edExpressionName.text.toString()
        )
        GlobalScope.launch(Dispatchers.Main) {
          binding.tvResults.text = report;
          binding.btCompile.isEnabled = true
        }
      }
    }

    binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) { load(tab!!.position) }
      override fun onTabUnselected(tab: TabLayout.Tab?) { save(tab!!.position) }
      override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
  }

  private fun load(tabPosition: Int) {
    binding.etTextEditor.setText(tabs.get(tabPosition))
  }

  private fun save(tabPosition: Int) {
    tabs.set(tabPosition, binding.etTextEditor.text.toString());
  }
}