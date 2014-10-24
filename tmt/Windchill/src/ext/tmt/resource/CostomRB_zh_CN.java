package ext.tmt.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.tmt.resource.CostomRB")
public class CostomRB_zh_CN  extends WTListResourceBundle{
	
	@RBEntry("报表")
	public static final String LZR_FIRST_TITLE = "navigation.reports.description";
	
	@RBEntry("报表")
    public static final String LZR_FIRST = "navigation.reports.tooltip";
	
	@RBEntry("custom.gif")
	public static final String LZR_FIRST_ICON = "navigation.reports.icon";
    
	@RBEntry("部件报表")
	public static final String REPORT_PARTS_DES = "reports.parts.description";
	
	@RBEntry("部件报表")
    public static final String REPORT_PARTS_TOOL = "reports.parts.tooltip";
	
	@RBEntry("文档报表")
	public static final String REPORT_DOCS_DES = "reports.docs.description";
	
	@RBEntry("文档报表")
    public static final String REPORT_DOCS_TOOL = "reports.docs.tooltip";
	
	@RBEntry("BOM报表")
	public static final String REPORT_BOMS_DES = "reports.boms.description";
	
	@RBEntry("BOM报表")
    public static final String REPORT_BOMS_TOOL = "reports.boms.tooltip";
	
	@RBEntry("标准件报表")
	public static final String PART_STAND_DES = "part.standardReport.description";
	
	@RBEntry("标准件报表")
    public static final String PART_STAND_TOOL = "part.standardReport.tooltip";
	
	
}
