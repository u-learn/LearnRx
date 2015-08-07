package com.sunand;

import org.apache.commons.io.FileUtils;
import rx.Subscriber;
import rx.functions.ActionN;
import rx.util.async.Async;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sunand on 8/6/2015.
 */
public class CSVAsyncRx {
    public static long start = 0;
    public static long end = 0;
    public static void main( String[] args ) {
        List<String> list = new ArrayList<String>();
        try {
            list = FileUtils.readLines(new File("./Accounts.csv"));
            System.out.println("Size : " + list.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        helloRx(list);
        end = System.currentTimeMillis();
        System.out.println("Time Taken Async: " + (end - start));


        start = System.currentTimeMillis();
        helloSync(list);
        end = System.currentTimeMillis();
        System.out.println("Time Taken Sync1: " + (end - start));

        start = System.currentTimeMillis();
        helloSync(list);
        end = System.currentTimeMillis();
        System.out.println("Time Taken Sync2: " + (end - start));

    }

    public static void helloRx(List<String> names) {

        ActionN action1 = null;
        try {
            action1 = new ActionN() {
                File f =  new File("async" + increment() + ".csv");
                FileOutputStream fos = new FileOutputStream(f, true);
                public void call(Object... names) {
                    for(Object name: names) {
                        try {
//                            FileUtils.writeStringToFile(f, String.valueOf(name));
                            List<String> values = (List<String>) name;
                            for(String val : values) {
                                fos.write(String.valueOf(val + "\n").getBytes());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ActionN action2 = null;
        try {
            action2 = new ActionN() {
                File f =  new File("async" + increment() + ".csv");
                FileOutputStream fos = new FileOutputStream(f, true);
                public void call(Object... names) {
                    for(Object name: names) {
                        try {
    //                            FileUtils.writeStringToFile(f, String.valueOf(name));
                            List<String> values = (List<String>) name;
                            for(String val : values) {
                                fos.write(String.valueOf(val + "\n").getBytes());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final long start1 = System.currentTimeMillis();

        Async.toAsync(action1).call(names).subscribe(new Subscriber<Void>() {
            public void onCompleted() {
                System.out.println("I am done - 1");
                System.out.println(System.currentTimeMillis() - start1);
                System.out.println(Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(Void aVoid) {
                System.out.println("Why am i here - 1");
            }
        });

        final long end1 = System.currentTimeMillis();
        System.out.println("Time Taken RX1: " + (end1 - start1));

        final long start2 = System.currentTimeMillis();
        Async.asyncAction(action2).call(names).subscribe(new Subscriber<Void>() {
            public void onCompleted() {
                System.out.println("I am done - 2");
                System.out.println(System.currentTimeMillis() - start1);
                System.out.println(Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(Void aVoid) {
                System.out.println("Why am i here - 2");
            }
        });
        final long end2 = System.currentTimeMillis();
        System.out.println("Time Taken RX2: " + (end2 - start2));

        final long start3 = System.currentTimeMillis();
        Async.asyncAction(action2).call(names).subscribe(new Subscriber<Void>() {
            public void onCompleted() {
                System.out.println("I am done - 3");
                System.out.println(System.currentTimeMillis() - start1);
                System.out.println(Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(Void aVoid) {
                System.out.println("Why am i here - 3");
            }
        });
        final long end3 = System.currentTimeMillis();
        System.out.println("Time Taken RX3: " + (end3 - start3));

        /*Observable.from(names).subscribe(new Action1<String>() {
            public void call(String name) {
                System.out.println("Hello " + name + "!");
            }

        });*/
    }

    public static int index = 1;
    private static int increment() {
        return index ++;
    }


    public static void helloSync(List<String> names) {
        File f =  new File("sync" + increment() + ".csv");
        FileOutputStream fos = null;
        try {
            fos =  new FileOutputStream(f, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(String name : names) {
            try {
                fos.write(String.valueOf(name + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
