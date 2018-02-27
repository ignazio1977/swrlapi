package org.swrlapi.factory;

import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentType;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentVisitor;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgumentVisitorEx;
import org.swrlapi.builtins.arguments.SWRLObjectPropertyExpressionBuiltInArgument;
import org.swrlapi.exceptions.SWRLBuiltInException;

class DefaultSWRLObjectPropertyExpressionBuiltInArgument extends DefaultSWRLBuiltInArgument
  implements SWRLObjectPropertyExpressionBuiltInArgument
{
  private static final long serialVersionUID = 1L;

  private final OWLObjectPropertyExpression propertyExpression;

  @Override
  public Stream<?> components() {
      return Stream.of(propertyExpression, getBoundVariableName());
  }

  @Override
  public int hashIndex() {
      return 170683;
  }

  @Override
  public int typeIndex() {
      return 176008;
  }

  @Override
  public int initHashCode() {
    int hash = hashIndex();
    hash = OWLObject.hashIteration(hash, propertyExpression.hashCode());
    return OWLObject.hashIteration(hash, getBoundVariableName().hashCode());
  }

  public DefaultSWRLObjectPropertyExpressionBuiltInArgument(@NonNull OWLObjectPropertyExpression propertyExpression)
  {
    this.propertyExpression = propertyExpression;
  }

  @NonNull @Override public SWRLBuiltInArgumentType<?> getSWRLBuiltInArgumentType()
  {
    return SWRLBuiltInArgumentType.OBJECT_PROPERTY_EXPRESSION;
  }

  @NonNull @Override public OWLObjectPropertyExpression getOWLObjectPropertyExpression()
  {
    return this.propertyExpression;
  }

  @NonNull @Override public <@NonNull T> T accept(@NonNull SWRLBuiltInArgumentVisitorEx<@NonNull T> visitor)
  {
    return visitor.visit(this);
  }

  @NonNull @Override public SWRLObjectPropertyExpressionBuiltInArgument asSWRLObjectPropertyExpressionBuiltInArgument()
    throws SWRLBuiltInException
  {
    return this;
  }

  @Override public void accept(@NonNull SWRLBuiltInArgumentVisitor visitor)
  {
    visitor.visit(this);
  }
}
