package cn.handyplus.pln;

import cn.handyplus.lib.InitApi;
import cn.handyplus.lib.constants.HookPluginEnum;
import cn.handyplus.lib.util.HookPluginUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.hook.PlaceholderUtil;
import cn.handyplus.pln.listener.PlayerTaskScheduleEventListener;
import cn.handyplus.pln.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * 主类
 *
 * @author handy
 */
public class PlayerReturn extends JavaPlugin {
    public static PlayerReturn INSTANCE;
    public static boolean USE_PAPI;
    public static boolean USE_TASK;
    public static boolean USE_AUTH_ME;

    @Override
    public void onEnable() {
        INSTANCE = this;
        InitApi initApi = InitApi.getInstance(this);
        // 加载配置文件
        ConfigUtil.init();
        // 加载 PlaceholderApi
        USE_PAPI = HookPluginUtil.hook(HookPluginEnum.PLACEHOLDER_API);
        if (USE_PAPI) {
            new PlaceholderUtil(this).register();
        }
        // 加载 PlayerTask
        USE_TASK = HookPluginUtil.hook(HookPluginEnum.PLAYER_TASK);
        // 加载 authme
        USE_AUTH_ME = HookPluginUtil.hook(HookPluginEnum.AUTH_ME);
        // 查询是否开启 plk系统
        List<String> classList = new ArrayList<>();
        if (!USE_TASK) {
            classList.add(PlayerTaskScheduleEventListener.class.getName());
        }
        initApi.initCommand("cn.handyplus.pln.command")
                .initListener("cn.handyplus.pln.listener", classList)
                .initClickEvent("cn.handyplus.pln.listener.gui")
                .enableSql("cn.handyplus.pln.enter")
                .addMetrics(16638)
                .checkVersion();
        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "已成功载入服务器！");
        MessageUtil.sendConsoleMessage(ChatColor.GREEN + "author:handy WIKI: https://ricedoc.handyplus.cn/wiki/PlayerReturn/README/");
    }

    @Override
    public void onDisable() {
        InitApi.disable();
    }

}