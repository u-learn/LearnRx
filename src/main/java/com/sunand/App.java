package com.sunand;

import rx.Observable;
import rx.functions.Action1;

/**
 * Hello world!
 *
 */
public class App 
{
    public static long start = 0;
    public static long end = 0;
    public static void main(String[] args) {
        System.out.println("Hello World!");

        start = System.currentTimeMillis();
        helloRx("Sunand", "Jagadeesh");
        end = System.currentTimeMillis();

        System.out.println("Time Taken Async: " + (end - start));


        start = System.currentTimeMillis();
        helloSync("Sunand", "Jagadeesh");
        end = System.currentTimeMillis();

        System.out.println("Time Taken Sync: " + (end-start));



    }

    public static void helloRx(String... names) {
        final Observable<String> observable = Observable.from(names);


        Observable.from(names).subscribe(new Action1<String>() {

            public void call(String name) {
                System.out.println("Hello " + name + "!");
            }

        });
    }

    public static void helloSync(String... names) {
        for(String name : names) {
            System.out.println("Hello " + name + "!");
        }
    }

}
