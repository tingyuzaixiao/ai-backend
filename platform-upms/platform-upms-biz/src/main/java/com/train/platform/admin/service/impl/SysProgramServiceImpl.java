package com.train.platform.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.entity.BizExperiment;
import com.train.platform.admin.api.entity.SysProgram;
import com.train.platform.admin.api.entity.SysRoleProgram;
import com.train.platform.admin.mapper.SysProgramMapper;
import com.train.platform.admin.mapper.service.SysProgramMapperService;
import com.train.platform.admin.service.BizExperimentService;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.admin.service.SysProgramService;
import com.train.platform.admin.service.SysRoleProgramService;
import com.train.platform.common.core.mlflow.MlflowWrapper;
import com.train.platform.common.core.util.ProgramLock;
import com.train.platform.common.file.core.FileTemplate;
import com.train.platform.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 项目管理
 *
 * @author Lee
 * @date 2024-05-17 17:18:42
 */
@Slf4j
@Service
public class SysProgramServiceImpl implements SysProgramService {
	@Autowired
	SysRoleProgramService sysRoleProgramService;

	@Autowired
	FileTemplate fileTemplate;

	@Autowired
	SysDeletedDirService sysDeletedDirService;

	@Autowired
	private SysProgramMapperService sysProgramMapperService;

	@Autowired
	private BizExperimentService bizExperimentService;

	@Autowired
	private MlflowWrapper mlflowWrapper;

	@Autowired
	private ProgramLock programLock;

	private static final int WAIT_TIMEOUT_SECONDS = 2;

	private SysProgramMapper getBaseMapper() {
		return sysProgramMapperService.getBaseMapper();
	}

	@Override
	public Integer updateById(SysProgram sysProgram) {
		SysProgram dbSysProgram = getDetails(sysProgram.getId());
		if (dbSysProgram == null) {
			throw new RuntimeException("program not exist");
		}

		updateCommonFields(sysProgram);

		ReentrantLock lock = programLock.getLock(dbSysProgram.getName());
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait name lock timeout");
			}

			dbSysProgram = getDetails(dbSysProgram.getId());
			if (dbSysProgram == null) {
				throw new RuntimeException("program not exist");
			}

			return getBaseMapper().updateById(sysProgram);
		} catch (Throwable throwable) {
			log.error("updateById catch exception: {}", throwable.getMessage());
			throw new RuntimeException(throwable);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
	}

	private void updateCommonFields(SysProgram sysProgram) {
		sysProgram.setName(null);
		sysProgram.setUpdateTime(LocalDateTime.now());
		sysProgram.setUpdateBy(SecurityUtils.getUser().getUsername());
	}

	@Override
	public SysProgram getDetails(Long id) {
		SysProgram query = new SysProgram();
		query.setId(id);
		query.setDelFlag("0");
		return sysProgramMapperService.getOne(Wrappers.query(query));
	}

	@Override
	public List<SysProgram> findProgramByRoleId(Long roleId) {
		return getBaseMapper().listProgramsByRoleId(roleId);
	}

	/**
	 * 注意：如果deleteExperiment很耗时，则将其改为异步
	 *
	 * @param id 主键
	 * @return 空
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeProgramById(Long id) {
		SysProgram program = getDetails(id);
		if (program == null) {
			return true;
		}

		ReentrantLock lock = programLock.getLock(program.getName());
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait name lock timeout");
			}

			if (!bizExperimentService.removeByProgramId(program.getProgramId())) {
				throw new RuntimeException("项目中有实验正在运行或正在停止，无法删除");
			}

			sysRoleProgramService.removeById(id);
			getBaseMapper().logicDelete(id, LocalDateTime.now());

			mlflowWrapper.deleteExperiment(program.getProgramId());
		} catch(Throwable throwable) {
			log.error("removeProgramById catch exception: {}", throwable.getMessage());
			throw new RuntimeException(throwable);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
		return true;
	}

	@Override
	public Boolean addRoleProgram(Long roleId, Long id) {
		SysRoleProgram sysRoleProgram = new SysRoleProgram();
		sysRoleProgram.setProgramId(id);
		sysRoleProgram.setRoleId(roleId);
		return sysRoleProgramService.save(sysRoleProgram);
	}

	@Override
	public IPage<List<SysProgram>> getProgramVOsWithRolePage(Page page, SysProgram sysProgram, List<Long> roles) {
		return getBaseMapper().getProgramVOsWithRolePage(page, sysProgram, roles);
	}

	@Override
	public SysProgram getSysProgram(Long id, String programId, String name) {
		LambdaQueryWrapper<SysProgram> lambdaWrapper = Wrappers.<SysProgram>lambdaQuery().
				eq(SysProgram::getDelFlag, "0");

		if (id != null) {
			lambdaWrapper = lambdaWrapper.eq(SysProgram::getId, id);
		} else if (StringUtils.hasText(programId)) {
			lambdaWrapper = lambdaWrapper.eq(SysProgram::getProgramId, programId);
		} else if (StringUtils.hasText(name)) {
			lambdaWrapper = lambdaWrapper.eq(SysProgram::getName, name);
		}
		return getBaseMapper().selectOne(lambdaWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SysProgram saveProgram(SysProgram sysProgram) {
		ReentrantLock lock = programLock.getLock(sysProgram.getName());
		boolean locked = false;
		try {
			locked = lock.tryLock(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			if (!locked) {
				throw new RuntimeException("wait name lock timeout");
			}

			checkProgramNameAvailable(sysProgram.getName());

			String programId = mlflowWrapper.createExperiment(sysProgram.getName());
			LocalDateTime now = LocalDateTime.now();
			sysProgram.setCreateTime(now);
			sysProgram.setUpdateTime(now);
			sysProgram.setProgramId(programId);
			String userName = SecurityUtils.getUser().getUsername();
			sysProgram.setCreateBy(userName);
			sysProgram.setUpdateBy(userName);

			getBaseMapper().insert(sysProgram);
		} catch (Throwable throwable) {
			log.error("saveProgram catch exception: {}", throwable.getMessage());
			throw new RuntimeException(throwable);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}

		SecurityUtils.getRoles().forEach(roleId -> addRoleProgram(roleId, sysProgram.getId()));
		return getDetails(sysProgram.getId());
	}

	@Override
	public Boolean checkProgramNameAvailable(String programName) {
		return !getBaseMapper().hasRepeatName(programName);
	}
}
