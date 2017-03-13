import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.io.IOException;
import java.util.logging.SimpleFormatter;

public class Main {

    //Constants
    public static final int ID_NOT_SPECIFIED = -1;

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

    //Fetch exercise actions
    public static final int GOALS_ALL = 0;
    public static final int GOALS_BY_ID = 1;

    //Type
    public static final int FETCH_RESULT = 0;
    public static final int FETCH_GOAL = 1;

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
            //System.out.println("Statement created");
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

    public static void createResult(Connection conn, Statement stmt, Scanner reader){

        //Bugfix
        reader.nextLine();

        printPersons(getPersons(stmt));
        System.out.println("-------------------");
        System.out.println("Skriv inn personnummer til medlem");
        String SSN = reader.nextLine();

        printWorkouts(conn, getWorkouts(stmt));
        System.out.println("-------------------");
        System.out.println("Velg trening");
        int workout = reader.nextInt();

        printExercises(getExercises(stmt, EXERCISES_BY_WORKOUT, workout));
        System.out.println("-------------------");
        System.out.println("Velg øvelse");
        int exercise = reader.nextInt();

        String SQL = ("INSERT INTO Result (WorkoutID, ExerciseID, SSN) values (" + workout + ", " + exercise + ", '" + SSN + "')");

        //Create the general result
        int inserted_id = 0;
        try {
            System.out.println("Opprettet resultat");
            stmt.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                inserted_id = keys.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println("Could not create result");
            printExeption(ex);
        }

        //Analyse the exercise - Create SQL based on type
        ResultSet type = getExercises(stmt, GOALS_BY_ID, exercise);
        try {
            while(type.next()) {
                switch (type.getInt("type")) {
                    case EXERCISE_CARDIO:

                        System.out.println("Hva løftet du? (Heltall KG)");
                        int cardio_load = reader.nextInt();

                        System.out.println("Hvor mange repetisjoner?");
                        int cardio_repetitions = reader.nextInt();

                        System.out.println("Hvor mange sett?");
                        int cardio_sets = reader.nextInt();

                        SQL = ("INSERT INTO Cardio (ResultID, LoadKG, Repetitions, Sets) values (" + inserted_id + ", " + exercise + ", " + cardio_load + ", " + cardio_repetitions + ", " + cardio_sets + ")");
                        break;
                    case EXERCISE_STRENGTH:

                        System.out.println("Hva løftet du? (Heltall KG)");
                        int strength_load = reader.nextInt();

                        System.out.println("Hvor mange repetisjoner?");
                        int strength_repetitions = reader.nextInt();

                        System.out.println("Hvor mange sett?");
                        int strength_sets = reader.nextInt();

                        SQL = ("INSERT INTO Strength (ResultID, LoadKG, Repetitions, Sets) values (" + inserted_id + ", " + strength_load + ", " + strength_repetitions + ", " + strength_sets + ")");
                        break;
                    case EXERCISE_ENDURANCE:

                        System.out.println("Hvor langt løp du?");
                        int endurance_length = reader.nextInt();

                        System.out.println("Hvor mange minutter?");
                        int endurance_minutes = reader.nextInt();

                        SQL = ("INSERT INTO Endurance (ResultID, Length, Minutes) values (" + inserted_id + ", " + endurance_length + ", " + endurance_minutes + ")");

                        break;
                }
            }

        } catch (SQLException ex) {
            System.out.println("Could not fetch exercise");
            printExeption(ex);
        }

        try {
            //System.out.println(SQL);
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create specific result based on type");
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
                return stmt.executeQuery("SELECT * FROM WorkoutHasExercise JOIN Workout ON WorkoutHasExercise.WorkoutID = Workout.WorkoutID JOIN Exercise ON WorkoutHasExercise.ExerciseID = Exercise.ExerciseID WHERE Workout.WorkoutID=" + id);
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

    public static ResultSet getCardio(Statement stmt, int type, int result){
        try {
            //1System.out.println("Fetching results");
            if (type == FETCH_RESULT) return stmt.executeQuery("SELECT * FROM Cardio WHERE ResultID = " + result);
            if (type == FETCH_GOAL) return stmt.executeQuery("SELECT * FROM Cardio WHERE GoalID = " + result);
        } catch (SQLException ex) {
            System.out.println("Could not fetch cardio results");
            printExeption(ex);
        }
        return null;
    }

    public static ResultSet getStrength(Statement stmt, int type, int result){
        try {
            //1System.out.println("Fetching results");
            if (type == FETCH_RESULT) return stmt.executeQuery("SELECT * FROM Strength WHERE ResultID = " + result);
            if (type == FETCH_GOAL) return stmt.executeQuery("SELECT * FROM Strength WHERE GoalID = " + result);
        } catch (SQLException ex) {
            System.out.println("Could not fetch strength results");
            printExeption(ex);
        }
        return null;
    }

    public static ResultSet getEndurance(Statement stmt, int type, int result){
        try {
            //1System.out.println("Fetching results");
            if (type == FETCH_RESULT) return stmt.executeQuery("SELECT * FROM Endurance WHERE ResultID = " + result);
            if (type == FETCH_GOAL) return stmt.executeQuery("SELECT * FROM Endurance WHERE GoalID = " + result);
        } catch (SQLException ex) {
            System.out.println("Could not fetch endurance results");
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


    public static void printWorkouts(Connection conn, ResultSet workouts){
        int count = 0;
        try {
            //System.out.println("Printing workouts");
            while(workouts.next())
            {
                ++count;
                System.out.println("--> Trening " + workouts.getInt("WorkoutID") + ", " + workouts.getString("Name") + " - Tid: " + readableTimestamp(workouts.getTimestamp("Time")) + " - Varighet: " + workouts.getString("Duration") + " minutter");
                printExercises(getExercises(createStatement(conn), EXERCISES_BY_WORKOUT, workouts.getInt("WorkoutID")));
            }
            if (count == 0) {
                System.out.println("Ingen treninger funnet");
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static void printExercises(ResultSet exercises){
        int count = 0;
        try {
            //System.out.println("Printing workouts");
            while(exercises.next())
            {
                ++count;
                System.out.println(exercises.getInt("ExerciseID") + ", " + exercises.getString("Exercise.Name") + " - " +  exercises.getString("Exercise.Description"));
            }
            if (count == 0) {
                System.out.println("Ingen øvelser funnet");
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    public static void printResults(Connection conn, ResultSet results){
        int count = 0;
        try {
            //System.out.println("Printing workouts");
            while(results.next())
            {
                ++count;
                System.out.println(results.getString("Person.Name") + " - " + results.getString("Workout.Name") + " - " + results.getString("Exercise.Name"));

                switch(results.getInt("Exercise.Type")){
                    case EXERCISE_CARDIO:
                        ResultSet cardio = getCardio(createStatement(conn), FETCH_RESULT, results.getInt("ResultID"));
                        cardio.next();
                        System.out.println("Vekt: " + cardio.getString("LoadKG") + " - Repitisjoner: " + cardio.getString("repetitions") + " - Set: " + cardio.getString("sets"));
                        break;
                    case EXERCISE_STRENGTH:
                        ResultSet strength = getStrength(createStatement(conn), FETCH_RESULT, results.getInt("ResultID"));
                        strength.next();
                        System.out.println("Vekt: " + strength.getString("LoadKG") + " - Repitisjoner: " + strength.getString("repetitions") + " - Set: " + strength.getString("sets"));
                        break;
                    case EXERCISE_ENDURANCE:
                        ResultSet endurance = getEndurance(createStatement(conn), FETCH_RESULT, results.getInt("ResultID"));
                        endurance.next();
                        System.out.println("Lengde: " + endurance.getString("Length") + " - Tid: " + endurance.getString("Minutes"));
                        break;
                }

            }
            if (count == 0) {
                System.out.println("Ingen resultater funnet");
            }

        } catch (SQLException ex) {
            System.out.println("Could not print workouts");
            printExeption(ex);
        }

    }

    //This is the goal method
    public static void createGoal(Statement stmt, Scanner reader){

        //Bugfix
        reader.nextLine();

        printPersons(getPersons(stmt));
        System.out.println("Skriv inn personnummer til medlem");
        String SSN = reader.nextLine();

        printExercises(getExercises(stmt, EXERCISES_ALL, ID_NOT_SPECIFIED));
        System.out.println("-------------------");
        System.out.println("Velg øvelse");
        int exercise = reader.nextInt();

        //Bugfix
        reader.nextLine();

        System.out.println("Skriv inn fra dato på formatet 2017-03-15 18:00:00");
        String fromDate = reader.nextLine();

        System.out.println("Skriv inn til dato på formatet 2017-03-15 18:00:00");
        String toDate = reader.nextLine();

        String SQL = ("INSERT INTO Goal (ExerciseID, SSN, FromDate, ToDate) values (" + exercise + ", '" + SSN + "', '" + fromDate + "', '" + toDate + "')");
        //System.out.println(SQL);

        //Create the general result
        int inserted_id = 0;
        try {
            System.out.println("Opprettet mål");
            stmt.executeUpdate(SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                inserted_id = keys.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println("Could not create result");
            printExeption(ex);
        }

        //Analyse the exercise - Create SQL based on type
        ResultSet type = getExercises(stmt, GOALS_BY_ID, exercise);
        try {
            while(type.next()) {
                switch (type.getInt("type")) {
                    case EXERCISE_CARDIO:

                        System.out.println("Hvilken vekt vil du oppnå? (Heltall KG)");
                        int cardio_load = reader.nextInt();

                        System.out.println("Hvor mange repetisjoner?");
                        int cardio_repetitions = reader.nextInt();

                        System.out.println("Hvor mange sett?");
                        int cardio_sets = reader.nextInt();

                        SQL = ("INSERT INTO Cardio (GoalID, LoadKG, Repetitions, Sets) values (" + inserted_id + ", " + exercise + ", " + cardio_load + ", " + cardio_repetitions + ", " + cardio_sets + ")");
                        break;
                    case EXERCISE_STRENGTH:

                        System.out.println("Hvilken vekt vil du oppnå? (Heltall KG)");
                        int strength_load = reader.nextInt();

                        System.out.println("Hvor mange repetisjoner?");
                        int strength_repetitions = reader.nextInt();

                        System.out.println("Hvor mange sett?");
                        int strength_sets = reader.nextInt();

                        SQL = ("INSERT INTO Strength (GoalID, LoadKG, Repetitions, Sets) values (" + inserted_id + ", " + strength_load + ", " + strength_repetitions + ", " + strength_sets + ")");
                        break;
                    case EXERCISE_ENDURANCE:

                        System.out.println("Hva er lengden du ønsker å oppnå?");
                        int endurance_length = reader.nextInt();

                        System.out.println("På hvor mange minutter?");
                        int endurance_minutes = reader.nextInt();

                        SQL = ("INSERT INTO Endurance (GoalID, Length, Minutes) values (" + inserted_id + ", " + endurance_length + ", " + endurance_minutes + ")");

                        break;
                }
            }

        } catch (SQLException ex) {
            System.out.println("Could not fetch exercise");
            printExeption(ex);
        }

        try {
            //System.out.println(SQL);
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Could not create specific result based on type");
            printExeption(ex);
        }

    }


    //This is the getGoals-method
    public static ResultSet getGoals(Statement stmt) {
        try {
            System.out.println("Fetching results");
            return stmt.executeQuery("SELECT * FROM Goal JOIN Exercise ON Goal.ExerciseID = Exercise.ExerciseID JOIN Person ON Person.SSN = Goal.SSN");

        } catch (SQLException ex) {
            System.out.println("Could not fetch workouts");
            printExeption(ex);
        }
        return null;
    }

        //This is goal methods print
    public static void printGoals(Connection conn, ResultSet goals){

        int count = 0;
        try {
            //System.out.println("Printing workouts");
            while(goals.next())
            {
                ++count;
                System.out.println(goals.getString("Person.Name") + ", Øvelse: " + goals.getString("Exercise.Name") + ", Fra dato: " + goals.getString("FromDate") + ", Til dato " + goals.getString("ToDate"));

                switch(goals.getInt("Exercise.Type")){
                    case EXERCISE_CARDIO:
                        ResultSet cardio = getCardio(createStatement(conn), FETCH_GOAL, goals.getInt("GoalID"));
                        cardio.next();
                        System.out.println("Vekt: " + cardio.getString("LoadKG") + " - Repitisjoner: " + cardio.getString("repetitions") + " - Set: " + cardio.getString("sets"));
                        break;
                    case EXERCISE_STRENGTH:
                        ResultSet strength = getStrength(createStatement(conn), FETCH_GOAL, goals.getInt("GoalID"));
                        strength.next();
                        System.out.println("Vekt: " + strength.getString("LoadKG") + " - Repitisjoner: " + strength.getString("repetitions") + " - Set: " + strength.getString("sets"));
                        break;
                    case EXERCISE_ENDURANCE:
                        ResultSet endurance = getEndurance(createStatement(conn), FETCH_GOAL, goals.getInt("GoalID"));
                        endurance.next();
                        System.out.println("Lengde: " + endurance.getString("Length") + " - Tid: " + endurance.getString("Minutes"));
                        break;
                }

            }
            if (count == 0) {
                System.out.println("Ingen resultater funnet");
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
        int count = 0;
        try {
            //System.out.println("Printing workouts");
            while(persons.next())
            {
                ++count;
                System.out.println("Personnummer: " + persons.getString("SSN") + " - Navnr: " + persons.getString("Name") + " - Telefonnummer: " + persons.getString("Telephone") + " - Mail: " + persons.getString("Email"));
            }
            if (count == 0) {
                System.out.println("Ingen resultater funnet");
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

    public static void addExerciseToWorkout(Connection conn, Statement stmt, Scanner reader){

        printWorkouts(conn, getWorkouts(stmt));
        System.out.println("-------------------");
        System.out.println("Velg trening");
        int workout = reader.nextInt();

        printExercises(getExercises(stmt, EXERCISES_ALL, ID_NOT_SPECIFIED));
        System.out.println("-------------------");
        System.out.println("Velg øvelse");
        int exercise = reader.nextInt();

        String SQL = ("INSERT INTO WorkoutHasExercise (WorkoutID, ExerciseID) values (" + workout + ", " + exercise + ")");
        //System.out.println(SQL);

        try {
            System.out.println("La øvelse til trening");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Kunne ikke legge øvelse til trening");
            printExeption(ex);
        }
    }

    public static void addPersonToWorkout(Connection conn, Statement stmt, Scanner reader){

        reader.nextLine();

        printPersons(getPersons(stmt));
        System.out.println("Skriv inn personnummer til medlem");
        String SSN = reader.nextLine();

        printWorkouts(conn, getWorkouts(stmt));
        System.out.println("-------------------");
        System.out.println("Velg trening");
        int workout = reader.nextInt();

        System.out.println("Skriv inn ytelsen (1-10)");
        int performance = reader.nextInt();

        reader.nextLine();

        System.out.println("Skriv inn notater");
        String notes = reader.nextLine();

        String SQL = ("INSERT INTO PersonHasWorkout (SSN, WorkoutID, Performance, Notes) values ('" + SSN + "', " + workout + ", " + performance+ ", '" + notes + "')");
        //System.out.println(SQL);

        try {
            System.out.println("La person til trening");
            stmt.executeUpdate(SQL);
        } catch (SQLException ex) {
            System.out.println("Kunne ikke legge person til trening");
            printExeption(ex);
        }
    }

    public static void createTrainingLog(Connection conn, Statement stmt, Scanner reader){

        Logger logger = Logger.getLogger("TrainingLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler("TrainingLog.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            //1System.out.println("Fetching workouts");
            ResultSet logs = stmt.executeQuery("SELECT * FROM PersonHasWorkout JOIN Workout ON Workout.WorkoutID = PersonHasWorkout.WorkoutID JOIN Person ON Person.SSN = PersonHasWorkout.SSN");

            //System.out.println("Printing workouts");
            while(logs.next())
            {
                // the following statement is used to log any messages
                logger.info( logs.getString("Person.Name") + " - " + logs.getString("Workout.Name") + " - Tid: " + readableTimestamp(logs.getTimestamp("Time")) + " - Ytelse: " + logs.getString("Performance") + " - Notater: " + logs.getString("Notes") );
            }

        } catch (SQLException ex) {
            System.out.println("Could not fetch logs");
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
        System.out.println("13) Opprett trenings logg");
        System.out.println("14) Legg øvelse til trening");
        System.out.println("15) Registrer utført trening");
        //System.out.println("16) Generer rapport");
        //System.out.println("17) Generer statistikk");
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
                    printWorkouts(conn, getWorkouts(stmt));
                    break;
                case 3:
                    printExercises(getExercises(stmt, EXERCISES_ALL, ID_NOT_SPECIFIED));
                    break;
                case 4:
                    printResults(conn, getResults(stmt));
                    break;
                case 5:
                    printCategory(getCategory(stmt));
                    break;
                case 6:
                    printGoals(conn, getGoals(stmt));
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
                    createResult(conn, stmt, reader);
                    break;
                case 11:
                    createCategory(stmt, reader);
                    break;
                case 12:
                    createGoal(stmt, reader);
                    break;
                case 13:
                    createTrainingLog(conn, stmt, reader);
                    break;
                case 14:
                    addExerciseToWorkout(conn, stmt, reader);
                    break;
                case 15:
                    addPersonToWorkout(conn, stmt, reader);
                    break;
                case 16:
                    //createStatistics(stmt, reader);
                    break;
                case 17:
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
