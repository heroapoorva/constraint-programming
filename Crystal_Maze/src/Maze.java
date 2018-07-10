import java.io.*;
import java.util.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.IIntConstraintFactory.*;


public class Maze {


    public static void main(String args[]) throws IOException {
        Scanner sc    = new Scanner(new File("cm8.txt"));
        int n         = sc.nextInt(); // vertices
        Model model   = new Model("maze");
        Solver solver = model.getSolver();
        IntVar[] v    = model.intVarArray("v",n,1,n); // n variables, values 1 to n

        while (sc.hasNext()){
            int i = sc.nextInt();
            int j = sc.nextInt();
            model.distance(v[i],v[j],">",1).post(); // adjacent -> not consecutive
        }
        sc.close();

        model.allDifferent(v).post();

        System.out.println(solver.solve());
        for (int i=0;i<n;i++) System.out.println(v[i].getValue());
        System.out.println("nodes: " + solver.getMeasures().getNodeCount() +
                "   cpu: " + solver.getMeasures().getTimeCount());
    }
}
