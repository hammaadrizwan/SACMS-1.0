package com.example.sacms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class Club {
    private String clubName;
    private String clubDescription;
    private String clubID;
    private String teacherIncharge;
    private ArrayList<Event> events;
    private ArrayList<Student> students;
    private ArrayList<Request> requests;


    Club(String clubID,String clubName, String clubDescription, String teacherIncharge){//To get it from the database
        this.clubID=clubID;
        this.clubDescription=clubDescription;
        this.clubName=clubName;
        this.teacherIncharge = teacherIncharge;
        this.students=loadStudentsOfClub(getClubID());
        //this.events=loadStudentsOfClub(clubID);

    }

    public String getClubName() {
        return clubName;
    }
    public String getTeacherIncharge() {
        return teacherIncharge;
    }
    public String getClubDescription() {
        return clubDescription;
    }
    public String getClubID() {
        return clubID;
    }

    public void displayReport() {

    }



    //DATABASE METHODS FOR CLUB
    public static String generateClubID() {
        int idLength = 4;//club ID of 5 digits
        StringBuilder stringBuilder = new StringBuilder("C");
        Random random = new Random();
        for (int i = 0; i < idLength; i++) {
            int digit = random.nextInt(10);
            stringBuilder.append(digit);
        }

        return stringBuilder.toString();// returns it
    }

    public String generateMembershipID() {

        int idLength = 9;//Mmebreshoip ID of 10 digits
        StringBuilder stringBuilder = new StringBuilder("M");
        Random random = new Random();
        for (int i = 0; i < idLength; i++) {
            int digit = random.nextInt(10);
            stringBuilder.append(digit);
        }

        return stringBuilder.toString();
    }

    public static void createClubTableOnDatabase() {
        try (Connection connection = Database.getConnection()) {//gets the connection from the database using the Database class getConnection method
            String query ="CREATE TABLE IF NOT exists Club (" +
                    "    ClubID VARCHAR(5) PRIMARY KEY," +
                    "    ClubName VARCHAR(255)," +
                    "    Description VARCHAR(255)," +
                    "    TeacherID VARCHAR(5)," +
                    "    FOREIGN KEY (TeacherID) REFERENCES Teacher(TeacherID)" +
                    ");";// same SQL query is given here as string
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {//this is then converted to a prerpare statment
                preparedStatement.executeUpdate();// finally its then executed on the database
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createClubMembershipTableOnDatabase() {//to store students and club ID
        try (Connection connection = Database.getConnection()) {//gets the connection from the database using the Database class getConnection method
            String query ="CREATE TABLE IF NOT EXISTS ClubsMembership (" +
                    "    MembershipID VARCHAR(10) PRIMARY KEY," +
                    "    ClubID VARCHAR(5)," +
                    "    StudentID VARCHAR(5)," +
                    "    FOREIGN KEY (ClubID) REFERENCES Club(ClubID)," +
                    "    FOREIGN KEY (StudentID) REFERENCES Student(StudentID)" +
                    ");";// same SQL query is given here as string
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {//this is then converted to a prerpare statment
                preparedStatement.executeUpdate();// finally its then executed on the database
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Club> loadClubsFromDatabase()  {//Load data from the student database
        createClubTableOnDatabase();
        ArrayList<Club> clubs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM club");//prepare the statement
             ResultSet results = preparedStatement.executeQuery()) {//we get it as a Result set

            while (results.next()) {// until the set of results are empty we populate the list of clubs and return it
                Club club = new Club(results.getString("ClubID"),results.getString("ClubName"),results.getString("Description"),results.getString("TeacherID"));
                clubs.add(club);//loads all clubs to the ArrayList
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clubs;
    }

    public void addStudent(Student student){
        students.add(student);//add the studetn to tthe list of students
        String membershipID;
        do {
            membershipID = generateMembershipID();//generate a membership ID for each student
        } while (loadExistingMemberships().contains(membershipID));//until its unique we generate a new ID
        String insertClubMembershipQuery = "INSERT INTO clubsmembership VALUES (?, ?,?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertClubMembershipQuery)) {
            preparedStatement.setString(1, membershipID);//inserts the Membership ID,student ID and the club ID to the table
            preparedStatement.setString(2, getClubID());
            preparedStatement.setString(3, student.getStudentID());
            preparedStatement.executeUpdate();//push

    } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeStudent(Student s){
        students.remove(s);//remove the student from the database
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM clubsMembership WHERE StudentID = ? AND ClubID = ?")) {// deketes the row where the student ID is equal to the on e entered

            preparedStatement.setString(1, s.getStudentID());
            preparedStatement.setString(2,getClubID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> loadExistingMemberships(){//get the exsisting Memberships to an arraylist, count howmany members
        ArrayList<String> existingMembershipIDs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM clubsMembership");
             ResultSet results = preparedStatement.executeQuery()) {


            while (results.next()) {
                existingMembershipIDs.add(results.getString("MembershipID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existingMembershipIDs;

    }

    public static ArrayList<Student> loadStudentsOfClub(String clubID) {
        createClubMembershipTableOnDatabase();//create teh membership table if not exsist
        ArrayList<Student> students = new ArrayList<>();//Loads all the students from the database who are the members of that club
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT Student.StudentID, Student.FirstName, Student.LastName, Student.Email, Student.DateOfBirth, Student.Password, Student.ContactNo, Student.Classroom " +
                             "FROM Student " +
                             "JOIN ClubsMembership ON Student.StudentID = ClubsMembership.StudentID " +
                             "WHERE ClubsMembership.ClubID = ?")) {

            preparedStatement.setString(1, clubID);// returns all the students for that specific club by joingin the studentID FK from the clubmembership table to the student Table

            try (ResultSet results = preparedStatement.executeQuery()) {
                while (results.next()) {
                    Student student = new Student(
                            results.getString("FirstName"),
                            results.getString("LastName"),
                            results.getString("Email"),
                            results.getString("Password"),
                            results.getString("DateOfBirth"),
                            results.getString("ContactNo"),
                            results.getString("StudentID"),
                            results.getString("Classroom")
                    );//finally creates an object of the student class
                    students.add(student);//adds to the students list
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students; // return the list of students
    }

    public void deleteClub(String clubID){
        String deleteClubAdvisorQuery = "Delete from clubadvisor where clubadvisor.clubid = ?;";//to delete a club first we remove them from the club advisor thenclub membership and finally in the club id
        String deleteClubMembershipQuery = "Delete from clubsmembership where clubsmembership.clubid = ?;";
        String deleteClubQuery = "Delete from club where clubid = ?;";
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedDeleteClubAdvisorQueryStatement = connection.prepareStatement(deleteClubAdvisorQuery);
             PreparedStatement preparedDeleteClubMembershipQueryStatement = connection.prepareStatement(deleteClubMembershipQuery);
             PreparedStatement preparedDeleteClubQueryStatement = connection.prepareStatement(deleteClubQuery)) {

            connection.setAutoCommit(false);//pause autocommit as theres a squence of insturections to be followed

            preparedDeleteClubAdvisorQueryStatement.setString(1,clubID);
            preparedDeleteClubAdvisorQueryStatement.executeUpdate();

            preparedDeleteClubMembershipQueryStatement.setString(1,clubID);
            preparedDeleteClubMembershipQueryStatement.executeUpdate();

            preparedDeleteClubQueryStatement.setString(1,clubID);
            preparedDeleteClubQueryStatement.executeUpdate();// once its updated we then set commit all at once


            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*public void addRequest(){

        ArrayList<String> exsistingRequests= new ArrayList<String>();
        String requestID;
        do {
            requestID = generateRequestID();//generate a membership ID for each student
        } while (Request.loadExistingRequests().contains(requestID));//until its unique we generate a new ID
        requests.add(request);

    }*/
    public static String generateRequestID() {
        int idLength = 9;
        StringBuilder stringBuilder = new StringBuilder("R");
        Random random = new Random();
        for (int i = 0; i < idLength; i++) {
            int digit = random.nextInt(10);
            stringBuilder.append(digit);
        }

        return stringBuilder.toString();// returns it
    }
    public static void createTableOnDatabase(){
        try (Connection connection = Database.getConnection()) {//gets the connection from the database using the Database class getConnection method
            String query ="CREATE TABLE IF NOT exists Request (" +
                    "    RequestID VARCHAR(10) PRIMARY KEY," +
                    "    TeacherID VARCHAR(5)," +
                    "    StudentID VARCHAR(5)," +
                    "    Position VARCHAR(25)," +
                    "    FOREIGN KEY (TeacherID) REFERENCES Teacher(TeacherID)" +
                    "    FOREIGN KEY (StudentID) REFERENCES Student(StudentID)" +
                    ");";// same SQL query is given here as string
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {//this is then converted to a prerpare statment
                preparedStatement.executeUpdate();// finally its then executed on the database
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

