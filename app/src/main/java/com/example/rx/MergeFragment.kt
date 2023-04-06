package com.example.rx

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rx.databinding.FragmentMergeBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import rx_api.createObservable
import rx_api.createObservableFromIterable
import rx_api.schedule
import rx_api.zipOnObservable
import java.util.concurrent.TimeUnit

class MergeFragment : Fragment() {
    private var _binding: FragmentMergeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMergeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        with(binding) {
            mergeBtn.setOnClickListener { mergeOnSingleThread() }
            concatBtn.setOnClickListener { concat() }
            ambBtn.setOnClickListener { amb() }
            zipBtn.setOnClickListener { zip() }
            combineLatestBtn.setOnClickListener { combineLatest() }
            withLatestBtn.setOnClickListener { withLatestFrom() }
            flatMapBtn.setOnClickListener { flatMap() }
            concatMapBtn.setOnClickListener { concatMap() }
        }
    }

    /**
     * just merge elements that emitted by several observables into single
     */
    private fun merge() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        observable1.mergeWith(observable2)
            .subscribe(createObserver())
    }

    private fun mergeOnSingleThread() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        Observable.merge(
            listOf(
                observable1,
                observable2
            ),
            1
        ).subscribe(createObserver())
    }

    /**
     * concat it's a merge with maxConcurrency = 1
     */
    private fun concat() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        Observable.concat(observable1, observable2)
            .subscribe(createObserver())
    }

    /**
     * amb takes the first observable that emitted value and listen to it only
     */
    private fun amb() {
        val observable1 = Observable.interval(700, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        Observable.amb(
            listOf(
                observable1,
                observable2
            )
        ).subscribe(createObserver())
    }

    /**
     * takes the slower observable and makes transformation (A, B) -> R
     */
    private fun zip() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(15)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(15)
            .map { valueNumber -> valueNumber + 100 }

        observable1.zipOnObservable(observable2) { t1, t2 ->
            "$t1 and $t2"
        }.subscribe(createObserver())
    }

    /**
     * looks like zip, but don't wait the slower observable and emit every last element when newest one arrived
     */
    private fun combineLatest() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        Observable.combineLatest(observable1, observable2) { t1, t2 ->
            "$t1 and $t2"
        }.subscribe(createObserver())
    }

    /**
     * looks like zip, but has orientation to main observable in which the operator was attached
     */
    private fun withLatestFrom() {
        val observable1 = Observable.interval(300, TimeUnit.MILLISECONDS)
            .take(10)

        val observable2 = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(10)
            .map { valueNumber -> valueNumber + 100 }

        observable2.withLatestFrom(observable1) { t1, t2 ->
            "$t1 and $t2"
        }.subscribe(createObserver())
    }

    private fun flatMap() {
        val userGroups = UserGroup.generate()
        val userGroupObservable = Observable.just(userGroups)

        userGroupObservable.flatMap { group ->
            Observable.fromIterable(group.users)
        }.subscribe(createObserver())
    }

    /**
     * it's flatMap with maxConcurrent = 1
     */
    private fun concatMap() {
        val userGroups = UserGroup.generate()
        val userGroupObservable = Observable.just(userGroups)

        userGroupObservable.concatMap { group ->
            Observable.fromIterable(group.users)
        }.subscribe(createObserver())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class UserGroup(
    val users: List<String>
) {
    companion object {

        @JvmStatic
        fun generate(): UserGroup {
            return UserGroup(
                users = listOf(
                    "user1",
                    "user2",
                    "user3",
                    "user4",
                    "user5",
                )
            )
        }
    }
}