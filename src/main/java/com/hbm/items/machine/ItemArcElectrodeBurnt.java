package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

public class ItemArcElectrodeBurnt extends ItemEnumMulti {

    public ItemArcElectrodeBurnt(String s) {
        super(s, ItemArcElectrode.EnumElectrodeType.class, true, true);
        this.setFull3D();
    }
}
