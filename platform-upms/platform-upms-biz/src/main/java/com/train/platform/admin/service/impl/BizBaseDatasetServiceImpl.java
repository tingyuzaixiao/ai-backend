package com.train.platform.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.BaseDatasetVO;
import com.train.platform.admin.api.vo.LabelTaskListVO;
import com.train.platform.admin.mapper.*;
import com.train.platform.admin.service.BizBaseDatasetService;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.file.core.FileTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.train.platform.common.core.constant.CommonConstants.*;
import static com.train.platform.common.core.constant.PlatformConstants.*;

/**
 * @author lee
 * @description 针对表【aircas_base_dataset】的数据库操作Service实现
 * @createDate 2024-05-23 14:20:24
 */
@Service
public class BizBaseDatasetServiceImpl extends ServiceImpl<BizBaseDatasetMapper, BizBaseDataset>
		implements BizBaseDatasetService {

	@Autowired
	private FileTemplate fileTemplate;

	@Autowired
	private BizDatasetMapper bizDatasetMapper;

	@Autowired
	private BizExperimentMapper bizExperimentMapper;

	@Autowired
	private SysDeletedDirService sysDeletedDirService;

	@Autowired
	private BizLabelTaskMapper bizLabelTaskMapper;

	@Autowired
	private SysFileMapper sysFileMapper;

	@Override
	public Long saveBaseDataset(BizBaseDataset bizBaseDataset) {
		Map<String, String> pathParams = new HashMap<>() {{
			put("type", DATASET_FOLDER);
			put("baseDatasetName", bizBaseDataset.getBaseDatasetName());
		}};
		String path = FileUtils.getPath(pathParams);
		fileTemplate.createBucket(path + FileUtil.FILE_SEPARATOR + IMG_FOLDER);
		baseMapper.insertBizBaseDataset(bizBaseDataset);
		return bizBaseDataset.getBaseDatasetId();
	}

	@Override
	public IPage<List<BaseDatasetVO>> getBaseDatasetVOsPage(Page page, BizBaseDataset bizBaseDataset) {
		return baseMapper.getBaseDatasetVOsPage(page, bizBaseDataset);
	}

	@Override
	public List<BaseDatasetVO> listGroup(BizBaseDataset bizBaseDataset) {
		return baseMapper.selectListGroup(bizBaseDataset);
	}

	@Override
	public List<Long> getImageIds(Long id) {
		return baseMapper.selectImageIds(id);
	}

	@Override
	public int updateBaseDataset(BizBaseDataset bizBaseDataset) {
		int updateBaseDataset = baseMapper.updateById(bizBaseDataset);
		int updateDataset = bizDatasetMapper.updateDatasetByBaseDataset(bizBaseDataset);
		return updateDataset + updateBaseDataset;
	}

	@Override
	public String getLabelListById(Long datasetId) {
		return baseMapper.getLabelListById(datasetId);
	}

	@Override
	public List<Long> getFilesByBaseDatasetId(Long baseDatasetId) {
		return baseMapper.getFilesByBaseDatasetId(baseDatasetId);
	}

	@Override
	public R removeBaseDatasetById(Long id) {
		//删除关联数据集
		BizBaseDataset byId = getById(id);
		List<BizDataset> datasets = bizDatasetMapper.selectList(Wrappers.lambdaQuery(BizDataset.class).eq(BizDataset::getBaseDatasetId, id));

		for (BizDataset dataset : datasets) {
			List<BizExperiment> bizExperiments = bizExperimentMapper.selectList(Wrappers.<BizExperiment>lambdaQuery().
					eq(BizExperiment::getDatasetId, dataset.getDatasetId()));
			if (!bizExperiments.isEmpty()) {
				List<String> expNames = bizExperiments.stream().map(BizExperiment::getExperimentName).toList();
				return R.failed(byId.getBaseDatasetName() + "中" + dataset.getDatasetName() + "已被实验" + String.join(",", expNames) + "引用，无法删除！");
			}
		}

		//判断是否正在标注
		List<LabelTaskListVO> bizLabelTasks = bizLabelTaskMapper.selectLabelTaskByBaseDatasetId(id);
		if (!bizLabelTasks.isEmpty()) {
			List<String> taskNames = bizLabelTasks.stream().map(LabelTaskListVO::getTaskName).toList();
			return R.failed("基础数据集" + byId.getBaseDatasetName() + "正在 " + String.join(",", taskNames) + "中标注，无法删除！");
		}

		//将目录加入待删除目录表
		SysDeletedDir sysDeletedDir = new SysDeletedDir();
		sysDeletedDir.setDirPath(byId.getBaseDatasetName());
		sysDeletedDir.setFunctionType(FILE_FUNCTION_DATASET);
		sysDeletedDirService.saveDeleteDir(sysDeletedDir);

		//删除文件
		sysFileMapper.delete(Wrappers.<SysFile>lambdaQuery().eq(SysFile::getBaseDatasetId, id));
		return R.ok(baseMapper.deleteById(id));
	}

	@Override
	public R getState(BizBaseDataset bizBaseDataset) {
		BizBaseDataset dbBaseDataset = baseMapper.selectById(bizBaseDataset);
		if (dbBaseDataset != null) {
			switch (dbBaseDataset.getState()) {
				case IMPORTING -> {
					return R.failed("基础数据集" + dbBaseDataset.getBaseDatasetName() + "，正在导入中,请稍后再试！");
				}
				case IMPORT_PARSING -> {
					return R.failed("基础数据集" + dbBaseDataset.getBaseDatasetName() + "，正在解析中,请稍后再试！");
				}
				case NOT_IMPORTED, IMPORTED, IMPORT_FAILED -> {
					dbBaseDataset.setState(IMPORTING);
					baseMapper.updateById(dbBaseDataset);
				}
			}

			return R.ok(dbBaseDataset);
		} else {
			return R.failed("基础数据集不存在！");
		}
	}

	@Override
	public Boolean checkMd5(Long baseDatasetId, String md5) {
		return !sysFileMapper.selectList(Wrappers.<SysFile>lambdaQuery().eq(SysFile::getBaseDatasetId, baseDatasetId).eq(SysFile::getMd5, md5)).isEmpty();
	}
}
