package org.snaker.engine.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @Author jym
 * @Description:
 * @Param:
 * @Return:
 * @Create: 2021/3/8
 */
public class sendMailUtils {

    //读取cdm的mail.propertices
    public static  Properties getPro(){
        Properties properties = new Properties();
       // File file = new File(".","mail.properties");
        File file = new File("./src/main/resources","mail.properties");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  properties;
    }

    public boolean send(String[] toEmall ,String content ,String title){
       try {
           MailAccount account = new MailAccount();
           Properties getPro = sendMailUtils.getPro();
           if (getPro == null) {
               return false;
           }
           account.setHost(getPro.getProperty("host"));
           account.setPort(Integer.valueOf(getPro.getProperty("port")));
           account.setAuth(true);
           account.setFrom(getPro.getProperty("from"));
           account.setUser(getPro.getProperty("user"));
           account.setPass(getPro.getProperty("pass"));

           MailUtil.send(account, CollUtil.newArrayList(toEmall), title,content , false);
       }catch (Exception e ){
           e.printStackTrace();
           return false;
       }
        return true ;
    }
    public boolean send(String toEmall ,String content ,String title){
       try {
           MailAccount account = new MailAccount();
           Properties getPro = sendMailUtils.getPro();
           if (getPro == null) {
               return false;
           }
           account.setHost(getPro.getProperty("host"));
           account.setPort(Integer.valueOf(getPro.getProperty("port")));
           account.setAuth(true);
           account.setFrom(getPro.getProperty("from"));
           account.setUser(getPro.getProperty("user"));
           account.setPass(getPro.getProperty("pass"));

           MailUtil.send(account, CollUtil.newArrayList(toEmall), title,content , false);
       }catch (Exception e ){
           e.printStackTrace();
           return false;
       }
        return true ;
    }

    public static void main(String[] args) {

        //   m.send(s,"1","2");
        //erfycutdzxsbbbci
        MailAccount account = new MailAccount();
//        account.setHost("smtp.qq.com");
////        account.setFrom("1009175172@qq.com");
////        account.setUser("1009175172");
////        account.setPass("erfycutdzxsbbbci");

//        account.setHost("mail.yiling.cn");
//        account.setPort(25);
//        account.setAuth(true);
//        account.setFrom("dems@yiling.cn");
//        account.setUser("dems");
//        account.setPass("YILING123a");
//
//        MailUtil.send(account, CollUtil.newArrayList("925058637@qq.com"), "标题：测试",new Date().toString() , false);
//


        sendMailUtils m =new sendMailUtils();
        String [] s = {"jiyongming@yiling.cn"};
        CollUtil.newArrayList(s);
        System.out.println();
        m.send(s,"123","2");
    }
}
