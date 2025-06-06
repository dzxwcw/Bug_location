import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.neo4j.driver.*;

import java.io.File;

import static org.neo4j.driver.Values.parameters;

public class link_bugreport_java {
    private Driver driver;

    public link_bugreport_java(Driver driver) {
        this.driver = driver;
    }

    public void createNode(String java, String issue_report){
        //MATCH p=(n:issues WHERE n.name=~"CALCITE-[0-9]+")-[r:solve]->() RETURN p LIMIT 100
        Session session = driver.session();
        String cypherQuery = "MATCH (n:javaFile {name: $value}) RETURN n LIMIT 1";
        Result result = session.run(cypherQuery, parameters("value", java));
        if(!result.hasNext()) {
            cypherQuery = "CREATE (n:javaFile {name:$file})";
            session.run(cypherQuery,parameters("file",java));
        }
        cypherQuery="MATCH (a:issues {name:$issueID}), (b:javaFile {name:$file}) MERGE (a)-[:solve]->(b)";
        session.run(cypherQuery,parameters("issueID",issue_report,"file",java));
    }

    public  void traverseFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                //D:\Bug_location\commit\aries
                for (File file : files) {
                    if (file.isDirectory()) {
                        //D:\Bug_location\commit\aries\00b0f90_New Feature_ARIES-79
                        File[] file_sons = file.listFiles();
                        for (File file_son : file_sons){
                            String file_sonName = file_son.getName();
                            String[] split = file_sonName.split("_");
                            if(split.length==3){
                                String issue_report = split[2];
                                File java_folder = new File(file_son.getPath() + "\\from");
                                File[] java_sources = java_folder.listFiles();
                                if(java_sources!=null){
                                    for(File java_source:java_sources){
                                        String java = java_source.getName();
                                        createNode(java,issue_report);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j123"));
        link_bugreport_java link = new link_bugreport_java(driver);
        String commit_path = "D:\\Bug_location\\commit";
        File folder = new File(commit_path);
        link.traverseFolder(folder);
    }
}
