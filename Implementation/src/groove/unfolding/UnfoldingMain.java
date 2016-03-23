package groove.unfolding;

import static groove.io.FileType.GRAMMAR;
import groove.transform.Proof;
import groove.unfolding.dependency.DepUTIL;
import groove.unfolding.dependency.DependencyStructure;

import groove.unfolding.tree.Tree;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class that calls the process steps of the attack tree generation approach
 * First the unfolding is constructed, then the attacks are extracted 
 * and converted into an attack tree.
 */
public class UnfoldingMain {

    public static void main(String[] args) {
        try {


            /************************************************************
             *** Step 1: Construct the Unfolding
             ************************************************************/

    		Unfolder unfolder = new Unfolder(new File(GRAMMAR.addExtension(args[0])));	
    
    		long startTime = System.nanoTime();
    		
    		Unfolding unfolding = unfolder.constructUnfolding();     

    		System.out.println("That took " + (System.nanoTime() - startTime )/1000000 + " milliseconds");

            /************************************************************
             *** Step 2: Given the unfolding, extract the attacks
             ************************************************************/

            ArrayList<Proof> matches = unfolding.extractRecordedRules(unfolder.getAttackerGoalCondition());

            System.out.println("===== LIST OF ALL ATTACKS=====");
            // Print out all the possible attacks.
            DepUTIL.depedenciesToAttacks(matches);

//          Optional: Remove all attacks that contain cycles.
//            DepUTIL.removeCycles(matches);

            System.out.println("That took " + (System.nanoTime() - startTime )/1000000 + " milliseconds");

            /************************************************************
             *** Step 3: Construct a (compact) attack tree.
             ************************************************************/

            // Construct the combined dependency structure based on all individual attacks.
            DependencyStructure depStruct = new DependencyStructure(matches);

            
            if(matches.size()<1){
            	System.out.println("No attacks found");
            } else {
            	
                String rootString = DepUTIL.proofToString(matches.get(0));

                // Optinal: Covert the DependencyStructure to a DAG structure
                // depStruct.DependencyTreeToDAG(rootString);

                // Construct an actual tree for a given DependencyStructure
                Tree tree2 = DepUTIL.depedencyStructToTree(depStruct, rootString);

                System.out.println("===== ADTOOL XML OF TREE =====");

                DepUTIL.treeToXML(tree2);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
