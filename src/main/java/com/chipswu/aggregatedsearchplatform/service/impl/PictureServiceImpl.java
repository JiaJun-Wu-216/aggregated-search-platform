package com.chipswu.aggregatedsearchplatform.service.impl;

import com.chipswu.aggregatedsearchplatform.domain.entity.Picture;
import com.chipswu.aggregatedsearchplatform.mapper.PictureMapper;
import com.chipswu.aggregatedsearchplatform.service.PictureService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 图片相关应用层接口实现类
 *
 * @author WuJiaJun
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

}
