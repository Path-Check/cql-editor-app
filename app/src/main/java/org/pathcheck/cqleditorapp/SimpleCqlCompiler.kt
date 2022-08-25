package org.pathcheck.cqleditorapp

import org.cqframework.cql.cql2elm.CqlCompiler
import org.cqframework.cql.cql2elm.LibraryManager
import org.cqframework.cql.cql2elm.ModelManager

class SimpleCqlCompiler {
  fun run(code: String): CqlCompiler {
    val modelManager = ModelManager()
    val libraryManager = LibraryManager(modelManager)
    val compiler = CqlCompiler(modelManager, libraryManager)

    compiler.run(code)

    return compiler
  }
}