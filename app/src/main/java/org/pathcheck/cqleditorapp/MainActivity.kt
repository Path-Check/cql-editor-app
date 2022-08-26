package org.pathcheck.cqleditorapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private lateinit var tvResults: TextView;
  private lateinit var edEditor: EditText;
  private lateinit var edExpression: EditText;
  private lateinit var btCompile: Button;
  private lateinit var tabLayout: TabLayout;

  enum class TabType (val value: Int) {
    CODE(0),
    DATA(1)
  }

  private var tabModel = mutableListOf("","")

  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    tvResults = findViewById(R.id.tvResults)
    edEditor = findViewById(R.id.etTextEditor)
    edExpression = findViewById(R.id.edExpressionName)
    btCompile = findViewById(R.id.btCompile)
    tabLayout = findViewById(R.id.tabLayout)

    tabModel.set(TabType.CODE.value, assets.open("ImmunityCheck-1.0.0.cql").bufferedReader().readText())
    tabModel.set(TabType.DATA.value, assets.open("ImmunizationHistory.json").bufferedReader().readText())

    edEditor.setText(tabModel.get(TabType.CODE.value))
    edExpression.setText("CompletedImmunization")

    btCompile.setOnClickListener {
      save(tabLayout.selectedTabPosition)
      tvResults.text = "Compiling..."
      btCompile.isEnabled = false
      GlobalScope.launch(Dispatchers.IO) {
        val report = CqlBuildReporter().run(
          tabModel.get(TabType.CODE.value),
          tabModel.get(TabType.DATA.value),
          edExpression.text.toString()
        )
        GlobalScope.launch(Dispatchers.Main) {
          tvResults.text = report;
          btCompile.isEnabled = true
        }
      }
    }

    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) { load(tab!!.position) }
      override fun onTabUnselected(tab: TabLayout.Tab?) { save(tab!!.position) }
      override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
  }

  private fun load(tabPosition: Int) {
    edEditor.setText(tabModel.get(tabPosition))
  }

  private fun save(tabPosition: Int) {
    tabModel.set(tabPosition, edEditor.text.toString());
  }
}