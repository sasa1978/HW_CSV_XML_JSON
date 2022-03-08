import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.beans.PropertyEditorSupport;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, "data.json");

        list = parseXML("data.xml");
        json = listToJson(list);
        writeString(json, "data2.json");

        json = readString("data.json");
        list = jsonToList(json);
        System.out.printf(list.toString());
    }

    private static <jsonElement> List<Employee> jsonToList(String json) {
        List<Employee> staff = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(json);
            JSONArray jsonArray = (JSONArray) object;
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (Object obj: jsonArray) {
                staff.add(gson.fromJson(obj.toString(), Employee.class));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return staff;
    }

    private static String readString(String fileName) {
        String json = "";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String s;
            while ((s = br.readLine()) != null) {
                json += s;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    private static List<Employee> parseXML(String fileName) {
        List<Employee> staff = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));

            NodeList nodeList = doc.getElementsByTagName("employee");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element employee = (Element) node;
                long id = Long.parseLong(employee.getElementsByTagName("id").item(0).getTextContent());
                String firstName = employee.getElementsByTagName("firstName").item(0).getTextContent();
                String lastName = employee.getElementsByTagName("lastName").item(0).getTextContent();
                String country = employee.getElementsByTagName("country").item(0).getTextContent();
                int age = Integer.parseInt(employee.getElementsByTagName("age").item(0).getTextContent());
                staff.add(new Employee(id, firstName, lastName, country, age));
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return staff;
    }

    private static void writeString(String json, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        String json = gson.toJson(list, listType);

        return json;
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader("data.csv"))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return staff;
    }


}
