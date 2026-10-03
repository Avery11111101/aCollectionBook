package tw.avery.collection.data;

import java.util.*;

public class PlayerData {

    private final UUID uuid;
    private final Set<String> unlockedMobs = new HashSet<>();
    private final Set<String> unlockedItems = new HashSet<>();
    private final Map<String, Integer> mobOrderMap = new HashMap<>();
    private final Map<String, Integer> itemOrderMap = new HashMap<>();
    private final Set<Integer> claimedRewards = new HashSet<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    public boolean isMobUnlocked(String mob) {
        return unlockedMobs.contains(mob.toLowerCase());
    }

    public boolean isItemUnlocked(String item) {
        return unlockedItems.contains(item.toLowerCase());
    }

    public void unlockMob(String mob, int order) {
        String clean = mob.toLowerCase();
        unlockedMobs.add(clean);
        mobOrderMap.put(clean, order);
    }

    public void unlockItem(String item, int order) {
        String clean = item.toLowerCase();
        unlockedItems.add(clean);
        itemOrderMap.put(clean, order);
    }

    public int getMobOrder(String mob) {
        return mobOrderMap.getOrDefault(mob.toLowerCase(), 0);
    }

    public int getItemOrder(String item) {
        return itemOrderMap.getOrDefault(item.toLowerCase(), 0);
    }

    public Set<String> getUnlockedMobs() { return Collections.unmodifiableSet(unlockedMobs); }
    public Set<String> getUnlockedItems() { return Collections.unmodifiableSet(unlockedItems); }
    public Map<String, Integer> getMobOrderMap() { return Collections.unmodifiableMap(mobOrderMap); }
    public Map<String, Integer> getItemOrderMap() { return Collections.unmodifiableMap(itemOrderMap); }

    public int getTotalUnlockedCount() {
        return unlockedMobs.size() + unlockedItems.size();
    }

    public boolean isRewardClaimed(int percent) {
        return claimedRewards.contains(percent);
    }

    public void setRewardClaimed(int percent) {
        claimedRewards.add(percent);
    }

    public Set<Integer> getClaimedRewards() { return Collections.unmodifiableSet(claimedRewards); }
}
