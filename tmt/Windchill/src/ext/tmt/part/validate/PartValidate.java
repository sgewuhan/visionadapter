package ext.tmt.part.validate;

import java.util.Locale;

import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.org.WTPrincipal;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.ui.validation.DefaultUIComponentValidator;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationResult;
import com.ptc.core.ui.validation.UIValidationResultSet;
import com.ptc.core.ui.validation.UIValidationStatus;

@SuppressWarnings("all")
public class PartValidate extends DefaultUIComponentValidator{
	/**
	 * 处理单个Action的的验证
	 * @param UIValidationKey 			要验证的action
	 * @param UIValidationCriteria      验证标志实体类
	 * @param Locale                    本地化的
	 * 注册命令
	 * xconfmanager -s wt.services/svc/default/com.ptc.core.ui.validation.UIComponentValidator/standardReport(要验证的菜单名)/null/0= ext.tmt.part.validate.PartValidate/duplicate -t codebase/service.properties –p
	 * 
	 */
	@Override
	public UIValidationResultSet performFullPreValidation(UIValidationKey actionKey,
			UIValidationCriteria criteria, Locale locale) throws WTException {
		//获取操作对象
		Persistable pbo =criteria.getContextObject().getObject();
		//获得参与者
		WTPrincipal wtp = SessionHelper.manager.getPrincipal();
		//获得验证结果
		UIValidationResultSet rs = new UIValidationResultSet();
		//判断参与者是不是管理员
		if("Administrator".equals(wtp.getName())){
			//显示菜单
			rs.addResult(new UIValidationResult(actionKey, UIValidationStatus.ENABLED));
		}else{
			//隐藏菜单
			rs.addResult(new UIValidationResult(actionKey, UIValidationStatus.HIDDEN));
		}
		return rs;
	}

}
