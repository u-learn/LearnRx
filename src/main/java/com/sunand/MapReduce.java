package com.sunand;

import org.apache.commons.io.FileUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sunand on 8/10/2015.
 */
public class MapReduce {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        try {
            list = FileUtils.readLines(new File("./ABB.csv"));
            System.out.println("Size : " + list.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String[] headers = list.get(0).replaceAll("\"", "").trim().split(",");
        System.out.println("Starting");
        System.out.println("List size : " + list.size());
        Observable.from(list).skip(1)
                /*.doOnNext(new Action1<String>() {
                    public void call(String s) {
                        System.out.println(s);
                    }
                })*/.flatMap(
                new Func1<String, Observable<Map<String, Double>>>() {
                    public Observable<Map<String, Double>> call(String s) {
                        String[] values = s.replaceAll("\"", "").trim().split(",");
                        Map<String, Double> record = new HashMap<String, Double>();
                        for (int i = 1; i < headers.length; i++) {
                            record.put(headers[i], Double.parseDouble(values[i]));
                        }
                        return Observable.just(record);
                    }
                }
        ).reduce(new Func2<Map<String, Double>, Map<String, Double>, Map<String, Double>>() {
            Map<String, Double> record = new HashMap<String, Double>();

            public Map<String, Double> call(Map<String, Double> firstValue, Map<String, Double> secondValue) {
                if (firstValue.containsKey("Sum")) {
                    final Double sum = (Double) firstValue.get("Sum");
                    final Double close = (Double) secondValue.get("Close");
                    Double sumValue = sum + close;
                    Double count = (Double) record.get("Count");
                    Double min = (Double) record.get("Min");
                    Double max = (Double) record.get("Max");
                    record.put("Count", count + 1);
                    record.put("Sum", sumValue);
                    record.put("Average", sumValue/count);
                    record.put("Min", Math.min(min, close));
                    record.put("Max", Math.max(max, close));
                }
                else {
                    final Double firstClose = (Double) firstValue.get("Close");
                    final Double close = (Double) secondValue.get("Close");
                    Double sumValue = firstClose + close;
                    record.put("Sum", sumValue);
                    record.put("Min", Math.min(firstClose, close));
                    record.put("Max", Math.max(firstClose, close));
                    record.put("Count", 2.0);
                }
                return record;
            }
        }).subscribe(new Action1<Map<String, Double>>() {
            public void call(Map<String, Double> stringObjectMap) {
                System.out.println("Recuded values : " + stringObjectMap);
            }
        });


    }
}
