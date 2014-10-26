package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;

/**
 * 文档 对应于 PLM系统中的文档，例如WTDocument
 * 
 * @author zhonghua
 *
 */
public final class PMDocument extends BasicDocument implements IProjectRelative {

	private static final String PRODUCT_NUMBER = "productnumber";

	private static final String VAULT = "vault";

	private static final String CONTENT = "contentvault";

	private static final String URL = "url";

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
		put(PRODUCT_NUMBER, productNumber);
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
		if(value instanceof DBObject){
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
		put(URL, url);
	}

	@Override
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	@Override
	public void setProjectId(ObjectId projectId) {
		put(PROJECT_ID, projectId);
	}

	@Override
	public String getProjectName() {
		return (String) get(PROJECT_DESC);
	}

	@Override
	public void setProjectName(String projectDesc) {
		put(PROJECT_DESC, projectDesc);
	}

	@Override
	public String getProjectNumber() {
		return (String) get(PROJECT_NUMBER);
	}

	@Override
	public void setProjectNumber(String projectNumber) {
		put(PROJECT_NUMBER, projectNumber);
	}

	@Override
	public String getProjectWorkOrder() {
		return (String) get(PROJECT_WORK_ORDER);
	}

	@Override
	public void setProjectWorkOrder(String projectWorkOrder) {
		put(PROJECT_WORK_ORDER, projectWorkOrder);
	}
	

}
