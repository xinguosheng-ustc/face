package com.cloud.TestPackage;

import com.cloud.plugin.ShowImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.*;
import java.util.Arrays;


public class Test {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public void test1() throws IOException {
        File file = new File("E://xxx2.jpg");
        int ch;
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int)file.length());
        BufferedInputStream in = null;
        in = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[1024];
        int len = 0;
        while (-1 != (len = in.read(buffer, 0, 1024))) {
            baos.write(buffer, 0, len);
        }

        Mat mat = Imgcodecs.imdecode(new MatOfByte(baos.toByteArray()),1);

        ShowImage window = new ShowImage(mat);
        window.getFrame().setVisible(true);
    }
    public static void main(String args[]) throws IOException {
        Float[] encode ={1.2f,2.0f,3.0f};
        String strencode = Arrays.toString(encode);
        System.out.println(strencode);
    }
}
