package com.example.rx

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rx.databinding.FragmentFirstBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import rx_api.createObservable
import rx_api.schedule
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val linksToTask = mutableListOf<Disposable>()

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
            init()
        }
    }

    fun init() {
        val observable: Observable<Int> = createObservable { emitter ->
            var i = 0
            while (i++ < 100) {
                emitter.onNext(i)
                TimeUnit.SECONDS.sleep(2)
            }
        }

        linksToTask.add(
            observable.schedule(
                onNext = { nextValue ->
                    binding.textviewFirst.text = nextValue.toString()
                },
                onError = { error ->
                    Log.e("LOG_TAG", "error:$error")
                })
        )
    }

    fun start() {
        val observable = Observable.fromIterable(listOf("one", "two", "three"))
        val observer = object : Observer<String> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
                Log.d("LOG_TAG", "onError:$e")
            }

            override fun onComplete() {
                Log.d("LOG_TAG", "onComplete")
            }

            override fun onNext(t: String) {
                Log.d("LOG_TAG", "onNext: $t")
            }
        }


        observable.subscribe(observer)
    }

    private fun range() {
        val observable = Observable.range(10, 4)
        val observer = object : Observer<Int> {
            override fun onSubscribe(d: Disposable) {
                Log.d("LOG_TAG", "onSubscribe")
            }

            override fun onError(e: Throwable) {
                Log.d("LOG_TAG", "onError:$e")
            }

            override fun onComplete() {
            }

            override fun onNext(t: Int) {
                Log.d("LOG_TAG", "onNext: $t")
            }
        }
        observable.subscribe(observer)
    }

    private fun interval() {
        val observable = Observable.interval(500, TimeUnit.MILLISECONDS)
        val observer = object : Observer<Long> {
            override fun onSubscribe(d: Disposable) {
                Log.d("LOG_TAG", "onSubscribe")
            }

            override fun onError(e: Throwable) {
                Log.d("LOG_TAG", "onError:$e")
            }

            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                Log.d("LOG_TAG", "onNext: $t")
            }
        }
        observable.subscribe(observer)
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
        linksToTask.forEach { action -> action.dispose() }
        _binding = null
    }
}