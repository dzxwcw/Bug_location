package bug_loc_v0;
import com.huaban.analysis.jieba.JiebaSegmenter;
import opennlp.tools.stemmer.PorterStemmer;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import javax.swing.plaf.IconUIResource;
import java.util.*;

public class word2vec_class {
    private Word2Vec word2Vec;
    private Set<String> stopwords;
    private JiebaSegmenter segmenter;
    public word2vec_class(String modelPath) {
        word2Vec = WordVectorSerializer.readWord2VecModel(modelPath);
        segmenter = new JiebaSegmenter();
        initStopwords();
    }
    public word2vec_class(){
        segmenter = new JiebaSegmenter();
        initStopwords();
    }

    private void initStopwords(){
        stopwords = new HashSet<>();
        stopwords.add(")");
        stopwords.add("(");
        stopwords.add(",");
        stopwords.add("'");
        stopwords.add("()");
        stopwords.add(" ");
        stopwords.add("\"");
        stopwords.add(".");
        stopwords.add("#");
        stopwords.add("-");
        stopwords.add("{");
        stopwords.add("}");
    }
    public List<String> filterWords(String words){
        String[] word_list = words.split(" ");
        List<String> filter = new ArrayList<>();
        for(String s: word_list){
            List<String> word = new ArrayList<>();
            if(!s.matches(".*n't")){
                word = segmenter.sentenceProcess(s);
            }
            else {
                word.add(s);
            }
            for(String temp : word){
                if(!stopwords.contains(temp)){
                    filter.add(temp);
                }
            }
        }
        return filter;
    }

    public double calculateSimilarity(String sentence1, String sentence2){
        List<String> sen1 = filterWords(sentence1);
        List<String> sen2 = filterWords(sentence2);
        //System.out.println(sen1);
        //System.out.println(sen2);
        double[] vector1 = sentenceSimilar(sen1);
        double[] vector2 = sentenceSimilar(sen2);

        double similarity = cosineSimilarity(vector1, vector2);
        return similarity;
    }

    public double[] sentenceSimilar(List<String> words){
        double[] argVector = new double[word2Vec.getWordVector(word2Vec.vocab().wordAtIndex(0)).length];
        for (String word : words){
            Collection<String> all_words = word2Vec.vocab().words();
            double[] wordVector = new double[0];
            if(all_words.contains(word)){
                wordVector = word2Vec.getWordVector(word);
            }
            else{
                wordVector = word2Vec.getWordVector(word);
            }
            if(wordVector!=null){
                for (int i=0;i<wordVector.length;i++){
                    argVector[i] += wordVector[i];
                }
            }
            //System.out.println(Arrays.toString(wordVector));
        }

        for (int i =0;i<argVector.length;i++){
            argVector[i] = argVector[i]/words.size();
        }

        //System.out.println("words:" + words);
        //System.out.println("vector:" + Arrays.toString(argVector));
        return argVector;
    }

    private double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }


    public static void main(String[] args) {
        String modelPath = "./GoogleNews-vectors-negative300.bin";
        word2vec_class word2vecClass = new word2vec_class(modelPath);
        String str1 = "Typo in exception message at JsonSlurper.parseObject()";
        String str2 = "JsonSlurper's methods are package private";
        String str3 = "JsonSlurper should support URL and File variants for parse";
        String str4 = "Add additional NamedParam annotations in ResourceGroovyMethods,NioExtensions and JsonSlurper";
        String str5 = "Private inheritance bug: Closure accessing private method";
        double res = word2vecClass.calculateSimilarity(str1, str3);
        System.out.println(res);
    }
}
