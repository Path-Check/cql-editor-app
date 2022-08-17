package org.pathcheck.cqleditorapp

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.cqframework.cql.elm.execution.Library
import org.hl7.fhir.instance.model.api.IBaseBundle
import org.opencds.cqf.cql.engine.data.CompositeDataProvider
import org.opencds.cqf.cql.engine.data.DataProvider
import org.opencds.cqf.cql.engine.execution.Context
import org.opencds.cqf.cql.engine.fhir.model.R4FhirModelResolver
import org.opencds.cqf.cql.evaluator.engine.retrieve.BundleRetrieveProvider

/**
 * Evaluates a CQL expression on the DDCC Composite
 */
class SimpleCQLEvaluator {
    class CachedR4FhirModelResolver : R4FhirModelResolver(FhirContext.forCached(FhirVersionEnum.R4))

    private fun loadDataProvider(assetBundle: IBaseBundle): DataProvider {
        val bundleRetrieveProvider = BundleRetrieveProvider(FhirContext.forR4Cached(), assetBundle)
        return CompositeDataProvider(CachedR4FhirModelResolver(), bundleRetrieveProvider)
    }

    fun prepare(library: Library, assetBundle: IBaseBundle): Context {
        val context = Context(library)
        context.setExpressionCaching(true)
        context.registerDataProvider("http://hl7.org/fhir", loadDataProvider(assetBundle))
        return context
    }

    fun resolve(expression: String, library: Library, asset: IBaseBundle): Any? {
        val context = prepare(library, asset)
        return context.resolveExpressionRef(expression).evaluate(context)
    }
}