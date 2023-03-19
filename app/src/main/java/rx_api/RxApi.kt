package rx_api

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe

class RxApi {

    companion object {

        @JvmStatic
        fun <T : Any> createObservable(source: ObservableOnSubscribe<T>): Observable<T> {
            return Observable.create(source)
        }
    }
}