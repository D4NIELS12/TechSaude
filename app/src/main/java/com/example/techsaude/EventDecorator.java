package com.example.techsaude;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

/**
 * Classe usada para marcar dias com uma bolinha colorida no calendário.
 */
public class EventDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Adiciona um ponto (bolinha) colorido abaixo da data
        view.addSpan(new com.prolificinteractive.materialcalendarview.spans.DotSpan(10, color));
        // Deixa o texto do dia preto para boa visibilidade
        view.addSpan(new ForegroundColorSpan(Color.BLACK));
    }
}
