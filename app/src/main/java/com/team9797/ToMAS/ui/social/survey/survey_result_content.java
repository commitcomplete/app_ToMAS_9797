package com.team9797.ToMAS.ui.social.survey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team9797.ToMAS.MainActivity;
import com.team9797.ToMAS.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class survey_result_content extends Fragment {

    MainActivity mainActivity;
    FragmentManager fragmentManager;
    String post_id;
    String path;
    TextView title_textView;
    TextView writer_textView;
    TextView due_date_textView;
    Button enroll_button;
    LinearLayout container_linearLayout;
    ArrayList<survey_content_customView> customView_list;
    DocumentReference mPostReference;

    // need to fix 댓글 보기 기능 추가해야 함.
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.survey_content, container, false);

        mainActivity = (MainActivity)getActivity();
        fragmentManager = getFragmentManager();
        post_id = getArguments().getString("post_id");
        path = getArguments().getString("path");

        // get View
        title_textView = root.findViewById(R.id.survey_content_title);
        writer_textView = root.findViewById(R.id.survey_content_writer);
        due_date_textView = root.findViewById(R.id.survey_content_due_date);
        enroll_button = root.findViewById(R.id.survey_content_enroll_button);
        container_linearLayout = root.findViewById(R.id.survey_content_container);

        customView_list = new ArrayList<>();

        // 선택한 게시물 document reference
        mPostReference = mainActivity.db.collection(path).document(post_id);
        // id를 바탕으로
        mPostReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        title_textView.setText(document.get("title", String.class));
                        due_date_textView.setText(document.get("due_date", String.class));
                        writer_textView.setText(document.get("writer", String.class));
                        mPostReference.collection("questions").orderBy("index").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int count = 1;
                                    for (QueryDocumentSnapshot tmp_document : task.getResult()) {
                                        int tmp_type = tmp_document.get("type", Integer.class);
                                        String item_question = tmp_document.get("question", String.class);
                                        survey_content_customView tmp_customView;
                                        if (tmp_type == 1)
                                        {
                                            tmp_customView = new survey_content_customView(mainActivity, null, tmp_type, count, item_question, (ArrayList<String>) tmp_document.get("multi_choice_questions"));
                                        }
                                        else
                                        {
                                            tmp_customView = new survey_content_customView(mainActivity, null, tmp_type, count, item_question, null);
                                        }
                                        customView_list.add(tmp_customView);
                                        container_linearLayout.addView(tmp_customView, count + 1);
                                        count++;
                                    }
                                }
                                else {

                                }
                            }
                        });


                    } else {

                    }
                } else {

                }
            }
        });

        enroll_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> post = new HashMap<>();
                for (int i = 0; i < customView_list.size(); i++)
                {
                    if (customView_list.get(i).question_type == 1)
                    {// 객관식
                        // 이게 너무 느리면 radiobutton 을 extend한 class 생성해서 할 것.
                        int tmp_radioButtonID = customView_list.get(i).type1_radioGroup.getCheckedRadioButtonId();
                        RadioButton tmp_radioButton = customView_list.get(i).type1_radioGroup.findViewById(tmp_radioButtonID);
                        int tmp_idx = customView_list.get(i).type1_radioGroup.indexOfChild(tmp_radioButton);
                        post.put(Integer.toString(i+1), tmp_idx);
                    }
                    else
                    {// 주관식
                        post.put(Integer.toString(i+1), customView_list.get(i).type2_editText.getText().toString());
                    }
                }
                mPostReference.collection("submissions").document(mainActivity.getUid()).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d("AAA", "DocumentSnapshot successfully written!");
                        fragmentManager.beginTransaction().remove(survey_result_content.this).commit();
                        fragmentManager.popBackStack();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Log.w("AAA", "Error writing document", e);
                            }
                        });
            }
    });
        return root;
}


}