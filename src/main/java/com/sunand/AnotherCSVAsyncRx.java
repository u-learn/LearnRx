package com.sunand;

import org.apache.commons.io.FileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
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
public class AnotherCSVAsyncRx {
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

        /*start = System.currentTimeMillis();
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
*/
        helloRxAsync(list);
    }

    private static void helloRxAsync(List<String> lineItems) {

        Action1 action1 = null;
        try {
            action1 = new Action1<String>() {
                File f =  new File("async" + increment() + ".csv");
                FileOutputStream fos = new FileOutputStream(f, true);
                public void call(String name) {
                    try {
//                            FileUtils.writeStringToFile(f, String.valueOf(name));
                        fos.write(String.valueOf(name + "\n").getBytes());
                        //throw new RuntimeException("BANG");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Action1 errorAction = new Action1<Exception>() {
            public void call(Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        };

        Action0 completeAction = new Action0() {
            public void call() {
                System.out.println("Finally");
                System.out.println(Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
            }
        };

//        Async.
        //Observable.from(lineItems).subscribe(action1, errorAction, completeAction);
        start = System.currentTimeMillis();
        Observable.from(lineItems).doOnNext(action1).subscribe(new Subscriber<String>() {
            public void onCompleted() {
                System.out.println("WHy oh Why");
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(String s) {
                System.out.println("Boss : " + s);
            }
        });
        end = System.currentTimeMillis();
        System.out.println("Sync Observable Time Taken : " + (end - start));


        final Func1<String, Observable<Void>> func1 = Async.toAsync(action1);
        /*func1.call("sunand").subscribe(new Subscriber<Void>() {
            public void onCompleted() {
                System.out.println("Each Record Yay");
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(Void aVoid) {
            }

        });*/


        start = System.currentTimeMillis();
        Observable.from(lineItems).subscribe(new Subscriber<String>() {
            public void onCompleted() {
                System.out.println("Final Final");
                System.out.println("Final Final - " + Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(String s) {
                func1.call(s).subscribe(new Subscriber<Void>() {
                    public void onCompleted() {
                        System.out.println("Yappa");
                        System.out.println("Yappa - " + Thread.currentThread().getName() + " - " + Thread.currentThread().getId());
                    }

                    public void onError(Throwable throwable) {

                    }

                    public void onNext(Void aVoid) {

                    }
                });
            }
        });
        end = System.currentTimeMillis();
        System.out.println("Async Observable Time Taken : " + (end - start));


    }

    public static void helloRx(List<String> names) {

        Action1 action1 = null;
        try {
            action1 = new Action1<List<String>>() {
                File f =  new File("async" + increment() + ".csv");
                FileOutputStream fos = new FileOutputStream(f, true);
                public void call(List<String> names) {
                    for(String name: names) {
                        try {
//                            FileUtils.writeStringToFile(f, String.valueOf(name));
                            fos.write(String.valueOf(name + "\n").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Action1 action2 = null;
        try {
            action2 = new Action1<List<String>>() {
                File f =  new File("async" + increment() + ".csv");
                FileOutputStream fos = new FileOutputStream(f, true);
                public void call(List<String> names) {
                    for(Object name: names) {
                        try {
    //                            FileUtils.writeStringToFile(f, String.valueOf(name));
                            fos.write(String.valueOf(name + "\n").getBytes());
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

        Async.toAsync(action1).call(names);
        final long end1 = System.currentTimeMillis();
        System.out.println("Time Taken RX1: " + (end1 - start1));

        final long start2 = System.currentTimeMillis();
        Async.toAsync(action2).call(names);
        final long end2 = System.currentTimeMillis();
        System.out.println("Time Taken RX2: " + (end2 - start2));

        final long start3 = System.currentTimeMillis();
        Async.toAsync(action2).call(names);
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
