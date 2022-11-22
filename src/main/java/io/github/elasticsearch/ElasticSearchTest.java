package io.github.elasticsearch;

import io.github.elasticsearch.Entity.Person;
import io.github.elasticsearch.Repository.ConnectionDao;

import java.io.IOException;

public class ElasticSearchTest {


	private static ConnectionDao connectionDao = ConnectionDao.getInstance();


    public static void main(String[] args) throws IOException {

        //Establishing connection to ElasticSearch Server
        connectionDao.makeConnection();

        System.out.println("\n\nDocument API_______________________________________________________________________________________");
        
        
        // Inserting data to the userdata index
        Person person = new Person();
        connectionDao.insertPerson(person);
        

        // Getting data from the server
        connectionDao.getPersonById("1");


        // Updating name to the userdata index -
        person.setEmpId("1");
        person.setName("Barath E");
        person.setAge(22);
        person.setDepartment("Software Engineer");
        person.setSalary(15000);
        connectionDao.updatePersonById(person.getEmpId(), person);


        // Deleting data from the userdata index
        connectionDao.deletePersonById("4");


        // Execute bulk request
        connectionDao.bulkAPI(); 


        // Check whether the given id exist in the given index
        connectionDao.doExist("1");


        // Returns the selected data from the server
        connectionDao.multiGet();


        // Copies the document to another index
        connectionDao.reIndex();


        // Search based on match query
        connectionDao.searchAPI();


        // Uses template query for search
        connectionDao.searchTemplateAPI();


        // Aggregation methods
        connectionDao.aggregationAPI();


        System.out.println("___________________________________________________________________________________________________");

        //Closing the connection
        connectionDao.closeConnection();
    }
}
