package de.blinkt.openvpn;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;

import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;

public class OpenVpnApi {
    /**
     * @param inlineConfig 一般都保存在文件中，通过读取文件获取配置信息
     * @param userName     某些ovpn的连接方式需要用户名和密码，可以为空
     * @param pw           某些ovpn的连接方式需要用户名和密码，可以为空
     * @throws RemoteException
     */
    public static void startVpn(Context context, String inlineConfig, String userName, String pw) throws RemoteException {
        if (TextUtils.isEmpty(inlineConfig)) throw new RemoteException("config is empty");
        VpnService.prepare(context);
        startVpnInternal(context, inlineConfig, userName, pw);
    }

    private static void startVpnInternal(Context context, String inlineConfig, String userName, String pw) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(inlineConfig));
            VpnProfile vp = cp.convertProfile();// 解析.ovpn
            vp.mName = Build.MODEL;
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found) {
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
            vp.mPassword = pw;
            ProfileManager.setTemporaryProfile(vp);
            VPNLaunchHelper.startOpenVpn(vp, context);// 开启Vpn服务
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }
}
