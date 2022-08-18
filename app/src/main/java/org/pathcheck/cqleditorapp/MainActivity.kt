package org.pathcheck.cqleditorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.DataFormatException
import com.fasterxml.jackson.core.JsonParseException
import com.google.android.material.tabs.TabLayout
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import org.cqframework.cql.cql2elm.CqlCompiler
import org.cqframework.cql.cql2elm.LibraryManager
import org.cqframework.cql.cql2elm.ModelManager
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.opencds.cqf.cql.evaluator.engine.elm.LibraryMapper
import org.pathcheck.cqleditorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  enum class TabEntry (val value: Int) {
    CODE(0),
    DATA(1)
  }

  private var tabs = mutableListOf("","")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    tabs.set(TabEntry.CODE.value, assets.open("ImmunityCheck-1.0.0.cql").bufferedReader().readText())
    tabs.set(TabEntry.DATA.value, assets.open("ImmunizationHistory.json").bufferedReader().readText())

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

    binding.etTextEditor.setText(tabs.get(TabEntry.CODE.value))
    binding.edExpressionName.setText("CompletedImmunization")

    binding.btCompile.setOnClickListener {
      save(binding.tabLayout.selectedTabPosition)
      compile()
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

  @OptIn(ExperimentalTime::class)
  private fun compile() {
    val (context, loadTime) = measureTimedValue {
      FhirContext.forCached(FhirVersionEnum.R4)
    }

    val (compiler, compileTime) = measureTimedValue {
      val modelManager = ModelManager()
      val libraryManager = LibraryManager(modelManager)
      val compiler = CqlCompiler(modelManager, libraryManager)
      compiler.run(tabs.get(TabEntry.CODE.value))
      compiler
    }

    if (!compiler.errors.isEmpty()) {
      reportError(compiler)
      return;
    }

    val (execLibrary, mapTime) = measureTimedValue {
      LibraryMapper.INSTANCE.map(compiler.library)
    }

    val (bundle, dataTime) = measureTimedValue {
      try {
        FhirContext.forR4Cached().newJsonParser().parseResource(
          tabs.get(TabEntry.DATA.value)
        ) as IBaseBundle
      } catch (e: DataFormatException) {
        binding.tvResults.text = reportException(e.cause as JsonParseException)
        return;
      }
    }

    val (evalContext, prepTime) = measureTimedValue {
      SimpleCQLEvaluator().prepare(execLibrary, bundle)
    }

    val (result, evaluateTime) = measureTimedValue {
      evalContext
        .resolveExpressionRef(binding.edExpressionName.text.toString())
        .evaluate(evalContext)
    }

    binding.tvResults.text = reportSucess(compiler, loadTime, compileTime, mapTime, dataTime, prepTime, evaluateTime, result)
  }

  private fun reportSucess(compiler: CqlCompiler, load: Duration, compile: Duration, map: Duration, data: Duration, prep: Duration, evaluate: Duration, result: Any): String {
    return buildString {
      appendLine("Compiled sucessfully: Result $result")
      appendLine("  Fhir Context  ${load.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Compile       ${compile.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Map           ${map.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Parse Bundle  ${data.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  ModelResolver ${prep.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Evaluate      ${evaluate.inWholeMilliseconds / 1000.0f} seconds")
    }
  }

  private fun reportError(compiler: CqlCompiler): String {
    return buildString {
      appendLine("Compiled with ${compiler.errors.size} errors")
      compiler.errors.forEach {
        appendLine("  [${it.locator.startLine}] ${it.message}")
      }
    }
  }

  private fun reportException(exception: JsonParseException): String {
    return buildString {
      appendLine("Exception Raised")
      appendLine("  [${exception.location.lineNr}] ${exception.message}")
    }
  }
}