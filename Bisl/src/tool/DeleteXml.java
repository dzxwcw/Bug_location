
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.JavaParser;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeleteXml {
    public static boolean check(String summary, String description, String filename, String path){
        List<String > package_set = new ArrayList<>();
        String[] split = filename.split("\\.");
        int k=3;
        for(int i=0;i<Math.min(k,split.length-1);i++){
            package_set.add(split[split.length-i-2]);
        }

        for (int i=0;i<package_set.size();i++){
            if (summary.contains(package_set.get(i) )|| description.contains(package_set.get(i))){
                return false;
            }
        }

        String file = path+"\\"+filename;
        Set<String> func_set = new HashSet<>();
        try (FileInputStream in = new FileInputStream(file)) {
            // 解析 Java 文件
            JavaParser Parser = new JavaParser();
            ParseResult<CompilationUnit> result = Parser.parse(in);
            // Extract class names
            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                // Extract class names
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                    func_set.add(String.valueOf(cls.getName()));
                });

                // Extract method names
                cu.findAll(MethodDeclaration.class).forEach(method -> {
                    func_set.add(String.valueOf(method.getName()));
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String f:func_set){
            if (summary.contains(f)||description.contains(f)){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        List<String> pro = new ArrayList<>();
        pro.add("CALCITE");
        pro.add("CASSANDRA");
        pro.add("FLINK");

        for (String project:pro){
            String data_folder = "/home/haozhong/project/approach/Bench4BL/data/Apache/"+project+"/bugrepo/repository";
            org.dom4j.Document document_merge = DocumentHelper.createDocument();
            org.dom4j.Element bugrepository_merge = document_merge.addElement("bugrepository");
            bugrepository_merge.addAttribute("name", project);

            File[] files = new File(data_folder).listFiles();
            int before =0;
            int after = 0;
            for(File file: files){
                String name = file.getName();
                // 创建 DocumentBuilderFactory
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                // 解析 XML 文件
                Document document = builder.parse(file);
                // 获取 employee 节点
                NodeList BugList = document.getElementsByTagName("bug");
                before = before+BugList.getLength();
                after = after +BugList.getLength();
                for (int i=0;i<BugList.getLength();i++){
                    Node bug = BugList.item(i);
                    //获取子节点信息
                    Element bug_ele = (Element) bug;
                    Node buginformation = bug_ele.getElementsByTagName("buginformation").item(0);
                    Node fixedFiles = bug_ele.getElementsByTagName("fixedFiles").item(0);
                    Element bug_inf = (Element) buginformation;
                    Element fixed_file = (Element)fixedFiles;
                    String summary = bug_inf.getElementsByTagName("summary").item(0).getTextContent();
                    String description = bug_inf.getElementsByTagName("description").item(0).getTextContent();
                    NodeList link_file = fixed_file.getElementsByTagName("file");
                    String source_path ="/home/haozhong/project/approach/Bench4BL/data/Apache/"+project+"/sources/"+name.split("\\.")[0]+"/";
                    for (int j =0;j<link_file.getLength();j++){
                        String filename = link_file.item(j).getTextContent();
                        if (!check(summary,description,filename,source_path)){
                            //删除结点
                            Node parentNode = bug.getParentNode();
                            parentNode.removeChild(bug);
                            i--;
                            after--;
                            break;
                        }
                    }
                    if (!(bug ==null)){
                        // 深度克隆节点（包括子节点）
                        Node clonedBug = bug.cloneNode(true);


                    }
                }
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            }
            // 5、设置生成xml的格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            // 设置编码格式
            format.setEncoding("UTF-8");
            // 6、生成xml文件
            String target_file = "/home/haozhong/project/approach/Bench4BL/data/Apache/"+project+"/bugrepo/repository.xml";
            File file = new File(target_file);
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
            // 设置是否转义，默认使用转义字符
            writer.setEscapeText(true);
            writer.write(document_merge);
            writer.close();

            System.out.println(project+"\n");
            System.out.println("before = "+before+"\n");
            System.out.println("after = "+after+"\n");
        }
    }
}
