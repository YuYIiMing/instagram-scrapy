
import com.dao.UserDao;
import com.service.User;
import org.junit.Test;
import java.util.List;


public class test {


    @Test//测试插入操作
    public void  test_insertUser()throws Exception
    {
        User user=new User();

        UserDao.insertUser(user);

    }
}
