package com.example.container1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@RestController
@RequestMapping("/")
public class container1scan {

    @Value("${FILE_PATH}")
    private String FILE_PATH;

    @PostMapping("calculate")
    public ResponseEntity<Map<String, Object>> operation(@RequestBody Map<Object, Object> input) {

        Map<String, Object> output = new HashMap<>();

        if (!input.containsKey("file") || !input.containsKey("product") || input.get("file") == null) {
            output.put("file", null);
            output.put("error", "Invalid JSON input.");
        } else if (scanFile(input, output) == true) {
            String url = "http://container2-service:9090/calculate?file=" + input.get("file") + "&product=" + input.get("product");
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            output.putAll(result);
            System.out.println("it is true");
        }
        return ResponseEntity.ok(output);
    }


    @PostMapping("store-file")
    public ResponseEntity<Map<String, Object>> storeFile(@RequestBody Map<String, String> input) {

        System.out.println("FILE_PATH: " + FILE_PATH);
        Map<String, Object> output = new HashMap<>();

        if (!input.containsKey("file") || !input.containsKey("data") || input.get("file") == null) {
            output.put("file", null);
            output.put("error", "Invalid JSON input.");
            return ResponseEntity.badRequest().body(output);
        }

        String fileName = input.get("file");
        String data = input.get("data");

        File directory = new File(FILE_PATH);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");
                output.put("error", "Failed to create directory.");
                return ResponseEntity.status(500).body(output);
            }
        } else {
            System.out.println("Directory already exists.");
        }

        File file = new File(directory + "/" + fileName);
        System.out.println("Full file path: " + file.getAbsolutePath());

        // Write data to the file
        try (FileOutputStream writer = new FileOutputStream(file)) {
            System.out.println("Data: " + data);
            writer.write(data.getBytes());

            output.put("file", fileName);
            output.put("message", "Success.");

            return ResponseEntity.ok(output);
        }
        catch (Exception e){
            output.put("file", fileName);
            output.put("error", "Error while storing the file to the storage.");
            return ResponseEntity.status(500).body(output);
        }
    }

    private boolean scanFile(Map<Object, Object> input, Map<String, Object> output) {
        String fileName = (String) input.get("file");
        String filePath = FILE_PATH + "/" + fileName;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if(line.isEmpty()){
                    System.out.println("empty line");
                    continue;
                }
                System.out.println("the line "+ line);
                String[] parts = line.split(",");
                System.out.println("the line parts: " + parts);
                //test2
                //test
                System.out.println("the length of the line parts: " + parts.length);

                if (parts.length != 2) {
                    output.put("file", fileName);
                    output.put("error", "Input file not in CSV format.");
                    return false;
                }
            }
        } catch (IOException e) {
            output.put("file", fileName);
            output.put("error", "File not found.");
            return false;
        }

        return true;
    }
}





 /*   private boolean scanFile(Map<Object, Object> input, Map<String, Object> output) {
        String fileName = (String) input.get("file");
        String filePath = FILE_PATH + "/" + fileName;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    output.put("file", fileName);
                    output.put("error", "Input file not in CSV format.");
                    return false;
                }
            }
        } catch (IOException e) {
            output.put("file", fileName);
            output.put("error", "Error reading file.");
            return false;
        }

        return true;
    }*/

