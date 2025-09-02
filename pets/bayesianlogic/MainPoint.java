package pets.bayesianlogic;

import pets.bayesianlogic.except.BayesianLogicalException;
import pets.bayesianlogic.except.ProbabilityInputException;
import pets.bayesianlogic.stuff.BayesianBody;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainPoint {
    public static void main(String... args)
            throws FileNotFoundException, BayesianLogicalException, ProbabilityInputException {
        Path fp = Paths.get("pets\\bayesianlogic\\", "test_thesis.txt");
        BayesianBody mB = new BayesianBody(fp);

        mB.setDirectory(fp);
        mB.readThesis();

        fp = Paths.get(mB.getDirectory().getParent().toString(), "test_hypotheses.txt");
        mB.setDirectory(fp);
        mB.readHypotheses();

        fp = Paths.get(mB.getDirectory().getParent().toString(), "test_facts.txt");
        mB.setDirectory(fp);
        mB.readFacts();

        fp = Paths.get(mB.getDirectory().getParent().toString(), "test_probabilities.txt");
        mB.setDirectory(fp);
        mB.readProbabilities();

        mB.getStringResult(true);
        System.out.println(mB.getCachedResultString());
        System.out.flush();
    }
}