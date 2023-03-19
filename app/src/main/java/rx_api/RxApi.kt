package rx_api

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

fun <T : Any> createObservable(source: ObservableOnSubscribe<T>): Observable<T> {
    return Observable.create(source)
}

fun <T : Any> Observable<T>.schedule(
    onNext: Consumer<T>,
    onError: Consumer<Throwable>
): Disposable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError)
}