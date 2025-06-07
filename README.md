### Locating Fault not Mentioned in Bug Report

We propose a novel approach called BisL to locate fault with Issue-Fix Graph. In contrast to traditional IR-based method, we do not compare with source files but with fixed issue reports. We build IfGraphs from fixed issue reports and propose an algorithm to rank suspicious buggy files. Our approach improves accuracy and stability of fault location. When we remove all bug reports that mention code names of buggy files, the effectiveness of the baselines is very poor, but BisL sitll holds high.

#### Subjects

| **Project** | Git Repository                                                                 |
| :---------- | :----------------------------------------------------------------------------- |
| ARIES       | [https://github.com/apache/aries.git](https://github.com/apache/hbase.git)     |
| CALCITE     | [https://github.com/apache/calcite.git](https://github.com/apache/hbase.git)   |
| CASSANDRA   | [https://github.com/apache/cassandra.git](https://github.com/apache/hbase.git) |
| FLINK       | [https://github.com/apache/flink.git](https://github.com/apache/hbase.git)     |
| GEODE       | [https://github.com/apache/geode.git](https://github.com/apache/hbase.git)     |
| HBASE       | <https://github.com/apache/hbase.git>                                          |
| HIVE        | [https://github.com/apache/hive.git](https://github.com/apache/hbase.git)      |
| NUTCH       | [https://github.com/apache/nutch.git](https://github.com/apache/hbase.git)     |

#### Dataset Directory Structure

*   Dataset: This folder includes all data we crawl from issue trackers. We collect eight github projects such as Aries, Calcite, Cassandra, Flink, Geode, Hbase, Hive and Nutch.

    *   Aries

        *   Fixed Bug: This folder includes all fixed issue reports.

        *   Selected Bug: This folder includes all selected issue reports which do not mention code names of buggy files.&#x20;

        *   version.txt: This file lists all versions we select from Aries project.

    *   Calcite

    *   Cassandra

    *   Flink

    *   Geode

    *   Hbase

    *   Hive

    *   Nutch

![issue](https://github.com/dzxwcw/Bug_location/blob/main/issue%20report%20example/issue.png)

Issue reports are stored in xml format and each report contains information such as fixdate, opendate, summary, description, fixed version and fixed files.&#x20;

#### Code Structure

*   BisL/src

    *   bug\_loc\_v0: This folder includes source code of our proposed apporach.

    *   tool: This folder includes some elevant preprocessor functions.

You should ensure your java version is 1.7 and you need to download and install your own graph database neo4j and the version of it need > 5.13.0

In each function, you need to fill in the port number and the neo4j password like:

    Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j123"));

In the folder tool, you can preprocess your own dataset. Firstly, you need crawl the bug reports and save the information about these bug reports in a .csv file format.

Then, you can use general\_csv\_xml.java to convert the bug report into the .xml format needed for the dataset. You should set the path of you own dataset like:

    FileReader reader= new FileReader("D:\\neo4j\\neo4j-community-5.13.0\\import\\bug_table.csv");

DeleteXml.java can help you remove all bug reports if they mention the code names of faulty files. Again, you need to set the path of .xml files.

import\_graphDatabse.java helps you import your dataset to graph database neo4j.

link\_bugreport\_java.java helps you link each bug report to corresponding faulty files.

CAL\_mrr\_map.java and CAL\_MRR\_MAP\_ALL.java can help you calculate the value of mrr and map for multiply version strategy and the latest  version strategy respectively.



After you have finished pre-processing your dataset or directly using the dataset we have provided, you can run Bisl to locate faulty files.&#x20;

There are two entry function Approach.java and Approach\_all.java which locates faulty files in each of the two version selection strategies.&#x20;

You need to set some path parameters before you run it like :

![](README_md_files/6a2f5030-4347-11f0-850d-f1851df33fef.jpeg?v=1\&type=image)

Besides, you need point out the name of project and its code versions you adopt like:

![](README_md_files/e9abe120-4347-11f0-850d-f1851df33fef.jpeg?v=1\&type=image)

This part will output Intermediate results which means the most similar issue reports.&#x20;

CAL\_result.java will use this result to generate the finally recommended faulty files in your given output folder. &#x20;
