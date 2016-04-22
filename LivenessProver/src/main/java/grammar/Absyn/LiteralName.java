package grammar.Absyn; // Java Package generated by the BNF Converter.

public class LiteralName extends Name {
  public final String labelident_;

  public LiteralName(String p1) { labelident_ = p1; }

  public <R,A> R accept(grammar.Absyn.Name.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.LiteralName) {
      grammar.Absyn.LiteralName x = (grammar.Absyn.LiteralName)o;
      return this.labelident_.equals(x.labelident_);
    }
    return false;
  }

  public int hashCode() {
    return this.labelident_.hashCode();
  }


}
