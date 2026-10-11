# Changelog

## 0.0.7

### New: Radar Control Node (Create: Radars compatibility)

The **Radar Control Node (RCN)** connects a radar from **Create: Radars** to your missile hardpoints. It picks a target from the radar, passes its position to the linked hardpoints, and fires them when you press a button. You control it remotely with Create's **Linked Controller** or **Redstone Links**.

The RCN only exists when **both Create and Create: Radars** are installed. Without them, the rest of the mod works as before.

#### Setup

You need: a Create: Radars radar, a Radar Control Node, one or more missile hardpoints, and the **Radar Toolkit**.

1. **Link the radar to the RCN:** with the Radar Toolkit in linking mode, **right-click the radar** to copy its position, then **sneak + right-click the RCN** to save it.
2. **Link the RCN to each hardpoint:** **right-click the RCN** to copy its position, then **sneak + right-click each hardpoint**.
3. **Pick a channel per hardpoint (optional):** switch the toolkit to configuring mode (right-click into the air) and right-click a hardpoint to step through channels **0–15**. The current channel is shown above your hotbar.
4. **Set the frequencies (optional):** **right-click the RCN with an empty hand** to open its frequency screen (see below).
5. **Set up your transmitters** with the same frequencies.

Works with this mod's Wing, Dark Wing and Wingtip hardpoints, and with WariumCE's own missile hardpoints.

#### Frequencies

Each function listens on its own frequency pair, just like a Create Redstone Link. **Right-click the RCN with an empty hand** to open the frequency screen:

- Each row (Next target, Previous target, Fire, Channel up, Channel down) has two slots: first and second frequency.
- Click an item from your inventory into a slot to set it. The item isn't used up. Click a slot with an empty hand to clear it.
- The **reset** button restores the defaults below. The **check** button closes the screen.
- The new frequencies take effect when you close the screen.

Give each RCN its own frequencies, so that several RCNs (or players) near each other don't control each other's RCN.

New RCNs start with these defaults:

| Function | First slot | Second slot |
|---|---|---|
| Next target | Redstone Block | Gold Ingot |
| Previous target | Redstone Block | Diamond |
| Fire | Redstone Block | Iron Ingot |
| Channel up | Redstone Block | Cobblestone |
| Channel down | Redstone Block | Stone |

- **Linked Controller:** the **top** slot of a button is the first slot, the **bottom** slot the second.
- **Redstone Link on a wall:** the **upper** slot is the first slot. If a Redstone Link on a floor or ceiling doesn't react, swap the two items.
- Make sure it's a **Redstone Block**, not Redstone Dust. They count as different frequencies.

#### How it behaves

- **Targeting:** "Next/Previous target" cycles through the aircraft the radar currently sees. The lock follows that aircraft until the radar loses it.
- **Channels:** the RCN has one active channel (0–15). "Fire" triggers every hardpoint set to that channel.
- **Firing:** each hardpoint fires **one missile per press**. Several hardpoints on the same channel fire together (a salvo).
- **Lock marker:** players within **32 blocks** of the RCN see a green diamond on the locked target. It's visible through blocks.
- **Radar Warning Receiver:** a Create: Radars RWR on the locked aircraft plays its lock warning while an RCN is locked onto it, and stops when the lock is dropped or lost.
- **Missile warning:** while a **Radar Spear** fired through an RCN is in flight towards the aircraft, its RWR beeps **rapidly**. The rapid beeping stops as soon as the missile hits, explodes or runs out of fuel, or when the RCN drops the lock.
- **Feedback:** players within 32 blocks get short messages above the hotbar (channel, target locked or lost, fired, "no radar linked", …).
- **Status:** **sneak + right-click the RCN with an empty hand** to see its radar link, number of targets, current lock, channel and the signal on each input.

### Fixes

- Radar Spears fired from **Wingtip hardpoints** now receive the target and are guided. Before, they always flew unguided.
- Hardpoints now read targets and triggers in the same format WariumCE uses, so this mod's and WariumCE's hardpoints work together on one RCN.

### Known limitations

These come from the current design. Please read them before reporting a bug.

- **Shared frequencies react together:** RCNs with the **same** frequencies within Create's link range (256 blocks by default) all respond to the same buttons. That includes RCNs left on the default frequencies. Change the frequencies if you want them to be independent.
- **Aircraft only:** the RCN only locks onto aircraft and ships (Sable sub-levels). Mobs, players and other radar contacts are ignored.
- **No lock, no guidance:** if you fire without a lock, or the lock is lost mid-flight, missiles fly unguided.
- **Marker visibility:** the lock marker is shown to **everyone** within 32 blocks of the RCN, not just the pilot. Players further away get no marker and no messages.
- **Very fast presses:** two "Fire" presses within about 2 game ticks (0.1 s) count as one.
- **Missile warning only for this mod's hardpoints:** the rapid RWR beeping only works for Radar Spears fired from this mod's hardpoints through an RCN. Missiles from WariumCE's own hardpoints, IR/Seeker and Strike Spears, and missiles fired by plain redstone only trigger the normal lock warning.
- **One channel at a time:** the RCN fires only its current channel. Firing a different group means switching channels first.
- **No crafting recipe yet:** the Radar Control Node is creative-only for now.
