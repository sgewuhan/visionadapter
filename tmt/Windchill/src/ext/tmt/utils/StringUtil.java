package ext.tmt.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
public class StringUtil {
	
	
	/**
	 * 处理流水号
	 * 
	 * @param i
	 * @param subNum
	 * @return
	 */
	public static String int2String(int i, int subNum) {
		String returnValue = "00000000" + String.valueOf(i);
		return returnValue.substring(returnValue.length() - subNum, returnValue.length());
	}
	
	//通过HashSet剔除ArrayList中重复元素,
	public static void removeDuplicate(List list) {
	      HashSet h = new HashSet(list);
	      list.clear();
	      list.addAll(h);
	      System.out.println(list);
	}
	
	
	//删除ArrayList中重复元素，保持顺序 
	public static void removeDuplicateWithOrder(List list) {
		   Set set = new HashSet();
		   List newList = new ArrayList();
		   for (Iterator iter = list.iterator(); iter.hasNext();) {
		          Object element = iter.next();
		          if (set.add(element))
		             newList.add(element);
		       }
		      list.clear();
		      list.addAll(newList);
		     System.out.println( " remove duplicate " + list);
		}
	
	/**
	 * 正则表达式判断字符串是否为数字
	 * @author Eilaiwang
	 * @param str
	 * @return
	 * @return boolean
	 * @Description
	 */
	public static boolean isNumeric(String str){
	    Pattern pattern = Pattern.compile("[0-9]*");
	    return pattern.matcher(str).matches();   
	 }
	
	
	/**
	 * 获取文件扩展名
	 * @param filename
	 * @return
	 */
	public static String getExtension(String filename){
		String extension ="";
		extension = filename.substring(filename.lastIndexOf(".")+1);
		return extension;
	}
    
	/**
	 * 移除文件扩展名
	 * @param filename
	 * @return
	 */
	public static String removeExtension(String filename){
		return filename.substring(0,filename.lastIndexOf("."));
	}
	
	/**
	 * list 中每个字符串中添加引号
	 * @param list
	 * @return
	 */
    public static List quoteStrList(List list) {
        List tmpList = list;
        list = new ArrayList();
        Iterator i = tmpList.iterator();
        while (i.hasNext()) {
            String str = (String) i.next();
            str = "'" + str + "'";
            list.add(str);
        }
        return list;
    }
    
    /**
     * 将list 转换为字符串
     * @param list
     * @param delim
     * @return
     */
    public static String join(List list, String delim) {
        if (list == null || list.size() < 1)
            return null;
        StringBuffer buf = new StringBuffer();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            buf.append((String) i.next());
            if (i.hasNext())
                buf.append(delim);
        }
        return buf.toString();
    }


    /**
	 * Escape SQL tags, ' to ''; \ to \\.
	 * 
	 * @param input
	 *            string to replace
	 * @return string
	 */
    public static String escapeSQLTags(String input) {
        if (input == null || input.length() == 0)
            return input;
        StringBuffer buf = new StringBuffer();
        char ch = ' ';
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (ch == '\\')
                buf.append("\\\\");
            else if (ch == '\'')
                buf.append("\'\'");
            else
                buf.append(ch);
        }
        return buf.toString();
    }
    /**
	 * Escape HTML tags.
	 * 
	 * @param input
	 *            string to replace
	 * @return string
	 */
    public static String escapeHTMLTags(String input) {
        if (input == null || input.length() == 0)
            return input;
        StringBuffer buf = new StringBuffer();
        char ch = ' ';
        for (int i = 0; i < input.length(); i++) {
            ch = input.charAt(i);
            if (ch == '<')
                buf.append("&lt;");
            else if (ch == '>')
                buf.append("&gt;");
            else if (ch == '&')
                buf.append("&amp;");
            else if (ch == '"')
                buf.append("&quot;");
            else
                buf.append(ch);
        }
        return buf.toString();
    }
    /**
	 * Convert new lines, \n or \r\n to <BR />.
	 * 
	 * @param input
	 *            string to convert
	 * @return string
	 */
    public static String convertNewlines(String input) {
        input = replace(input, "\r\n", "\n");
        input = replace(input, "\n", "<BR/>");
        return input;
    }
    
    /**
	 * Convert new lines, \n or \r\n to <BR />.
	 * 
	 * @param input
	 *            string to convert
	 * @return string
	 */
    public static String convertNewlinesText(String input) {
        input = replace(input, "\n", "\r\n");
        input = replace(input, "<BR/>", "\r\n");
        input = replace(input, "<BR>", "\r\n");
        input = replace(input, "<br/>", "\r\n");
        input = replace(input, "<br>", "\r\n");
        return input;
    }
    
    public static String replace(
            String mainString,
            String oldString,
            String newString) {
        if (mainString == null)
            return null;
        int i = mainString.lastIndexOf(oldString);
        if (i < 0)
            return mainString;
        StringBuffer mainSb = new StringBuffer(mainString);
        while (i >= 0) {
            mainSb.replace(i, i + oldString.length(), newString);
            i = mainSb.toString().lastIndexOf(oldString, i - 1);
        }
        return mainSb.toString();
    }
    
    /**
	 * Check a string null or blank.
	 * 
	 * @param param
	 *            string to check
	 * @return boolean
	 */
    public static boolean nullOrBlank(String param) {
        return (param == null || param.trim().equals("") || param.trim().equals("null")) ? true : false;
    }
    public static String notNull(String param) {
        return param == null ? "" : param.trim();
    }
    /**
	 * Parse a string to int.
	 * 
	 * @param param
	 *            string to parse
	 * @return int value, on exception return 0.
	 */
    
    public static int parseInt(String param) {
        int i = 0;
        try {
            i = Integer.parseInt(param);
        } catch (Exception e) {
            i = (int) parseFloat(param);
        }
        return i;
    }
    public static long parseLong(String param) {
        long l = 0;
        try {
            l = Long.parseLong(param);
        } catch (Exception e) {
            l = (long) parseDouble(param);
        }
        return l;
    }
    public static float parseFloat(String param) {
        float f = 0;
        try {
            f = Float.parseFloat(param);
        } catch (Exception e) {
            //
        }
        return f;
    }
    public static double parseDouble(String param) {
        double d = 0;
        try {
            d = Double.parseDouble(param);
        } catch (Exception e) {
            //
        }
        return d;
    }
    
    /**
	 * Parse a string to boolean.
	 * 
	 * @param param
	 *            string to parse
	 * @return boolean value, if param begin with(1,y,Y,t,T) return true, on
	 *         exception return false.
	 */
    public static boolean parseBoolean(String param) {
        if (nullOrBlank(param))
            return false;
        switch (param.charAt(0)) {
            case '1' :
            case 'y' :
            case 'Y' :
            case 't' :
            case 'T' :
                return true;
        }
        return false;
    }
    
    /**
	 * Parse a Object to String.
	 * 
	 * @param param
	 *            string to parse
	 * @return boolean value, if param begin with(1,y,Y,t,T) return true, on
	 *         exception return false.
	 */
    public static String parseString(Object param) {
        return param == null ? "" : param.toString().trim();
    }
 
    
    /**
	 * @param ss
	 * @return return_type String
	 */
    public static String toChinese(String ss) {
        // 处理中文问题,实现编码转换
        if (ss != null) {
            try {
                String temp_p = ss;
                byte[] temp_t = temp_p.getBytes("ISO8859-1");
                ss = new String(temp_t);
            } catch (Exception e) {
                System.err.println("toChinese exception:" + e.getMessage());
                System.err.println("The String is:" + ss);
            }
        }
        return ss;
    }
    
    
    // 将型double数据转换为不待小数点得字符
    public static String caststring1(String tmpq) {
        String tmp1 = "";
        if(tmpq.indexOf("-")<=0) {
            int tt = tmpq.indexOf(".");
            if(tt>0)
                tmp1 = tmpq.substring(0,tt);
            else
                tmp1 = tmpq;
        }else {
            tmp1 = tmpq;
        }
        return tmp1;
    }
    
    /**
	 * Description: 如果给定number的长度小于给定的nLength，就在number前面补零 Created on 2005-09-30
	 * 
	 * @param number
	 *            键值
	 * @param nLength
	 *            需要设定的长度 return_type String
	 */
    public static String formatNumber(int number, int nLength) {
        return formatNumber(number, nLength, '0');
    }

     public static String formatNumber(long number, int nLength) {
        return formatNumber(number, nLength, '0');
    }
    
    /**
	 * Description: 如果给定number的长度小于给定的nLength，就在number前面补给定的字符 Created on
	 * 2005-09-30
	 * 
	 * @param number
	 *            键值
	 * @param nLength
	 *            需要设定的长度
	 * @param nChar
	 *            补的字符 return_type String
	 */
    public static String formatNumber(int number, int nLength, char nChar) {
        String s = String.valueOf(number);
        
        if (StringUtil.nullOrBlank(s)) s = "";
        
        StringBuffer buf = new StringBuffer();
        
        // 前面补给定的字符
        while((nLength - s.length()) > 0) {
            buf.append(nChar);
            nLength --;
        }
        
        return (buf.toString() + s) ;
    }

     public static String formatNumber(long number, int nLength, char nChar) {
        String s = String.valueOf(number);

        if (StringUtil.nullOrBlank(s)) s = "";

        StringBuffer buf = new StringBuffer();

        // 前面补给定的字符
        while((nLength - s.length()) > 0) {
            buf.append(nChar);
            nLength --;
        }

        return (buf.toString() + s) ;
    }
    /**
	 * Description: 如果给定number的长度小于给定的nLength，就在number前面补零 Created on 2005-09-30
	 * 
	 * @param s
	 *            键值
	 * @param nLength
	 *            需要设定的长度 return_type String
	 */
    public static String formatNumber(String s, int nLength) {
        return formatNumber(s, nLength, '0');
    }
    
    /**
	 * Description: 如果给定number的长度小于给定的nLength，就在number前面补给定的字符 Created on
	 * 2005-09-30
	 * 
	 * @param s
	 *            键值
	 * @param nLength
	 *            需要设定的长度
	 * @param nChar
	 *            补的字符 return_type String
	 */
    public static String formatNumber(String s, int nLength, char nChar) {
        if (StringUtil.nullOrBlank(s)) s = "";
        
        StringBuffer buf = new StringBuffer();
        
        // 前面补给定的字符
        while((nLength - s.length()) > 0) {
            buf.append(nChar);
            nLength --;
        }
        
        return (buf.toString() + s) ;
    }
    
    public static String[] parseIntoArray(String s, String s1, int i) {
        String as[] = new String[i];
        int j = 0;
        for(int l = 0; l < as.length; l++) {
            int k = s.indexOf(s1, j);
            if(k == -1)
                as[l] = null;
            else
                as[l] = s.substring(j, k);
            j = k + s1.length();
        }
        
        return as;
    }
    
 
    
    /**
	 * Description: 获得小数点的位数，如果小数点后面的部分以零结尾，就把后面的零都去除 Created on 2005-10-14
	 * 
	 * @param f
	 *            return_type int
	 */
    public static int getScale(float f) {
        return getScale(f, true);
    }
    
    /**
	 * Description: 获得小数点的位数 Created on 2005-10-14
	 * 
	 * @param f
	 *            需要处理的浮点数
	 * @param quChuLing
	 *            如果小数点后面的部分以零结尾，是否把后面的零去除 return_type int
	 */
    public static int getScale(float f, boolean quChuLing) {
        try {
            String fStr = String.valueOf(f);
            
            int indexOf = fStr.indexOf(".");
            if(indexOf >= 0) {
                String strss = fStr.substring(indexOf + 1, fStr.length());
                
                if(quChuLing) {
                    // 如果将后面的零去除
                    while(strss.endsWith("0"))
                        strss = strss.substring(0, strss.length() - 1);
                }
                
                // 小数点位数
                return strss.length();
            }
        } catch (Exception e) {
        }
        return 0;
    }
    
     public static String[] stringSplit(String s, String delm) {
       
         String ss[] = s.split(delm);
         return ss;
    }

     /**
 	 * @deprecated
 	 * replace old string with new String 
 	 * @param text
 	 * @param repl
 	 * @param with
 	 * @return
 	 */
 	public static String replaceAll(String text, String repl, String with) {
 		return replace(text, repl, with, -1);
 	}
 	
 	/**
 	 * replace first old string with new String
 	 * @param text
 	 * @param repl
 	 * @param with
 	 * @return
 	 */
 	public static String replaceFirst(String text, String repl, String with) {
 		return replace(text, repl, with, 1);
 	}
 	
 	/**
 	 * @deprecated user String.replaceAll()
 	 * @param text
 	 * @param repl
 	 * @param with
 	 * @param max
 	 * @return
 	 */
 	public static String replace(String text, String repl, String with, int max) {
 		if (text == null || repl == null || repl.length() == 0 || max == 0)
 			return text;
 		if (with == null)
 			with = "";
 		StringBuffer buf = new StringBuffer(text.length());
 		int start = 0;
 		int end = 0;
 		do {
 			if ((end = text.indexOf(repl, start)) == -1)
 				break;
 			buf.append(text.substring(start, end)).append(with);
 			start = end + repl.length();
 		} while (--max != 0);
 		buf.append(text.substring(start));
 		return buf.toString();
 	}
 	/**
 	 * check the string is null or empty
 	 * @param str
 	 * @return
 	 */
 	public static boolean isNullOrEmpty(String str) {
 		return (str == null || "".equals(str));
 	}
 	
 	/**
 	 * check the char is 0--9
 	 * @param c
 	 * @return
 	 */
 	public static boolean isNumber(char c) {
 		return (c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
 				|| c == '5' || c == '6' || c == '7' || c == '8' || c == '9');
 	}
     
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
