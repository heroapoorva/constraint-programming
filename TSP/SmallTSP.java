import java.io.*;
import java.util.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.solver.constraints.IIntConstraintFactory.*;
import org.chocosolver.solver.search.strategy.Search;

public class SmallTSP{
    int n;                 // number of visits
    int[][] distance;      // distance between locations
    int[] flatDistance;    // flattened distance array
    int maxDistance;       // longest inter-city distance
    IntVar[] succ;         // succ[i] = j <-> visit city j immediately after city i
    IntVar[] edgeDistance; // edgeDistance[i] = distance[i][succ[i]]
    IntVar[] index;        // index[i] = i*n+succ[i], used to access flatDistance
    IntVar tourLength;     // sum of edge distances
    Model model;
    Solver solver;    
    
    public SmallTSP(String fname) throws IOException {
	maxDistance = 0;
        Scanner sc = new Scanner(new File(fname));
        n = sc.nextInt();
	distance = new int[n][n];
	for (int i=0;i<n;i++)
	    for (int j=0;j<n;j++){
		distance[i][j] = sc.nextInt();
		maxDistance = Math.max(maxDistance,distance[i][j]);
	    }
	sc.close();
        flatDistance = ArrayUtils.flatten(distance); // flatten the distance array
    }
    
    void build(){
	model        = new Model("small tsp");
	solver       = model.getSolver();
        succ         = model.intVarArray("succ",n,0,n-1);
	edgeDistance = model.intVarArray("edgeDist",n,0,maxDistance);
	tourLength   = model.intVar("tourLength",0,maxDistance*n);
	index        = model.intVarArray("index",n,0,n*n-1);
	
	model.circuit(succ).post();
	
	for (int i=0;i<n;i++){
	    model.arithm(index[i],"=",succ[i],"+",i*n).post();
	    model.element(edgeDistance[i],flatDistance,index[i]).post();
	}
	
	model.sum(edgeDistance,"=",tourLength).post(); // tour length is sum of edges in tour
    }
    
    void solve(){
        model.setObjective(Model.MINIMIZE,tourLength);   
        solver.setSearch(Search.minDomLBSearch(succ));
        while(solver.solve()){
	    System.out.print("cost: "+ tourLength.getValue() +"  tour: ");
	    for (IntVar v : succ) System.out.print((1 + v.getValue()) +" ");
	    System.out.println();
	}
    }

    public static void main(String args[]) throws IOException {
	SmallTSP tsp = new SmallTSP(args[0]);
	tsp.build();
	tsp.solve();
    }
}
	    
