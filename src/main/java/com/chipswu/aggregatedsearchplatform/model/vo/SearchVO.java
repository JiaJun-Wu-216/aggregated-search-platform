package com.chipswu.aggregatedsearchplatform.model.vo;

import com.chipswu.aggregatedsearchplatform.model.entity.Picture;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索结果相关视图类
 *
 * @author WuJiaJun
 */
@Data
public class SearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6483522088879023805L;

    private List<UserVO> userList;

    private List<ArticleVO> postList;

    private List<Picture> pictureList;

    private List<?> dataList;

}
