package com.coderpwh.service.iml;

import com.coderpwh.entity.User;
import com.coderpwh.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author coderpwh
 */
@Service
public class UserServiceImpl implements UserService {


    /***
     * 获取所有用户
     * @return
     */
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setEmail("110@qq.com");
        user.setCreatedAt(LocalDateTime.now());
        list.add(user);


        User userTwo = new User();
        userTwo.setId(2L);
        userTwo.setName("李四");
        userTwo.setEmail("120@qq.com");
        userTwo.setCreatedAt(LocalDateTime.now());
        list.add(userTwo);
        return list;
    }

}
