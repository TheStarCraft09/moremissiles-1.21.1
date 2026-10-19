# Changelog

## 1.0.0

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

### New: Hunter Spear (active radar homing missile)

The **Hunter Spear** is a fire-and-forget missile, loaded into the **Hunter Spear Missile Hardpoint**. Unlike the Radar Spear, which needs the RCN's lock for its whole flight, the Hunter only takes the target over **at launch** and then guides itself with its own seeker.

| | Radar Spear (semi-active) | Hunter Spear (active) |
|---|---|---|
| Guidance | Follows the RCN's lock the whole flight | Own seeker after launch |
| Top speed | ~8 blocks/tick | ~10 blocks/tick |
| Flight time | 6.5 s | 13 s |
| Seeker field of view | – | 60° (30° either side) |
| Turn rate | – | 21°/s |
| Seeker range | – | 2000 blocks (configurable) |

- **Launch with a lock:** the missile takes over the RCN's locked aircraft.
- **Launch without a lock (maddog):** the missile picks the **nearest aircraft** that enters its field of view. If it hasn't found one **1.5 s after reaching top speed**, it goes dumb. It never targets the aircraft it was launched from.
- **Losing the target:** if the target leaves the 60° field of view or the seeker range, the missile goes **dumb for good**. It flies straight until its fuel runs out and doesn't re-acquire.
- **Warhead:** explodes on impact, by **proximity fuse** within about 6 blocks of the target aircraft, underwater, or at the end of its flight. WariumCE's missile warhead with a slightly **larger blast** on top.
- **Long range:** the missile keeps the chunks along its flight path loaded, so it doesn't freeze between distant players.
- **Launched from an aircraft:** it starts with the aircraft's velocity and in the direction the hardpoint is facing on the aircraft.
- **RWR:** a Create: Radars RWR on the target beeps **rapidly** while a Hunter is homing on it, even if no radar has the aircraft locked. It stops when the missile goes dumb, hits or runs out of fuel.

### New: long-range radar

Create: Radars caps its plane radar at 1000 blocks. This mod raises it to **2000 blocks**.

Both ranges live in this mod's **server config** (`serverconfig/more_missiles-server.toml` in your world folder):

- `hunterSeekerRange`: the Hunter Spear's seeker range (default 2000).
- `planeRadarRange`: the plane radar range (default 2000; 0 keeps Create: Radars' own setting).

**Tip for long-range fights:** Create: Radars radars only see targets within **±20 blocks of their own height** by default. Raise `radarYScanRange` in Create: Radars' server config (`create_radar-server.toml`), otherwise distant aircraft at other altitudes stay invisible.

### Survival

Everything is now craftable, using WariumCE parts:

| Item | Recipe |
|---|---|
| Hunter Spear Missile | Shaped, like WariumCE's Radar Spear: Radar Component, 4 Aluminum Plates, Fire Spear Rocket, Bent Component, Advanced Component |
| Radar Control Node | Shaped: 5 Aluminum Plates, 2 Create Redstone Links, 2 Tech Components, Advanced Component (only with Create + Create: Radars) |
| Radar Toolkit | Shaped, diagonal: Tech Component, Aluminum Plate, Steel Component |
| Aluminum Wing Hardpoint | Shapeless: WariumCE Missile Hardpoint + 2 Aluminum Plates |
| Dark Aluminum Wing Hardpoint | Shapeless: Aluminum Wing Hardpoint + Gray Dye |
| Wingtip Missile Hardpoint | Shapeless: WariumCE Missile Hardpoint + Aluminum Plate + Steel Component |
| Hunter Spear Missile Hardpoint | Shapeless: WariumCE Missile Hardpoint + Aluminum Plate + 2 Steel Components |

- All hardpoints and the Radar Control Node now **drop themselves** when broken (a loaded missile drops too), and are mined faster with a pickaxe.

### Fixes

- Radar Spears fired from **Wingtip hardpoints** now receive the target and are guided. Before, they always flew unguided.
- Hardpoints now read targets and triggers in the same format WariumCE uses, so this mod's and WariumCE's hardpoints work together on one RCN.
- Hardpoints now show their proper names instead of translation keys.
- The Radar Control Node item now shows as a 3D block in inventories and in hand.

### Known limitations

These come from the current design. Please read them before reporting a bug.

- **Shared frequencies react together:** RCNs with the **same** frequencies within Create's link range (256 blocks by default) all respond to the same buttons. That includes RCNs left on the default frequencies. Change the frequencies if you want them to be independent.
- **Aircraft only:** the RCN only locks onto aircraft and ships (Sable sub-levels). Mobs, players and other radar contacts are ignored.
- **Radar Spear: no lock, no guidance:** if you fire a Radar Spear without a lock, or the lock is lost mid-flight, it flies unguided. (The Hunter Spear searches on its own instead.)
- **Marker visibility:** the lock marker is shown to **everyone** within 32 blocks of the RCN, not just the pilot. Players further away get no marker and no messages.
- **Very fast presses:** two "Fire" presses within about 2 game ticks (0.1 s) count as one.
- **Missile warning only for this mod's missiles:** the rapid RWR beeping works for Hunter Spears, and for Radar Spears fired from this mod's hardpoints through an RCN. Missiles from WariumCE's own hardpoints, IR/Seeker and Strike Spears, and Radar Spears fired by plain redstone only trigger the normal lock warning.
- **Hunter turning circle:** at full speed the Hunter needs a turning radius of about 550 blocks. Targets that turn hard at close range will escape it.
- **Long-range performance:** radars with a 2000-block range check a large area on every scan, and every Hunter in flight keeps a few chunks loaded. On busy servers, consider lowering the ranges in the server config.
- **One channel at a time:** the RCN fires only its current channel. Firing a different group means switching channels first.
