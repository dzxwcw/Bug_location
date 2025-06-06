package bug_loc_v0;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class CAL_result {
    public static void main(String[] args) throws IOException {
        String project = "CASSANDRA";
        String folder = "D:\\Bug_location\\process\\statistics\\" + project + "\\result_all";
        String recommend_path = "D:\\Bug_location\\process\\statistics\\"+project+"\\recommended_all";
        File[] file = new File(folder).listFiles();
        String metric_path = "D:\\Bug_location\\process\\statistics\\" + project + "\\answer_all-40.txt";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(metric_path));

        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4j123"));
        Session session = driver.session();
        String query = "match (n:issues{name:$name})-[:solve]->(m:javaFile) return m.name";

        int k = 50;
        int target_num = 40;
        float MAP = 0;
        float MRR = 0;
        float total_count = 0;

        float mrr = 0;
        float map = 0;
        int count = 0;
        for (File f : file) {
            bufferedWriter.write(f.getName() + "\n");
            File file1 = new File(recommend_path + "\\" + f.getName());
            BufferedWriter bufferedWriter1 = new BufferedWriter(new FileWriter(file1));
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            map = 0;
            mrr = 0;
            count = 0;

            while (true) {
                String bug_repo = bufferedReader.readLine();
                if (bug_repo==null){
                    break;
                }
                Result result = session.run(query, parameters("name", bug_repo));
                //answer list
                List<String> link_list = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    String link_file = record.get("m.name").asString();
                    link_list.add(link_file);
                }
                //ÍÆ¼ölist
                Map<String, Integer> recommend = new HashMap<>();
                for (int i = 1; i <= 2 * k; i++) {
                    String line = bufferedReader.readLine();
                    if (line==null){
                        break;
                    }
                    if (i % 2 == 0) {
                        continue;
                    }
                    if(i>target_num){
                        continue;
                    }
                    String recommend_issue = line.split(":")[0];
                    result = session.run(query, parameters("name", recommend_issue));
                    while (result.hasNext()) {
                        Record record = result.next();
                        String recommend_file = record.get("m.name").asString();
                        if (recommend.containsKey(recommend_file)) {
                            Integer i1 = recommend.get(recommend_file);
                            recommend.put(recommend_file, i1 + 1);
                        } else {
                            recommend.put(recommend_file, 1);
                        }
                    }
                }
                List<String> topKeys = recommend.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(10)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());


                bufferedWriter1.write(bug_repo + "\n");
                bufferedWriter1.write(topKeys.toString());
                bufferedWriter1.write("\n");

                //¼ÆËãmertic
                count++;
                int num = 0;
                int flag = 0;
                float t_map = 0;
                for (int j = 1; j <= topKeys.size(); j++) {
                    String s = topKeys.get(j-1);
                    if (link_list.contains(s)) {
                        num++;
                        t_map = t_map + (float) num / j;
                        if (flag == 0) {
                            mrr = mrr + (float) 1 / j;
                            flag = 1;
                        }
                    }
                }
                if (num!=0){
                    map = map + t_map/num;
                }

                if (bufferedReader.readLine()==null){
                    break;
                }

            }
            bufferedWriter.write("MRR = "+mrr/count+"\n");
            bufferedWriter.write("MAP = "+map/count+"\n");
            bufferedWriter.write("n = "+count+"\n");
            bufferedWriter1.close();
            MRR = MRR + mrr;
            MAP = MAP + map;
            total_count = total_count + count;
        }
        bufferedWriter.write("totol multiply:\n");
        bufferedWriter.write("MRR = "+MRR/total_count+"\n");
        bufferedWriter.write("MAP = "+MAP/total_count+"\n");
        bufferedWriter.write("totai_n = "+total_count+"\n");
        bufferedWriter.close();
    }
}
