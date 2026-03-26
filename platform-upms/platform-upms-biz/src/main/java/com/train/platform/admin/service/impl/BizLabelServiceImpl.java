package com.train.platform.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.train.platform.admin.api.dto.LabelDTO;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.api.entity.BizLabelTagInfo;
import com.train.platform.admin.api.entity.BizLabelTask;
import com.train.platform.admin.api.vo.LabelVO;
import com.train.platform.admin.mapper.BizLabelTagInfoMapper;
import com.train.platform.admin.mapper.BizLabelTaskMapper;
import com.train.platform.admin.service.BizLabelService;
import com.train.platform.admin.mapper.BizLabelMapper;
import com.train.platform.admin.service.BizLabelTagInfoService;
import com.train.platform.common.core.exception.CheckedException;
import com.train.platform.common.core.util.R;
import lombok.AllArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.train.platform.common.core.constant.PlatformConstants.*;

/**
 * @author li
 * @description 针对表【aircas_label】的数据库操作Service实现
 * @createDate 2024-07-12 13:57:59
 */
@Service
@AllArgsConstructor
public class BizLabelServiceImpl extends ServiceImpl<BizLabelMapper, BizLabel>
		implements BizLabelService {

	SqlSessionTemplate sqlSessionTemplate;

	private BizLabelTagInfoService bizLabelTagInfoService;

	private BizLabelTaskMapper bizLabelTaskMapper;

	@Override
	public void insertBatches(List<BizLabel> bizLabels) {
		SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		try {
			BizLabelMapper mapper = session.getMapper(BizLabelMapper.class);
			for (int i = 0; i < bizLabels.size(); i++) {
				mapper.insert(bizLabels.get(i));

				if (i % 400 == 0 || i == bizLabels.size() - 1) {
					session.commit();
					session.clearCache();
				}
			}
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	@Override
	public void insertLabelVOBatches(List<LabelVO> labelVOS) {
		SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		try {
			BizLabelMapper mapper = session.getMapper(BizLabelMapper.class);
			for (int i = 0; i < labelVOS.size(); i++) {
				mapper.insertLabelVO(labelVOS.get(i));

				if (i % 400 == 0 || i == labelVOS.size() - 1) {
					session.commit();
					session.clearCache();
				}
			}
		} catch (Exception e) {
			session.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}

	@Override
	public void insertTagInfoBatches(List<BizLabelTagInfo> bizLabelTagInfos) {
		try(SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
			BizLabelTagInfoMapper mapper = session.getMapper(BizLabelTagInfoMapper.class);
			for (int i = 0; i < bizLabelTagInfos.size(); i++) {
				mapper.insert(bizLabelTagInfos.get(i));

				if ((i+1) % 400 == 0 || i == bizLabelTagInfos.size() - 1) {
					session.commit();
					session.clearCache();
				}
			}
		} catch (Exception e) {
			// 使用日志框架记录异常（例如SLF4J）
			throw new CheckedException("批量插入失败", e);  // 抛出自定义异常
		}
	}

	@Override
	public void updateBatches(List<BizLabel> bizLabels) {
		SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		try {
			BizLabelMapper mapper = session.getMapper(BizLabelMapper.class);
			for (int i = 0; i < bizLabels.size(); i++) {
				mapper.updateById(bizLabels.get(i));

				if (i % 400 == 0 || i == bizLabels.size() - 1) {
					session.commit();
//					session.clearCache();
					session.flushStatements(); // 强制刷新批处理语句[8](@ref)
				}
			}
		} catch (Exception e) {
			session.rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public IPage getByPage(Page page, BizLabel bizLabel) {
		return baseMapper.selectLabelPage(page, bizLabel);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveLabel(LabelDTO labelDTO) {
		int insert = saveOrUpdateLabel(labelDTO);

		if (insert > 0) {
			return R.ok(labelDTO.getLabelId());
		} else {
			return R.failed("标签保存失败");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R saveLabels(LabelDTO labelDTO) {
		if (labelDTO.getLabelType().equals(CLASSIFY_MODE)) {
			List<BizLabel> bizLabels = new ArrayList<>();
			List<BizLabelTagInfo> bizLabelTagInfos = new ArrayList<>();
			if (labelDTO.getFileId() != null) {
				List<Long> fileIds = Arrays.stream(labelDTO.getFileId().split(",")).map(s ->
						Long.parseLong(s.trim())
				).toList();

				if (!fileIds.isEmpty()) {
					for (Long fileId : fileIds) {
						BizLabel bizLabel = new BizLabel();
						BeanUtils.copyProperties(labelDTO, bizLabel);
						bizLabel.setFileId(fileId);
						bizLabels.add(bizLabel);
					}
					this.saveBatch(bizLabels);
				}
				if (!bizLabels.isEmpty()) {
					for (BizLabel bizLabel : bizLabels) {
						BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
						bizLabelTagInfo.setLabelId(bizLabel.getLabelId());
						bizLabelTagInfo.setTagId(Long.parseLong(bizLabel.getTagId()));
						bizLabelTagInfo.setTagInfo(labelDTO.getTagInfo());
						bizLabelTagInfos.add(bizLabelTagInfo);
					}
					bizLabelTagInfoService.saveBatch(bizLabelTagInfos);
				}
				return R.ok(bizLabels);
			} else {
				return R.failed("参数错误");
			}
		} else {
			return R.failed("批量标注目前仅支持图像分类");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R updateBizLabelById(LabelDTO labelDTO) {
		if (labelDTO.getLabelType().equals(CLASSIFY_MODE)) {
			List<BizLabel> bizLabels = new ArrayList<>();
			List<Long> labelIds = Arrays.stream(labelDTO.getLabelId().split(",")).map(s ->
					Long.parseLong(s.trim())
			).toList();

			for (Long labelId : labelIds) {
				BizLabel bizLabel = new BizLabel();
				BeanUtils.copyProperties(labelDTO, bizLabel);
				bizLabel.setLabelId(labelId);
				bizLabels.add(bizLabel);
			}
			this.updateBatchById(bizLabels);
		} else {
			int modify = saveOrUpdateLabel(labelDTO);

			if (modify > 0) {
				return R.ok(Long.parseLong(labelDTO.getLabelId()));
			} else {
				return R.failed("修改标签失败");
			}
		}
		return R.ok();
	}

	@Override
	public LabelVO getLabelById(Long id) {
		LabelVO labelVO = new LabelVO();
		BeanUtils.copyProperties(baseMapper.selectOne(Wrappers.lambdaQuery(BizLabel.class).eq(BizLabel::getLabelId, id)), labelVO);
		if (labelVO.getTagId() != null) {
			List<BizLabelTagInfo> bizLabelTagInfos = bizLabelTagInfoService.list(Wrappers.lambdaQuery(BizLabelTagInfo.class)
					.eq(BizLabelTagInfo::getLabelId, id));
			labelVO.setTagInfoList(bizLabelTagInfos);
		}
		return labelVO;
	}

	@Override
	public LabelVO getDetails(BizLabel query) {
		return baseMapper.getDetails(query);
	}

	@Override
	public R syncLabel(Long taskId, String labelType, String subLabelType) {
		List<BizLabel> bizLabels = baseMapper.selectList(Wrappers.lambdaQuery(BizLabel.class).eq(BizLabel::getTaskId, taskId));

		List<BizLabelTagInfo> bizLabelTagInfos = new ArrayList<>();
		if (labelType.equals(CLASSIFY_MODE)) {
			for (BizLabel bizLabel : bizLabels) {
				BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
				bizLabelTagInfo.setLabelId(bizLabel.getLabelId());
				bizLabelTagInfo.setTagId(Long.valueOf(bizLabel.getTagId()));
//				bizLabelTagInfo.setTagInfo(bizLabel.getTagInfo());
				bizLabelTagInfos.add(bizLabelTagInfo);
			}
		} else {
			String labelStr = TAG_ID;

			if (labelType.equals(DETECTION_MODE)) {
				labelStr = switch (subLabelType) {
					case DETECTION_LINE_MODE -> LINE_LABEL;
					case DETECTION_CIRCLE_MODE -> CIRCLE_LABEL;
					default -> BBOX_LABEL;
				};
			}

			for (BizLabel item : bizLabels) {
//				JSONArray jsonArray = JSON.parseArray(item.getTagInfo());
				JSONArray jsonArray = new JSONArray();
				if (!jsonArray.isEmpty()) {
					for (Object object : jsonArray) {
						JSONObject o = (JSONObject) object;
						BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
						bizLabelTagInfo.setLabelId(item.getLabelId());
						bizLabelTagInfo.setTagId(Long.valueOf(o.get(labelStr).toString()));
						bizLabelTagInfo.setTagInfo(object.toString());
						bizLabelTagInfos.add(bizLabelTagInfo);
					}
				}
			}
		}

		boolean res = bizLabelTagInfoService.saveBatch(bizLabelTagInfos);
		Map<String, Object> map = new HashMap<>();
		map.put("bizLabels", bizLabels.size());
		map.put("bizLabelTagInfos", bizLabelTagInfos.size());
		map.put("res", res);

		return R.ok(map);
	}

	@Override
	public R autoAsyncLabel() {

		List<BizLabelTask> list = bizLabelTaskMapper.selectList(null);
		list.forEach(bizLabelTask -> {
			syncLabel(bizLabelTask.getTaskId(), bizLabelTask.getLabelType(), bizLabelTask.getSubLabelType());
		});

		return R.ok("同步完成");
	}

	public int saveOrUpdateLabel(LabelDTO labelDTO) {

		String labelType = labelDTO.getLabelType();

		BizLabel bizLabel = new BizLabel();
		BeanUtils.copyProperties(labelDTO, bizLabel);
		if (labelDTO.getLabelId() != null && !labelDTO.getLabelId().isEmpty()) {
			bizLabel.setLabelId(Long.parseLong(labelDTO.getLabelId()));
		}
		bizLabel.setFileId(Long.parseLong(labelDTO.getFileId()));
		bizLabel.setTagId(labelDTO.getTagId());

		int modify = saveOrUpdateLabel(bizLabel);

		Long labelId = bizLabel.getLabelId();
		labelDTO.setLabelId(labelId.toString());

		//暂时取消存储tag_info表
		bizLabelTagInfoService.removeByLabelId(labelId);
		List<BizLabelTagInfo> bizLabelTagInfos = new ArrayList<>();

		if (labelType.equals(CLASSIFY_MODE)) {
			//判断tagId数量
			String[] splitTagId = labelDTO.getTagId().split(",");
			String[] splitTagInfo = labelDTO.getTagInfo().split(",");
			for (int  i = 0; i < splitTagId.length; i++) {
				BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
				bizLabelTagInfo.setLabelId(labelId);
				bizLabelTagInfo.setTagId(Long.valueOf(splitTagId[i]));
				bizLabelTagInfo.setTagInfo(splitTagInfo[i]);
				bizLabelTagInfos.add(bizLabelTagInfo);
			}
		} else {
			String labelStr = TAG_ID;

			if (labelType.equals(DETECTION_MODE)) {
				labelStr = switch (labelDTO.getSubLabelType()) {
					case DETECTION_LINE_MODE -> LINE_LABEL;
					case DETECTION_CIRCLE_MODE -> CIRCLE_LABEL;
					default -> BBOX_LABEL;
				};
			}
			List<Object> list = JSON.parseArray(labelDTO.getTagInfo());

			for (Object o : list) {
				JSONObject jsonObject = (JSONObject) o;
				BizLabelTagInfo bizLabelTagInfo = new BizLabelTagInfo();
				bizLabelTagInfo.setLabelId(bizLabel.getLabelId());
				bizLabelTagInfo.setTagId(jsonObject.getLong(labelStr));
				bizLabelTagInfo.setTagInfo(o.toString());
				bizLabelTagInfos.add(bizLabelTagInfo);
			}
		}
		bizLabelTagInfoService.saveBatch(bizLabelTagInfos);
		return modify;
	}

	public int saveOrUpdateLabel(BizLabel bizLabel) {
		if (bizLabel.getLabelId() == null) {
			bizLabel.setLabelId(IdUtil.getSnowflake(19, 19).nextId());
			return baseMapper.insert(bizLabel);
		} else {
			return baseMapper.updateById(bizLabel);
		}
	}
}




