package com.hts.poc.cardswipetestproject;

import android.media.MediaPlayer;

import org.junit.Test;

import static org.junit.Assert.*;

public class CardReaderActivityTest {

    @Test
    public void add() {
        int a = 4;
        int b = 5;

        CardReaderActivity cardReaderActivity = new CardReaderActivity();
        assertEquals(9, cardReaderActivity.add(a, b));
    }


}