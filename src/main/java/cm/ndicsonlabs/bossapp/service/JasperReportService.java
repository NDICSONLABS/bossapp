// src/main/java/com/institution/finance/service/JasperReportService.java
package cm.ndicsonlabs.bossapp.service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JasperReportService {

    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    public JasperPrint fill(String jasperResource, Map<String, Object> parameters, List<?> data) {
        try {
            JasperReport report = getCompiledReport(jasperResource);
            JRDataSource dataSource = new JRBeanCollectionDataSource(data);
            return JasperFillManager.fillReport(report, parameters, dataSource);
        } catch (JRException ex) {
            throw new RuntimeException("Failed to fill Jasper report: " + jasperResource, ex);
        }
    }

    public byte[] exportPdf(JasperPrint print) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));

            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            return output.toByteArray();
        } catch (JRException ex) {
            throw new RuntimeException("Failed to export PDF report", ex);
        }
    }

    public byte[] exportCsv(JasperPrint print) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRCsvExporter exporter = new JRCsvExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleWriterExporterOutput(output));

            SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            return output.toByteArray();
        } catch (JRException ex) {
            throw new RuntimeException("Failed to export CSV report", ex);
        }
    }

    public byte[] exportXlsx(JasperPrint print) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));

            SimpleXlsxExporterConfiguration configuration = new SimpleXlsxExporterConfiguration();
            exporter.setConfiguration(configuration);

            exporter.exportReport();

            return output.toByteArray();
        } catch (JRException ex) {
            throw new RuntimeException("Failed to export XLSX report", ex);
        }
    }

    private JasperReport getCompiledReport(String jasperResource) {
        return reportCache.computeIfAbsent(jasperResource, resource -> {
            try (InputStream input = getClass().getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IllegalArgumentException("Jasper resource not found: " + resource);
                }

                return JasperCompileManager.compileReport(input);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to compile Jasper report: " + resource, ex);
            }
        });
    }
}