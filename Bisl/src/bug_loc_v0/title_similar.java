package bug_loc_v0;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.neo4j.driver.Driver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class title_similar {
    private TokenizerModel token_model;
    private POSModel pos_model;
    private word2vec_class my_word2vec;

    public title_similar(){
        try {
            InputStream modelIn = new FileInputStream("D:\\Bug_location\\process\\opennlp_models\\opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
            token_model = new TokenizerModel(modelIn);
            modelIn = new FileInputStream("D:\\Bug_location\\process\\opennlp_models\\opennlp-en-ud-ewt-pos-1.0-1.9.3.bin");
            pos_model = new POSModel(modelIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        my_word2vec = new word2vec_class();
    }

    public HashSet<String> extract_n_adj(String source){
        //实例化分词器
        //Tokenizer = new TokenizerME(token_model);
        //String[] tokens = tokenizer.tokenize(source);
        /*
        //实例化句子检测器
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(sen_model);
        String[] sentences = sentenceDetectorME.sentDetect("sentence");
        */
        HashSet<String> res = new HashSet<>();
        List<String> tokens_list = my_word2vec.filterWords(source);
        String[] tokens = tokens_list.toArray(new String[0]);
        //实例化词性标注器
        POSTaggerME posTaggerME = new POSTaggerME(pos_model);
        String[] tags = posTaggerME.tag(tokens);
        //System.out.println(Arrays.toString(tokens));
        //System.out.println(Arrays.toString(tags));
        for (int i =0;i<tags.length;i++){
            if(tags[i].equals("ADJ") || tags[i].equals("NOUN") || tags[i].equals("PROPN")||tags[i].equals("X")){
                res.add(tokens[i]);
            }
        }
        //System.out.println(res);
        return res;
    }

    public double cal_word_similar(String word1,String word2){
        //通过计算最长连续子序列来计算单词之间的相似度
        int m = word1.length();
        int n = word2.length();
        // 创建一个二维数组来存储子问题的解
        int[][] dp = new int[m + 1][n + 1];
        int maxLength = 0; // 最长连续子序列的长度
        int endIdx = 0; // 最长连续子序列的结束索引
        // 动态规划过程
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1) || Character.toUpperCase(word1.charAt(i - 1)) == word2.charAt(j-1)||Character.toLowerCase(word1.charAt(i-1))==word2.charAt(j-1)) {
                    // 如果当前字符相同，则在前一个状态的基础上加1
                    dp[i][j] = dp[i - 1][j - 1] + 1;

                    // 更新最长连续子序列的长度和结束索引
                    if (dp[i][j] > maxLength) {
                        maxLength = dp[i][j];
                        endIdx = i - 1; // 更新为当前字符的结束索引
                    }
                }
            }
        }
        // 根据最长连续子序列的结束索引和长度，找到起始索引
        //int startIdx = endIdx - maxLength + 1;
        // 输出最长连续子序列
        //String longestSubsequence = word1.substring(startIdx, endIdx + 1);
        //System.out.println("最长连续子序列为: " + longestSubsequence);
        //System.out.println(maxLength);
        // 返回最长连续子序列的长度
        return (double) maxLength /Math.max(m,n);
    }


    public double cal_title(HashSet<String> bug, HashSet<String>  issue){
        //计算标题之间的相似度，将bug中的每个单词与issue比较，记录对应相似度最大的再取平均值
        //HashSet<String> bugs = extract_n_adj(bug);
        //HashSet<String> issues = extract_n_adj(issue);
        ArrayList<String> bug_list = new ArrayList<>(bug);
        ArrayList<String> issue_list = new ArrayList<>(issue);
        //System.out.println(bug_list);
        //System.out.println(issue_list);

        double[] title_similar = new double[bug_list.size()];
        for(int i=0;i<bug_list.size();i++){
            double temp = 0;
            for(int j=0;j<issue_list.size();j++){
                double v = cal_word_similar(bug_list.get(i), issue_list.get(j));
                if (v>temp){
                    temp = v;
                }
            }
            title_similar[i] = temp;
        }
        //System.out.println(Arrays.toString(title_similar));
        double arg=0;
        for(int i=0;i<title_similar.length;i++){
            arg = arg+title_similar[i];
        }
        return arg/title_similar.length;
    }


    public static void main(String[] args) {
        title_similar issuesSimilar = new title_similar();
        String str1 = "Unresolved hostname in replace address";
        String str2 = "gossip and tokenMetadata get hostId out of sync on failed replace_node with the same IP address";
        HashSet<String> extract_n_adj = new HashSet<>();
        extract_n_adj.add("unresolved");
        HashSet<String> extract_n_adj1 = issuesSimilar.extract_n_adj(str2);
        System.out.println(extract_n_adj);
        System.out.println(extract_n_adj1);
        double title_similar = issuesSimilar.cal_title(extract_n_adj,extract_n_adj1);
        System.out.println(title_similar);
    }
}
