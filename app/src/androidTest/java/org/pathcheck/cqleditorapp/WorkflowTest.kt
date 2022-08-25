package org.pathcheck.cqleditorapp

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.delete
import com.google.android.fhir.workflow.FhirOperator
import java.io.InputStream
import kotlinx.coroutines.runBlocking
import org.cqframework.cql.cql2elm.LibraryContentType
import org.hl7.fhir.r4.model.Attachment
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Library
import org.hl7.fhir.r4.model.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkflowTest {
  private lateinit var fhirEngine: FhirEngine
  private lateinit var fhirOperator: FhirOperator

  private val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)
  private val jsonParser = fhirContext.newJsonParser()

  private fun open(assetName: String): InputStream? {
    return javaClass.getResourceAsStream(assetName)
  }

  private fun load(asset: String): Bundle {
    return jsonParser.parseResource(open(asset)) as Bundle
  }

  @Before
  fun setUp() = runBlocking {
    fhirEngine = FhirEngineProvider.getInstance(ApplicationProvider.getApplicationContext())
    fhirOperator = FhirOperator(fhirContext, fhirEngine)

    val patientImmunizationHistory = load("/ImmunizationHistory.json")
    for (entry in patientImmunizationHistory.entry) {
      fhirEngine.remove(entry.resource.javaClass, entry.resource.id)
      fhirEngine.create(entry.resource)
    }
  }

  @Test
  fun compileEvaluateTest() = runBlocking {
    val immunizationCheckCode = open("/ImmunityCheck-1.0.0.cql")!!.bufferedReader().readText()

    fhirOperator.loadLib(assembleFhirLib(immunizationCheckCode, "ImmunityCheck", "1.0.0"))
    val results = fhirOperator.evaluateLibrary(
      "http://localhost/Library/ImmunityCheck|1.0.0",
      "d4d35004-24f8-40e4-8084-1ad75924514f",
      setOf("CompletedImmunization")
    ) as Parameters

    println(results.getParameterBool("CompletedImmunization"));
  }

  private fun assembleFhirLib(cqlText: String, libName: String, libVersion: String): Library {
    val attachment =
      Attachment().apply {
        contentType = LibraryContentType.CQL.mimeType()
        data = cqlText.toByteArray()
      }

    return Library().apply {
      id = "$libName-$libVersion"
      name = libName
      version = libVersion
      status = Enumerations.PublicationStatus.ACTIVE
      experimental = true
      url = "http://localhost/Library/$libName|$libVersion"
      addContent(attachment)
    }
  }
}