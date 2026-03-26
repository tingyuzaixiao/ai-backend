package com.train.platform.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.train.platform.admin.api.dto.FileDTO;
import com.train.platform.admin.api.entity.BizLabel;
import com.train.platform.admin.api.entity.SysFile;
import com.train.platform.admin.api.vo.FileVO;
import com.train.platform.admin.api.vo.LabelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件管理
 *
 * @author Luckly
 * @date 2024-06-18 17:18:42
 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {

    List<SysFile> selectListInFileIds(List<Long> list);

	Long sumFileSizeByFileIds(List<Long> list);

	List<SysFile> selectListInFileIdStr(@Param("fileIdStr") String fileIdStr);

	IPage<FileVO> selectSysFilesPage(Page page, @Param("query") FileDTO file);

//	List<FileVO> selectNewSysFilesPage(@Param("query") FileDTO file, @Param("ids")List<Long> ids);
	List<FileVO> selectNewSysFilesPage0512(@Param("query") FileDTO file, @Param("ids")List<Long> ids);

	List<FileVO> selectFileVOList(@Param("query") FileDTO file);

	IPage<FileVO> selectSysFilesLabeledPage(Page page, @Param("query") FileDTO file);
	IPage<FileVO> selectSysFilesLabeledIdsPage0721(Page page, @Param("query") FileDTO file);
	IPage<FileVO> selectNewSysFilesLabeledPage(Page page, @Param("query") FileDTO file);

	IPage<FileVO> selectNewSysFilesUnLabeledPage0512(Page page, @Param("query") FileDTO file);

	List<FileVO> selectFileLabelList(@Param("query") FileDTO fileDTO);

	IPage<Long> selectFileOrderIds(Page page, @Param("query") FileDTO file);

	List<LabelVO> selectLabelByIds0512(@Param("query") FileDTO file, @Param("list") List<Long> list);

	Long saveFile(SysFile sysFile);
}
