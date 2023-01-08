/*
 * Copyright (c) 2017 dmfs GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smoothsync.smoothsetup.microfragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import com.smoothsync.smoothsetup.R;
import com.smoothsync.smoothsetup.autocomplete.AbstractAutoCompleteAdapter;
import com.smoothsync.smoothsetup.autocomplete.AutoCompleteArrayIterable;
import com.smoothsync.smoothsetup.services.SetupChoiceService;
import com.smoothsync.smoothsetup.setupbuttons.BasicButtonViewHolder;
import com.smoothsync.smoothsetup.utils.AccountDetails;
import com.smoothsync.smoothsetup.utils.AfterTextChangedFlowable;
import com.smoothsync.smoothsetup.utils.AutoCompleteIterable;
import com.smoothsync.smoothsetup.utils.Domain;
import com.smoothsync.smoothsetup.utils.FlatMapFirst;
import com.smoothsync.smoothsetup.utils.ServiceBinder;

import org.dmfs.android.microfragments.FragmentEnvironment;
import org.dmfs.android.microfragments.MicroFragmentEnvironment;
import org.dmfs.android.microfragments.transitions.ForwardTransition;
import org.dmfs.android.microfragments.transitions.Swiped;
import org.dmfs.android.microwizard.MicroWizard;
import org.dmfs.iterables.EmptyIterable;
import org.dmfs.jems.optional.Optional;
import org.dmfs.jems.optional.elementary.Present;
import org.dmfs.jems.single.elementary.Collected;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.procedure.ForEach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * A generic Login form with a text edit field and one or more buttons.
 */
public final class GenericLoginFragment extends Fragment
{
    private AutoCompleteTextView mLogin;
    private MicroFragmentEnvironment<Params> mMicroFragmentEnvironment;
    private CompositeDisposable mDisposable;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mMicroFragmentEnvironment = new FragmentEnvironment<>(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View result = inflater.inflate(R.layout.smoothsetup_microfragment_login, container, false);
        Flowable<SetupChoiceService> setupChoiceService = ServiceBinder.<SetupChoiceService>localService(getContext(),
            mMicroFragmentEnvironment.microFragment().parameter().setupChoicesService()).cache();

        mLogin = result.findViewById(android.R.id.input);
        mLogin.setAdapter(new AutoCompleteAdapter(domain -> setupChoiceService.compose(new FlatMapFirst<>(s -> s.autoComplete(domain)))));
        mLogin.setOnItemClickListener((parent, view, position, id) -> mLogin.post(() ->
        {
            // an autocomplete item has been clicked, trigger autocomplete once again by setting the same text.
            int start = mLogin.getSelectionStart();
            int end = mLogin.getSelectionEnd();
            mLogin.setText(mLogin.getText());
            mLogin.setSelection(start, end);
        }));

        RecyclerView list = result.findViewById(android.R.id.list);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        list.setLayoutManager(llm);
        list.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        final Adapter adapter = new Adapter();
        list.setAdapter(adapter);
        mDisposable = new CompositeDisposable();

        mDisposable.add(
            new AfterTextChangedFlowable(mLogin)
                .map(login -> new Domain(login).value())
                .publish(publishedItems -> publishedItems.take(1)
                    .concatWith(publishedItems.skip(1).debounce(item -> (item.isEmpty() ? Flowable.empty() : Flowable.timer(500, TimeUnit.MILLISECONDS)))))
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                .switchMap(domain -> setupChoiceService.compose(new FlatMapFirst<>(scs -> scs.choices(domain))))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    adapter::update,
                    error -> Log.e(this.getClass().getName(), "Discovery Error", error)));

        mDisposable.add(adapter.clicks().subscribe(
            choice ->
                mMicroFragmentEnvironment.host()
                    .execute(getContext(),
                        new Swiped(
                            new ForwardTransition<>(
                                choice.nextStep(
                                    mMicroFragmentEnvironment.microFragment().parameter().next(),
                                    new Present<>(mLogin.getText().toString())).value(getActivity()))))));

        return result;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        // trigger button update
        mLogin.post(() ->
        {
            int start = mLogin.getSelectionStart();
            int end = mLogin.getSelectionEnd();
            mLogin.setText(mLogin.getText());
            mLogin.setSelection(start, end);
        });
    }


    @Override
    public void onDestroyView()
    {
        mDisposable.dispose();
        super.onDestroyView();
    }


    public interface Params
    {
        @NonNull
        Optional<String> username();

        @NonNull
        MicroWizard<AccountDetails> next();

        @NonNull
        Intent setupChoicesService();
    }


    public final static class Adapter extends RecyclerView.Adapter<BasicButtonViewHolder>
    {
        private final static int VIEWTYPE_PRIMARY = 0;
        private final static int VIEWTYPE_SECONDARY = 1;

        private final List<SetupChoiceService.SetupChoice> choices = new ArrayList<>();
        private final PublishProcessor<SetupChoiceService.SetupChoice> clickProcessor = PublishProcessor.create();


        @Override
        public int getItemViewType(int position)
        {
            return choices.get(position).isPrimary() ? VIEWTYPE_PRIMARY : VIEWTYPE_SECONDARY;
        }


        @NonNull
        @Override
        public BasicButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                viewType == VIEWTYPE_PRIMARY ?
                    R.layout.smoothsetup_button_provider :
                    R.layout.smoothsetup_button_other, parent, false);

            return new BasicButtonViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull BasicButtonViewHolder holder, int position)
        {
            holder.updateText(choices.get(position).title());
            holder.updateOnClickListener(view -> clickProcessor.onNext(choices.get(position)));
        }


        @Override
        public int getItemCount()
        {
            return choices.size();
        }


        public void update(@NonNull Iterable<SetupChoiceService.SetupChoice> setupChoices)
        {
            choices.clear();
            new ForEach<>(setupChoices).process(choices::add);

            notifyDataSetChanged();
        }


        @NonNull
        public Flowable<SetupChoiceService.SetupChoice> clicks()
        {
            return clickProcessor.hide();
        }
    }


    public static final class AutoCompleteAdapter extends AbstractAutoCompleteAdapter implements Filterable
    {
        private final static int MAX_RESULTS = 5;

        private final Filter mFilter = new ResultFilter();
        private final List<AutoCompleteItem> mValues;
        private final Function<String, Flowable<Iterable<String>>> mAutoCompleter;


        /**
         *
         */
        public AutoCompleteAdapter(Function<String, Flowable<Iterable<String>>> autoCompleter)
        {
            mAutoCompleter = autoCompleter;
            mValues = Collections.synchronizedList(new ArrayList<>(MAX_RESULTS));
        }


        @Override
        public int getCount()
        {
            return mValues.size();
        }


        @Override
        public AutoCompleteItem getItem(int position)
        {
            return mValues.get(position);
        }


        @Override
        public Filter getFilter()
        {
            return mFilter;
        }


        private final class ResultFilter extends Filter
        {
            @Override
            protected FilterResults performFiltering(final CharSequence prefix)
            {
                FilterResults results = new FilterResults();

                // by default we don't have any results;
                results.values = Collections.emptyList();
                results.count = 0;

                if (prefix == null || prefix.length() == 0)
                {
                    // no prefix, no results
                    return results;
                }

                String prefixStr = prefix.toString();
                final int atPos = prefixStr.indexOf('@');
                if (atPos < 0 || atPos == prefixStr.length() - 1)
                {
                    // no domain, no results either
                    return results;
                }
                String localPart = prefixStr.substring(0, atPos);
                String domainPart = prefixStr.substring(atPos + 1);

                // fetch the auto-complete result
                List<String> autoCompleteResult = mAutoCompleter.value(domainPart)
                    .lastElement()
                    .map(i -> new Collected<>(ArrayList::new, i).value())
                    .blockingGet();

                if (autoCompleteResult == null)
                {
                    // no result, no results either
                    return results;
                }

                List<String> values = new Collected<>(ArrayList::new, new AutoCompleteArrayIterable(autoCompleteResult, localPart, domainPart)).value();

                if (values.contains(prefixStr))
                {
                    // don't show autocomplete if we have an exact result
                    return results;
                }

                if (values.size() <= MAX_RESULTS)
                {
                    List<AutoCompleteItem> result = new ArrayList<>(values.size());
                    for (String value : values)
                    {
                        result.add(new AutoCompleteItem()
                        {
                            @Override
                            public String autoComplete()
                            {
                                return value;
                            }


                            @Override
                            public Iterable<String> extensions()
                            {
                                return EmptyIterable.instance();
                            }
                        });
                    }
                    results.values = result;
                    results.count = result.size();
                }
                else
                {
                    // try to find common prefixes, first sort the list
                    Collections.sort(values);
                    List<AutoCompleteItem> result = new ArrayList<>(values.size());
                    for (AutoCompleteItem item : new AutoCompleteIterable(values, prefixStr))
                    {
                        result.add(item);
                    }
                    results.values = result;
                    results.count = result.size();

                }

                return results;
            }


            @Override
            public CharSequence convertResultToString(Object resultValue)
            {
                return ((AutoCompleteItem) resultValue).autoComplete();
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                mValues.clear();

                if (results != null && results.values != null)
                {
                    mValues.addAll((List<AutoCompleteItem>) results.values);
                }

                notifyDataSetChanged();
            }
        }

    }
}
