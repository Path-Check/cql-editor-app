package org.pathcheck.cqleditorapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.InputStream
import org.cqframework.cql.cql2elm.CqlCompiler
import org.cqframework.cql.cql2elm.CqlCompilerException
import org.cqframework.cql.cql2elm.LibraryBuilder
import org.cqframework.cql.cql2elm.LibraryManager
import org.cqframework.cql.cql2elm.ModelManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompileNoFhirTest {
  private fun open(assetName: String): InputStream? {
    return javaClass.getResourceAsStream(assetName)
  }

  @Test
  fun compileTest() {
    val modelManager = ModelManager()
    val libraryManager = LibraryManager(modelManager)

    val compiler = CqlCompiler(modelManager, libraryManager)
    compiler.run(
      open("/StringOperators.cql"),
      CqlCompilerException.ErrorSeverity.Info,
      LibraryBuilder.SignatureLevel.All
    )

    compiler.errors.forEach {
      println(it.message);
    }

    assertTrue(compiler.errors.isEmpty())
  }
}