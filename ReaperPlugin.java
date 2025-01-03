package net.runelite.client.plugins.example;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@PluginDescriptor(
		name = "Reaper Plugin",
		description = "WIP, Tileman Mode that lets you traverse the game based on enemies killed."
)
public class ReaperPlugin extends Plugin
{
	@Inject
	private Client client;

	private final Set<Integer> npcIdsInRenderDistance = new HashSet<>();

	private static final int MAX_ATTACK_DISTANCE = 10; //max weapon range
	private static final int MELEE_ATTACK_RANGE = 2;

	private static int tilecount = 0;

	@Override
	protected void startUp() throws Exception
	{
		System.out.println("Reaper Plugin started.");
	}

	@Override
	protected void shutDown() throws Exception
	{
		npcIdsInRenderDistance.clear();
		System.out.println("Reaper Plugin stopped.");
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		int npcCombatLevel = npc.getCombatLevel();
		if (npcCombatLevel <= 0) return;

		WorldPoint npcLocation = npc.getWorldLocation();
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		int distance = playerLocation.distanceTo(npcLocation);

		if (distance <= MAX_ATTACK_DISTANCE)
		{
			npcIdsInRenderDistance.add(npc.getIndex());
			System.out.println("NPC spawned within 10 unit range: " + npc.getId());
		}

		String npcName = npc.getName();
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "NPC Spawned: " + npcName, null);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc == null) return;
		if (!npc.isDead()) return;

		WorldPoint npcLocation = npc.getWorldLocation();
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		int distance = playerLocation.distanceTo(npcLocation);

		if (distance < MELEE_ATTACK_RANGE) { //hardwiring that the player must be next to the dying NPC to count

			if (npcIdsInRenderDistance.contains(npc.getIndex())) {
				npcIdsInRenderDistance.remove(npc.getIndex());

				for (int i = 0; i < npc.getCombatLevel(); i++){
					tilecount++;
				}

				String message = "NPC with ID " + npc.getId() + " " + npc.getIndex() + " " + npc.getName() + " has died or despawned!";
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
				System.out.println("NPC Dead or Despawned: " + npc.getId() + " " + npc.getName());
				System.out.println("tiles available: " + tilecount);
			}
		}
	}
