package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.BasicBSONList;

import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * 文档 对应于 PLM系统中的文档，例如WTDocument
 * 
 * @author zhonghua
 *
 */
public final class PMDocument extends BasicDocument  {

	private static final String PRODUCT_NUMBER = "productnumber";

	private static final String VAULT = "vault";

	private static final String CONTENT = "contentvault";

	private static final String URL = "url";
	
	/**
	 * 是否提交，为PLM两段式提交打标记
	 */
	private static final String F_IS_COMMIT = "iscommit";
	

	public PMDocument(){
		
	}
	
	/**
	 * @return 成品编码
	 */
	public String getProductNumber() {
		return (String) get(PRODUCT_NUMBER);
	}

	/**
	 * @param productNumber
	 *            要设置的成品编码
	 */
	public void setProductNumber(String productNumber) {
		setValue(PRODUCT_NUMBER, productNumber);
	}

	/**
	 * @return 附件文件列表
	 */
	public List<IFileProvider> getAttachments() {
		List<IFileProvider> result = new ArrayList<IFileProvider>();
		Object list = get(VAULT);
		if (list instanceof BasicBSONList) {
			for (int i = 0; i < ((BasicBSONList) list).size(); i++) {
				IFileProvider item = getGridFSFileProvider((DBObject) ((BasicBSONList) list)
						.get(i));
				if(item!=null){
					result.add(item);
				}
			}
		}
		return result;
	}

	/**
	 * @return 主文件
	 */
	public IFileProvider getContent() {
		Object value = get(CONTENT);
		if(value instanceof BasicBSONList ){
			if(((BasicBSONList) value).size()>0){
				return getGridFSFileProvider((DBObject) ((BasicBSONList) value).get(0));
			}
		}else if(value instanceof DBObject){
			return getGridFSFileProvider((DBObject) value);
		}
		return null;
	}


	/**
	 * 获得访问的url
	 * 
	 * @return 供外部访问的url
	 */
	public String getUrl() {
		return (String) get(URL);
	}

	protected void setUrl(String url) {
		setValue(URL, url);
	}

	@Override
	public WriteResult doInsert() throws Exception {
		throw new Exception("PM Document can not insert by adapter.");
//		return super.doInsert();
	}
	
	/**
	 * 设置PLM提交的状态
	 * @param iscommit true or false
	 */
	public void setCommit(boolean iscommit) {
		setValue(F_IS_COMMIT, iscommit);
	}

}
