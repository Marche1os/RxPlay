package com.example.rx

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.rx.databinding.FragmentSecondBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import io.reactivex.rxjava3.subjects.UnicastSubject
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            publishSubjectBtn.setOnClickListener { publishSubject() }
            replaySubjectBtn.setOnClickListener { replaySubject() }
            behaviorSubjectBtn.setOnClickListener { behaviorSubject() }
            asyncSubjectBtn.setOnClickListener { asyncSubject() }
            unicastSubjectBtn.setOnClickListener { unicastSubject() }
            serializedSubjectBtn.setOnClickListener { safeSummatorWithSerializedSubject() }
        }
    }

    //only a single observer allowed
    private fun unicastSubject() {
        val (firstObserver, secondObserver) = createPairOfObservers<Long>()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
            .take(20)
        val subject = UnicastSubject.create<Long>()

        observable.subscribe(subject)

        postDelayed({
            subject.subscribe(firstObserver)
        }, 2500)

        postDelayed({
            subject.subscribe(secondObserver)
        }, 4500)

        postDelayed({
            subject.subscribe(secondObserver)
        }, 7500)
    }

    private fun serializedSubject() {
        val subject = PublishSubject.create<Long>()

        val consumer = object : Consumer<Long> {
            var sum = 0L

            override fun accept(t: Long) {
                sum += t
            }

            fun toStr(): String {
                return "sum = $sum"
            }
        }

        subject.subscribe(consumer)

        thread {
            for (i in 0..100_000) {
                subject.onNext(1L)
            }
        }

        thread {
            for (i in 0..100_000) {
                subject.onNext(1L)
            }
        }

        postDelayed({
            Log.d("LOG_TAG", consumer.toStr())
        }, 2000)
    }

    private fun safeSummatorWithSerializedSubject() {
        val subject = PublishSubject.create<Long>().toSerialized()

        val consumer = object : Consumer<Long> {
            var sum = 0L

            override fun accept(t: Long) {
                sum += t
            }

            fun toStr(): String {
                return "sum = $sum"
            }
        }

        subject.subscribe(consumer)

        thread {
            for (i in 0 until 100_000) {
                subject.onNext(1L)
            }
        }

        thread {
            for (i in 0 until 100_000) {
                subject.onNext(1L)
            }
        }

        postDelayed({
            Log.d("LOG_TAG", consumer.toStr())
        }, 2000)
    }

    private fun asyncSubject() {
        val (firstObserver, secondObserver) = createPairOfObservers<Long>()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
            .take(4)
        val subject = AsyncSubject.create<Long>()

        observable.subscribe(subject)

        postDelayed({
            subject.subscribe(firstObserver)
        }, 1500)

        postDelayed({
            subject.subscribe(secondObserver)
        }, 7500)
    }

    private fun behaviorSubject() {
        val (firstObserver, secondObserver) = createPairOfObservers<Long>()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
            .take(10)

        val subject = BehaviorSubject.createDefault(-1L)

        subject.subscribe(secondObserver)

        postDelayed({
            observable.subscribe(subject)
        }, 2000)

        postDelayed({
            subject.subscribe(firstObserver)
        }, 7500)
    }

    private fun replaySubject() {
        val observer1 = createObserver<Long>(onNext = { nextValue -> logOnNext(value = nextValue) })
        val observer2 = createObserver<Long>(onNext = { nextValue ->
            logOnNext(
                source = "2",
                value = nextValue
            )
        })

        val observable = Observable.interval(1, TimeUnit.SECONDS)
            .take(10)

        val subject = ReplaySubject.create<Long>()

        observable.subscribe(subject)

        postDelayed({
            subject.subscribe(observer1)
        }, 3500)

        postDelayed({
            subject.subscribe(observer2)
        }, 5500)

        postDelayed({
            subject.onNext(1996)
        }, 7500)
    }

    private fun publishSubject() {
        val observer1 = createObserver<Long>(onNext = { nextValue -> logOnNext(value = nextValue) })

        val observer2 = createObserver<Long>(onNext = { nextValue ->
            logOnNext(
                source = "2",
                value = nextValue
            )
        })

        val observable = Observable.interval(1, TimeUnit.SECONDS)
            .take(10)

        val subject = PublishSubject.create<Long>()

        observable.subscribe(subject)

        postDelayed({
            subject.subscribe(observer1)
        }, 3500)

        postDelayed({
            subject.subscribe(observer2)
        }, 5500)

        postDelayed({
            subject.onNext(1996)
        }, 7500)
    }

    private fun <T : Any> createPairOfObservers(): Pair<Observer<T>, Observer<T>> {
        val firstObserver =
            createObserver<T>(onNext = { nextValue -> logOnNext(value = nextValue) })
        val secondObserver = createObserver<T>(onNext = { nextValue ->
            logOnNext(source = "2", value = nextValue)
        })

        return Pair(first = firstObserver, second = secondObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

fun Fragment.postDelayed(r: Runnable, delayMs: Long) {
    requireActivity().window.decorView.postDelayed(r, delayMs)
}

fun logOnNext(source: String = "1", value: Any) {
    Log.d("LOG_TAG", "$source => $value")
}