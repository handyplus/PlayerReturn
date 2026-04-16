package cn.handyplus.pln.util;

import cn.handyplus.lib.core.CollUtil;
import cn.handyplus.lib.core.StrUtil;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.RgbTextUtil;
import cn.handyplus.pln.constants.ReturnConstants;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 工具类
 *
 * @author handy
 */
public class ReturnUtil {

    /**
     * 回归提醒
     *
     * @param player 玩家
     */
    public static void setRemind(Player player) {
        String returnRemind = BaseUtil.getLangMsg("returnRemind");
        String returnSignInRemind = BaseUtil.getLangMsg("returnSignInRemind");
        String returnShopRemind = BaseUtil.getLangMsg("returnShopRemind");
        String returnTaskRemind = BaseUtil.getLangMsg("returnTaskRemind");
        RgbTextUtil message = RgbTextUtil.init(returnRemind);
        message.addExtra(RgbTextUtil.init(returnSignInRemind).addClickCommand(ReturnConstants.BAR + ReturnConstants.SIGN_IN));
        message.addExtra(RgbTextUtil.init(returnShopRemind).addClickCommand(ReturnConstants.BAR + ReturnConstants.SHOP));
        message.addExtra(RgbTextUtil.init(returnTaskRemind).addClickCommand(ReturnConstants.BAR + ReturnConstants.TASK));
        message.send(player);
    }

    /**
     * 执行命令
     *
     * @param player      玩家
     * @param commandList 命令
     */
    public static void command(Player player, List<String> commandList) {
        if (CollUtil.isEmpty(commandList)) {
            return;
        }
        for (String command : commandList) {
            if (StrUtil.isEmpty(command)) {
                continue;
            }
            command = command.replace("${player}", player.getName()).trim();
            PlayerSchedulerUtil.syncDispatchCommand(command);
        }
    }

}