import com.github.javaparser.ParseResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.datavec.api.writable.LongWritable;

import org.neo4j.driver.*;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.JavaParser.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.neo4j.driver.Record;

import javax.management.openmbean.CompositeData;

import static com.github.javaparser.JavaParser.*;
import static org.neo4j.driver.Values.parameters;
import static sun.management.MonitorInfoCompositeData.getClassName;

public class general_csv_xml {
    public static int getLongestCommonSubsequenceLength(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];
        int maxLength = 0;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxLength = Math.max(maxLength, dp[i][j]);
                } else {
                    dp[i][j] = 0;
                }
            }
        }

        return maxLength;
    }

    public static boolean check(List<String> link_files, String summary, String description){
        for(String file : link_files){
            String[] split = file.split("\\.");
            String filename = "";
            if (split.length>1){
                filename = split[split.length-2];
            }
            else {
                filename = split[0];
            }
            int length = filename.length();
            int l1 = getLongestCommonSubsequenceLength(filename, summary);
            int l2 = getLongestCommonSubsequenceLength(filename,description);
            if (l1==length || l2 ==length){
                return false;
            }
        }
        return  true;
    }

    public static boolean Delete(List<String> link_files,String project,String summary, String description,String version){
        String path = "D:\\Bug_location\\process\\statistics\\CALCITE\\sources\\"+project+"\\sources\\";
        path = path+project+"_"+version.replace(".","_")+"\\";
        for(String f : link_files){
            String[] split = f.split("\\.");
            if (split[split.length-1].equals("java")){
                String filepath =path;
                for (int k =0;k<split.length-2;k++){
                    filepath=filepath+split[k]+"\\";
                }
                filepath=filepath+split[split.length-2]+"."+split[split.length-1];
                List<String> classes = new ArrayList<>();
                List<String> methods = new ArrayList<>();
                try {
                    JavaParser parser = new JavaParser();
                    File file = new File(filepath);
                    ParseResult<CompilationUnit> result = parser.parse(file);
                    // 检查解析结果
                    if (result.isSuccessful() && result.getResult().isPresent()) {
                        CompilationUnit cu = result.getResult().get();

                        // 提取类名
                        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                            classes.add(classDecl.getNameAsString());
                        });

                        // 提取方法名
                        cu.findAll(MethodDeclaration.class).forEach(methodDecl -> {
                            methods.add( methodDecl.getNameAsString());
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (String c: classes){
                    if (summary.contains(c)||description.contains(c)){
                        return false;
                    }
                }
                for (String m: methods){
                    if (summary.contains(m)||description.contains(m)){
                        return false;
                    }
                }
            }
            else {
                continue;
            }
        }
        return true;
    }

    public static void OutputVersion(List<Map.Entry<String, List<String>>> entries,String project){
        JSONObject object = new JSONObject();
        JSONObject root = new JSONObject();

        for (Map.Entry<String, List<String>> entry : entries) {
            if (entry.getValue().size()<10){
                break;
            }
            else{
                String key = entry.getKey();
                String version =  project.toLowerCase()+"-"+key;
                object.put(key,version);
            }
        }
        root.put(project,object);
        String path = "D:\\Bug_location\\process\\statistics\\"+project+"\\versions.txt";
        try (FileWriter file = new FileWriter(path)){
            file.write(root.toString(4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void construct_version_bug(List<Map.Entry<String, List<String>>> entries,String project) throws IOException {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j123"));
        Session session = driver.session();
        String query = "MATCH (n:issues) WHERE n.name = $pro RETURN n.title,n.description,n.created,n.resolved,n.resolution,n.type,n.affect_version,n.fix_version";
        String query2 = "match (n:issues{name:$name})-[:solve]->(m:javaFile) return m.name";

        Document document_merge = DocumentHelper.createDocument();
        Element bugrepository_merge = document_merge.addElement("bugrepository");
        bugrepository_merge.addAttribute("name", project);
        int before = 0;
        int part1=0;
        int after = 0;

        for (Map.Entry<String, List<String>> entry : entries){
            if (entry.getValue().size()<10){
                break;
            }
            Document document = DocumentHelper.createDocument();
            Element bugrepository = document.addElement("bugrepository");
            bugrepository.addAttribute("name", project);

            String version = entry.getKey();
            List<String> bug_repos = entry.getValue();
            //计算删之前的数目
            before = before+bug_repos.size();
            for(String bug:bug_repos){
                Result result = session.run(query, parameters("pro",bug));
                Record record = result.next();
                String type = record.get("n.type").asString();
                String summary = record.get("n.title").asString();
                String description = record.get("n.description").asString();
                String created = record.get("n.created").asString();
                String resolved = record.get("n.resolved").asString();
                String resolution = record.get("n.resolution").asString();
                String affect_version = record.get("n.affect_version").asString();
                String fix_version = record.get("n.fix_version").asString();

                Result result1 = session.run(query2, parameters("name", bug));
                List<String > link_files = new ArrayList<>();
                while (result1.hasNext()){
                    Record record1 = result1.next();
                    String link_file = record1.get("m.name").asString();
                    link_file = link_file.replace("_",".");
                    link_files.add(link_file);
                }

                if (link_files.isEmpty()){
                    continue;
                }
                part1++;
                if (!check(link_files,summary,description)){
                    continue;
                }
                //if (!Delete(link_files,project,summary,description,version)){
                //    continue;
                //}
                //计算删除之后的数量
                after++;

                //生成版本xml
                Element bug_repo = bugrepository.addElement("bug");
                Element bug_repo1 = bugrepository_merge.addElement("bug");
                bug_repo.addAttribute("id",bug.split("-")[1]);
                bug_repo1.addAttribute("id",bug.split("-")[1]);

                String opendate = created.split("/")[2]+"-"+created.split("/")[1]+"-"+created.split("/")[0]+" 00:00:00";
                String fixdate = created.split("/")[2]+"-"+resolved.split("/")[1]+"-"+created.split("/")[0]+" 01:00:00";
                bug_repo.addAttribute("opendate",opendate);
                bug_repo.addAttribute("fixdate",fixdate);
                bug_repo.addAttribute("resolution",resolution);
                bug_repo1.addAttribute("opendate",opendate);
                bug_repo1.addAttribute("fixdate",fixdate);
                bug_repo1.addAttribute("resolution",resolution);
                Element buginformation = bug_repo.addElement("buginformation");
                Element buginformation1 = bug_repo1.addElement("buginformation");
                //填写bug信息
                Element bug_summary = buginformation.addElement("summary");
                bug_summary.setText(summary);
                Element bug_summary1 = buginformation1.addElement("summary");
                bug_summary1.setText(summary);

                Element bug_description = buginformation.addElement("description");
                bug_description.setText(description);
                Element bug_description1 = buginformation1.addElement("description");
                bug_description1.setText(description);

                Element bug_version = buginformation.addElement("version");
                bug_version.setText(affect_version);
                Element bug_version1 = buginformation1.addElement("version");
                bug_version1.setText(affect_version);

                Element fixedVersion = buginformation.addElement("fixedVersion");
                fixedVersion.setText(fix_version);
                Element fixedVersion1 = buginformation1.addElement("fixedVersion");
                fixedVersion1.setText(fix_version);

                Element bug_type = buginformation.addElement("type");
                bug_type.setText(type);
                Element bug_type1 = buginformation1.addElement("type");
                bug_type1.setText(type);
                //填写bug链接文件
                Element fixedFiles = bug_repo.addElement("fixedFiles");
                Element fixedFiles1 = bug_repo1.addElement("fixedFiles");
                for (String f:link_files) {
                    Element file = fixedFiles.addElement("file");
                    file.addAttribute("type", "M");
                    file.setText(f);
                    Element file1 = fixedFiles1.addElement("file");
                    file1.addAttribute("type", "M");
                    file1.setText(f);
                }
            }
            // 5、设置生成xml的格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置编码格式
            format.setEncoding("UTF-8");
            // 6、生成xml文件
            String file_name = project+"_"+ version.replace(".","_");
            String target_file = "D:\\Bug_location\\process\\statistics\\"+project+"\\repository\\"+file_name+".xml";
            File file = new File(target_file);
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(true);
            writer.write(document);
            writer.close();

        }
        // 5、设置生成xml的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");
        // 6、生成xml文件
        String target_file = "D:\\Bug_location\\process\\statistics\\"+project+"\\repository.xml";
        File file = new File(target_file);
        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
        // 设置是否转义，默认使用转义字符
        writer.setEscapeText(true);
        writer.write(document_merge);
        writer.close();
        System.out.println(project);
        System.out.println("before: "+ before);
        System.out.println("after: "+after);
        System.out.println("part: "+part1);
        System.out.println("\n");
    }

    public static void main(String[] args) throws IOException {
        //Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j123"));
        FileReader reader= new FileReader("D:\\neo4j\\neo4j-community-5.13.0\\import\\bug_table.csv");
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT);
        List<String> pro = new ArrayList<>();
        //pro.add("CALCITE");
        //pro.add("CASSANDRA");
        //pro.add("DERBY");
        //pro.add("FLINK");
        //pro.add("GEODE");
        //pro.add("HBASE");
        //pro.add("HIVE");
        //pro.add("NUTCH");
        pro.add("ARIES");
        for (String project:pro){
            //定义List用于接收文件数据
            ArrayList<String> list = new ArrayList<>();
            Set<String> version_set = new TreeSet<>();
            Map<String, List<String>> version_map = new HashMap<>();
            int k = 0;
            String current_row = "";
            for (CSVRecord record:parser){
                if(record.get(0).startsWith(project) && !record.get(6).equals("None")){
                    current_row = record.get(6);
                    break;
                }
            }

            for (CSVRecord record:parser){
                if(k==0){
                    k=1;
                    continue;
                }
                list.clear();
                for(int i=0;i<record.size();i++){
                    list.add(record.get(i));
                }
                //更改project
                if(list.get(0).startsWith(project)){

                    String v  = list.get(6);
                    String f_v = list.get(7);
                    String f_name =list.get(0);
                    if(v.equals("None")){
                        v = current_row;
                    }
                    else{
                        current_row= v;
                    }

                    String[] split_v = v.split(",");
                    String flag ="";
                    for(String affect_v : split_v){
                        /*
                        if(Character.isLetter(affect_v.charAt(0))||affect_v.equals("0.4")){
                            flag = affect_v;
                        }
                        else{
                            if (f_v.equals("None")||!Character.isLetter(f_v.charAt(0))){
                                continue;
                            }
                            for(int i=0;i< f_v.length();i++){
                                if(f_v.charAt(i)>='0' && f_v.charAt(i)<='9'){
                                    flag = flag+affect_v;
                                    break;
                                }
                                else{
                                    flag = flag+f_v.charAt(i);
                                }
                            }
                        }
                        */
                        flag = affect_v;
                        if(!version_map.containsKey(flag) && !flag.isEmpty()){
                            List<String> temp = new ArrayList<>();
                            temp.add(f_name);
                            version_map.put(flag,temp);
                        }
                        else {
                            version_map.get(flag).add(f_name);
                        }
                    }
                }
                else if(!version_map.isEmpty()){
                    break;
                }
            /*
            //创建xml文件
            Document document = DocumentHelper.createDocument();
            Element rss = document.addElement("rss");
            // 3、向rss节点添加version属性
            rss.addAttribute("version", "1.0");
            Element channel = rss.addElement("channel");
            Element title = channel.addElement("title");
            title.setText("ASF JIRA");
            Element link = channel.addElement("link");
            link.setText("https://issues.apache.org/jira");
            Element description = channel.addElement("description");
            description.setText("This file is an XML representation of an issue");
            Element language = channel.addElement("language");
            language.setText("en-uk");

            Element item = channel.addElement("item");
            Element bug_title = item.addElement("title");
            bug_title.setText("["+list.get(0)+"]"+list.get(1));
            Element bug_description = item.addElement("description");
            bug_description.setText(list.get(13));
            Element summary = item.addElement("summary");
            summary.setText(list.get(1));
            Element type = item.addElement("type");
            type.setText(list.get(2));
            Element priority = item.addElement("priority");
            priority.setText(list.get(4));
            Element status = item.addElement("status");
            status.setText(list.get(3));
            Element resolution = item.addElement("resolution");
            resolution.setText(list.get(5));
            Element assignee = item.addElement("assignee");
            assignee.setText(list.get(9));
            Element reporter = item.addElement("assignee");
            reporter.setText(list.get(10));
            Element created = item.addElement("created");
            created.setText(list.get(14));
            Element updated = item.addElement("updated");
            updated.setText(list.get(15));
            Element resolved = item.addElement("resolved");
            resolved.setText(list.get(16));
            Element version = item.addElement("version");
            version.setText(list.get(6));
            Element fixedVersion = item.addElement("fixedVersion");
            fixedVersion.setText(list.get(7));

            // 5、设置生成xml的格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置编码格式
            format.setEncoding("UTF-8");
            // 6、生成xml文件
            String target_file = "D:\\Bug_location\\process\\statistics\\ARIES\\bugs\\"+list.get(0)+".xml";
            File file = new File(target_file);
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(false);
            writer.write(document);
            writer.close();

            //Session session = driver.session();
            //String cypherQuery = "CREATE (n:issues {name: $name, title:$title, type:$type, status:$status, priority:$priority,resolution:$resolution,affect_version:$affect_version,fix_version:$fix_version,component:$component,assignee:$assignee,reporter:$reporter,description:$description,created:$created,updated:$updated,resolved:$resolved});";
            //session.run(cypherQuery , parameters("name",list.get(0),"title",list.get(1),"type",list.get(2),"status",list.get(3),"priority",list.get(4),"resolution",list.get(5),"affect_version",list.get(6),"fix_version",list.get(7),"component",list.get(8),"assignee",list.get(9),"reporter",list.get(10),"description",list.get(13),"created",list.get(14),"updated",list.get(15),"resolved",list.get(16)));
            //System.out.println(list);
            */
            }
            List<Map.Entry<String, List<String>>> entries = new ArrayList<>(version_map.entrySet());
            entries.sort(Comparator.comparing(e->e.getValue().size(), Comparator.reverseOrder()));
            //OutputVersion(entries,project);
            construct_version_bug(entries,project);
        }
    }
}
