package org.pathcheck.cqleditorapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import org.cqframework.cql.cql2elm.CqlCompiler
import org.cqframework.cql.cql2elm.CqlCompilerException
import org.cqframework.cql.cql2elm.LibraryBuilder
import org.cqframework.cql.cql2elm.LibraryManager
import org.cqframework.cql.cql2elm.ModelManager

class MainActivity : AppCompatActivity() {
  lateinit var tvResults: TextView;
  lateinit var etCode: EditText;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    tvResults = findViewById(R.id.tvResults)
    etCode = findViewById(R.id.etCode)

    val button: Button = findViewById(R.id.btCompile)
    button.setOnClickListener {
      compile();
    }
  }

  override fun onResume() {
    super.onResume()
    etCode.setText(assets.open("ImmunityCheck-1.0.0.cql").bufferedReader().readText());
  }

  fun compile() {
    val start = System.currentTimeMillis();

    val modelManager = ModelManager()
    val libraryManager = LibraryManager(modelManager)

    val compiler = CqlCompiler(modelManager, libraryManager)
    compiler.run(
      etCode.text.toString(),
      CqlCompilerException.ErrorSeverity.Info,
      LibraryBuilder.SignatureLevel.All
    )

    val timeSpent = (System.currentTimeMillis()-start)/1000.0f;

    val results = StringBuilder()
    results.appendLine("Compiled with ${compiler.errors.size} errors in ${timeSpent} seconds\n")

    compiler.errors.forEach {
      results.appendLine("[${it.locator.getStartLine()}] ${it.message}");
    }

    tvResults.setText(results.toString())
  }
}