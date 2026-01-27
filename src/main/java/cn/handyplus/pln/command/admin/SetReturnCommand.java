package cn.handyplus.pln.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.constants.BaseConstants;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.event.PlayerReturnEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 设置回归
 *
 * @author handy
 */
public class SetReturnCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "setReturn";
    }

    @Override
    public String permission() {
        return "playerReturn.setReturn";
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 参数是否正常
        AssertUtil.notTrue(args.length < 1, BaseUtil.getLangMsg("paramFailureMsg"));
        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("failureMsg"));
            return;
        }
        // 发送玩家回归事件
        int returnDay = BaseConstants.CONFIG.getInt("returnDay");
        Bukkit.getServer().getPluginManager().callEvent(new PlayerReturnEvent(player, returnDay));
        MessageUtil.sendMessage(sender, BaseUtil.getLangMsg("succeedMsg"));
    }

}