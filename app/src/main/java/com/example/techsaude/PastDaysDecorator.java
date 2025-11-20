package com.example.techsaude;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

/**
 * Deixa os dias passados em cinza e desabilita o clique.
 */
public class PastDaysDecorator implements DayViewDecorator {

    private final Calendar today = Calendar.getInstance();

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Calendar date = Calendar.getInstance();
        date.set(day.getYear(), day.getMonth(), day.getDay());
        return date.before(today);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Cinza para dias passados
        view.addSpan(new ForegroundColorSpan(Color.parseColor("#B0B0B0")));
        view.setDaysDisabled(true);
    }
}

