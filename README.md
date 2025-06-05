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
