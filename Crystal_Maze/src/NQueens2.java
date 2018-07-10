//
// place n non-attacking queens on an n by n chessboard
// Change order of making decisions
// All Different!
//
// NOTE: CP "hello world"
//
import java.io.*;
import java.util.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.IIntConstraintFactory.*;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.exception.ContradictionException;

public class NQueens2 {

    public static void main(String[] args) {

        int n         = Integer.parseInt(args[0]);
        Model model   = new Model("nqueens");
        Solver solver = model.getSolver();
        IntVar[] q    = model.intVarArray("queen",n,0,n-1);

        //
        // columns constraint ... allDiff!
        //
        model.allDifferent(q).post();

        //
        // diagonal constraint
        //
        for (int i=0;i<n-1;i++)
            for (int j=i+1;j<n;j++)
                model.distance(q[i],q[j],"!=",Math.abs(i-j)).post();

        solver.setSearch(Search.minDomLBSearch(q)); // fail-first
        boolean solved = solver.solve();

        if (solved){
            for (int i=0;i<n;i++){
                for (int j=0;j<q[i].getValue();j++) System.out.print(".");
                System.out.print("Q");
                for (int j=q[i].getValue();j<n;j++) System.out.print(".");
                System.out.println();
            }
        }
        System.out.println(solver.getMeasures());
    }
}
