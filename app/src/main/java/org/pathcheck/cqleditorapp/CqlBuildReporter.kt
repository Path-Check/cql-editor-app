package org.pathcheck.cqleditorapp

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.DataFormatException
import com.fasterxml.jackson.core.JsonParseException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import org.cqframework.cql.cql2elm.CqlCompiler
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.opencds.cqf.cql.evaluator.engine.elm.LibraryMapper

class CqlBuildReporter {
  @OptIn(ExperimentalTime::class)
  fun run(code: String, data: String, expression: String): String {
    val (context, loadTime) = measureTimedValue {
      FhirContext.forCached(FhirVersionEnum.R4)
    }

    val (compiler, compileTime) = measureTimedValue {
      SimpleCqlCompiler().run(code)
    }

    if (!compiler.errors.isEmpty()) {
      return reportCompilationError(compiler)
    }

    val (execLibrary, mapTime) = measureTimedValue {
      LibraryMapper.INSTANCE.map(compiler.library)
    }

    val (bundle, dataTime) = measureTimedValue {
      try {
        FhirContext.forR4Cached().newJsonParser().parseResource(data) as IBaseBundle
      } catch (e: DataFormatException) {
        return reportParseException(e.cause as JsonParseException)
      }
    }

    val (evalContext, prepTime) = measureTimedValue {
      SimpleCQLEvaluator().prepare(execLibrary, bundle)
    }

    val (result, evaluateTime) = measureTimedValue {
      evalContext.resolveExpressionRef(expression).evaluate(evalContext)
    }

    return reportSuccess(
      loadTime, compileTime, mapTime, dataTime, prepTime, evaluateTime, result
    )
  }

  private fun reportSuccess(load: Duration, compile: Duration, map: Duration, data: Duration, prep: Duration, evaluate: Duration, result: Any): String {
    return buildString {
      appendLine("Compiled successfully: Result $result")
      appendLine("  Fhir Context  ${load.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Compile       ${compile.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Map           ${map.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Parse Bundle  ${data.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  ModelResolver ${prep.inWholeMilliseconds / 1000.0f} seconds")
      appendLine("  Evaluate      ${evaluate.inWholeMilliseconds / 1000.0f} seconds")
    }
  }

  private fun reportCompilationError(compiler: CqlCompiler): String {
    return buildString {
      appendLine("Compiled with ${compiler.errors.size} errors")
      compiler.errors.forEach {
        appendLine("  [${it.locator.startLine}] ${it.message}")
      }
    }
  }

  private fun reportParseException(exception: JsonParseException): String {
    return buildString {
      appendLine("Data bundle exception: ")
      appendLine("  [${exception.location.lineNr}] ${exception.message}")
    }
  }
}