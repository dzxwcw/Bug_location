package bug_loc_v0;

import java.util.HashSet;
import java.util.List;

public class issue_report {
    private String name;
    private String summary;
    private String type;
    private String description;
    private HashSet<String> extract_summary;
    private List<String> extract_description;
    private HashSet<String> extract_method;
    private HashSet<String> extract_exception;
    private HashSet<String> extract_methodName;
    private HashSet<String> extract_class;
    private List<String> link_files;

    public issue_report(String name, String summary, String description,List<String> list) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.link_files = list;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashSet<String> getExtract_summary() {
        return extract_summary;
    }

    public void setExtract_summary(HashSet<String> extract_summary) {
        this.extract_summary = extract_summary;
    }

    public List<String> getExtract_description() {
        return extract_description;
    }

    public void setExtract_description(List<String> extract_description) {
        this.extract_description = extract_description;
    }

    public HashSet<String> getExtract_exception() {
        return extract_exception;
    }

    public void setExtract_exception(HashSet<String> extract_exception) {
        this.extract_exception = extract_exception;
    }

    public HashSet<String> getExtract_methodName() {
        return extract_methodName;
    }

    public void setExtract_methodName(HashSet<String> extract_methodName) {
        this.extract_methodName = extract_methodName;
    }

    public HashSet<String> getExtract_class() {
        return extract_class;
    }

    public void setExtract_class(HashSet<String> extract_class) {
        this.extract_class = extract_class;
    }

    public HashSet<String> getExtract_method() {
        return extract_method;
    }

    public void setExtract_method(HashSet<String> extract_method) {
        this.extract_method = extract_method;
    }
}
