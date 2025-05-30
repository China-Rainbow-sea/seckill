package com.rainbowsea.seckill.utill;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainbowsea.seckill.pojo.User;
import com.rainbowsea.seckill.vo.RespBean;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author RainbowSea
 * @version 1.0
 * UserUtil: 生成多用户测试脚本，这里生成 2000 个用户，
 * 2000 个用户的密码都为 123456,用户手机号为： 13300000100L 列加到 2000
 * 1. 创建多个用户,保存到seckill_user表
 * 2. 模拟http请求,生成jmeter压测的脚本
 */
//生成多用户工具类
//创建用户，并且去登录得到userticket，得到的userTicket写入到config.txt文件内
public class UserUtil {
    public static void create(int count) throws Exception {
        List<User> users = new ArrayList<>(count);
        //count表示你要创建的用户个数
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(13300000100L + i);
            user.setNickname("user" + i);
            //小伙伴也可以生成不同的盐,这里老师就是有一个
            user.setSlat("cLo8QmTG");//用户数据表的slat,由程序员设置
            //?是用户原始密码,比如123456 , hello等
            //小伙伴也可以生成不同的密码
            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSlat()));
            users.add(user);
        }
        System.out.println("create user");
        //将插入数据库-seckill_user
        Connection connection = getConn();
        String sql = "insert into seckill_user(nickname,slat,password,id) values(?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            preparedStatement.setString(1, user.getNickname());
            preparedStatement.setString(2, user.getSlat());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.clearParameters();//关闭
        connection.close();
        System.out.println("insert to do");
        //模拟登录,发出登录拿到userTicket
        String urlStr = "http://localhost:8080/login/doLogin";

        // 生成 Jmeter 需要的脚本的位置,指定的位置。
        //File file = new File("D:\\temp\\config0.txt");
        File file = new File("E:\\Java\\project\\seckill\\jmeter测试脚本\\config.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            //请求
            URL url = new URL(urlStr);

            //使用HttpURLConnection 发出 http请求
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            // 设置输入网页密码（相当于输出到页面）
            co.setDoOutput(true);
            OutputStream outputStream = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToMidPass("123456");
            outputStream.write(params.getBytes());
            outputStream.flush();
            // 获取网页输出，（得到输入流，把结果得到，再输出到 ByteArrayOutputStream内）
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) >= 0) {
                bout.write(bytes, 0, len);
            }
            inputStream.close();
            bout.close();
            // 把 ByteArrayOutputStream 内的东西转换为 respBean 对象
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            RespBean respBean = mapper.readValue(response, RespBean.class);
            // 得到 userTicket
            String userTicket = (String) respBean.getObj();
            System.out.println("create userTicket" + userTicket);
            String row = user.getId() + "," + userTicket;
            // 写入指定文件
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file:" + user.getId());
        }
        raf.close();
        System.out.println("over");
    }

    private static Connection getConn() throws Exception {
        String url = "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "MySQL123";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }


    /**
     * 用于创建  2000 个用户信息，用于进行 Jmeter 工具进行压测。
     * 2000 个用户的密码都为 123456,用户手机号为： 13300000100L 列加到 2000
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        create(2000);//这里老师创建了2000个用户.
    }
}
