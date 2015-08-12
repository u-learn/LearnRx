package com.sunand;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.currentThread;

/**
 * Created by Sunand on 8/8/2015.
 */
public class SubscribeOnTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Integer[] a1 = {1, 2, 3, 4, 5};
        Integer[] a2 = {1, 2, 3, 4, 5};

        final Subscription subscribe = Observable.from(a1).flatMap(new Func1<Integer, Observable<Integer>>() {
            public Observable<Integer> call(Integer integer) {
                System.out.println(integer);
                //System.out.println(currentThread().getName() + " - " + currentThread().getId());
                return Observable.just(integer + 1).subscribeOn(Schedulers.computation());
            }
        }).subscribe(new Action1<Integer>() {
            public void call(Integer integer) {
                System.out.println("Value : " + integer);
            }
        });

        final Object waitMonitor = new Object();
        //synchronized (waitMonitor) {
        final Observable<Integer> observable = Observable.from(a2);

        observable.subscribeOn(Schedulers.io()).subscribe(new Subscriber<Integer>() {
            public void onCompleted() {
                System.out.println("Done");
                waitMonitor.notifyAll();
            }

            public void onError(Throwable throwable) {
                System.out.println("Error : " + throwable.toString());
            }

            public void onNext(Integer integer) {
                System.out.println("Balls : " + integer);
                System.out.println(currentThread().getName() + " - " + currentThread().getId());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //waitMonitor.wait();

        //}

        final List<Integer> single = observable.toList().toBlocking().single();
        System.out.println(single);
        System.out.println("Heya");

    }

    private static void didnotwork1(Integer[] a) {
        Observable.from(a).subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<Integer>() {
            public void onCompleted() {
                System.out.println("Done");
            }

            public void onError(Throwable throwable) {
                System.out.println("Whaaaaaaaaaat");
            }

            public void onNext(Integer integer) {
                System.out.println("Value : " + integer);
                System.out.println(currentThread().getName() + " - " + currentThread().getId());
            }
        });

        final Subscriber<Integer> done = new Subscriber<Integer>() {
            public void onCompleted() {
                System.out.println("Done");
            }

            public void onError(Throwable throwable) {

            }

            public void onNext(Integer integer) {
                System.out.println("ON nExt: " + integer);
            }
        };
    }
}
