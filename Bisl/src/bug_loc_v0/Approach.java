package bug_loc_v0;
import org.json.JSONObject;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import io.kuy.infozilla.cli.Main;
import picocli.CommandLine;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.neo4j.driver.Values.parameters;

public class Approach implements Runnable{
    private Driver driver;
    private List<List<issue_report>> train_issue;
    private List<List<issue_report>> test_issue;
    private title_similar my_title_similar;
    private word2vec_class my_word2vec_class;
    private String previous_folder;
    private String test_folder;
    private String project;
    private List<String> versions;
    private String output;
    private Main infozilla = new Main();
    private int version_index;
    //加载句子检测器
    private SentenceModel sentenceModel = new SentenceModel(new FileInputStream("D:\\Bug_location\\process\\opennlp_models\\opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin"));

    public Approach(Driver driver,String project, List<String> versions,int index) throws IOException, ParserConfigurationException, SAXException {
        this.driver = driver;
        this.project = project;
        this.versions = versions;
        this.version_index = index;
        this.train_issue = new ArrayList<>();
        this.test_issue = new ArrayList<>();
        this.my_title_similar = new title_similar();
        this.output = "D:\\Bug_location\\process\\statistics\\" + this.project + "\\result\\";
        this.my_word2vec_class = new word2vec_class("./GoogleNews-vectors-negative300.bin");
        this.previous_folder="D:\\Bug_location\\process\\statistics\\"+project+"\\pre_data\\repository";
        this.test_folder = "D:\\Bug_location\\process\\statistics\\"+project+"\\test_data\\repository";
        load_data();
    }

    public void load_data() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("loading data....");
        List<issue_report> pre_list = new ArrayList<>();
        List<issue_report> test_list = new ArrayList<>();
        for (int i = 0; i < this.versions.size(); i++) {
            String ver = this.versions.get(i);
            System.out.println("loading the version " + ver);
            String pre_path = this.previous_folder + "\\" + this.project + "_" + ver.replace(".", "_") + ".xml";
            String test_path = this.test_folder + "\\" + this.project + "_" + ver.replace(".", "_") + ".xml";
            File pre_file = new File(pre_path);
            File test_file = new File(test_path);
            // 创建 DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            //解析xml
            Document pre = builder.parse(pre_file);
            Document test = builder.parse(test_file);
            // 获取 employee 节点,以及子节点信息
            NodeList BugList = pre.getElementsByTagName("bug");
            for (int j = 0; j < BugList.getLength(); j++) {
                Node bug = BugList.item(j);
                //获取子节点信息
                Element bug_ele = (Element) bug;
                Node buginformation = bug_ele.getElementsByTagName("buginformation").item(0);
                Node fixedFiles = bug_ele.getElementsByTagName("fixedFiles").item(0);
                Element bug_inf = (Element) buginformation;
                Element fixed_file = (Element) fixedFiles;
                String summary = bug_inf.getElementsByTagName("summary").item(0).getTextContent();
                String description = bug_inf.getElementsByTagName("description").item(0).getTextContent();
                String name = this.project + "-" + ((Element) bug).getAttribute("id");
                NodeList link_file = fixed_file.getElementsByTagName("file");
                List<String> temp = new ArrayList<>();
                for (int num = 0; num < link_file.getLength(); num++) {
                    String filename = link_file.item(num).getTextContent();
                    temp.add(filename);
                }
                issue_report issueReport = new issue_report(name, summary, description, temp);
                issueReport = deal_data(issueReport);
                pre_list.add(issueReport);
            }
            System.out.println("loading pre_list finished");
            // 获取 employee 节点,以及子节点信息
            NodeList BugList2 = test.getElementsByTagName("bug");
            for (int j = 0; j < BugList2.getLength(); j++) {
                Node bug = BugList2.item(j);
                //获取子节点信息
                Element bug_ele = (Element) bug;
                Node buginformation = bug_ele.getElementsByTagName("buginformation").item(0);
                Node fixedFiles = bug_ele.getElementsByTagName("fixedFiles").item(0);
                Element bug_inf = (Element) buginformation;
                Element fixed_file = (Element) fixedFiles;
                String summary = bug_inf.getElementsByTagName("summary").item(0).getTextContent();
                String description = bug_inf.getElementsByTagName("description").item(0).getTextContent();
                String name = this.project + "-" + ((Element) bug).getAttribute("id");
                NodeList link_file = fixed_file.getElementsByTagName("file");
                List<String> temp = new ArrayList<>();
                for (int num = 0; num < link_file.getLength(); num++) {
                    String filename = link_file.item(num).getTextContent();
                    temp.add(filename);
                    System.out.println();
                }
                issue_report issueReport = new issue_report(name, summary, description, temp);
                issueReport = deal_data(issueReport);
                test_list.add(issueReport);
            }
            System.out.println("loading test_list finished");
            List<issue_report> tt = new ArrayList<>();
            tt.addAll(test_list);
            this.test_issue.add(tt);
            List<issue_report> t = new ArrayList<>();
            t.addAll(pre_list);
            this.train_issue.add(t);

            test_list.clear();
            pre_list.clear();
        }

        /*
        Session session = driver.session();
        String query = "MATCH (n:issues) WHERE n.name =~ $pro RETURN n.name,n.description,n.type,n.title";
        String project = "HIVE-[0-9]+";
        Result result = session.run(query, parameters("pro",project));
        while (result.hasNext()){
            Record record = result.next();
            String name = record.get("n.name").asString();
            String type = record.get("n.type").asString();
            String summary = record.get("n.title").asString();
            String description = record.get("n.description").asString();
            issue_report issueReport = new issue_report(name, type, summary, description);
            //初始化issueReport
            query  = "match (n:issues{name:$name})-[:solve]->(m:javaFile) return count(m)";
            Result bug_res = session.run(query, parameters("name",issueReport.getName()));
            Record next = bug_res.next();
            int num = next.get("count(m)").asInt();
            if(num>0){
                issueReport = deal_data(issueReport);
                this.issue_report_list.add(issueReport);
            }
        }
        Collections.shuffle(issue_report_list);
        for(int i =0;i<issue_report_list.size()*9/10;i++){
            train_issue.add(issue_report_list.get(i));
        }
        for (int i = issue_report_list.size()*9/10
             ;i<issue_report_list.size();i++){
            test_issue.add(issue_report_list.get(i));
        }
        */
    }

    public issue_report deal_data(issue_report issueReport) throws IOException {
        //提取summary的关键信息,加载setExtract_summary
        if (issueReport.getSummary() != null) {
            HashSet<String> extract_n_adj = this.my_title_similar.extract_n_adj(issueReport.getSummary());
            issueReport.setExtract_summary(extract_n_adj);
        }
        //利用infozilla-master分割description为三个部分
        File temp = new File("temp.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));
        bufferedWriter.write(issueReport.getDescription());
        bufferedWriter.close();
        CommandLine.run(this.infozilla, "temp.txt");
        String issue_clean = "temp.txt.cleaned";
        String issue_xml = "temp.txt.xml";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(issue_clean));
        String sb = "";
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            sb = sb + line;
        }
        bufferedReader.close();
        //加载句子检测器
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(this.sentenceModel);
        String[] sentences = sentenceDetectorME.sentDetect(sb);
        //过滤句子
        List<String> list = new ArrayList<>();
        String[] check = {"when", "When", "If", "if", "As", "But", "but", "So", "so ", "However", "however", "While", "while", "not", "fail", "n't", "cause", "Cause"};
        for (String sentence : sentences) {
            for (String s : check) {
                if (sentence.contains(s)) {
                    list.add(sentence);
                    break;
                }
            }
        }
        if (list.isEmpty()) {
            list.addAll(Arrays.asList(sentences));
        }
        issueReport.setExtract_description(list);

        //读取xml文件，保存java code 和 strack 信息
        //提取方法名
        String description = issueReport.getDescription();
        String pattern = "\\w+\\(\\w*\\)";
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(description);
        HashSet<String> method = new HashSet<>();
        while (matcher.find()) {
            String match = matcher.group();
            method.add(match);
        }
        issueReport.setExtract_method(method);
        //提取堆栈跟踪
        HashSet<String> exception = new HashSet<>();
        HashSet<String> methodNames = new HashSet<>();
        HashSet<String> classNames = new HashSet<>();
        String regex = "\\s+at\\s+(\\S+)\\.(\\S+)\\(";
        Pattern pattern1 = Pattern.compile(regex);
        Matcher matcher1 = pattern1.matcher(description);
        while (matcher1.find()) {
            classNames.add(matcher1.group(1));
            methodNames.add(matcher1.group(2));
        }
        String regex2 = "[a-zA-Z]+Exception";
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(description);
        while (matcher2.find()) {
            exception.add(matcher2.group());
        }
        issueReport.setExtract_exception(exception);
        issueReport.setExtract_class(classNames);
        issueReport.setExtract_methodName(methodNames);
        return issueReport;
    }

    public void test_data() throws IOException {
        for (int epoch = this.version_index; epoch <= this.version_index; epoch++) {
            String ver = this.versions.get(epoch);
            System.out.println("processing the version " + ver + "\n");
            List<issue_report> test_list = this.test_issue.get(epoch);
            List<issue_report> pre_list = new ArrayList<>();
            for (int i = 0; i <= epoch; i++) {
                pre_list.addAll(this.train_issue.get(i));
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.output + project + "_" + ver.replace(".", "_") + ".txt"));
            int ok = 0;
            Map<Integer, Double> map = new HashMap<>();
            for (int i = 0; i < test_list.size(); i++) {
                issue_report test_bug = test_list.get(i);
                System.out.println(test_bug.getName());
                bufferedWriter.write(test_bug.getName() + "\n");
                map.clear();
                for (int j = 0; j < pre_list.size(); j++) {
                    issue_report temp_issue = pre_list.get(j);
                    if (Objects.equals(temp_issue.getName(), test_bug.getName())) {
                        continue;
                    }
                    //计算标题相似度
                    double v1 = this.my_title_similar.cal_title(test_bug.getExtract_summary(), temp_issue.getExtract_summary());
                    map.put(j, v1);
                }
                List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(map.entrySet());
                Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
                        return entry2.getValue().compareTo(entry1.getValue());
                    }
                });
                LinkedHashMap<Integer, Double> sortedMap = new LinkedHashMap<>();
                for (Map.Entry<Integer, Double> entry : entryList) {
                    sortedMap.put(entry.getKey(), entry.getValue());
                }

                int k_size = sortedMap.entrySet().size() / 2;
                List<issue_report> second_part = new ArrayList<>();
                List<Double> second_part_title = new ArrayList<>();

                for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
                    if (k_size == 0) {
                        break;
                    }
                    second_part.add(pre_list.get(entry.getKey()));
                    second_part_title.add(entry.getValue());
                    k_size--;
                }
                map.clear();

                for (int j = 0; j < second_part.size(); j++) {
                    double res = 0;
                    issue_report temp_issue = second_part.get(j);
                    double v1 = second_part_title.get(j);
                    //计算方法名相似度
                    double v3 = 0;
                    if (!test_bug.getExtract_methodName().isEmpty() && !temp_issue.getExtract_methodName().isEmpty()) {
                        v3 = this.my_title_similar.cal_title(test_bug.getExtract_methodName(), temp_issue.getExtract_methodName());
                    }
                    double v4 = 0;
                    if (!test_bug.getExtract_method().isEmpty() && !temp_issue.getExtract_method().isEmpty()) {
                        v4 = this.my_title_similar.cal_title(test_bug.getExtract_method(), temp_issue.getExtract_method());
                    }
                    //计算异常信息相似度
                    HashSet<String> extractException1 = test_bug.getExtract_exception();
                    HashSet<String> extractException2 = temp_issue.getExtract_exception();
                    Set<String> intersection = new HashSet<>(extractException1);
                    intersection.retainAll(extractException2);
                    int size = intersection.size();
                    double v5 = 0;
                    if (size > 0) {
                        v5 = 1;
                    }
                    //计算文本语义相似度
                    List<String> description_issue_list = temp_issue.getExtract_description();
                    List<String> description_bug_list = test_bug.getExtract_description();
                    double v2 = 0;
                    if (!description_bug_list.isEmpty() && !description_issue_list.isEmpty()) {
                        for (int a = 0; a < description_bug_list.size(); a++) {
                            double temp = 0;
                            for (int b = 0; b < description_issue_list.size(); b++) {
                                double v = this.my_word2vec_class.calculateSimilarity(description_issue_list.get(b), description_bug_list.get(a));
                                if (v > temp) {
                                    temp = v;
                                }
                            }
                            v2 = v2 + temp;
                        }
                        v2 = v2 / description_bug_list.size();
                    }

                    int effect = 0;
                    res = v1 + v2 + v3 + v4 + v5;
                    if (v1 != 0) effect++;
                    if (v2 != 0) effect++;
                    if (v3 != 0) effect++;
                    if (v4 != 0) effect++;
                    if (v5 != 0) effect++;
                    if (effect != 0) {
                        res = res / effect;
                    } else {
                        res = 0;
                    }
                    map.put(j, res);
                }
                List<Map.Entry<Integer, Double>> entryList2 = new ArrayList<>(map.entrySet());
                Collections.sort(entryList2, new Comparator<Map.Entry<Integer, Double>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
                        return entry2.getValue().compareTo(entry1.getValue());
                    }
                });
                LinkedHashMap<Integer, Double> sortedMap2 = new LinkedHashMap<>();
                for (Map.Entry<Integer, Double> entry : entryList2) {
                    sortedMap2.put(entry.getKey(), entry.getValue());
                }

                int num = 10;
                int temp = 0;
                for (Map.Entry<Integer, Double> entry : sortedMap2.entrySet()) {
                    if (num == 0) {
                        System.out.println("\n");
                        bufferedWriter.write("\n");
                        break;
                    }
                    bufferedWriter.write(second_part.get(entry.getKey()).getName() + ": " + entry.getValue() + "\n");
                    System.out.println(second_part.get(entry.getKey()).getName() + ": " + entry.getValue());

                    List<String> check = checkMatch(test_bug.getName(), second_part.get(entry.getKey()).getName());
                    System.out.println(check);
                    bufferedWriter.write(check.toString() + "\n");
                    if (!check.isEmpty()) {
                        temp = 1;
                    }
                    num--;
                }
                ok = ok + temp;
            }
            bufferedWriter.close();
        }
    }

    public List<String> checkMatch(String bug_name, String test_name) {
        String query = "match (n:issues{name:$name})-[:solve]->(m:javaFile) return m.name";
        Session session = driver.session();
        Result bug_res = session.run(query, parameters("name", bug_name));
        Result test_res = session.run(query, parameters("name", test_name));
        HashSet<String> bug_res_list = new HashSet<>();
        List<String> res = new ArrayList<>();
        while (bug_res.hasNext()) {
            Record record = bug_res.next();
            String name = record.get("m.name").asString();
            bug_res_list.add(name);
        }
        while (test_res.hasNext()) {
            Record record = test_res.next();
            String name = record.get("m.name").asString();
            if (bug_res_list.contains(name)) {
                res.add(name);
            }
        }
        return res;
    }

    @Override
    public void run(){
        try {
            test_data();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4j123"));
        String project = "CASSANDRA";
        String version_path = "D:\\Bug_location\\process\\statistics\\"+project+"\\versions.txt";
        Path path = Paths.get(version_path);
        String Ste = new String(Files.readAllBytes(path));
        JSONObject jsonObject = new JSONObject(Ste);
        JSONObject ProjectObject = jsonObject.getJSONObject(project);
        // 获取所有键并存入 List
        List<String>versions = new ArrayList<>(ProjectObject.keySet());
        // 排序版本号
        Collections.sort(versions, (v1, v2) -> {
            String[] parts1 = v1.split("\\.");
            String[] parts2 = v2.split("\\.");
            for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            }
            return Integer.compare(parts1.length, parts2.length); // 比较长度
        });
        System.out.println(versions);

        for(int version_index = 0;version_index<versions.size();version_index++){
            new Thread(new Approach(driver,project,versions,version_index)).start();
        }
    }
}
