
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.*;

public class Main {

    private static boolean MODEL_DEBUG = false;
    private static boolean USE_MODEL = true;
    private static boolean USE_FILE = true;
    private static String modelName = "CMBModelValidate";

    public static void modelDebug() throws Exception{

        Booster booster = null;
        //模型调参
        if(MODEL_DEBUG){
            DMatrix totalMat = new DMatrix("totalMat");
            DMatrix trainMat = new DMatrix("trainMat");
            DMatrix validateMat = new DMatrix("validateMat");
            //参数设置
            Map<String, Object> paramMap = new HashMap<String, Object>() {
                {
                    put("objective", "binary:logistic");
                    put("eta", 0.1);
                    put("max_depth", 150);
                    put("eval_metric", "auc");
                    put("gamma", 0.1);
                    put("silent", 1);
                    put("min_child_weight", 2);
                    put("scale_pos_weight", 0.1);
                    put("colsample_bytree", 0.8);
                    put("subsample", 0.8);
                    put("learning_rate", 0.1);
                }
            };
            Iterable<Map.Entry<String, Object>> params = paramMap.entrySet();
            Map<String, DMatrix> watchs = new HashMap<>();
            watchs.put("train", trainMat);
            watchs.put("test", validateMat);
            int round = 1000;
            String[] metrics = new String[]{"auc"};
            try {
                //训练
                System.out.println("交叉验证");
                String[] result = XGBoost.crossValidation(totalMat, paramMap, round, 4, metrics, null, null);
                FileWriter file = new FileWriter("result.txt");
                for(String s : result)
                    file.write(s);
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws Exception {

        if(MODEL_DEBUG){
            modelDebug();
            return;
        }

        String base = "E:\\Java Projec WorkSpace\\CMBCompetition\\src\\data\\";
        Booster booster = null;

        String train_agg = base + "train_agg.csv";
        String train_flg = base + "train_flg.csv";
        String train_log = base + "train_log.csv";

        List<LogBean> trainLogList = FileTool.parseLogToBean(train_log);
        System.out.println("训练数据预处理");
        //处理log文件
        List<String> NameList = processTrainLog(trainLogList);


        if(!USE_MODEL) {

            DMatrix totalMat = new DMatrix("totalMat");
            DMatrix trainMat = new DMatrix("trainMat");
            DMatrix validateMat = new DMatrix("validateMat");

            if(!USE_FILE) {
                System.out.println("从文件训练模型");
                System.out.println("训练数据加载");
                //训练数据加载
                CSVData csvData = FileTool.readData(train_agg);
                csvData.sort("USRID");
                CSVData csvData1 = FileTool.readData(train_flg);
                csvData1.sort("USRID");

                DenseMartrix trainMartrix = process(csvData, trainLogList, NameList);

                int start = 70000;
                int end = 80000;
                float[][] train = trainMartrix.getRowData(0, start);
                float[][] validate = trainMartrix.getRowData(start, end);
                float[] label = csvData1.getColumn("FLAG");

                totalMat = new DMatrix(flatten(trainMartrix.getData()), trainMartrix.getNrows(), trainMartrix.getNcols());
                totalMat.setLabel(label);
                trainMat = new DMatrix(flatten(train), train.length, train[0].length);
                trainMat.setLabel(Arrays.copyOfRange(label, 0, start));
                validateMat = new DMatrix(flatten(validate), validate.length, validate[0].length);
                validateMat.setLabel(Arrays.copyOfRange(label, start, end));

                totalMat.saveBinary("totalMat");
                trainMat.saveBinary("trainMat");
                validateMat.saveBinary("validateMat");
            }
            /**
             float[][] Data = csvData.getRangeColumn(0, csvData.getColumns() - 1);
             DenseMartrix trainMartrix1 = martrixToArray(Data);

             DMatrix trainMat = new DMatrix(trainMartrix1.getData(), trainMartrix1.getNrows(), trainMartrix1.getNcols());
             */

            //参数设置
            Map<String, Object> paramMap = new HashMap<String, Object>() {
                {
                    put("objective", "binary:logistic");
                    put("eta", 0.1);
                    put("max_depth", 150);
                    put("eval_metric", "auc");
                    put("gamma", 0.1);
                    put("silent", 1);
                    put("min_child_weight", 2);
                    put("scale_pos_weight", 0.1);
                    put("colsample_bytree", 0.8);
                    put("subsample", 0.8);
                    put("learning_rate", 0.1);
                }
            };
            Iterable<Map.Entry<String, Object>> params = paramMap.entrySet();
            Map<String, DMatrix> watchs = new HashMap<>();
            watchs.put("train", totalMat);
            watchs.put("test", validateMat);
            int round = 100;
            try {
                //训练
                System.out.println("进行模型训练");
                booster = XGBoost.train(totalMat, paramMap, round, watchs, null, null);
            } catch(Exception e) {
                e.printStackTrace();
            }
            //保存模型
            System.out.println("保存模型");
            booster.saveModel(modelName);
        } else {
            System.out.println("从文件记载已有模型");
            booster = XGBoost.loadModel(modelName);
        }

        /**
         * 测试数据
         */
        System.out.println("加载测试数据");
        String test_agg = base + "test_agg.csv";
        String test_log = base + "test_log.csv";

        CSVData csvData2 = FileTool.readData(test_agg);
        csvData2.sort("USRID");
        List<LogBean> testLogList = FileTool.parseLogToBean(test_log);

        DenseMartrix testMartrix = process(csvData2, testLogList, NameList);

        DMatrix testMat = new DMatrix(flatten(testMartrix.getData()), testMartrix.getNrows(), testMartrix.getNcols());

        /*
        float[][] Data = csvData2.getRangeColumn(0, csvData2.getColumns() - 1);
        DenseMartrix testMartrix1 = martrixToArray(Data);

        DMatrix testMat = new DMatrix(testMartrix1.getData(), testMartrix1.getNrows(), testMartrix1.getNcols());
*/
        //进行预测
        System.out.println("进行预测");
        float[][] predict = booster.predict(testMat);
        int[] testUSRID = floatToInt(csvData2.getColumn("USRID"));

        //输出文件
        CSVData resutlCSVData = new CSVData();
        resutlCSVData.setHeaders(new String[]{"USRID", "RST"});
        float[][] resutlData = new float[testUSRID.length][2];
        for(int i = 0; i < testUSRID.length; i++) {
            resutlData[i][0] = testUSRID[i];
            resutlData[i][1] = predict[i][0];



            /*
            if(predict[i][0] > 0.4) {
                System.out.println(predict[i][0]);
                resutlData[i][1] = 1.0f;
            }
            else
                resutlData[i][1] = predict[i][0];
               */

        }
        resutlCSVData.setData(resutlData);
        FileTool.writeToCSV(resutlCSVData, "test_result.csv");
    }


    public static List<String> processTrainLog(List<LogBean> logList) {
        List<String> list = new ArrayList<>();
        Set<String> set = new LinkedHashSet<>();

        for(int i = 0; i < logList.size(); i++) {
            set.add(logList.get(i).getEventName());
            if(!list.contains(logList.get(i).getEventName()))
                list.add(logList.get(i).getEventName());
        }
        return list;
    }

    public static float[] flatten(float[][] mat) {
        int size = 0;
        for (float[] array : mat) size += array.length;
        float[] result = new float[size];
        int pos = 0;
        for (float[] ar : mat) {
            System.arraycopy(ar, 0, result, pos, ar.length);
            pos += ar.length;
        }

        return result;
    }

    public static DenseMartrix concatToDenseMartrix(float[][] a, float[][] b) {
        DenseMartrix result = new DenseMartrix();
        float[][] data = new float[a.length][a[0].length + b[0].length];
        for(int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, data[i], 0, a[i].length);
            System.arraycopy(b[i], 0, data[i], a[i].length, b[i].length);
        }
        result.setData(data);
        result.setNrows(a.length);
        result.setNcols(a[0].length + b[0].length);
        return result;
    }

    public static Map<Integer, float[]> processTrainLog(List<LogBean> logList, List<String> list) {

        Map<Integer, float[]> map = new HashMap<>();
        for(int i = 0; i < logList.size(); i++) {
            Integer userId = logList.get(i).getUserId();
            String eventName = logList.get(i).getEventName();
            int idx = list.indexOf(eventName);
            if(map.containsKey(userId)) {
                if(idx != -1){
                    float[] count = map.get(userId);
                    count[idx] += 1;
                    map.put(userId, count);
                }
            } else {
                float[] count = new float[list.size()];
                if(idx != -1) {
                    count[idx] += 1;
                }
                map.put(userId, count);
            }
        }
        return map;
    }

    public static int[] floatToInt(float[] data) {
        int[] result = new int[data.length];
        for(int i = 0; i < data.length; i++) {
            result[i] = (int) data[i];
        }
        return result;
    }

    public static float[][] dealAggAndLog(int[] userID, List<String> tempList, Map<Integer, float[]> map) {
        float[][] appDataList = new float[userID.length][tempList.size()];
        for(Map.Entry<Integer, float[]> entry : map.entrySet()) {
            int idx = Arrays.binarySearch(userID, entry.getKey());
            appDataList[idx] = entry.getValue();
        }
        return appDataList;
    }


    public static DenseMartrix process(CSVData csvData, List<LogBean> LogList, List<String> NameList) {
        Map<Integer, float[]> LogMap = processTrainLog(LogList, NameList);

        int[] userID = floatToInt(csvData.getColumn("USRID"));
        float[][] appDataList = dealAggAndLog(userID, NameList, LogMap);

        float[][] Data = csvData.getRangeColumn(0, csvData.getColumns() - 1);
        DenseMartrix denseMartrix = concatToDenseMartrix(Data, appDataList);
        return denseMartrix;
    }

}
