package bug_loc_v0;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class description_similar {
    private SentenceModel sen_model;
    private String description;

    public description_similar(String str) throws IOException {
        InputStream modelIn = new FileInputStream("D:\\Bug_location\\process\\opennlp_models\\opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
        sen_model = new SentenceModel(modelIn);
        description = str;
    }

    public double cal_semantics_similar(String bug_des, String issue_des){
        double res = 0;
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(sen_model);
        String[] bug_sens = sentenceDetectorME.sentDetect(bug_des);
        String[] issue_sens = sentenceDetectorME.sentDetect(issue_des);
        for(int i=0;i<bug_sens.length;i++){
            System.out.println(bug_sens[i]);
        }
        return  res;
    }

    public void extract_message(){
        String message = this.description;
        String method_pattern = "[a-zA-Z]+\\([a-zA-Z]*\\)";
        String exception_pattern = "[a-zA-Z]+Exception";
        String at_pattern = "^[a-zA-Z]+(\\.[a-zA-Z]+)+";
        Pattern pattern = Pattern.compile(at_pattern);
        Matcher matcher = pattern.matcher(message);
        while(matcher.find()){
            String str = matcher.group(0);
            System.out.println(str);
        }
    }

    public static void main(String[] args) throws IOException {
        String bug_des ="Given the class  class SomeClass {  // this is the inaccessible static attribute called  public static Object Class = null;  }  if we try to access the class static attribute from a Groovy script, it won't work.  An actual example is the com.hp.hpl.jena.vocabulary.OWL class in the jena.sf.net project.  Here's the test script also attached to this issue:  import com.hp.hpl.jena.vocabulary.*  println OWL.class  println OWL.Class  println OWL.class  println OWL.@Class  ... here's the output:  class com.hp.hpl.jena.vocabulary.OWL  class java.lang.Class  class java.lang.Class  Caught: groovy.lang.MissingFieldException: No such field: Class for class: java.lang.Class  The expected behaviour would be calling OWL.Class and getting the value of the attribute. class SomeClass {  // this is the inaccessible static attribute called 'Class'  public static Object Class = null;  } import com.hp.hpl.jena.vocabulary.* ... here's the output:  class com.hp.hpl.jena.vocabulary.OWL  class java.lang.Class  class java.lang.Class  Caught: groovy.lang.MissingFieldException: No such field: Class for class: java.lang.Class Given the class if we try to access the 'Class' static attribute from a Groovy script, it won't work. An actual example is the com.hp.hpl.jena.vocabulary.OWL class in the jena.sf.net project. Here's the test script also attached to this issue: println OWL.class  println OWL.Class  println OWL.'Class'  println OWL.@Class The expected behaviour would be calling OWL.Class and getting the value of the attribute. ";
        String issue_des="\"When XmlNodePrinter prints out an attribute, it embeds strings within quotes, but only strings - not GStrings.  This is very counter-intuitive. Here's a failing test (easier to c'n'p than upload):  int x = 5  Node node = new Node (null, 'root', [attr:\"$x\"])  StringWriter sw = new StringWriter()  PrintWriter pw = new PrintWriter(sw)  new XmlNodePrinter(pw).print(node)  assert sw.toString()=='<root attr=\"5\">'  The actual value is <root attr=5> which isn't valid XML. Changing \"$x\" to \"$x\".toString() works, but is clunky.  It's not clear to me why XmlNodePrinter.printNameAttributes expects the value to quote itself if it's not a String. What kind of value would quote itself? int x = 5  Node node = new Node (null, 'root', [attr:'$x']) StringWriter sw = new StringWriter()  PrintWriter pw = new PrintWriter(sw)  new XmlNodePrinter(pw).print(node)  assert sw.toString()=='<root attr='5'>' When XmlNodePrinter prints out an attribute, it embeds strings within quotes, but only strings - not GStrings.  This is very counter-intuitive. Here's a failing test (easier to c'n'p than upload): The actual value is <root attr=5> which isn't valid XML. Changing '$x' to '$x'.toString() works, but is clunky. It's not clear to me why XmlNodePrinter.printNameAttributes expects the value to quote itself if it's not a String. What kind of value would quote itself? ";
        String test = "org.sd.asda";
        description_similar bugreport = new description_similar(test);
        //double v = bugreport.cal_semantics_similar(bug_des, issue_des);
        bugreport.extract_message();
    }
}
