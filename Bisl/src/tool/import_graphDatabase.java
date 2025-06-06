import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import static org.neo4j.driver.Values.parameters;

public class import_graphDatabase {
    private static Driver driver;

    public static void handleFiles(String filename) throws IOException {
        File file = new File(filename);
        BufferedReader reader = null;
        System.out.println("开始读取");
        reader = new BufferedReader(new FileReader(file));
        //HashMap<String, ArrayList<String>> record = new HashMap<String, ArrayList<String>>();

        String issueID ="";
        String type = "";
        ArrayList<String> files = new ArrayList<>();
        String summary = "";
        String description = "";

        String action  = reader.readLine();
        while(action!=null){
            if(action.contains("issueID")){
                //提取出issueId
                issueID = action.split(":")[1];
                //System.out.println(issueID);

                String action_type = reader.readLine();
                if(action_type.contains("type")){
                    //提取出issue的type
                    type = action_type.split(":")[1];
                    //System.out.println(type);

                    String action_files = reader.readLine();
                    while(!action_files.split(":")[0].equals("summary")){
                        String[] split_temp = action_files.split("/");
                        files.add(split_temp[split_temp.length-1]);
                        action_files = reader.readLine();
                    }
                    //提取出javafiles
                    //System.out.println(files);

                    if(action_files.contains("summary")){
                        //提取出summary
                        summary = action_files.split(":")[1];
                        //System.out.println(summary);

                        String action_description = reader.readLine();
                        if(action_description.contains("description")){
                            String temp = reader.readLine();
                            while(!temp.split(":")[0].equals("issueID")){
                                temp = temp.strip();
                                description = description + temp+" ";
                                temp = reader.readLine();
                                if(temp==null){
                                    break;
                                }
                            }
                            //提取出description
                            //System.out.println(description);

                            action = temp;
                            import_graph(issueID,type,files,summary,description);

                            issueID = "";
                            type = "";
                            files.clear();
                            summary = "";
                            description = "";
                        }
                    }
                }
            }
        }
    }

    public static void import_graph(String issueID, String type, ArrayList<String> files, String summary, String description){
        Session session = driver.session();
        String cypherQuery = "CREATE (n:issue_report {name: $issueID, type:$type, summary:$summary, description:$description})";
        session.run(cypherQuery , parameters("issueID",issueID,"type",type,"summary",summary,"description",description));
        for(int i=0; i <files.size();i++){
            String file = files.get(i);
            cypherQuery = "MATCH (n:javaFile {name: $value}) RETURN n LIMIT 1";
            Result result = session.run(cypherQuery, parameters("value", file));
            if(!result.hasNext()) {
                cypherQuery = "CREATE (n:javaFile {name:$file})";
                session.run(cypherQuery,parameters("file",file));
            }
            cypherQuery="MATCH (a:issue_report {name:$issueID}), (b:javaFile {name:$file}) MERGE (a)-[:solve]->(b)";
            session.run(cypherQuery,parameters("issueID",issueID,"file",file));
        }
    }

    public static void test() throws IOException {
        driver = GraphDatabase.driver("bolt://localhost:7687",AuthTokens.basic("neo4j","neo4j123"));
        Session session = driver.session();
        //String query  = "match (n)-[:solve]->(m:javaFile{name:\"DeprecationException.java\"})\n return n.name,n.summary";
        String query = "";
        Result result = session.run(query);

        while (result.hasNext()){
            Record next = result.next();

        }
    }

    public static void main(String[] args) throws IOException {
        /*
        String file_name = "D:\\Bug_location\\process\\statistics\\dataset\\groovy.txt";

        try {
            driver = GraphDatabase.driver("bolt://localhost:7687",AuthTokens.basic("neo4j","neo4j123"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        handleFiles(file_name);
        */

        test();
    }
}
