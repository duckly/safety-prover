package main;

import common.Utility;
import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import encoding.ISatSolverFactory;
import encoding.SatSolver;
import grammar.Yylex;
import grammar.parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import verification.FiniteStateSets;
import verification.IncrementalVerifier;
import verification.MonolithicVerifier;
import visitor.AllVisitorImpl;
import visitor.RegularModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final ISatSolverFactory SOLVER_FACTORY =
            //MinisatSolver.FACTORY;            // Minisat
            SatSolver.FACTORY;                // Sat4j
    //LingelingSolver.FACTORY;          // Lingeling

    private final static boolean verifySolutions = false;

    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No input, doing nothing");
            return;
        }

        String fileName = args[0];
        RegularModel problem = parse(fileName);

        if (problem.getLogLevel() <= 0)
            Configurator.setRootLevel(Level.ERROR);
        else if (problem.getLogLevel() == 1)
            Configurator.setRootLevel(Level.INFO);
        else
            Configurator.setRootLevel(Level.ALL);

        writeInputProblem(problem);

        determize(problem);

        //verifyFiniteInstances(problem, problem.getExplicitChecksUntilLength());

        if (false && problem.getCloseInitStates() && !problem.getAlwaysMonolithic()) {
            IncrementalVerifier verifier =
                    new IncrementalVerifier(problem, SOLVER_FACTORY,
                            problem.getUseRankingFunctions(),
                            problem.getPrecomputedInv(),
                            verifySolutions);
            verifier.setup();
            verifier.verify();
        } else {
            MonolithicVerifier verifier = new MonolithicVerifier
                    (problem, SOLVER_FACTORY, problem.getUseRankingFunctions());
            verifier.verify();
        }
    }

    public static RegularModel parse(String fileName) {
        RegularModel problem;
        try {
            problem = parseFromReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return problem;
    }

    private static RegularModel parseFromReader(Reader reader) {
        parser p;
        Yylex l = new Yylex(reader);
        p = new parser(l);

        try {
            grammar.Absyn.ModelRule parse_tree = p.pModelRule();

            RegularModel problem = new RegularModel();
            parse_tree.accept(new AllVisitorImpl(), problem);

            LOGGER.info("Parse Succesful!");
            return problem;
        } catch (Throwable e) {

            String error = ("At line " + String.valueOf(l.line_num()) + ", near \"" + l.buff() + "\" :\n") +
                    ("     " + e.getMessage());
            throw new RuntimeException(error);
        }
    }

    /**
     * Determinizes all components of a problem
     *
     * @param[in,out] problem  The problem to determinize
     */
    private static void determize(RegularModel problem) {
        EdgeWeightedDigraph player2 = problem.getPlayer2();
        if (!VerificationUltility.isDFA(player2, problem.getNumberOfLetters())) {
            player2 = VerificationUltility.toDFA(problem.getPlayer2(), problem.getNumberOfLetters());
            problem.setPlayer2(player2);
        }

        Automata I0 = problem.getI0();
        if (!I0.isDFA()) {
            I0 = AutomataConverter.toDFA(I0);
            problem.setI0(I0);
        }

        Automata F = problem.getF();
        if (!F.isDFA()) {
            F = AutomataConverter.toDFA(F);
            problem.setF(F);
        }
    }

    public static void verifyFiniteInstances(RegularModel problem, int sizeBound) {
        final FiniteStateSets finiteStates =
                new FiniteStateSets(problem.getNumberOfLetters(),
                        problem.getI0(), problem.getF(),
                        problem.getPlayer2(),
                        problem.getLabelToIndex());
        for (int s = 0; s <= sizeBound; ++s) {
            System.out.println("Verifying system instance for length " + s + " ... ");
//	    finiteStates.verifyInstance(s, problem.getCloseInitStates());
            finiteStates.verifyInstanceSymbolically(s, problem.getCloseInitStates());
        }
    }

    public static void writeInputProblem(RegularModel problem) {
        try {
            Utility.writeOut(Utility.toDot(problem.getI0(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonI0.dot");
            Utility.writeOut(Utility.toDot(problem.getF(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonF.dot");
//            Utility.writeOut(Utility.toDot(problem.getPlayer1(),
//                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP1.dot");
            Utility.writeOut(Utility.toDot(problem.getPlayer2(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP2.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

// vim: tabstop=4
