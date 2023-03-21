package rx_api

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Any> createObservable(source: ObservableOnSubscribe<T>): Observable<T> {
    return Observable.create(source)
}

fun <T : Any> createObservableFromIterable(data: Iterable<T>): Observable<T> {
    return Observable.fromIterable(data)
}

fun <T : Any> createMergedObservables(
    first: Observable<T>,
    second: Observable<T>
): Observable<T> {
    return first.mergeWith(second)
}

fun <T : Any> Observable<T>.mergeTwoObservables(
    observable: Observable<T>
): Observable<T> {
    return mergeWith(observable)
}

fun <T1: Any, T2: Any, R: Any> zipTwoObservables(
    firstSource: Observable<T1>,
    secondSource: Observable<T2>,
    zipper: BiFunction<T1, T2, R>
): Observable<R> {
    return firstSource.zipWith(secondSource, zipper)
}

fun <T1: Any, T2: Any, R: Any> Observable<T1>.zipOnObservable(
    source: Observable<T2>,
    zipper: BiFunction<T1, T2, R>
): Observable<R> {
    return zipWith(source, zipper)
}

fun <T : Any> Observable<T>.schedule(
    onNext: Consumer<T>,
    onError: Consumer<Throwable>
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}