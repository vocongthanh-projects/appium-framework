package reports;

import bases.ConfigManager;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

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

public class AllureToExcelExporter {
    private static final String DEFAULT_TESTER = "Công Thành";

    // =====================================================================
    // FIX #1: Cache CellStyle theo workbook để tránh tạo lặp vượt giới hạn
    // 64,000 styles của Apache POI → gây corrupt file Excel
    // =====================================================================
    private static final Map<XSSFWorkbook, Map<String, CellStyle>> STYLE_CACHE = new WeakHashMap<>();

    private static CellStyle getCachedStyle(XSSFWorkbook workbook, String key) {
        return STYLE_CACHE
                .computeIfAbsent(workbook, k -> new HashMap<>())
                .get(key);
    }

    private static void putCachedStyle(XSSFWorkbook workbook, String key, CellStyle style) {
        STYLE_CACHE
                .computeIfAbsent(workbook, k -> new HashMap<>())
                .put(key, style);
    }

    /**
     * Tạo hoặc lấy CellStyle từ cache. Tránh tạo style trùng lặp trong vòng lặp.
     */
    private static CellStyle getOrCreateStyle(XSSFWorkbook workbook, String key,
            StyleConfigurator configurator) {
        CellStyle cached = getCachedStyle(workbook, key);
        if (cached != null)
            return cached;
        CellStyle style = workbook.createCellStyle();
        configurator.configure(style, workbook);
        putCachedStyle(workbook, key, style);
        return style;
    }

    @FunctionalInterface
    interface StyleConfigurator {
        void configure(CellStyle style, XSSFWorkbook workbook);
    }

    // =====================================================================
    // Các hàm tạo style dùng chung — chỉ tạo 1 lần per workbook
    // =====================================================================

    private static CellStyle buildHeaderStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "HEADER", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 32, 96), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorderStyle(style);
            style.setWrapText(false);
        });
    }

    private static CellStyle buildWrapHeaderStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "HEADER_WRAP", (style, workbook) -> {
            style.cloneStyleFrom(buildHeaderStyle(workbook));
            style.setWrapText(true);
        });
    }

    private static CellStyle buildValueStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "VALUE", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildLeftValueStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "VALUE_LEFT", (style, workbook) -> {
            style.cloneStyleFrom(buildValueStyle(workbook));
            style.setAlignment(HorizontalAlignment.LEFT);
        });
    }

    private static CellStyle buildMainHeaderStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "MAIN_HEADER", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setBold(true);
            font.setColor(IndexedColors.BLACK.getIndex());
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildSttStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "STT", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildTestCaseStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "TEST_CASE", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildCenterStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "CENTER", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildFeatureStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "FEATURE", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildTotalRowStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "TOTAL_ROW", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    private static CellStyle buildStatusPassStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "STATUS_PASS", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(166, 227, 182), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildStatusFailStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "STATUS_FAIL", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(246, 179, 174), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildStatusSkipStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "STATUS_SKIP", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 204), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildPriorityCriticalStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "PRIORITY_CRITICAL", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 0, 0), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildPriorityHighStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "PRIORITY_HIGH", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 97, 97), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildPriorityMediumStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "PRIORITY_MEDIUM", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildPriorityLowStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "PRIORITY_LOW", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(148, 239, 251), null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildDefaultPriorityStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "PRIORITY_DEFAULT", (style, workbook) -> {
            style.cloneStyleFrom(buildLeftValueStyle(workbook));
            style.setAlignment(HorizontalAlignment.CENTER);
        });
    }

    private static CellStyle buildHyperlinkStyle(XSSFWorkbook wb) {
        return getOrCreateStyle(wb, "HYPERLINK", (style, workbook) -> {
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setUnderline(Font.U_SINGLE);
            font.setColor(IndexedColors.BLUE.getIndex());
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorderStyle(style);
        });
    }

    // =====================================================================
    // Phần logic nghiệp vụ — GIỮ NGUYÊN, chỉ thay thế style creation
    // =====================================================================

    @SuppressWarnings("unchecked")
    public void exportCucumberReportToExcel(String[] jsonPaths, String excelPathBase) {
        Map<String, List<JSONObject>> scenariosByConfig = new HashMap<>();

        try {
            JSONParser parser = new JSONParser();
            for (String path : jsonPaths) {
                File f = new File(path);
                if (!f.exists())
                    continue;
                JSONArray report = (JSONArray) parser.parse(new FileReader(f));
                for (Object featureObj : report) {
                    JSONObject feature = (JSONObject) featureObj;
                    JSONArray elements = (JSONArray) feature.get("elements");
                    if (elements == null)
                        continue;
                    for (Object elementObj : elements) {
                        JSONObject scenario = (JSONObject) elementObj;
                        scenario.put("parentFeatureName", feature.get("name"));
                        String config = extractConfigFromScenario(scenario);
                        scenariosByConfig.computeIfAbsent(config, k -> new ArrayList<>()).add(scenario);
                    }
                }
            }

            for (Map.Entry<String, List<JSONObject>> entry : scenariosByConfig.entrySet()) {
                String configName = entry.getKey();
                String safeConfigName = configName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

                File baseFile = new File(excelPathBase);
                String rootDirName = baseFile.getParentFile().getName();

                String browserExcelDir = ConfigManager.getExcelReportPath(rootDirName) + safeConfigName + "/";
                new File(browserExcelDir).mkdirs();
                String finalExcelPath = browserExcelDir + baseFile.getName();

                String browserScreenshotPath = ConfigManager.getScreenshotPath() + rootDirName + "/" + safeConfigName
                        + "/";
                new File(browserScreenshotPath).mkdirs();

                processSingleExcelExport(entry.getValue(), finalExcelPath, browserScreenshotPath);
                System.out.println("✅ Đã xuất báo cáo trình duyệt [" + configName + "] tại: " + browserExcelDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractConfigFromScenario(JSONObject scenario) {
        String[] hookKeys = { "before", "steps", "after" };
        for (String key : hookKeys) {
            JSONArray hooks = (JSONArray) scenario.get(key);
            if (hooks == null)
                continue;
            for (Object h : hooks) {
                JSONObject hook = (JSONObject) h;
                List<Object> outputs = new ArrayList<>();
                if (hook.containsKey("output"))
                    outputs.addAll((JSONArray) hook.get("output"));
                JSONObject res = (JSONObject) hook.get("result");
                if (res != null && res.containsKey("output"))
                    outputs.addAll((JSONArray) res.get("output"));
                for (Object o : outputs) {
                    if (o != null) {
                        String s = o.toString().trim();
                        if (s.startsWith("CONFIG="))
                            return s.split("=")[1].trim();
                    }
                }
            }
        }
        return "Default";
    }

    @SuppressWarnings("unchecked")
    public void processSingleExcelExport(List<JSONObject> scenariosForThisConfig,
            String excelPath,
            String currentScreenshotPath) {
        Map<String, Integer> totalSeverityDistribution = new HashMap<>();
        totalSeverityDistribution.put("Nghiêm trọng", 0);
        totalSeverityDistribution.put("Cao", 0);
        totalSeverityDistribution.put("Trung bình", 0);
        totalSeverityDistribution.put("Thấp", 0);
        totalSeverityDistribution.put("Không xác định", 0);

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet summarySheet = workbook.createSheet("Tổng hợp");

            // Tự động phân tách root profile từ excelPath
            File excelFile = new File(excelPath);
            String profile = excelFile.getParentFile().getParentFile().getName(); // Lấy 'notlogin' hoặc 'login'
            File directory = new File(ConfigManager.getExcelReportPath(profile));
            if (!directory.exists()) {
                directory.mkdirs();
                System.out.println("📁 Đã tạo thư mục lưu báo cáo: " + directory.getAbsolutePath());
            }

            File sDir = new File(currentScreenshotPath);
            if (!sDir.exists()) {
                sDir.mkdirs();
                System.out.println("📁 Đã tạo thư mục lưu ảnh: " + currentScreenshotPath);
            }

            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            ZonedDateTime earliestStartTime = null;
            ZonedDateTime latestEndTime = null;

            int globalTotalTests = 0, globalPassed = 0, globalFailed = 0, globalSkipped = 0;
            int totalNewBugs = 0, totalFixedBugs = 0;

            Map<String, String> featureMap = new LinkedHashMap<>();
            int featureIndex = 0;

            Map<String, List<JSONObject>> scenariosByFeature = new LinkedHashMap<>();
            for (JSONObject sc : scenariosForThisConfig) {
                String fName = (String) sc.getOrDefault("parentFeatureName", "UnnamedFeature");
                scenariosByFeature.computeIfAbsent(fName, k -> new ArrayList<>()).add(sc);
            }

            for (Map.Entry<String, List<JSONObject>> featureEntry : scenariosByFeature.entrySet()) {
                String featureName = featureEntry.getKey();
                List<JSONObject> scenarios = featureEntry.getValue();

                featureIndex++;
                String sheetName = String.format("%03d", featureIndex);
                featureMap.put(sheetName, featureName);

                XSSFSheet testCaseSheet = workbook.getSheet(sheetName);
                if (testCaseSheet == null) {
                    testCaseSheet = workbook.createSheet(sheetName);
                } else {
                    clearSheet(testCaseSheet);
                }

                int totalTests = 0, passed = 0, failed = 0, skipped = 0;
                int newBugs = 0, fixedBugs = 0;
                Map<String, Integer> severityDistribution = new HashMap<>();
                severityDistribution.put("Nghiêm trọng", 0);
                severityDistribution.put("Cao", 0);
                severityDistribution.put("Trung bình", 0);
                severityDistribution.put("Thấp", 0);
                severityDistribution.put("Không xác định", 0);

                for (JSONObject scenario : scenarios) {

                    JSONArray steps = (JSONArray) scenario.get("steps");
                    totalTests++;

                    String overallStatus = "Đạt";
                    String priority = "-";
                    String skippedStepName = "";

                    for (Object stepObj : steps) {
                        JSONObject step = (JSONObject) stepObj;
                        JSONObject result = (JSONObject) step.get("result");
                        if (result == null)
                            continue;

                        String status = (String) result.get("status");
                        if (status == null)
                            status = "skipped";

                        if (status.equalsIgnoreCase("failed")) {
                            overallStatus = "Không đạt";
                            failed++;
                            String errorMessage = result.containsKey("error_message")
                                    ? (String) result.get("error_message")
                                    : "";
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
                    if ("Đạt".equals(overallStatus))
                        passed++;

                    String rawStartTime = (String) scenario.get("start_timestamp");
                    String rawEndTime = (String) scenario.get("end_timestamp");
                    ZonedDateTime startTime = null;
                    ZonedDateTime endTime = null;

                    if (rawStartTime != null && !rawStartTime.isEmpty()) {
                        startTime = ZonedDateTime.parse(rawStartTime).withZoneSameInstant(vietnamZone);
                        if (earliestStartTime == null || startTime.isBefore(earliestStartTime))
                            earliestStartTime = startTime;
                    }

                    if (rawEndTime != null && !rawEndTime.isEmpty()) {
                        endTime = ZonedDateTime.parse(rawEndTime).withZoneSameInstant(vietnamZone);
                    } else if (startTime != null) {
                        endTime = startTime.plusSeconds(1);
                    } else {
                        endTime = ZonedDateTime.now(vietnamZone);
                    }
                    if (latestEndTime == null || endTime.isAfter(latestEndTime))
                        latestEndTime = endTime;

                    String scenarioName = (String) scenario.get("name");
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String startTimeFormatted = (startTime != null) ? startTime.format(fmt) : "N/A";
                    String endTimeFormatted = (endTime != null) ? endTime.format(fmt) : "N/A";

                    String testSteps = "";
                    String expectedResult = "N/A";
                    int lastThenIndex = -1;

                    for (int i = 0; i < steps.size(); i++) {
                        JSONObject step = (JSONObject) steps.get(i);
                        String keyword = (String) step.get("keyword");
                        if ("Then ".equals(keyword))
                            lastThenIndex = i;
                    }

                    if (lastThenIndex >= 0) {
                        for (int i = 0; i < lastThenIndex; i++) {
                            JSONObject step = (JSONObject) steps.get(i);
                            testSteps += (i + 1) + ". " + step.get("name") + "\n";
                        }
                        JSONObject lastThenStep = (JSONObject) steps.get(lastThenIndex);
                        expectedResult = (String) lastThenStep.get("name");
                        if (lastThenStep.containsKey("rows")) {
                            JSONArray rows = (JSONArray) lastThenStep.get("rows");
                            StringBuilder tableData = new StringBuilder(expectedResult).append("\n");
                            for (Object rowObj : rows) {
                                JSONObject row = (JSONObject) rowObj;
                                JSONArray cells = (JSONArray) row.get("cells");
                                for (Object cell : cells)
                                    tableData.append("| ").append(cell).append(" ");
                                tableData.append("|\n");
                            }
                            expectedResult = tableData.toString().trim();
                        }
                    } else {
                        for (int i = 0; i < steps.size(); i++) {
                            JSONObject step = (JSONObject) steps.get(i);
                            testSteps += (i + 1) + ". " + step.get("name") + "\n";
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
                            if (result == null)
                                continue;
                            String status = (String) result.get("status");
                            if (status != null && status.equalsIgnoreCase("skipped")) {
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

                    String screenshotPath = extractScreenshotPathFromScenario(scenario, currentScreenshotPath);

                    byte[] screenshotBytes = null;
                    if (screenshotPath != null) {
                        File sFile = new File(screenshotPath);
                        if (sFile.exists() && sFile.isFile())
                            screenshotBytes = convertScreenshotToBytes(screenshotPath);
                    }

                    addTestCaseDetails(
                            workbook, testCaseSheet,
                            featureName, scenarioName, testSteps,
                            overallStatus, priority,
                            expectedResult, actualResult,
                            startTimeFormatted, endTimeFormatted,
                            DEFAULT_TESTER, screenshotBytes);
                }

                globalTotalTests += totalTests;
                globalPassed += passed;
                globalFailed += failed;
                globalSkipped += skipped;
                totalNewBugs += newBugs;
                totalFixedBugs += fixedBugs;
                for (Map.Entry<String, Integer> entry : severityDistribution.entrySet()) {
                    totalSeverityDistribution.put(entry.getKey(),
                            totalSeverityDistribution.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }

            addSummaryReport(workbook, summarySheet,
                    globalPassed, globalFailed, globalSkipped,
                    (globalTotalTests > 0) ? (double) globalPassed / globalTotalTests * 100 : 0,
                    (globalTotalTests > 0) ? (double) globalFailed / globalTotalTests * 100 : 0,
                    (globalTotalTests > 0) ? (double) globalSkipped / globalTotalTests * 100 : 0,
                    earliestStartTime != null
                            ? earliestStartTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                            : "N/A",
                    latestEndTime != null
                            ? latestEndTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                            : "N/A",
                    "1.0.0", "Mac mini m2", "Build 20250226",
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
    }

    @SuppressWarnings("unchecked")
    private String extractScreenshotPathFromScenario(JSONObject scenario, String currentScreenshotPath) {
        if (scenario == null)
            return null;

        String[] keys = { "after", "steps", "before" };
        for (String key : keys) {
            JSONArray hooks = (JSONArray) scenario.get(key);
            if (hooks == null)
                continue;
            for (Object h : hooks) {
                JSONObject hook = (JSONObject) h;
                List<Object> outputs = new ArrayList<>();
                if (hook.containsKey("output")) {
                    JSONArray out = (JSONArray) hook.get("output");
                    if (out != null)
                        outputs.addAll(out);
                }
                JSONObject res = (JSONObject) hook.get("result");
                if (res != null && res.containsKey("output")) {
                    JSONArray out = (JSONArray) res.get("output");
                    if (out != null)
                        outputs.addAll(out);
                }
                for (Object o : outputs) {
                    if (o != null && o.toString().trim().startsWith("SCREENSHOT_PATH="))
                        return o.toString().split("=")[1].trim();
                }
            }
        }

        try {
            String scenarioName = (String) scenario.get("name");
            if (scenarioName != null) {
                String safeName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_");
                File dir = new File(currentScreenshotPath);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles((d, name) -> name.startsWith(safeName) && name.endsWith(".png"));
                    if (files != null && files.length > 0) {
                        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                        return files[0].getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi đọc đường dẫn ảnh dự phòng (fallback): " + e.getMessage());
        }
        return null;
    }

    // =====================================================================
    // FIX #2: addSummaryReport — dùng style cache, tránh tạo style mới
    // mỗi lần gọi; đồng thời tránh merged region trùng chéo
    // =====================================================================
    public static void addSummaryReport(XSSFWorkbook workbook, XSSFSheet summarySheet,
            int passCount, int failCount, int skippedCount,
            double passPercentage, double failPercentage, double skippedPercentage,
            String startTime, String endTime, String releaseVersion,
            String deviceInfo, String buildVersion, int fixedBugs, int newBugs,
            Map<String, Integer> severityDistribution,
            Map<String, String> featureMap) {

        // Xóa tất cả merged regions trước khi thêm mới → tránh chồng chéo
        for (int i = summarySheet.getNumMergedRegions() - 1; i >= 0; i--) {
            summarySheet.removeMergedRegion(i);
        }

        // Dùng hàm cache thay vì tạo style mới
        CellStyle headerStyle = buildHeaderStyle(workbook);
        CellStyle wrapHeaderStyle = buildWrapHeaderStyle(workbook);
        CellStyle valueStyle = buildValueStyle(workbook);
        CellStyle leftAlignedStyle = buildLeftValueStyle(workbook);
        CellStyle mainHeaderStyle = buildMainHeaderStyle(workbook);

        // ===== BẢNG THÔNG TIN MÔI TRƯỜNG (row 2) =====
        Row envHeaderRow = summarySheet.createRow(1);
        envHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 4; i++) {
            Cell cell = envHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1)
                cell.setCellValue("BẢNG THÔNG TIN MÔI TRƯỜNG KIỂM THỬ");
        }
        safeAddMergedRegion(summarySheet, 1, 1, 1, 4);

        Row deviceRow = summarySheet.createRow(2);
        deviceRow.setHeightInPoints(33f);
        String[] deviceHeaders = { "STT", "MÔI TRƯỜNG", "THÔNG TIN THIẾT BỊ", "PHIÊN BẢN" };
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

        // ===== BẢNG KẾT QUẢ KIỂM THỬ (row 6) =====
        Row testResultHeaderRow = summarySheet.createRow(5);
        testResultHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 13; i++) {
            Cell cell = testResultHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1)
                cell.setCellValue("BẢNG THÔNG TIN KẾT QUẢ KIỂM THỬ QUA CÁC LẦN");
        }
        safeAddMergedRegion(summarySheet, 5, 5, 1, 13);

        Row testCaseRow = summarySheet.createRow(6);
        testCaseRow.setHeightInPoints(33f);
        String[] testCaseHeaders = { "STT", "PHIÊN BẢN", "TỔNG SỐ TC", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA",
                "ĐẠT %", "KHÔNG ĐẠT %", "BỎ QUA %", "THỜI GIAN BẮT ĐẦU", "THỜI GIAN KẾT THÚC",
                "LỖI MỚI", "LỖI ĐÃ SỬA" };
        for (int i = 0; i < testCaseHeaders.length; i++) {
            Cell cell = testCaseRow.createCell(i + 1);
            cell.setCellValue(testCaseHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        Object[] newData = {
                1, buildVersion,
                passCount + failCount + skippedCount,
                passCount, failCount, skippedCount,
                String.format("%.2f%%", passPercentage),
                String.format("%.2f%%", failPercentage),
                String.format("%.2f%%", skippedPercentage),
                startTime, endTime, newBugs, fixedBugs
        };

        int firstTestCaseRow = 7;
        Row dataRow = summarySheet.getRow(firstTestCaseRow);
        if (dataRow == null)
            dataRow = summarySheet.createRow(firstTestCaseRow);
        dataRow.setHeightInPoints(30f);
        for (int j = 1; j <= 13; j++) {
            Cell cell = dataRow.getCell(j);
            if (cell == null)
                cell = dataRow.createCell(j);
            cell.setCellStyle(valueStyle);
            Object value = newData[j - 1];
            if (value instanceof String)
                cell.setCellValue((String) value);
            else if (value instanceof Double)
                cell.setCellValue((Double) value);
            else if (value instanceof Integer)
                cell.setCellValue((Integer) value);
        }

        // ===== BẢNG MỨC ĐỘ NGHIÊM TRỌNG =====
        int nextRowAfterTestCase = firstTestCaseRow + 1;
        Row severityHeaderRow = summarySheet.createRow(nextRowAfterTestCase + 1);
        severityHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 5; i++) {
            Cell cell = severityHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1)
                cell.setCellValue("BẢNG ĐÁNH GIÁ MỨC ĐỘ NGHIÊM TRỌNG CÁC LỖI PHÁT HIỆN");
        }
        safeAddMergedRegion(summarySheet, nextRowAfterTestCase + 1, nextRowAfterTestCase + 1, 1, 5);

        Row severityRow = summarySheet.createRow(nextRowAfterTestCase + 2);
        severityRow.setHeightInPoints(33f);
        String[] severityHeaders = { "STT", "MỨC ĐỘ NGHIÊM TRỌNG", "SỐ LƯỢNG LỖI", "ĐÃ SỬA", "CÒN TỒN ĐỌNG" };
        for (int i = 0; i < severityHeaders.length; i++) {
            Cell cell = severityRow.createCell(i + 1);
            cell.setCellValue(severityHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        String[] severityLevels = { "🔴 NGHIÊM TRỌNG", "🟠 CAO", "🟡 TRUNG BÌNH", "🟢 THẤP" };
        String[] severityKeys = { "Nghiêm trọng", "Cao", "Trung bình", "Thấp" };
        for (int i = 0; i < severityLevels.length; i++) {
            Row row = summarySheet.createRow(nextRowAfterTestCase + 3 + i);
            row.setHeightInPoints(30f);
            for (int j = 1; j <= 5; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(j == 2 ? leftAlignedStyle : valueStyle);
            }
            int severityCount = severityDistribution.getOrDefault(severityKeys[i], 0);
            row.getCell(1).setCellValue(i + 1);
            row.getCell(2).setCellValue(severityLevels[i]);
            row.getCell(3).setCellValue(severityCount);
            row.getCell(4).setCellValue(0);
            row.getCell(5).setCellValue(severityCount);
        }

        // ===== BẢNG TỔNG HỢP CHI TIẾT =====
        int nextRowAfterSeverity = nextRowAfterTestCase + 3 + severityLevels.length;
        Row detailHeaderRow = summarySheet.createRow(nextRowAfterSeverity + 1);
        detailHeaderRow.setHeightInPoints(20f);
        for (int i = 1; i <= 6; i++) {
            Cell cell = detailHeaderRow.createCell(i);
            cell.setCellStyle(mainHeaderStyle);
            if (i == 1)
                cell.setCellValue("BẢNG TỔNG HỢP CHI TIẾT");
        }
        safeAddMergedRegion(summarySheet, nextRowAfterSeverity + 1, nextRowAfterSeverity + 1, 1, 6);

        Row pageListRow1 = summarySheet.createRow(nextRowAfterSeverity + 2);
        pageListRow1.setHeightInPoints(33f);
        String[] pageListHeadersTop = { "STT", "DANH SÁCH TRANG", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA",
                "TỔNG SỐ TRƯỜNG HỢP KIỂM THỬ" };
        for (int i = 0; i < pageListHeadersTop.length; i++) {
            Cell cell = pageListRow1.createCell(i + 1);
            cell.setCellValue(pageListHeadersTop[i]);
            cell.setCellStyle(i == 5 ? wrapHeaderStyle : headerStyle);
        }

        CellStyle hyperlinkStyle = buildHyperlinkStyle(workbook);

        int firstDetailRow = nextRowAfterSeverity + 3;
        int rowIndex = firstDetailRow;
        Map<String, Integer> existingFeatures = new HashMap<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if ("Tổng hợp".equals(sheetName))
                continue;

            String sheetIndex = sheetName;
            String fullFeatureName = sheetIndex + " - " + featureMap.getOrDefault(sheetIndex, "UnnamedFeature");

            int totalTests = 0, passed = 0, failed = 0, skipped = 0;
            for (Row row : sheet) {
                if (row.getRowNum() <= 4)
                    continue;
                Cell statusCell = row.getCell(4);
                if (statusCell != null) {
                    String status;
                    switch (statusCell.getCellType()) {
                        case STRING:
                            status = statusCell.getStringCellValue();
                            break;
                        case NUMERIC:
                            status = String.valueOf((int) statusCell.getNumericCellValue());
                            break;
                        case FORMULA:
                            status = statusCell.getCellFormula();
                            break;
                        default:
                            status = "";
                            break;
                    }
                    totalTests++;
                    if ("Đạt".equals(status))
                        passed++;
                    else if ("Không đạt".equals(status))
                        failed++;
                    else if ("Bỏ qua".equals(status))
                        skipped++;
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

        // Hàng TỔNG
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

        // Auto-size cột
        for (int i = 0; i <= 13; i++) {
            summarySheet.autoSizeColumn(i);
            int currentWidth = summarySheet.getColumnWidth(i);
            switch (i) {
                case 0:
                    summarySheet.setColumnWidth(i, 700);
                    break;
                case 3:
                case 10:
                case 11:
                    summarySheet.setColumnWidth(i, (int) (currentWidth * 1.5));
                    break;
                default:
                    if (currentWidth < 4000)
                        summarySheet.setColumnWidth(i, 4000);
                    break;
            }
        }
    }

    // =====================================================================
    // FIX #3: Thêm helper safeAddMergedRegion để kiểm tra trùng trước khi merge
    // =====================================================================
    private static void safeAddMergedRegion(XSSFSheet sheet, int firstRow, int lastRow,
            int firstCol, int lastCol) {
        CellRangeAddress newRegion = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        // Kiểm tra xem region mới có chồng chéo với region nào đã có không
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress existing = sheet.getMergedRegion(i);
            if (existing.intersects(newRegion)) {
                sheet.removeMergedRegion(i);
                break;
            }
        }
        sheet.addMergedRegion(newRegion);
    }

    private static int getNumericCellValue(Cell cell) {
        if (cell == null)
            return 0;
        try {
            return (int) cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            String value = cell.getStringCellValue().replaceAll("[^0-9]", "");
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        }
    }

    private String determinePriorityFromError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty())
            return "Không xác định";
        String lower = errorMessage.toLowerCase();
        if (lower.contains("timeout"))
            return getPriorityLevel("TimeoutError");
        if (lower.contains("browser"))
            return getPriorityLevel("BrowserError");
        if (lower.contains("page"))
            return getPriorityLevel("PageError");
        if (lower.contains("protocol"))
            return getPriorityLevel("ProtocolError");
        if (lower.contains("element not found") || lower.contains("no such element"))
            return getPriorityLevel("ElementNotFoundError");
        if (lower.contains("assertion") || lower.contains("assert"))
            return getPriorityLevel("AssertionError");
        if (lower.contains("navigation"))
            return getPriorityLevel("NavigationError");
        if (lower.contains("frame detached"))
            return getPriorityLevel("FrameDetachedError");
        if (lower.contains("file not found"))
            return getPriorityLevel("FileNotFoundError");
        if (lower.contains("interception"))
            return getPriorityLevel("InterceptionError");
        return "Không xác định";
    }

    public static String getPriorityLevel(String errorType) {
        if (errorType == null || errorType.trim().isEmpty())
            return "Không xác định";
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
            if (file.exists() && !file.isDirectory())
                return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.out.println("❌ Lỗi khi đọc file ảnh: " + e.getMessage());
        }
        return null;
    }

    private void clearSheet(XSSFSheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null)
                sheet.removeRow(row);
        }
    }

    public static void addImageToExcel(XSSFWorkbook workbook, XSSFSheet sheet,
            byte[] imageBytes, int rowIndex, int colIndex) {
        int pictureIdx = workbook.addPicture(imageBytes, XSSFWorkbook.PICTURE_TYPE_PNG);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(colIndex);
        anchor.setRow1(rowIndex);

        XSSFRow row = sheet.getRow(rowIndex);
        if (row == null)
            row = sheet.createRow(rowIndex);
        row.setHeightInPoints(118);
        sheet.setColumnWidth(colIndex, 20 * 256);

        XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
        double colWidth = sheet.getColumnWidthInPixels(colIndex);
        double rowHeight = row.getHeightInPoints() * 0.75;
        double scaleX = colWidth / picture.getImageDimension().getWidth();
        double scaleY = rowHeight / picture.getImageDimension().getHeight();
        picture.resize(Math.min(scaleX, scaleY));
    }

    // =====================================================================
    // FIX #4: addTestCaseDetails — dùng style cache, KHÔNG tạo style mới
    // trong mỗi lần gọi → tránh vượt giới hạn 64,000 styles
    // =====================================================================
    public static void addTestCaseDetails(
            XSSFWorkbook workbook, XSSFSheet stepSheet,
            String tinhNang, String testCaseName, String testSteps,
            String status, String mucDoUuTien,
            String expectedResult, String actualResult,
            String startTime, String endTime,
            String testerName, byte[] screenshotBytes) {

        // Lấy style từ cache — KHÔNG gọi workbook.createCellStyle() ở đây nữa
        CellStyle headerStyle1 = buildHeaderStyle(workbook);
        CellStyle valueStyle = buildLeftValueStyle(workbook);
        CellStyle featureStyle = buildFeatureStyle(workbook);
        CellStyle sttStyle = buildSttStyle(workbook);
        CellStyle testCaseStyle = buildTestCaseStyle(workbook);
        CellStyle centerStyle = buildCenterStyle(workbook);
        CellStyle totalRowStyle = buildTotalRowStyle(workbook);
        CellStyle statusPassStyle = buildStatusPassStyle(workbook);
        CellStyle statusFailStyle = buildStatusFailStyle(workbook);
        CellStyle statusSkipStyle = buildStatusSkipStyle(workbook);
        CellStyle priorityCritical = buildPriorityCriticalStyle(workbook);
        CellStyle priorityHigh = buildPriorityHighStyle(workbook);
        CellStyle priorityMedium = buildPriorityMediumStyle(workbook);
        CellStyle priorityLow = buildPriorityLowStyle(workbook);
        CellStyle priorityDefault = buildDefaultPriorityStyle(workbook);

        // Tạo header nếu chưa có
        if (stepSheet.getPhysicalNumberOfRows() == 0) {
            String[] stepHeaders1 = { "TỔNG CỘNG", "ĐẠT", "KHÔNG ĐẠT", "BỎ QUA", "ĐẠT %", "KHÔNG ĐẠT %", "BỎ QUA %" };
            String[] stepHeaders2 = { "STT", "TRƯỜNG HỢP KIỂM THỬ", "BƯỚC THỰC HIỆN", "TRẠNG THÁI",
                    "MỨC ĐỘ ƯU TIÊN", "KẾT QUẢ MONG MUỐN", "KẾT QUẢ THỰC TẾ",
                    "THỜI GIAN BẮT ĐẦU", "THỜI GIAN KẾT THÚC", "NGƯỜI KIỂM THỬ", "BẰNG CHỨNG" };

            Row stepHeaderRow1 = stepSheet.createRow(1);
            stepHeaderRow1.setHeightInPoints(33f);
            for (int i = 0; i < stepHeaders1.length; i++) {
                Cell cell = stepHeaderRow1.createCell(i + 1);
                cell.setCellValue(stepHeaders1[i]);
                cell.setCellStyle(headerStyle1);
            }

            Row totalRow = stepSheet.createRow(2);
            totalRow.setHeightInPoints(30f);
            for (int i = 1; i <= 7; i++) {
                Cell cell = totalRow.createCell(i);
                cell.setCellStyle(totalRowStyle);
                cell.setCellValue(0);
            }

            stepSheet.createRow(3);

            Row stepHeaderRow2 = stepSheet.createRow(4);
            stepHeaderRow2.setHeightInPoints(37.5f);
            for (int i = 0; i < stepHeaders2.length; i++) {
                Cell cell = stepHeaderRow2.createCell(i + 1);
                cell.setCellValue(stepHeaders2[i]);
                cell.setCellStyle(headerStyle1);
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

        testSteps = testSteps.replace(",", "\n").trim();
        expectedResult = expectedResult.replace(",", "\n").trim();
        actualResult = actualResult.replace(",", "\n").trim();

        int rowNum = stepSheet.getPhysicalNumberOfRows();
        int newRowNum = rowNum + 1;

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

        if (screenshotBytes != null && screenshotBytes.length > 0)
            addImageToExcel(workbook, stepSheet, screenshotBytes, newRowNum, 11);

        // Áp dụng style cho từng ô
        for (int i = 1; i <= 12; i++) {
            Cell cell = infoRow.getCell(i);
            if (cell == null)
                cell = infoRow.createCell(i);
            switch (i) {
                case 1:
                    cell.setCellStyle(sttStyle);
                    break;
                case 2:
                    cell.setCellStyle(testCaseStyle);
                    break;
                case 3:
                    cell.setCellStyle(valueStyle);
                    break;
                case 4:
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
                    break;
                case 5:
                    switch (mucDoUuTien) {
                        case "Nghiêm trọng":
                            cell.setCellStyle(priorityCritical);
                            break;
                        case "Cao":
                            cell.setCellStyle(priorityHigh);
                            break;
                        case "Trung bình":
                            cell.setCellStyle(priorityMedium);
                            break;
                        case "Thấp":
                            cell.setCellStyle(priorityLow);
                            break;
                        default:
                            cell.setCellStyle(priorityDefault);
                            break;
                    }
                    break;
                case 6:
                case 7:
                    cell.setCellStyle(valueStyle);
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                    cell.setCellStyle(centerStyle);
                    break;
            }
        }

        // Cập nhật thống kê tổng hợp
        Row totalRow = stepSheet.getRow(2);
        if (totalRow == null) {
            totalRow = stepSheet.createRow(2);
            totalRow.setHeightInPoints(30f);
            for (int i = 1; i <= 7; i++) {
                Cell cell = totalRow.createCell(i);
                cell.setCellStyle(totalRowStyle);
                cell.setCellValue(0);
            }
        }

        int total = 0, passCount = 0, failCount = 0, skipCount = 0;
        for (int i = 5; i <= stepSheet.getLastRowNum(); i++) {
            Row row = stepSheet.getRow(i);
            if (row != null) {
                Cell sc = row.getCell(4);
                if (sc != null && sc.getCellType() == CellType.STRING) {
                    String sv = sc.getStringCellValue();
                    total++;
                    if ("Đạt".equals(sv))
                        passCount++;
                    else if ("Không đạt".equals(sv))
                        failCount++;
                    else if ("Bỏ qua".equals(sv))
                        skipCount++;
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

        for (int i = 1; i <= 7; i++) {
            Cell cell = totalRow.getCell(i);
            if (cell == null)
                cell = totalRow.createCell(i);
            cell.setCellStyle(totalRowStyle);
        }

        // FIX #5: Logic merge Tính năng — dùng safeAddMergedRegion tránh chồng chéo
        List<Integer> rowsToMerge = new ArrayList<>();
        for (int i = 5; i <= newRowNum; i++) {
            Row row = stepSheet.getRow(i);
            if (row != null) {
                Cell featureCellCheck = row.getCell(2);
                if (featureCellCheck != null && tinhNang.equals(featureCellCheck.getStringCellValue()))
                    rowsToMerge.add(i);
            }
        }

        if (rowsToMerge.size() > 1) {
            // Xóa merge region cũ của tinhNang này
            for (int i = stepSheet.getNumMergedRegions() - 1; i >= 0; i--) {
                CellRangeAddress region = stepSheet.getMergedRegion(i);
                if (region.getFirstColumn() == 2 && region.getLastColumn() == 2) {
                    Row rowInRegion = stepSheet.getRow(region.getFirstRow());
                    if (rowInRegion != null) {
                        Cell cellInRegion = rowInRegion.getCell(2);
                        if (cellInRegion != null && tinhNang.equals(cellInRegion.getStringCellValue()))
                            stepSheet.removeMergedRegion(i);
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

    private static void setBorderStyle(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // =====================================================================
    // Inner classes giữ nguyên
    // =====================================================================

}