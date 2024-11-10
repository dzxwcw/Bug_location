### Locating Fault with Issue-Fix Graph

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

#### Repository Directory Structure

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

*   Experiment: This folder includes all expreiment result of baselines and BisL.

    *   BugLocator:

        *   recommended\_multiple: This folder includes all recommended buggy files of each version using the multiple version matching strategy.

        *   recommender\_latest: This folder includes all recommended buggy files using the latest version matching strategy.

        *   answer\_multiple: This folder includes the value of MAP and MRR of the multiple version strategy.

        *   answer\_latest: This folder includes the value of MAP and MRR of the latest version strategy.

    *   BRTeacer

    *   BisL

