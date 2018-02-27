package org.swrlapi.factory;

import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentType;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentVisitor;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentVisitorEx;
import org.swrlapi.builtins.arguments.SWRLDataPropertyExpressionBuiltInArgument;
import org.swrlapi.exceptions.SWRLBuiltInException;

class DefaultSWRLDataPropertyExpressionBuiltInArgument extends DefaultSWRLBuiltInArgument
  implements SWRLDataPropertyExpressionBuiltInArgument
{
  private static final long serialVersionUID = 1L;

  private final OWLDataPropertyExpression propertyExpression;

  @Override
  public Stream<?> components() {
      return Stream.of(propertyExpression, getBoundVariableName());
  }

  @Override
  public int hashIndex() {
      return 190683;
  }

  @Override
  public int typeIndex() {
      return 196008;
  }

  @Override
  public int initHashCode() {
    int hash = hashIndex();
    hash = OWLObject.hashIteration(hash, propertyExpression.hashCode());
    return OWLObject.hashIteration(hash, getBoundVariableName().hashCode());
  }

  public DefaultSWRLDataPropertyExpressionBuiltInArgument(@NonNull OWLDataPropertyExpression propertyExpression)
  {
    this.propertyExpression = propertyExpression;
  }

  @NonNull @Override public SWRLBuiltInArgumentType<?> getSWRLBuiltInArgumentType()
  {
    return SWRLBuiltInArgumentType.DATA_PROPERTY_EXPRESSION;
  }

  @NonNull @Override public OWLDataPropertyExpression getOWLDataPropertyExpression()
  {
    return this.propertyExpression;
  }

  @NonNull @Override public <@NonNull T> T accept(@NonNull SWRLBuiltInArgumentVisitorEx<@NonNull T> visitor)
  {
    return visitor.visit(this);
  }

  @NonNull @Override public SWRLDataPropertyExpressionBuiltInArgument asSWRLDataPropertyExpressionBuiltInArgument()
    throws SWRLBuiltInException
  {
    return this;
  }

  @Override public void accept(@NonNull SWRLBuiltInArgumentVisitor visitor)
  {
    visitor.visit(this);
  }
}
