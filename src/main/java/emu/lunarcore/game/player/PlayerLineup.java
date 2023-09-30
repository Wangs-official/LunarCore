package emu.lunarcore.game.player;

import java.util.ArrayList;
import java.util.List;

import dev.morphia.annotations.Entity;
import emu.lunarcore.GameConstants;
import emu.lunarcore.game.avatar.GameAvatar;
import emu.lunarcore.proto.ExtraLineupTypeOuterClass.ExtraLineupType;
import emu.lunarcore.proto.LineupInfoOuterClass.LineupInfo;
import lombok.Getter;

@Entity(useDiscriminator = false) @Getter
public class PlayerLineup {
    private transient Player owner;
    private transient int index;
    private transient int extraLineupType;

    private String name;
    private List<Integer> avatars;

    @Deprecated // Morphia only!
    public PlayerLineup() {

    }
    
    public PlayerLineup(int index) {
        this(index, 0);
    }

    public PlayerLineup(int index, int extraLineupType) {
        this.extraLineupType = extraLineupType;
        this.avatars = new ArrayList<>(GameConstants.MAX_AVATARS_IN_TEAM);
        
        // Set team name if not an extra lineup
        if (!this.isExtraLineup()) {
            this.name = "Squad " + (index + 1);
        } else {
            this.name = "";
        }
    }

    protected void setOwnerAndIndex(Player player, int index) {
        this.owner = player;
        this.index = index;
    }
    
    public boolean isExtraLineup() {
        return this.extraLineupType != 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return avatars.size();
    }

    public boolean contains(GameAvatar avatar) {
        return getAvatars().contains(avatar.getAvatarId());
    }

    public boolean addAvatar(GameAvatar avatar) {
        if (contains(avatar)) {
            return false;
        }

        getAvatars().add(avatar.getAvatarId());

        return true;
    }

    public boolean removeAvatar(int slot) {
        if (size() <= 1) {
            return false;
        }

        getAvatars().remove(slot);

        return true;
    }

    public LineupInfo toProto() {
        var proto = LineupInfo.newInstance()
                .setIndex(index)
                .setName(this.getName())
                .setLeaderSlot(this.getOwner().getLineupManager().getCurrentLeader())
                .setMp(this.getOwner().getLineupManager().getMp())
                .setMaxMp(GameConstants.MAX_MP)
                .setExtraLineupType(ExtraLineupType.LINEUP_NONE);

        for (int slot = 0; slot < this.getAvatars().size(); slot++) {
            GameAvatar avatar = owner.getAvatars().getAvatarById(getAvatars().get(slot));
            if (avatar == null) continue;

            proto.addAvatarList(avatar.toLineupAvatarProto(slot));
        }

        return proto;
    }
}
