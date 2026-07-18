package com.chipswu.aggregatedsearchplatform.service.impl;

import com.chipswu.aggregatedsearchplatform.domain.entity.Article;
import com.chipswu.aggregatedsearchplatform.mapper.ArticleMapper;
import com.chipswu.aggregatedsearchplatform.service.ArticleService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 文章相关应用层接口实现类
 *
 * @author WuJiaJun
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

}
