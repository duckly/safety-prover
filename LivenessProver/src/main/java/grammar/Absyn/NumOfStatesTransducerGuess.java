package grammar.Absyn; // Java Package generated by the BNF Converter.

public class NumOfStatesTransducerGuess extends VerifierOption {
  public final Integer integer_1, integer_2;

  public NumOfStatesTransducerGuess(Integer p1, Integer p2) { integer_1 = p1; integer_2 = p2; }

  public <R,A> R accept(grammar.Absyn.VerifierOption.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.NumOfStatesTransducerGuess) {
      grammar.Absyn.NumOfStatesTransducerGuess x = (grammar.Absyn.NumOfStatesTransducerGuess)o;
      return this.integer_1.equals(x.integer_1) && this.integer_2.equals(x.integer_2);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.integer_1.hashCode())+this.integer_2.hashCode();
  }


}