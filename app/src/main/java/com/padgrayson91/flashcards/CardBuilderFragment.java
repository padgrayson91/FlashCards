package com.padgrayson91.flashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private EditText mQuestionEdit;
    private EditText mAnswerEdit;
    private EditText mOption1Edit;
    private EditText mOption2Edit;
    private EditText mOption3Edit;

    private String question;
    private String answer;
    private ArrayList<String>  options;

    public CardBuilderFragment (){
        options = new ArrayList<String>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_card_builder, container, false);
        mQuestionEdit = (EditText) root.findViewById(R.id.question_edit);
        mAnswerEdit = (EditText) root.findViewById(R.id.answer_edit);
        mOption1Edit = (EditText) root.findViewById(R.id.option_edit_1);
        mOption2Edit = (EditText) root.findViewById(R.id.option_edit_2);
        mOption3Edit = (EditText) root.findViewById(R.id.option_edit_3);
        return root;
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
            }

        }

        return result;
    }

    //TODO: should check if questions or answers are too long
    public int validate() {
        question = mQuestionEdit.getText().toString();
        answer = mAnswerEdit.getText().toString();
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
