package io.github.elasticsearch.Repository;

import io.github.elasticsearch.Entity.Person;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseDao {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/EsDatabase";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";

    private static DatabaseDao instance;
    private static Connection con;

    public static DatabaseDao getInstance()
	{
	    if (instance == null){
	        instance = new DatabaseDao();
	    }
	    return instance;
	}

    private DatabaseDao()
    {
        con = makeConnection();
    }
    
    public Connection makeConnection() {
        Connection con = null;
        try 
        {
            Class.forName("org.postgresql.Driver");
            con = DriverManager
                .getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } 
        catch (Exception e) 
        {
            e.getLocalizedMessage();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("\nOpened database successfully");

        return con;
    }

    public void closeConnection() throws SQLException 
    {
        con.close();
        con = null;
    }

    
    public List<Person> getDbValues(Person person)
    {
        Statement stmt = null;
        List<Person> list = new ArrayList<Person>();
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM employee;" );
            while ( rs.next() ) {
                Person newPerson = new Person();
                newPerson.setEmpId(rs.getString("emp_id"));
                newPerson.setName(rs.getString("emp_name"));
                newPerson.setAge(rs.getInt("age"));
                newPerson.setDepartment(rs.getString("department"));
                newPerson.setSalary(rs.getInt("salary"));
                list.add(newPerson);
            }
        System.out.println(list.toString());
        rs.close();
        stmt.close();
        }
        catch(Exception e)
        {
            e.getLocalizedMessage();
        }
        return list;
    }
}
