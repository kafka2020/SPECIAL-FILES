import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testListToJson() {
        // Подготовка тестовых данных
        List<Employee> testEmployees = List.of(
                new Employee(1, "Ivan", "Ivanov", "RU", 30)
        );

        // Вызов тестируемого метода
        String json = Main.listToJson(testEmployees);

        // Проверка результата
        assertTrue(json.contains("\"id\": 1"));
        assertTrue(json.contains("\"firstName\": \"Ivan\""));
    }

    @Test
    void testJsonToList() {
        // Подготовка тестовых данных
        String testJson = "[{\"id\":2,\"firstName\":\"Anna\",\"lastName\":\"Petrova\",\"country\":\"US\",\"age\":25}]";

        // Вызов тестируемого метода
        List<Employee> employees = Main.jsonToList(testJson);

        // Проверка результата
        assertEquals(1, employees.size());
        assertEquals("Anna", employees.get(0).firstName);
    }

    @Test
    void testWriteAndReadString() {
        // Подготовка тестовых данных
        String testContent = "test content";
        String testFileName = "test_file.txt";

        // Тестируем запись
        Main.writeString(testContent, testFileName);

        // Тестируем чтение
        String readContent = Main.readString(testFileName);

        // Проверка
        assertEquals(testContent, readContent);

        // Удаляем временный файл
        new File(testFileName).delete();
    }

    @Test
    void testParseCSV(@TempDir Path tempDir) throws Exception {
        // 1. Создаём временный CSV-файл
        File csvFile = tempDir.resolve("employees.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("1,John,Smith,USA,25\n");
            writer.write("2,Ivan,Petrov,RU,23\n");
        }

        // 2. Маппинг колонок
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        // 3. Запускаем парсинг
        List<Employee> employees = Main.parseCSV(columnMapping, csvFile.getAbsolutePath());

        // 4. Проверки
        assertEquals(2, employees.size(), "Должно быть 2 сотрудника");

        Employee first = employees.get(0);
        assertEquals(1, first.id);
        assertEquals("John", first.firstName);
        assertEquals("Smith", first.lastName);
        assertEquals("USA", first.country);
        assertEquals(25, first.age);

        Employee second = employees.get(1);
        assertEquals(2, second.id);
        assertEquals("Ivan", second.firstName);
        assertEquals("Petrov", second.lastName);
        assertEquals("RU", second.country);
        assertEquals(23, second.age);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "good.csv",    // файл с корректными данными
            "empty.csv",   // пустой файл
            "broken.csv"   // файл с ошибками
    })
    void testParseCSV_WithValueSource(String filename) {
        String path = "src/test/resources/" + filename;
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        switch(filename) {
            case "good.csv":
                List<Employee> goodEmployees = Main.parseCSV(columnMapping, path);
                assertEquals(2, goodEmployees.size());
                break;

            case "empty.csv":
                List<Employee> emptyEmployees = Main.parseCSV(columnMapping, path);
                assertTrue(emptyEmployees.isEmpty());
                break;

            case "broken.csv":
                List<Employee> brokenEmployees = Main.parseCSV(columnMapping, path);
                assertTrue(brokenEmployees.isEmpty(), "Для битого CSV должен вернуться пустой список");
                break;
        }
    }
}