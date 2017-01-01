package grammar.Absyn; // Java Package generated by the BNF Converter.

public class NumberName extends Name {
  public final String myinteger_;
  public NumberName(String p1) { myinteger_ = p1; }

  public <R,A> R accept(grammar.Absyn.Name.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.NumberName) {
      grammar.Absyn.NumberName x = (grammar.Absyn.NumberName)o;
      return this.myinteger_.equals(x.myinteger_);
    }
    return false;
  }

  public int hashCode() {
    return this.myinteger_.hashCode();
  }


}
