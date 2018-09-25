package com.dao;


import com.service.User;
import com.service.UserDynamic;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UserDao {
    private static SqlSession session;//设置成静态就不用每次就new了
    private final  static SqlSessionFactory sqlSessionFactory;
    static {
        String resource="config.xml";//mybatis的配置文件位置
        InputStream inputStream=null;
        try {
            inputStream = Resources.getResourceAsStream(resource);//将xml的配置信息注入
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);//建一个session的工厂类

        session= sqlSessionFactory.openSession();
    }

    public static void insertUser(User user)//插入一个User
    {
        session.insert("test.insertUser",user);
        session.commit();
    }
    public static void insertUserDynamic(UserDynamic userdynamic)//插入一个UserDynamic
    {
        session.insert("test.insertUserDynamic",userdynamic);
        session.commit();
    }

}
