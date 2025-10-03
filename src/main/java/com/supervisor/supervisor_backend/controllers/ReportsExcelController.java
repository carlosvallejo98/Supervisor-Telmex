package com.supervisor.supervisor_backend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supervisor.supervisor_backend.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@RestController
@RequestMapping("/reports")
public class ReportsExcelController {

    private final ProxyService proxy;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReportsExcelController(ProxyService proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(HttpServletRequest req) {
        try {
            String auth = req.getHeader("Authorization");

            // 🔹 Pido los reports al telecom-backend
            ResponseEntity<String> upstream = proxy.get(auth, "/reports", null);
            JsonNode root = mapper.readTree(upstream.getBody());

            // 🔹 Crear workbook en memoria
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Reportes");

            // Estilo para cabeceras
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Cabecera
            Row header = sheet.createRow(0);
            String[] cols = {"Ticket", "Ingeniero", "Contenido", "Fecha"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // 🔹 Rellenar filas con los datos de reports
            int rowIdx = 1;
            Iterator<JsonNode> it = root.elements();
            while (it.hasNext()) {
                JsonNode r = it.next();
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(r.path("ticketId").path("title").asText("—"));
                row.createCell(1).setCellValue(r.path("reporterId").path("name").asText("—"));
                row.createCell(2).setCellValue(r.path("content").asText("—"));
                row.createCell(3).setCellValue(r.path("createdAt").asText("—"));
            }

            // Autoajustar columnas
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            // 🔹 Escribir a byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            wb.close();

            byte[] bytes = bos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reportes.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar Excel: " + e.getMessage()).getBytes());
        }
    }
}
