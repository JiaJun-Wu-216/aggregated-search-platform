package com.chipswu.aggregatedsearchplatform;

import cn.hutool.core.util.RandomUtil;
import com.chipswu.aggregatedsearchplatform.mapper.UserMapper;
import com.chipswu.aggregatedsearchplatform.model.entity.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author WuJiaJun
 */
@SpringBootTest
public class UserTest {

    @Resource
    private UserMapper userMapper;

    @Test
    public void insertUser(){
        List<String> profileList = Arrays.asList(
                "热爱编程与旅行的前端工程师",
                "资深后端开发，喜欢开源项目",
                "UI设计师，擅长Figma和Sketch",
                "数据分析师，精通Python和SQL",
                "产品经理，关注用户体验与增长",
                "运维工程师，熟悉K8s与Docker",
                "算法研究员，研究NLP方向",
                "全栈开发者，爱好摄影",
                "学生，计算机专业大三，实习中",
                "自由职业者，接Web开发私活",
                "高校教师，讲授数据库课程",
                "游戏开发工程师，Unity专家",
                "网络安全爱好者，CTF选手",
                "内容运营，负责社区建设",
                "嵌入式开发，专注IoT设备",
                "AI产品经理，推动大模型落地",
                "测试工程师，自动化测试专家",
                "技术博客作者，每周更新技术文章",
                "创业公司CTO，技术驱动型管理者",
                "实习生，正在学习React和Node.js"
        );
        List<User> userList = new ArrayList<>();
        for (String profile : profileList) {
            User user = User.builder().username(RandomUtil.randomString(5)).profile(profile).build();
            userList.add(user);
        }
        userMapper.insertBatch(userList);
    }

    @Test
    public void insertOneUser(){
        User user = User.builder().username(RandomUtil.randomString(5)).build();
        userMapper.insert(user);
    }
}
