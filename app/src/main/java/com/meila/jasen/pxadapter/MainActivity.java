package com.meila.jasen.pxadapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;


public class MainActivity extends Activity implements View.OnClickListener {

    private TextView heightText;
    private TextView widthText;
    private TextView dpiText;
    private Button adapterButton;
    private static String folderName;
    private static String folderPath;
    private static final String FILE_NAME = "px_dimens.xml";
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListener();

        //获取屏幕分辨率和密度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        int screenDpi = displayMetrics.densityDpi;
        heightText.setText(" " + Integer.toString(screenHeight) + "px");
        widthText.setText(" " + Integer.toString(screenWidth) + "px");
        dpiText.setText(" " + Integer.toString(screenDpi) + getDpiLevel(screenDpi));

        //根据设备dpi生成相应的文件夹
        folderName = "values" + getDpiLevel(screenDpi) + "-" + Integer.toString(screenHeight) + "x" + Integer.toString(screenWidth);
    }

    /**
     * 初始化view
     */
    private void initViews() {
        heightText = (TextView)findViewById(R.id.height_detail);
        widthText = (TextView)findViewById(R.id.width_detail);
        dpiText = (TextView)findViewById(R.id.dpi_detail);
        adapterButton = (Button)findViewById(R.id.adapter_button);
    }

    /**
     * 设置监听
     */
    private void setListener() {
        adapterButton.setOnClickListener(this);
    }

    /**
     * 设置点击响应事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adapter_button:
                String dimens = getAllDimens();
                writeFile(dimens);
                break;
        }
    }

    /**
     *判断 dpiLevel
     * @param screenDpi
     * @return
     */
    private String getDpiLevel(int screenDpi) {
        String dpiLevel = "";
        if (screenDpi < 160) {
            dpiLevel = "-ldpi";
        } else if (screenDpi < 240) {
            dpiLevel = "-mdpi";
        } else if (screenDpi < 320) {
            dpiLevel = "-hdpi";
        } else if (screenDpi < 480) {
            dpiLevel = "-xhdpi";
        } else if (screenDpi < 640) {
            dpiLevel = "-xxhdpi";
        } else {
            dpiLevel = "-xxxhdpi";
        }
        return dpiLevel;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        System.out.print("------------------" + scale + "------------------");
        float dpValue = (pxValue * 160) / scale;
        BigDecimal bigDecimal = new BigDecimal(dpValue);
        float finDp = bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
        return finDp;
    }

    /** 生成内容 */
    public String getAllDimens() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n");
            sb.append("<resources>" + "\r\n");

            sb.append("<dimen name=\"px_0" + "\">" + 0 + "dp</dimen>"
                    + "\r\n");
            for (int i = 1; i <= 1024; i++) {
                System.out.println("i=" + i);
                float dpValue = pxToDip(this, (float) i);
                sb.append("<dimen name=\"px_" + i + "\">" + dpValue + "dp</dimen>"
                        + "\r\n");
            }

            sb.append("<dimen name=\"px_text_0" + "\">" + 0 + "sp</dimen>"
                    + "\r\n");
            for (int j = 1; j <= 60; j++) {
                float spValue = pxToDip(this, (float) j);
                sb.append("<dimen name=\"px_text_" + j + "\">" + spValue + "sp</dimen>"
                        + "\r\n");
            }
            sb.append("</resources>" + "\r\n");
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 生成 "px_dimens.xml" 文件
     * @param st
     */
    public void writeFile(String st) {
        folderPath = SDCARD_PATH + File.separator + folderName;
        File fileDir = new File(folderPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        try {

            FileOutputStream fileOs = new FileOutputStream(folderPath + File.separator + FILE_NAME);
            byte [] bytes = st.getBytes();
            fileOs.write(bytes);
            fileOs.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
