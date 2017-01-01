package grammar.Absyn; // Java Package generated by the BNF Converter.

public class SymmetryOptions extends VerifierOption {
  public final ListSymmetryOption listsymmetryoption_;
  public SymmetryOptions(ListSymmetryOption p1) { listsymmetryoption_ = p1; }

  public <R,A> R accept(grammar.Absyn.VerifierOption.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.SymmetryOptions) {
      grammar.Absyn.SymmetryOptions x = (grammar.Absyn.SymmetryOptions)o;
      return this.listsymmetryoption_.equals(x.listsymmetryoption_);
    }
    return false;
  }

  public int hashCode() {
    return this.listsymmetryoption_.hashCode();
  }


}