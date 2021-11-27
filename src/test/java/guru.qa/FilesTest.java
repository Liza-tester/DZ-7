package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Configuration;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilesTest {

    @BeforeAll
    public static void beforeAllTests() {
        Configuration.browserSize = "1900x1200";
    }

    @Test
    @DisplayName("Загрузка файла по абсолютному пути (не рекомендуется)")
    public void visibleFileNameAfterUploadAbsolutePathTest() {
        open("https://translate.google.com/?hl=ru&sl=ru&tl=en&op=docs");
        File exampleFile = new File("C:\\Users\\Professional\\IdeaProjects\\DZ-7\\src\\test\\resources\\goose connection.txt");
        $("input[type = 'file']").uploadFile(exampleFile);
        $x("//span[text()='Перевести']").click();
        $("pre").shouldHave(text("GUSI"));
    }

    @Test
    @DisplayName("Загрузка файла по относительному пути (рекомендуется)")
    public void visibleFileNameAfterUploadClassPathTest() {
        open("https://dropmefiles.com/");
        $("input[type = 'file']").uploadFromClasspath("Aphrodite with a goose.jpg");
        $(byText("загружено")).shouldBe(visible);
    }

    @Test
    @DisplayName("Скачивание txt файла и проверка его содержимого")
    public void downloadTxtFileTest() throws IOException {
        open("https://knigolub.net/romance-fiction/8161-pernatym-ne-mesto-vo-dvorce.html");
        File download = $("span[content = 'txt' ]>a").download();
        System.out.println(download.getAbsolutePath());
        String fileContent = IOUtils.toString(new FileInputStream(download), "UTF-8");
        String expectedTextInFile = "Им пришлось принять чужака.";
        Assertions.assertTrue(fileContent.contains(expectedTextInFile));
    }

    @Test
    @DisplayName("Скачивание pdf файла и проверка количества страниц")
    public void downloadPdfFileTest() throws IOException {
        open("https://knigolub.net/romance-fiction/1701-astra-shustroe-schaste-ili-ohota-na-malenkogo-drakona.html");
        File download = $("span[content = 'pdf' ]>a").download();
        PDF parsedPdf = new PDF(download);
        assertEquals("Анна Гаврилова", parsedPdf.author);
        assertEquals(46, parsedPdf.numberOfPages);
    }

    @Test
    @DisplayName("Скачивание XLS файла и проверка его содержимого")
    void xlsFileDownloadTest() throws IOException {

        open("http://rrrcn.ru/ru/ringing/formyi-otchetnosti");
        File file = $(byText("1. Скачать полную форму базы данных по кольцеванию >>>")).download();
        XLS parsedXls = new XLS(file);
        boolean checkPassed = parsedXls.excel
                .getSheetAt(0)
                .getRow(1)
                .getCell(37)
                .getStringCellValue()
                .contains("Упитанность");

        Assertions.assertTrue(checkPassed);
    }

    @Test
    @DisplayName(" Парсинг CSV файла и проверка его содержимого")
    void parseCsvFileTest() throws IOException, CsvException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("red book 2017.csv");
             Reader reader = new InputStreamReader(is)) {

            CSVReader csvReader = new CSVReader(reader);
            List<String[]> strings = csvReader.readAll();
            boolean isStringInFile = false;
            for (String[] str : strings) {
                if (str[8].equals("Eulabeia indica")) isStringInFile = true;
            }
            assertTrue(isStringInFile);
        }
    }

    @Test
    @DisplayName("Парсинг ZIP файлов")
    void parseZipFileTest() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("goose.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            Set<String> expectedFileNames = new HashSet<>();
            expectedFileNames.add("alphabetGoose.bmp");
            expectedFileNames.add("gooseSong.docx");
            Set<String> realFileNames = new HashSet<>();
            while ((entry = zis.getNextEntry()) != null) {
                realFileNames.add(entry.getName());
            }
            assertEquals(expectedFileNames, realFileNames);
        }
    }
}