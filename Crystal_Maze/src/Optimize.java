import java.io.*;
import java.util.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.constraints.IIntConstraintFactory;
public class Optimize {
    // number of meetings taking place
    int num_meetings;
    // number of people we are given information about
    int num_people;
    // number of slots we are supposed to fit the meetings into
    int num_slots;
    // the matrix containing the distances between the meeting spots
    int[][] meeting_distance;
    // the maximum distance between the pairs of cities
    int max_distance;
    // we need to see the timelimit
    int period;
    // the matrix keeping track of who attends which meeting
    int[][] attend_matrix;
    // Will use this to get the last value from while(solver.solve())
    int[] timing;
    // An array of length (number of meetings). ith element tells when the ith meeting takes place. These are the only constraint variables used.
    IntVar [] time;
    // need to minimize the maximum of the  time
    IntVar max_slots;
    Model model;
    Solver solver;
    // initation function, takes in input and stores number of meetings, number of people, number of slots.
    // Then stores a matrix which records who are attending which meeting. The dimensions are (number of people * number of meetings)
    // Then stores a matrix which records the distances between the meeting points. The dimensions are (number of meetings * number of meetings)
    public Optimize(String fname, String timelimit) throws IOException {
        max_distance=0;
        period=Integer.parseInt(timelimit);
        Scanner sc = new Scanner(new File(fname));
        // get the values
        num_meetings = sc.nextInt();
        num_people = sc.nextInt();
        num_slots = sc.nextInt();
        // had to declare them after getting the values of num_meetings, num_people.
        timing=new int[num_meetings];
        meeting_distance = new int[num_meetings][num_meetings];
        attend_matrix = new int[num_people][num_meetings];
        // getting the values in the matrix
        for (int i = 0; i < num_people; i++){
            // have to skip the first element of each row
            sc.next();
            for (int j = 0; j < num_meetings; j++) {
                attend_matrix[i][j] = sc.nextInt();
            }
        }
        // getting the values in the matrix
        for (int i = 0; i < num_meetings; i++){
            // have to skip the first element of each row
            sc.next();
            for (int j = 0; j < num_meetings; j++) {
                meeting_distance[i][j] = sc.nextInt();
                // keeping a track of the maximum distance between any pair of cities, will use this for an upper bound
                // for the values on time array and max_slots
                max_distance=Math.max(max_distance,meeting_distance[i][j]);
            }
        }
        sc.close();

    }
    void build(){
        //naming the model, is this needed?
        model = new Model("optimize");
        // initializing the solver
        solver = model.getSolver();
        if(period!=0){
            solver.limitTime(period*1000);
        }
        // initializing the contraint variables
        time =model.intVarArray("time",num_meetings,0,num_meetings*(max_distance+1));
        max_slots =model.intVar(0,num_meetings*(max_distance+1));
        for (int i = 0; i < num_meetings; i++) {
            // we are taking care of duplicate constraints by starting with j=i+1 instead of 0.
            for (int j = i + 1; j < num_meetings; j++) {
                // for a pair of meetings (i,j)  if there is a person who attends both then, the time
                // difference betwee when they take place needs to be > meeting_distance[i][j].
                // to avoid duplicate constraints we break off the loop there.
                for (int k = 0; k < num_people; k++) {
                    if (attend_matrix[k][i] == 1 && attend_matrix[k][j] == 1) {
                        model.distance(time[i], time[j], ">", meeting_distance[i][j]).post();
                        break;
                    }
                }
            }
        }
        //setting max_slots as the maximum of time array
        model.max(max_slots, time).post();
    }

    void solve() {
        // need the max_slots to be minimum, that is how the optimum is defined.
        model.setObjective(Model.MINIMIZE,max_slots);
        // the search methods used is inputOrderLBSearch.
        solver.setSearch(Search.inputOrderLBSearch(time));
        // keeps on modifying the best value we have
        while(solver.solve()){
            for (int i=0;i<num_meetings;i++){
                timing[i]=time[i].getValue();
            }
        }
        //prints out hte final value we get.
        for (int i=0;i<num_meetings;i++){
            System.out.print(i+" "+timing[i]+"\n");
        }
        System.out.println(solver.getMeasures());

    }
    public static void main(String args[]) throws IOException {
        //Take the name of the input file and use it to get the various details about the problem.
        String name=args[0];
        String time;
        if (args.length==1){
            time="0";
        }
        else{
            time=args[1];
        }
        Optimize optimize = new Optimize(name, time);
        //Makes the model, solver and gives our model the required constraints.
        optimize.build();
        //Solves/ searches for the min via the model with the help of constraints and the given searching order.
        optimize.solve();
    }
}
