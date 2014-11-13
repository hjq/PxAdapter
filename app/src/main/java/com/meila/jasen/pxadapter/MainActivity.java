package com.meila.jasen.pxadapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText heightText;
    private EditText widthText;
    private EditText dpiText;
    private TextView dpiUnitText;
    private Button resetButton;
    private Button adapterButton;
    private Toast toast;
    private RadioGroup tabRadioGroup;
    private RadioButton autoRadioButton;
    private RadioButton handRadioButton;
    private static float screenHeight;
    private static float screenWidth;
    private static float screenDpi;
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
        autoRadioButton.setChecked(true);
    }

    /**
     * 初始化view
     */
    private void initViews() {
        heightText = (EditText)findViewById(R.id.height_detail);
        widthText = (EditText)findViewById(R.id.width_detail);
        dpiText = (EditText)findViewById(R.id.dpi_detail);
        dpiUnitText = (TextView)findViewById(R.id.dpi_unit);
        resetButton = (Button)findViewById(R.id.reset_button);
        adapterButton = (Button)findViewById(R.id.adapter_button);
        tabRadioGroup = (RadioGroup)findViewById(R.id.tab_group);
        autoRadioButton = (RadioButton)findViewById(R.id.auto_tab);
        handRadioButton = (RadioButton)findViewById(R.id.hand_tab);
    }

    /**
     * 设置监听
     */
    private void setListener() {
        resetButton.setOnClickListener(this);
        adapterButton.setOnClickListener(this);
        tabRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.auto_tab:
                        resetButton.setVisibility(View.INVISIBLE);
                        heightText.setEnabled(false);
                        widthText.setEnabled(false);
                        dpiText.setEnabled(false);

                        //获取当前设备屏幕分辨率和密度
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        screenHeight = displayMetrics.heightPixels;
                        screenWidth = displayMetrics.widthPixels;
                        screenDpi = displayMetrics.densityDpi;
                        heightText.setText(" " + Integer.toString((int)screenHeight));
                        widthText.setText(" " + Integer.toString((int)screenWidth));
                        dpiText.setText(" " + Integer.toString((int)screenDpi));
                        dpiUnitText.setText(getDpiLevel((int)screenDpi));

                        //根据设备dpi生成相应的文件夹
                        folderName = "values" + getDpiLevel((int)screenDpi) + "-" + Integer.toString((int)screenHeight)
                                + "x" + Integer.toString((int)screenWidth);
                        break;

                    case R.id.hand_tab:
                        resetButton.setVisibility(View.VISIBLE);
                        upDateViews();
                        break;
                }
            }
        });
    }

    /**
     * 设置点击响应事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        String dimens;
        switch (v.getId()) {
            case R.id.adapter_button:
                if (handRadioButton.isChecked()) {
                    String heightStr = heightText.getText().toString();
                    String widthStr = widthText.getText().toString();
                    String dpiStr = dpiText.getText().toString();
                    if (!heightStr.equals("") && !widthStr.equals("") && !dpiStr.equals("")) {
                        heightText.setEnabled(false);
                        widthText.setEnabled(false);
                        dpiText.setEnabled(false);
                        adapterButton.setEnabled(false);
                        dimens = getAllDimens();

                        //根据输入信息生成相应的文件夹
                        folderName = "values" + getDpiLevel(Integer.valueOf(dpiStr)) + "-" + heightStr + "x" + widthStr;

                        writeFile(dimens);

                        heightText.setText(heightStr);
                        widthText.setText(widthStr);
                        dpiText.setText(dpiStr);
                        dpiUnitText.setText(getDpiLevel(Integer.valueOf(dpiStr)));
                    } else {
                        if (toast != null){
                            toast.cancel();
                        }
                        toast = Toast.makeText(this, R.string.error_tips, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else {
                    adapterButton.setEnabled(false);
                    dimens = getAllDimens();
                    writeFile(dimens);
                }
                break;

            case R.id.reset_button:
                upDateViews();
                break;
        }
    }

    /**
     * 更新 “手动换算” 模式下的view
     */
    private void upDateViews() {
        heightText.setText("");
        heightText.setEnabled(true);
        widthText.setText("");
        widthText.setEnabled(true);
        dpiText.setText("");
        dpiText.setEnabled(true);
        dpiUnitText.setText("");
    }

    /**
     *判断 dpiLevel
     * @param screenDpi
     * @return
     */
    private String getDpiLevel(int screenDpi) {
        String dpiLevel;
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
     * 把 px(像素) 的单位 转成为 dp
     */
    public float pxToDip(float pxValue) {
        float dpValue;
        if (autoRadioButton.isChecked()) {
            dpValue = (screenWidth/480) / (screenDpi/240) * (pxValue/2);
        } else {
            dpValue = (Float.valueOf(widthText.getText().toString())/480)
                    / (Float.valueOf(dpiText.getText().toString())/240) * (pxValue/2);
        }
        BigDecimal bigDecimal = new BigDecimal(dpValue);
        float finDp = bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
        return finDp;
    }

    /** 生成内容 */
    public String getAllDimens() {
        float dpValue;
        float spValue;
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\r\n");
            sb.append("<resources>" + "\r\n");

//            sb.append("<dimen name=\"px_0" + "\">" + 0 + "dp</dimen>"
//                    + "\r\n");
            for (int i = 0; i <= 1024; i++) {
                dpValue = pxToDip((float)i);
                sb.append("<dimen name=\"px_" + i + "\">" + dpValue + "dp</dimen>"
                        + "\r\n");
            }

//            sb.append("<dimen name=\"px_text_0" + "\">" + 0 + "sp</dimen>"
//                    + "\r\n");
            for (int j = 0; j <= 60; j++) {
                spValue = pxToDip((float)j);
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
            adapterButton.setEnabled(true);
            toast = Toast.makeText(this, getResources().getText(R.string.file_path_tips) + folderPath + File.separator + FILE_NAME,
                    Toast.LENGTH_LONG);
            toast.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (toast != null) {
            toast.cancel();
        }
    }
}
