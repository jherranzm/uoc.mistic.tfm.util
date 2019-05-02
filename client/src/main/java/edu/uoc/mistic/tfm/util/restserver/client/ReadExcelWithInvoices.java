package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StrSubstitutor;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.format.CellDateFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcelWithInvoices {

	private static Log logger = LogFactory.getLog(ReadExcelWithInvoices.class);

	public static void main(String[] args) throws IOException {

		try {
			
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
			String release = df.format(Calendar.getInstance().getTime());
			
			String directoryName ="/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/"+ release;
			
			File directory = new File(directoryName);
		    if (! directory.exists()){
		        directory.mkdir();
		        // If you require it to make the entire directory path including parents,
		        // use directory.mkdirs(); here instead.
		    }
		    directoryName +="/signed";
			
			directory = new File(directoryName);
		    if (! directory.exists()){
		        directory.mkdir();
		        // If you require it to make the entire directory path including parents,
		        // use directory.mkdirs(); here instead.
		    }
			
			File excelFile = new File("/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/Planificació_jherranzm.xlsx");
			if (!excelFile.exists()) {
				throw new FileNotFoundException();
			}
			FileInputStream fis = new FileInputStream(excelFile);

			// we create an XSSF Workbook object for our XLSX Excel File
			XSSFWorkbook workbook = new XSSFWorkbook(fis);

			XSSFSheet sheet = workbook.getSheet("Facturas");

			logger.info(String.format("La pestaña tiene [%d] filas. ", sheet.getLastRowNum()));

			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			String templateString = "";
			File file = new File(classLoader.getResource("Facturae.template").getFile());
			if (file.exists()) {
				templateString = new String(Files.readAllBytes(file.toPath()));
				System.out.println(templateString);
			}

			Map<Integer, String> headers = new HashMap<>();
			XSSFRow headersRow = sheet.getRow(1);
			Iterator<Cell> cellHeaderIterator = headersRow.cellIterator();
			while (cellHeaderIterator.hasNext()) {
				XSSFCell c = (XSSFCell) cellHeaderIterator.next();
				System.out.println(c.getColumnIndex() + " : " + c.getStringCellValue());
				headers.put(c.getColumnIndex(), c.getStringCellValue());
			}
			// System.exit(0);

			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			String numFactura = "";
			for (Row row : sheet) {

				if (row.getRowNum() > 1) {

					Map<String, String> substitutes = new HashMap<>();

					for (Cell cell : row) {

						CellValue cellValue = evaluator.evaluate(cell);
						
						if(headers.get(cell.getColumnIndex()).equals("BatchIdentifier")) {
							numFactura = cellValue.getStringValue();
						}
						
						switch (cellValue.getCellTypeEnum()) {
						case BOOLEAN:
							System.out.println(headers.get(cell.getColumnIndex())+"|"+cellValue.getBooleanValue()+";");
							substitutes.put(headers.get(cell.getColumnIndex()),
									(cellValue.getBooleanValue() ? "True" : "False"));
							break;
						case NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
								System.out.println("NUMERIC+DATE|"+headers.get(cell.getColumnIndex())+"|"+dateFormat.format(cell.getDateCellValue()) + ";");
								
								substitutes.put(headers.get(cell.getColumnIndex()),
										""+dateFormat.format(cell.getDateCellValue()));
							} else {
								System.out.println("NUMERIC|"+headers.get(cell.getColumnIndex())+"|"+""+ (new Double(cell.getNumericCellValue()).intValue()) + ";");
								if(headers.get(cell.getColumnIndex()).equals("UnitOfMeasure")) {
									substitutes.put(headers.get(cell.getColumnIndex()),
											""+ (new Double(cell.getNumericCellValue()).intValue()) );
								}else if(headers.get(cell.getColumnIndex()).equals("InvoiceNumber")) {
									substitutes.put(headers.get(cell.getColumnIndex()),
											""+ (new Double(cell.getNumericCellValue()).intValue()) );
								}else{
									substitutes.put(headers.get(cell.getColumnIndex()),
										""+cellValue.getNumberValue());
								}
							}
							break;
						case STRING:
							System.out.println(headers.get(cell.getColumnIndex())+"|"+cellValue.getStringValue()+";");
							substitutes.put(headers.get(cell.getColumnIndex()),
									""+cellValue.getStringValue());
							break;
						case BLANK:
							System.out.println("BLANK|"+headers.get(cell.getColumnIndex())+"|"+cellValue.getStringValue()+";");
							substitutes.put(headers.get(cell.getColumnIndex()),
									""+cellValue.getStringValue());
							break;
						case ERROR:
							if(cell.getCellTypeEnum() == CellType.NUMERIC) {
								System.out.println(headers.get(cell.getColumnIndex())+"|"+cell.getNumericCellValue() + ";");
								substitutes.put(headers.get(cell.getColumnIndex()),
										""+cellValue.getNumberValue());
							}else if (cell.getCellTypeEnum() == CellType.STRING) {
								System.out.println("ERROR|"+headers.get(cell.getColumnIndex())+"|"+cell.getRichStringCellValue()+";");
								substitutes.put(headers.get(cell.getColumnIndex()),
										""+cell.getRichStringCellValue());
							}else {
								if (DateUtil.isCellDateFormatted(cell)) {
									
									double dv = cellValue.getNumberValue();
									Date date = HSSFDateUtil.getJavaDate(dv);
									
									String dateFmt = cell.getCellStyle().getDataFormatString();
									String strValue = new CellDateFormatter(dateFmt).format(date); 
									
									SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
									System.out.println(headers.get(cell.getColumnIndex())+"|"+dateFormat.format(cell.getDateCellValue()) + ";"+ strValue);
									substitutes.put(headers.get(cell.getColumnIndex()),
											""+dateFormat.format(cell.getDateCellValue()));
								} else {
									System.out.println(headers.get(cell.getColumnIndex())+"|"+cell.getNumericCellValue() + ";");
									substitutes.put(headers.get(cell.getColumnIndex()),
											""+cellValue.getNumberValue());
								}
							}
							break;
						case FORMULA:
							System.out.println("FORMULA|"+headers.get(cell.getColumnIndex())+"|"+cellValue.getStringValue()+";");
							substitutes.put(headers.get(cell.getColumnIndex()),
									""+cellValue.getStringValue());
							break;
						default:
							System.out.println("DEFAULT|"+headers.get(cell.getColumnIndex())+"|"+cellValue.getStringValue()+";");
							substitutes.put(headers.get(cell.getColumnIndex()),
									""+cellValue.getStringValue());
							break;
						}// switch
					} // for

					System.out.println("");

					StrSubstitutor sub = new StrSubstitutor(substitutes);
					String result = sub.replace(templateString);
					System.out.println(result);
					
					// Save to file
					String fileNameXML = "/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/"+release+"/"+numFactura+".xml";
					try (PrintWriter outFile = new PrintWriter(fileNameXML)) {
						outFile.println(result);
					}
					
					EnvelopedSignature.signXMLFile(fileNameXML, "/Users/jherranzm/Dropbox/Jose_Luis/TFM_2019/"+release+"/signed/");
					
				} // if

			} // for
			workbook.close();
			// ...
			logger.info("Fin!");
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getClass().getName() + ":" + e.getLocalizedMessage() + ":" + e.getMessage() );
			e.printStackTrace();
		}
	}
}
