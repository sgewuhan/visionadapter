package ext.tmt.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 序列化反序列化操作
 * @author Administrator
 *
 */
public class SerializeObjectUtil implements Cloneable,Serializable{
     
	private transient InputStream ins;
	
	
	

	/**
	 * 序列化对象
	 */
	public void serializableObject(Object object) throws IOException {
		 ByteArrayOutputStream bos=new ByteArrayOutputStream();
		 ObjectOutputStream os=new ObjectOutputStream(bos);
		 os.defaultWriteObject();
		 os.writeObject(object);
		 os.close();
		
	}

	
   /**
    * 反序列化对象
    */

	public void readExternal(ObjectInputStream ins) throws IOException,
			ClassNotFoundException {
		Object obj=new Object();
		ins.defaultReadObject();
	   Object reObj=ins.readObject();
	   ins.close();
	}
	
	


}
