package grammar.Absyn; // Java Package generated by the BNF Converter.

public abstract class AutomatonRule implements java.io.Serializable {
  public abstract <R,A> R accept(AutomatonRule.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(grammar.Absyn.Automaton p, A arg);

  }

}