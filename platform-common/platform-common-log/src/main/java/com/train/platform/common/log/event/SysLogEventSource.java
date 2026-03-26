package com.train.platform.common.log.event;

import com.train.platform.admin.api.entity.SysLog;
import lombok.Data;

/**
 * spring event log
 *
 * @author lee
 * @date 2024/8/11
 */
@Data
public class SysLogEventSource extends SysLog {

	/**
	 * 参数重写成object
	 */
	private Object body;

}
