package tests.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterSuite;
import reports.reports.AllureToExcelExporter;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CucumberOptions(
        features = "src/test/java/feature",
        glue = {"hooks", "stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "testng:target/cucumber-reports/cucumber-testng-results.xml",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
                "json:target/allure-results/Cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {

    public static List<Object[]> oldTestCaseData = new ArrayList<>(); // Lưu dữ liệu cũ từ sheet "Tổng hợp"

    @AfterSuite
    public void generateExcelReport() {
        String jsonReportPath = "target/cucumber-reports/cucumber.json";
        String directoryPath = "test-output/excel-report";

        // Tìm file Excel cũ mới nhất (nếu có)
        String latestExcelPath = getLatestExcelFile(directoryPath);
        if (latestExcelPath != null) {
            System.out.println("📄 Đã tìm thấy file Excel cũ: " + latestExcelPath);
            readOldExcelData(latestExcelPath); // Đọc dữ liệu cũ và lưu vào oldTestCaseData
        } else {
            System.out.println("ℹ️ Không tìm thấy file Excel cũ.");
        }

        // Tạo tên file Excel mới với timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String excelReportPath = directoryPath + "/test-report_" + timestamp + ".xlsx";

        // Tạo thư mục nếu chưa tồn tại
        File parentDir = new File(directoryPath);
        if (!parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("📁 Đã tạo thư mục: " + parentDir.getPath());
        }

        try {
            // Gọi exportCucumberReportToExcel() để tạo file mới, truyền thêm oldTestCaseData
            AllureToExcelExporter exporter = new AllureToExcelExporter();
            List<AllureToExcelExporter.TestCaseDetails> testCaseDetailsList =
                    exporter.exportCucumberReportToExcel(new String[]{jsonReportPath}, excelReportPath, oldTestCaseData);

            System.out.println("✅ Đã tạo file báo cáo mới: " + excelReportPath);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo báo cáo Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getLatestExcelFile(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        File latestFile = null;
        long lastModified = 0;

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".xlsx")) {
                    long modifiedTime = file.lastModified();
                    if (modifiedTime > lastModified) {
                        lastModified = modifiedTime;
                        latestFile = file;
                    }
                }
            }
        }
        return latestFile != null ? latestFile.getAbsolutePath() : null;
    }

    public void readOldExcelData(String excelFilePath) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet summarySheet = workbook.getSheet("Tổng hợp");
            if (summarySheet == null) {
                System.out.println("⚠️ Không tìm thấy sheet 'Tổng hợp' trong file cũ.");
                return;
            }

            int firstTestCaseRow = 7;
            int maxTestCaseRows = 5;
            int lastTestCaseRow = firstTestCaseRow + maxTestCaseRows - 1;

            oldTestCaseData.clear(); // Xóa dữ liệu cũ trước khi đọc mới

            for (int i = firstTestCaseRow; i <= lastTestCaseRow; i++) {
                Row row = summarySheet.getRow(i);
                if (row != null && row.getCell(1) != null && row.getCell(1).toString().trim().length() > 0) {
                    Object[] rowData = new Object[13];
                    for (int j = 1; j <= 13; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    rowData[j - 1] = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    rowData[j - 1] = cell.getNumericCellValue();
                                    break;
                                default:
                                    rowData[j - 1] = "";
                                    break;
                            }
                        } else {
                            rowData[j - 1] = "";
                        }
                    }
                    oldTestCaseData.add(rowData);
                }
            }
            System.out.println("📋 Đã đọc " + oldTestCaseData.size() + " dòng dữ liệu cũ từ sheet 'Tổng hợp'.");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đọc file Excel cũ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
