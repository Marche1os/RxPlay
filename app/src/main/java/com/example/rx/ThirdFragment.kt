package com.example.rx

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rx.databinding.FragmentThirdBinding
import io.reactivex.rxjava3.core.Observable
import rx_api.createObservable

class ThirdFragment : Fragment() {

    private var _binding: FragmentThirdBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThirdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
    }

    private fun setClickListeners() {
        with(binding) {
            onErrorReturnBtn.setOnClickListener { onErrorReturn() }
            onErrorResumeNextBtn.setOnClickListener { onErrorResumeNext() }
            retryBtn.setOnClickListener { retry() }
            retryWhenBtn.setOnClickListener { retryWhen() }
            retryWhenWithErrorBtn.setOnClickListener { retryWhenWithError() }
        }
    }

    private fun onErrorReturn() {
        val observable = createObservable { emitter ->
            for (i in 0 until 100) {
                emitter.onNext(i)
            }
            emitter.onError(Throwable("just error"))
            emitter.onNext(-1)
        }.onErrorReturn { error ->
            Log.d("LOG_TAG", "error: $error")
            1996
        }

        val observer = createObserver<Int>()
        observable.subscribe(observer)
    }

    private fun onErrorResumeNext() {
        val observable = createObservable { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onNext(3)
            emitter.onError(Throwable("some error"))
        }.onErrorResumeWith(Observable.just(4, 5, 6, 7, 8, 9))

        val observer = createObserver<Int>()
        observable.subscribe(observer)
    }

    private fun retry() {
        val observable = createObservable { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onNext(4)
            emitter.onError(Throwable("throwable"))
        }.retry(2)
        val observer = createObserver<Int>()
        observable.subscribe(observer)
    }

    private fun retryWhen() {
        val dataSource = Observable.just("1", "2", "c", "4", "5")

        val retryObserver = createObserver<Long>(onNext = { nextValue ->
            logOnNext(
                source = "retry",
                value = nextValue
            )
        })

        val longObservable: Observable<Long> = dataSource.map { str ->
            str.toLong()
        }.retryWhen { sourceWithThrowable ->
            Log.d("LOG_TAG", "retryWhen")
            sourceWithThrowable.take(3)
        }
        longObservable.subscribe(retryObserver)
    }

    private fun retryWhenWithError() {
        val dataSource = Observable.just("1", "2", "c", "4", "5")
        val observableMain: Observable<Long> = dataSource
            .map { item ->
                item.toLong()
            }.retryWhen { observableError ->
                observableError.zipWith(Observable.range(1, 3)) { error, item ->
                    if (item < 3) {
                        Observable.just(1996L)
                    } else {
                        Observable.error(error)
                    }
                }.flatMap { observableRetry ->
                    observableRetry
                }
            }
        observableMain.subscribe(createObserver())
    }

    private fun logError(error: Throwable) {
        Log.d("LOG_TAG", "error -> $error")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}