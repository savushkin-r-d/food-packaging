package org.acme.foodpackaging.bootstrap;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.foodpackaging.domain.Job;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonExporter {

    private final Map<String, Object> jsonMap;
    private HardMediumSoftScore totalScore;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public JsonExporter( String DATE, List<Job> jobs) {

        List<JobRecord> jobList = getJobList(jobs);
        this.jsonMap = new LinkedHashMap<>();

        jsonMap.put("DATE", DATE);
        jsonMap.put("Job", jobList);

    }

    public void convertToJsonFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);

        try {
            // Создаем все необходимые родительские директории
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, jsonMap);
            System.out.println("JSON файл успешно создан: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при создании JSON файла: " + e.getMessage());
        }
    }

    private List<JobRecord> getJobList(List<Job> jobs){
        List<JobRecord> jobRecordList = new ArrayList<>(jobs.size());
        for(Job job : jobs){
            jobRecordList.add(new JobRecord(job.getId(),job.getName(),job.getProduct().getType().getDisplayName(),
                    job.getProduct().getGlaze().getDisplayName(),String.valueOf(job.getQuantity()),job.getLine().getName(), String.valueOf(job.getDuration())));
        }
        return jobRecordList;
    }

    @JsonPropertyOrder({"ID", "Job","ProductType", "Glaze", "Quantity", "Line", "Duration"})
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    record JobRecord (String id, String jobName, String type, String glaze, String quantity, String line, String duration) {

        @Override
        @JsonProperty("ID")
        public String id() {
            return id;
        }

        @Override
        @JsonProperty("Job")
        public String jobName() {
            return jobName;
        }

        @Override
        @JsonProperty("ProductType")
        public String type() {
            return type;
        }

        @Override
        @JsonProperty("Glaze")
        public String glaze() {
            return glaze;
        }
        @Override
        @JsonProperty("Quantity")
        public String quantity() {
            return quantity;
        }

        @Override
        @JsonProperty( "Line")
        public String line() {
            return line;
        }

        @Override
        @JsonProperty( "Duration")
        public String duration() {
            return duration;
        }
    }
}
