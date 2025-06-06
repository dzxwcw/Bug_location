import org.dom4j.Element;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class CAL_MRR_MAP_ALL {
    public static void main(String[] args) throws IOException {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j123"));
        Session session = driver.session();
        String query = "match (n:issues{name:$name})-[:solve]->(m:javaFile) return m.name";
        String workdir = "D:\\Bug_location\\process\\AfterDelete\\Apache\\Locus_GEODE_all";
        File work = new File(workdir);
        File[] listFiles = work.listFiles();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("D:\\Bug_location\\process\\AfterDelete\\Apache\\LocusOutput_flink_all.txt"));
        float total_mrr=0;
        float total_map = 0;
        int total_count=0;
        for (File f: listFiles){
            String name = f.getName();
            String target_folder = workdir+"\\"+name+"\\recommended";
            File file_folder = new File(target_folder);
            File[] files = file_folder.listFiles();
            float MAP = 0;
            float MRR = 0;
            int k = 10;
            int based_num = files.length;
            for (File file : files){
                String bug_repo = "FLINK-"+file.getName().split("\\.")[0];
                Result result = session.run(query, parameters("name", bug_repo));
                List<String> link_list = new ArrayList<>();
                float map = 0;
                float mrr = 0;
                int count = 0;
                while (result.hasNext()){
                    Record record = result.next();
                    String link_file = record.get("m.name").asString();
                    link_file = link_file.replace("_",".");
                    //处理不完整路径
                    String[] split = link_file.split("\\.");
                    if (split.length>1){
                        link_file=split[split.length-2];
                    }
                    link_list.add(link_file);
                }

                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                int flag = 1;
                for(int i=1;i<=k;i++){
                    String line = bufferedReader.readLine();
                    if (line==null){
                        continue;
                    }
                    String linkfile = line.split("\t")[2];
                    //处理不完整路径名
                    String[] split = linkfile.split("\\.");
                    linkfile = split[split.length-2];

                    if(link_list.contains(linkfile)){
                        count++;
                        map = (float) count /i + map;
                        if(flag == 1 ){
                            MRR = MRR + (float) 1 /i;
                            flag = 0;
                        }
                    }
                }
                if(count!=0){
                    MAP = MAP + map/count;
                }
            }
            total_mrr = total_mrr+MRR;
            total_map = total_map+MAP;
            total_count = total_count+based_num;

            MRR = MRR/based_num;
            MAP = MAP/based_num;

            bufferedWriter.write(name+":\n");
            bufferedWriter.write("MRR = "+MRR+"\n");
            bufferedWriter.write("MAP = "+MAP+"\n");
            bufferedWriter.write("n = "+based_num+"\n");

        }
        total_mrr =total_mrr/total_count;
        total_map =total_map/total_count;
        bufferedWriter.write("totol multiply:\n");
        bufferedWriter.write("MRR = "+total_mrr+"\n");
        bufferedWriter.write("MAP = "+total_map+"\n");
        bufferedWriter.write("totai_n = "+total_count+"\n");
        bufferedWriter.close();
    }
}
