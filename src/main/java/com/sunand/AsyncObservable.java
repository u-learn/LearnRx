package com.sunand;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.util.async.Async;

/**
 * Hello world!
 *
 */
public class AsyncObservable {
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

        System.out.println("Time Taken Sync: " + (end - start));

    }

    public static void helloRx(String... names) {
        final Observable<String> observable = Observable.from(names);


        final Action1<String> onNext = new Action1<String>() {

            public void call(String name) {
                System.out.println("Hello " + name + "!");
            }

        };

        Observable.from(names).subscribe(onNext);

        final Func1<String, Observable<Void>> observableFunc1 = Async.toAsync(onNext);
        //observableFunc1.call(names[0]).

    }

    public static void helloSync(String... names) {
        for (String name : names) {
            System.out.println("Hello " + name + "!");
        }

    }
}
