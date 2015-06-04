package org.swrlapi.factory;

import checkers.nullness.quals.NonNull;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.swrlapi.resolvers.OWLDataPropertyExpressionResolver;

import java.util.HashMap;
import java.util.Map;

public class DefaultOWLDataPropertyExpressionResolver implements OWLDataPropertyExpressionResolver
{
  @NonNull private final Map<String, OWLDataPropertyExpression> id2OWLPropertyExpression;
  @NonNull private final Map<OWLDataPropertyExpression, String> owlPropertyExpression2ID;

  public DefaultOWLDataPropertyExpressionResolver()
  {
    this.id2OWLPropertyExpression = new HashMap<>();
    this.owlPropertyExpression2ID = new HashMap<>();
  }

  @Override public void reset()
  {
    this.id2OWLPropertyExpression.clear();
    this.owlPropertyExpression2ID.clear();
  }

  @Override public void record(@NonNull String propertyExpressionID,
      @NonNull OWLDataPropertyExpression propertyExpression)
  {
    this.id2OWLPropertyExpression.put(propertyExpressionID, propertyExpression);
    this.owlPropertyExpression2ID.put(propertyExpression, propertyExpressionID);
  }

  @Override public boolean records(@NonNull OWLDataPropertyExpression propertyExpression)
  {
    return this.owlPropertyExpression2ID.containsKey(propertyExpression);
  }

  @Override @NonNull public String resolve(@NonNull OWLDataPropertyExpression propertyExpression)
  {
    if (this.owlPropertyExpression2ID.containsKey(propertyExpression))
      return this.owlPropertyExpression2ID.get(propertyExpression);
    else
      throw new IllegalArgumentException("no ID found for data property expression " + propertyExpression);
  }

  @Override @NonNull public OWLDataPropertyExpression resolve(@NonNull String propertyExpressionID)
  {
    if (this.id2OWLPropertyExpression.containsKey(propertyExpressionID))
      return this.id2OWLPropertyExpression.get(propertyExpressionID);
    else
      throw new IllegalArgumentException("no data property expression found with ID " + propertyExpressionID);
  }
}
