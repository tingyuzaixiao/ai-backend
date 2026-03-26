package com.train.platform.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.dto.BizDatasetDTO;
import com.train.platform.admin.api.dto.DatasetImgDTO;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.DatasetVO;
import com.train.platform.admin.api.vo.FilePageVO;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.admin.mapper.BizBaseDatasetMapper;
import com.train.platform.admin.mapper.BizExperimentMapper;
import com.train.platform.admin.mapper.SysFileMapper;
import com.train.platform.admin.service.BizDatasetService;
import com.train.platform.admin.mapper.BizDatasetMapper;
import com.train.platform.admin.service.SysDeletedDirService;
import com.train.platform.common.core.util.FileUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.core.util.TimeId;
import com.train.platform.common.file.core.FileTemplate;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.train.platform.common.core.constant.CommonConstants.*;

/**
 * @author lee
 * @description 针对表【aircas_dataset】的数据库操作Service实现
 * @createDate 2024-05-23 14:24:59
 */
@Service
public class BizDatasetServiceImpl extends ServiceImpl<BizDatasetMapper, BizDataset> implements BizDatasetService {

	@Autowired
	FileTemplate fileTemplate;

	@Autowired
	BizBaseDatasetMapper bizBaseDatasetMapper;

	@Autowired
	SysFileMapper sysFileMapper;

	@Autowired
	private BizExperimentMapper bizExperimentMapper;

	@Autowired
	private SysDeletedDirService sysDeletedDirService;

	@SneakyThrows
	@Override
	public Long saveBizDataset(BizDatasetDTO bizDatasetDTO) {
		BizDataset newBizDataset = new BizDataset();
		BeanUtils.copyProperties(bizDatasetDTO, newBizDataset);
		if (bizDatasetDTO.getLabelJson() != null) {
			List<Object> jsonArray;
			jsonArray = new ArrayList<>();
			jsonArray.add(bizDatasetDTO.getLabelJson());
			newBizDataset.setLabelJson(JSONArray.toJSONString(jsonArray));
		}

		QueryWrapper<BizBaseDataset> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("base_dataset_id", bizDatasetDTO.getBaseDatasetId());
		BizBaseDataset bizBaseDataset = bizBaseDatasetMapper.selectOne(queryWrapper);

		Map<String, String> pathParams = new HashMap<>() {{
			put("type", DATASET_FOLDER);
			put("baseDatasetName", bizBaseDataset.getBaseDatasetName());
			put("datasetName", bizDatasetDTO.getDatasetName());
		}};

		if (bizDatasetDTO.getVersion() != null) {
			pathParams.put("datasetName", bizDatasetDTO.getDatasetName()+"_"+bizDatasetDTO.getVersion());
		}

		String datasetPath = FileUtils.getPath(pathParams);
		// 创建目录
		fileTemplate.createBucket(datasetPath);

		if (bizDatasetDTO.getInheritId() != null) {
			// 根据基础数据集ID + 版本号查询
			BizDataset inheritDataset = baseMapper.selectOne(Wrappers.query(new BizDataset()).eq("dataset_id", bizDatasetDTO.getInheritId()));
			newBizDataset.setImageIds(inheritDataset.getImageIds());
			newBizDataset.setCount(inheritDataset.getCount());
			newBizDataset.setSize(inheritDataset.getSize());
			newBizDataset.setFirstPicture(inheritDataset.getFirstPicture());
		}

		// 插入数据
		baseMapper.insert(newBizDataset);
		return newBizDataset.getDatasetId();
	}

	@Override
	public IPage<DatasetVO> getDatasetsPage(Page page, BizDataset bizDataset) {
		return baseMapper.getDatasetsPage(page, bizDataset);
	}

	@Override
	public FilePageVO getDatasetImgPage(DatasetImgDTO datasetImgDTO) {
		FilePageVO filePageVO = new FilePageVO();
		filePageVO.setPageNum(datasetImgDTO.getPageNum());
		filePageVO.setPageSize(datasetImgDTO.getPageSize());

		BizDataset byId = getById(datasetImgDTO.getDatasetId());
		if (byId == null) {
			filePageVO.setFileList(new ArrayList<>());
			return filePageVO;
		}
		String imageIds = byId.getImageIds();
		List<Long> likeIds = new ArrayList<>(), fileIds = new ArrayList<>();

		FileDTO fileDTO = new FileDTO();
		fileDTO.setBaseDatasetId(byId.getBaseDatasetId());

		if (datasetImgDTO.getOriginalName() != null && !datasetImgDTO.getOriginalName().isEmpty()) {
			fileDTO.setOriginal(datasetImgDTO.getOriginalName());
			List<SysFile> sysFiles = sysFileMapper.selectList(Wrappers.<SysFile>lambdaQuery().
					like(SysFile::getOriginal, fileDTO.getOriginal()).
					eq(SysFile::getBaseDatasetId, fileDTO.getBaseDatasetId()));
			likeIds = sysFiles.stream().map(SysFile::getId).toList();
		}

		if (datasetImgDTO.getLabelFileName() != null && !datasetImgDTO.getLabelFileName().isEmpty()) {
			fileDTO.setLabelFileName(datasetImgDTO.getLabelFileName());
		}

		if (imageIds != null && !imageIds.isEmpty()) {
			fileIds = JSONObject.parseArray(imageIds, Long.class);
			if (!fileIds.isEmpty()) {
				if (!likeIds.isEmpty()) {
					fileIds = fileIds.stream().filter(likeIds::contains).collect(Collectors.toList());
				}

				int fileSize = fileIds.size(), pageSize = datasetImgDTO.getPageSize(), pageNum = datasetImgDTO.getPageNum();
				filePageVO.setTotals(fileSize);
				if (fileSize > pageSize) {
					int startSize = (pageNum - 1) * pageSize;
					int endSize = pageSize > (fileSize - startSize) ? fileSize : (startSize + pageSize);
					fileIds = fileIds.subList(startSize, endSize);
				}
				fileDTO.setFileIds(fileIds);
				List<FileVO> sysFiles = sysFileMapper.selectFileVOList(fileDTO);
				filePageVO.setFileList(sysFiles);
			}
		} else {
			filePageVO.setFileList(new ArrayList<>());
		}
		return filePageVO;
	}

	@Override
	public void updateDatasetLabelProgress(BizDataset bizDataset) {
		// 更新数据集标注进度
		String labelJson = bizDataset.getLabelJson();
		if (labelJson != null) {
			// todo 调用算法服务器更新进度
		}
	}

	@Override
	public boolean updateById(BizDatasetDTO bizDatasetDTO) {
		if (bizDatasetDTO.getImageIds() != null && !bizDatasetDTO.getImageIds().isEmpty()) {
			String imageIds = bizDatasetDTO.getImageIds();
			List<Long> fileIds = JSONObject.parseArray(imageIds, Long.class);

			if (!fileIds.isEmpty()) {
				Long fileId = fileIds.get(fileIds.size() - 1);
				SysFile sysFile = sysFileMapper.selectOne(Wrappers.query(new SysFile()).eq("id", fileId));
				bizDatasetDTO.setFirstPicture(sysFile.getBucketName() + FileUtil.FILE_SEPARATOR + sysFile.getFileName());

				//根据IDs设置count、size
				Long fileSizeByFileIds = sysFileMapper.sumFileSizeByFileIds(fileIds);
				bizDatasetDTO.setSize(fileSizeByFileIds);
				bizDatasetDTO.setCount(fileIds.size());
			}
		}
		BizDataset bizDataset = new BizDataset();
		BeanUtils.copyProperties(bizDatasetDTO, bizDataset);
		if (bizDatasetDTO.getLabelJson() != null) {
			List<Object> jsonArray;
			String labelJson = bizDataset.getLabelJson();
			if (labelJson != null) {
				jsonArray = JSONObject.parseArray(labelJson);
				jsonArray.add(bizDatasetDTO.getLabelJson());
			} else {
				jsonArray = new ArrayList<>();
				jsonArray.add(bizDatasetDTO.getLabelJson());
			}
			bizDataset.setLabelJson(JSONArray.toJSONString(jsonArray));
		}

		return baseMapper.updateById(bizDataset) > 0;
	}

	@Override
	public String getLabelListById(Long datasetId) {
		return baseMapper.getLabelListById(datasetId);
	}

	@Override
	public R deleteById(Long id) {
		List<BizExperiment> bizExperiments = bizExperimentMapper.selectList(Wrappers.<BizExperiment>lambdaQuery().
				eq(BizExperiment::getDatasetId, id));
		if (!bizExperiments.isEmpty()) {
			BizDataset bizDataset = getById(id);
			List<String> expNames = bizExperiments.stream().map(BizExperiment::getExperimentName).toList();
			return R.failed(bizDataset.getDatasetName() + "已被实验" + String.join(",", expNames) + "引用，无法删除！");
		}

		DatasetVO byId = baseMapper.getDatasetVOById(id);
		SysDeletedDir sysDeletedDir = new SysDeletedDir();
		sysDeletedDir.setFunctionType(FILE_FUNCTION_DATASET);

		String prefixPath = DATASET_FOLDER + FileUtil.FILE_SEPARATOR + byId.getBaseDatasetName() + FileUtil.FILE_SEPARATOR;
		String newPath = byId.getDatasetName() + "-" + TimeId.getPkStrMMddHHmmss();
		fileTemplate.amendBucketName(prefixPath + byId.getDatasetName(), newPath);
		sysDeletedDir.setDirPath(prefixPath + newPath);
		sysDeletedDirService.save(sysDeletedDir);

		return R.ok(baseMapper.deleteById(id));
	}

}
