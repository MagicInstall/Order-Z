package com.magicinstall.phone.order_z;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.net.DatagramPacket;
import java.net.UnknownHostException;

/**
 * Created by wing on 16/4/30.
 */
public class ServerPreference extends Preference {
    private static final String TAG = "ServerPreference";

    private LinearLayout mLayout;

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
    public ServerPreference(Context context, AttributeSet attrs) {
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
    public ServerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        setLayoutResource(R.layout.setting_server);

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.setting_server,
                parent, false);
    }


    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mLayout = (LinearLayout) view.findViewById(R.id.setting_server_layout);

        // 作为客户端
        Button send_btn = (Button) view.findViewById(R.id.setting_send_button);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tttsend();
            }
        });

        // 作为服务端
        Button receive_btn = (Button) view.findViewById(R.id.setting_receive_button);
        receive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tttreceive();
            }
        });
    }

    // 客户端
    public void tttsend() {
        LanAutoSearch net = null;
        try {
            net = new LanAutoSearch(getContext(), "224.0.0.1"){
                /**
                 * 客户端收到一个服务端的应答的事件
                 * <p>哩个事件已经被放入主线程运行
                 *
                 * @param packet
                 */
                @Override
                public void onFoundServer(DatagramPacket packet) {
                    Log.w(TAG, "Test Thread!");
                }
            };
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        boolean s = net.startClient(13130, 13139, 5000);
        Log.i(TAG, "send ");
    }

    // 服务端
    public void tttreceive() {
        LanAutoSearch net = null;
        try {
            net = new LanAutoSearch(getContext(), "224.0.0.1") {
                /**
                 * 客户端收到一个服务端的应答的事件
                 * <p>哩个事件已经被放入主线程运行
                 *
                 * @param packet
                 */
                @Override
                public void onFoundServer(DatagramPacket packet) {

                }


            };
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        boolean s = net.startServer(13130, 13139);
        Log.i(TAG, "receive");
    }
}
