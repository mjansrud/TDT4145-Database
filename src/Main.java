import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class Main {

    //Constants
    public static final int WORKOUT_INDOOR = 0;
    public static final int WORKOUT_OUTDOOR = 1;

    //Global variables

    //Help functions
    public static void createInstance(){

        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println("Driver loaded");
        } catch (Exception ex) {
            System.out.println("Failed to load driver");
        }

    }

    public static Connection createConnection(){

        try {
            System.out.println("Database connected");
            return DriverManager.getConnection("jdbc:mysql://87.76.28.159/utviklin_school?" + "user=utviklin_user&password=1h5Pv9ay1Y");
        } catch (SQLException ex) {
            System.out.println("Failed to connect to DB");
            printExeption(ex);
        }

        return null;
    }

    public static Statement createStatement(Connection conn){

        try {
            System.out.println("Statement created");
            return conn.createStatement();
        } catch (SQLException ex) {
            System.out.println("Failed to connect to DB");
            printExeption(ex);
        }

        return null;
    }

    public static ResultSet getWorkouts(Statement stmt){
        try {
            System.out.println("Fetching workouts");
            return stmt.executeQuery("SELECT * FROM Workout JOIN ;
        } catch (SQLException ex) {
            System.out.println("Could not fetch workouts");
            printExeption(ex);
        }
        return null;
    }


    public static void printWorkouts(ResultSet workouts){
        try {
            System.out.println("Printing workouts");
            while(workouts.next())
            {
                System.out.println(workouts.getString("WorkoutID") + " - Navn: " + workouts.getString("Name") + " - Tid: " + readableTimestamp(workouts.getTimestamp("Time")) + " - Varighet: " + workouts.getString("Duration") + " minutter - Type: " + workouts.getString("Template"));
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static void closeStatement(Statement stmt ){
        try {
            System.out.println("Closing statement");
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("Could not close statement");
            printExeption(ex);
        }

    }

    public static void closeConnection(Connection conn){
        try {
            System.out.println("Closing connection");
            conn.close();
        } catch (SQLException ex) {
            System.out.println("Could not close connection");
            printExeption(ex);
        }
    }

    //Main
    public static void main(String[] args) {

        //Instance
        createInstance();
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);

        //Main features
        ResultSet workouts = getWorkouts(stmt);
        printWorkouts(workouts);

        //Exit functions
        closeStatement(stmt);
        closeConnection(conn);

    }

    //Extra functions
    public static void printExeption(SQLException ex){
        // handle any errors
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }

    public static String readableTimestamp(Timestamp timestamp){
        return new SimpleDateFormat("dd/MM HH:mm").format(timestamp).toString();
    }

}
