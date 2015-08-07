package com.sunand;

import rx.functions.ActionN;
import rx.util.async.Async;

/**
 * Created by Sunand on 8/6/2015.
 */
public class AsyncRx {
    public static long start = 0;
    public static long end = 0;
    public static void main( String[] args )
    {
        start = System.currentTimeMillis();
        helloRx("Sunand", "Jagadeesh");
        end = System.currentTimeMillis();
        System.out.println("Time Taken Async: " + (end - start));


        start = System.currentTimeMillis();
        helloSync("Sunand", "Jagadeesh");
        end = System.currentTimeMillis();
        System.out.println("Time Taken Sync1: " + (end - start));

        start = System.currentTimeMillis();
        helloSync("Sunand", "Jagadeesh");
        end = System.currentTimeMillis();
        System.out.println("Time Taken Sync2: " + (end - start));

    }

    public static void helloRx(String... names) {

        final ActionN action = new ActionN() {
            public void call(Object... names) {
                for(Object name: names) {
                    System.out.println("Hello " + String.valueOf(name) + "!");
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        final long start1 = System.currentTimeMillis();
        Async.toAsync(action).call(names).subscribe();
        final long end1 = System.currentTimeMillis();
        System.out.println("Time Taken RX1: " + (end1 - start1));

        final long start2 = System.currentTimeMillis();
        Async.toAsync(action).call(names).subscribe();
        final long end2 = System.currentTimeMillis();
        System.out.println("Time Taken RX2: " + (end2 - start2));
/*        Observable.from(names).subscribe(new Action1<String>() {

            public void call(String name) {
                System.out.println("Hello " + name + "!");
            }

        });*/
    }

    public static void helloSync(String... names) {
        for(String name : names) {
            System.out.println("Hello " + name + "!");
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
