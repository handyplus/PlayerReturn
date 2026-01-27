package cn.handyplus.pln.command.player;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.lib.util.MessageUtil;
import cn.handyplus.pln.api.PlayerReturnApi;
import cn.handyplus.pln.inventory.TaskGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * 回归任务
 *
 * @author handy
 */
public class TaskCommand implements IHandyCommandEvent {

    @Override
    public String command() {
        return "task";
    }

    @Override
    public String permission() {
        return "playerReturn.task";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = AssertUtil.notPlayer(sender, BaseUtil.getLangMsg("noPlayerFailureMsg"));
        // 判断是否回归
        if (!PlayerReturnApi.isReturnPlayer(player.getName())) {
            MessageUtil.sendMessage(player, BaseUtil.getLangMsg("noReturnMsg"));
            return;
        }
        Inventory inventory = TaskGui.getInstance().createGui(player);
        PlayerSchedulerUtil.syncOpenInventory(player, inventory);
    }

}