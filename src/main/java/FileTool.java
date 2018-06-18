/*
 *  CMB Confidential
 *
 *  Copyright (C) 2016 China Merchants Bank Co., Ltd. All rights reserved.
 *
 *  No part of this file may be reproduced or transmitted in any form or by any
 *  means, electronic, mechanical, photocopying, recording, or otherwise, without
 *  prior written permission of China Merchants Bank Co., Ltd.
 */

import akka.japi.pf.FI;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileTool {

    /**
     * 从文件中读取数据到CSVData中
     *
     * @param filename
     * @return
     */
    public static CSVData readData(String filename) {
        try {
            Scanner sc = new Scanner(new File(filename));
            List<String> list = new ArrayList<String>();
            String headers = sc.nextLine();
            while(sc.hasNextLine()) {
                list.add(sc.nextLine());
            }
            CSVData trainData = new CSVData();
            trainData.setHeaders(headers.split("\t"));
            trainData.setData(list, "\t");

            return trainData;

        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  log文件单独处理
     * @param filename
     */
    public static String[][] parseLog(String filename){
        try{
            Scanner sc = new Scanner(new File(filename));
            List<String> list = new ArrayList<String>();
            while(sc.hasNextLine()){
                list.add(sc.nextLine());
            }
            String[][] result = new String[list.size() - 1][4];
            for(int i = 1; i < list.size(); i++){
                String[] str = list.get(i).split("\t");
                result[i - 1][0] = str[0];
                result[i - 1][1] = str[1];
                result[i - 1][2] = str[2];
                result[i - 1][3] = str[3];
            }
            return result;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<LogBean> parseLogToBean(String filename){
        try{
            Scanner sc = new Scanner(new File(filename));
            List<String> list = new ArrayList<String>();
            while(sc.hasNextLine()){
                list.add(sc.nextLine());
            }
            List<LogBean> LogBeanList = new ArrayList<>();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for(int i = 1; i < list.size(); i++){
                LogBean bean = new LogBean();
                String[] str = list.get(i).split("\t");
                bean.setUserId(Integer.parseInt(str[0]));
                bean.setEventName(str[1]);
                try {
                    bean.setDate(dateFormat.parse(str[2]));
                }catch(Exception e){
                    e.printStackTrace();
                }
                bean.setType(Integer.parseInt(str[3]));
                LogBeanList.add(bean);
            }
            return LogBeanList;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static void writeToCSV(CSVData csvData, String filename) {
        Writer writer = null;
        try {
            writer = new FileWriter(filename);
            List<String> headers = csvData.getHeaders();
            for(int i = 0; i < headers.size(); i++) {
                writer.write(headers.get(i));
                if(i == headers.size() - 1) {
                    writer.write("\n");
                    break;
                }
                writer.write("\t");
            }

            float[][] data = csvData.getData();
            for(int i = 0; i < data.length; i++) {
                for(int j = 0; j < data[i].length - 1; j++) {
                    writer.write(String.valueOf((int)data[i][j]) + '\t');
                }
                writer.write(String.valueOf(data[i][data[i].length - 1]) + '\n');
            }
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        CSVData data = readData("../src/data/train_agg.csv");
        data.sort("USRID");
        writeToCSV(data, "./out.txt");
    }
}
