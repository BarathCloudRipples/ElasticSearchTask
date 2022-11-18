package io.github.elasticsearch.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import io.github.elasticsearch.Entity.Person;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionDao {


    private static final String HOST = "localhost";
    private static final int PORT_ONE = 9200;
    private static final String SCHEME = "http";

    private static ConnectionDao instance;
    private static RestHighLevelClient restHighLevelClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String INDEX = "userdata";
    private static final String TYPE = "user";

    public static ConnectionDao getInstance()
	{
	    if (instance == null){
	        instance = new ConnectionDao();
	    }
	    return instance;
	}

    private ConnectionDao()
    {
        restHighLevelClient = makeConnection();
    }

    public synchronized RestHighLevelClient makeConnection() 
    {

        if(restHighLevelClient == null) 
        {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME)));
        }

        return restHighLevelClient;
    }

    public synchronized void closeConnection() throws IOException 
    {
        restHighLevelClient.close();
        restHighLevelClient = null;
    }


    public Person insertPerson(Person person)
    {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("personId", person.getPersonId());
        dataMap.put("name", person.getName());
        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, person.getPersonId())
                .source(dataMap);
        try 
        {
            IndexResponse response = restHighLevelClient.index(indexRequest);
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return person;
    }


    public Person getPersonById(String id)
    {
        GetRequest getPersonRequest = new GetRequest(INDEX, TYPE, id);
        GetResponse getResponse = null;
        try 
        {
            getResponse = restHighLevelClient.get(getPersonRequest);
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return getResponse != null ?
                objectMapper.convertValue(getResponse.getSourceAsMap(), Person.class) : null;
    }


    public boolean updatePersonById(String id, Person person)
    {
        UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, id).fetchSource(true);

        try 
        {
            String personJson = objectMapper.writeValueAsString(person);
            updateRequest.doc(personJson, XContentType.JSON);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest);
            return true;
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return false;
    }


    public boolean deletePersonById(String id) 
    {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, id);
        try {
            Person person = getPersonById(id);
            if(person != null)
            {
                DeleteResponse deleteResponse  = restHighLevelClient.delete(deleteRequest);
                return true;
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return false;
    }
}
