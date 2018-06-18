/*
 *  CMB Confidential
 *
 *  Copyright (C) 2016 China Merchants Bank Co., Ltd. All rights reserved.
 *
 *  No part of this file may be reproduced or transmitted in any form or by any
 *  means, electronic, mechanical, photocopying, recording, or otherwise, without
 *  prior written permission of China Merchants Bank Co., Ltd.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.*;

public class CSVData {

    private List<String> headers;

    private float[][] data;

    private int rows;

    private int columns;

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = new ArrayList<String>();
        for(String s : headers){
            this.headers.add(s);
        }
        setColumns(this.headers.size());
    }

    public float[][] getData() {
        return data;
    }

    public void setData(float[][] data) {
        this.data = data;
        setRows(this.data.length);
        setColumns(this.data[0].length);
    }
    public void setData(List<String> list, String delimeter) {

        String[] temp = list.get(0).split(delimeter);
        setColumns(temp.length);
        setRows(list.size());

        data = new float[rows][columns];
        for(int i = 0; i < list.size(); i++){
            String[] str = list.get(i).split(delimeter);
            for(int j = 0; j < str.length; j++){
                data[i][j] = Float.parseFloat(str[j]);
            }
        }
    }

    public float[] getColumn(int idx){
        float[] result = new float[rows];
        for(int i = 0; i < rows; i++){
            result[i] = data[i][idx];
        }
        return result;
    }

    public float[] getColumn(String name){
        int idx = headers.indexOf(name);
        float[] result = new float[rows];
        for(int i = 0; i < rows; i++){
            result[i] = data[i][idx];
        }
        return result;
    }

    public float[][] getRangeColumn(int begin, int end){
        float[][] result = new float[rows][end - begin];
        for(int i = 0; i < rows; i++){
            System.arraycopy(data[i], begin, result[i], 0, end - begin);
        }
        return result;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void sort(int column){
        if(column >= 0 && column < columns){
            Arrays.sort(data, new ArrayComparaterIncr(column));
        }else{
            throw new RuntimeException("指定的列不在数据范围内");
        }
    }

    public void sort(String columnName){
        int idx = headers.indexOf(columnName);
        if(idx != -1){
            Arrays.sort(data, new ArrayComparaterIncr(idx));
        }else{
            throw new RuntimeException("没有对应名字的列");
        }
    }

    public void sort(String columnName, Comparator<float[]> comparator){
        int idx = headers.indexOf(columnName);
        if(idx != -1){
            Arrays.sort(data, comparator);
        }else{
            throw new RuntimeException("没有对应的列");
        }
    }

    /**
     * 输出信息到文件
     * @param filename
     */
    public void writeToCSV(String filename) {
        Writer writer = null;
        try {
            writer = new FileWriter(filename);
            List<String> headers = getHeaders();
            for(int i = 0; i < headers.size(); i++) {
                writer.write(headers.get(i));
                if(i == headers.size() - 1) {
                    writer.write("\n");
                    break;
                }
                writer.write("\t");
            }

            float[][] data = getData();
            for(int i = 0; i < data.length; i++) {
                for(int j = 0; j < data[i].length - 1; j++) {
                    writer.write(String.valueOf(data[i][j]) + '\t');
                }
                writer.write(String.valueOf(data[i][data[i].length - 1]) + '\n');
            }
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 文件读取CSV信息
     * @param filename
     */
    public void readData(String filename, String delimeter) {
        try {
            Scanner sc = new Scanner(new File(filename));
            List<String> list = new ArrayList<String>();
            String headers = sc.nextLine();
            while(sc.hasNextLine()) {
                list.add(sc.nextLine());
            }
            setHeaders(headers.split(delimeter));
            setData(list, delimeter);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 升序比较器
 */
class ArrayComparaterIncr implements Comparator<float[]>{

    private int dim;

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public ArrayComparaterIncr(int dim) {
        this.dim = dim;
    }

    public int compare(float[] o1, float[] o2) {
        return (int)(o1[dim] - o2[dim]);
    }
}

/**
 * 降序比较器
 */
class ArrayComparaterDesc implements Comparator<float[]>{

    private int dim;

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public ArrayComparaterDesc(int dim) {
        this.dim = dim;
    }

    public int compare(float[] o1, float[] o2) {
        return (int)(o2[dim] - o1[dim]);
    }
}