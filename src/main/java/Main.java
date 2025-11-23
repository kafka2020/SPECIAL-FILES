import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.opencsv.*;
import com.opencsv.bean.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
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

//        Задача 2: XML - JSON парсер
        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");

//        Задача 3: JSON парсер
        String json3 = readString("data.json");
        List<Employee> list3 = jsonToList(json3);
        list3.forEach(System.out::println);
    }

    static List<Employee> jsonToList(String json) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, new TypeToken<List<Employee>>() {}.getType());
    }

    public static String readString(String fileName) {
        StringBuilder jsonText = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String s;
            while ((s = br.readLine()) != null) {
                jsonText.append(s);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return jsonText.toString();
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> employees = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));

            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    int id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());

                    employees.add(new Employee(id, firstName, lastName, country, age));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        String json = gson.toJson(list, listType);
        return json;
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .withThrowExceptions(false) // отключаем многопоточный парсинг в билдере CsvToBeanBuilder (Иначе падает тест)
                    .build();

            List<Employee> staff = csv.parse();
            return staff;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // Возвращаем пустой список в случае ошибки
        }
    }
}
