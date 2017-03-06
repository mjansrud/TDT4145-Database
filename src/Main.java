import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Scanner;


public class Main {

    //Constants
    public static final int WORKOUT_INDOOR = 0;
    public static final int WORKOUT_OUTDOOR = 1;
    public static final int EXERCISE_CARDIO = 0;
    public static final int EXERCISE_STRENGTH = 1;
    public static final int EXERCISE_ENDURANCE = 2;

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
            return DriverManager.getConnection("jdbc:mysql://87.76.28.159/utviklin_school?" + "user=utviklin_user&password=" + System.getenv("DTD4145_SERVER_PASSWORD"));
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

    public static void createWorkout(Statement stmt, Scanner reader){

        //Bugfix
        reader.nextLine();

        System.out.println("Tast inn navn på treningen ");
        String name = reader.nextLine();

        System.out.println("Tast inn tid (Format: 2017-03-15 18:00:00)");
        String time = reader.nextLine();

        System.out.println("Tast inn varighet i timer (Format: 2017-03-15 18:00:00)");
        String duration = reader.nextLine();

        System.out.println("Er treningen en mal? (Nei: 0, Ja:1)");
        int template = reader.nextInt();

        System.out.println("Er treningen inne eller ute? (Inne: " + WORKOUT_INDOOR + ", ute: " + WORKOUT_OUTDOOR + ")");
        int type = reader.nextInt();

        String SQL = ("INSERT INTO Workout (Name, Time, Duration, Template, Type) values ('" + name + "', '" + time + "', " + duration + ", " + template + ", " + type + ")");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet trening");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create workout");
            printExeption(ex);
        }

    }

    public static void createExercise(Statement stmt, Scanner reader){

        //Bugfix
        reader.nextLine();

        System.out.println("Tast inn navn på øvelsen");
        String name = reader.nextLine();

        System.out.println("Tast inn beskrivelse av øvelsen");
        String description = reader.nextLine();

        System.out.println("Hvilken type er øvelsen? (Cardio: " + EXERCISE_CARDIO + ", styrke: " + EXERCISE_STRENGTH + ", utholdenhet: " + EXERCISE_ENDURANCE + ")");
        int type = reader.nextInt();

        String SQL = ("INSERT INTO Exercise (Name, description, type) values ('" + name + "', '" + description + "', " + type + ")");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet øvelse");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create exercise");
            printExeption(ex);
        }

    }

    public static ResultSet getWorkouts(Statement stmt){
        try {
            //1System.out.println("Fetching workouts");
            return stmt.executeQuery("SELECT * FROM Workout ");
        } catch (SQLException ex) {
            System.out.println("Could not fetch workouts");
            printExeption(ex);
        }
        return null;
    }


    public static void printWorkouts(ResultSet workouts){
        try {
            //System.out.println("Printing workouts");
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

    public static void printMenu(){
        System.out.println("-------------------");
        System.out.println("Velg et alternativ:");
        System.out.println("1) Skriv ut treninger");
        System.out.println("2) Opprett ny trening");
        System.out.println("3) Opprett ny øvelse");
        System.out.println("-------------------");
    }

    //Main
    public static void main(String[] args) {

        //Variables
        Boolean active = true;
        Scanner reader = new Scanner(System.in);

        //Instance
        createInstance();
        Connection conn = createConnection();
        Statement stmt = createStatement(conn);


        while(active){
            printMenu();
            System.out.println("Tast et nummer: ");
            int alternative = reader.nextInt();
            System.out.println("-------------------");
            switch(alternative){
                case 1:
                    ResultSet workouts = getWorkouts(stmt);
                    printWorkouts(workouts);
                    break;
                case 2:
                    createWorkout(stmt, reader);
                    break;
                case 3:
                    createExercise(stmt, reader);
                    break;
                default:
                    System.out.println("Ugyldig handling");

            }
        }

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
