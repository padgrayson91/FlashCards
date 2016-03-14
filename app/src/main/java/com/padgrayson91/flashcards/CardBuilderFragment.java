package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_ANSWER;
import static com.padgrayson91.flashcards.Constants.ERROR_EMPTY_QUESTION;
import static com.padgrayson91.flashcards.Constants.ERROR_NO_OPTIONS;
import static com.padgrayson91.flashcards.Constants.ERROR_WRITE_FAILED;
import static com.padgrayson91.flashcards.Constants.SUCCESS;

/**
 * Created by patrickgrayson on 3/10/16.
 */
public class CardBuilderFragment extends Fragment {
    private static final String TAG = "FlashCards";

    private EditText mQuestionEdit;
    private EditText mAnswerEdit;
    private EditText mOption1Edit;
    private EditText mOption2Edit;
    private EditText mOption3Edit;

    private static final String KEY_QUESTION = "question";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTIONS = "options";

    private String question;
    private String answer;
    private ArrayList<String>  options;
    private Storage mStorage;

    public CardBuilderFragment (){
        options = new ArrayList<String>();
        question = "";
        answer = "";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Creating card builder");
        View root = inflater.inflate(R.layout.fragment_card_builder, container, false);
        mQuestionEdit = (EditText) root.findViewById(R.id.question_edit);
        mAnswerEdit = (EditText) root.findViewById(R.id.answer_edit);
        mOption1Edit = (EditText) root.findViewById(R.id.option_edit_1);
        mOption2Edit = (EditText) root.findViewById(R.id.option_edit_2);
        mOption3Edit = (EditText) root.findViewById(R.id.option_edit_3);
        mStorage = new Storage(getActivity());
        initFields();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Activity created: " + savedInstanceState);
        mStorage = new Storage(getActivity());
        if (savedInstanceState != null){
            question = savedInstanceState.getString(KEY_QUESTION, "");
            answer = savedInstanceState.getString(KEY_ANSWER, "");
            Log.d(TAG, "Question: " + question + " Answer: " + answer);
            options = new ArrayList<>();
            options = savedInstanceState.getStringArrayList(KEY_OPTIONS);
        } else {
            question = mStorage.getInProgressQuestion();
            answer = mStorage.getInProgressAnswer();
            options = mStorage.getInPorgressOptions();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "RESUMING CARD BUILDER: " + question + " " + answer);
        initFields();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving state");
        outState.putString(KEY_ANSWER, mAnswerEdit.getText().toString());
        outState.putString(KEY_QUESTION, mQuestionEdit.getText().toString());
        options.add(mOption1Edit.getText().toString());
        options.add(mOption2Edit.getText().toString());
        options.add(mOption3Edit.getText().toString());
        outState.putStringArrayList(KEY_OPTIONS, options);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mStorage.storeInProgressQuestion(mQuestionEdit.getText().toString());
        mStorage.storeInProgressAnswer(mAnswerEdit.getText().toString());
        options = new ArrayList<>();
        options.add(mOption1Edit.getText().toString());
        options.add(mOption2Edit.getText().toString());
        options.add(mOption3Edit.getText().toString());
        mStorage.storeInProgressOptions(options);
        super.onPause();
    }

    public void initFields(){
        Log.d(TAG, "Setting data for fields");
        if(mQuestionEdit == null){
            //Could check all, but they are all initialized in the same place
            return;
        }
        if(!"".equals(question)) {
            Log.d(TAG, "Setting question text");
            mQuestionEdit.setText(question);
        } else {
            question = mQuestionEdit.getText().toString();
        }
        if(!"".equals(answer)) {
            mAnswerEdit.setText(answer);
        } else {
            answer = mAnswerEdit.getText().toString();
        }
        try {
            if(!"".equals(options.get(0))) {
                mOption1Edit.setText(options.get(0));
            } else {
                options.set(0, mOption1Edit.getText().toString());
            }
            if(!"".equals(options.get(1))) {
                mOption2Edit.setText(options.get(1));
            } else {
                options.set(1, mOption2Edit.getText().toString());
            }
            if(!"".equals(options.get(2))) {
                mOption3Edit.setText(options.get(2));
            } else {
                options.set(2, mOption3Edit.getText().toString());
            }
        } catch (IndexOutOfBoundsException ex){
            //don't really care because if there were no options then these should
            //stay blank
        }
    }

    public int validateAndSave(Deck d){
        int result = validate();
        if(result == SUCCESS){
            Card c = new Card(question.hashCode() + "");
            c.setQuestion(question);
            c.setAnswer(answer);
            c.setOptions(options);
            d.addCard(c);
            Storage storage = new Storage(getActivity());
            if(!storage.writeDeckToFile(d)){
                result = ERROR_WRITE_FAILED;
            } else {
                question = "";
                answer = "";
                options = new ArrayList<>();
                mQuestionEdit.setText("");
                mAnswerEdit.setText("");
                mOption1Edit.setText("");
                mOption2Edit.setText("");
                mOption3Edit.setText("");
                storage.clearInProgressCard();
            }

        }

        return result;
    }

    //TODO: should check if questions or answers are too long
    public int validate() {
        question = mQuestionEdit.getText().toString();
        answer = mAnswerEdit.getText().toString();
        options = new ArrayList<>();
        String option1 = mOption1Edit.getText().toString();
        String option2 = mOption2Edit.getText().toString();
        String option3 = mOption3Edit.getText().toString();
        if(option1 != null && !option1.equals("")){
            options.add(option1);
        }
        if(option2 != null && !option2.equals("")){
            options.add(option2);
        }
        if(option3 != null && !option3.equals("")){
            options.add(option3);
        }
        if (question == null || question.equals("")) {
            return ERROR_EMPTY_QUESTION;
        } else if (answer == null || answer.equals("")) {
            return ERROR_EMPTY_ANSWER;
        } else if (options.isEmpty()){
            return ERROR_NO_OPTIONS;
        }


        return SUCCESS;
    }
}
