package com.soloman.org.cn.utis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneRoMailVerify
{
    /**
     * 邮箱验证
     * 
     * @param mobiles
     * @return
     */
    public static boolean checkEmail(String email)
    {
        boolean flag = false;
        try
        {
            String check = "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        }
        catch(Exception e)
        {
            flag = false;
        }

        return flag;
    }

    /**
     * 手机号码验证
     * 
     * @param mobiles
     * @return
     */
    public static boolean isMobileNum(String mobiles)
    {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
}
