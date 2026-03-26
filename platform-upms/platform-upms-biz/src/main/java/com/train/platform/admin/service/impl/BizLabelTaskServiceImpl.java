package com.train.platform.admin.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.opencsv.CSVWriter;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.dto.LabelTaskDTO;
import com.train.platform.admin.api.entity.*;
import com.train.platform.admin.api.vo.*;
import com.train.platform.admin.mapper.*;
import com.train.platform.admin.service.BizLabelService;
import com.train.platform.admin.service.BizLabelTagService;
import com.train.platform.admin.service.BizLabelTaskService;
import com.train.platform.common.core.util.ObjectCopyUtils;
import com.train.platform.common.core.util.R;
import com.train.platform.common.file.core.FileProperties;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static com.train.platform.common.core.constant.CommonConstants.*;
import static com.train.platform.common.core.constant.PlatformConstants.*;

/**
 * @author li
 * @description 针对表【aircas_label_task(标注任务表)】的数据库操作Service实现
 * @createDate 2024-09-12 16:40:34
 */
@Service
@AllArgsConstructor
public class BizLabelTaskServiceImpl extends ServiceImpl<BizLabelTaskMapper, BizLabelTask>
		implements BizLabelTaskService {

	private final BizLabelTagMapper bizLabelTagMapper;

	private final BizLabelMapper bizLabelMapper;

	private final BizLabelService bizLabelService;

	private final BizLabelTagInfoMapper bizLabelTagInfoMapper;

	private final BizLabelTagService bizLabelTagService;

	private final SysFileMapper sysFileMapper;

	private final FileProperties fileProperties;

	@Override
	public IPage getLabelTaskByPage(Page page, BizLabelTask bizLabelTask) {
		return baseMapper.getLabelTaskByPage(page, bizLabelTask);
	}

	@Override
	public IPage getLabelTaskByPageNew(Page page, BizLabelTask bizLabelTask) {
		IPage<BaseDatasetVO> baseDatasetPageVO = baseMapper.getLabelTaskByPageNew(page, bizLabelTask);
		List<BaseDatasetVO> datasetVOList = baseDatasetPageVO.getRecords();

		if (datasetVOList == null) {
			return new Page<BaseDatasetLabelTaskVO>();
		}

		List<Long> baseDatasetIds = datasetVOList.stream().map(BaseDatasetVO::getBaseDatasetId).collect(Collectors.toList());
		List<BaseDatasetLabelTaskVO> result = new ArrayList<>();
		if (!baseDatasetIds.isEmpty()) {
			List<LabelTaskListVO> bizLabelTasks = baseMapper.selectLabelTaskByIds(bizLabelTask, baseDatasetIds);
			Map<Long, List<LabelTaskListVO>> childrenMap = bizLabelTasks.stream()
					.collect(Collectors.groupingBy(LabelTaskListVO::getBaseDatasetId));

			result = datasetVOList.stream().map(fileVO -> {
				BaseDatasetLabelTaskVO item = new BaseDatasetLabelTaskVO();
				BeanUtils.copyProperties(fileVO, item);
				item.setLabelTaskList(childrenMap.getOrDefault(fileVO.getBaseDatasetId(), Collections.emptyList()));
				return item;
			}).collect(Collectors.toList());
		}
		Page<BaseDatasetLabelTaskVO> fileVOPage = new Page<>();
		fileVOPage.setRecords(result);
		fileVOPage.setTotal(baseDatasetPageVO.getTotal());
		return fileVOPage;
	}

	@Override
	public LabelTaskVO getLabelTaskById(Long taskId) {
		LabelTaskVO labelTaskVO = baseMapper.selectLabelTaskById(taskId);

		//上线时逻辑修改为
		List<BizLabelTag> bizLabelTags = bizLabelTagMapper
				.selectList(Wrappers.<BizLabelTag>lambdaQuery()
						.eq(BizLabelTag::getTaskId, taskId));
		if (bizLabelTags != null && !bizLabelTags.isEmpty()) {
			labelTaskVO.setTagList(bizLabelTags);
		}

		return labelTaskVO;
	}

	@Override
	public R saveLabelTask(LabelTaskDTO labelTaskDTO) {
		if (labelTaskDTO.getTagIds().equals("[]")) {
			labelTaskDTO.setTagIds(null);
		}

		if (labelTaskDTO.getGroupId() != null) {
			List<BizLabelTag> bizLabelTags = bizLabelTagMapper.selectLabelTagByGroupId(labelTaskDTO.getGroupId());

			BizLabelTask bizLabelTask = new BizLabelTask();
			BeanUtils.copyProperties(labelTaskDTO, bizLabelTask);
			int insert = baseMapper.insert(bizLabelTask);

			bizLabelTags.forEach(bizLabelTag -> {
				bizLabelTag.setTagId(null);
				bizLabelTag.setGroupId(null);
				bizLabelTag.setTaskId(bizLabelTask.getTaskId());
			});
			bizLabelTagService.saveBatch(bizLabelTags);

			List<BizLabelTag> labelTaskTagIds = bizLabelTagService.list(Wrappers.lambdaQuery(BizLabelTag.class).eq(BizLabelTag::getTaskId, bizLabelTask.getTaskId()));
			List<Long> bizLabelTagIds = labelTaskTagIds.stream().map(BizLabelTag::getTagId).toList();
			bizLabelTask.setTagIds(JSONArray.toJSONString(bizLabelTagIds));
			baseMapper.updateById(bizLabelTask);

			if (insert > 0) {
				return R.ok(bizLabelTask.getTaskId());
			} else {
				return R.failed("保存失败");
			}
		} else {
			BizLabelTask bizLabelTask = new BizLabelTask();
			BeanUtils.copyProperties(labelTaskDTO, bizLabelTask);
			int insert = baseMapper.insert(bizLabelTask);
			if (insert > 0) {
				return R.ok(bizLabelTask.getTaskId());
			} else {
				return R.failed("保存失败");
			}
		}
	}

	@Override
	public Long saveLabelTask(BizLabelTask bizLabelTask) {
		baseMapper.insert(bizLabelTask);
		return bizLabelTask.getTaskId();
	}

	@Override
	public R removeLabelTaskByIds(List<Long> taskIds) {
		if (taskIds != null) {
			baseMapper.deleteBatchIds(taskIds);
			bizLabelMapper.delete(Wrappers.lambdaQuery(BizLabel.class).in(BizLabel::getTaskId, taskIds));
			bizLabelTagMapper.delete(Wrappers.lambdaQuery(BizLabelTag.class).in(BizLabelTag::getTaskId, taskIds));

			List<Long> labelIds = bizLabelMapper.selectLabelIdsByTaskIds(taskIds);

			if (labelIds != null && !labelIds.isEmpty()) {
				bizLabelTagInfoMapper.delete(Wrappers.lambdaQuery(BizLabelTagInfo.class).in(BizLabelTagInfo::getLabelId, labelIds));
			}
			return R.ok("删除成功");
		} else {
			return R.failed("删除失败");
		}


	}

	@Override
	public R updateLabelTaskById(LabelTaskDTO labelTaskDTO) {

		BizLabelTask task = baseMapper.selectById(labelTaskDTO.getTaskId());
		if (task == null) {
			return R.failed("标注任务不存在");
		}

		if (labelTaskDTO.getTagIds() != null && labelTaskDTO.getTagIds().equals("[]")) {
			labelTaskDTO.setTagIds(null);
		}

		String oldTagIds = task.getTagIds();
		String newTagIds = labelTaskDTO.getTagIds();
		List<Long> oldList = JSONArray.parseArray(oldTagIds, Long.class);
		Set<Long> oldSet = oldList == null ? new HashSet<>() : new HashSet<>(oldList);
		List<Long> newList = JSONArray.parseArray(newTagIds, Long.class);
		Set<Long> newSet = newList == null ? new HashSet<>() : new HashSet<>(newList);
		Set<Long> differenceTagIds = Sets.difference(oldSet, newSet);

		if (!newSet.containsAll(oldSet) && (newList == null || oldList == null || newList.size() < oldList.size())) {
			//如果修改（删除）了标签
			if (newTagIds == null || newTagIds.isEmpty()) {
				bizLabelMapper.delete(Wrappers.lambdaQuery(BizLabel.class).eq(BizLabel::getTaskId, labelTaskDTO.getTaskId()));
			} else {
				//label修改、taginfo删除
				List<BizLabel> deletedLabels = new ArrayList<>();
				List<BizLabel> updateBizLabels = new ArrayList<>();
				for (Long item : differenceTagIds) {
					List<BizLabel> bizLabels = bizLabelMapper.selectList(Wrappers.lambdaQuery(BizLabel.class)
							.eq(BizLabel::getTaskId, labelTaskDTO.getTaskId())
							.like(BizLabel::getTagId, item));
					for (BizLabel bizLabel : bizLabels) {
						String[] split = bizLabel.getTagId().split(",");
						List<String> tagIdStr = new ArrayList<>(Arrays.asList(split));
						if (tagIdStr.size() == 1) {
							deletedLabels.add(bizLabel);
						} else {
							//检测删除tag_info
//							if (labelTaskDTO.getLabelType().equals(DETECTION_MODE) && bizLabel.getTagId().contains(item.toString())) {
//								//删除tagInfo
//								String tagInfo = bizLabel.getTagInfo();
//								JSONArray objects = JSONArray.parseArray(tagInfo);
//								List<LabelDetection> list = objects.toJavaList(LabelDetection.class);
//								list.forEach(ite -> {
//									if (bizLabel.getTagId().contains(ite.getBbox_label().toString())) {
//										list.remove(ite);
//									}
//								});
//								bizLabel.setTagInfo(JSONArray.toJSONString(list));
//							}

							String[] labelTagIdSplit = bizLabel.getTagId().split(",");
							List<String> labelTagIdStr = new ArrayList<>(Arrays.asList(labelTagIdSplit));
							differenceTagIds.forEach(tagId -> {
								labelTagIdStr.remove(tagId.toString());
							});
							bizLabel.setTagId(StringUtils.join(labelTagIdStr, ","));
							updateBizLabels.add(bizLabel);
						}
						bizLabelTagInfoMapper.delete(Wrappers.lambdaQuery(BizLabelTagInfo.class)
								.eq(BizLabelTagInfo::getLabelId, bizLabel.getLabelId())
								.in(BizLabelTagInfo::getTagId, differenceTagIds));
					}
				}

				if (!deletedLabels.isEmpty()) {
					bizLabelService.removeBatchByIds(deletedLabels);
				}
				if (!updateBizLabels.isEmpty()) {
					bizLabelService.updateBatchById(updateBizLabels);
				}
			}
			//删除相关联的tagId
			bizLabelTagMapper.delete(Wrappers.lambdaQuery(BizLabelTag.class).in(BizLabelTag::getTagId, differenceTagIds));
		}

		if (labelTaskDTO.getGroupId() != null) {
			List<BizLabelTag> bizLabelTags = bizLabelTagMapper.selectLabelTagByGroupId(labelTaskDTO.getGroupId());
			bizLabelTags.forEach(bizLabelTag -> {
				bizLabelTag.setTagId(null);
				bizLabelTag.setGroupId(null);
				bizLabelTag.setTaskId(labelTaskDTO.getTaskId());
			});
			bizLabelTagService.saveBatch(bizLabelTags);
			List<Long> bizLabelTagIds = new ArrayList<>(bizLabelTags.stream().map(BizLabelTag::getTagId).toList());
			if (oldList != null) {
				bizLabelTagIds.addAll(oldSet);
			}
			labelTaskDTO.setTagIds(JSONArray.toJSONString(bizLabelTagIds));
		}

		BizLabelTask bizLabelTask = new BizLabelTask();
		BeanUtils.copyProperties(labelTaskDTO, bizLabelTask);
		baseMapper.updateById(bizLabelTask);

		return R.ok(bizLabelTask);
	}

	@Override
	public List<LabelFileVO> exportLabelTask(LabelTaskDTO bizLabelTask) {
		return baseMapper.selectExportLabelTask(bizLabelTask);
	}

	@Override
	public IPage getLabeledTotal(Page page, FileDTO file) {
		return sysFileMapper.selectNewSysFilesLabeledPage(page, file);
	}

	@Override
	public void exportLabelsToCsv(Writer writer, List<LabelFileVO> labelFileVOS) {
		try (CSVWriter csvWriter = new CSVWriter(writer)) {
			// 写入UTF-8 BOM头（解决Excel中文乱码）
			writer.write("\uFEFF");

			// 写入表头
			csvWriter.writeNext(new String[]{"labelId", "fileId", "fileName", "original", "bucketName", "md5", "type", "tagId", "tagInfo", "createBy", "createTime"});

			// 写入数据行
			labelFileVOS.parallelStream().forEach(fileVO -> {
				List<LabelTagInfoVO> labelTagInfoVOS = fileVO.getTagInfoList();
				JSONArray jsonArray = new JSONArray();
				if (labelTagInfoVOS != null && !labelTagInfoVOS.isEmpty()) {
					labelTagInfoVOS.forEach(labelTagInfoVO -> {
						JSONObject jsonObject = JSONObject.parseObject(labelTagInfoVO.getTagInfo());
						jsonArray.add(jsonObject);
					});
				}
				csvWriter.writeNext(new String[]{
						fileVO.getLabelId().toString(),
						fileVO.getFileId().toString(),
						fileVO.getFileName(),
						fileVO.getOriginal(),
						fileVO.getBucketName(),
						fileVO.getMd5(),
						fileVO.getType(),
						fileVO.getTagId() == null ? "" : fileVO.getTagId(),
						jsonArray.toJSONString(),
						fileVO.getCreateBy(),
						fileVO.getCreateTime().toString()
				});
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void exportLabelsToCsv(Writer writer, LabelTaskDTO bizLabelTask) {
		exportLabelsToCsv(writer, getLabelFileVOS(bizLabelTask));
	}

	@Override
	public List<String> getJsonLabels(Long baseDatasetId, Long taskId) {
		BizLabelTask query = new BizLabelTask();
		query.setTaskId(taskId);
		query.setBaseDatasetId(baseDatasetId);
		List<LabelTaskListVO> labelTaskListVOs = baseMapper.selectLabelTaskByIds(query, null);
		if (labelTaskListVOs == null || labelTaskListVOs.isEmpty()) {
			return null;
		}
		LabelTaskListVO labelTaskListVO = labelTaskListVOs.get(0);

		LabelTaskDTO labelTaskDTO = new LabelTaskDTO();
		ObjectCopyUtils.copyProperties(labelTaskListVO, labelTaskDTO);
		String content = exportLabelsToJSON(labelTaskDTO);

		return List.of(labelTaskDTO.getTaskName(), content);
	}

	@Override
	public String exportLabelsToJSON(LabelTaskDTO bizLabelTask) {
		Map<String, Object> labelJson = new LinkedHashMap<>();
		Map<String, Object> metainfo = new HashMap<>();

		List<LabelFileVO> labelFileVOS = getLabelFileVOS(bizLabelTask);
		if (labelFileVOS == null || labelFileVOS.isEmpty()) {
			return null;
		}

		Map<String, String> url = new HashMap<>();
		url.put("external", fileProperties.getLocal().getExternalNetwork());
		url.put("local", fileProperties.getLocal().getLocalAreaNetwork());
		url.put("dataset", "dataset" + FileUtil.FILE_SEPARATOR + bizLabelTask.getBaseDatasetName() + FileUtil.FILE_SEPARATOR + BASE_FOLDER);
		metainfo.put("base_path", url);

		Map<Long, Integer> tagCoordinate = new HashMap<>();
		List<BizLabelTag> bizLabelTags = bizLabelTagMapper.selectList(Wrappers.<BizLabelTag>lambdaQuery().eq(BizLabelTag::getTaskId, bizLabelTask.getTaskId()));
		if (bizLabelTags != null && !bizLabelTags.isEmpty()) {
			Map<String, Integer> tags = new HashMap<>();

			bizLabelTags.forEach((item) -> {
				tags.put(item.getTagName(), tags.size());
				tagCoordinate.put(item.getTagId(), tags.size() - 1);
			});
			metainfo.put(CLASSES_INDICES, tags);
		}

		//适配了分类、检测、分割导出
		if (bizLabelTask.getLabelType().equals(CLASSIFY_MODE)) {
			metainfo.put(TASK_TYPE, CLASSIFY_MODE_STR);
			labelJson.put(METAINFO, metainfo);

			List<Map<String, Object>> dataList = new ArrayList<>();

			if (!labelFileVOS.isEmpty()) {
				labelFileVOS.parallelStream().forEach(item -> {
					Map<String, Object> dataItem = new HashMap<>();
					dataItem.put(IMG_PATH, (item.getBucketName() + FileUtil.FILE_SEPARATOR + item.getFileName()).split("base")[1]);
					List<Integer> tagIds = new ArrayList<>();
					item.getTagInfoList().forEach(tagInfoVO -> {
						tagIds.add(tagCoordinate.get(Long.parseLong(tagInfoVO.getTagId().toString())));
					});
					dataItem.put(IMG_LABEL, StringUtils.join(tagIds, ","));
					dataList.add(dataItem);
				});
				labelJson.put(DATA_LIST, dataList);
			}
		} else if (bizLabelTask.getLabelType().equals(DETECTION_MODE)) {
			metainfo.put(TASK_TYPE, DETECTION_MODE_STR);
			String labelTagStr = "";
			if (bizLabelTask.getSubLabelType() != null) {
				if (bizLabelTask.getSubLabelType().equals(DETECTION_RECTANGLE_MODE)) {
					metainfo.put(LABEL_TYPE, DETECTION_RECTANGLE_MODE_STR);
					labelTagStr = BBOX_LABEL;
				} else if (bizLabelTask.getSubLabelType().equals(DETECTION_LINE_MODE)) {
					metainfo.put(LABEL_TYPE, DETECTION_LINE_MODE_STR);
					labelTagStr = LINE_LABEL;
				} else {
					metainfo.put(LABEL_TYPE, DETECTION_CIRCLE_MODE_STR);
					labelTagStr = CIRCLE_LABEL;
				}
			}
			labelJson.put(METAINFO, metainfo);
			List<Map<String, Object>> dataList = new ArrayList<>();
			if (!labelFileVOS.isEmpty()) {
				final String labelStr = labelTagStr;
				labelFileVOS.forEach(item -> {
					Map<String, Object> dataItem = new LinkedHashMap<>();
					dataItem.put(FILE_ID, item.getFileId());
					dataItem.put(LABEL_ID, item.getLabelId());
					dataItem.put(IMG_PATH, (item.getBucketName() + FileUtil.FILE_SEPARATOR + item.getFileName()).split("base")[1]);
					dataItem.put("height", item.getHeight());
					dataItem.put("width", item.getWidth());
					List<LabelTagInfoVO> tagInfoList = item.getTagInfoList();
					dataItem.put("objects", tagInfoList.size());
					JSONArray jsonArray = new JSONArray();
					if (!tagInfoList.isEmpty()) {
						tagInfoList.forEach(tagInfo -> {
							JSONObject parse = JSONObject.parseObject(tagInfo.getTagInfo());
							parse.put(labelStr, tagCoordinate.get(Long.parseLong(parse.getString(labelStr))));
							jsonArray.add(parse);
						});
					}
					dataItem.put(INSTANCES, jsonArray);
					dataList.add(dataItem);
				});
				labelJson.put(DATA_LIST, dataList);
			}
		} else {
			//分割-调用RLEProcessor生成mask并获取mask路径
			metainfo.put(TASK_TYPE, SEGMENTATION_MODE_STR);
			labelJson.put(METAINFO, metainfo);
			List<Map<String, Object>> dataList = new ArrayList<>();
			if (!labelFileVOS.isEmpty()) {
				labelFileVOS.forEach(item -> {
					Map<String, Object> dataItem = new LinkedHashMap<>();
					String bucketName = item.getBucketName();
					String imgPath = bucketName + FileUtil.FILE_SEPARATOR + item.getFileName();
					dataItem.put(FILE_ID, item.getFileId());
					dataItem.put(LABEL_ID, item.getLabelId());
					dataItem.put(IMG_PATH, imgPath);
					List<LabelTagInfoVO> tagInfoList = item.getTagInfoList();

					if (!tagInfoList.isEmpty()) {
						dataItem.put(MASK_PATH, replaceFirstOccurrence(imgPath, IMG_FOLDER, MASK_FOLDER));
					}

					dataItem.put("height", item.getHeight());
					dataItem.put("width", item.getWidth());
					dataItem.put("objects", tagInfoList.size());
//					dataItem.put("instances", objects);
					dataList.add(dataItem);
				});
				labelJson.put(DATA_LIST, dataList);
			}
		}

		return JSON.toJSONString(labelJson, SerializerFeature.PrettyFormat);
	}

	public List<LabelFileVO> getLabelFileVOS(LabelTaskDTO bizLabelTask) {
		List<LabelFileVO> labelFileVOS = exportLabelTask(bizLabelTask);
		if (labelFileVOS == null || labelFileVOS.isEmpty()) {
			return null;
		}
		List<Long> labelIds = labelFileVOS.stream().map(LabelFileVO::getLabelId).toList();
		List<LabelTagInfoVO> labelTagInfoVOS = bizLabelTagInfoMapper.selectByLabelIds(labelIds);
		if (labelTagInfoVOS != null && !labelTagInfoVOS.isEmpty()) {
			Map<Long, List<LabelTagInfoVO>> childrenMap = labelTagInfoVOS.stream()
					.collect(Collectors.groupingBy(LabelTagInfoVO::getLabelId));
			labelFileVOS.forEach(item -> {
				item.setTagInfoList(childrenMap.getOrDefault(item.getLabelId(), Collections.emptyList()));
			});
		}
		return labelFileVOS;
	}

	public static String replaceFirstOccurrence(String original, String target, String replacement) {
		int index = original.indexOf(target);
		if (index == -1) {
			return original; // 未找到直接返回原字符串
		}
		return original.substring(0, index)
				+ replacement
				+ original.substring(index + target.length());
	}
}




