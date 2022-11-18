package io.github.elasticsearch;

import io.github.elasticsearch.Entity.Person;
import io.github.elasticsearch.Repository.ConnectionDao;

import java.io.IOException;
import java.util.UUID;

public class ElasticSearchTest {


	private static ConnectionDao connectionDao = ConnectionDao.getInstance();

    private static void getPerson(String id)
    {
        Person personFromDB = connectionDao.getPersonById(id);
        if(personFromDB != null)
        {
            System.out.println("\nPerson from Database --> " + personFromDB);
        }
        else
        {
            System.out.println("\nFailed to get the person");
        }
    }

    private static void insertPerson(Person person)
    {
        Person result = connectionDao.insertPerson(person);

        if(result != null)
        {
            System.out.println("\nPerson inserted --> " + person);
        }
        else
        {
            System.out.println("\nFailed to insert the person");
        }
    }

    private static void updatePerson(Person person)
    {
        boolean result = connectionDao.updatePersonById(person.getPersonId(), person);

        if(result)
        {
            System.out.println("\nPerson updated --> " + person);
        }
        else
        {
            System.out.println("\nFailed to update the person");
        }
    }

    private static void deletePerson(String id)
    {
        boolean result = connectionDao.deletePersonById(id);

        if(result)
        {
            System.out.println("\nPerson deleted");
        }
        else
        {
            System.out.println("\nFailed to delete the person");
        }
    }


    public static void main(String[] args) throws IOException {

        Person person = new Person();
        String id = UUID.randomUUID().toString();

        //Establishing connection to ElasticSearch Server
        connectionDao.makeConnection();

        System.out.println("\n\nDocument API_______________________________________________________________________________________");
        
        //Inserting data to the userdata index
        person.setPersonId(id);
        person.setName("Barath");
        insertPerson(person);
        
        //Getting data from the server
        getPerson(id);

        //Updating name to the userdata index
        person.setName("Barath E");
        updatePerson(person);

        //Deleting data from the userdata index
        deletePerson(id);

        System.out.println("___________________________________________________________________________________________________");

        //Closing the connection
        connectionDao.closeConnection();
    }
}
