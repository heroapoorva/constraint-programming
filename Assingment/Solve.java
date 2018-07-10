import java.io.*;
import java.util.*;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.IIntConstraintFactory;
import org.chocosolver.solver.constraints.ISetConstraintFactory;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.solver.constraints.IIntConstraintFactory.*;
import org.chocosolver.solver.search.strategy.Search;

public class Solve {
// number of meetings taking place
    int num_meetings;
    // number of people we are given information about
    int num_people;
    // number of slots we are supposed to fit the meetings into
    int num_slots;
    // the matrix containing the distances between the meeting spots
    int[][] meeting_distance;
    // the maximum distance between the pairs of cities, i.e. max element of matrix meeting_distance
    int max_distance;
    // the matrix keeping track of who attends which meeting
    int[][] attend_matrix;
    // An array of length (number of meetings). ith element tells when the ith meeting takes place. These are the only constraint variables used.
    IntVar [] time;
    Model model;
    Solver solver;
    // initation function, takes in input and stores number of meetings, number of people, number of slots.
    // Then stores a matrix which records who are attending which meeting. The dimensions are (number of people * number of meetings)
    // Then stores a matrix which records the distances between the meeting points. The dimensions are (number of meetings * number of meetings)
    public Solve(String fname) throws IOException {
        max_distance=0;
        Scanner sc = new Scanner(new File(fname));
        // get the values
        num_meetings = sc.nextInt();
        num_people = sc.nextInt();
        num_slots = sc.nextInt();
        // had to declare them after getting the values of num_meetings, num_people.
        meeting_distance = new int[num_meetings][num_meetings];
        //meeting_distance[i][j]= k means that distance between ith and jth meeting points is k units
        attend_matrix = new int[num_people][num_meetings];
        // attend_matrix[i][j]=1 means person i is going to attend meeting j, 0 otherwise
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
                // keeping a track of the maximum distance between any pair of cities, will use this in a heuristic.
                max_distance=Math.max(max_distance,meeting_distance[i][j]);
            }
        }
        sc.close();

    }
    void build(){
        //naming the model, is this needed?
        model = new Model("problem solver");
        // initializing the solver
        solver = model.getSolver();
        // initializing the contraint variables
        time =model.intVarArray("time",num_meetings,0,num_slots-1);
        if(num_slots>= num_meetings*(max_distance+1)){
            // so we have that the number of slots given is so big that, people can attend ith meeting,
            // then take max_distance time and move to their next meeting.
            for(int i=0;i<num_meetings-1;i++){
                // so the contraints are that we do the first meeting on ith day, then 2nd meeting on i+max_distance day,
                // so on. So the numbered meetings happen later and happen max_distance days apart.
                model.max(time[i+1],time[i],time[i+1]).post();
                model.distance(time[i], time[i+1], ">", max_distance).post();
            }
        }
        else {
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
        }
    }

    void solve() {
        // the search methods used is inputOrderLBSearch.
        solver.setSearch(Search.inputOrderLBSearch(time));
        // to print out the solutions nicely need this i
        int i=0;
        // if we got a solution
        if (solver.solve()) {
            // this is the method we have to use to get the value(getValue!)
            for (IntVar v : time) {
                System.out.print( i +" "+ (v.getValue()) +"\n");
                i++;
            }
        }
        // if no solution is found, need more days.
        else{
            System.out.print( "Sorry, no solution! Need more days.\n");
        }
    }
    public static void main(String args[]) throws IOException {
        //Take the name of the input file and use it to get the various details about the problem.
        Solve problem = new Solve(args[0]);
        //Makes the model, solver and gives our model the required constraints.
        problem.build();
        //Solves the model along with the constraints via the given searching order.
        problem.solve();
    }
}
