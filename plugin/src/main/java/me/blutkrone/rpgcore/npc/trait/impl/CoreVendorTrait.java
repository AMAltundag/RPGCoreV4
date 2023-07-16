package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.item.EditorItemPrice;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorVendorTrait;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A vendor allows to trade one item for another, process
 * denomination directly.
 */
public class CoreVendorTrait extends AbstractCoreTrait {

    // offers from the vendor
    public List<VendorOffer> offers = new ArrayList<>();

    public CoreVendorTrait(EditorVendorTrait editor) {
        super(editor);

        for (IEditorBundle offer : editor.offers) {
            if (offer instanceof EditorItemPrice) {
                this.offers.add(new VendorOffer(((EditorItemPrice) offer)));
            }
        }
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        RPGCore.inst().getHUDManager().getVendorMenu().present(player, this);
    }

    public class VendorOffer {
        public String item;
        public String currency;
        public int price;

        public VendorOffer(EditorItemPrice price) {
            this.item = price.item.toLowerCase();
            this.currency = price.currency.toLowerCase();
            this.price = (int) price.price;
        }
    }
}