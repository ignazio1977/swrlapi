package org.swrlapi.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.swrlapi.builtins.SWRLBuiltInLibraryManager;
import org.swrlapi.core.arguments.SWRLBuiltInArgument;
import org.swrlapi.core.arguments.SWRLBuiltInArgumentFactory;
import org.swrlapi.exceptions.BuiltInException;
import org.swrlapi.exceptions.SWRLBuiltInBridgeException;
import org.swrlapi.exceptions.SWRLRuleEngineBridgeException;
import org.swrlapi.exceptions.TargetRuleEngineException;
import org.swrlapi.ext.OWLDatatypeFactory;
import org.swrlapi.ext.OWLLiteralFactory;
import org.swrlapi.ext.SWRLAPILiteralFactory;
import org.swrlapi.ext.SWRLAPIOWLDataFactory;
import org.swrlapi.ext.SWRLAPIOWLOntology;
import org.swrlapi.owl2rl.OWL2RLPersistenceLayer;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.SQWRLResultGenerator;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

/**
 * Default implementation of a SWRL rule engine bridge, built-in bridge, built-in bridge controller, and rule engine
 * bridge controller.
 * <p>
 * Asserted OWL axioms are managed by a {@link SWRLRuleEngine}, which passes them to a {@link TargetRuleEngine} using
 * the {@link TargetRuleEngine#defineOWLAxiom(OWLAxiom)} call.
 */
public class DefaultSWRLBridge implements SWRLRuleEngineBridge, SWRLBuiltInBridge, SWRLBuiltInBridgeController,
		SWRLRuleEngineBridgeController
{
	private final SWRLAPIOWLOntology swrlapiOWLOntology;
	private final OWLClassExpressionResolver classExpressionResolver;
	private final OWLPropertyExpressionResolver propertyExpressionResolver;
	private final OWL2RLPersistenceLayer owl2RLPersistenceLayer;

	/**
	 * The target rule engine implementation (e.g., Drools, Jess)
	 */
	private TargetRuleEngine targetRuleEngine;

	/**
	 * OWL axioms inferred by a rule engine (via the {@link #inferOWLAxiom()} call). A {@link SWRLRuleEngine} can retrieve
	 * these using the the {@link #getInjectedOWLAxioms()} call after calling {@link TargetRuleEngine#runRuleEngine()}.
	 */
	private final Set<OWLAxiom> inferredOWLAxioms;

	/**
	 * OWL axioms inferred by SWRL built-ins (via the {@link #injectOWLAxiomCall}). A {@link SWRLRuleEngine} can retrieve
	 * these using the {@link #getInjectedOWLAxioms()} call after calling {@link TargetRuleEngine#runRuleEngine()}.
	 */
	private final Set<OWLAxiom> injectedOWLAxioms;

	public DefaultSWRLBridge(SWRLAPIOWLOntology swrlapiOWLOntology, OWL2RLPersistenceLayer owl2RLPersistenceLayer)
			throws SWRLBuiltInBridgeException
	{
		this.swrlapiOWLOntology = swrlapiOWLOntology;
		this.owl2RLPersistenceLayer = owl2RLPersistenceLayer;
		this.targetRuleEngine = null;

		this.classExpressionResolver = new OWLClassExpressionResolver();
		this.propertyExpressionResolver = new OWLPropertyExpressionResolver();

		this.inferredOWLAxioms = new HashSet<OWLAxiom>();
		this.injectedOWLAxioms = new HashSet<OWLAxiom>();

		resetController();
	}

	@Override
	public void setTargetRuleEngine(TargetRuleEngine targetRuleEngine)
	{
		this.targetRuleEngine = targetRuleEngine;
	}

	@Override
	public void resetController() throws SWRLBuiltInBridgeException
	{
		this.inferredOWLAxioms.clear();
		this.injectedOWLAxioms.clear();

		SWRLBuiltInLibraryManager.invokeAllBuiltInLibrariesResetMethod(this);
	}

	@Override
	public boolean hasOntologyChanged()
	{
		return getOWLOntology().hasOntologyChanged();
	}

	/**
	 * The inject methods can be used by SWRL built-ins to inject new axioms into a bridge, which will also reflect them
	 * in the underlying engine.
	 */
	@Override
	public void injectOWLAxiom(OWLAxiom axiom) throws SWRLBuiltInBridgeException
	{
		if (!this.injectedOWLAxioms.contains(axiom)) {
			this.injectedOWLAxioms.add(axiom);
			exportOWLAxiom(axiom); // Export the axiom to the rule engine.
		}
	}

	@Override
	public OWLIRIResolver getOWLIRIResolver()
	{
		return this.swrlapiOWLOntology.getOWLIRIResolver();
	}

	@Override
	public OWLClassExpressionResolver getOWLClassExpressionResolver()
	{
		return this.classExpressionResolver;
	}

	@Override
	public OWLPropertyExpressionResolver getOWLPropertyExpressionResolver()
	{
		return this.propertyExpressionResolver;
	}

	@Override
	public OWL2RLPersistenceLayer getOWL2RLPersistenceLayer()
	{
		return this.owl2RLPersistenceLayer;
	}

	@Override
	public Set<OWLAxiom> getInjectedOWLAxioms()
	{
		return new HashSet<OWLAxiom>(this.injectedOWLAxioms);
	}

	@Override
	public int getNumberOfInjectedOWLAxioms()
	{
		return this.injectedOWLAxioms.size();
	}

	@Override
	public boolean isInjectedOWLAxiom(OWLAxiom axiom)
	{
		return this.injectedOWLAxioms.contains(axiom);
	}

	@Override
	public Set<OWLAxiom> getInferredOWLAxioms()
	{
		return this.inferredOWLAxioms;
	}

	@Override
	public int getNumberOfInferredOWLAxioms()
	{
		return this.inferredOWLAxioms.size();
	}

	@Override
	public void inferOWLAxiom(OWLAxiom axiom) throws SWRLRuleEngineBridgeException
	{
		if (!this.inferredOWLAxioms.contains(axiom) && !getSWRLAPIOntologyProcessor().hasAssertedOWLAxiom(axiom)) // Exclude
																																																							// already
			// asserted axioms
			this.inferredOWLAxioms.add(axiom);
	}

	@Override
	public List<List<SWRLBuiltInArgument>> invokeSWRLBuiltIn(String ruleName, String builtInName, int builtInIndex,
			boolean isInConsequent, List<SWRLBuiltInArgument> arguments) throws BuiltInException
	{
		return SWRLBuiltInLibraryManager.invokeSWRLBuiltIn(this, ruleName, builtInName, builtInIndex, isInConsequent,
				arguments);
	}

	public boolean isOWLClass(IRI classIRI)
	{
		return this.swrlapiOWLOntology.containsClassInSignature(classIRI, true);
	}

	public boolean isOWLObjectProperty(IRI propertyIRI)
	{
		return this.swrlapiOWLOntology.containsObjectPropertyInSignature(propertyIRI, true);
	}

	public boolean isOWLDataProperty(IRI propertyIRI)
	{
		return this.swrlapiOWLOntology.containsDataPropertyInSignature(propertyIRI, true);
	}

	public boolean isOWLNamedIndividual(IRI individualIRI)
	{
		return this.swrlapiOWLOntology.containsIndividualInSignature(individualIRI, true);
	}

	@Override
	public SWRLAPIOWLOntology getOWLOntology()
	{
		return this.swrlapiOWLOntology;
	}

	public SQWRLResult getSQWRLResult(String queryName) throws SQWRLException
	{
		return this.getSWRLAPIOntologyProcessor().getSQWRLResult(queryName);
	}

	@Override
	public SQWRLResultGenerator getSQWRLResultGenerator(String queryName) throws SQWRLException
	{
		return this.getSWRLAPIOntologyProcessor().getSQWRLResultGenerator(queryName);
	}

	private void exportOWLAxiom(OWLAxiom axiom) throws SWRLBuiltInBridgeException
	{
		try {
			this.targetRuleEngine.defineOWLAxiom(axiom);
		} catch (TargetRuleEngineException e) {
			throw new SWRLBuiltInBridgeException("error exporting OWL axiom " + axiom + " to target rule engine: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public SWRLAPIOWLDataFactory getOWLDataFactory()
	{
		return this.getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory();
	}

	@Override
	public OWLLiteralFactory getOWLLiteralFactory()
	{
		return getSWRLAPIOWLDataFactory().getOWLLiteralFactory();
	}

	@Override
	public OWLDatatypeFactory getOWLDatatypeFactory()
	{
		return getSWRLAPIOWLDataFactory().getOWLDatatypeFactory();
	}

	@Override
	public SWRLAPILiteralFactory getSWRLAPILiteralFactory()
	{
		return getSWRLAPIOWLDataFactory().getSWRLAPILiteralFactory();
	}

	@Override
	public SWRLBuiltInArgumentFactory getSWRLBuiltInArgumentFactory()
	{
		return getSWRLAPIOWLDataFactory().getSWRLBuiltInArgumentFactory();
	}

	private SWRLAPIOWLDataFactory getSWRLAPIOWLDataFactory()
	{
		return getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory();
	}

	private SWRLAPIOWLOntology getSWRLAPIOWLOntology()
	{
		return swrlapiOWLOntology;
	}

	private SWRLAPIOntologyProcessor getSWRLAPIOntologyProcessor()
	{
		return getSWRLAPIOWLOntology().getSWRLAPIOntologyProcessor();
	}
}