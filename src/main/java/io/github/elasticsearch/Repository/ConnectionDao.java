package io.github.elasticsearch.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.ReindexRequest;

import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;

import org.elasticsearch.common.xcontent.XContentType;

import io.github.elasticsearch.Entity.Person;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ConnectionDao {


    private static final String HOST = "localhost";
    private static final int PORT_ONE = 9200;
    private static final String SCHEME = "http";

    private static final String ES_USERNAME = "barath";
    private static final String ES_PASSWORD = "Test@123";

    private static ConnectionDao instance;
    private static RestHighLevelClient restHighLevelClient;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String INDEX = "userdata";
    private static final String NEWINDEX = "persondata";
    private static final String TYPE = "user";

	private static DatabaseDao databaseDao = DatabaseDao.getInstance();


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
        final CredentialsProvider credentialsProvider =
            new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(ES_USERNAME, ES_PASSWORD));

        if(restHighLevelClient == null) 
        {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME))
                            .setHttpClientConfigCallback(new HttpClientConfigCallback() {
                                @Override
                                public HttpAsyncClientBuilder customizeHttpClient(
                                        HttpAsyncClientBuilder httpClientBuilder) {
                                    return httpClientBuilder
                                        .setDefaultCredentialsProvider(credentialsProvider);
                                }
                            }));
                            
        }

        return restHighLevelClient;
    }

    public synchronized void closeConnection() throws IOException 
    {
        restHighLevelClient.close();
        restHighLevelClient = null;
    }


    public boolean insertPerson(Person person)
    {
        List<Person> addList = databaseDao.getDbValues(person);

        for(Person newPerson : addList)
        {
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("empId", newPerson.getEmpId());
            dataMap.put("name", newPerson.getName());
            dataMap.put("age", newPerson.getAge());
            dataMap.put("department", newPerson.getDepartment());
            dataMap.put("salary", newPerson.getSalary());
            IndexRequest indexRequest = new IndexRequest(INDEX, TYPE, newPerson.getEmpId())
                    .source(dataMap);
            try 
            {
                IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                if(response == null)
                {
                    System.out.println("\nFailed to insert the person");
                    return false;
                }
            } 
            catch (Exception e)
            {
                e.getLocalizedMessage();
            }
        }
        System.out.println("\nValues inserted");
        return true;
    }


    public Person getPersonById(String id)
    {
        GetRequest getPersonRequest = new GetRequest(INDEX, TYPE, id);
        GetResponse getResponse = null;
        Person getPerson = null;
        try 
        {
            getResponse = restHighLevelClient.get(getPersonRequest, RequestOptions.DEFAULT);
            if(getResponse != null )
            {
                getPerson = objectMapper.convertValue(getResponse.getSourceAsMap(), Person.class);
                System.out.println("\nPerson from Database --> " + getPerson);
            }
            else
            {
                System.out.println("\nFailed to get the person");
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return getPerson;
    }


    public boolean updatePersonById(String id, Person person)
    {
        UpdateRequest updateRequest = new UpdateRequest(INDEX, TYPE, id).fetchSource(true);

        try 
        {
            Person result = getPersonById(id);
            if(result != null)
            {
                String personJson = objectMapper.writeValueAsString(person);
                updateRequest.doc(personJson, XContentType.JSON);   //.fields("cat")
                UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
                System.out.println(updateResponse.getGetResult());

                System.out.println("\nPerson updated --> " + person);
                return true;
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        System.out.println("\nFailed to update the person");
        return false;
    }


    public void deletePersonById(String id) 
    {
        DeleteRequest deleteRequest = new DeleteRequest(INDEX, TYPE, id);
        try {
            Person result = getPersonById(id);
            if(result != null)
            {
                DeleteResponse deleteResponse  = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                if(deleteResponse != null)
                {
                    System.out.println("\nPerson deleted");
                }
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        System.out.println("\nFailed to delete the person");
    }


    public void doExist(String id)
    {
        GetRequest getRequest = new GetRequest(INDEX, TYPE, id);
        getRequest.fetchSourceContext(new FetchSourceContext(false)); 
        getRequest.storedFields("_none_");   

        boolean exists = false;

        try 
        {
            exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
            if(exists)
            {
                System.out.println("\nDocument exist");
            }
            else{
                System.out.println("\nDocument doesn't exist");
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
    }


    public void bulkAPI()
    {
        BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest(INDEX, TYPE, "4")); 
        // request.add(new UpdateRequest(INDEX, TYPE, "2") 
        //         .doc(XContentType.JSON,"name", "test"));
        // request.add(new IndexRequest(INDEX, TYPE, "4")  
        //         .source(XContentType.JSON,"name", "barath"));
        try 
        {
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            if(bulkResponse != null)
            {
                System.out.println("\nBulk task completed");
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        System.out.println("\nFails to execute bulk task");
    }


    public boolean multiGet()
    {
        MultiGetRequest request = new MultiGetRequest();
        request.add(new MultiGetRequest.Item(INDEX, TYPE, "1")); 
        request.add(new MultiGetRequest.Item(INDEX, TYPE, "2")); 
        request.add(new MultiGetRequest.Item(INDEX, TYPE, "3")); 
        request.add(new MultiGetRequest.Item(INDEX, TYPE, "4")); 
        try 
        {
            MultiGetResponse response = restHighLevelClient.mget(request, RequestOptions.DEFAULT);
            if(response == null)
            {
                return false;
            }
            for(int i = 0 ; i < response.getResponses().length ; i++)
            {
                MultiGetItemResponse itemResponse = response.getResponses()[i];
                GetResponse getResponse = itemResponse.getResponse();  
                if (getResponse.isExists()) {
                    String sourceAsString = getResponse.getSourceAsString();        
                    System.out.println(sourceAsString);  
                } else {
                    String id = itemResponse.getId();
                    System.out.println(id + " doesn't exist");  
                }
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return true;
    }


    public boolean reIndex()
    {
        ReindexRequest reindexRequest = new ReindexRequest(); 
        reindexRequest.setSourceIndices(INDEX); 
        reindexRequest.setDestIndex(NEWINDEX);
        try 
        {
            TaskSubmissionResponse reindexSubmission = restHighLevelClient.submitReindexTask(reindexRequest, RequestOptions.DEFAULT); 
            if(reindexSubmission.getTask() == null)
            {
                System.out.println("Fails to copy the content");
                return false;
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        System.out.println("Document copied");
        return true;
    }


    public boolean searchAPI()
    {
        // MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("likes", "0");
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "Gopi");
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("username", "john");
        
        SearchRequest searchRequest = new SearchRequest(); 
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(matchQueryBuilder); 
        searchRequest.source(searchSourceBuilder); 
        try 
        {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if(searchResponse == null)
            {
                return false;
            }
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString();
                System.out.println(sourceAsString);
            }
        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return true;
    }


    public boolean searchTemplateAPI()
    {
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest()); 

        request.setScriptType(ScriptType.INLINE);
        request.setScript( 
            "{" +
            "  \"query\": { \"match\" : { \"{{field}}\" : \"{{value}}\" } } }");

        Map<String, Object> scriptParams = new HashMap<String, Object>();
        scriptParams.put("field", "salary");
        scriptParams.put("value", "30000");
        request.setScriptParams(scriptParams);
        
        try 
        {
            SearchTemplateResponse response = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT);
            if(response == null)
            {
                return false;
            }
            SearchResponse searchResponse = response.getResponse();
            SearchHits searchHits = searchResponse.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString();
                System.out.println(sourceAsString);
            }

        } 
        catch (Exception e)
        {
            e.getLocalizedMessage();
        }
        return true;
    }

    
    public boolean aggregationAPI()
    {   
        SearchRequest searchRequest = new SearchRequest(); 
        searchRequest.indices("employee");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(QueryBuilders.matchAllQuery()); 
        searchSourceBuilder.aggregation(AggregationBuilders.sum("sum").field("salary"));
        searchRequest.source(searchSourceBuilder); 

        try 
        {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if(searchResponse == null)
            {
                return false;
            }
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString();
                System.out.println(sourceAsString);
            }
            Sum sum = searchResponse.getAggregations().get("sum");
            double result = sum.getValue();
            System.out.println("Aggr Sum: " + result);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }
}
