package com.example.rx

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rx.databinding.FragmentFirstBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.observables.ConnectableObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import rx_api.createMergedObservables
import rx_api.createObservable
import rx_api.createObservableFromIterable
import rx_api.schedule
import rx_api.zipTwoObservables
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            binding.textviewFirst.text = null
        }
        createClickListeners()
    }

    private fun createClickListeners() {
        with(binding) {
            buttonNavigate.setOnClickListener {
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
            toErrorsPageBtn.setOnClickListener {
                findNavController().navigate(R.id.action_FirstFragment_to_thirdFragment)
            }

            takeUntilBtn.setOnClickListener { takeUntil() }
            zipBtn.setOnClickListener { zip() }
            mergeBtn.setOnClickListener { merge() }
            bufferBtn.setOnClickListener { buffer() }
            mapBtn.setOnClickListener { map() }
            fromCallableBtn.setOnClickListener { fromCallable() }
            intervalBtn.setOnClickListener { interval() }
            twoObservablesBtn.setOnClickListener { createTwoColdObservables() }
            twoObservablesHotBtn.setOnClickListener { createTwoHotObservables() }
            replayBtn.setOnClickListener { replay() }
            refCountBtn.setOnClickListener { refCount() }
            cacheBtn.setOnClickListener { cache() }
        }
    }

    private fun cache() {
        val observer1 = createObserver<Long>()

        val observer2 = createObserver<Long>(
            onNext = { nextValue ->
                Log.d("LOG_TAG", "onNext2 => $nextValue")
            }
        )

        val connectableObservable = Observable.interval(1, TimeUnit.SECONDS)
            .take(8)
            .cache()

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer1)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer2)
        }, 4500)
    }


    private fun refCount() {
        val observer1 = createObserver<Long>()

        val observer2 = createObserver<Long>(
            onNext = { nextValue ->
                Log.d("LOG_TAG", "onNext2 => $nextValue")
            }
        )

        val connectableObservable = Observable.interval(1, TimeUnit.SECONDS)
            .take(8)
            .publish()
            .refCount(1)

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer1)
        }, 2000)

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer2)
        }, 4500)
    }

    private fun replay() {
        val observer1 = createObserver<Long>()

        val observer2 = createObserver<Long>(
            onNext = { nextValue ->
                Log.d("LOG_TAG", "onNext2 => $nextValue")
            }
        )

        val connectableObservable: ConnectableObservable<Long> =
            Observable.interval(1, TimeUnit.SECONDS)
                .take(8)
                .replay()

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer1)
            connectableObservable.connect()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer2)
        }, 7000)
    }

    private fun createTwoHotObservables() {
        val observer1 = createObserver<Long>()

        val observer2 = createObserver<Long>(
            onNext = { nextVal ->
                Log.d("LOG_TAG", "onNext2 => $nextVal")
            }
        )

        val connectableObservable: ConnectableObservable<Long> =
            Observable.interval(1, TimeUnit.SECONDS)
                .take(8)
                .publish()

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer1)
            connectableObservable.connect()
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            connectableObservable.subscribe(observer2)
        }, 7000)
    }

    private fun createTwoColdObservables() {
        val observable1 = Observable.interval(1, TimeUnit.SECONDS)
            .take(5)

        val observer1 = createObserver<Long>()
        val observer2 = createObserver<Long>()

        Handler(Looper.getMainLooper()).postDelayed({
            observable1.subscribe(observer1)
        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            observable1.subscribe(observer2)
        }, 7000)
    }

    fun takeUntil() {
        val firstDecade: Observable<Int> =
            createObservableFromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

        val secondDecade: Observable<Int> =
            createObservableFromIterable(listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))

        val zipped = zipTwoObservables(firstDecade, secondDecade) { t1, t2 ->
            (t1 + t2)
        }

        val stopPredicate = Predicate<Int> { number -> number < 15 }

        zipped.takeUntil(stopPredicate)
            .schedule(
                onNext = { result ->
                    binding.textviewFirst.apply {
                        text = "$text $result"
                    }
                },
                onError = ::logError
            )
    }

    fun zip() {
        val firstDecade: Observable<Int> =
            createObservableFromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

        val secondDecade: Observable<Int> =
            createObservableFromIterable(listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))

        val zipped = zipTwoObservables(firstDecade, secondDecade) { t1, t2 ->
            (t1 + t2).toString()
        }

        zipped.schedule(
            onNext = { result ->
                binding.textviewFirst.apply {
                    text = "$text $result"
                }
            },
            onError = ::logError
        )
    }

    fun merge() {
        val firstDecade: Observable<Int> =
            createObservableFromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

        val secondDecade: Observable<Int> =
            createObservableFromIterable(listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))

        val completedSource = createMergedObservables(firstDecade, secondDecade)
        completedSource.schedule(
            onNext = { item ->
                binding.textviewFirst.apply {
                    text = "$text $item"
                }
            },
            onError = ::logError
        )
    }

    fun buffer() {
        val observable =
            createObservableFromIterable(listOf("0", "1", "2", "3", "4", "5", "6", "7", "8"))

        observable
            .buffer(4)
            .schedule(
                onNext = { items ->
                    val currentText = binding.textviewFirst.text

                    binding.textviewFirst.text = "$currentText ${items.joinToString()}\n"
                },
                onError = ::logError
            )
    }

    fun map() {
        val observable: Observable<String> = createObservable { emitter ->
            val currentDay = Calendar.getInstance().getDisplayName(
                Calendar.DAY_OF_WEEK,
                Calendar.LONG,
                Locale.getDefault()
            )
            emitter.onNext("Today is $currentDay.")
        }

        val disposable = observable.map { currentDay ->
            "$currentDay Time to go out"
        }.schedule(
            onNext = { item ->
                binding.textviewFirst.text = item
            },
            onError = ::logError
        )

        compositeDisposable.add(disposable)
    }

    private fun logError(error: Throwable) {
        Log.e("LOG_TAG", "error: $error")
    }

    private fun range() {
        val observable = Observable.range(10, 4)
        val observer = createObserver<Int>()
        observable.subscribe(observer)
    }

    private fun interval() {
        val observable = Observable.interval(500, TimeUnit.MILLISECONDS)
        val disposable = observable.schedule(
            onError = ::logError,
            onNext = { item ->
                binding.textviewFirst.apply {
                    text = "$text $item"
                }
            }
        )

        activity?.window?.decorView?.postDelayed(
            {
                disposable.dispose()
            }, 4500
        )
    }

    private fun fromCallable() {
        Observable.fromCallable { someHighLoadOperation() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun someHighLoadOperation() {
        TimeUnit.SECONDS.sleep(10)

        Log.d("LOG_TAG", "highLoadOperation finished.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        compositeDisposable.clear()
        _binding = null
    }
}

fun <T : Any> createObserver(
    onSubscribe: ((d: Disposable) -> Unit)? = null,
    onNext: ((t: T) -> Unit)? = null,
    onError: ((e: Throwable) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
): Observer<T> {
    return object : Observer<T> {
        override fun onSubscribe(d: Disposable) {
            onSubscribe?.invoke(d) ?: Log.d("LOG_TAG", "onSubscribe")
        }

        override fun onNext(t: T) {
            onNext?.invoke(t) ?: Log.d("LOG_TAG", "onNext => $t")
        }

        override fun onError(e: Throwable) {
            onError?.invoke(e) ?: Log.d("LOG_TAG", "onError => $e")
        }

        override fun onComplete() {
            onComplete?.invoke() ?: Log.d("LOG_TAG", "onComplete")
        }
    }
}