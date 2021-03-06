package grammar.Absyn; // Java Package generated by the BNF Converter.

public class Model extends ModelRule {
  public final AutomatonRule automatonrule_1, automatonrule_2;
  public final MaybeClosed maybeclosed_;
  public final TransducerRule transducerrule_;
  public final ListVerifierOption listverifieroption_;
  public Model(AutomatonRule p1, MaybeClosed p2, TransducerRule p3, AutomatonRule p4, ListVerifierOption p5) { automatonrule_1 = p1; maybeclosed_ = p2; transducerrule_ = p3; automatonrule_2 = p4; listverifieroption_ = p5; }

  public <R,A> R accept(grammar.Absyn.ModelRule.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof grammar.Absyn.Model) {
      grammar.Absyn.Model x = (grammar.Absyn.Model)o;
      return this.automatonrule_1.equals(x.automatonrule_1) && this.maybeclosed_.equals(x.maybeclosed_) && this.transducerrule_.equals(x.transducerrule_) && this.automatonrule_2.equals(x.automatonrule_2) && this.listverifieroption_.equals(x.listverifieroption_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(37*(37*(37*(this.automatonrule_1.hashCode())+this.maybeclosed_.hashCode())+this.transducerrule_.hashCode())+this.automatonrule_2.hashCode())+this.listverifieroption_.hashCode();
  }


}
