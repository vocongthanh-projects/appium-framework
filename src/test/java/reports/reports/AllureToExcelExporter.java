package reports.reports;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static tests.runners.TestRunner.oldTestCaseData;

public class AllureToExcelExporter {
    protected static final String SCREENSHOT_PATH = "test-output/screenshots/TTCH/";
    private static final String DEFAULT_TESTER = "Công Thành";
    private static final String DEFAULT_PRIORITY = "Thấp";
    public List<TestCaseDetails> exportCucumberReportToExcel(String[] jsonPaths, String excelPath, List<Object[]> oldTestCaseData) {
        List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
        Map<String, Integer> totalSeverityDistribution = new HashMap<>();
        totalSeverityDistribution.put("Nghiêm trọng", 0);
        totalSeverityDistribution.put("Cao", 0);
        totalSeverityDistribution.put("Trung bình", 0);
        totalSeverityDistribution.put("Thấp", 0);
        totalSeverityDistribution.put("Không xác định", 0);

        try {
            XSSFWorkbook workbook;
            File excelFile = new File(excelPath);
            if (excelFile.exists()) {
                try (FileInputStream fis = new FileInputStream(excelFile)) {
                    workbook = new XSSFWorkbook(fis);
                }
            } else {
                workbook = new XSSFWorkbook();
            }

            File screenshotDir = new File(SCREENSHOT_PATH);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
                System.out.println("📁 Đã tạo thư mục lưu ảnh: " + SCREENSHOT_PATH);
            }

            XSSFSheet summarySheet = workbook.getSheet("Tổng hợp");
            if (summarySheet == null) {
                summarySheet = workbook.createSheet("Tổng hợp");
                workbook.setSheetOrder("Tổng hợp", 0);
            } else {
                clearSheet(summarySheet);
            }

            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime earliestStartTime = null;
            ZonedDateTime latestEndTime = null;

            int globalTotalTests = 0, globalPassed = 0, globalFailed = 0, globalSkipped = 0;
            int totalNewBugs = 0, totalFixedBugs = 0;

            Map<String, String> featureMap = new HashMap<>(); // Lưu sheetName và featureName

            int featureIndex = 0;

            for (String jsonPath : jsonPaths) {
                System.out.println("🔍 Bắt đầu export với file JSON: " + jsonPath);
                File jsonFile = new File(jsonPath);
                if (!jsonFile.exists()) {
                    System.out.println("❌ File JSON không tồn tại: " + jsonPath);
                    continue;
                }

                JSONParser parser = new JSONParser();
                JSONArray cucumberReport = (JSONArray) parser.parse(new FileReader(jsonFile));

                for (Object featureObj : cucumberReport) {
                    JSONObject feature = (JSONObject) featureObj;
                    String featureName = (String) feature.get("name");
                    if (featureName == null || featureName.isEmpty()) {
                        featureName = "UnnamedFeature";
                    }

                    featureIndex++;
                    String pageName = String.format("%03d - %s", featureIndex, featureName);
                    String sheetName = String.format("%03d", featureIndex); // Giữ "001", "002", "003"

                    featureMap.put(sheetName, featureName);

                    XSSFSheet testCaseSheet = workbook.getSheet(sheetName);
                    if (testCaseSheet == null) {
                        testCaseSheet = workbook.createSheet(sheetName);
                    } else {
                        clearSheet(testCaseSheet);
                    }

                    JSONArray scenarios = (JSONArray) feature.get("elements");

                    int totalTests = 0, passed = 0, failed = 0, skipped = 0;
                    int newBugs = 0;
                    int fixedBugs = 0;
                    Map<String, Integer> severityDistribution = new HashMap<>();
                    severityDistribution.put("Nghiêm trọng", 0);
                    severityDistribution.put("Cao", 0);
                    severityDistribution.put("Trung bình", 0);
                    severityDistribution.put("Thấp", 0);
                    severityDistribution.put("Không xác định", 0);

                    int scenarioIndex = 0;

                    for (Object scenarioObj : scenarios) {
                        scenarioIndex++;
                        JSONObject scenario = (JSONObject) scenarioObj;
                        JSONArray steps = (JSONArray) scenario.get("steps");
                        totalTests++;

                        String overallStatus = "Đạt";
                        String priority = "-";
                        String skippedStepName = "";

                        for (Object stepObj : steps) {
                            JSONObject step = (JSONObject) stepObj;
                            JSONObject result = (JSONObject) step.get("result");
                            String status = (String) result.get("status");

                            if (status.equalsIgnoreCase("failed")) {
                                overallStatus = "Không đạt";
                                failed++;
                                String errorMessage = result.containsKey("error_message") ?
                                        (String) result.get("error_message") : "";
                                priority = determinePriorityFromError(errorMessage);
                                newBugs++;
                                severityDistribution.put(priority, severityDistribution.getOrDefault(priority, 0) + 1);
                                break;
                            } else if (status.equalsIgnoreCase("skipped")) {
                                overallStatus = "Bỏ qua";
                                skipped++;
                                skippedStepName = (String) step.get("name");
                                priority = "Không xác định";
                                break;
                            }
                        }
                        if ("Đạt".equals(overallStatus)) {
                            passed++;
                        }

                        String rawStartTime = (String) scenario.get("start_timestamp");
                        String rawEndTime = (String) scenario.get("end_timestamp");
                        ZonedDateTime startTime = null;
                        ZonedDateTime endTime = null;

                        if (rawStartTime != null && !rawStartTime.isEmpty()) {
                            startTime = ZonedDateTime.parse(rawStartTime).withZoneSameInstant(vietnamZone);
                            if (earliestStartTime == null || startTime.isBefore(earliestStartTime)) {
                                earliestStartTime = startTime;
                            }
                        }

                        if (rawEndTime != null && !rawEndTime.isEmpty()) {
                            endTime = ZonedDateTime.parse(rawEndTime).withZoneSameInstant(vietnamZone);
                        } else if (startTime != null) {
                            endTime = startTime.plusSeconds(1);
                        } else {
                            endTime = ZonedDateTime.now(vietnamZone);
                        }
                        if (latestEndTime == null || endTime.isAfter(latestEndTime)) {
                            latestEndTime = endTime;
                        }

                        String scenarioName = (String) scenario.get("name");
                        String startTimeFormatted = (startTime != null)
                                ? startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                                : "N/A";
                        String endTimeFormatted = (endTime != null)
                                ? endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                                : "N/A";

                        String testSteps = "";
                        String expectedResult = "N/A";
                        int lastThenIndex = -1;

                        for (int i = 0; i < steps.size(); i++) {
                            JSONObject step = (JSONObject) steps.get(i);
                            String keyword = (String) step.get("keyword");
                            if ("Then ".equals(keyword)) {
                                lastThenIndex = i;
                            }
                        }

                        if (lastThenIndex >= 0) {
                            for (int i = 0; i < lastThenIndex; i++) {
                                JSONObject step = (JSONObject) steps.get(i);
                                testSteps += (i + 1) + ". " + (String) step.get("name") + "\n";
                            }
                            JSONObject lastThenStep = (JSONObject) steps.get(lastThenIndex);
                            expectedResult = (String) lastThenStep.get("name");
                            // Kiểm tra và xử lý bảng dữ liệu trong bước "Then"
                            if (lastThenStep.containsKey("rows")) {
                                JSONArray rows = (JSONArray) lastThenStep.get("rows");
                                StringBuilder tableData = new StringBuilder();
                                tableData.append(expectedResult).append("\n");
                                for (Object rowObj : rows) {
                                    JSONObject row = (JSONObject) rowObj;
                                    JSONArray cells = (JSONArray) row.get("cells");
                                    for (Object cell : cells) {
                                        tableData.append("| ").append(cell.toString()).append(" ");
                                    }
                                    tableData.append("|\n");
                                }
                                expectedResult = tableData.toString().trim();
                            }
                        } else {
                            for (int i = 0; i < steps.size(); i++) {
                                JSONObject step = (JSONObject) steps.get(i);
                                testSteps += (i + 1) + ". " + (String) step.get("name") + "\n";
                            }
                        }

                        String actualResult;
                        if ("Đạt".equals(overallStatus)) {
                            actualResult = expectedResult;
                        } else if ("Bỏ qua".equals(overallStatus)) {
                            int skippedStepNumber = 1;
                            for (int i = 0; i < steps.size(); i++) {
                                JSONObject step = (JSONObject) steps.get(i);
                                JSONObject result = (JSONObject) step.get("result");
                                String status = (String) result.get("status");
                                if (status.equalsIgnoreCase("skipped")) {
                                    skippedStepNumber = i + 1;
                                    skippedStepName = (String) step.get("name");
                                    break;
                                }
                            }
                            actualResult = skippedStepName.isEmpty()
                                    ? "Không xác định bước bị bỏ qua"
                                    : "Không thể thao tác: " + skippedStepNumber + ". " + skippedStepName;
                        } else {
                            actualResult = "Không khớp với kết quả mong muốn";
                        }

                        String screenshotPath = SCREENSHOT_PATH + scenarioName.replaceAll("\\s+", "_") + ".png";

                        testCaseDetailsList.add(new TestCaseDetails(
                                scenarioName, overallStatus, scenarioIndex, priority, startTimeFormatted,
                                endTimeFormatted, testSteps, expectedResult, actualResult,
                                startTimeFormatted.split(" ")[0], DEFAULT_TESTER, screenshotPath
                        ));



                        testCaseDetailsList.add(new TestCaseDetails(
                                scenarioName, overallStatus, scenarioIndex, priority, startTimeFormatted,
                                endTimeFormatted, testSteps, expectedResult, actualResult,
                                startTimeFormatted.split(" ")[0], DEFAULT_TESTER, screenshotPath
                        ));

                        File screenshotFile = new File(screenshotPath);
                        byte[] screenshotBytes = null;
                        if (screenshotFile.exists() && !screenshotFile.isDirectory()) {
                            System.out.println("✅ Tìm thấy ảnh tại: " + screenshotPath);
                            try {
                                screenshotBytes = Files.readAllBytes(screenshotFile.toPath());
                                System.out.println("✅ Đọc thành công " + screenshotBytes.length + " bytes từ ảnh.");
                            } catch (IOException e) {
                                System.out.println("❌ Lỗi đọc file ảnh: " + e.getMessage());
                            }
                        } else {
                            System.out.println("⚠️ Không tìm thấy file ảnh tại: " + screenshotPath);
                        }

                        addTestCaseDetails(
                                workbook, testCaseSheet,
                                feature.get("name").toString(),
                                scenarioName,
                                testSteps,
                                overallStatus,
                                priority,
                                expectedResult,
                                actualResult,
                                startTimeFormatted,
                                endTimeFormatted,
                                DEFAULT_TESTER,
                                screenshotBytes
                        );
                    }

                    globalTotalTests += totalTests;
                    globalPassed += passed;
                    globalFailed += failed;
                    globalSkipped += skipped;
                    totalNewBugs += newBugs;
                    totalFixedBugs += fixedBugs;
                    for (Map.Entry<String, Integer> entry : severityDistribution.entrySet()) {
                        totalSeverityDistribution.put(entry.getKey(), totalSeverityDistribution.getOrDefault(entry.getKey(), 0) + entry.getValue());
                    }
                }
            }

            addSummaryReport(workbook, summarySheet,
                    globalPassed, globalFailed, globalSkipped,
                    (globalTotalTests > 0) ? (double) globalPassed / globalTotalTests * 100 : 0,
                    (globalTotalTests > 0) ? (double) globalFailed / globalTotalTests * 100 : 0,
                    (globalTotalTests > 0) ? (double) globalSkipped / globalTotalTests * 100 : 0,
                    earliestStartTime != null ? earliestStartTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A",
                    latestEndTime != null ? latestEndTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A",
                    "1.0.0", "Android 14 - Emulator", "Build 20250226",
                    totalFixedBugs, totalNewBugs, totalSeverityDistribution, featureMap);

            try (FileOutputStream out = new FileOutputStream(excelPath)) {
                workbook.write(out);
                System.out.println("✅ Export thành công: " + excelPath);
            }
            workbook.close();
        } catch (Exception e) {
            System.out.println("❌ Lỗi trong quá trình xuất báo cáo: " + e.getMessage());
            e.printStackTrace();
        }
        return testCaseDetailsList;
    }

    public static void addSummaryReport(XSSFWorkbook workbook, XSSFSheet summarySheet,
                                        int passCount, int failCount, int skippedCount,
                                        double passPercentage, double failPercentage, double skippedPercentage,
                                        String startTime, String endTime, String releaseVersion,
                                        String deviceInfo, String buildVersion, int fixedBugs, int newBugs,
                                        Map<String, Integer> severityDistribution,
                                        Map<String, String> featureMap) {

        // Xóa tất cả các merged regions hiện có trong sheet
        for (int i = summarySheet.getNumMergedRegions() - 1; i >= 0; i--) {
            summarySheet.removeMergedRegion(i);
        }

        // Style header chung
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setFontName("Arial");
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 32, 96), null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(false);

        // Style riêng cho header "TỔNG SỐ TRƯỜNG HỢP KIỂM THỬ"
        CellStyle wrapTextHeaderStyle = workbook.createCellStyle();
        wrapTextHeaderStyle.cloneStyleFrom(headerStyle);
        wrapTextHeaderStyle.setWrapText(true);

        // Style cho các ô giá trị
        CellStyle valueStyle = workbook.createCellStyle();
        Font valueFont = workbook.createFont();
        valueFont.setFontName("Arial");
        valueStyle.setFont(valueFont);
        valueStyle.setWrapText(true);
        valueStyle.setAlignment(HorizontalAlignment.CENTER);
        valueStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        valueStyle.setBorderTop(BorderStyle.THIN);
        valueStyle.setBorderBottom(BorderStyle.THIN);
        valueStyle.setBorderLeft(BorderStyle.THIN);
        valueStyle.setBorderRight(BorderStyle.THIN);

        // Style riêng cho ô severityLevels căn trái
        CellStyle leftAlignedValueStyle = workbook.createCellStyle();
        leftAlignedValueStyle.cloneStyleFrom(valueStyle);
        leftAlignedValueStyle.setAlignment(HorizontalAlignment.LEFT);

        // Style riêng cho 4 header lớn
        CellStyle mainHeaderStyle = workbook.createCellStyle();
        Font mainHeaderFont = workbook.createFont();
        mainHeaderFont.setFontName("Arial");
        mainHeaderFont.setBold(true);
        mainHeaderFont.setColor(IndexedColors.BLACK.getIndex());
        mainHeaderStyle.setFont(mainHeaderFont);
        mainHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
        mainHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        mainHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        mainHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        mainHeaderStyle.setBorderTop(BorderStyle.THIN);
        mainHeaderStyle.setBorderBottom(BorderStyle.THIN);
        mainHeaderStyle.setBorderLeft(BorderStyle.THIN);
        mainHeaderStyle.setBorderRight(BorderStyle.THIN);

        // ===== Thêm header "BẢNG THÔNG TIN MÔI TRƯỜNG" vào dòng 2 =====
        Row envHeaderRow = summarySheet.createRow(1);
        envHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 4; i++) {
            Cell cell = envHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1) {
                cell.setCellValue("BẢNG THÔNG TIN MÔI TRƯỜNG KIỂM THỬ");
            }
        }
        summarySheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4));

        // ===== Dữ liệu thiết bị (dòng 3) =====
        Row deviceRow = summarySheet.createRow(2);
        deviceRow.setHeightInPoints(33f);
        String[] deviceHeaders = {"STT", "MÔI TRƯỜNG", "THÔNG TIN THIẾT BỊ", "PHIÊN BẢN"};
        for (int i = 0; i < deviceHeaders.length; i++) {
            Cell cell = deviceRow.createCell(i + 1);
            cell.setCellValue(deviceHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        Row deviceDataRow = summarySheet.createRow(3);
        deviceDataRow.setHeightInPoints(30f);
        for (int i = 1; i <= 4; i++) {
            Cell cell = deviceDataRow.createCell(i);
            cell.setCellStyle(valueStyle);
        }
        deviceDataRow.getCell(1).setCellValue(1);
        deviceDataRow.getCell(2).setCellValue("PRO");
        deviceDataRow.getCell(3).setCellValue(deviceInfo);
        deviceDataRow.getCell(4).setCellValue(releaseVersion);

        // ===== Thêm header "BẢNG THÔNG TIN KẾT QUẢ KIỂM THỬ" vào dòng 6 =====
        Row testResultHeaderRow = summarySheet.createRow(5);
        testResultHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 13; i++) {
            Cell cell = testResultHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1) {
                cell.setCellValue("BẢNG THÔNG TIN KẾT QUẢ KIỂM THỬ QUA CÁC LẦN");
            }
        }
        summarySheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 13));

        // Phần test case (header dòng 7)
        Row testCaseRow = summarySheet.createRow(6);
        testCaseRow.setHeightInPoints(33f);
        String[] testCaseHeaders = {"STT", "PHIÊN BẢN", "TỔNG SỐ TC", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA", "ĐẠT %", "KHÔNG ĐẠT %", "BỎ QUA %", "THỜI GIAN BẮT ĐẦU", "THỜI GIAN KẾT THÚC", "LỖI MỚI", "LỖI ĐÃ SỬA"};
        for (int i = 0; i < testCaseHeaders.length; i++) {
            Cell cell = testCaseRow.createCell(i + 1);
            cell.setCellValue(testCaseHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // Sử dụng dữ liệu cũ từ oldTestCaseData
        List<Object[]> oldData = new ArrayList<>(oldTestCaseData != null ? oldTestCaseData : Collections.emptyList());

// Nếu đủ 5 dòng, xóa dòng cũ nhất và cập nhật lại STT cho các dòng còn lại
        int maxTestCaseRows = 5;
        if (oldData.size() >= maxTestCaseRows) {
            oldData.remove(0); // Xóa dòng đầu tiên (cũ nhất)
            // Cập nhật lại STT cho các dòng còn lại
            for (int i = 0; i < oldData.size(); i++) {
                Object[] row = oldData.get(i);
                row[0] = i + 1; // Gán lại STT từ 1, 2, 3, ...
            }
        }

// Tính toán newBugs và fixedBugs dựa trên dữ liệu cũ
        int calculatedNewBugs = newBugs; // Giá trị mặc định từ tham số
        int calculatedFixedBugs = fixedBugs; // Giá trị mặc định từ tham số
        int latestFailCount = failCount; // Số "Không đạt" của lần mới nhất

        if (!oldData.isEmpty()) {
            // Lấy dữ liệu của lần gần nhất (dòng cuối cùng trong oldData trước khi thêm mới)
            Object[] previousData = oldData.get(oldData.size() - 1);

            // Xử lý an toàn kiểu dữ liệu
            int previousFailCount = 0;
            int previousNewBugs = 0;

            // Cột "KHÔNG ĐẠT" (index 4)
            if (previousData[4] instanceof Integer) {
                previousFailCount = (Integer) previousData[4];
            } else if (previousData[4] instanceof Double) {
                previousFailCount = ((Double) previousData[4]).intValue();
            }

            // Cột "LỖI MỚI" (index 11)
            if (previousData[11] instanceof Integer) {
                previousNewBugs = (Integer) previousData[11];
            } else if (previousData[11] instanceof Double) {
                previousNewBugs = ((Double) previousData[11]).intValue();
            }

            // Tính lỗi mới (giả định tất cả failCount tăng thêm là lỗi mới)
            if (latestFailCount > previousFailCount) {
                calculatedNewBugs = latestFailCount - previousFailCount;
            } else {
                calculatedNewBugs = 0; // Nếu failCount không tăng, không có lỗi mới
            }

            // Tính lỗi đã sửa (giữ nguyên)
            if (latestFailCount < previousFailCount) {
                calculatedFixedBugs = previousFailCount - latestFailCount;
            } else {
                calculatedFixedBugs = 0;
            }
        }

// Thêm dữ liệu mới với STT mới
        Object[] newData = new Object[] {
                oldData.size() + 1, // STT (sẽ là số tiếp theo)
                buildVersion,       // PHIÊN BẢN
                passCount + failCount + skippedCount, // TỔNG SỐ TC
                passCount,          // ĐẠT
                failCount,          // KHÔNG ĐẠT
                skippedCount,       // BỎ QUA
                String.format("%.2f%%", passPercentage), // ĐẠT %
                String.format("%.2f%%", failPercentage), // KHÔNG ĐẠT %
                String.format("%.2f%%", skippedPercentage), // BỎ QUA %
                startTime,          // THỜI GIAN BẮT ĐẦU
                endTime,            // THỜI GIAN KẾT THÚC
                calculatedNewBugs,  // LỖI MỚI (đã tính toán)
                calculatedFixedBugs // LỖI ĐÃ SỬA (đã tính toán)
        };
        oldData.add(newData);

// Ghi lại toàn bộ dữ liệu (cũ + mới) vào sheet
        int firstTestCaseRow = 7;
        for (int i = 0; i < oldData.size(); i++) {
            Row dataRow = summarySheet.getRow(firstTestCaseRow + i);
            if (dataRow == null) {
                dataRow = summarySheet.createRow(firstTestCaseRow + i);
            }
            dataRow.setHeightInPoints(30f);
            Object[] rowData = oldData.get(i);

            for (int j = 1; j <= 13; j++) {
                Cell cell = dataRow.getCell(j);
                if (cell == null) {
                    cell = dataRow.createCell(j);
                }
                cell.setCellStyle(valueStyle);
                Object value = rowData[j - 1];
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                }
            }
        }

        // ===== Thêm header "BẢNG ĐÁNH GIÁ MỨC ĐỘ NGHIÊM TRỌNG" =====
        int nextRowAfterTestCase = firstTestCaseRow + maxTestCaseRows;
        Row severityHeaderRow = summarySheet.createRow(nextRowAfterTestCase + 1);
        severityHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 5; i++) {
            Cell cell = severityHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1) {
                cell.setCellValue("BẢNG ĐÁNH GIÁ MỨC ĐỘ NGHIÊM TRỌNG CÁC LỖI PHÁT HIỆN");
            }
        }
        summarySheet.addMergedRegion(new CellRangeAddress(nextRowAfterTestCase + 1, nextRowAfterTestCase + 1, 1, 5));

        Row severityRow = summarySheet.createRow(nextRowAfterTestCase + 2);
        severityRow.setHeightInPoints(33f);
        String[] severityHeaders = {"STT", "MỨC ĐỘ NGHIÊM TRỌNG", "SỐ LƯỢNG LỖI", "ĐÃ SỬA", "CÒN TỒN ĐỌNG"};
        for (int i = 0; i < severityHeaders.length; i++) {
            Cell cell = severityRow.createCell(i + 1);
            cell.setCellValue(severityHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        String[] severityLevels = {"🔴 NGHIÊM TRỌNG", "🟠 CAO", "🟡 TRUNG BÌNH", "🟢 THẤP"};
        String[] severityKeys = {"Nghiêm trọng", "Cao", "Trung bình", "Thấp"};
        for (int i = 0; i < severityLevels.length; i++) {
            Row row = summarySheet.createRow(nextRowAfterTestCase + 3 + i);
            row.setHeightInPoints(30f);
            for (int j = 1; j <= 5; j++) {
                Cell cell = row.createCell(j);
                if (j == 2) {
                    cell.setCellStyle(leftAlignedValueStyle);
                } else {
                    cell.setCellStyle(valueStyle);
                }
            }
            row.getCell(1).setCellValue(i + 1);
            row.getCell(2).setCellValue(severityLevels[i]);
            int severityCount = severityDistribution.getOrDefault(severityKeys[i], 0);
            row.getCell(3).setCellValue(severityCount);
            int fixedBugsForSeverity = 0; // Giả sử chưa có dữ liệu cụ thể
            row.getCell(4).setCellValue(fixedBugsForSeverity);
            row.getCell(5).setCellValue(severityCount - fixedBugsForSeverity);
        }

        // ===== Thêm header "BẢNG TỔNG HỢP CHI TIẾT" =====
        int nextRowAfterSeverity = nextRowAfterTestCase + 3 + severityLevels.length;
        Row detailHeaderRow = summarySheet.createRow(nextRowAfterSeverity + 1);
        detailHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 6; i++) {
            Cell cell = detailHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1) {
                cell.setCellValue("BẢNG TỔNG HỢP CHI TIẾT");
            }
        }
        summarySheet.addMergedRegion(new CellRangeAddress(nextRowAfterSeverity + 1, nextRowAfterSeverity + 1, 1, 6));

        // Phần danh sách trang (header chỉ 1 hàng)
        Row pageListRow1 = summarySheet.createRow(nextRowAfterSeverity + 2);
        pageListRow1.setHeightInPoints(33f);
        String[] pageListHeadersTop = {"STT", "DANH SÁCH TRANG", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA", "TỔNG SỐ TRƯỜNG HỢP KIỂM THỬ"};
        for (int i = 0; i < pageListHeadersTop.length; i++) {
            Cell cell = pageListRow1.createCell(i + 1);
            cell.setCellValue(pageListHeadersTop[i]);
            if (i == 5) {
                cell.setCellStyle(wrapTextHeaderStyle);
            } else {
                cell.setCellStyle(headerStyle);
            }
        }

        // Lấy dữ liệu từ tất cả các sheet (trừ sheet "Tổng hợp")
        int firstDetailRow = nextRowAfterSeverity + 3;
        int rowIndex = firstDetailRow;
        Map<String, Integer> existingFeatures = new HashMap<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            if ("Tổng hợp".equals(sheetName)) {
                continue;
            }

            String sheetIndex = sheetName;
            String fullFeatureName = sheetIndex + " - " + featureMap.getOrDefault(sheetIndex, "UnnamedFeature");

            int totalTests = 0, passed = 0, failed = 0, skipped = 0;
            for (Row row : sheet) {
                if (row.getRowNum() <= 4) continue;
                Cell statusCell = row.getCell(4);
                if (statusCell != null) {
                    String status = "N/A";
                    switch (statusCell.getCellType()) {
                        case STRING:
                            status = statusCell.getStringCellValue();
                            break;
                        case NUMERIC:
                            status = String.valueOf((int) statusCell.getNumericCellValue());
                            break;
                        case BLANK:
                            status = "";
                            break;
                        case FORMULA:
                            status = statusCell.getCellFormula();
                            break;
                        default:
                            status = "N/A";
                            break;
                    }
                    totalTests++;
                    if ("Đạt".equals(status)) {
                        passed++;
                    } else if ("Không đạt".equals(status)) {
                        failed++;
                    } else if ("Bỏ qua".equals(status)) {
                        skipped++;
                    }
                }
            }

            if (!existingFeatures.containsKey(fullFeatureName)) {
                Row pageListDataRow = summarySheet.createRow(rowIndex);
                pageListDataRow.setHeightInPoints(30f);
                for (int j = 1; j <= 6; j++) {
                    Cell cell = pageListDataRow.createCell(j);
                    cell.setCellStyle(valueStyle);
                }

                CreationHelper creationHelper = workbook.getCreationHelper();
                Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
                hyperlink.setAddress(String.format("'%s'!A1", sheetName));

                Cell pageNameCell = pageListDataRow.getCell(2);
                pageNameCell.setCellValue(fullFeatureName);
                pageNameCell.setHyperlink(hyperlink);

                CellStyle hyperlinkStyle = workbook.createCellStyle();
                Font hyperlinkFont = workbook.createFont();
                hyperlinkFont.setFontName("Arial");
                hyperlinkFont.setUnderline(Font.U_SINGLE);
                hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(hyperlinkFont);
                hyperlinkStyle.setAlignment(HorizontalAlignment.LEFT);
                hyperlinkStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                hyperlinkStyle.setBorderTop(BorderStyle.THIN);
                hyperlinkStyle.setBorderBottom(BorderStyle.THIN);
                hyperlinkStyle.setBorderLeft(BorderStyle.THIN);
                hyperlinkStyle.setBorderRight(BorderStyle.THIN);
                pageNameCell.setCellStyle(hyperlinkStyle);

                pageListDataRow.getCell(1).setCellValue(Integer.parseInt(sheetIndex));
                pageListDataRow.getCell(3).setCellValue(passed);
                pageListDataRow.getCell(4).setCellValue(failed);
                pageListDataRow.getCell(5).setCellValue(skipped);
                pageListDataRow.getCell(6).setCellValue(totalTests);

                existingFeatures.put(fullFeatureName, rowIndex);
                rowIndex++;
            }
        }

        // Tính tổng từ các hàng trong "BẢNG TỔNG HỢP CHI TIẾT"
        int totalPassed = 0, totalFailed = 0, totalSkipped = 0, totalTestsOverall = 0;
        for (int rowNum = firstDetailRow; rowNum < rowIndex; rowNum++) {
            Row row = summarySheet.getRow(rowNum);
            if (row != null) {
                totalPassed += getNumericCellValue(row.getCell(3));
                totalFailed += getNumericCellValue(row.getCell(4));
                totalSkipped += getNumericCellValue(row.getCell(5));
                totalTestsOverall += getNumericCellValue(row.getCell(6));
            }
        }

        // Thêm hàng "TỔNG"
        Row totalRow = summarySheet.createRow(rowIndex);
        totalRow.setHeightInPoints(30f);
        for (int i = 1; i <= 6; i++) {
            Cell cell = totalRow.createCell(i);
            cell.setCellStyle(valueStyle);
        }
        totalRow.getCell(1).setCellValue("TỔNG");
        totalRow.getCell(3).setCellValue(totalPassed);
        totalRow.getCell(4).setCellValue(totalFailed);
        totalRow.getCell(5).setCellValue(totalSkipped);
        totalRow.getCell(6).setCellValue(totalTestsOverall);

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i <= 13; i++) {
            summarySheet.autoSizeColumn(i);
            int currentWidth = summarySheet.getColumnWidth(i);
            int minWidth = 4000;
            switch (i) {
                case 0:
                    summarySheet.setColumnWidth(i, 700);
                    break;
                case 3:
                case 10:
                case 11:
                    summarySheet.setColumnWidth(i, (int)(currentWidth * 1.5));
                    break;
                case 2:
                case 5:
                    if (summarySheet.getRow(nextRowAfterTestCase + 2) != null) {
                        summarySheet.setColumnWidth(i, (int)(currentWidth * 1.5));
                    }
                    break;
                default:
                    if (currentWidth < minWidth) {
                        summarySheet.setColumnWidth(i, minWidth);
                    }
                    break;
            }
        }

        // Thêm biểu đồ (charts) cách 1 hàng dưới hàng "TỔNG"
        int chartStartRow = rowIndex + 2;
        XSSFDrawing drawing = summarySheet.createDrawingPatriarch();

// Định nghĩa kích thước chung cho tất cả các biểu đồ
        // Định nghĩa kích thước chung
        int chartWidthCols = 8; // Tăng độ rộng mỗi biểu đồ để cân đối
        int chartHeightRows = 12; // Giữ nguyên độ cao
        int chartStartRow2 = chartStartRow + chartHeightRows + 2; // Hàng bắt đầu cho dòng 2, thêm khoảng cách

// Biểu đồ 1: Đạt/Không Đạt/Bỏ Qua qua các lần chạy (Bar Chart)
        XSSFClientAnchor anchor1 = drawing.createAnchor(0, 0, 0, 0, 0, chartStartRow, chartWidthCols, chartStartRow + chartHeightRows);
        XSSFChart chart1 = drawing.createChart(anchor1);
        chart1.setTitleText("TRẠNG THÁI KIỂM THỬ QUA CÁC LẦN");
        chart1.setTitleOverlay(false);
        XDDFChartLegend legend1 = chart1.getOrAddLegend();
        legend1.setPosition(LegendPosition.TOP_RIGHT);

// Chuẩn bị dữ liệu cho biểu đồ
        String[] runCategories = new String[oldData.size()];
        Double[] passValues = new Double[oldData.size()];
        Double[] failValues = new Double[oldData.size()];
        Double[] skipValues = new Double[oldData.size()];

        for (int i = 0; i < oldData.size(); i++) {
            runCategories[i] = "Lần " + (i + 1); // Sử dụng STT tự nhiên thay vì oldData.get(i)[0]

            // Xử lý an toàn kiểu dữ liệu cho cột "ĐẠT" (index 3)
            Object passValue = oldData.get(i)[3];
            if (passValue instanceof Number) {
                passValues[i] = ((Number) passValue).doubleValue();
            } else if (passValue instanceof String) {
                try {
                    passValues[i] = Double.parseDouble((String) passValue);
                } catch (NumberFormatException e) {
                    passValues[i] = 0.0; // Giá trị mặc định nếu không parse được
                }
            } else {
                passValues[i] = 0.0; // Giá trị mặc định cho các trường hợp khác
            }

            // Xử lý an toàn kiểu dữ liệu cho cột "KHÔNG ĐẠT" (index 4)
            Object failValue = oldData.get(i)[4];
            if (failValue instanceof Number) {
                failValues[i] = ((Number) failValue).doubleValue();
            } else if (failValue instanceof String) {
                try {
                    failValues[i] = Double.parseDouble((String) failValue);
                } catch (NumberFormatException e) {
                    failValues[i] = 0.0; // Giá trị mặc định nếu không parse được
                }
            } else {
                failValues[i] = 0.0; // Giá trị mặc định cho các trường hợp khác
            }

            // Xử lý an toàn kiểu dữ liệu cho cột "BỎ QUA" (index 5)
            Object skipValue = oldData.get(i)[5];
            if (skipValue instanceof Number) {
                skipValues[i] = ((Number) skipValue).doubleValue();
            } else if (skipValue instanceof String) {
                try {
                    skipValues[i] = Double.parseDouble((String) skipValue);
                } catch (NumberFormatException e) {
                    skipValues[i] = 0.0; // Giá trị mặc định nếu không parse được
                }
            } else {
                skipValues[i] = 0.0; // Giá trị mặc định cho các trường hợp khác
            }
        }

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromArray(runCategories);
        XDDFNumericalDataSource<Double> passData = XDDFDataSourcesFactory.fromArray(passValues);
        XDDFNumericalDataSource<Double> failData = XDDFDataSourcesFactory.fromArray(failValues);
        XDDFNumericalDataSource<Double> skipData = XDDFDataSourcesFactory.fromArray(skipValues);

        XDDFCategoryAxis bottomAxis1 = chart1.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis1.setTitle("Lần chạy");
        XDDFValueAxis leftAxis1 = chart1.createValueAxis(AxisPosition.LEFT);
        leftAxis1.setTitle("Số lượng");
        leftAxis1.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFBarChartData barData = (XDDFBarChartData) chart1.createData(ChartTypes.BAR, bottomAxis1, leftAxis1);
        barData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series passSeries = (XDDFBarChartData.Series) barData.addSeries(categories, passData);
        passSeries.setTitle("Đạt", null);
        passSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xA6, (byte)0xE3, (byte)0xB6}));

        XDDFBarChartData.Series failSeries = (XDDFBarChartData.Series) barData.addSeries(categories, failData);
        failSeries.setTitle("Không Đạt", null);
        failSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xF6, (byte)0xB3, (byte)0xAE}));

        XDDFBarChartData.Series skipSeries = (XDDFBarChartData.Series) barData.addSeries(categories, skipData);
        skipSeries.setTitle("Bỏ Qua", null);
        skipSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xCC}));

        chart1.plot(barData);

// Biểu đồ 2: Severity Distribution (Bar Chart)
        XSSFClientAnchor anchor2 = drawing.createAnchor(0, 0, 0, 0, chartWidthCols + 2, chartStartRow, chartWidthCols * 2 + 2, chartStartRow + chartHeightRows);
        XSSFChart chart2 = drawing.createChart(anchor2);
        chart2.setTitleText("Biểu đồ % Mức độ nghiêm trọng");
        chart2.setTitleOverlay(false);
        XDDFChartLegend legend2 = chart2.getOrAddLegend();
        legend2.setPosition(LegendPosition.TOP_RIGHT);

// (Giữ nguyên phần dữ liệu và cấu hình biểu đồ 2 như cũ)
        int totalRemaining = severityDistribution.getOrDefault("Nghiêm trọng", 0) +
                severityDistribution.getOrDefault("Cao", 0) +
                severityDistribution.getOrDefault("Trung bình", 0) +
                severityDistribution.getOrDefault("Thấp", 0);
        String[] severityCategoriesArray = {"Nghiêm trọng", "Cao", "Trung bình", "Thấp"};
        Double[] severityPercentages = new Double[4];
        severityPercentages[0] = (totalRemaining > 0) ? (double) severityDistribution.getOrDefault("Nghiêm trọng", 0) / totalRemaining * 100 : 0.0;
        severityPercentages[1] = (totalRemaining > 0) ? (double) severityDistribution.getOrDefault("Cao", 0) / totalRemaining * 100 : 0.0;
        severityPercentages[2] = (totalRemaining > 0) ? (double) severityDistribution.getOrDefault("Trung bình", 0) / totalRemaining * 100 : 0.0;
        severityPercentages[3] = (totalRemaining > 0) ? (double) severityDistribution.getOrDefault("Thấp", 0) / totalRemaining * 100 : 0.0;

        XDDFDataSource<String> severityCategories = XDDFDataSourcesFactory.fromArray(severityCategoriesArray);
        XDDFNumericalDataSource<Double> severityValues = XDDFDataSourcesFactory.fromArray(severityPercentages);

        XDDFCategoryAxis bottomAxis2 = chart2.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis2.setTitle("Mức độ nghiêm trọng");
        XDDFValueAxis leftAxis2 = chart2.createValueAxis(AxisPosition.LEFT);
        leftAxis2.setTitle("Tỷ lệ phần trăm (%)");
        leftAxis2.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFBarChartData severityBarData = (XDDFBarChartData) chart2.createData(ChartTypes.BAR, bottomAxis2, leftAxis2);
        severityBarData.setBarDirection(BarDirection.COL);

        Double[] nghiTrongData = new Double[]{severityPercentages[0], 0.0, 0.0, 0.0};
        Double[] caoData = new Double[]{0.0, severityPercentages[1], 0.0, 0.0};
        Double[] trungBinhData = new Double[]{0.0, 0.0, severityPercentages[2], 0.0};
        Double[] thapData = new Double[]{0.0, 0.0, 0.0, severityPercentages[3]};

        XDDFBarChartData.Series nghiTrongSeries = (XDDFBarChartData.Series) severityBarData.addSeries(severityCategories, XDDFDataSourcesFactory.fromArray(nghiTrongData));
        nghiTrongSeries.setTitle("Nghiêm trọng", null);
        nghiTrongSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xff, (byte)0x00, (byte)0x00}));

        XDDFBarChartData.Series caoSeries = (XDDFBarChartData.Series) severityBarData.addSeries(severityCategories, XDDFDataSourcesFactory.fromArray(caoData));
        caoSeries.setTitle("Cao", null);
        caoSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xff, (byte)0x61, (byte)0x61}));

        XDDFBarChartData.Series trungBinhSeries = (XDDFBarChartData.Series) severityBarData.addSeries(severityCategories, XDDFDataSourcesFactory.fromArray(trungBinhData));
        trungBinhSeries.setTitle("Trung bình", null);
        trungBinhSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xff, (byte)0xc0, (byte)0x00}));

        XDDFBarChartData.Series thapSeries = (XDDFBarChartData.Series) severityBarData.addSeries(severityCategories, XDDFDataSourcesFactory.fromArray(thapData));
        thapSeries.setTitle("Thấp", null);
        thapSeries.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0x94, (byte)0xef, (byte)0xfb}));

        chart2.plot(severityBarData);

// Biểu đồ 3: SỐ LẦN ĐẠT QUA CÁC LẦN (Pass Count Over Runs - Bar Chart)
        XSSFClientAnchor anchor3 = drawing.createAnchor(0, 0, 0, 0, 0, chartStartRow2, chartWidthCols, chartStartRow2 + chartHeightRows);
        XSSFChart chart3 = drawing.createChart(anchor3);
        chart3.setTitleText("SỐ LẦN ĐẠT QUA CÁC LẦN");
        chart3.setTitleOverlay(false);
        XDDFChartLegend legend3 = chart3.getOrAddLegend();
        legend3.setPosition(LegendPosition.TOP_RIGHT);

        XDDFDataSource<String> passCategories = XDDFDataSourcesFactory.fromArray(runCategories);
        XDDFNumericalDataSource<Double> passDataValues = XDDFDataSourcesFactory.fromArray(passValues);

        XDDFCategoryAxis bottomAxis3 = chart3.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis3.setTitle("Lần chạy");
        XDDFValueAxis leftAxis3 = chart3.createValueAxis(AxisPosition.LEFT);
        leftAxis3.setTitle("Số lần đạt");
        leftAxis3.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFBarChartData passBarData = (XDDFBarChartData) chart3.createData(ChartTypes.BAR, bottomAxis3, leftAxis3);
        passBarData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series passSeries1 = (XDDFBarChartData.Series) passBarData.addSeries(passCategories, passDataValues);
        passSeries1.setTitle("Số lần đạt", null);
        passSeries1.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xA6, (byte)0xE3, (byte)0xB6}));

        chart3.plot(passBarData);

// Biểu đồ 4: SỐ LẦN KHÔNG ĐẠT QUA CÁC LẦN (Fail Count Over Runs - Bar Chart)
        XSSFClientAnchor anchor4 = drawing.createAnchor(0, 0, 0, 0, chartWidthCols + 2, chartStartRow2, chartWidthCols * 2 + 2, chartStartRow2 + chartHeightRows);
        XSSFChart chart4 = drawing.createChart(anchor4);
        chart4.setTitleText("SỐ LẦN KHÔNG ĐẠT QUA CÁC LẦN");
        chart4.setTitleOverlay(false);
        XDDFChartLegend legend4 = chart4.getOrAddLegend();
        legend4.setPosition(LegendPosition.TOP_RIGHT);

        XDDFDataSource<String> failCategories = XDDFDataSourcesFactory.fromArray(runCategories);
        XDDFNumericalDataSource<Double> failDataValues = XDDFDataSourcesFactory.fromArray(failValues);

        XDDFCategoryAxis bottomAxis4 = chart4.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis4.setTitle("Lần chạy");
        XDDFValueAxis leftAxis4 = chart4.createValueAxis(AxisPosition.LEFT);
        leftAxis4.setTitle("Số lần không đạt");
        leftAxis4.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFBarChartData failBarData = (XDDFBarChartData) chart4.createData(ChartTypes.BAR, bottomAxis4, leftAxis4);
        failBarData.setBarDirection(BarDirection.COL);

        XDDFBarChartData.Series failSeries1 = (XDDFBarChartData.Series) failBarData.addSeries(failCategories, failDataValues);
        failSeries1.setTitle("Số lần không đạt", null);
        failSeries1.setShapeProperties(createSolidFillProperties(new byte[]{(byte)0xF6, (byte)0xB3, (byte)0xAE}));

        chart4.plot(failBarData);

    }

    private static XDDFShapeProperties createSolidFillProperties(byte[] rgb) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties();
        XDDFColor color = XDDFColor.from(rgb); // RGB values
        fill.setColor(color);
        XDDFShapeProperties properties = new XDDFShapeProperties();
        properties.setFillProperties(fill);
        return properties;
    }

    // Hàm hỗ trợ lấy giá trị số từ ô, tránh lỗi IllegalStateException
    private static int getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        try {
            return (int) cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            String value = cell.getStringCellValue().replaceAll("[^0-9]", "");
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        }
    }
    private String determinePriorityFromError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return "Không xác định"; // Giá trị mặc định nếu không có thông tin lỗi
        }

        String lowerCaseMessage = errorMessage.toLowerCase();
        if (lowerCaseMessage.contains("timeout")) {
            return getPriorityLevel("TimeoutError");
        } else if (lowerCaseMessage.contains("browser")) {
            return getPriorityLevel("BrowserError");
        } else if (lowerCaseMessage.contains("page")) {
            return getPriorityLevel("PageError");
        } else if (lowerCaseMessage.contains("protocol")) {
            return getPriorityLevel("ProtocolError");
        } else if (lowerCaseMessage.contains("element not found") || lowerCaseMessage.contains("no such element")) {
            return getPriorityLevel("ElementNotFoundError");
        } else if (lowerCaseMessage.contains("assertion") || lowerCaseMessage.contains("assert")) {
            return getPriorityLevel("AssertionError");
        } else if (lowerCaseMessage.contains("navigation")) {
            return getPriorityLevel("NavigationError");
        } else if (lowerCaseMessage.contains("frame detached")) {
            return getPriorityLevel("FrameDetachedError");
        } else if (lowerCaseMessage.contains("file not found")) {
            return getPriorityLevel("FileNotFoundError");
        } else if (lowerCaseMessage.contains("interception")) {
            return getPriorityLevel("InterceptionError");
        }
        return "Không xác định"; // Mặc định nếu không khớp
    }

    public static String getPriorityLevel(String errorType) {
        if (errorType == null || errorType.trim().isEmpty()) {
            return "Không xác định";
        }

        switch (errorType) {
            case "TimeoutError":
                return "Nghiêm trọng";
            case "BrowserError":
            case "PageError":
                return "Cao";
            case "ProtocolError":
            case "ElementNotFoundError":
            case "AssertionError":
                return "Trung bình";
            case "NavigationError":
            case "FrameDetachedError":
            case "FileNotFoundError":
            case "InterceptionError":
                return "Thấp";
            default:
                System.out.println("Warning: Unknown error type: " + errorType);
                return "Không xác định";
        }
    }
    private byte[] convertScreenshotToBytes(String screenshotPath) {
        try {
            File file = new File(screenshotPath);
            if (file.exists() && !file.isDirectory()) {
                return Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            System.out.println("❌ Lỗi khi đọc file ảnh: " + e.getMessage());
        }
        return null;
    }

    // Hàm hỗ trợ xóa sheet
    private void clearSheet(XSSFSheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }


    public class SummaryDetails {
        private int passCount;
        private int failCount;
        private int skippedCount;
        private double passPercentage;
        private double failPercentage;
        private double skippedPercentage;
        private String startTime;
        private String endTime;
        private String releaseVersion;
        private String deviceInfo;
        private String buildVersion;
        private int fixedBugs;
        private int newBugs;
        private Map<String, Integer> severityDistribution;

        public SummaryDetails(int passCount, int failCount, int skippedCount,
                              double passPercentage, double failPercentage, double skippedPercentage,
                              String startTime, String endTime, String releaseVersion,
                              String deviceInfo, String buildVersion, int fixedBugs, int newBugs,
                              Map<String, Integer> severityDistribution) {
            this.passCount = passCount;
            this.failCount = failCount;
            this.skippedCount = skippedCount;
            this.passPercentage = passPercentage;
            this.failPercentage = failPercentage;
            this.skippedPercentage = skippedPercentage;
            this.startTime = startTime;
            this.endTime = endTime;
            this.releaseVersion = releaseVersion;
            this.deviceInfo = deviceInfo;
            this.buildVersion = buildVersion;
            this.fixedBugs = fixedBugs;
            this.newBugs = newBugs;
            this.severityDistribution = severityDistribution;
        }

        // Getters (có thể thêm setters nếu cần)
        public int getPassCount() { return passCount; }
        public int getFailCount() { return failCount; }
        public int getSkippedCount() { return skippedCount; }
        public double getPassPercentage() { return passPercentage; }
        public double getFailPercentage() { return failPercentage; }
        public double getSkippedPercentage() { return skippedPercentage; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getReleaseVersion() { return releaseVersion; }
        public String getDeviceInfo() { return deviceInfo; }
        public String getBuildVersion() { return buildVersion; }
        public int getFixedBugs() { return fixedBugs; }
        public int getNewBugs() { return newBugs; }
        public Map<String, Integer> getSeverityDistribution() { return severityDistribution; }
    }

    public class TestCaseDetails {
        private String testCaseName;
        private String status;
        private int duration;
        private String severity;
        private String startTime;
        private String endTime;
        private String testSteps;
        private String expectedResult;
        private String actualResult;
        private String testDate;
        private String testerName;
        private String screenshotPath;

        public TestCaseDetails(String testCaseName, String status, int duration, String severity,
                               String startTime, String endTime, String testSteps,
                               String expectedResult, String actualResult, String testDate,
                               String testerName, String screenshotPath) {
            this.testCaseName = testCaseName;
            this.status = status;
            this.duration = duration;
            this.severity = severity;
            this.startTime = startTime;
            this.endTime = endTime;
            this.testSteps = testSteps;
            this.expectedResult = expectedResult;
            this.actualResult = actualResult;
            this.testDate = testDate;
            this.testerName = testerName;
            this.screenshotPath = screenshotPath;
        }

        // Getter cho các field
        public String getTestCaseName() { return testCaseName; }
        public String getStatus() { return status; }
        public int getDuration() { return duration; }
        public String getSeverity() { return severity; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getTestSteps() { return testSteps; }
        public String getExpectedResult() { return expectedResult; }
        public String getActualResult() { return actualResult; }
        public String getTestDate() { return testDate; }
        public String getTesterName() { return testerName; }
        public String getScreenshotPath() { return screenshotPath; }
    }




    // Hàm setBorderStyle
    private static void setBorderStyle(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }


    public static void addImageToExcel(XSSFWorkbook workbook, XSSFSheet sheet, byte[] imageBytes, int rowIndex, int colIndex) {
        if (imageBytes == null || imageBytes.length == 0) {
            System.out.println("❌ Không có dữ liệu ảnh để chèn tại hàng " + rowIndex + ", cột " + colIndex);
            return;
        }
        try {
            int pictureIdx = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setCol1(colIndex);
            anchor.setRow1(rowIndex);
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);
            row.setHeightInPoints(118);
            sheet.setColumnWidth(colIndex, 20 * 256);
            XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
            double colWidth = sheet.getColumnWidthInPixels(colIndex);
            double rowHeight = row.getHeightInPoints() * 0.75;
            double scaleX = colWidth / picture.getImageDimension().getWidth();
            double scaleY = rowHeight / picture.getImageDimension().getHeight();
            double scale = Math.min(scaleX, scaleY);
            picture.resize(scale);
            System.out.println("✅ Ảnh đã được thêm vào thành công tại " + rowIndex + "," + colIndex);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi chèn ảnh: " + e.getMessage());
        }
    }




    public static void addTestCaseDetails(
            XSSFWorkbook workbook, XSSFSheet stepSheet,
            String tinhNang, String testCaseName, String testSteps,
            String status, String mucDoUuTien,
            String expectedResult, String actualResult,
            String startTime, String endTime,
            String testerName, byte[] screenshotBytes) {

        // Nếu chưa có header, tạo header mới
        if (stepSheet.getPhysicalNumberOfRows() == 0) {
            String[] stepHeaders1 = {"TỔNG CỘNG", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA", "ĐẠT %", "KHÔNG ĐẠT %", "BỎ QUA %"};
            String[] stepHeaders2 = {"STT", "TRƯỜNG HỢP KIỂM THỬ", "BƯỚC THỰC HIỆN", "TRẠNG THÁI",
                    "MỨC ĐỘ ƯU TIÊN", "KẾT QUẢ MONG MUỐN", "KẾT QUẢ THỰC TẾ", "THỜI GIAN BẮT ĐẦU",
                    "THỜI GIAN KẾT THÚC", "NGƯỜI KIỂM THỬ", "BẰNG CHỨNG"};

            Row stepHeaderRow1 = stepSheet.createRow(1);
            stepHeaderRow1.setHeightInPoints(33f);
            CellStyle headerStyle1 = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle1.setFont(headerFont);
            headerStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 32, 96), null));
            headerStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle1.setAlignment(HorizontalAlignment.CENTER);
            headerStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(headerStyle1);

            for (int i = 0; i < stepHeaders1.length; i++) {
                Cell cell = stepHeaderRow1.createCell(i + 1);
                cell.setCellValue(stepHeaders1[i]);
                cell.setCellStyle(headerStyle1);
            }

            Row totalRow = stepSheet.createRow(2);
            totalRow.setHeightInPoints(30f);
            CellStyle totalRowStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setFontName("Arial");
            totalRowStyle.setFont(totalFont);
            totalRowStyle.setAlignment(HorizontalAlignment.CENTER);
            totalRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(totalRowStyle);

            totalRow.createCell(1).setCellValue(0);
            totalRow.createCell(2).setCellValue(0);
            totalRow.createCell(3).setCellValue(0);
            totalRow.createCell(4).setCellValue(0);
            totalRow.createCell(5).setCellValue(0.0);
            totalRow.createCell(6).setCellValue(0.0);
            totalRow.createCell(7).setCellValue(0.0);

            for (int i = 1; i <= 7; i++) {
                Cell cell = totalRow.getCell(i);
                if (cell == null) {
                    cell = totalRow.createCell(i);
                }
                cell.setCellStyle(totalRowStyle);
            }

            stepSheet.createRow(3);

            Row stepHeaderRow2 = stepSheet.createRow(4);
            stepHeaderRow2.setHeightInPoints(37.5f);
            CellStyle headerStyle2 = workbook.createCellStyle();
            headerStyle2.cloneStyleFrom(headerStyle1);
            headerStyle2.setAlignment(HorizontalAlignment.CENTER);
            headerStyle2.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int i = 0; i < stepHeaders2.length; i++) {
                Cell cell = stepHeaderRow2.createCell(i + 1);
                cell.setCellValue(stepHeaders2[i]);
                cell.setCellStyle(headerStyle2);
            }

            stepSheet.setColumnWidth(0, 700);
            stepSheet.setColumnWidth(1, 15 * 256);
            stepSheet.setColumnWidth(2, 30 * 256);
            stepSheet.setColumnWidth(3, 50 * 256);
            stepSheet.setColumnWidth(4, 30 * 256);
            stepSheet.setColumnWidth(5, 30 * 256);
            stepSheet.setColumnWidth(6, 50 * 256);
            stepSheet.setColumnWidth(7, 50 * 256);
            stepSheet.setColumnWidth(8, 30 * 256);
            stepSheet.setColumnWidth(9, 30 * 256);
            stepSheet.setColumnWidth(10, 30 * 256);
            stepSheet.setColumnWidth(11, 25 * 256);
        }

        // Tạo style cho các ô giá trị
        CellStyle valueStyle = workbook.createCellStyle();
        Font valueFont = workbook.createFont();
        valueFont.setFontName("Arial");
        valueStyle.setFont(valueFont);
        valueStyle.setWrapText(true);
        valueStyle.setAlignment(HorizontalAlignment.LEFT);
        valueStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(valueStyle);

        CellStyle featureStyle = workbook.createCellStyle();
        Font featureFont = workbook.createFont();
        featureFont.setFontName("Arial");
        featureStyle.setFont(featureFont);
        featureStyle.setWrapText(true);
        featureStyle.setAlignment(HorizontalAlignment.LEFT);
        featureStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(featureStyle);

        CellStyle sttStyle = workbook.createCellStyle();
        Font sttFont = workbook.createFont();
        sttFont.setFontName("Arial");
        sttStyle.setFont(sttFont);
        sttStyle.setAlignment(HorizontalAlignment.CENTER);
        sttStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(sttStyle);

        CellStyle testCaseStyle = workbook.createCellStyle();
        Font testCaseFont = workbook.createFont();
        testCaseFont.setFontName("Arial");
        testCaseStyle.setFont(testCaseFont);
        testCaseStyle.setWrapText(true);
        testCaseStyle.setAlignment(HorizontalAlignment.LEFT);
        testCaseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(testCaseStyle);

        CellStyle centerStyle = workbook.createCellStyle();
        Font centerFont = workbook.createFont();
        centerFont.setFontName("Arial");
        centerStyle.setFont(centerFont);
        centerStyle.setWrapText(true);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(centerStyle);

        CellStyle statusPassStyle = workbook.createCellStyle();
        statusPassStyle.cloneStyleFrom(valueStyle);
        statusPassStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(166, 227, 182), null));
        statusPassStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusPassStyle.setAlignment(HorizontalAlignment.CENTER);
        statusPassStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle statusFailStyle = workbook.createCellStyle();
        statusFailStyle.cloneStyleFrom(valueStyle);
        statusFailStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(246, 179, 174), null));
        statusFailStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusFailStyle.setAlignment(HorizontalAlignment.CENTER);
        statusFailStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle statusSkipStyle = workbook.createCellStyle();
        statusSkipStyle.cloneStyleFrom(valueStyle);
        statusSkipStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 204), null));
        statusSkipStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        statusSkipStyle.setAlignment(HorizontalAlignment.CENTER);
        statusSkipStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle priorityCriticalStyle = workbook.createCellStyle();
        priorityCriticalStyle.cloneStyleFrom(valueStyle);
        priorityCriticalStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 0, 0), null));
        priorityCriticalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        priorityCriticalStyle.setAlignment(HorizontalAlignment.CENTER);
        priorityCriticalStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle priorityHighStyle = workbook.createCellStyle();
        priorityHighStyle.cloneStyleFrom(valueStyle);
        priorityHighStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 97, 97), null));
        priorityHighStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        priorityHighStyle.setAlignment(HorizontalAlignment.CENTER);
        priorityHighStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle priorityMediumStyle = workbook.createCellStyle();
        priorityMediumStyle.cloneStyleFrom(valueStyle);
        priorityMediumStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), null));
        priorityMediumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        priorityMediumStyle.setAlignment(HorizontalAlignment.CENTER);
        priorityMediumStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle priorityLowStyle = workbook.createCellStyle();
        priorityLowStyle.cloneStyleFrom(valueStyle);
        priorityLowStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(148, 239, 251), null));
        priorityLowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        priorityLowStyle.setAlignment(HorizontalAlignment.CENTER);
        priorityLowStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle defaultPriorityStyle = workbook.createCellStyle();
        defaultPriorityStyle.cloneStyleFrom(valueStyle);
        defaultPriorityStyle.setAlignment(HorizontalAlignment.CENTER);
        defaultPriorityStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(defaultPriorityStyle);

        // Xử lý dữ liệu đầu vào
        testSteps = testSteps.replace(",", "\n").trim();
        expectedResult = expectedResult.replace(",", "\n").trim();
        actualResult = actualResult.replace(",", "\n").trim();

        int rowNum = stepSheet.getPhysicalNumberOfRows();
        int newRowNum = rowNum + 1;

        // Thêm hàng mới
        Row infoRow = stepSheet.createRow(newRowNum);
        infoRow.createCell(1).setCellValue(newRowNum - 4);
        infoRow.createCell(2).setCellValue(testCaseName);
        infoRow.createCell(3).setCellValue(testSteps);
        Cell statusCell = infoRow.createCell(4);
        statusCell.setCellValue(status);
        Cell priorityCell = infoRow.createCell(5);
        priorityCell.setCellValue(mucDoUuTien);
        infoRow.createCell(6).setCellValue(expectedResult);
        infoRow.createCell(7).setCellValue(actualResult);
        infoRow.createCell(8).setCellValue(startTime);
        infoRow.createCell(9).setCellValue(endTime);
        infoRow.createCell(10).setCellValue(testerName);

        if (screenshotBytes != null && screenshotBytes.length > 0) {
            System.out.println("✅ Chuẩn bị chèn ảnh vào ô " + newRowNum + ",11");
            addImageToExcel(workbook, stepSheet, screenshotBytes, newRowNum, 11);
            System.out.println("✅ Đã chèn ảnh thành công.");
        } else {
            System.out.println("⚠️ Không chèn ảnh vì screenshotBytes rỗng hoặc null.");
        }

        // Áp dụng style cho các ô
        for (int i = 1; i <= 12; i++) {
            Cell cell = infoRow.getCell(i);
            if (cell == null) {
                cell = infoRow.createCell(i);
            }
            if (i == 1) {
                cell.setCellStyle(sttStyle);
            } else if (i == 2) {
                cell.setCellStyle(testCaseStyle);
            } else if (i == 3) {
                cell.setCellStyle(valueStyle);
            } else if (i == 4) {
                switch (status) {
                    case "Đạt":
                        cell.setCellStyle(statusPassStyle);
                        break;
                    case "Không đạt":
                        cell.setCellStyle(statusFailStyle);
                        break;
                    case "Bỏ qua":
                        cell.setCellStyle(statusSkipStyle);
                        break;
                    default:
                        cell.setCellStyle(valueStyle);
                        break;
                }
            } else if (i == 5) {
                switch (mucDoUuTien) {
                    case "Nghiêm trọng":
                        cell.setCellStyle(priorityCriticalStyle);
                        break;
                    case "Cao":
                        cell.setCellStyle(priorityHighStyle);
                        break;
                    case "Trung bình":
                        cell.setCellStyle(priorityMediumStyle);
                        break;
                    case "Thấp":
                        cell.setCellStyle(priorityLowStyle);
                        break;
                    default:
                        cell.setCellStyle(defaultPriorityStyle);
                        break;
                }
            } else if (i == 6) {
                cell.setCellStyle(valueStyle);
            } else if (i == 7) {
                cell.setCellStyle(valueStyle);
            } else if (i == 8) {
                cell.setCellStyle(centerStyle);
            } else if (i == 9) {
                cell.setCellStyle(centerStyle);
            } else if (i == 10) {
                cell.setCellStyle(centerStyle);
            } else if (i == 11) {
                cell.setCellStyle(centerStyle);
            }
        }

        // Cập nhật số liệu thống kê
        Row totalRow = stepSheet.getRow(2);
        if (totalRow == null) {
            totalRow = stepSheet.createRow(2);
            totalRow.setHeightInPoints(30f);
            CellStyle totalRowStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setFontName("Arial");
            totalRowStyle.setFont(totalFont);
            totalRowStyle.setAlignment(HorizontalAlignment.CENTER);
            totalRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(totalRowStyle);

            totalRow.createCell(1).setCellValue(0);
            totalRow.createCell(2).setCellValue(0);
            totalRow.createCell(3).setCellValue(0);
            totalRow.createCell(4).setCellValue(0);
            totalRow.createCell(5).setCellValue(0.0);
            totalRow.createCell(6).setCellValue(0.0);
            totalRow.createCell(7).setCellValue(0.0);
        }

        int total = 0, passCount = 0, failCount = 0, skipCount = 0;
        for (int i = 5; i <= stepSheet.getLastRowNum(); i++) {
            Row row = stepSheet.getRow(i);
            if (row != null) {
                Cell statusCellCheck = row.getCell(4); // Cột trạng thái giờ là cột 4
                if (statusCellCheck != null && statusCellCheck.getCellType() == CellType.STRING) {
                    String statusValue = statusCellCheck.getStringCellValue();
                    total++;
                    if ("Đạt".equals(statusValue)) {
                        passCount++;
                    } else if ("Không đạt".equals(statusValue)) {
                        failCount++;
                    } else if ("Bỏ qua".equals(statusValue)) {
                        skipCount++;
                    }
                }
            }
        }

        double passPercent = total > 0 ? (double) passCount / total * 100 : 0;
        double failPercent = total > 0 ? (double) failCount / total * 100 : 0;
        double skipPercent = total > 0 ? (double) skipCount / total * 100 : 0;

        totalRow.getCell(1).setCellValue(total);
        totalRow.getCell(2).setCellValue(passCount);
        totalRow.getCell(3).setCellValue(failCount);
        totalRow.getCell(4).setCellValue(skipCount);
        totalRow.getCell(5).setCellValue(String.format("%.2f", passPercent));
        totalRow.getCell(6).setCellValue(String.format("%.2f", failPercent));
        totalRow.getCell(7).setCellValue(String.format("%.2f", skipPercent));

        // Áp dụng style cho totalRow
        CellStyle totalRowStyle = workbook.createCellStyle();
        Font totalFont = workbook.createFont();
        totalFont.setFontName("Arial");
        totalRowStyle.setFont(totalFont);
        totalRowStyle.setAlignment(HorizontalAlignment.CENTER);
        totalRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorderStyle(totalRowStyle);
        for (int i = 1; i <= 7; i++) {
            Cell cell = totalRow.getCell(i);
            if (cell == null) {
                cell = totalRow.createCell(i);
            }
            cell.setCellStyle(totalRowStyle);
        }

        // Logic merge mới cho Tính năng
        List<Integer> rowsToMerge = new ArrayList<>();
        for (int i = 5; i <= newRowNum; i++) {
            Row row = stepSheet.getRow(i);
            if (row != null) {
                Cell featureCellCheck = row.getCell(2);
                if (featureCellCheck != null && tinhNang.equals(featureCellCheck.getStringCellValue())) {
                    rowsToMerge.add(i);
                }
            }
        }

        if (rowsToMerge.size() > 1) {
            for (int i = stepSheet.getNumMergedRegions() - 1; i >= 0; i--) {
                CellRangeAddress region = stepSheet.getMergedRegion(i);
                if (region.getFirstColumn() == 2 && region.getLastColumn() == 2) {
                    Row rowInRegion = stepSheet.getRow(region.getFirstRow());
                    if (rowInRegion != null) {
                        Cell cellInRegion = rowInRegion.getCell(2);
                        if (cellInRegion != null && tinhNang.equals(cellInRegion.getStringCellValue())) {
                            stepSheet.removeMergedRegion(i);
                        }
                    }
                }
            }

            int firstRow = rowsToMerge.get(0);
            int lastRow = rowsToMerge.get(rowsToMerge.size() - 1);
            stepSheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, 2, 2));
            Row firstRowMerged = stepSheet.getRow(firstRow);
            Cell mergedCell = firstRowMerged.getCell(2);
            mergedCell.setCellStyle(featureStyle);
        }

        infoRow.setHeightInPoints(75);
    }


}
