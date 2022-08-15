package org.pathcheck.cqleditorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import org.cqframework.cql.cql2elm.CqlCompiler
import org.cqframework.cql.cql2elm.CqlCompilerException
import org.cqframework.cql.cql2elm.LibraryBuilder
import org.cqframework.cql.cql2elm.LibraryManager
import org.cqframework.cql.cql2elm.ModelManager
import org.pathcheck.cqleditorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.etCode.setText(assets.open("ImmunityCheck-1.0.0.cql").bufferedReader().readText())
    binding.btCompile.setOnClickListener { compile() }
  }

  @OptIn(ExperimentalTime::class)
  private fun compile() {
    val (compiler, elapsed) = measureTimedValue {
      val modelManager = ModelManager()
      val libraryManager = LibraryManager(modelManager)

      val compiler = CqlCompiler(modelManager, libraryManager)
      compiler.run(
        binding.etCode.text.toString(),
        CqlCompilerException.ErrorSeverity.Info,
        LibraryBuilder.SignatureLevel.All
      )
      compiler
    }

    binding.tvResults.text = report(compiler, elapsed)
  }

  private fun report(compiler: CqlCompiler, elapsed: Duration): String {
    val results = StringBuilder()
    results.appendLine("Compiled with ${compiler.errors.size} errors in ${elapsed.inWholeMilliseconds/1000.0f} seconds\n")

    compiler.errors.forEach {
      results.appendLine("[${it.locator.startLine}] ${it.message}")
    }

    return results.toString()
  }
}