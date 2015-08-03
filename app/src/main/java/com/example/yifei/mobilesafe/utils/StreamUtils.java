package com.example.yifei.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Yifei on 2015/8/3.
 *
 * 读取流的工具类
 */
public class StreamUtils {


    /*
    * 将输入流读取成为string
    * */
    public static String readFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len= 0;
        byte[] b = new byte[1024];
        while((len=inputStream.read(b))!=-1){
            out.write(b,0,len);
        }
        String result = out.toString();
        out.close();
        inputStream.close();
        return result;
    }
}
