package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.BasicBSONList;

import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * �ĵ� ��Ӧ�� PLMϵͳ�е��ĵ�������WTDocument
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
	 * �Ƿ��ύ��ΪPLM����ʽ�ύ����
	 */
	private static final String F_IS_COMMIT = "iscommit";
	

	public PMDocument(){
		
	}
	
	/**
	 * @return ��Ʒ����
	 */
	public String getProductNumber() {
		return (String) get(PRODUCT_NUMBER);
	}

	/**
	 * @param productNumber
	 *            Ҫ���õĳ�Ʒ����
	 */
	public void setProductNumber(String productNumber) {
		setValue(PRODUCT_NUMBER, productNumber);
	}

	/**
	 * @return �����ļ��б�
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
	 * @return ���ļ�
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
	 * ��÷��ʵ�url
	 * 
	 * @return ���ⲿ���ʵ�url
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
	 * ����PLM�ύ��״̬
	 * @param iscommit true or false
	 */
	public void setCommit(boolean iscommit) {
		setValue(F_IS_COMMIT, iscommit);
	}

}
