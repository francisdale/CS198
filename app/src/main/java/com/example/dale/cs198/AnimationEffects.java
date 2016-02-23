package com.example.dale.cs198;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by DALE on 2/23/2016.
 */
public class AnimationEffects {

    public static void slide_down(Context context, View view){
        Animation a = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        if(a != null){
            a.reset();
            if(view != null){
                view.clearAnimation();
                view.startAnimation(a);
            }
        }
    }

    public static void slide_up(Context context, View view){
        Animation a = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        if(a != null){
            a.reset();
            if(view != null){
                view.clearAnimation();
                view.startAnimation(a);
            }
        }
    }

}
