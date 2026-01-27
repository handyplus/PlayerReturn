package cn.handyplus.pln.command.admin;

import cn.handyplus.lib.command.IHandyCommandEvent;
import cn.handyplus.lib.internal.PlayerSchedulerUtil;
import cn.handyplus.lib.util.AssertUtil;
import cn.handyplus.lib.util.BaseUtil;
import cn.handyplus.pln.inventory.ViewReturnGui;
import cn.handyplus.pln.inventory.ViewShopGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * 管理员进行查看信息
 *
 * @author handy
 */
public class ViewCommand implements IHandyCommandEvent {
    @Override
    public String command() {
        return "view";
    }

    @Override
    public String permission() {
        return "playerReturn.view";
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, Command command, String s, String[] args) {
        // 参数是否正常
        AssertUtil.notTrue(args.length < 2, BaseUtil.getLangMsg("paramFailureMsg"));
        // 是否为玩家
        AssertUtil.notPlayer(sender, BaseUtil.getLangMsg("noPlayerFailureMsg"));
        Player player = (Player) sender;
        Inventory inventory;
        switch (args[1]) {
            case "return":
                inventory = ViewReturnGui.getInstance().createGui(player);
                break;
            case "shop":
                inventory = ViewShopGui.getInstance().createGui(player);
                break;
            default:
                return;
        }
        if (inventory == null) {
            return;
        }
        PlayerSchedulerUtil.syncOpenInventory(player, inventory);
    }

}