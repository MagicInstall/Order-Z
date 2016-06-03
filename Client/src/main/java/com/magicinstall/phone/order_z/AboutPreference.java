package com.magicinstall.phone.order_z;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wing on 16/4/30.
 */
public class AboutPreference extends Preference {
    private static final String TAG = "AboutPreference";

    /**
     * Constructor that is called when inflating a Preference from XML. This is
     * called when a Preference is being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     *                access the current theme, resources, {@link SharedPreferences},
     *                etc.
     * @param attrs   The attributes of the XML tag that is inflating the
     *                preference.
     * @see #Preference(Context, AttributeSet, int)
     */
    public AboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of Preference allows subclasses to use their own base style
     * when they are inflating. For example, a {@link CheckBoxPreference}
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * {@link CheckBoxPreference} class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #Preference(Context, AttributeSet)
     */
    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        setLayoutResource(R.layout.setting_server);

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.setting_about,
                parent, false);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView verstion_view = (TextView) view.findViewById(R.id.about_version);
        String app_verstion = ((MainApplication)(getContext().getApplicationContext())).getAppVerstionName();
        verstion_view.setText(
                getContext().getString(R.string.pref_about_app_verstion) +
                ":" + app_verstion);
    }


}
