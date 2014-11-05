package ext.tmt.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ExcelUtils {

	private HSSFWorkbook		wb;
	private HSSFSheet			sheet;
	private ArrayList<HSSFRow>	templateRows;
	private ArrayList<Region>	templateMergedRegion;
	private ArrayList<Object[]>	bodyMap;

	private int					maxPageCount	= 0;
	private int					startRow		= 0;

	public ExcelUtils() {

	}

	public boolean sheetWriter(String templatePath, String outputPath, ArrayList<ArrayList<String>> headData, ArrayList<Object[]> bodyData, int sheetIdx) throws FileNotFoundException, IOException {

		wb = null;
		sheet = null;
		templateRows = new ArrayList<HSSFRow>();
		templateMergedRegion = new ArrayList<Region>();
		bodyMap = new ArrayList<Object[]>();

		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(templatePath));
		wb = new HSSFWorkbook(fs);
		sheet = wb.getSheetAt(sheetIdx);
		System.out.println("SheetWriter Starting...");

		fillData(headData, bodyData);
		
		FileOutputStream fos = new FileOutputStream(outputPath);
		wb.write(fos); // 写文件
		fos.close(); // 关闭文件
		return true;
	}

	private void fillData(ArrayList<ArrayList<String>> headData, ArrayList<Object[]> bodyData) {
		// get region of template
		ArrayList<Number[]> a1 = getLableIndex("PAGESTART");
		ArrayList<Number[]> a2 = getLableIndex("PAGEEND");
		Number[] n1 = (Number[]) a1.get(0);
		Number[] n2 = (Number[]) a2.get(0);
		HashMap modelRegion = getArea(n1[0].intValue(), n1[1].shortValue(), n2[0].intValue(), n2[1].shortValue());
		int r1 = ((Number) modelRegion.get("startRow")).intValue();
		this.startRow = r1;
		short c1 = ((Number) modelRegion.get("startCell")).shortValue();
		int r2 = ((Number) modelRegion.get("endRow")).intValue();
		short c2 = ((Number) modelRegion.get("endCell")).shortValue();

		// clear Start label cells
		sheet.getRow(r1).getCell(c1).setCellValue(new HSSFRichTextString(""));
		sheet.getRow(r2).getCell(c2).setCellValue(new HSSFRichTextString(""));

		// save Template
		for (int i = r1; i <= r2; i++) {
			HSSFRow tempRow = sheet.getRow(i);
			templateRows.add(tempRow);
		}

		// Save Merged Cells
		int nrgn = sheet.getNumMergedRegions();
		for (int i = 0; i < nrgn; i++) {
			templateMergedRegion.add(sheet.getMergedRegionAt(i));
		}
		// create full template，preparing for gird and image
		templatePrepare(bodyData, r2 + 1);

		// fill bodyData
		System.out.println("bodyMap.size()---->"+bodyMap.size());
		for (int i = 0; i < bodyMap.size(); i++) {
			Object[] aMap = bodyMap.get(i);
			Number[] cord = (Number[]) aMap[0];
			String[][] matrix = (String[][]) aMap[1];
			System.out.println(cord);
			System.out.println(matrix.length);
			addGrid(cord, matrix);
		}

		addHead(headData);
		cleanSheet();
		//removeColumn(1, 0);

	}

	private void addHead(ArrayList headData) {
		Iterator it = headData.iterator();
		while (it.hasNext()) {
			ArrayList fieldValue = (ArrayList) it.next();
			String lableText = (String) fieldValue.get(0);
			String cellValue = (String) fieldValue.get(1);
			addTxtHead(getLableIndex(lableText), cellValue);
		}
	}

	private void cleanSheet() {
		Iterator rowIter = sheet.rowIterator();
		while (rowIter.hasNext()) {
			HSSFRow row = (HSSFRow) rowIter.next();
			Iterator cellIter = row.cellIterator();
			while (cellIter.hasNext()) {
				HSSFCell cell = (HSSFCell) cellIter.next();
				if (cell.getCellType() != 0) {
					String cellText = cell.getRichStringCellValue().getString().trim();
					if (cellText.startsWith("<") && cellText.endsWith(">")) {
						cell.setCellValue(new HSSFRichTextString());
					}
				}
			}
		}
	}
	
    
    /**
     * 写隐藏/删除后的Excel文件
     * @param targetFile  目标文件
     * @param wb          Excel对象
     * @throws Exception
     */
    public static void fileWrite(String targetFile,HSSFWorkbook wb) throws Exception{
        FileOutputStream fileOut = new FileOutputStream(targetFile);
        wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
    }
    
    /**
     *删掉指定的列 
     * @author Eilaiwang
     * @param sheet 要操作的Sheet
     * @param removeColumnNum 要删除的列索引(从 0开始)
     * @param removeColumnTotal 要删除第removeColumnNum列 中的行数 ，Cell不能为空
     * @return void
     * @Description
     */
    public static void removeColumn( HSSFSheet sheet,int removeColumnNum, int removeColumnTotal){
		  
		  if(sheet == null){
		   return;
		  }
		  for (Iterator<Row> rowIterator = sheet.rowIterator(); rowIterator.hasNext();) {
		          HSSFRow row = (HSSFRow)rowIterator.next();
		         HSSFCell cell = row.getCell(removeColumnNum);
		         
		         if(cell == null){
		          continue;
		         }
		          row.removeCell(cell);
		         
		          for(int n = removeColumnNum; n < (removeColumnTotal + removeColumnNum); n ++){
		           int columnWidth = sheet.getColumnWidth(n + 1);
		           
		           HSSFCell cell2 = row.getCell(n + 1);
		           
		           if(cell2 == null){
		            break;
		           }
		           sheet.setColumnWidth(n, columnWidth);
		           row.moveCell(cell2, (short)n);
		          }
		  }
		 }
    
    public static String deleteRowAndColumn(String fileFullPath,int columnLength) throws Exception{
    	FileInputStream fis = new FileInputStream(fileFullPath);
        HSSFWorkbook wb = new HSSFWorkbook(fis);
        HSSFSheet sheet= wb.getSheetAt(0);
        int i = 0;//sheet.getLastRowNum();
        //Debug.P("LastRowNum()-->"+i);
        sheet.shiftRows(1, sheet.getLastRowNum(), -1);
        while(i<=columnLength){
			Row row = sheet.getRow(i);
			Cell cell = row.createCell((short)0);
			cell.setCellValue("1");
			i++;
		}
        
        removeColumn(sheet,0,columnLength);
        FileOutputStream fos = new FileOutputStream(fileFullPath);
        wb.write(fos);
		fos.close();
		fis.close();
			return fileFullPath;
    }

	private void addTxtHead(ArrayList lableList, String cellValue) {
		Iterator lableIter = lableList.iterator();// 0--row,1--cell
		while (lableIter.hasNext()) {
			Number[] coordinate = (Number[]) lableIter.next();
			int rowNumber = coordinate[0].intValue();
			short cellNumber = coordinate[1].shortValue();
			HSSFRow row = sheet.getRow(rowNumber);
			HSSFCell cell = row.getCell(cellNumber);
			cell.setCellValue(new HSSFRichTextString((String) cellValue));
		}
	}

	private int setCell(HSSFRow row, String celldata, int pageGridRows, int step, int initStartRow, int pageRowCount) {
		if (celldata == null || celldata.equals("")) {
			return 0;
		}
		HSSFCell cell = row.createCell(initStartRow);

		if (cell != null)
			cell.setCellValue(new HSSFRichTextString(celldata == null ? "" : celldata));
		return 0;

	}

	private void addGrid(Number[] cord, String[][] matrix) {
		int initStartRow = cord[0].intValue();
		short initStartCell = cord[1].shortValue();
		int initEndRow = cord[2].intValue();
		short initEndCell = cord[3].shortValue();

		int columnCount = initEndCell - initStartCell + 1;
		int pageIdx = 0;
		int pageRowCount = templateRows.size();
		// int pageGridRows = initEndRow - initStartRow + 1;
		int step = pageRowCount - (initEndRow - initStartRow) - 1;

		int rowNum = 0;
		for (int x = 0; x < matrix.length; x++) {// row
			int rowCord = initStartRow + pageIdx * step;

			int dataColumn = 0;
			int appendRowNum = 0;
			HSSFRow row = sheet.createRow(rowCord + rowNum);

			for (short y = 0; y < columnCount; y++) {// cell
				String[] rowData = matrix[x];
				if (dataColumn < rowData.length) {
					HSSFCell cell = row.createCell(dataColumn + 1);
					cell.setCellValue(rowData[dataColumn]);
					dataColumn++;
				}
			}

			rowNum += appendRowNum;
			rowNum++;

		}

	}

	private ArrayList<Object[]> templatePrepare(ArrayList bodyData, int pageStart) {

		// get how many page needed.
		// int maxPageCount = 0;
		for (int i = 0; i < bodyData.size(); i++) {
			Object[] elem = (Object[]) bodyData.get(i);
			String label = ((String) elem[0]).trim();
			if (label.startsWith("GRD:")) {
				String[][] valuemap = (String[][]) elem[1];
				int rowCount = valuemap.length;
                System.out.println("rowCount====>"+rowCount);
				ArrayList labelidx = getLableIndex(label);
				Number[] na = (Number[]) labelidx.get(0);
				Number[] nb = (Number[]) labelidx.get(1);
				HashMap dataRegion = getArea(na[0].intValue(), na[1].shortValue(), nb[0].intValue(), nb[1].shortValue());

				Number[] cord = new Number[4];
				cord[0] = (Number) dataRegion.get("startRow");
				cord[1] = (Number) dataRegion.get("startCell");
				cord[2] = (Number) dataRegion.get("endRow");
				cord[3] = (Number) dataRegion.get("endCell");

				int startRowIdx = cord[0].intValue();
				int endRowIdx = cord[2].intValue();

				int everyPageSpendRow = endRowIdx - startRowIdx + 1;
				int pageCount = getPageCount(rowCount, everyPageSpendRow);
				if (maxPageCount < pageCount) {
					maxPageCount = pageCount;
				}

				Object[] aBodyMap = new Object[2];
				aBodyMap[0] = cord;
				aBodyMap[1] = valuemap;
				bodyMap.add(aBodyMap);
			}
		}
		System.out.println("MaxPageCount:" + maxPageCount);
		// add those pages
		int nextPageStart = pageStart;
		for (int i = 1; i < maxPageCount; i++) {
			nextPageStart = addPageFrom(nextPageStart);
		}
		return bodyMap;
	}

	private int addPageFrom(int pageStart) {
		int tc = templateRows.size();
		int j = 0;
		for (int i = pageStart; i < pageStart + tc; i++) {
			HSSFRow trow = templateRows.get(j++);

			HSSFRow nrow = sheet.createRow(i);

			nrow.setHeight(trow.getHeight());

			for (short k = 0; k <= trow.getLastCellNum(); k++) {
				HSSFCell tcell = trow.getCell(k);
				HSSFCell ncell = nrow.createCell(k);
				if (tcell != null) {
					ncell.setCellStyle(tcell.getCellStyle());

					try {
						HSSFRichTextString v = tcell.getRichStringCellValue();
						if (!v.getString().equals("")) {
							ncell.setCellValue(v);
						}
					} catch (Exception e) {
						try {
							ncell.setCellValue(tcell.getDateCellValue());
						} catch (Exception e1) {
							try {
								ncell.setCellValue(tcell.getBooleanCellValue());
							} catch (Exception e2) {
								ncell.setCellValue(tcell.getNumericCellValue());
							}
						}
					}

					try {
						ncell.setCellComment(tcell.getCellComment());
					} catch (Exception e) {
					}

					try {
						ncell.setCellFormula(tcell.getCellFormula());
					} catch (Exception e) {
					}

				}
			}
		}
		for (int i = 0; i < templateMergedRegion.size(); i++) {
			Region tr = templateMergedRegion.get(i);
			Region nr = new Region();
			nr.setColumnFrom(tr.getColumnFrom());
			nr.setColumnTo(tr.getColumnTo());
			nr.setRowFrom(tr.getRowFrom() + pageStart);
			nr.setRowTo(tr.getRowTo() + pageStart);
			sheet.addMergedRegion(nr);
		}

		return pageStart + tc;
	}

	private int getPageCount(int rowCount, int everyPageSpendRow) {
		int i1 = rowCount / everyPageSpendRow;
		int i2 = rowCount % everyPageSpendRow;
		int pageCount;
		if (i1 > 0 && i2 > 0) {
			pageCount = i1 + 1;
		} else {
			pageCount = i1;
		}
		return pageCount;

	}

	private ArrayList<Number[]> getLableIndex(String lableText) {
		ArrayList<Number[]> cordlist = new ArrayList<Number[]>();

		String aLable = "<" + lableText.trim() + ">";
		Iterator<?> rowIter = sheet.rowIterator();
		while (rowIter.hasNext()) {
			HSSFRow row = (HSSFRow) rowIter.next();
			Iterator<?> cellIter = row.cellIterator();
			while (cellIter.hasNext()) {

				HSSFCell cell = (HSSFCell) cellIter.next();
				if (cell.getCellType() != 0) {
					String cellText = cell.getRichStringCellValue().getString().trim();
					if (cellText.equals(aLable)) {
						Number[] cord = new Number[2];
						cord[0] = row.getRowNum();
						cord[1] = cell.getCellNum();
						cordlist.add(cord);
					}
				}
			}
		}
		return cordlist;
	}

	private HashMap getArea(int row1, short cell1, int row2, short cell2) {
		HashMap<String, Number> hs = new HashMap<String, Number>();
		if (row1 == row2) {
			if (cell1 < cell2) {
				hs.put("startCell", cell1);
				hs.put("startRow", row1);
				hs.put("endCell", cell2);
				hs.put("endRow", row2);
			} else {
				hs.put("startCell", cell2);
				hs.put("startRow", row2);
				hs.put("endCell", cell1);
				hs.put("endRow", row1);
			}
		} else {
			if (row1 < row2) {
				hs.put("startCell", cell1);
				hs.put("startRow", row1);
				hs.put("endCell", cell2);
				hs.put("endRow", row2);
			} else {
				hs.put("startCell", cell2);
				hs.put("startRow", row2);
				hs.put("endCell", cell1);
				hs.put("endRow", row1);
			}
		}
		return hs;
	}

}
