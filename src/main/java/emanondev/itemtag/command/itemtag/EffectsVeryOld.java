package emanondev.itemtag.command.itemtag;

import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import emanondev.itemtag.EquipMethod;
import emanondev.itemtag.command.ItemTagCommand;

public class EffectsVeryOld extends EffectsBase {

	public EffectsVeryOld(ItemTagCommand cmd) {
		super(cmd);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void event(final InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		if (event.getWhoClicked().hasMetadata("NPC"))
			return;
		Player p = (Player) event.getWhoClicked();
		EquipmentSlot clickedSlot = getEquipmentSlotAtPosition(event.getRawSlot(), p, event.getView());

		switch (event.getAction()) {
		case CLONE_STACK:
		case NOTHING:
		case HOTBAR_MOVE_AND_READD:
			return;
		case DROP_ONE_CURSOR:
		case DROP_ALL_CURSOR:
			clickDrop.add(p);
			return;
		case DROP_ONE_SLOT:
			if (!isAirOrNull(event.getCursor()))
				return;
			if (clickedSlot != null && event.getCurrentItem().getAmount() == 1)
				onEquipChange(p, EquipMethod.INVENTORY_DROP, clickedSlot, event.getCurrentItem(), null);
			clickDrop.add(p);
			return;
		case DROP_ALL_SLOT:
			if (!isAirOrNull(event.getCursor()))
				return;
			if (clickedSlot != null)
				onEquipChange(p, EquipMethod.INVENTORY_DROP, clickedSlot, event.getCurrentItem(), null);

			clickDrop.add(p);
			return;
		case PICKUP_ALL:
			if (clickedSlot == null)
				return;
			onEquipChange(p, EquipMethod.INVENTORY_PICKUP, clickedSlot, event.getCurrentItem(), null);
			return;
		case PICKUP_HALF:
			if (clickedSlot == null)
				return;
			if (event.getCurrentItem().getAmount() > 1)
				return;
			onEquipChange(p, EquipMethod.INVENTORY_PICKUP, clickedSlot, event.getCurrentItem(), null);
			return;
		case PICKUP_ONE:
			if (clickedSlot == null)
				return;
			if (event.getCurrentItem().getAmount() != 1)
				return;
			onEquipChange(p, EquipMethod.INVENTORY_PICKUP, clickedSlot, event.getCurrentItem(), null);
			return;
		case PLACE_ALL:
		case PLACE_ONE:
		case PLACE_SOME:
			if (clickedSlot == null)
				return;
			if (isAirOrNull(event.getCurrentItem()))
				onEquipChange(p, EquipMethod.INVENTORY_PLACE, clickedSlot, null, event.getCursor());
			return;
		case SWAP_WITH_CURSOR:
			if (clickedSlot == null)
				return;
			if (isSimilarIgnoreDamage(event.getCurrentItem(), event.getCursor()))
				return;
			onEquipChange(p, EquipMethod.INVENTORY_SWAP_WITH_CURSOR, clickedSlot, event.getCurrentItem(),
					event.getCursor());
			return;
		case HOTBAR_SWAP: {
			ItemStack to = p.getInventory().getItem(event.getHotbarButton() == -1 ? 45 : event.getHotbarButton());
			if (isSimilarIgnoreDamage(event.getCurrentItem(), to))
				return;
			if (clickedSlot != null)
				onEquipChange(p, EquipMethod.INVENTORY_HOTBAR_SWAP, clickedSlot, event.getCurrentItem(), to);
			else if (event.getHotbarButton() == p.getInventory().getHeldItemSlot())
				onEquipChange(p, EquipMethod.INVENTORY_HOTBAR_SWAP, EquipmentSlot.HAND, to, event.getCurrentItem());
			return;
		}
		case MOVE_TO_OTHER_INVENTORY: {
			EquipmentSlot slot = event.getView().getTopInventory().getType() == InventoryType.CRAFTING
					? guessDispenserSlotType(event.getCurrentItem())
					: null;
			if (slot != null && isAirOrNull(getEquip(p, slot)))
				onEquipChange(p, EquipMethod.INVENTORY_MOVE_TO_OTHER_INVENTORY, slot, null, event.getCurrentItem());
			if (clickedSlot == null || clickedSlot == EquipmentSlot.HAND)
				new SlotCheck(p, EquipMethod.INVENTORY_MOVE_TO_OTHER_INVENTORY, EquipmentSlot.HAND)
						.runTaskLater(getPlugin(), 1L);
			else
				new SlotCheck(p, EquipMethod.INVENTORY_MOVE_TO_OTHER_INVENTORY, clickedSlot).runTaskLater(getPlugin(),
						1L);
			return;
		}
		case COLLECT_TO_CURSOR:
			ArrayList<EquipmentSlot> slots = new ArrayList<>();
			if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING)
				for (EquipmentSlot slot : EquipmentSlot.values()) {
					if (event.getCursor().isSimilar(getEquip(p, slot)))
						slots.add(slot);
				}
			else if (event.getCursor().isSimilar(getEquip(p, EquipmentSlot.HAND)))
				slots.add(EquipmentSlot.HAND);
			if (slots.size() > 0)
				new SlotCheck(p, EquipMethod.INVENTORY_COLLECT_TO_CURSOR, slots).runTaskLater(getPlugin(), 1L);
			return;
		case PICKUP_SOME:
		case UNKNOWN:
			return; // ???
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void event(@SuppressWarnings("deprecation") PlayerPickupItemEvent event) {
		if (event.getPlayer().hasMetadata("NPC"))
			return;

		if (!isAirOrNull(getEquip(event.getPlayer(), EquipmentSlot.HAND)))
			return;
		for (int i = 0; i < event.getPlayer().getInventory().getHeldItemSlot(); i++)
			if (isAirOrNull(event.getPlayer().getInventory().getItem(i)))
				return;
		new SlotCheck(event.getPlayer(), EquipMethod.PICKUP, EquipmentSlot.HAND).runTaskLater(getPlugin(), 1L);
	}

	@SuppressWarnings("deprecation")
	public boolean isSimilarIgnoreDamage(ItemStack item, ItemStack item2) {
		if (isAirOrNull(item))
			if (isAirOrNull(item2))
				return true;
			else
				return false;
		if (isAirOrNull(item2))
			return false;
		if (item.isSimilar(item2))
			return true;
		if (item.getType() != item2.getType())
			return false;
		ItemMeta meta1 = item.getItemMeta();
		ItemMeta meta2 = item2.getItemMeta();
		if (isUbreakable(meta1) || isUbreakable(meta2))
			return false;
		ItemStack itemCopy = item.clone();
		ItemStack itemCopy2 = item2.clone();
		itemCopy.setDurability(itemCopy2.getDurability());
		return itemCopy.isSimilar(itemCopy2);
	}

	private boolean isUbreakable(ItemMeta meta) {
		return meta.serialize().containsKey("Unbreakable");
	}
}