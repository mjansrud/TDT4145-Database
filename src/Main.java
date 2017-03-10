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
    public static final int NOT_SPECIFIED = -1;

    //Workout types
    public static final int WORKOUT_INDOOR = 0;
    public static final int WORKOUT_OUTDOOR = 1;

    //Exercise types
    public static final int EXERCISE_CARDIO = 0;
    public static final int EXERCISE_STRENGTH = 1;
    public static final int EXERCISE_ENDURANCE = 2;

    //Fetch exercise actions
    public static final int EXERCISES_ALL = 0;
    public static final int EXERCISES_BY_ID = 1;
    public static final int EXERCISES_BY_WORKOUT = 2;

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

    public static void createResult(Statement stmt, Scanner reader){

        printWorkouts(getWorkouts(stmt));
        System.out.println("-------------------");
        System.out.println("Velg trening");
        int workout = reader.nextInt();

        printExercises(getExercises(stmt, EXERCISES_BY_WORKOUT, workout));
        System.out.println("-------------------");
        System.out.println("Velg øvelse");
        int exercise = reader.nextInt();

        //Bugfix
        reader.nextLine();

        //printPersons(getPersons(stmt));
        System.out.println("Skriv inn personnummer til medlem");
        String SSN = reader.nextLine();

        String SQL = ("INSERT INTO Result (WorkoutID, ExerciseID, SSN) values (" + workout + ", " + exercise + ", '" + SSN + "')");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet resultat");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create result");
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

    public static ResultSet getExercises(Statement stmt, int action, int id){
        try {
            //1System.out.println("Fetching exercises");
            if (action == EXERCISES_BY_WORKOUT && id > 0){
                return stmt.executeQuery("SELECT * FROM WorkoutHasExercise JOIN Workout ON WorkoutHasExercise.WorkoutID = Workout.WorkoutID JOIN Exercise ON WorkoutHasExercise.ExerciseID = Exercise.ExerciseID");
            } else if (action == EXERCISES_BY_ID && id > 0){
                return stmt.executeQuery("SELECT * FROM Exercise WHERE ExerciseID = " + id);
            }else{
                return stmt.executeQuery("SELECT * FROM Exercise");
            }
        } catch (SQLException ex) {
            System.out.println("Could not fetch workouts");
            printExeption(ex);
        }
        return null;
    }

    public static ResultSet getResults(Statement stmt){
        try {
            //1System.out.println("Fetching results");
            return stmt.executeQuery("SELECT * FROM Result JOIN Workout ON Result.WorkoutID = Workout.WorkoutID JOIN Exercise ON Result.ExerciseID = Exercise.ExerciseID JOIN Person ON Person.SSN = Result.SSN");
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
                System.out.println(workouts.getString("WorkoutID") + " - " + workouts.getString("Name") + " - Tid: " + readableTimestamp(workouts.getTimestamp("Time")) + " - Varighet: " + workouts.getString("Duration") + " minutter - Type: " + workouts.getString("Type"));
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static void printExercises(ResultSet exercises){
        try {
            //System.out.println("Printing workouts");
            while(exercises.next())
            {
                System.out.println(exercises.getString("ExerciseID") + " - " + exercises.getString("Name") + " - " +  exercises.getString("Description") + " - Type: " + exercises.getInt("Type"));
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static void printResults(ResultSet results){
        try {
            //System.out.println("Printing workouts");
            while(results.next())
            {
                System.out.println(results.getString("Person.Name") + " - " + results.getString("Workout.Name") + " - " + results.getString("Exercise.Name"));
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    //This is the goal method
    public static void createGoal(Statement stmt, Scanner reader){

        /*
        printWorkouts(getWorkouts(stmt));
        System.out.println("-------------------");
        System.out.println("Velg trening");
        int workout = reader.nextInt();

        printExercises(getExercises(stmt, EXERCISES_BY_WORKOUT, workout));
        System.out.println("-------------------");
        System.out.println("Velg øvelse");
        int exercise = reader.nextInt();
        */
        //Bugfix
        reader.nextLine();

        //printPersons(getPersons(stmt));
        System.out.println("Skriv inn personnummer til medlem");
        String SSN = reader.nextLine();

        System.out.println("Skriv inn fra dato på formatet 2017-03-15 18:00:00");
        String fromDate = reader.nextLine();

        System.out.println("Skriv inn til dato på formatet 2017-03-15 18:00:00");
        String toDate = reader.nextLine();

        String SQL = ("INSERT INTO Goal (SSN, FromDate, ToDate) values (" + SSN + ", '" + fromDate + "', '" + toDate + "')");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet goal");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create goal");
            printExeption(ex);
        }
    }


//This is the getGoals-method
    public static ResultSet getGoals(Statement stmt) {
        try {
            System.out.println("Fetching results");
            return stmt.executeQuery("SELECT * FROM Goal JOIN Person ON Person.SSN = Goal.SSN");

        } catch (SQLException ex) {
            System.out.println("Could not fetch workouts");
            printExeption(ex);
        }
        return null;
    }

        //This is goal methods print
    public static void printGoals(ResultSet goals){
        try {
            System.out.println("Printing workouts");
            while(goals.next())
            {
                System.out.println("Personnummer: " + goals.getString("Person.Name") + "Fra dato: " + goals.getString("FromDate") + "Til dato " + goals.getString("ToDate"));
                System.out.println("Dette er en test");
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static ResultSet getPersons(Statement stmt){
        try {
            //1System.out.println("Fetching workouts");
            return stmt.executeQuery("SELECT * FROM Person ");
        } catch (SQLException ex) {
            System.out.println("Could not fetch persons");
            printExeption(ex);
        }
        return null;
    }

    public static void createPerson(Statement stmt, Scanner reader){

        //Bugfix
        reader.nextLine();

        System.out.println("Tast inn personnummer");
        String ssn = reader.nextLine();

        System.out.println("Tast inn navn");
        String person_name = reader.nextLine();

        System.out.println("Tast inn telefonnummeret");
        String phonenumber = reader.nextLine();

        System.out.println("Tast inn email");
        String email = reader.nextLine();

        String SQL = ("INSERT INTO Person (SSN, Name, Telephone, Email) values ('" + ssn + "', + '" + person_name + "', '" + phonenumber + "', '" + email + "')");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet nytt medlem");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create member");
            printExeption(ex);
        }

    }

    public static void printPersons(ResultSet persons){
        try {
            //System.out.println("Printing workouts");
            while(persons.next())
            {
                System.out.println("Personnummer: " + persons.getString("SSN") + " - Navnr: " + persons.getString("Name") + " - Telefonnummer: " + persons.getString("Telephone") + " - Mail: " + persons.getString("Email"));
            }

        } catch (SQLException ex) {
            System.out.println("Could not print members");
            printExeption(ex);
        }

    }

    public static void createCategory(Statement stmt, Scanner reader){

        reader.nextLine();

        System.out.println("Tast inn navn på kategorien");
        String category_name = reader.nextLine();

        String SQL = ("INSERT INTO Category (Name) values ('" + category_name + "')");
        //System.out.println(SQL);

        try {
            System.out.println("Opprettet ny kategori");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create category");
            printExeption(ex);
        }
    }

    public static ResultSet getCategory(Statement stmt){
        try {
            //1System.out.println("Fetching workouts");
            return stmt.executeQuery("SELECT * FROM Category ");
        } catch (SQLException ex) {
            System.out.println("Could not fetch persons");
            printExeption(ex);
        }
        return null;
    }

    public static void printCategory(ResultSet category){
        try {
            //System.out.println("Printing workouts");
            while(category.next())
            {
                System.out.println(  " Category " + category.getString("Name") + " Similar " + category.getString("Similar"));
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
        System.out.println("1) Skriv ut medlemmer");
        System.out.println("2) Skriv ut treninger");
        System.out.println("3) Skriv ut øvelser");
        System.out.println("4) Skriv ut resultater");
        System.out.println("5) Skriv ut kategorier");
        System.out.println("6) Skriv ut mål");
        System.out.println("7) Opprett nytt medlem");
        System.out.println("8) Opprett ny trening");
        System.out.println("9) Opprett ny øvelse");
        System.out.println("10) Opprett nytt resultat");
        System.out.println("11) Opprett ny kategori");
        System.out.println("12) Opprett nytt mål");
        System.out.println("13) Generer rapport");
        System.out.println("14) Generer statistikk");
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
                    printPersons(getPersons(stmt));
                    break;
                case 2:
                    printWorkouts(getWorkouts(stmt));
                    break;
                case 3:
                    printExercises(getExercises(stmt, EXERCISES_ALL, -1));
                    break;
                case 4:
                    printResults(getResults(stmt));
                    break;
                case 5:
                    printCategory(getCategory(stmt));
                    break;
                case 6:
                    printGoals(getGoals(stmt));
                    break;
                case 7:
                    createPerson(stmt, reader);
                    break;
                case 8:
                    createWorkout(stmt, reader);
                    break;
                case 9:
                    createExercise(stmt, reader);
                    break;
                case 10:
                    createResult(stmt, reader);
                    break;
                case 11:
                    createCategory(stmt, reader);
                    break;
                case 12:
                    createGoal(stmt, reader);
                    break;
                case 13:
                    //createReport(stmt, reader);
                    break;
                case 14:
                    //createStatistics(stmt, reader);
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
