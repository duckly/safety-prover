package main;

import common.Utility;
import common.VerificationUltility;
import common.bellmanford.EdgeWeightedDigraph;
import common.finiteautomata.Automata;
import common.finiteautomata.AutomataConverter;
import common.finiteautomata.language.InclusionCheckingImpl;
import de.libalf.LibALFFactory;
import encoding.ISatSolverFactory;
import encoding.SatSolver;
import grammar.Yylex;
import grammar.parser;
import learning.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import verification.IncrementalVerifier;
import verification.MonolithicVerifier;
import visitor.AllVisitorImpl;
import visitor.RegularModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class Main {
//    private static final Logger LOGGER = LogManager.getLogger();

    private static final ISatSolverFactory SOLVER_FACTORY =
            //MinisatSolver.FACTORY;            // Minisat
            SatSolver.FACTORY;                // Sat4j
    //LingelingSolver.FACTORY;          // Lingeling

    private final static boolean verifySolutions = false;

    /// directory name of the output
    private final static String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No input, doing nothing.");
            return;
        }
        File in = new File(args[0]);
        File[] modelFiles;
        if (in.isFile()) {
            modelFiles = new File[]{in};
        } else if (in.isDirectory()) {
            modelFiles = in.listFiles();
        } else {
            System.out.println("Input model is not valid.");
            return;
        }
        for (File modelFile : modelFiles) {
            if (!modelFile.isFile()) continue;
            if (modelFile.getName().charAt(0) == '_') continue;
            System.out.println("Checking " + modelFile.getName() + "...");
            long startTime = System.nanoTime();
            checkModel(modelFile.getAbsolutePath());
            int elapsedTime = (int) ((System.nanoTime() - startTime) / 1e9);
            System.out.println("Elapsed time for " + modelFile.getName() + " : " + elapsedTime + " seconds.");
        }
    }

    static void checkModel(String fileName) {
        RegularModel problem = parse(fileName);

        if (problem.getLogLevel() <= 0)
            Configurator.setRootLevel(Level.ERROR);
        else if (problem.getLogLevel() == 1)
            Configurator.setRootLevel(Level.INFO);
        else
            Configurator.setRootLevel(Level.ALL);

        writeInputProblem(problem);

        determize(problem);

        Automata lang = VerificationUltility.getIntersection(problem.getB(), problem.getI());
        List<Integer> cex = AutomataConverter.getSomeWord(lang);
        if (cex != null) {
            System.out.println("VERDICT: Bad configurations intersect initial configurations.");
            return;
        }

        if (problem.getPrecomputedInv()) {
            Automata invariant;
            if (true) {
                //Learner learner = new LStarLearner();
                Learner learner = new LibALFLearner(LibALFFactory.Algorithm.ANGLUIN);
                Teacher teacher = new BasicRMCTeacher(problem.getNumberOfLetters(),
                        problem.getI(), problem.getB(), problem.getT());
                invariant = MonolithicLearning.inferWith(learner, teacher);
            } else {
                invariant = findReachabilitySet(problem.getI(), problem.getT());
            }
            //invariant = AutomataConverter.toMinimalDFA(invariant);
            invariant = AutomataConverter.pruneUnreachableStates(invariant);
            Map<Integer, String> indexToLabel = problem.getIndexToLabel();
            System.err.print("\nL-star successfully found an invariant!\n");
            System.out.println("VERDICT: Bad configurations are not reachable from any " +
                    (problem.getCloseInitStates() ? "reachable" : "initial") + " configuration.");
            System.out.println();
            System.out.println("// Configurations visited in the game are contained in");
            System.out.println(invariant.prettyPrint("Invariant", indexToLabel));
        } else if (problem.getCloseInitStates() && !problem.getAlwaysMonolithic()) {
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

    static Automata findReachabilitySet(Automata I, EdgeWeightedDigraph T) {
        Automata reachable = I;
        Automata newConfig = reachable;
        while (true) {
            Automata post = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getImage(newConfig, T));

            newConfig = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getDifference(post, reachable));

            LOGGER.debug("reachable " + reachable.getStates().length + ", new " + newConfig.getStates().length);
            //LOGGER.debug("reachable:\n" + reachable);
            //LOGGER.debug("new:\n" + newConfig);

            if (new InclusionCheckingImpl().isSubSetOf(
                    newConfig, AutomataConverter.toCompleteDFA(reachable)))
                break;

            reachable = AutomataConverter.minimiseAcyclic(
                    VerificationUltility.getUnion(reachable, post));
        }
        return reachable;
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
        EdgeWeightedDigraph T = problem.getT();

        if (!VerificationUltility.isDFA(T, problem.getNumberOfLetters())) {
            T = VerificationUltility.toDFA(problem.getT(), problem.getNumberOfLetters());
            problem.setT(T);
        }

        Automata I = problem.getI();
        if (!I.isDFA()) {
            I = AutomataConverter.toDFA(I);
            problem.setI(I);
        }

        Automata B = problem.getB();
        if (!B.isDFA()) {
            B = AutomataConverter.toDFA(B);
            problem.setB(B);
        }
    }

    public static void writeInputProblem(RegularModel problem) {
        try {
            Utility.writeOut(Utility.toDot(problem.getI(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonI0.dot");
            Utility.writeOut(Utility.toDot(problem.getB(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/automatonF.dot");
//            Utility.writeOut(Utility.toDot(problem.getPlayer1(),
//                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP1.dot");
            Utility.writeOut(Utility.toDot(problem.getT(),
                    problem.getLabelToIndex()), OUTPUT_DIR + "/transducerP2.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

// vim: tabstop=4
