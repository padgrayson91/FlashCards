package com.padgrayson91.flashcards;

/**
 * Created by patrickgrayson on 3/10/16.
 * Single source for all static constants used in more than one class
 */
public class Constants {
    //ERROR CODES
    public static final int SUCCESS = 0;
    public static final int ERROR_DUPLICATE_NAME = 1;
    public static final int ERROR_WRITE_FAILED = 2;
    public static final int ERROR_EMPTY_NAME = 3;
    public static final int ERROR_EMPTY_QUESTION = 4;
    public static final int ERROR_EMPTY_ANSWER = 5;
    public static final int ERROR_NO_OPTIONS = 6;
    public static final int ERROR_NO_ID = 7;

    //Flashcard JSON keys
    public static final String KEY_ID = "card_id";
    public static final String KEY_QUESTION = "card_question";
    public static final String KEY_ANSWER = "card_answer";
    public static final String KEY_OPTIONS = "card_answer_choices";
    public static final String KEY_TIMES_CORRECT = "card_times_correct";
    public static final String KEY_TIMES_INCORRECT = "card_times_incorrect";
    public static final String KEY_CARD_TYPE = "card_type";
    public static final String KEY_CARD_CONTENTS = "card";
    public static final String KEY_LAST_CORRECT = "card_last_correct";

    //Deck JSON keys
    public static final String KEY_NAME = "deck_name";
    public static final String KEY_CARDS = "deck_cards";
    public static final String KEY_LAST_PLAYED = "last_played";
    public static final String KEY_OWNER = "owner";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_IS_PUBLIC = "is_public";

    //FLASHCARD_TYPE_CONSTANTS
    public static final int CARD_TYPE_MULTIPLE_CHOICE = 101;
    public static final int CARD_TYPE_FREE_RESPONSE = 102;
    public static final int CARD_TYPE_VARIABLE = 103;

    //Intent actions
    public static final String ACTION_BUILD_DECK = "com.padgrayson91.flashcards.action.ACTION_BUILD_DECK";

    //Bundle extras
    public static final String EXTRA_DECK_NAME = "deck_name";
    public static final String EXTRA_CARD_ID = "card_id";

}
